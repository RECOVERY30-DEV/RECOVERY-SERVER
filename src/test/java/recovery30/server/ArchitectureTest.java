package recovery30.server;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * 모듈(bounded context) 경계를 강제하는 아키텍처 테스트. ./gradlew test 실행 시 다른 테스트와 함께 자동으로 돌아갑니다.
 *
 * <p>규칙 요약: 1. 각 모듈의 internal 패키지는 그 모듈 밖에서 절대 참조할 수 없다. 2. 각 모듈의 domain 패키지는 그 모듈 밖에서 직접 참조할 수 없다
 * (api를 통해서만 접근).
 *
 * <p>새 모듈을 추가하면 아래 블록 하나를 그대로 복사해서 모듈명만 바꿔 추가하세요.
 */
@AnalyzeClasses(packages = "recovery30.server")
public class ArchitectureTest {

  // ── member 모듈 ── (새 모듈 추가 시 이 블록을 복사해서 모듈명만 바꾸세요)
  @ArchTest
  static final ArchRule member_internal_is_not_accessed_from_outside =
      noClasses()
          .that()
          .resideOutsideOfPackage("..member..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..member.internal..")
          .because("member.internal은 member 모듈 내부에서만 사용해야 합니다");

  @ArchTest
  static final ArchRule member_domain_is_not_accessed_from_outside =
      noClasses()
          .that()
          .resideOutsideOfPackage("..member..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..member.domain..")
          .because("member.domain은 member 모듈 밖에서 직접 참조할 수 없습니다 (member.api를 통해서만 접근)");

  // ── business 모듈 ──
  @ArchTest
  static final ArchRule business_internal_is_not_accessed_from_outside =
      noClasses()
          .that()
          .resideOutsideOfPackage("..business..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..business.internal..")
          .because("business.internal은 business 모듈 내부에서만 사용해야 합니다");

  @ArchTest
  static final ArchRule business_domain_is_not_accessed_from_outside =
      noClasses()
          .that()
          .resideOutsideOfPackage("..business..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..business.domain..")
          .because("business.domain은 business 모듈 밖에서 직접 참조할 수 없습니다 (business.api를 통해서만 접근)");

  // ── source 모듈 ──
  @ArchTest
  static final ArchRule source_internal_is_not_accessed_from_outside =
      noClasses()
          .that()
          .resideOutsideOfPackage("..source..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..source.internal..")
          .because("source.internal은 source 모듈 내부에서만 사용해야 합니다");

  @ArchTest
  static final ArchRule source_domain_is_not_accessed_from_outside =
      noClasses()
          .that()
          .resideOutsideOfPackage("..source..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..source.domain..")
          .because("source.domain은 source 모듈 밖에서 직접 참조할 수 없습니다 (source.api를 통해서만 접근)");

  // ── forecast 모듈 ──
  @ArchTest
  static final ArchRule forecast_internal_is_not_accessed_from_outside =
      noClasses()
          .that()
          .resideOutsideOfPackage("..forecast..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..forecast.internal..")
          .because("forecast.internal은 forecast 모듈 내부에서만 사용해야 합니다");

  @ArchTest
  static final ArchRule forecast_domain_is_not_accessed_from_outside =
      noClasses()
          .that()
          .resideOutsideOfPackage("..forecast..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..forecast.domain..")
          .because("forecast.domain은 forecast 모듈 밖에서 직접 참조할 수 없습니다 (forecast.api를 통해서만 접근)");

  // ── recoveryoption 모듈 ──
  @ArchTest
  static final ArchRule recoveryoption_internal_is_not_accessed_from_outside =
      noClasses()
          .that()
          .resideOutsideOfPackage("..recoveryoption..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..recoveryoption.internal..")
          .because("recoveryoption.internal은 recoveryoption 모듈 내부에서만 사용해야 합니다");

  @ArchTest
  static final ArchRule recoveryoption_domain_is_not_accessed_from_outside =
      noClasses()
          .that()
          .resideOutsideOfPackage("..recoveryoption..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..recoveryoption.domain..")
          .because(
              "recoveryoption.domain은 recoveryoption 모듈 밖에서 직접 참조할 수 없습니다 (recoveryoption.api를 통해서만 접근)");

  // ── supportprogram 모듈 ──
  @ArchTest
  static final ArchRule supportprogram_internal_is_not_accessed_from_outside =
      noClasses()
          .that()
          .resideOutsideOfPackage("..supportprogram..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..supportprogram.internal..")
          .because("supportprogram.internal은 supportprogram 모듈 내부에서만 사용해야 합니다");

  @ArchTest
  static final ArchRule supportprogram_domain_is_not_accessed_from_outside =
      noClasses()
          .that()
          .resideOutsideOfPackage("..supportprogram..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..supportprogram.domain..")
          .because(
              "supportprogram.domain은 supportprogram 모듈 밖에서 직접 참조할 수 없습니다 (supportprogram.api를 통해서만 접근)");

  // ── packet 모듈 ──
  @ArchTest
  static final ArchRule packet_internal_is_not_accessed_from_outside =
      noClasses()
          .that()
          .resideOutsideOfPackage("..packet..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..packet.internal..")
          .because("packet.internal은 packet 모듈 내부에서만 사용해야 합니다");

  @ArchTest
  static final ArchRule packet_domain_is_not_accessed_from_outside =
      noClasses()
          .that()
          .resideOutsideOfPackage("..packet..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..packet.domain..")
          .because("packet.domain은 packet 모듈 밖에서 직접 참조할 수 없습니다 (packet.api를 통해서만 접근)");

  // ── consultation 모듈 ──
  @ArchTest
  static final ArchRule consultation_internal_is_not_accessed_from_outside =
      noClasses()
          .that()
          .resideOutsideOfPackage("..consultation..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..consultation.internal..")
          .because("consultation.internal은 consultation 모듈 내부에서만 사용해야 합니다");

  @ArchTest
  static final ArchRule consultation_domain_is_not_accessed_from_outside =
      noClasses()
          .that()
          .resideOutsideOfPackage("..consultation..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..consultation.domain..")
          .because(
              "consultation.domain은 consultation 모듈 밖에서 직접 참조할 수 없습니다 (consultation.api를 통해서만 접근)");
}
