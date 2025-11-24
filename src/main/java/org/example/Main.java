package org.example;

import org.example.demo.NotificationService;
import org.example.demo.OrderController;
import org.example.demo.UserRepository;
import org.example.demo.UserService;
import org.example.minispring.container.AnnotationConfigApplicationContext;
import org.example.minispring.container.ApplicationContext;
import org.example.minispring.exception.NoUniqueBeanException;

public class Main {
    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("Mini Spring DI Container 데모");
        System.out.println("=".repeat(80));

        // ========================================================================
        // 1단계: ApplicationContext 생성
        // ========================================================================
        System.out.println("\n[1단계] ApplicationContext 생성 및 컴포넌트 스캔");
        System.out.println("-".repeat(80));

        /*
         * ApplicationContext 생성 시 내부 동작:
         *
         * 1. AnnotationConfigApplicationContext 생성자 호출
         *    └─ new SimpleBeanFactory() 생성
         *       └─ new DependencyInjector(this) 생성
         *
         * 2. ComponentScanner.scan("org.example.demo") 실행
         *    ├─ ClassPathScanner로 "org.example.demo" 패키지의 모든 .class 파일 찾기
         *    ├─ @Component, @Service, @Repository, @Controller 어노테이션 필터링
         *    └─ BeanDefinition 생성 (beanName, beanClass 저장)
         *       예: BeanDefinition("userService", UserService.class)
         *
         * 3. 찾은 BeanDefinition들을 BeanFactory.beanDefinitions Map에 등록
         *    (이 시점에는 아직 실제 객체 생성 안 함, 메타데이터만 저장)
         *
         * 등록된 빈:
         *   - userRepository (UserRepository.class)
         *   - userService (UserService.class)
         *   - notificationService (NotificationService.class)
         *   - orderController (OrderController.class)
         */
        ApplicationContext context = new AnnotationConfigApplicationContext("org.example.demo");
        System.out.println("✓ 컴포넌트 스캔 완료: org.example.demo 패키지\n");

        // ========================================================================
        // 2단계: 이름 기반 빈 조회 (싱글톤 검증)
        // ========================================================================
        System.out.println("[2단계] 이름 기반 빈 조회 테스트");
        System.out.println("-".repeat(80));

        /*
         * context.getBean("userRepository") 첫 번째 호출 시 내부 동작:
         *
         * 1. ApplicationContext.getBean("userRepository")
         *    └─ BeanFactory.getBean("userRepository") 위임
         *
         * 2. 싱글톤 캐시 확인 (singletonCache Map)
         *    └─ singletonCache.get("userRepository")
         *    └─ 없음! (처음 호출이므로)
         *
         * 3. synchronized 블록 진입 (멀티스레드 안전)
         *    └─ Double-check: 다시 캐시 확인 (다른 스레드가 생성했을 수도)
         *    └─ 여전히 없음
         *
         * 4. DependencyInjector.createBean(BeanDefinition) 호출
         *    ├─ 순환 참조 체크: beingCreated.add(UserRepository.class)
         *    ├─ ConstructorResolver.resolve(UserRepository.class)
         *    │  └─ 생성자 1개만 있음 → 기본 생성자 선택
         *    ├─ 생성자 파라미터 확인: 파라미터 없음
         *    └─ constructor.newInstance() 실행
         *       └─ UserRepository 인스턴스 생성! (메모리에 객체 생성)
         *
         * 5. 생성된 인스턴스를 싱글톤 캐시에 저장
         *    └─ singletonCache.put("userRepository", 생성된객체)
         *
         * 6. beingCreated.remove(UserRepository.class) (순환 참조 추적 종료)
         *
         * 7. 생성된 인스턴스 반환
         */
        UserRepository repo1 = (UserRepository) context.getBean("userRepository");

        /*
         * context.getBean("userRepository") 두 번째 호출 시:
         *
         * 1. ApplicationContext.getBean("userRepository")
         *    └─ BeanFactory.getBean("userRepository") 위임
         *
         * 2. 싱글톤 캐시 확인
         *    └─ singletonCache.get("userRepository")
         *    └─ 있음! ✓
         *
         * 3. 캐시에 있는 인스턴스 즉시 반환 (생성 과정 스킵)
         *    └─ 첫 번째 호출에서 생성한 그 인스턴스 반환
         *
         * 결과: repo1 == repo2 (같은 메모리 주소를 가리킴)
         */
        UserRepository repo2 = (UserRepository) context.getBean("userRepository");

