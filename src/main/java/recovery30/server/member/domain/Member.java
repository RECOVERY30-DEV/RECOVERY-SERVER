package recovery30.server.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import recovery30.server.shared.exception.BusinessException;
import recovery30.server.shared.exception.ErrorCode;

/** 회원 애그리거트 루트 (JPA 엔티티). */
@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
public class Member {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Embedded private Email email;

  @Column(nullable = false)
  private String nickname;

  @Column(name = "registered_at", updatable = false)
  private Instant registeredAt;

  public Member(Email email, String nickname) {
    if (nickname == null || nickname.isBlank()) {
      throw new BusinessException(ErrorCode.INVALID_NICKNAME);
    }
    this.email = email;
    this.nickname = nickname;
    this.registeredAt = Instant.now();
  }
}
