package recovery30.server.forecast.getdaily;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import recovery30.server.forecast.domain.ForecastDaily;

/** Dashboard "일자별 현금흐름" 캘린더 행. 금액은 원 단위 정수. */
public record DailyView(
    @Schema(description = "대상 날짜", example = "2025-07-20") LocalDate targetDate,
    @Schema(description = "D-day (기준일부터 며칠째, 0=기준일)", example = "5") Integer dDay,
    @Schema(description = "시작 잔액(원)", example = "1200000") Long openingBalance,
    @Schema(description = "확정 유입(원)", example = "680000") Long confirmedInflow,
    @Schema(description = "확정 유출(원)", example = "0") Long confirmedOutflow,
    @Schema(description = "예상 유입 하한(원)", example = "0") Long expectedInflowMin,
    @Schema(description = "예상 유입 상한(원)", example = "300000") Long expectedInflowMax,
    @Schema(description = "예상 유출 하한(원)", example = "0") Long expectedOutflowMin,
    @Schema(description = "예상 유출 상한(원)", example = "120000") Long expectedOutflowMax,
    @Schema(description = "보정값 합계(원, 부호 포함)", example = "50000") Long adjustmentNet,
    @Schema(description = "보수적 마감잔액(원)", example = "-300000") Long closingBalanceConservative,
    @Schema(description = "예상 마감잔액(원)", example = "150000") Long closingBalanceExpected,
    @Schema(description = "낙관 마감잔액(원)", example = "500000") Long closingBalanceOptimistic,
    @Schema(description = "이 날 잔액 부족 발생 여부", example = "false") boolean shortfall,
    @Schema(description = "공휴일 여부", example = "false") boolean holiday,
    @Schema(description = "공휴일로 인한 납부일 이동 안내. nullable", example = "7월 19일 공휴일로 원리금 상환일이 앞당겨졌습니다.")
        String holidayShiftNote) {

  static DailyView of(ForecastDaily d) {
    return new DailyView(
        d.getTargetDate(),
        d.getDDay(),
        d.getOpeningBalance(),
        d.getConfirmedInflow(),
        d.getConfirmedOutflow(),
        d.getExpectedInflowMin(),
        d.getExpectedInflowMax(),
        d.getExpectedOutflowMin(),
        d.getExpectedOutflowMax(),
        d.getAdjustmentNet(),
        d.getClosingBalanceConservative(),
        d.getClosingBalanceExpected(),
        d.getClosingBalanceOptimistic(),
        d.isShortfall(),
        d.isHoliday(),
        d.getHolidayShiftNote());
  }
}
