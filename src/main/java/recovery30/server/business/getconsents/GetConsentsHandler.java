package recovery30.server.business.getconsents;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import recovery30.server.business.domain.Consent;
import recovery30.server.business.domain.ConsentStatus;
import recovery30.server.business.internal.BusinessRepository;
import recovery30.server.business.internal.ConsentRepository;
import recovery30.server.business.internal.ConsentTypeRepository;
import recovery30.server.shared.exception.BusinessException;
import recovery30.server.shared.exception.ErrorCode;
import recovery30.server.shared.response.ApiError;
import recovery30.server.shared.response.ApiResponse;

/** '사업자 동의 현재 상태 조회' 슬라이스. 동의 항목 마스터 전체에 사업자의 응답을 덮어 반환한다. */
@RestController
@RequestMapping("/api/businesses")
@Tag(name = "Consent", description = "동의 (항목 마스터·현재 상태·grant/withdraw)")
public class GetConsentsHandler {

  private final BusinessRepository businessRepository;
  private final ConsentTypeRepository consentTypeRepository;
  private final ConsentRepository consentRepository;

  public GetConsentsHandler(
      BusinessRepository businessRepository,
      ConsentTypeRepository consentTypeRepository,
      ConsentRepository consentRepository) {
    this.businessRepository = businessRepository;
    this.consentTypeRepository = consentTypeRepository;
    this.consentRepository = consentRepository;
  }

  @Operation(
      summary = "사업자 동의 현재 상태 조회",
      description = "동의 항목 전체를 반환하되, 사업자가 응답한 항목은 GRANTED/WITHDRAWN, 응답 이력이 없으면 NOT_SET.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 사업자",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{businessId}/consents")
  public ResponseEntity<ApiResponse<List<ConsentStatusView>>> handle(
      @Parameter(description = "사업자 ID", example = "1") @PathVariable Long businessId) {
    if (!businessRepository.existsById(businessId)) {
      throw new BusinessException(ErrorCode.BUSINESS_NOT_FOUND);
    }

    Map<String, Consent> byType =
        consentRepository.findByBusinessId(businessId).stream()
            .collect(
                Collectors.toMap(Consent::getConsentTypeCode, Function.identity(), (a, b) -> a));

    List<ConsentStatusView> views =
        consentTypeRepository.findAllByOrderByRequiredDescCodeAsc().stream()
            .map(
                t -> {
                  Consent c = byType.get(t.getCode());
                  if (c == null) {
                    return new ConsentStatusView(
                        t.getCode(),
                        t.getName(),
                        t.isRequired(),
                        ConsentStatus.NOT_SET,
                        null,
                        null,
                        null,
                        null);
                  }
                  Instant last = latest(c.getGrantedAt(), c.getWithdrawnAt());
                  return new ConsentStatusView(
                      t.getCode(),
                      t.getName(),
                      t.isRequired(),
                      ConsentStatus.valueOf(c.getStatus()),
                      c.getGrantedAt(),
                      c.getWithdrawnAt(),
                      last,
                      c.getConsentVersion());
                })
            .toList();
    return ResponseEntity.ok(ApiResponse.success(views));
  }

  private static Instant latest(Instant a, Instant b) {
    if (a == null) {
      return b;
    }
    if (b == null) {
      return a;
    }
    return a.isAfter(b) ? a : b;
  }
}
