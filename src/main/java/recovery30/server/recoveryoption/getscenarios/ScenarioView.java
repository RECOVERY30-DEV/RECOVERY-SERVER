package recovery30.server.recoveryoption.getscenarios;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import recovery30.server.recoveryoption.domain.ScenarioType;

/** 회복안 비교 화면 "시나리오 비교" 행 한 개. */
public record ScenarioView(
    @Schema(description = "시나리오 ID", example = "12") Long scenarioId,
    @Schema(description = "시나리오 유형") ScenarioType scenarioType,
    @Schema(description = "(적용 후) 첫 부족 예상일. 없으면 30일 내 부족 없음", example = "2025-05-30")
        LocalDate firstShortfallDate,
    @Schema(description = "(적용 후) 예상 최저잔액(원)", example = "-630000") Long minBalance,
    @Schema(description = "BASELINE 대비 부족일 지연 일수. BASELINE은 null", example = "16")
        Integer deltaDays,
    @Schema(description = "BASELINE 대비 최저잔액 개선액(원). BASELINE은 null", example = "610000")
        Long deltaMinBalance,
    @Schema(description = "BASELINE 대비 월 상환 부담 변화(원). 음수면 감소", example = "-150000")
        Long monthlyPaymentDelta,
    @Schema(description = "설명 문구", example = "상담 및 심사 결과에 따라 실제 효과는 달라질 수 있습니다.") String note,
    @Schema(description = "이 시나리오에 적용된 회복안 ID 목록. BASELINE은 빈 배열", example = "[3]")
        List<Long> appliedOptionIds) {}
