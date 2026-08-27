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

/**
 * 동의/철회 append-only 이력 (audit_consent_logs). {@code core_consents}는 항목당 최신 상태 1건만 유지하고, 변경 이력은 여기에
 * 쌓는다. 분리 동의 / 동의 관리 화면의 법적 증빙.
 */
@Entity
@Table(name = "audit_consent_logs")
@Getter
@Setter
@NoArgsConstructor
public class ConsentLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long businessId;

  @Column(nullable = false)
  private String consentTypeCode;

  /** GRANTED / WITHDRAWN (최초 기록 시 null 가능) */
  private String fromStatus;

  /** GRANTED / WITHDRAWN */
  @Column(nullable = false)
  private String toStatus;

  @Column(nullable = false)
  private String consentVersion;

  private String ipAddress;

  private String userAgent;

  @Column(updatable = false)
  private Instant changedAt;
}
