package recovery30.server.consultation.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.consultation.domain.Counselor;

/** 상담자 저장소. */
public interface CounselorRepository extends JpaRepository<Counselor, Long> {

  List<Counselor> findAllByOrderByIdAsc();
}
