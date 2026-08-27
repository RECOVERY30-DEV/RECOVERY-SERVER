package recovery30.server.recoveryoption.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 자체 실행 준비 항목 (recovery_self_action_items). 셀프 액션 저장 화면 "자체 실행 준비 항목"의 각 줄 (예정일 입력). */
@Entity
@Table(name = "recovery_self_action_items")
@Getter
@Setter
@NoArgsConstructor
public class SelfActionItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long selfActionPlanId;

  @Column(nullable = false)
  private String title;

  private LocalDate targetDate;

  /** PENDING / DONE */
  @Column(nullable = false)
  private String status = "PENDING";

  private String memo;
}
