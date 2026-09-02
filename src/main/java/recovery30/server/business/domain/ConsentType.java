package recovery30.server.business.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 동의 항목 마스터 (core_consent_types). 예: ANALYSIS, PACKET_TRANSFER, FOLLOWUP_TRACKING. */
@Entity
@Table(name = "core_consent_types")
@Getter
@Setter
@NoArgsConstructor
public class ConsentType {

  @Id private String code;

  @Column(nullable = false)
  private String name;

  @Column(name = "is_required", nullable = false)
  private boolean required;

  @Column(columnDefinition = "TEXT")
  private String purpose;

  @Column(columnDefinition = "TEXT")
  private String dataScope;

  @Column(columnDefinition = "TEXT")
  private String withdrawEffect;

  @Column(nullable = false)
  private String version;
}
