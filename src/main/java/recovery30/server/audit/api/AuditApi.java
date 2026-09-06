package recovery30.server.audit.api;

/** 다른 모듈이 감사 이력을 남길 때 쓰는 통로. 구현체는 audit.internal 에 있으며 외부에서는 이 인터페이스만 참조한다. */
public interface AuditApi {

  /**
   * 동의/철회 이력을 append-only 로 기록한다 (audit_consent_logs). core_consents 는 최신 상태만 유지하므로 변경 이력은 여기에 쌓인다.
   *
   * @param fromStatus 이전 상태 (최초 기록이면 null)
   * @param toStatus 새 상태 (GRANTED / WITHDRAWN)
   */
  void appendConsentLog(
      Long businessId,
      String consentTypeCode,
      String fromStatus,
      String toStatus,
      String consentVersion,
      String ipAddress,
      String userAgent);
}
