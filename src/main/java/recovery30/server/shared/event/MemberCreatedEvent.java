package recovery30.server.shared.event;

/** 회원 가입 완료 시 발행되는 이벤트. 다른 모듈이 member.api를 동기 호출하는 대신, 이 이벤트를 구독해서 비동기로 반응할 수 있다. */
public record MemberCreatedEvent(Long memberId, String email, String nickname) {}
