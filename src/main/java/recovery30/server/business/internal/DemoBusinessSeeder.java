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
import recovery30.server.business.domain.ConsentType;
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
  private final ConsentTypeRepository consentTypeRepository;

  public DemoBusinessSeeder(
      UserRepository userRepository,
      BusinessRepository businessRepository,
      ConsentRepository consentRepository,
      ConsentTypeRepository consentTypeRepository) {
    this.userRepository = userRepository;
    this.businessRepository = businessRepository;
    this.consentRepository = consentRepository;
    this.consentTypeRepository = consentTypeRepository;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    seedConsentTypesIfEmpty();

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

  /** core_consent_types 는 Flyway V14 가 모든 환경에 적재. Flyway 가 없는 테스트 환경을 위해 비어 있으면 동일 3건을 채운다. */
  private void seedConsentTypesIfEmpty() {
    if (consentTypeRepository.count() > 0) {
      return;
    }
    consentTypeRepository.save(
        consentType(
            "ANALYSIS",
            "서비스 분석 동의",
            true,
            "30일 현금흐름 예측 및 부족 원인 분석에 사업자 거래 데이터를 활용합니다.",
            "사업자 거래 내역, 보정값, 예측 결과",
            "철회 시 30일 현금흐름 분석을 포함한 모든 서비스 이용이 중단됩니다."));
    consentTypeRepository.save(
        consentType(
            "PACKET_TRANSFER",
            "상담원 전송 동의",
            false,
            "상담 예약 시 Recovery Packet을 상담원에게 사전 전송합니다.",
            "Recovery Packet (위험 Snapshot, 원인, 선택안, 질문, 준비서류)",
            "철회해도 상담 예약은 유지되나 Packet이 전송되지 않습니다."));
    consentTypeRepository.save(
        consentType(
            "FOLLOWUP_TRACKING",
            "30·60·90일 사후 점검 동의",
            false,
            "실행 결과와 잔액 회복 여부를 확인해 추천을 개선합니다.",
            "실행 결과, 잔액 회복 여부, 연체 발생 여부",
            "미동의 시 추적 알림을 받지 않으며 분석 이용에는 영향을 주지 않습니다."));
    log.info("[demo] core_consent_types 3건 시딩 (Flyway V14 미적용 환경)");
  }

  private static ConsentType consentType(
      String code,
      String name,
      boolean required,
      String purpose,
      String dataScope,
      String withdrawEffect) {
    ConsentType t = new ConsentType();
    t.setCode(code);
    t.setName(name);
    t.setRequired(required);
    t.setPurpose(purpose);
    t.setDataScope(dataScope);
    t.setWithdrawEffect(withdrawEffect);
    t.setVersion("v1.0");
    return t;
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
