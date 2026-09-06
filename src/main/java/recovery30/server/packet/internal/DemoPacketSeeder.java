package recovery30.server.packet.internal;

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
import recovery30.server.forecast.api.ForecastApi;
import recovery30.server.packet.domain.RecoveryPacket;

/**
 * demo 프로파일에서 QA-RISK 페르소나의 Recovery Packet v1(DRAFT)을 주입한다. snapshot 은 회복안 비교/셀프 액션 화면에서 조립되는 구조를
 * 그대로 담은 목 JSON. 전송 이력은 상담 예약 플로우에서 생기므로 여기서는 만들지 않는다.
 */
@Component
@Profile("demo")
@ConditionalOnProperty(
    prefix = "demo.seed",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
@Order(5)
public class DemoPacketSeeder implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(DemoPacketSeeder.class);

  private static final String SNAPSHOT_JSON =
      """
      {
        "riskSnapshot": {
          "firstShortfallDate": "2025-07-26",
          "minBalanceRange": "-180만 원 ~ -240만 원",
          "status": "위험 — 부족 가능성 높음"
        },
        "adjustments": [
          {"label": "현금매출 추가 입력", "amountText": "+65만 원 / 7월 20일"},
          {"label": "예정 지출 (인테리어 대금)", "amountText": "-120만 원 / 7월 22일"}
        ],
        "causes": [
          {"rank": 1, "title": "월말 임차료·원리금 집중", "contributionText": "-210만 원 기여"},
          {"rank": 2, "title": "최근 8주 매출 감소", "contributionText": "-95만 원 기여"},
          {"rank": 3, "title": "계절적 매출 회복 지연", "contributionText": "확인 필요"}
        ],
        "selectedOptions": [
          {
            "title": "상환조건 조정 상담",
            "expectedEffect": "월 부담 -40만 원 추정",
            "preparation": "사업자등록증, 최근 3개월 거래내역",
            "nextAction": "상담 예약 완료 — 7월 16일 14:00"
          },
          {
            "title": "고정비 납부일 재배치",
            "expectedEffect": "부족일 +7일 개선 추정",
            "preparation": "임차계약서 확인, 임대인 협의",
            "nextAction": "직접 실행 저장됨"
          }
        ],
        "analysisBasis": "사업자계좌·카드정산·자동이체 포함. 현금거래·타행자금은 보정값 반영분만 포함됩니다."
      }
      """;

  private final BusinessApi businessApi;
  private final ForecastApi forecastApi;
  private final RecoveryPacketRepository recoveryPacketRepository;

  public DemoPacketSeeder(
      BusinessApi businessApi,
      ForecastApi forecastApi,
      RecoveryPacketRepository recoveryPacketRepository) {
    this.businessApi = businessApi;
    this.forecastApi = forecastApi;
    this.recoveryPacketRepository = recoveryPacketRepository;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    Long businessId = businessApi.findBusinessIdByRegNo("QA-RISK").orElse(null);
    if (businessId == null) {
      log.warn("[demo] QA-RISK 사업자가 없습니다 — DemoBusinessSeeder 확인");
      return;
    }
    if (recoveryPacketRepository.findTopByBusinessIdOrderByVersionDesc(businessId).isPresent()) {
      return;
    }
    Long runId = forecastApi.findLatestForecastRunId(businessId).orElse(null);
    if (runId == null) {
      log.warn("[demo] QA-RISK 예측 실행이 없습니다 — DemoForecastSeeder 확인");
      return;
    }

    RecoveryPacket packet = new RecoveryPacket();
    packet.setBusinessId(businessId);
    packet.setForecastRunId(runId);
    packet.setVersion(1);
    packet.setSnapshotJson(SNAPSHOT_JSON);
    packet.setStatus("DRAFT");
    packet.setGeneratedAt(Instant.parse("2025-07-14T00:32:00Z"));
    packet = recoveryPacketRepository.save(packet);
    log.info("[demo] QA-RISK Recovery Packet v1 시딩 (packetId={})", packet.getId());
  }
}
