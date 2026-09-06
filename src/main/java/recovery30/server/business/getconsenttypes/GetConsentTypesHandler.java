package recovery30.server.business.getconsenttypes;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import recovery30.server.business.internal.ConsentTypeRepository;
import recovery30.server.shared.response.ApiResponse;

/** '동의 항목 마스터 조회' 슬라이스. 분리 동의 / 동의 관리 화면. */
@RestController
@RequestMapping("/api/consent-types")
@Tag(name = "Consent", description = "동의 (항목 마스터·현재 상태·grant/withdraw)")
public class GetConsentTypesHandler {

  private final ConsentTypeRepository consentTypeRepository;

  public GetConsentTypesHandler(ConsentTypeRepository consentTypeRepository) {
    this.consentTypeRepository = consentTypeRepository;
  }

  @Operation(summary = "동의 항목 마스터 조회", description = "필수 항목이 먼저 오도록 정렬된 동의 항목 전체 (목적·범위·철회 영향 포함).")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공")
  })
  @GetMapping
  public ResponseEntity<ApiResponse<List<ConsentTypeView>>> handle() {
    List<ConsentTypeView> views =
        consentTypeRepository.findAllByOrderByRequiredDescCodeAsc().stream()
            .map(
                t ->
                    new ConsentTypeView(
                        t.getCode(),
                        t.getName(),
                        t.isRequired(),
                        t.getPurpose(),
                        t.getDataScope(),
                        t.getWithdrawEffect(),
                        t.getVersion()))
            .toList();
    return ResponseEntity.ok(ApiResponse.success(views));
  }
}
