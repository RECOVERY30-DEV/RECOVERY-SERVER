package recovery30.server.consultation.internal;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.consultation.domain.CounselorSlot;

/** 상담 슬롯 저장소. */
public interface CounselorSlotRepository extends JpaRepository<CounselorSlot, Long> {

  List<CounselorSlot> findByCounselorIdOrderByStartAtAsc(Long counselorId);

  List<CounselorSlot> findByCounselorIdAndStartAtBetweenOrderByStartAtAsc(
      Long counselorId, Instant from, Instant to);
}
