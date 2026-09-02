package recovery30.server.recoveryoption.domain;

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

/** 고객이 실제로 선택한 회복안 (recovery_user_option_selections). 최대 2개 제한은 애플리케이션 레벨에서 강제. */
@Entity
@Table(name = "recovery_user_option_selections")
@Getter
@Setter
@NoArgsConstructor
public class UserOptionSelection {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long forecastRunId;

  @Column(nullable = false)
  private Long recoveryOptionId;

  @Column(updatable = false)
  private Instant selectedAt;
}
