package recovery30.server.business.getconsenttypes;

import io.swagger.v3.oas.annotations.media.Schema;

/** 분리 동의 / 동의 관리 화면의 동의 항목 마스터 한 개. */
public record ConsentTypeView(
    @Schema(description = "동의 항목 코드", example = "ANALYSIS") String code,
    @Schema(description = "항목명", example = "서비스 분석 동의") String name,
    @Schema(description = "필수 여부", example = "true") boolean required,
    @Schema(description = "수집·이용 목적", example = "30일 현금흐름 예측 및 부족 원인 분석에 사업자 거래 데이터를 활용합니다.")
        String purpose,
    @Schema(description = "데이터 사용 범위", example = "사업자 거래 내역, 보정값, 예측 결과") String dataScope,
    @Schema(description = "철회 시 영향", example = "철회 시 30일 현금흐름 분석을 포함한 모든 서비스 이용이 중단됩니다.")
        String withdrawEffect,
    @Schema(description = "약관 버전", example = "v1.0") String version) {}
