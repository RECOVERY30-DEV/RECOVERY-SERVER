package recovery30.server.forecast.getshortfall;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/** 홈 화면 "첫 부족 예상일" 박스. "D-11" 표기·게이지 비율({@code dDay / horizonDays})·날짜 포맷은 클라이언트가 만든다. */
public record ShortfallView(
    @Schema(description = "예측 실행 ID", example = "4821") Long forecastRunId,
    @Schema(description = "30일 내 부족 발생 여부. false면 나머지 값 null", example = "true")
        boolean hasShortfall,
    @Schema(description = "부족 발생일까지 남은 일수", example = "11") Integer dDay,
    @Schema(description = "첫 부족 예상일", example = "2025-07-26") LocalDate expectedDate,
    @Schema(description = "예측 기간(일). 게이지 분모", example = "30") Integer horizonDays,
    @Schema(description = "예상 부족액 하한(원). 홈 미표시, Dashboard·원인상세용", example = "760000")
        Long shortfallAmountMin,
    @Schema(description = "예상 부족액 상한(원). 홈 미표시, Dashboard·원인상세용", example = "1240000")
        Long shortfallAmountMax) {}
