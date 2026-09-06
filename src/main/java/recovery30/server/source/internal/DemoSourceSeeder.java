package recovery30.server.source.internal;

import java.math.BigDecimal;
import java.time.Instant;
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
import recovery30.server.source.domain.SourceDataSource;

/**
 * demo 프로파일에서 데이터 범위 확인 화면용 연동 상태({@code source_data_sources})를 주입한다. 커버리지율은 각 페르소나의 예측
 * 커버리지(DemoForecastSeeder)와 맞춰, 자동이체가 낮은 RISK / 다수 임계미달 HOLD 를 재현한다.
 */
@Component
@Profile("demo")
@ConditionalOnProperty(
    prefix = "demo.seed",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
@Order(7)
public class DemoSourceSeeder implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(DemoSourceSeeder.class);
  private static final Instant BANK_SYNC = Instant.parse("2025-07-14T21:14:00Z");
  private static final Instant CARD_SYNC = Instant.parse("2025-07-13T14:42:00Z");
  private static final Instant LOAN_SYNC = Instant.parse("2025-07-14T21:00:00Z");
  private static final Instant AUTO_SYNC = Instant.parse("2025-07-11T00:00:00Z");

  private final BusinessApi businessApi;
  private final SourceDataSourceRepository repository;

  public DemoSourceSeeder(BusinessApi businessApi, SourceDataSourceRepository repository) {
    this.businessApi = businessApi;
    this.repository = repository;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    seed("QA-RISK", 95, 92, 88, 61);
    seed("QA-STABLE", 96, 94, 97, 90);
    seed("QA-HOLD", 72, 55, 40, 61);
  }

  private void seed(String bizRegNo, int bank, int card, int loan, int auto) {
    Long businessId = businessApi.findBusinessIdByRegNo(bizRegNo).orElse(null);
    if (businessId == null || repository.countByBusinessId(businessId) > 0) {
      return;
    }
    repository.save(row(businessId, "BANK_ACCOUNT", "KB국민은행 · 신한은행", bank, 6, BANK_SYNC));
    repository.save(row(businessId, "CARD_SETTLEMENT", "BC카드 · KB카드 가맹점 정산", card, 3, CARD_SYNC));
    repository.save(row(businessId, "LOAN", "IBK기업은행 사업자대출 약정", loan, 12, LOAN_SYNC));
    repository.save(row(businessId, "AUTO_TRANSFER", "공과금 · 구독 · 보험료 등", auto, 1, AUTO_SYNC));
    log.info("[demo] {} source_data_sources 4건 시딩", bizRegNo);
  }

  private static SourceDataSource row(
      long businessId,
      String sourceType,
      String institution,
      int coverage,
      int months,
      Instant syncedAt) {
    SourceDataSource s = new SourceDataSource();
    s.setBusinessId(businessId);
    s.setSourceType(sourceType);
    s.setInstitutionName(institution);
    s.setCoverageRate(new BigDecimal(coverage + ".00"));
    s.setPeriodMonths(months);
    s.setLastSyncedAt(syncedAt);
    s.setSyncStatus(coverage < 70 ? "PARTIAL" : "SYNCED");
    return s;
  }
}
