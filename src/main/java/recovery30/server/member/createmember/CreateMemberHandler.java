package recovery30.server.member.createmember;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import recovery30.server.shared.response.ApiError;
import recovery30.server.shared.response.ApiResponse;

/** '회원 가입' 슬라이스. 컨트롤러 + 서비스 + 레포 호출을 이 클래스 하나에서 처리한다. */
@RestController
@RequestMapping("/api/members")
@Tag(name = "Member", description = "회원 관련 API")
public class CreateMemberHandler {

  private final MemberRepository memberRepository;
  private final ApplicationEventPublisher eventPublisher;

  public CreateMemberHandler(
      MemberRepository memberRepository, ApplicationEventPublisher eventPublisher) {
    this.memberRepository = memberRepository;
    this.eventPublisher = eventPublisher;
  }

  @Operation(summary = "회원 가입", description = "이메일과 닉네임으로 새 회원을 생성한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "201",
        description = "가입 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "이메일 형식이 올바르지 않거나 닉네임이 비어있음",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping
  public ResponseEntity<ApiResponse<CreateMemberResponse>> handle(
      @RequestBody CreateMemberCommand command) {
    Member saved =
        memberRepository.save(new Member(new Email(command.email()), command.nickname()));

    eventPublisher.publishEvent(
        new MemberCreatedEvent(saved.getId(), saved.getEmail().getValue(), saved.getNickname()));

    CreateMemberResponse response =
        new CreateMemberResponse(saved.getId(), saved.getEmail().getValue(), saved.getNickname());
    return ResponseEntity.status(201).body(ApiResponse.success(response));
  }
}