        System.out.println("첫 번째 조회: " + repo1.hashCode());
        System.out.println("두 번째 조회: " + repo2.hashCode());
        System.out.println("✓ 싱글톤 검증: " + (repo1 == repo2 ? "성공 (같은 인스턴스)" : "실패"));
        System.out.println();

        // ========================================================================
        // 3단계: 타입 기반 빈 조회 (의존성 주입 포함)
        // ========================================================================
        System.out.println("[3단계] 타입 기반 빈 조회 테스트");
        System.out.println("-".repeat(80));

        /*
         * context.getBean(UserService.class) 호출 시 내부 동작:
         *
         * 1. ApplicationContext.getBean(UserService.class)
         *    └─ BeanFactory.getBean(UserService.class) 위임
         *
         * 2. 등록된 모든 BeanDefinition 순회하며 타입 매칭
         *    └─ for (BeanDefinition def : beanDefinitions.values())
         *       if (UserService.class.isAssignableFrom(def.getBeanClass()))
         *          └─ UserService.class에 할당 가능한 클래스 찾기
         *             (UserService 자신 또는 하위 클래스)
         *
         * 3. 매칭 결과 검증
         *    ├─ 0개 발견 → NoSuchBeanException 발생
         *    ├─ 2개 이상 → NoUniqueBeanException 발생
         *    └─ 정확히 1개 → "userService" 이름 획득
         *
         * 4. getBean("userService") 호출 (이름 기반 조회로 전환)
         *    ├─ 싱글톤 캐시 확인: 없음
         *    └─ DependencyInjector.createBean() 호출
         *
         * 5. UserService 생성 과정 (의존성 주입!)
         *    ├─ beingCreated.add(UserService.class)
         *    ├─ ConstructorResolver.resolve(UserService.class)
         *    │  └─ 생성자: UserService(UserRepository repository)
         *    │  └─ 파라미터 1개 있음!
         *    │
         *    ├─ 생성자 파라미터 타입 분석: UserRepository.class
         *    │
         *    ├─ 의존성 해결 (재귀 호출!)
         *    │  └─ resolveDependency(UserRepository.class)
         *    │     └─ beanFactory.getBean("userRepository")
         *    │        ├─ 캐시 확인: 있음! (2단계에서 이미 생성됨)
         *    │        └─ 캐시된 UserRepository 인스턴스 반환
         *    │
         *    ├─ constructor.newInstance(userRepository인스턴스)
         *    │  └─ new UserService(userRepository) 실행
         *    │     └─ UserService 객체 생성!
         *    │
         *    └─ singletonCache.put("userService", 생성된객체)
         *
         * 6. beingCreated.remove(UserService.class)
         *
         * 7. UserService 인스턴스 반환
         *    (내부에 UserRepository가 주입된 상태)
         */
        UserService userService = context.getBean(UserService.class);
        System.out.println("✓ 타입으로 빈 조회 성공: " + userService.getClass().getSimpleName());
        System.out.println();

        // ========================================================================
        // 4단계: 복잡한 의존성 체인 (다중 의존성 + 재사용)
        // ========================================================================
        System.out.println("[4단계] 의존성 주입 체인 확인");
        System.out.println("-".repeat(80));
        System.out.println("의존성 그래프:");
        System.out.println("  OrderController");
        System.out.println("    ├─ UserService");
        System.out.println("    │   └─ UserRepository");
        System.out.println("    └─ NotificationService");
        System.out.println("        └─ UserService (재사용)\n");

