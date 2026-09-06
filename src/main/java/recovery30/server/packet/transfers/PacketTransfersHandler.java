package recovery30.server.packet.transfers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import recovery30.server.packet.domain.PacketTransfer;
import recovery30.server.packet.domain.RecoveryPacket;
import recovery30.server.packet.internal.PacketTransferRepository;
import recovery30.server.packet.internal.RecoveryPacketRepository;
import recovery30.server.shared.exception.BusinessException;
import recovery30.server.shared.exception.ErrorCode;
import recovery30.server.shared.response.ApiError;
import recovery30.server.shared.response.ApiResponse;
import tools.jackson.databind.ObjectMapper;

/** 'Packet 전송 이력 조회·생성' 슬라이스. Recovery Packet 화면 "전송 상태", 상담 예약 "정보 전송". */
@RestController
@RequestMapping("/api/packets")
@Tag(name = "Packet", description = "Recovery Packet (조회·버전 생성·전송)")
public class PacketTransfersHandler {

  private final RecoveryPacketRepository recoveryPacketRepository;
  private final PacketTransferRepository packetTransferRepository;
  private final ObjectMapper objectMapper;

  public PacketTransfersHandler(
      RecoveryPacketRepository recoveryPacketRepository,
      PacketTransferRepository packetTransferRepository,
      ObjectMapper objectMapper) {
    this.recoveryPacketRepository = recoveryPacketRepository;
    this.packetTransferRepository = packetTransferRepository;
    this.objectMapper = objectMapper;
  }

  @Operation(summary = "Packet 전송 이력 조회", description = "해당 Packet 의 상담자 전송 이력을 시간순으로 반환한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공 (없으면 빈 배열)"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 Packet",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{packetId}/transfers")
  public ResponseEntity<ApiResponse<List<PacketTransferView>>> list(
      @Parameter(description = "Packet ID", example = "1") @PathVariable Long packetId) {
    if (!recoveryPacketRepository.existsById(packetId)) {
      throw new BusinessException(ErrorCode.PACKET_NOT_FOUND);
    }
    List<PacketTransferView> views =
        packetTransferRepository.findByPacketIdOrderByIdAsc(packetId).stream()
            .map(t -> PacketTransferView.of(t, objectMapper))
            .toList();
    return ResponseEntity.ok(ApiResponse.success(views));
  }

  @Operation(
      summary = "Packet 상담자 전송",
      description = "전송 이력 1건을 추가하고 Packet 상태를 SENT 로 바꾼다. 전송 동의(consentId) 없이는 전송할 수 없다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "전송 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "consentId 누락",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 Packet",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping("/{packetId}/transfers")
  @Transactional
  public ResponseEntity<ApiResponse<PacketTransferView>> create(
      @Parameter(description = "Packet ID", example = "1") @PathVariable Long packetId,
      @RequestBody CreateTransferCommand command) {
    RecoveryPacket packet =
        recoveryPacketRepository
            .findById(packetId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PACKET_NOT_FOUND));
    if (command.consentId() == null) {
      throw new BusinessException(ErrorCode.INVALID_INPUT);
    }

    Instant now = Instant.now();
    PacketTransfer transfer = new PacketTransfer();
    transfer.setPacketId(packetId);
    transfer.setCounselorId(command.counselorId());
    transfer.setChannel(command.channel());
    transfer.setScopeJson(
        command.scope() == null ? "{}" : objectMapper.writeValueAsString(command.scope()));
    transfer.setConsentId(command.consentId());
    transfer.setSentAt(now);
    transfer = packetTransferRepository.save(transfer);

    if (!"SENT".equals(packet.getStatus())) {
      packet.setStatus("SENT");
      packet.setSentAt(now);
      recoveryPacketRepository.save(packet);
    }

    return ResponseEntity.ok(ApiResponse.success(PacketTransferView.of(transfer, objectMapper)));
  }
}
