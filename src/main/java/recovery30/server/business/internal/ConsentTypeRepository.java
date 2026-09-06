package recovery30.server.business.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.business.domain.ConsentType;

/** 동의 항목 마스터 저장소 (core_consent_types). */
public interface ConsentTypeRepository extends JpaRepository<ConsentType, String> {

  List<ConsentType> findAllByOrderByRequiredDescCodeAsc();
}
