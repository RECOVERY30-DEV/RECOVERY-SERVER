package recovery30.server.business.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 사업자 프로필 (core_businesses). 지원제도 자격판정 입력값의 원천. */
@Entity
@Table(name = "core_businesses")
@Getter
@Setter
@NoArgsConstructor
public class Business {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  private String bizRegNo;

  @Column(nullable = false)
  private String bizName;

  private String industryCode;

  private LocalDate openedAt;

  private String regionCode;

  private Long annualRevenue;

  private Integer employeeCount;

  @Column(nullable = false)
  private Long safetyBufferAmount = 1_000_000L;

  @Column(updatable = false)
  private Instant createdAt;
}
