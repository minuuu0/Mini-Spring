package org.example.minispring.injector;

import org.example.minispring.bean.BeanDefinition;
import org.example.minispring.container.BeanFactory;
import org.example.minispring.exception.CircularDependencyException;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;

/**
 * 생성자 기반 의존성 주입을 수행하는 핵심 컴포넌트
 *
 * 주요 기능:
 *   1. 리플렉션을 사용한 객체 생성
 *   2. 생성자 파라미터 분석 및 의존성 해결 (재귀)
 *   3. 순환 참조 감지 (ThreadLocal 사용)
 */
public class DependencyInjector {

    private final BeanFactory beanFactory;
    private final ConstructorResolver constructorResolver;

    /**
     * 현재 생성 중인 빈들을 추적하는 ThreadLocal Set
     *
     * ThreadLocal 사용 이유:
     *   - 각 스레드마다 독립적인 Set 보유
     *   - 멀티스레드 환경에서 순환 참조 감지가 안전
     *   - 스레드 A와 B가 동시에 빈을 생성해도 서로 영향 없음
     *
     * Set<Class<?>> 내용:
     *   - 현재 생성 중인 빈의 클래스들
     *   - 예: [UserService.class, OrderService.class]
     */
    private final ThreadLocal<Set<Class<?>>> beingCreated = ThreadLocal.withInitial(HashSet::new);

    public DependencyInjector(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        this.constructorResolver = new ConstructorResolver();
    }

    /**
     * BeanDefinition을 실제 객체로 생성 (의존성 주입 포함)
     *
     * @param definition 생성할 빈의 메타데이터
     * @return 생성된 빈 인스턴스
     * @throws CircularDependencyException 순환 참조 발견 시
     */
    public Object createBean(BeanDefinition definition) {
        Class<?> beanClass = definition.getBeanClass();

        // ================================================================
        // 1단계: 순환 참조 체크
        // ================================================================
        // 순환 참조란?
        //   A → B → A 처럼 빈들이 서로를 의존하는 상황
        //
        // 예시:
        //   class ServiceA {
        //       ServiceA(ServiceB b) {}  // B 필요
        //   }
        //   class ServiceB {
        //       ServiceB(ServiceA a) {}  // A 필요
        //   }
        //
        // 호출 스택:
        //   1) createBean(ServiceA)
        //      → beingCreated = [ServiceA]
        //   2) createBean(ServiceB) (재귀)
        //      → beingCreated = [ServiceA, ServiceB]
        //   3) createBean(ServiceA) 다시 호출! (재귀)
        //      → beingCreated.contains(ServiceA) == true
        //      → ❌ CircularDependencyException!
        if (beingCreated.get().contains(beanClass)) {
            throw new CircularDependencyException(
                "Circular dependency detected for bean: " + definition.getBeanName()
            );
        }

        // ================================================================
        // 2단계: 현재 빈을 "생성 중" 목록에 추가
        // ================================================================
        // 이 빈이 의존하는 다른 빈을 생성할 때 순환 참조 감지 가능
        beingCreated.get().add(beanClass);

        try {
            // ============================================================
            // 3단계: 사용할 생성자 선택
            // ============================================================
            // ConstructorResolver가 다음 우선순위로 선택:
            //   1) @Autowired 붙은 생성자
            //   2) 생성자 1개뿐이면 자동
            //   3) 기본 생성자
            Constructor<?> constructor = constructorResolver.resolve(beanClass);

            // ============================================================
            // 4단계: private 생성자도 접근 가능하도록 설정
            // ============================================================
            // setAccessible(true): 리플렉션으로 private 멤버 접근 허용
            // 이유: 일부 클래스는 싱글톤 패턴 등으로 private 생성자 사용
            constructor.setAccessible(true);

            // ============================================================
            // 5단계: 생성자 파라미터 확인 및 처리
            // ============================================================

            // Case 1: 파라미터 없는 생성자 (의존성 없음)
            if (constructor.getParameterCount() == 0) {
                // 예: new UserRepository()
                // 간단히 객체 생성 후 반환
                return constructor.newInstance();
            }

            // Case 2: 파라미터 있는 생성자 (의존성 있음)
            // 예: UserService(UserRepository repo, EmailService email)

            // ============================================================
            // 6단계: 생성자 파라미터 타입 분석
            // ============================================================
            // 예: [UserRepository.class, EmailService.class]
            Class<?>[] parameterTypes = constructor.getParameterTypes();

            // 각 파라미터에 주입할 의존성 객체를 저장할 배열
            Object[] dependencies = new Object[parameterTypes.length];

            // ============================================================
            // 7단계: 각 파라미터에 대한 의존성 해결 (재귀!)
            // ============================================================
            for (int i = 0; i < parameterTypes.length; i++) {
                // 예: parameterTypes[0] = UserRepository.class
                //
                // resolveDependency() 호출:
                //   1) UserRepository → "userRepository" 이름 생성
                //   2) beanFactory.getBean("userRepository") 호출
                //   3) 캐시에 없으면 createBean() 재귀 호출!
                //   4) 생성된 객체 반환
                dependencies[i] = resolveDependency(parameterTypes[i]);
            }

            // ============================================================
            // 8단계: 의존성을 주입하며 객체 생성
            // ============================================================
            // 예: new UserService(userRepository인스턴스, emailService인스턴스)
            //
            // constructor.newInstance(dependencies):
            //   1) 리플렉션으로 생성자 호출
            //   2) dependencies 배열의 객체들을 파라미터로 전달
            //   3) 새로운 인스턴스 반환
            return constructor.newInstance(dependencies);

        } catch (CircularDependencyException e) {
            // 순환 참조 예외는 그대로 전파
            throw e;

        } catch (Exception e) {
            // 기타 예외 (리플렉션 오류 등)는 RuntimeException으로 감싸서 던짐
            throw new RuntimeException("Failed to create bean: " + definition.getBeanName(), e);

        } finally {
            // ============================================================
            // 9단계: 빈 생성 완료 후 "생성 중" 목록에서 제거
            // ============================================================
            // 성공하든 실패하든 반드시 제거해야 함 (finally 블록)
            beingCreated.get().remove(beanClass);

            // ============================================================
            // 10단계: ThreadLocal 메모리 누수 방지
            // ============================================================
            // Set이 비었으면 ThreadLocal 자체를 제거
            // 이유: ThreadLocal은 스레드가 살아있는 동안 계속 메모리 점유
            //       사용 완료 후 명시적으로 제거해야 메모리 누수 방지
            if (beingCreated.get().isEmpty()) {
                beingCreated.remove();
            }
        }
    }

