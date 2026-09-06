package recovery30.server.packet.createpacket;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import recovery30.server.forecast.api.ForecastApi;
import recovery30.server.packet.domain.PacketStatus;
import recovery30.server.packet.domain.RecoveryPacket;
import recovery30.server.packet.internal.RecoveryPacketRepository;
import recovery30.server.shared.exception.BusinessException;
import recovery30.server.shared.exception.ErrorCode;
import recovery30.server.shared.response.ApiError;
import recovery30.server.shared.response.ApiResponse;
import tools.jackson.databind.ObjectMapper;

/**
 * 'Packet 새 버전 생성' 슬라이스. 회복안/셀프 액션 저장 후. 기존 Packet 을 덮어쓰지 않고 version+1 새 행을 만들며 supersedesPacketId
 * 로 이전 버전을 연결한다.
 */
@RestController
@RequestMapping("/api/forecasts")
@Tag(name = "Packet", description = "Recovery Packet (조회·버전 생성·전송)")
public class CreatePacketHandler {

  private final ForecastApi forecastApi;
  private final RecoveryPacketRepository recoveryPacketRepository;
  private final ObjectMapper objectMapper;

  public CreatePacketHandler(
      ForecastApi forecastApi,
      RecoveryPacketRepository recoveryPacketRepository,
      ObjectMapper objectMapper) {
    this.forecastApi = forecastApi;
    this.recoveryPacketRepository = recoveryPacketRepository;
    this.objectMapper = objectMapper;
  }

  @Operation(
      summary = "Packet 새 버전 생성",
      description = "덮어쓰지 않고 version+1 로 새 Packet 을 만든다. 이전 최신 버전이 있으면 supersedesPacketId 로 연결한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "생성 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "snapshot 누락",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 예측 실행",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping("/{forecastRunId}/packets")
  @Transactional
  public ResponseEntity<ApiResponse<CreatedPacketView>> handle(
      @Parameter(description = "예측 실행 ID", example = "1") @PathVariable Long forecastRunId,
      @RequestBody CreatePacketCommand command) {
    if (command.snapshot() == null || command.snapshot().isNull()) {
      throw new BusinessException(ErrorCode.INVALID_INPUT);
    }
    Long businessId =
        forecastApi
            .findBusinessId(forecastRunId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FORECAST_NOT_FOUND));

    RecoveryPacket previous =
        recoveryPacketRepository
            .findTopByBusinessIdAndForecastRunIdOrderByVersionDesc(businessId, forecastRunId)
            .orElse(null);

    RecoveryPacket packet = new RecoveryPacket();
    packet.setBusinessId(businessId);
    packet.setForecastRunId(forecastRunId);
    packet.setVersion(previous == null ? 1 : previous.getVersion() + 1);
    packet.setSupersedesPacketId(previous == null ? null : previous.getId());
    packet.setSnapshotJson(objectMapper.writeValueAsString(command.snapshot()));
    packet.setStatus("DRAFT");
    packet.setGeneratedAt(Instant.now());
    packet = recoveryPacketRepository.save(packet);

    CreatedPacketView view =
        new CreatedPacketView(
            packet.getId(),
            packet.getVersion(),
            packet.getSupersedesPacketId(),
            PacketStatus.valueOf(packet.getStatus()),
            packet.getGeneratedAt());
    return ResponseEntity.ok(ApiResponse.success(view));
  }
}
