package recovery30.server.consultation.getcounselorslots;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import recovery30.server.consultation.domain.CounselorSlot;
import recovery30.server.consultation.domain.CounselorSlotStatus;
import recovery30.server.consultation.internal.CounselorRepository;
import recovery30.server.consultation.internal.CounselorSlotRepository;
import recovery30.server.shared.exception.BusinessException;
import recovery30.server.shared.exception.ErrorCode;
import recovery30.server.shared.response.ApiError;
import recovery30.server.shared.response.ApiResponse;

/** '상담자 예약 가능 슬롯 조회' 슬라이스. 상담 예약 화면 "예약 가능 일시 선택". */
@RestController
@RequestMapping("/api/counselors")
@Tag(name = "Consultation", description = "상담 예약 (상담자·슬롯·예약)")
public class GetCounselorSlotsHandler {

  private final CounselorRepository counselorRepository;
  private final CounselorSlotRepository counselorSlotRepository;

  public GetCounselorSlotsHandler(
      CounselorRepository counselorRepository, CounselorSlotRepository counselorSlotRepository) {
    this.counselorRepository = counselorRepository;
    this.counselorSlotRepository = counselorSlotRepository;
  }

  @Operation(
      summary = "예약 가능 슬롯 조회",
      description = "상담자의 슬롯을 시작시각 순으로 반환한다. from/to(ISO-8601 UTC)로 기간을 좁힐 수 있다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공 (없으면 빈 배열)"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 상담자",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{counselorId}/slots")
  public ResponseEntity<ApiResponse<List<CounselorSlotView>>> handle(
      @Parameter(description = "상담자 ID", example = "1") @PathVariable Long counselorId,
      @Parameter(description = "조회 시작(UTC ISO)", example = "2025-07-14T00:00:00Z")
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          Instant from,
      @Parameter(description = "조회 끝(UTC ISO)", example = "2025-07-21T00:00:00Z")
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          Instant to) {
    if (!counselorRepository.existsById(counselorId)) {
      throw new BusinessException(ErrorCode.COUNSELOR_NOT_FOUND);
    }

    List<CounselorSlot> slots =
        (from != null && to != null)
            ? counselorSlotRepository.findByCounselorIdAndStartAtBetweenOrderByStartAtAsc(
                counselorId, from, to)
            : counselorSlotRepository.findByCounselorIdOrderByStartAtAsc(counselorId);

    List<CounselorSlotView> views =
        slots.stream()
            .map(
                s -> {
                  int remaining = s.getCapacity() - s.getBookedCount();
                  boolean bookable = !"BLOCKED".equals(s.getStatus()) && remaining > 0;
                  return new CounselorSlotView(
                      s.getId(),
                      s.getStartAt(),
                      s.getEndAt(),
                      s.getCapacity(),
                      s.getBookedCount(),
                      remaining,
                      CounselorSlotStatus.valueOf(s.getStatus()),
                      bookable);
                })
            .toList();
    return ResponseEntity.ok(ApiResponse.success(views));
  }
}
