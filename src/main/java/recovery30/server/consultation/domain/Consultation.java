package recovery30.server.consultation.domain;

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

/** 상담 예약 (recovery_consultations). 최종 판단(final_decision)은 상담자가 배정됐을 때만 기록할 수 있다 (AI 단독 결정 금지). */
@Entity
@Table(name = "recovery_consultations")
@Getter
@Setter
@NoArgsConstructor
public class Consultation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long businessId;

  private Long packetId;

  private Long counselorId;

  /** PHONE / VISIT / VIDEO / CHAT */
  @Column(nullable = false)
  private String channel;

  @Column(nullable = false)
  private Instant scheduledAt;

  @Column(columnDefinition = "TEXT")
  private String purposeText;

  @Column(columnDefinition = "TEXT")
  private String preQuestion;

  @Column(nullable = false)
  private boolean transferConsentGranted = false;

  /** REQUESTED / CONFIRMED / COMPLETED / CANCELED */
  @Column(nullable = false)
  private String status = "REQUESTED";

  private String finalDecision;

  private Long decidedBy;

  private Instant decidedAt;

  @Column(columnDefinition = "TEXT")
  private String resultNote;
}