    /**
     * 타입으로부터 의존성 해결 (재귀적으로 빈 조회)
     *
     * @param type 의존하는 클래스 타입
     * @return 해당 타입의 빈 인스턴스
     */
    private Object resolveDependency(Class<?> type) {
        // ================================================================
        // 1. 클래스 타입 → 빈 이름 변환
        // ================================================================
        // UserRepository.class → "userRepository"
        String beanName = generateBeanName(type);

        // ================================================================
        // 2. BeanFactory에서 빈 조회
        // ================================================================
        // getBean() 내부 동작:
        //   1) 싱글톤 캐시 확인 → 있으면 즉시 반환
        //   2) 없으면 createBean() 재귀 호출
        //   3) 생성된 빈 캐시에 저장 후 반환
        //
        // 재귀 흐름 예시:
        //   createBean(OrderService)
        //   → resolveDependency(UserService)
        //      → getBean("userService")
        //         → createBean(UserService)
        //            → resolveDependency(UserRepository)
        //               → getBean("userRepository")
        //                  → createBean(UserRepository)
        //                     → (의존성 없음, 객체 생성)
        //                  ← UserRepository 인스턴스
        //            ← UserService 인스턴스
        //   ← OrderService 인스턴스
        return beanFactory.getBean(beanName);
    }

    /**
     * 클래스 타입으로부터 빈 이름 생성 (camelCase)
     *
     * @param clazz 클래스 타입
     * @return camelCase 형식의 빈 이름
     */
    private String generateBeanName(Class<?> clazz) {
        // UserService.class → "userService"
        // OrderController.class → "orderController"
        String simpleName = clazz.getSimpleName();
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }
}
