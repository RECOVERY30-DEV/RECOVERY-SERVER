package recovery30.server.consultation.bookconsultation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.Objects;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import recovery30.server.business.api.BusinessApi;
import recovery30.server.consultation.domain.Consultation;
import recovery30.server.consultation.domain.ConsultationChannel;
import recovery30.server.consultation.domain.ConsultationOption;
import recovery30.server.consultation.domain.ConsultationStatus;
import recovery30.server.consultation.domain.CounselorSlot;
import recovery30.server.consultation.internal.ConsultationOptionRepository;
import recovery30.server.consultation.internal.ConsultationRepository;
import recovery30.server.consultation.internal.CounselorRepository;
import recovery30.server.consultation.internal.CounselorSlotRepository;
import recovery30.server.shared.exception.BusinessException;
import recovery30.server.shared.exception.ErrorCode;
import recovery30.server.shared.response.ApiError;
import recovery30.server.shared.response.ApiResponse;

/** '상담 예약 생성' 슬라이스. 상담 예약 화면 최종 확인 → 예약. 전송 동의가 없어도 예약은 성립한다. */
@RestController
@RequestMapping("/api/businesses")
@Tag(name = "Consultation", description = "상담 예약 (상담자·슬롯·예약)")
public class BookConsultationHandler {

  private final BusinessApi businessApi;
  private final CounselorRepository counselorRepository;
  private final CounselorSlotRepository counselorSlotRepository;
  private final ConsultationRepository consultationRepository;
  private final ConsultationOptionRepository consultationOptionRepository;

  public BookConsultationHandler(
      BusinessApi businessApi,
      CounselorRepository counselorRepository,
      CounselorSlotRepository counselorSlotRepository,
      ConsultationRepository consultationRepository,
      ConsultationOptionRepository consultationOptionRepository) {
    this.businessApi = businessApi;
    this.counselorRepository = counselorRepository;
    this.counselorSlotRepository = counselorSlotRepository;
    this.consultationRepository = consultationRepository;
    this.consultationOptionRepository = consultationOptionRepository;
  }

  @Operation(
      summary = "상담 예약 생성",
      description =
          "slotId 를 주면 그 슬롯 시각으로 예약하고 잔여석을 1 줄인다(정원 마감 시 status=BOOKED). slotId 가 없으면 scheduledAt"
              + " 이 필수. 전송 동의 없이도 예약은 성립한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "예약 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "잘못된 채널 / 예약 불가 슬롯 / 일시 누락",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 상담자",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping("/{businessId}/consultations")
  @Transactional
  public ResponseEntity<ApiResponse<BookedConsultationView>> handle(
      @Parameter(description = "사업자 ID", example = "1") @PathVariable Long businessId,
      @RequestBody BookConsultationCommand command) {
    if (!businessApi.businessExists(businessId)) {
      throw new BusinessException(ErrorCode.INVALID_INPUT);
    }
    ConsultationChannel channel = parseChannel(command.channel());
    if (command.counselorId() != null && !counselorRepository.existsById(command.counselorId())) {
      throw new BusinessException(ErrorCode.COUNSELOR_NOT_FOUND);
    }

    Instant scheduledAt = command.scheduledAt();
    if (command.slotId() != null) {
      CounselorSlot slot =
          counselorSlotRepository
              .findById(command.slotId())
              .orElseThrow(() -> new BusinessException(ErrorCode.SLOT_NOT_BOOKABLE));
      if ("BLOCKED".equals(slot.getStatus()) || slot.getBookedCount() >= slot.getCapacity()) {
        throw new BusinessException(ErrorCode.SLOT_NOT_BOOKABLE);
      }
      scheduledAt = slot.getStartAt();
      slot.setBookedCount(slot.getBookedCount() + 1);
      if (slot.getBookedCount() >= slot.getCapacity()) {
        slot.setStatus("BOOKED");
      }
      counselorSlotRepository.save(slot);
    }
    if (scheduledAt == null) {
      throw new BusinessException(ErrorCode.INVALID_INPUT);
    }

    Consultation consultation = new Consultation();
    consultation.setBusinessId(businessId);
    consultation.setPacketId(command.packetId());
    consultation.setCounselorId(command.counselorId());
    consultation.setChannel(channel.name());
    consultation.setScheduledAt(scheduledAt);
    consultation.setPurposeText(command.purposeText());
    consultation.setPreQuestion(command.preQuestion());
    consultation.setTransferConsentGranted(Boolean.TRUE.equals(command.transferConsentGranted()));
    consultation.setStatus("REQUESTED");
    consultation = consultationRepository.save(consultation);

    if (command.recoveryOptionIds() != null) {
      for (Long optionId :
          command.recoveryOptionIds().stream().filter(Objects::nonNull).distinct().toList()) {
        ConsultationOption link = new ConsultationOption();
        link.setConsultationId(consultation.getId());
        link.setRecoveryOptionId(optionId);
        consultationOptionRepository.save(link);
      }
    }

    BookedConsultationView view =
        new BookedConsultationView(
            consultation.getId(),
            ConsultationStatus.valueOf(consultation.getStatus()),
            channel,
            consultation.getScheduledAt());
    return ResponseEntity.ok(ApiResponse.success(view));
  }

  private static ConsultationChannel parseChannel(String raw) {
    try {
      return ConsultationChannel.valueOf(raw);
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new BusinessException(ErrorCode.INVALID_CONSULTATION_CHANNEL);
    }
  }
}
