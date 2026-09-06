package recovery30.server.recoveryoption.selfaction;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import recovery30.server.recoveryoption.domain.SelfActionItem;
import recovery30.server.recoveryoption.domain.SelfActionItemStatus;
import recovery30.server.recoveryoption.domain.SelfActionPlan;
import recovery30.server.recoveryoption.domain.SelfActionPlanStatus;

/** 셀프 액션 저장 화면 + 사후점검 화면의 자체 실행 계획 한 건. */
public record SelfActionPlanView(
    @Schema(description = "계획 ID", example = "5") Long id,
    @Schema(description = "대상 회복안 ID", example = "2") Long recoveryOptionId,
    @Schema(description = "기대 효과 요약. nullable", example = "첫 부족일 +12일 연장, 예상 최저잔액 +180만 원")
        String expectedEffectText,
    @Schema(description = "상태") SelfActionPlanStatus status,
    @Schema(description = "저장 시각(UTC)", example = "2025-07-14T00:00:00Z") Instant savedAt,
    @Schema(description = "준비 항목") List<SelfActionItemView> items) {

  /** 자체 실행 준비 항목 한 줄 (체크박스 + 라벨 + 예정일). */
  public record SelfActionItemView(
      @Schema(description = "항목 ID", example = "11") Long id,
      @Schema(description = "할 일", example = "임차인에게 납부일 조정 요청하기") String title,
      @Schema(description = "예정일. nullable", example = "2025-07-18") LocalDate targetDate,
      @Schema(description = "상태") SelfActionItemStatus status,
      @Schema(description = "메모. nullable") String memo) {

    static SelfActionItemView of(SelfActionItem i) {
      return new SelfActionItemView(
          i.getId(),
          i.getTitle(),
          i.getTargetDate(),
          SelfActionItemStatus.valueOf(i.getStatus()),
          i.getMemo());
    }
  }

  static SelfActionPlanView of(SelfActionPlan p, List<SelfActionItem> items) {
    return new SelfActionPlanView(
        p.getId(),
        p.getRecoveryOptionId(),
        p.getExpectedEffectText(),
        SelfActionPlanStatus.valueOf(p.getStatus()),
        p.getSavedAt(),
        items.stream().map(SelfActionItemView::of).toList());
  }
}
