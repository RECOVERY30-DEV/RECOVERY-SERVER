package recovery30.server;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * 모듈(bounded context) 경계를 강제하는 아키텍처 테스트.
 * ./gradlew test 실행 시 다른 테스트와 함께 자동으로 돌아갑니다.
 *
 * 규칙 요약:
 *   1. 각 모듈의 internal 패키지는 그 모듈 밖에서 절대 참조할 수 없다.
 *   2. 각 모듈의 domain 패키지는 그 모듈 밖에서 직접 참조할 수 없다 (api를 통해서만 접근).
 *
 * 새 모듈을 추가하면 아래 블록 하나를 그대로 복사해서 모듈명만 바꿔 추가하세요.
 */
@AnalyzeClasses(packages = "recovery30.server")
public class ArchitectureTest {

    // ── order 모듈 ──
    @ArchTest
    static final ArchRule order_internal_is_not_accessed_from_outside =
            noClasses()
                    .that().resideOutsideOfPackage("..order.internal..")
                    .should().dependOnClassesThat().resideInAPackage("..order.internal..")
                    .because("order.internal은 order 모듈 내부에서만 사용해야 합니다");

    @ArchTest
    static final ArchRule order_domain_is_not_accessed_from_outside =
            noClasses()
                    .that().resideOutsideOfPackage("..order..")
                    .should().dependOnClassesThat().resideInAPackage("..order.domain..")
                    .because("order.domain은 order 모듈 밖에서 직접 참조할 수 없습니다 (order.api를 통해서만 접근)");

    // ── product 모듈 ──
    @ArchTest
    static final ArchRule product_internal_is_not_accessed_from_outside =
            noClasses()
                    .that().resideOutsideOfPackage("..product.internal..")
                    .should().dependOnClassesThat().resideInAPackage("..product.internal..")
                    .because("product.internal은 product 모듈 내부에서만 사용해야 합니다");

    @ArchTest
    static final ArchRule product_domain_is_not_accessed_from_outside =
            noClasses()
                    .that().resideOutsideOfPackage("..product..")
                    .should().dependOnClassesThat().resideInAPackage("..product.domain..")
                    .because("product.domain은 product 모듈 밖에서 직접 참조할 수 없습니다 (product.api를 통해서만 접근)");

    // ── member 모듈 ──
    @ArchTest
    static final ArchRule member_internal_is_not_accessed_from_outside =
            noClasses()
                    .that().resideOutsideOfPackage("..member.internal..")
                    .should().dependOnClassesThat().resideInAPackage("..member.internal..")
                    .because("member.internal은 member 모듈 내부에서만 사용해야 합니다");

    @ArchTest
    static final ArchRule member_domain_is_not_accessed_from_outside =
            noClasses()
                    .that().resideOutsideOfPackage("..member..")
                    .should().dependOnClassesThat().resideInAPackage("..member.domain..")
                    .because("member.domain은 member 모듈 밖에서 직접 참조할 수 없습니다 (member.api를 통해서만 접근)");
}