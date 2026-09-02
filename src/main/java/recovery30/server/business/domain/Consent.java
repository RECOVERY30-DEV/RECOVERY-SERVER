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

/** 사업자별 동의 현재 상태 (core_consents). business_id + consent_type_code 조합당 최신 상태 1건. */
@Entity
@Table(name = "core_consents")
@Getter
@Setter
@NoArgsConstructor
public class Consent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long businessId;

  @Column(nullable = false)
  private String consentTypeCode;

  @Column(nullable = false)
  private String consentVersion;

  @Column(nullable = false)
  private String status;

  private Instant grantedAt;

  private Instant withdrawnAt;

  private String ipAddress;

  private String userAgent;
}
