package recovery30.server.business.updateconsent;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import recovery30.server.audit.api.AuditApi;
import recovery30.server.business.domain.Consent;
import recovery30.server.business.domain.ConsentStatus;
import recovery30.server.business.domain.ConsentType;
import recovery30.server.business.internal.BusinessRepository;
import recovery30.server.business.internal.ConsentRepository;
import recovery30.server.business.internal.ConsentTypeRepository;
import recovery30.server.shared.exception.BusinessException;
import recovery30.server.shared.exception.ErrorCode;
import recovery30.server.shared.response.ApiError;
import recovery30.server.shared.response.ApiResponse;

/**
 * '동의 grant/withdraw' 슬라이스. core_consents 는 항목당 최신 상태 1건으로 갱신(upsert)하고, 변경 이력은 AuditApi 로
 * audit_consent_logs 에 append 한다 (법적 증빙).
 */
@RestController
@RequestMapping("/api/businesses")
@Tag(name = "Consent", description = "동의 (항목 마스터·현재 상태·grant/withdraw)")
public class UpdateConsentHandler {

  private final BusinessRepository businessRepository;
  private final ConsentTypeRepository consentTypeRepository;
  private final ConsentRepository consentRepository;
  private final AuditApi auditApi;

  public UpdateConsentHandler(
      BusinessRepository businessRepository,
      ConsentTypeRepository consentTypeRepository,
      ConsentRepository consentRepository,
      AuditApi auditApi) {
    this.businessRepository = businessRepository;
    this.consentTypeRepository = consentTypeRepository;
    this.consentRepository = consentRepository;
    this.auditApi = auditApi;
  }

  @Operation(
      summary = "동의 항목 grant/withdraw",
      description = "granted=true면 동의, false면 철회. 요청자의 IP·User-Agent를 함께 기록하고 변경 이력을 append 한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "적용 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "granted 누락",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 사업자 또는 동의 항목",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PutMapping("/{businessId}/consents/{typeCode}")
  @Transactional
  public ResponseEntity<ApiResponse<UpdatedConsentView>> handle(
      @Parameter(description = "사업자 ID", example = "1") @PathVariable Long businessId,
      @Parameter(description = "동의 항목 코드", example = "PACKET_TRANSFER") @PathVariable
          String typeCode,
      @RequestBody UpdateConsentCommand command,
      HttpServletRequest request) {
    if (!businessRepository.existsById(businessId)) {
      throw new BusinessException(ErrorCode.BUSINESS_NOT_FOUND);
    }
    ConsentType type =
        consentTypeRepository
            .findById(typeCode)
            .orElseThrow(() -> new BusinessException(ErrorCode.CONSENT_TYPE_NOT_FOUND));
    if (command.granted() == null) {
      throw new BusinessException(ErrorCode.INVALID_INPUT);
    }

    ConsentStatus toStatus = command.granted() ? ConsentStatus.GRANTED : ConsentStatus.WITHDRAWN;
    String ip = request.getRemoteAddr();
    String ua = request.getHeader("User-Agent");
    Instant now = Instant.now();

    Consent consent =
        consentRepository
            .findByBusinessIdAndConsentTypeCode(businessId, typeCode)
            .orElseGet(
                () -> {
                  Consent c = new Consent();
                  c.setBusinessId(businessId);
                  c.setConsentTypeCode(typeCode);
                  return c;
                });
    String fromStatus = consent.getStatus();

    consent.setStatus(toStatus.name());
    consent.setConsentVersion(type.getVersion());
    consent.setIpAddress(ip);
    consent.setUserAgent(ua);
    if (toStatus == ConsentStatus.GRANTED) {
      consent.setGrantedAt(now);
    } else {
      consent.setWithdrawnAt(now);
    }
    consentRepository.save(consent);

    auditApi.appendConsentLog(
        businessId, typeCode, fromStatus, toStatus.name(), type.getVersion(), ip, ua);

    return ResponseEntity.ok(
        ApiResponse.success(new UpdatedConsentView(typeCode, toStatus, type.getVersion(), now)));
  }
}
