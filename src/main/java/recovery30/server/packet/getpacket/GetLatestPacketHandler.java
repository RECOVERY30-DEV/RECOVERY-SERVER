package recovery30.server.packet.getpacket;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import recovery30.server.packet.domain.RecoveryPacket;
import recovery30.server.packet.internal.RecoveryPacketRepository;
import recovery30.server.shared.exception.BusinessException;
import recovery30.server.shared.exception.ErrorCode;
import recovery30.server.shared.response.ApiError;
import recovery30.server.shared.response.ApiResponse;
import tools.jackson.databind.ObjectMapper;

/** '최신 Packet 조회' 슬라이스. Recovery Packet 화면 진입점. */
@RestController
@RequestMapping("/api/businesses")
@Tag(name = "Packet", description = "Recovery Packet (조회·버전 생성·전송)")
public class GetLatestPacketHandler {

  private final RecoveryPacketRepository recoveryPacketRepository;
  private final ObjectMapper objectMapper;

  public GetLatestPacketHandler(
      RecoveryPacketRepository recoveryPacketRepository, ObjectMapper objectMapper) {
    this.recoveryPacketRepository = recoveryPacketRepository;
    this.objectMapper = objectMapper;
  }

  @Operation(
      summary = "최신 Packet 조회",
      description = "사업자의 가장 최근 버전 Packet 1건을 반환한다. Packet 이 아직 없으면 404.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "Packet 이력이 없음",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{businessId}/packets/latest")
  public ResponseEntity<ApiResponse<PacketView>> handle(
      @Parameter(description = "사업자 ID", example = "1") @PathVariable Long businessId) {
    RecoveryPacket packet =
        recoveryPacketRepository
            .findTopByBusinessIdOrderByVersionDesc(businessId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PACKET_NOT_FOUND));
    return ResponseEntity.ok(ApiResponse.success(PacketView.of(packet, objectMapper)));
  }
}
