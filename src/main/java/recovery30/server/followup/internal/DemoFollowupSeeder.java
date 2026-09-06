package recovery30.server.followup.internal;

import jakarta.persistence.EntityManager;
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
import recovery30.server.business.api.BusinessApi;
import recovery30.server.followup.domain.FollowupResult;
import recovery30.server.followup.domain.FollowupSchedule;
import recovery30.server.followup.domain.RecoveryExecutionStatus;
import recovery30.server.forecast.api.ForecastApi;

/**
 * demo 프로파일에서 QA-RISK 페르소나의 사후점검 데이터를 주입한다 — D30(완료·결과 있음)/D60/D90 점검 일정 + D30 결과 + 회복안별 실행 상태 2건.
 * 사업자·예측·동의는 앞선 시더가 만들고 여기서는 {@link BusinessApi}/{@link ForecastApi} 로 id 만 조회한다. 이미 있으면 건너뛴다(멱등).
 *
 * <p>사후점검 결과는 db-design 6절대로 "시연용 목 결과"다.
 */
@Component
@Profile("demo")
@ConditionalOnProperty(
    prefix = "demo.seed",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
@Order(9)
public class DemoFollowupSeeder implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(DemoFollowupSeeder.class);
  private static final LocalDate BASE_DATE = LocalDate.of(2025, 7, 15);

  private final BusinessApi businessApi;
  private final ForecastApi forecastApi;
  private final FollowupScheduleRepository scheduleRepository;
  private final FollowupResultRepository resultRepository;
  private final RecoveryExecutionStatusRepository executionStatusRepository;
  private final EntityManager entityManager;

  public DemoFollowupSeeder(
      BusinessApi businessApi,
      ForecastApi forecastApi,
      FollowupScheduleRepository scheduleRepository,
      FollowupResultRepository resultRepository,
      RecoveryExecutionStatusRepository executionStatusRepository,
      EntityManager entityManager) {
    this.businessApi = businessApi;
    this.forecastApi = forecastApi;
    this.scheduleRepository = scheduleRepository;
    this.resultRepository = resultRepository;
    this.executionStatusRepository = executionStatusRepository;
    this.entityManager = entityManager;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    Long businessId = businessApi.findBusinessIdByRegNo("QA-RISK").orElse(null);
    if (businessId == null) {
      return;
    }
    if (!scheduleRepository.findByBusinessIdOrderByScheduledDateAsc(businessId).isEmpty()) {
      log.info("[demo] business {} 사후점검 일정이 이미 있어 건너뜁니다", businessId);
      return;
    }
    Long runId = forecastApi.findLatestForecastRunId(businessId).orElse(null);
    Long consentId = businessApi.findGrantedConsentId(businessId, "FOLLOWUP_TRACKING").orElse(null);
    if (runId == null || consentId == null) {
      log.warn("[demo] QA-RISK 예측/사후점검 동의가 없어 followup 시딩을 건너뜁니다");
      return;
    }

    FollowupSchedule d30 = schedule(businessId, runId, consentId, "D30", BASE_DATE.plusDays(30));
    d30.setStatus("DONE");
    d30 = scheduleRepository.save(d30);
    scheduleRepository.save(schedule(businessId, runId, consentId, "D60", BASE_DATE.plusDays(60)));
    scheduleRepository.save(schedule(businessId, runId, consentId, "D90", BASE_DATE.plusDays(90)));

    FollowupResult result = new FollowupResult();
    result.setFollowupScheduleId(d30.getId());
    result.setBalanceRecovered("PARTIAL");
    result.setDelinquency(false);
    result.setBaselineBalance(540_000L);
    result.setCurrentBalance(2_180_000L);
    result.setRecoveryAmount(1_640_000L);
    result.setLatestForecastRunId(runId);
    result.setRiskStatus("STABLE");
    result.setRecordedAt(Instant.parse("2025-08-14T02:00:00Z"));
    resultRepository.save(result);

    executionStatus(businessId, runId, "REPAYMENT_ADJUST", "IN_PROGRESS", null);
    executionStatus(businessId, runId, "DUEDATE_SHIFT", "BLOCKED", "임대인 회신 지연 — 담당자 확인 필요");

    log.info("[demo] QA-RISK 사후점검 일정 3건(D30 완료+결과) + 회복안 실행 상태 2건 시딩");
  }

  private FollowupSchedule schedule(
      long businessId, long runId, long consentId, String checkpoint, LocalDate scheduledDate) {
    FollowupSchedule s = new FollowupSchedule();
    s.setBusinessId(businessId);
    s.setForecastRunId(runId);
    s.setCheckpoint(checkpoint);
    s.setScheduledDate(scheduledDate);
    s.setStatus("SCHEDULED");
    s.setConsentId(consentId);
    return s;
  }

  private void executionStatus(
      long businessId, long runId, String optionCode, String status, String blockerText) {
    Long optionId = recoveryOptionId(optionCode);
    if (optionId == null) {
      log.warn("[demo] 회복안 {} 이 없어 실행 상태 시딩을 건너뜁니다", optionCode);
      return;
    }
    RecoveryExecutionStatus e = new RecoveryExecutionStatus();
    e.setBusinessId(businessId);
    e.setRecoveryOptionId(optionId);
    e.setForecastRunId(runId);
    e.setStatus(status);
    e.setBlockerText(blockerText);
    e.setUpdatedAt(Instant.parse("2025-08-10T05:00:00Z"));
    executionStatusRepository.save(e);
  }

  private Long recoveryOptionId(String optionCode) {
    var rows =
        entityManager
            .createNativeQuery("SELECT id FROM recovery_options WHERE option_code = :code")
            .setParameter("code", optionCode)
            .getResultList();
    return rows.isEmpty() ? null : ((Number) rows.get(0)).longValue();
  }
}
