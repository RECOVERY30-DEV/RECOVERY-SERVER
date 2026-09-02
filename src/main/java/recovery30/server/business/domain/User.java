package recovery30.server.business.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 로그인 계정 (core_users). */
@Entity
@Table(name = "core_users")
@Getter
@Setter
@NoArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String passwordHash;

  private String name;

  private String phone;

  @Column(nullable = false)
  private String status = "ACTIVE";

  private Instant lastLoginAt;

  @Column(updatable = false)
  private Instant createdAt;
}
