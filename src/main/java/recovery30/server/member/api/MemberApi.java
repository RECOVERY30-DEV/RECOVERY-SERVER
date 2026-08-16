package recovery30.server.member.api;

import java.util.Optional;

/**
 * 다른 모듈(order, product 등)이 member 데이터를 동기적으로 조회할 때 사용하는 유일한 통로. 구현체는 member.internal 패키지에 있으며,
 * 외부에서는 이 인터페이스만 참조해야 한다.
 */
public interface MemberApi {

  Optional<MemberSummary> findSummary(Long memberId);

  boolean existsById(Long memberId);
}
