package recovery30.server.supportprogram.internal;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import recovery30.server.supportprogram.domain.SupportProgram;

/** 지원제도 저장소. */
public interface SupportProgramRepository extends JpaRepository<SupportProgram, Long> {

  Optional<SupportProgram> findByProgramCode(String programCode);

  List<SupportProgram> findAllByOrderByApplyDeadlineAscIdAsc();

  /** 신청 가능(ACTIVE + 마감일 미도래 또는 마감일 없음)만. 신청기한 임박순 정렬. */
  @Query(
      "SELECT p FROM SupportProgram p WHERE p.status = 'ACTIVE'"
          + " AND (p.applyDeadline IS NULL OR p.applyDeadline >= :today)"
          + " ORDER BY p.applyDeadline ASC, p.id ASC")
  List<SupportProgram> findApplicable(@Param("today") LocalDate today);
}
