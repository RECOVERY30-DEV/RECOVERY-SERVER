package recovery30.server.audit.domain;

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

/** 전역 감사 로그 (audit_logs). actor_type 에 AI 포함 — 누가/무슨 목적으로 어떤 데이터를 다뤘는지 추적. */
@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** USER / COUNSELOR / SYSTEM / AI */
  @Column(nullable = false)
  private String actorType;

  private Long actorId;

  @Column(nullable = false)
  private String action;

  private String targetType;

  private Long targetId;

  private String purpose;

  private Long consentId;

  private String ipAddress;

  @Column(updatable = false)
  private Instant createdAt;
}
