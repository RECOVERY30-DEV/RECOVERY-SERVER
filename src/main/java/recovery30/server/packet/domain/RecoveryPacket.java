package recovery30.server.packet.domain;

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

/**
 * 상담용 Recovery Packet (recovery_packets). 수정 시 덮어쓰지 않고 {@link #supersedesPacketId}로 이전 버전을 연결한 새
 * 버전을 만든다.
 */
@Entity
@Table(name = "recovery_packets")
@Getter
@Setter
@NoArgsConstructor
public class RecoveryPacket {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long businessId;

  @Column(nullable = false)
  private Long forecastRunId;

  @Column(nullable = false)
  private Integer version;

  private Long supersedesPacketId;

  /** 생성 시점의 위험 스냅샷·원인·보정값·선택안을 동결한 JSON. */
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false)
  private String snapshotJson;

  /** DRAFT / CONFIRMED / SENT */
  @Column(nullable = false)
  private String status = "DRAFT";

  @Column(updatable = false)
  private Instant generatedAt;

  private Instant customerConfirmedAt;

  private Instant sentAt;

  @Column(columnDefinition = "TEXT")
  private String pdfUrl;
}
