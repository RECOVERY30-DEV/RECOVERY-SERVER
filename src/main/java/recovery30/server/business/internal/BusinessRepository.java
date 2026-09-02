package recovery30.server.business.internal;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.business.domain.Business;

/** 사업자 프로필 저장소 (core_businesses). */
public interface BusinessRepository extends JpaRepository<Business, Long> {

  Optional<Business> findByBizRegNo(String bizRegNo);
}
