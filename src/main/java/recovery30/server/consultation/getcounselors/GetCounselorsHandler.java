package recovery30.server.consultation.getcounselors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import recovery30.server.consultation.internal.CounselorRepository;
import recovery30.server.shared.response.ApiResponse;

/** '상담자 목록 조회' 슬라이스. 상담 예약 화면에서 상담자·기관 선택용. MVP는 목데이터 시드. */
@RestController
@RequestMapping("/api/counselors")
@Tag(name = "Consultation", description = "상담 예약 (상담자·슬롯·예약)")
public class GetCounselorsHandler {

  private final CounselorRepository counselorRepository;

  public GetCounselorsHandler(CounselorRepository counselorRepository) {
    this.counselorRepository = counselorRepository;
  }

  @Operation(summary = "상담자 목록 조회", description = "예약 가능한 상담자 전체를 반환한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공 (없으면 빈 배열)")
  })
  @GetMapping
  public ResponseEntity<ApiResponse<List<CounselorView>>> handle() {
    List<CounselorView> views =
        counselorRepository.findAllByOrderByIdAsc().stream()
            .map(
                c ->
                    new CounselorView(
                        c.getId(), c.getName(), c.getInstitution(), c.getBranch(), c.getRole()))
            .toList();
    return ResponseEntity.ok(ApiResponse.success(views));
  }
}
