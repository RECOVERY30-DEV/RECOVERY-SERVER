package recovery30.server.business.api;

import java.util.Optional;

/**
 * 다른 모듈이 business(사업자·동의) 데이터를 동기적으로 조회할 때 쓰는 유일한 통로. 구현체는 business.internal 패키지에 있으며 외부에서는 이
 * 인터페이스만 참조한다.
 */
public interface BusinessApi {

  /** 사업자등록번호로 business_id 조회. */
  Optional<Long> findBusinessIdByRegNo(String bizRegNo);

  /** 해당 사업자의 GRANTED 상태 동의 id 조회 (consent_type_code 예: "ANALYSIS"). */
  Optional<Long> findGrantedConsentId(Long businessId, String consentTypeCode);
}
