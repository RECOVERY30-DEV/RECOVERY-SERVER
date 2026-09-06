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

/** '특정 Packet 조회' 슬라이스. Recovery Packet 화면 (버전 링크로 이전 버전 열람). */
@RestController
@RequestMapping("/api/packets")
@Tag(name = "Packet", description = "Recovery Packet (조회·버전 생성·전송)")
public class GetPacketHandler {

  private final RecoveryPacketRepository recoveryPacketRepository;
  private final ObjectMapper objectMapper;

  public GetPacketHandler(
      RecoveryPacketRepository recoveryPacketRepository, ObjectMapper objectMapper) {
    this.recoveryPacketRepository = recoveryPacketRepository;
    this.objectMapper = objectMapper;
  }

  @Operation(summary = "특정 Packet 조회", description = "Packet ID로 동결된 snapshot과 버전·전송 상태를 반환한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 Packet",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{packetId}")
  public ResponseEntity<ApiResponse<PacketView>> handle(
      @Parameter(description = "Packet ID", example = "1") @PathVariable Long packetId) {
    RecoveryPacket packet =
        recoveryPacketRepository
            .findById(packetId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PACKET_NOT_FOUND));
    return ResponseEntity.ok(ApiResponse.success(PacketView.of(packet, objectMapper)));
  }
}
