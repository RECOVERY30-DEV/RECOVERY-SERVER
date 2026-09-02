package recovery30.server.forecast.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 예측 커버리지 스냅샷의 데이터 소스 유형 ({@code forecast_coverage.source_type}).
 *
 * <p>DB CHECK 제약과 값이 1:1로 대응한다.
 */
@Schema(
    name = "CoverageSourceType",
    description =
        """
        분석에 사용된 데이터 소스 유형
        - BANK_ACCOUNT: 사업자 계좌 입출금
        - CARD_SETTLEMENT: 카드 매출 정산
        - LOAN: 대출·원리금 상환 일정
        - AUTO_TRANSFER: 자동이체 (공과금·구독·보험료 등)
        """,
    enumAsRef = true)
public enum CoverageSourceType {
  BANK_ACCOUNT,
  CARD_SETTLEMENT,
  LOAN,
  AUTO_TRANSFER
}
