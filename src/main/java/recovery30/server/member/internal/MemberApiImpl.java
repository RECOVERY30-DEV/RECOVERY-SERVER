package recovery30.server.member.internal;

import java.util.Optional;
import org.springframework.stereotype.Component;
import recovery30.server.member.api.MemberApi;
import recovery30.server.member.api.MemberSummary;

/** MemberApi의 실제 구현체. 다른 모듈은 이 클래스가 아니라 MemberApi 인터페이스만 주입받아야 한다. */
@Component
public class MemberApiImpl implements MemberApi {

  private final MemberRepository memberRepository;

  public MemberApiImpl(MemberRepository memberRepository) {
    this.memberRepository = memberRepository;
  }

  @Override
  public Optional<MemberSummary> findSummary(Long memberId) {
    return memberRepository
        .findById(memberId)
        .map(m -> new MemberSummary(m.getId(), m.getNickname()));
  }

  @Override
  public boolean existsById(Long memberId) {
    return memberRepository.existsById(memberId);
  }
}
