package recovery30.server.consultation.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.consultation.domain.ConsultationOption;

/** 상담 ↔ 회복안 조인 저장소. */
public interface ConsultationOptionRepository extends JpaRepository<ConsultationOption, Long> {

  List<ConsultationOption> findByConsultationIdOrderByIdAsc(Long consultationId);
}
