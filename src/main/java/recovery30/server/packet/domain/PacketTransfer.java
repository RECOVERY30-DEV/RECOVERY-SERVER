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
 * Packet 상담자 전송 이력 (recovery_packet_transfers). Recovery Packet 화면 "전송 상태"와 상담 예약 "정보 전송". 전송
 * 동의({@link #consentId}) 없이는 행을 만들 수 없다.
 */
@Entity
@Table(name = "recovery_packet_transfers")
@Getter
@Setter
@NoArgsConstructor
public class PacketTransfer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long packetId;

  private Long counselorId;

  private String channel;

  /** 전송 범위 4항목(위험 Snapshot·보정값·선택안·사전 질문)을 담은 JSON 원문. */
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false)
  private String scopeJson;

  @Column(nullable = false)
  private Long consentId;

  @Column(updatable = false)
  private Instant sentAt;
}
