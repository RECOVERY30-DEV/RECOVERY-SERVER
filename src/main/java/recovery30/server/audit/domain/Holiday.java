package recovery30.server.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 공휴일 마스터 (audit_holidays). 원리금/자동이체 납부일 이동 계산에 쓴다 (Dashboard·안정 상태 "공휴일 납부일 이동"). 목데이터가 아니라 실제 한국
 * 공휴일을 시드한다.
 */
@Entity
@Table(name = "audit_holidays")
@Getter
@Setter
@NoArgsConstructor
public class Holiday {

  @Id private LocalDate holidayDate;

  @Column(nullable = false)
  private String name;

  @Column(name = "is_substitute", nullable = false)
  private boolean substitute = false;
}
