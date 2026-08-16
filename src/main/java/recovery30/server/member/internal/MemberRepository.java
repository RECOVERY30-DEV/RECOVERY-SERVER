package recovery30.server.member.internal;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;
import recovery30.server.member.domain.Member;

/** 실제 DB가 붙기 전까지 쓰는 메모리 기반 저장소. 나중에 JPA로 교체할 때 이 클래스만 갈아끼우면 되고, 다른 슬라이스는 영향받지 않는다. */
@Repository
public class MemberRepository {

  private final Map<Long, Member> store = new ConcurrentHashMap<>();
  private final AtomicLong sequence = new AtomicLong(0);

  public Member save(Member member) {
    if (member.getId() != null) {
      store.put(member.getId(), member);
      return member;
    }
    long newId = sequence.incrementAndGet();
    Member saved = member.withId(newId);
    store.put(newId, saved);
    return saved;
  }

  public Optional<Member> findById(Long id) {
    return Optional.ofNullable(store.get(id));
  }

  public boolean existsById(Long id) {
    return store.containsKey(id);
  }
}
