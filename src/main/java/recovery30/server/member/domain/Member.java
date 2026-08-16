package recovery30.server.member.domain;

import java.time.Instant;

/** 회원 애그리거트 루트. */
public class Member {

  private final Long id;
  private final Email email;
  private final String nickname;
  private final Instant registeredAt;

  private Member(Long id, Email email, String nickname, Instant registeredAt) {
    this.id = id;
    this.email = email;
    this.nickname = nickname;
    this.registeredAt = registeredAt;
  }

  public static Member register(Email email, String nickname) {
    if (nickname == null || nickname.isBlank()) {
      throw new IllegalArgumentException("nickname은 비어있을 수 없습니다");
    }
    return new Member(null, email, nickname, Instant.now());
  }

  /** 저장소가 id를 부여한 뒤 새 인스턴스를 만들 때 사용한다. */
  public Member withId(Long id) {
    return new Member(id, this.email, this.nickname, this.registeredAt);
  }

  public Long getId() {
    return id;
  }

  public Email getEmail() {
    return email;
  }

  public String getNickname() {
    return nickname;
  }

  public Instant getRegisteredAt() {
    return registeredAt;
  }
}
