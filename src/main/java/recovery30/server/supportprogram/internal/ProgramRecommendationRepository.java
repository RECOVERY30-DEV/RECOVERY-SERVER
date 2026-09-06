package recovery30.server.supportprogram.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.supportprogram.domain.ProgramRecommendation;

/** 예측 실행별 지원제도 추천 저장소. */
public interface ProgramRecommendationRepository
    extends JpaRepository<ProgramRecommendation, Long> {

  List<ProgramRecommendation> findByForecastRunIdOrderByRankNoAsc(Long forecastRunId);
}
