package recovery30.server.followup.internal;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.followup.domain.FollowupResult;

/** 사후 점검 결과 저장소. schedule당 1건. */
public interface FollowupResultRepository extends JpaRepository<FollowupResult, Long> {

  Optional<FollowupResult> findByFollowupScheduleId(Long followupScheduleId);

  List<FollowupResult> findByFollowupScheduleIdIn(List<Long> followupScheduleIds);
}
