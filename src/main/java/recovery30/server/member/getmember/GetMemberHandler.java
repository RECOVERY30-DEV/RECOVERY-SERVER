package recovery30.server.member.getmember;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import recovery30.server.member.domain.Member;
import recovery30.server.member.internal.MemberRepository;
import recovery30.server.shared.exception.BusinessException;
import recovery30.server.shared.exception.ErrorCode;
import recovery30.server.shared.response.ApiResponse;

/** '회원 단건 조회' 슬라이스. */
@RestController
@RequestMapping("/api/members")
public class GetMemberHandler {

  private final MemberRepository memberRepository;

  public GetMemberHandler(MemberRepository memberRepository) {
    this.memberRepository = memberRepository;
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<MemberView>> handle(@PathVariable Long id) {
    Member member =
        memberRepository
            .findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

    MemberView view =
        new MemberView(member.getId(), member.getEmail().getValue(), member.getNickname());
    return ResponseEntity.ok(ApiResponse.success(view));
  }
}
