package recovery30.server.member.getmember;

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
import recovery30.server.member.domain.Member;
import recovery30.server.member.internal.MemberRepository;
import recovery30.server.shared.exception.BusinessException;
import recovery30.server.shared.exception.ErrorCode;
import recovery30.server.shared.response.ApiError;
import recovery30.server.shared.response.ApiResponse;

/** '회원 단건 조회' 슬라이스. */
@RestController
@RequestMapping("/api/members")
@Tag(name = "Member", description = "회원 관련 API")
public class GetMemberHandler {

  private final MemberRepository memberRepository;

  public GetMemberHandler(MemberRepository memberRepository) {
    this.memberRepository = memberRepository;
  }

  @Operation(summary = "회원 단건 조회", description = "회원 ID로 회원 정보를 조회한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 회원",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<MemberView>> handle(
      @Parameter(description = "회원 ID", example = "1") @PathVariable Long id) {
    Member member =
        memberRepository
            .findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

    MemberView view =
        new MemberView(member.getId(), member.getEmail().getValue(), member.getNickname());
    return ResponseEntity.ok(ApiResponse.success(view));
  }
}
