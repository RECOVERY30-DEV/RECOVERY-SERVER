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

/** 은행/정책기관 상담자 (recovery_counselors). */
@Entity
@Table(name = "recovery_counselors")
@Getter
@Setter
@NoArgsConstructor
public class Counselor {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  private String institution;

  private String branch;

  private String role;
}
