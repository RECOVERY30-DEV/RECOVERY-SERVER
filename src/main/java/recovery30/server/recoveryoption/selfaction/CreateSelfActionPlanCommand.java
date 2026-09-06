package recovery30.server.recoveryoption.selfaction;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

/** 셀프 액션 저장 요청. 선택한 회복안을 실행 계획으로 저장한다. */
public record CreateSelfActionPlanCommand(
    @Schema(description = "대상 회복안 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
        Long recoveryOptionId,
    @Schema(description = "기대 효과 요약 (선택)", example = "첫 부족일 +12일 연장, 예상 최저잔액 +180만 원")
        String expectedEffectText,
    @Schema(description = "자체 실행 준비 항목") List<NewItem> items) {

  /** 준비 항목 입력. */
  public record NewItem(
      @Schema(
              description = "할 일",
              requiredMode = Schema.RequiredMode.REQUIRED,
              example = "임차인에게 납부일 조정 요청하기")
          String title,
      @Schema(description = "예정일 (선택)", example = "2025-07-18") LocalDate targetDate,
      @Schema(description = "메모 (선택)") String memo) {}
}
