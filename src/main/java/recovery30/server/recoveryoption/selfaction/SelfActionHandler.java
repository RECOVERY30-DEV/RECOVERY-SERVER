package recovery30.server.recoveryoption.selfaction;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import recovery30.server.forecast.api.ForecastApi;
import recovery30.server.recoveryoption.domain.SelfActionItem;
import recovery30.server.recoveryoption.domain.SelfActionPlan;
import recovery30.server.recoveryoption.internal.RecoveryOptionRepository;
import recovery30.server.recoveryoption.internal.SelfActionItemRepository;
import recovery30.server.recoveryoption.internal.SelfActionPlanRepository;
import recovery30.server.recoveryoption.selfaction.SelfActionPlanView.SelfActionItemView;
import recovery30.server.shared.exception.BusinessException;
import recovery30.server.shared.exception.ErrorCode;
import recovery30.server.shared.response.ApiError;
import recovery30.server.shared.response.ApiResponse;

/** 자체 실행 계획 조회·저장 + 준비 항목 수정. 셀프 액션 저장 화면, 사후점검 화면 "회복안 실행 상태". */
@RestController
@RequestMapping("/api/forecasts")
@Tag(name = "RecoveryOption", description = "회복안 비교 (회복안·시나리오·선택)")
public class SelfActionHandler {

  private final ForecastApi forecastApi;
  private final RecoveryOptionRepository recoveryOptionRepository;
  private final SelfActionPlanRepository planRepository;
  private final SelfActionItemRepository itemRepository;

  public SelfActionHandler(
      ForecastApi forecastApi,
      RecoveryOptionRepository recoveryOptionRepository,
      SelfActionPlanRepository planRepository,
      SelfActionItemRepository itemRepository) {
    this.forecastApi = forecastApi;
    this.recoveryOptionRepository = recoveryOptionRepository;
    this.planRepository = planRepository;
    this.itemRepository = itemRepository;
  }

  @Operation(summary = "자체 실행 계획 목록 조회", description = "예측 실행에 저장된 자체 실행 계획과 준비 항목을 반환한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 예측 실행",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{forecastRunId}/self-action-plans")
  public ResponseEntity<ApiResponse<List<SelfActionPlanView>>> list(
      @Parameter(description = "예측 실행 ID", example = "1") @PathVariable Long forecastRunId) {
    if (!forecastApi.forecastRunExists(forecastRunId)) {
      throw new BusinessException(ErrorCode.FORECAST_NOT_FOUND);
    }
    List<SelfActionPlan> plans = planRepository.findByForecastRunIdOrderByIdAsc(forecastRunId);
    Map<Long, List<SelfActionItem>> itemsByPlan =
        plans.isEmpty()
            ? Map.of()
            : itemRepository
                .findBySelfActionPlanIdInOrderByIdAsc(
                    plans.stream().map(SelfActionPlan::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(SelfActionItem::getSelfActionPlanId));

    List<SelfActionPlanView> views =
        plans.stream()
            .map(p -> SelfActionPlanView.of(p, itemsByPlan.getOrDefault(p.getId(), List.of())))
            .toList();
    return ResponseEntity.ok(ApiResponse.success(views));
  }

  @Operation(
      summary = "자체 실행 계획 저장",
      description = "선택한 회복안을 실행 계획으로 저장한다(status=ACTIVE). 준비 항목은 PENDING 으로 생성된다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "저장 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "회복안 ID 누락 / 준비 항목 제목 없음",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 예측 실행 또는 회복안",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping("/{forecastRunId}/self-action-plans")
  @Transactional
  public ResponseEntity<ApiResponse<SelfActionPlanView>> create(
      @Parameter(description = "예측 실행 ID", example = "1") @PathVariable Long forecastRunId,
      @RequestBody CreateSelfActionPlanCommand command) {
    if (command.recoveryOptionId() == null) {
      throw new BusinessException(ErrorCode.INVALID_INPUT);
    }
    Long businessId =
        forecastApi
            .findBusinessId(forecastRunId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FORECAST_NOT_FOUND));
    if (!recoveryOptionRepository.existsById(command.recoveryOptionId())) {
      throw new BusinessException(ErrorCode.RECOVERY_OPTION_NOT_FOUND);
    }

    SelfActionPlan plan = new SelfActionPlan();
    plan.setBusinessId(businessId);
    plan.setForecastRunId(forecastRunId);
    plan.setRecoveryOptionId(command.recoveryOptionId());
    plan.setExpectedEffectText(command.expectedEffectText());
    plan.setStatus("ACTIVE");
    plan.setSavedAt(Instant.now());
    plan = planRepository.save(plan);

    if (command.items() != null) {
      for (CreateSelfActionPlanCommand.NewItem in : command.items()) {
        if (in.title() == null || in.title().isBlank()) {
          throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        SelfActionItem item = new SelfActionItem();
        item.setSelfActionPlanId(plan.getId());
        item.setTitle(in.title());
        item.setTargetDate(in.targetDate());
        item.setStatus("PENDING");
        item.setMemo(in.memo());
        itemRepository.save(item);
      }
    }

    return ResponseEntity.ok(
        ApiResponse.success(
            SelfActionPlanView.of(
                plan, itemRepository.findBySelfActionPlanIdOrderByIdAsc(plan.getId()))));
  }

  @Operation(summary = "준비 항목 수정", description = "체크(PENDING/DONE)·예정일·메모 등을 부분 수정한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "수정 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "해당 예측 실행의 준비 항목이 아님",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PatchMapping("/{forecastRunId}/self-action-plans/items/{itemId}")
  @Transactional
  public ResponseEntity<ApiResponse<SelfActionItemView>> updateItem(
      @Parameter(description = "예측 실행 ID", example = "1") @PathVariable Long forecastRunId,
      @Parameter(description = "준비 항목 ID", example = "11") @PathVariable Long itemId,
      @RequestBody UpdateSelfActionItemCommand command) {
    SelfActionItem item =
        itemRepository
            .findById(itemId)
            .filter(
                i ->
                    planRepository
                        .findById(i.getSelfActionPlanId())
                        .map(p -> forecastRunId.equals(p.getForecastRunId()))
                        .orElse(false))
            .orElseThrow(() -> new BusinessException(ErrorCode.SELF_ACTION_ITEM_NOT_FOUND));

    if (command.title() != null) {
      item.setTitle(command.title());
    }
    if (command.targetDate() != null) {
      item.setTargetDate(command.targetDate());
    }
    if (command.status() != null) {
      item.setStatus(command.status().name());
    }
    if (command.memo() != null) {
      item.setMemo(command.memo());
    }
    return ResponseEntity.ok(ApiResponse.success(SelfActionItemView.of(itemRepository.save(item))));
  }
}
