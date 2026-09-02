package recovery30.server.source.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 원천 연동 커넥션의 현재 상태 (source_data_sources). "데이터 범위 확인" 화면과 홈/Dashboard "분석 데이터 범위"의 실시간 표시 원천. 특정 예측
 * 시점의 스냅샷은 {@code forecast_coverage}가 별도로 보관한다.
 */
@Entity
@Table(name = "source_data_sources")
@Getter
@Setter
@NoArgsConstructor
public class SourceDataSource {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long businessId;

  /** BANK_ACCOUNT / CARD_SETTLEMENT / LOAN / AUTO_TRANSFER */
  @Column(nullable = false)
  private String sourceType;

  private String institutionName;

  private BigDecimal coverageRate;

  @Column(nullable = false)
  private Integer periodMonths = 6;

  private Instant lastSyncedAt;

  /** SYNCED / PARTIAL / FAILED */
  @Column(nullable = false)
  private String syncStatus = "SYNCED";
}
