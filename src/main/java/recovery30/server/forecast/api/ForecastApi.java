package recovery30.server.forecast.api;

import java.util.Optional;

/**
 * 다른 모듈이 forecast(예측 실행) 데이터를 동기적으로 확인할 때 쓰는 유일한 통로. 구현체는 forecast.internal 패키지에 있으며 외부에서는 이 인터페이스만
 * 참조한다.
 */
public interface ForecastApi {

  /** 해당 예측 실행이 존재하는지. */
  boolean forecastRunExists(Long forecastRunId);

  /** 사업자의 가장 최근 예측 실행 id. */
  Optional<Long> findLatestForecastRunId(Long businessId);

  /** 예측 실행의 소유 사업자 id. */
  Optional<Long> findBusinessId(Long forecastRunId);
}
