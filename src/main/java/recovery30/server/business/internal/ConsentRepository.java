package recovery30.server.business.internal;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.business.domain.Consent;

/** 사업자별 동의 현재 상태 저장소 (core_consents). business_id + consent_type_code 당 1건. */
public interface ConsentRepository extends JpaRepository<Consent, Long> {

  Optional<Consent> findByBusinessIdAndConsentTypeCode(Long businessId, String consentTypeCode);
}
