package recovery30.server.business.internal;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.business.domain.User;

/** 로그인 계정 저장소 (core_users). */
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);
}
