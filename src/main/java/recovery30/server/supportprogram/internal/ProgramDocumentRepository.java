package recovery30.server.supportprogram.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.supportprogram.domain.ProgramDocument;

/** 지원제도 필요서류 저장소. */
public interface ProgramDocumentRepository extends JpaRepository<ProgramDocument, Long> {

  List<ProgramDocument> findByProgramIdOrderByIdAsc(Long programId);
}
