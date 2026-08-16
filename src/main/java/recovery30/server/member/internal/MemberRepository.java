package recovery30.server.member.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import recovery30.server.member.domain.Member;

/** Spring Data JPA 저장소. findById/existsById/save는 JpaRepository가 기본 제공한다. */
public interface MemberRepository extends JpaRepository<Member, Long> {}
