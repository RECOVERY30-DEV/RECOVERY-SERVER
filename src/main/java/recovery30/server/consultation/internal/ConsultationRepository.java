package recovery30.server.consultation.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.consultation.domain.Consultation;

/** 상담 예약 저장소. */
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {}
