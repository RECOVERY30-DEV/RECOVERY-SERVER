package recovery30.server.business.internal;

import java.util.Optional;
import org.springframework.stereotype.Component;
import recovery30.server.business.api.BusinessApi;
import recovery30.server.business.domain.Business;
import recovery30.server.business.domain.Consent;

/** BusinessApi 실제 구현체. 다른 모듈은 이 클래스가 아니라 BusinessApi 인터페이스만 주입받는다. */
@Component
public class BusinessApiImpl implements BusinessApi {

  private final BusinessRepository businessRepository;
  private final ConsentRepository consentRepository;

  public BusinessApiImpl(
      BusinessRepository businessRepository, ConsentRepository consentRepository) {
    this.businessRepository = businessRepository;
    this.consentRepository = consentRepository;
  }

  @Override
  public Optional<Long> findBusinessIdByRegNo(String bizRegNo) {
    return businessRepository.findByBizRegNo(bizRegNo).map(Business::getId);
  }

  @Override
  public Optional<Long> findGrantedConsentId(Long businessId, String consentTypeCode) {
    return consentRepository
        .findByBusinessIdAndConsentTypeCode(businessId, consentTypeCode)
        .filter(c -> "GRANTED".equals(c.getStatus()))
        .map(Consent::getId);
  }
}
