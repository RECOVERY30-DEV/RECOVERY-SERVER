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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** AI 호출 추적 (audit_ai_generations). RAG 근거 문서와 사람 검토 여부를 함께 남긴다 (심사 어필용). */
@Entity
@Table(name = "audit_ai_generations")
@Getter
@Setter
@NoArgsConstructor
public class AiGeneration {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String feature;

  private String model;

  private String modelVersion;

  private String promptRef;

  /** 검색된 근거 문서 목록 JSON 원문. */
  @JdbcTypeCode(SqlTypes.JSON)
  private String retrievedDocs;

  @Column(columnDefinition = "TEXT")
  private String outputSummary;

  @Column(name = "human_reviewed", nullable = false)
  private boolean humanReviewed = false;

  @Column(updatable = false)
  private Instant createdAt;
}