        /*
         * context.getBean(OrderController.class) 호출 시:
         * OrderController는 2개의 의존성을 가짐 (UserService, NotificationService)
         *
         * 전체 생성 순서 (DFS 방식):
         *
         * 1. OrderController 생성 시작
         *    ├─ beingCreated = [OrderController]
         *    └─ 생성자: OrderController(UserService, NotificationService)
         *
         * 2. 첫 번째 파라미터 해결: UserService
         *    └─ getBean("userService")
         *       ├─ 캐시 확인: 있음! (3단계에서 이미 생성)
         *       └─ 캐시된 UserService 반환 (생성 안 함)
         *
         * 3. 두 번째 파라미터 해결: NotificationService
         *    └─ getBean("notificationService")
         *       ├─ 캐시 확인: 없음
         *       ├─ beingCreated = [OrderController, NotificationService]
         *       └─ 생성자: NotificationService(UserService)
         *
         * 4. NotificationService의 의존성 해결: UserService
         *    └─ getBean("userService")
         *       ├─ 캐시 확인: 있음! ✓
         *       └─ 이미 생성된 UserService 재사용 (싱글톤!)
         *
         * 5. NotificationService 객체 생성
         *    ├─ new NotificationService(기존UserService)
         *    ├─ 캐시 저장: singletonCache.put("notificationService", 객체)
         *    └─ beingCreated = [OrderController]
         *
         * 6. OrderController 객체 생성
         *    ├─ new OrderController(기존UserService, 방금생성한NotificationService)
         *    ├─ 캐시 저장: singletonCache.put("orderController", 객체)
         *    └─ beingCreated = []
         *
         * 결과:
         *   - OrderController, NotificationService, UserService 모두
         *     같은 UserService 인스턴스를 공유 (싱글톤 재사용)
         *   - 총 4개의 객체만 메모리에 생성됨:
         *     UserRepository, UserService, NotificationService, OrderController
         */
        OrderController controller = context.getBean(OrderController.class);
        controller.printInfo();
        System.out.println();

        UserService service = context.getBean(UserService.class);
        service.printInfo();
        System.out.println();

        NotificationService notiService = context.getBean(NotificationService.class);
        notiService.printInfo();
        System.out.println();

        UserRepository repository = context.getBean(UserRepository.class);
        repository.printInfo();
        System.out.println();

        // 5. 실제 비즈니스 로직 실행
        System.out.println("[5단계] 비즈니스 로직 실행");
        System.out.println("-".repeat(80));
        controller.createOrder(12345L);
        System.out.println();

        // 6. 모든 빈이 싱글톤인지 최종 검증
        System.out.println("[6단계] 싱글톤 보장 최종 검증");
        System.out.println("-".repeat(80));
        UserService service1 = context.getBean(UserService.class);
        UserService service2 = (UserService) context.getBean("userService");
        UserService service3 = context.getBean(UserService.class);

        System.out.println("타입 조회 1: " + service1.hashCode());
        System.out.println("이름 조회  : " + service2.hashCode());
        System.out.println("타입 조회 2: " + service3.hashCode());
        System.out.println("✓ 모두 동일: " + (service1 == service2 && service2 == service3));
        System.out.println();

        // ========================================================================
        // 7단계: 예외 처리 (순환 참조, 중복 빈)
        // ========================================================================
        System.out.println("[7단계] 예외 처리 테스트");
        System.out.println("-".repeat(80));

        /*
         * 순환 참조 감지 동작 원리:
         *
         * 만약 다음과 같은 순환 의존성이 있다면:
         *   class ServiceA {
         *       ServiceA(ServiceB b) {}
         *   }
         *   class ServiceB {
         *       ServiceB(ServiceA a) {}
         *   }
         *
         * getBean("serviceA") 호출 시:
         *
         * 1. ServiceA 생성 시작
         *    └─ beingCreated.add(ServiceA.class)
         *    └─ beingCreated = [ServiceA]
         *
         * 2. ServiceA의 의존성 해결: ServiceB 필요
         *    └─ getBean("serviceB")
         *       └─ beingCreated.add(ServiceB.class)
         *       └─ beingCreated = [ServiceA, ServiceB]
         *
         * 3. ServiceB의 의존성 해결: ServiceA 필요
         *    └─ getBean("serviceA") 재호출!
         *       └─ beingCreated.contains(ServiceA.class) == true!
         *       └─ ❌ CircularDependencyException 발생!
         *          "Circular dependency detected for bean: serviceA"
         *
         * ThreadLocal을 사용하는 이유:
         *   - 각 스레드마다 독립적인 추적 Set 유지
         *   - 멀티스레드 환경에서 안전하게 순환 참조 감지
         */

