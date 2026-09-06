package recovery30.server.recoveryoption.getrecoveryoptions;

import io.swagger.v3.oas.annotations.media.Schema;
import recovery30.server.recoveryoption.domain.RecoveryOptionCategory;
import recovery30.server.recoveryoption.domain.RecoveryOptionDifficulty;

/** 회복안 비교 화면 "회복 옵션 선택" 카드 한 장. */
public record RecoveryOptionView(
    @Schema(description = "회복안 ID (option-selections 요청에 사용)", example = "3") Long optionId,
    @Schema(description = "회복안 코드", example = "REPAYMENT_ADJUST") String optionCode,
    @Schema(description = "분류") RecoveryOptionCategory category,
    @Schema(description = "예상 효과", example = "부족일 최대 16일 연장 가능") String expectedEffectText,
    @Schema(description = "월 부담 변화", example = "월 상환액 약 15만 원 감소 예상")
        String monthlyBurdenChangeText,
    @Schema(description = "조건(선행요건)", example = "원리금 3회 이상 정상 납부 이력") String preconditionText,
    @Schema(description = "실행 난이도. nullable") RecoveryOptionDifficulty difficulty,
    @Schema(description = "상담/심사 필요 여부", example = "true") boolean requiresReview,
    @Schema(description = "면책 문구", example = "승인 여부와 조건은 금융기관 심사 결과에 따릅니다.") String disclaimer,
    @Schema(description = "현재 이 예측 실행에서 사용자가 선택한 회복안인지", example = "false") boolean selected) {}
