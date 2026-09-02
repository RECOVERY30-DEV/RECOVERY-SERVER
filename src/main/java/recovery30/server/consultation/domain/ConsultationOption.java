package recovery30.server.consultation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 상담에서 다룰 회복안 연결 (recovery_consultation_options). 상담 예약 화면 "선택한 회복안" (N:M). */
@Entity
@Table(name = "recovery_consultation_options")
@Getter
@Setter
@NoArgsConstructor
public class ConsultationOption {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long consultationId;

  @Column(nullable = false)
  private Long recoveryOptionId;
}