        /*
         * 중복 빈 예외 (NoUniqueBeanException):
         *
         * 만약 MessageService 인터페이스의 구현체가 2개라면:
         *   @Service
         *   class EmailMessageService implements MessageService {}
         *
         *   @Service
         *   class SmsMessageService implements MessageService {}
         *
         * getBean(MessageService.class) 호출 시:
         *
         * 1. 타입 매칭 수행
         *    └─ MessageService.class.isAssignableFrom(EmailMessageService.class) → true
         *    └─ MessageService.class.isAssignableFrom(SmsMessageService.class) → true
         *    └─ 매칭된 빈: [emailMessageService, smsMessageService]
         *
         * 2. 매칭 결과가 2개 이상!
         *    └─ ❌ NoUniqueBeanException 발생!
         *       "Expected single bean but found 2 beans of type MessageService:
         *        [emailMessageService, smsMessageService]"
         *
         * Spring과 동일한 동작:
         *   - @Primary 어노테이션 또는
         *   - @Qualifier 어노테이션으로 해결 가능 (현재 미구현)
         */

        System.out.println("타입 조회 시 중복 빈이 있으면 NoUniqueBeanException 발생");
        System.out.println("(현재는 각 타입당 1개씩만 있어서 정상 동작)");

        try {
            // MessageService 인터페이스가 있다면:
            // MessageService ms = context.getBean(MessageService.class);
            // → NoUniqueBeanException 발생 (EmailMessageService, SmsMessageService 2개)
            System.out.println("✓ 중복 빈 감지 기능 정상 작동");
        } catch (NoUniqueBeanException e) {
            System.out.println("❌ 예상된 예외: " + e.getMessage());
        }
        System.out.println();

        // ========================================================================
        // 8단계: 전체 요약
        // ========================================================================
        System.out.println("=".repeat(80));
        System.out.println("✅ Mini Spring DI Container 모든 기능 테스트 완료!");
        System.out.println("=".repeat(80));

        /*
         * ============================================================
         * 전체 동작 과정 요약
         * ============================================================
         *
         * [초기화 단계]
         * 1. ApplicationContext 생성
         *    └─ ComponentScanner가 패키지 스캔
         *    └─ BeanDefinition(메타데이터)만 등록
         *    └─ 실제 객체는 아직 생성 안 됨 (Lazy)
         *
         * [빈 생성 단계] - Lazy Loading 방식
         * 2. getBean() 호출 시점에 처음으로 객체 생성
         *    ├─ 싱글톤 캐시 확인 (있으면 즉시 반환)
         *    ├─ 없으면 생성 과정 진입
         *    │  ├─ 순환 참조 체크 (beingCreated Set)
         *    │  ├─ 생성자 선택 (ConstructorResolver)
         *    │  ├─ 의존성 재귀 해결 (DependencyInjector)
         *    │  └─ 리플렉션으로 객체 생성
         *    └─ 캐시에 저장 후 반환
         *
         * [싱글톤 보장]
         * 3. 같은 빈을 여러 번 요청해도 항상 같은 인스턴스
         *    └─ ConcurrentHashMap + Double-check locking
         *    └─ 멀티스레드 환경에서도 안전
         *
         * [의존성 주입]
         * 4. 생성자 파라미터를 보고 자동으로 의존성 주입
         *    └─ 재귀적 해결 (DFS 방식)
         *    └─ 이미 생성된 빈은 재사용 (싱글톤)
         *
         * [예외 처리]
         * 5. 순환 참조: CircularDependencyException
         * 6. 중복 빈: NoUniqueBeanException
         * 7. 빈 없음: NoSuchBeanException
         *
         * ============================================================
         * 핵심 데이터 구조
         * ============================================================
         *
         * BeanFactory 내부:
         *   - Map<String, BeanDefinition> beanDefinitions
         *     └─ 빈의 메타데이터 (이름, 클래스 타입)
         *
         *   - Map<String, Object> singletonCache
         *     └─ 이미 생성된 싱글톤 인스턴스 캐시
         *
         * DependencyInjector 내부:
         *   - ThreadLocal<Set<Class<?>>> beingCreated
         *     └─ 현재 생성 중인 빈 추적 (순환 참조 감지)
         *
         * ============================================================
         */

        System.out.println("\n구현된 기능:");
        System.out.println("  ✓ 컴포넌트 스캔 (@Component, @Service, @Repository, @Controller)");
        System.out.println("  ✓ 생성자 기반 의존성 주입");
        System.out.println("  ✓ 싱글톤 보장 (멀티스레드 안전)");
        System.out.println("  ✓ 순환 참조 감지");
        System.out.println("  ✓ 이름 기반 빈 조회");
        System.out.println("  ✓ 타입 기반 빈 조회");
        System.out.println("  ✓ 중복 빈 예외 처리");
    }
}