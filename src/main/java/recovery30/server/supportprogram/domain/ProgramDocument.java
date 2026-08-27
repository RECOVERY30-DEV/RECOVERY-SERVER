package recovery30.server.supportprogram.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 지원제도 필요서류 (recovery_program_documents). 지원사업 상세 화면 "필요서류". */
@Entity
@Table(name = "recovery_program_documents")
@Getter
@Setter
@NoArgsConstructor
public class ProgramDocument {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long programId;

  @Column(nullable = false)
  private String name;

  private String description;

  @Column(name = "is_required", nullable = false)
  private boolean required = true;
}
