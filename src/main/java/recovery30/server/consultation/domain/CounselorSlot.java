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

/** 상담자 예약 가능 슬롯 (recovery_counselor_slots). 상담 예약 화면 "예약 가능 일시 선택". MVP는 목데이터로 시드한다. */
@Entity
@Table(name = "recovery_counselor_slots")
@Getter
@Setter
@NoArgsConstructor
public class CounselorSlot {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long counselorId;

  @Column(nullable = false)
  private Instant startAt;

  @Column(nullable = false)
  private Instant endAt;

  /** OPEN / BOOKED / BLOCKED */
  @Column(nullable = false)
  private String status = "OPEN";

  /** 슬롯 정원. 잔여석 = capacity - bookedCount */
  @Column(nullable = false)
  private int capacity = 1;

  /** 현재 예약 수 */
  @Column(nullable = false)
  private int bookedCount = 0;
}
