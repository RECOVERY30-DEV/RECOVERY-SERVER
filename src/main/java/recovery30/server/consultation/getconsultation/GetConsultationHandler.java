package recovery30.server.consultation.getconsultation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import recovery30.server.consultation.domain.Consultation;
import recovery30.server.consultation.domain.ConsultationChannel;
import recovery30.server.consultation.domain.ConsultationStatus;
import recovery30.server.consultation.internal.ConsultationOptionRepository;
import recovery30.server.consultation.internal.ConsultationRepository;
import recovery30.server.consultation.internal.CounselorRepository;
import recovery30.server.shared.exception.BusinessException;
import recovery30.server.shared.exception.ErrorCode;
import recovery30.server.shared.response.ApiError;
import recovery30.server.shared.response.ApiResponse;

/** '상담 예약 조회' 슬라이스. 상담 예약 확인 화면. */
@RestController
@RequestMapping("/api/consultations")
@Tag(name = "Consultation", description = "상담 예약 (상담자·슬롯·예약)")
public class GetConsultationHandler {

  private final ConsultationRepository consultationRepository;
  private final ConsultationOptionRepository consultationOptionRepository;
  private final CounselorRepository counselorRepository;

  public GetConsultationHandler(
      ConsultationRepository consultationRepository,
      ConsultationOptionRepository consultationOptionRepository,
      CounselorRepository counselorRepository) {
    this.consultationRepository = consultationRepository;
    this.consultationOptionRepository = consultationOptionRepository;
    this.counselorRepository = counselorRepository;
  }

  @Operation(summary = "상담 예약 조회", description = "예약 ID로 예약 정보와 다룰 회복안 목록을 반환한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 상담 예약",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{consultationId}")
  public ResponseEntity<ApiResponse<ConsultationView>> handle(
      @Parameter(description = "상담 예약 ID", example = "8") @PathVariable Long consultationId) {
    Consultation c =
        consultationRepository
            .findById(consultationId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CONSULTATION_NOT_FOUND));

    List<Long> optionIds =
        consultationOptionRepository.findByConsultationIdOrderByIdAsc(consultationId).stream()
            .map(o -> o.getRecoveryOptionId())
            .toList();

    String counselorName =
        c.getCounselorId() == null
            ? null
            : counselorRepository.findById(c.getCounselorId()).map(x -> x.getName()).orElse(null);

    ConsultationView view =
        new ConsultationView(
            c.getId(),
            c.getBusinessId(),
            c.getPacketId(),
            c.getCounselorId(),
            counselorName,
            ConsultationChannel.valueOf(c.getChannel()),
            c.getScheduledAt(),
            c.getPurposeText(),
            c.getPreQuestion(),
            c.isTransferConsentGranted(),
            ConsultationStatus.valueOf(c.getStatus()),
            optionIds,
            c.getFinalDecision(),
            c.getResultNote());
    return ResponseEntity.ok(ApiResponse.success(view));
  }
}
