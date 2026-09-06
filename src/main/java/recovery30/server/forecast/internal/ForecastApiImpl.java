package recovery30.server.forecast.internal;

import java.util.Optional;
import org.springframework.stereotype.Component;
import recovery30.server.forecast.api.ForecastApi;
import recovery30.server.forecast.domain.ForecastRun;

/** ForecastApi 실제 구현체. 다른 모듈은 이 클래스가 아니라 ForecastApi 인터페이스만 주입받는다. */
@Component
public class ForecastApiImpl implements ForecastApi {

  private final ForecastRunRepository forecastRunRepository;

  public ForecastApiImpl(ForecastRunRepository forecastRunRepository) {
    this.forecastRunRepository = forecastRunRepository;
  }

  @Override
  public boolean forecastRunExists(Long forecastRunId) {
    return forecastRunId != null && forecastRunRepository.existsById(forecastRunId);
  }

  @Override
  public Optional<Long> findLatestForecastRunId(Long businessId) {
    return forecastRunRepository
        .findTopByBusinessIdOrderByBaseDateDescCreatedAtDesc(businessId)
        .map(ForecastRun::getId);
  }

  @Override
  public Optional<Long> findBusinessId(Long forecastRunId) {
    return forecastRunRepository.findById(forecastRunId).map(ForecastRun::getBusinessId);
  }
}
