package recovery30.server.source.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/** 원천 데이터 소스 유형 ({@code source_data_sources.source_type}). DB CHECK 제약과 값이 1:1로 대응한다. */
@Schema(
    name = "SourceType",
    description =
        """
        원천 데이터 소스 유형
        - BANK_ACCOUNT: 사업자 계좌 입출금
        - CARD_SETTLEMENT: 카드 매출 정산
        - LOAN: 대출·원리금 상환 일정
        - AUTO_TRANSFER: 자동이체 (공과금·구독·보험료 등)
        """,
    enumAsRef = true)
public enum SourceType {
  BANK_ACCOUNT,
  CARD_SETTLEMENT,
  LOAN,
  AUTO_TRANSFER
}
