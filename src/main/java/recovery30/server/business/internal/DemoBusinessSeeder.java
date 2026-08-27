package recovery30.server.business.internal;

import java.time.Instant;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import recovery30.server.business.domain.Business;
import recovery30.server.business.domain.Consent;
import recovery30.server.business.domain.User;

/**
 * demo 프로파일에서 기동 시 QA용 사업자 페르소나(계정·프로필·동의)를 주입한다. 예측 데이터는 forecast 모듈의 DemoForecastSeeder(@Order
 * 2)가 이어서 채운다. 이미 있으면 건너뛴다(멱등).
 */
@Component
@Profile("demo")
@ConditionalOnProperty(
    prefix = "demo.seed",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
@Order(1)
public class DemoBusinessSeeder implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(DemoBusinessSeeder.class);
  private static final Instant DEMO_TS = Instant.parse("2025-07-14T23:32:00Z");

  private final UserRepository userRepository;
  private final BusinessRepository businessRepository;
  private final ConsentRepository consentRepository;

  public DemoBusinessSeeder(
      UserRepository userRepository,
      BusinessRepository businessRepository,
      ConsentRepository consentRepository) {
    this.userRepository = userRepository;
    this.businessRepository = businessRepository;
    this.consentRepository = consentRepository;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (businessRepository.findByBizRegNo("QA-RISK").isPresent()) {
      log.info("[demo] 사업자 페르소나가 이미 있어 시딩을 건너뜁니다");
      return;
    }

    long risk = persona("QA-RISK", "qa-risk@demo.recovery30", "QA 위험 상점", true);
    long stable = persona("QA-STABLE", "qa-stable@demo.recovery30", "QA 안정 상점", true);
    long hold = persona("QA-HOLD", "qa-hold@demo.recovery30", "QA 판단보류 상점", true);
    long fresh = persona("QA-NEW", "qa-new@demo.recovery30", "QA 신규 상점", false);

    log.info(
        "[demo] 사업자 페르소나 생성: QA-RISK=business {}, QA-STABLE=business {}, QA-HOLD=business {},"
            + " QA-NEW=business {}",
        risk,
        stable,
        hold,
        fresh);
  }

  private long persona(String bizRegNo, String email, String bizName, boolean withConsent) {
    User user = new User();
    user.setEmail(email);
    user.setPasswordHash("{noop}demo"); // 데모 전용, 실제 로그인에는 쓰지 않음
    user.setName(bizName + " 대표");
    user.setStatus("ACTIVE");
    user.setCreatedAt(DEMO_TS);
    user = userRepository.save(user);

    Business business = new Business();
    business.setUserId(user.getId());
    business.setBizRegNo(bizRegNo);
    business.setBizName(bizName);
    business.setIndustryCode("I56111"); // 한식 일반 음식점업
    business.setOpenedAt(LocalDate.of(2023, 1, 15));
    business.setRegionCode("11110"); // 종로구
    business.setAnnualRevenue(180_000_000L);
    business.setEmployeeCount(2);
    business.setSafetyBufferAmount(1_000_000L);
    business.setCreatedAt(DEMO_TS);
    business = businessRepository.save(business);

    if (withConsent) {
      grant(business.getId(), "ANALYSIS");
      grant(business.getId(), "FOLLOWUP_TRACKING");
    }
    return business.getId();
  }

  private void grant(long businessId, String consentTypeCode) {
    Consent consent = new Consent();
    consent.setBusinessId(businessId);
    consent.setConsentTypeCode(consentTypeCode);
    consent.setConsentVersion("v1.0");
    consent.setStatus("GRANTED");
    consent.setGrantedAt(DEMO_TS);
    consent.setIpAddress("127.0.0.1");
    consent.setUserAgent("demo-seeder");
    consentRepository.save(consent);
  }
}
