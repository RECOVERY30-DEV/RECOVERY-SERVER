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

/** 알림 발송·클릭·행동완료 추적 (audit_notifications). KPI "회복 행동 완료율" 산출용. MVP는 목 로그로 채운다. */
@Entity
@Table(name = "audit_notifications")
@Getter
@Setter
@NoArgsConstructor
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long businessId;

  @Column(nullable = false)
  private String type;

  private String title;

  @Column(columnDefinition = "TEXT")
  private String body;

  private String channel;

  private Instant sentAt;

  private Instant readAt;

  private Instant actionCompletedAt;

  @Column(updatable = false)
  private Instant createdAt;
}
