package recovery30.server.supportprogram.domain;

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
 * 예측 실행별 지원제도 추천 (recovery_program_recommendations). 지원사업 목록의 추천 정렬과 회복안 비교에서 재사용. MVP는 RAG 대신
 * 목데이터로 시드한다.
 */
@Entity
@Table(name = "recovery_program_recommendations")
@Getter
@Setter
@NoArgsConstructor
public class ProgramRecommendation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long forecastRunId;

  @Column(nullable = false)
  private Long programId;

  @Column(name = "rank_no", nullable = false)
  private Integer rankNo;

  @Column(columnDefinition = "TEXT")
  private String matchReason;

  @Column(updatable = false)
  private Instant createdAt;
}
