package recovery30.server.recoveryoption;

import recovery30.server.recoveryoption.domain.RecoveryOption;
import recovery30.server.recoveryoption.domain.Scenario;
import recovery30.server.recoveryoption.domain.ScenarioOption;

/** recoveryoption 슬라이스 통합테스트용 엔티티 픽스처. */
public final class RecoveryOptionFixtures {

  private RecoveryOptionFixtures() {}

  public static RecoveryOption option(String optionCode, String category, String difficulty) {
    RecoveryOption o = new RecoveryOption();
    o.setOptionCode(optionCode);
    o.setCategory(category);
    o.setDifficulty(difficulty);
    o.setExpectedEffectText("부족일 최대 16일 연장 가능");
    o.setMonthlyBurdenChangeText("월 상환액 약 15만 원 감소 예상");
    o.setPreconditionText("원리금 3회 이상 정상 납부 이력");
    o.setRequiresReview(true);
    o.setDisclaimer("승인 여부와 조건은 금융기관 심사 결과에 따릅니다.");
    return o;
  }

  public static Scenario scenario(long forecastRunId, String scenarioType) {
    Scenario s = new Scenario();
    s.setForecastRunId(forecastRunId);
    s.setScenarioType(scenarioType);
    return s;
  }

  public static ScenarioOption scenarioOption(long scenarioId, long recoveryOptionId) {
    ScenarioOption so = new ScenarioOption();
    so.setScenarioId(scenarioId);
    so.setRecoveryOptionId(recoveryOptionId);
    return so;
  }
}
