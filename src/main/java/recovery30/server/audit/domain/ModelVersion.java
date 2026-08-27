package recovery30.server.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 예측 모델 버전 마스터 (audit_model_versions). 재현성을 위해 {@code forecast_runs.model_version}이 가리키는 논리 버전. */
@Entity
@Table(name = "audit_model_versions")
@Getter
@Setter
@NoArgsConstructor
public class ModelVersion {

  @Id private String version;

  private String description;

  private LocalDate releasedAt;

  @Column(name = "is_active", nullable = false)
  private boolean active = false;
}
