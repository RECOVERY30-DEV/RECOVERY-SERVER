package recovery30.server.member.getmember;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import recovery30.server.member.internal.MemberRepository;

/** '회원 단건 조회' 슬라이스. */
@RestController
@RequestMapping("/api/members")
public class GetMemberHandler {

  private final MemberRepository memberRepository;

  public GetMemberHandler(MemberRepository memberRepository) {
    this.memberRepository = memberRepository;
  }

  @GetMapping("/{id}")
  public ResponseEntity<MemberView> handle(@PathVariable Long id) {
    return memberRepository
        .findById(id)
        .map(m -> new MemberView(m.getId(), m.getEmail().value(), m.getNickname()))
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }
}
