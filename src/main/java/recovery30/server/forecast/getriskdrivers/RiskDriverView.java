package recovery30.server.forecast.getriskdrivers;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

/**
 * 홈 화면 "주요 위험 신호" 행. 오른쪽 표시값은 클라이언트가 {@code metricText → occurrenceText → occurrenceDate} 순으로 만든다.
 * {@code evidence}는 {@code ?include=evidence}일 때만 채워지고 그 외에는 null이다.
 */
public record RiskDriverView(
    @Schema(description = "순위", example = "1") Integer rank,
    @Schema(
            description =
                "원인 코드. 아이콘·딥링크 매핑용 식별자로, 고정 enum이 아니라 확장 가능한 카탈로그다. "
                    + "현재 값: RENT_LOAN_CONCENTRATION(월말 원리금·임차료 집중), SALES_DECLINE_4W(최근 4주 매출 감소), "
                    + "AUTODEBIT_OVERLAP(자동이체 납부일 겹침), SEASONAL_RECOVERY_DELAY(계절적 회복 지연). "
                    + "미매핑 코드는 기본 아이콘으로 표시할 것.",
            example = "RENT_LOAN_CONCENTRATION")
        String driverCode,
    @Schema(description = "원인 제목", example = "월말 원리금 임차료 집중") String title,
    @Schema(description = "발생 예정일(정렬·표시용)", example = "2025-07-31") LocalDate occurrenceDate,
    @Schema(description = "복수 발생일 표시 문자열", example = "11월 20일·25일 발생") String occurrenceText,
    @Schema(description = "영향 기간 표시 문자열", example = "6월 15일~28일 영향") String impactPeriodText,
    @Schema(description = "금액이 아닌 지표 문자열", example = "-18%") String metricText,
    @Schema(description = "부족 기여 금액(원). null이면 '확인 필요'", example = "-1850000")
        Long contributionAmount,
    @Schema(description = "추정치 여부('근거 데이터 부족')", example = "false") boolean estimating,
    @Schema(description = "근거 거래 목록. ?include=evidence일 때만") List<EvidenceView> evidence) {

  /** 원인 상세 화면 "근거 거래" 한 줄. */
  public record EvidenceView(
      @Schema(
              description =
                  "근거가 된 원천 레코드의 종류. 현재 값: CARD_SETTLEMENT, BANK_ACCOUNT, LOAN_SCHEDULE, "
                      + "RECURRING_EXPENSE, ADJUSTMENT. refId와 함께 원천 데이터로 역추적하는 용도.",
              example = "CARD_SETTLEMENT")
          String refType,
      @Schema(description = "참조 레코드 ID (refType 테이블의 PK)", example = "3391") Long refId,
      @Schema(description = "표시 라벨", example = "신한카드 정산 5건") String label,
      @Schema(description = "기간 문자열", example = "6월 2일~11일") String periodText) {}
}
