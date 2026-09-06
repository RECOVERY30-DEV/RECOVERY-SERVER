package recovery30.server.forecast.getdailydetail;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import recovery30.server.forecast.domain.DailyItemKind;
import recovery30.server.forecast.domain.ForecastDaily;
import recovery30.server.forecast.domain.ForecastDailyItem;

/** 일자별 근거 바텀시트. 하루의 잔액·유입·유출 요약 + 근거 라인 전체. */
public record DailyDetailView(
    @Schema(description = "대상 날짜", example = "2025-07-20") LocalDate targetDate,
    @Schema(description = "D-day", example = "5") Integer dDay,
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
    @Schema(description = "잔액 부족 발생 여부", example = "false") boolean shortfall,
    @Schema(description = "공휴일 여부", example = "false") boolean holiday,
    @Schema(description = "공휴일 납부일 이동 안내. nullable") String holidayShiftNote,
    @Schema(description = "근거 라인 (확정 거래·예상 거래·보정값)") List<DailyItemView> items) {

  /** 근거 라인 한 줄. */
  public record DailyItemView(
      @Schema(description = "종류") DailyItemKind itemKind,
      @Schema(description = "라벨", example = "카드 매출 정산") String label,
      @Schema(description = "부가 설명. nullable", example = "신한카드 · 전일 매출 확정") String subLabel,
      @Schema(
              description = "방향 (I=유입, O=유출)",
              example = "I",
              allowableValues = {"I", "O"})
          String direction,
      @Schema(description = "금액 하한(원)", example = "680000") Long amountMin,
      @Schema(description = "금액 상한(원). 확정이면 하한과 동일", example = "680000") Long amountMax,
      @Schema(description = "원천 레코드 유형. nullable", example = "CARD_SETTLEMENT") String refType,
      @Schema(description = "원천 레코드 ID. nullable", example = "3391") Long refId) {

    static DailyItemView of(ForecastDailyItem i) {
      return new DailyItemView(
          DailyItemKind.valueOf(i.getItemKind()),
          i.getLabel(),
          i.getSubLabel(),
          i.getDirection(),
          i.getAmountMin(),
          i.getAmountMax(),
          i.getRefType(),
          i.getRefId());
    }
  }

  static DailyDetailView of(ForecastDaily d, List<ForecastDailyItem> items) {
    return new DailyDetailView(
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
        d.getHolidayShiftNote(),
        items.stream().map(DailyItemView::of).toList());
  }
}
