package recovery30.server.member.createmember;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import recovery30.server.member.domain.Email;
import recovery30.server.member.domain.Member;
import recovery30.server.member.internal.MemberRepository;
import recovery30.server.shared.event.MemberCreatedEvent;

/** '회원 가입' 슬라이스. 컨트롤러 + 서비스 + 레포 호출을 이 클래스 하나에서 처리한다. */
@RestController
@RequestMapping("/api/members")
public class CreateMemberHandler {

  private final MemberRepository memberRepository;
  private final ApplicationEventPublisher eventPublisher;

  public CreateMemberHandler(
      MemberRepository memberRepository, ApplicationEventPublisher eventPublisher) {
    this.memberRepository = memberRepository;
    this.eventPublisher = eventPublisher;
  }

  @PostMapping
  public ResponseEntity<CreateMemberResponse> handle(@RequestBody CreateMemberCommand command) {
    Member saved =
        memberRepository.save(new Member(new Email(command.email()), command.nickname()));

    eventPublisher.publishEvent(
        new MemberCreatedEvent(saved.getId(), saved.getEmail().getValue(), saved.getNickname()));

    return ResponseEntity.status(201)
        .body(
            new CreateMemberResponse(
                saved.getId(), saved.getEmail().getValue(), saved.getNickname()));
  }
}
