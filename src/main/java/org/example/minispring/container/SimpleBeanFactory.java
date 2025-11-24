package org.example.minispring.container;

import org.example.minispring.bean.BeanDefinition;
import org.example.minispring.exception.NoSuchBeanException;
import org.example.minispring.exception.NoUniqueBeanException;
import org.example.minispring.injector.DependencyInjector;
import org.example.minispring.lifecycle.BeanLifecycleManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BeanFactory의 기본 구현체
 *
 * 핵심 기능:
 *   1. 빈 메타데이터 저장 (beanDefinitions)
 *   2. 싱글톤 빈 캐싱 (singletonCache)
 *   3. 의존성 주입을 통한 빈 생성
 *   4. 이름/타입 기반 빈 조회
 *
 * 스레드 안전성:
 *   - ConcurrentHashMap 사용으로 멀티스레드 환경에서도 안전
 *   - Double-Checked Locking으로 싱글톤 보장
 */
public class SimpleBeanFactory implements BeanFactory {

    // ================================================================
    // 빈 메타데이터 저장소
    // ================================================================
    // Key: 빈 이름 (예: "userService")
    // Value: BeanDefinition (빈의 메타데이터)
    //
    // ConcurrentHashMap 사용 이유:
    //   - 멀티스레드 환경에서 안전
    //   - 읽기 작업은 락 없이 동시 실행 가능
    private final Map<String, BeanDefinition> beanDefinitions = new ConcurrentHashMap<>();

    // ================================================================
    // 싱글톤 빈 캐시
    // ================================================================
    // Key: 빈 이름 (예: "userService")
    // Value: 실제 생성된 빈 인스턴스
    //
    // 싱글톤 보장:
    //   - 같은 이름의 빈은 한 번만 생성
    //   - 이후 요청은 캐시된 인스턴스 반환
    private final Map<String, Object> singletonCache = new ConcurrentHashMap<>();

    // ================================================================
    // 의존성 주입 담당 객체
    // ================================================================
    // DependencyInjector:
    //   - 생성자 분석
    //   - 의존성 해결 (재귀)
    //   - 순환 참조 감지
    private final DependencyInjector dependencyInjector;

    // ================================================================
    // 생명주기 관리 담당 객체
    // ================================================================
    // BeanLifecycleManager:
    //   - @PostConstruct 메서드 호출
    //   - @PreDestroy 메서드 호출
    private final BeanLifecycleManager lifecycleManager;

    public SimpleBeanFactory() {
        // DependencyInjector에 자기 자신(this) 전달
        // → 의존성 해결 시 이 BeanFactory를 사용하여 빈 조회
        this.dependencyInjector = new DependencyInjector(this);
        this.lifecycleManager = new BeanLifecycleManager();
    }

    /**
     * 빈 메타데이터를 저장소에 등록
     *
     * @param definition 등록할 빈 정의 정보
     */
    @Override
    public void registerBeanDefinition(BeanDefinition definition) {
        // 빈 이름을 키로 BeanDefinition 저장
        // 예: "userService" → BeanDefinition(userService, UserService.class)
        beanDefinitions.put(definition.getBeanName(), definition);
    }

    /**
     * 특정 이름의 빈이 등록되어 있는지 확인
     *
     * @param beanName 확인할 빈 이름
     * @return 빈이 등록되어 있으면 true
     */
    @Override
    public boolean containsBean(String beanName) {
        return beanDefinitions.containsKey(beanName);
    }

    /**
     * 이름으로 빈 조회 (싱글톤 보장)
     *
     * 동작 흐름:
     *   1. 빈 등록 여부 확인
     *   2. 캐시 확인 (있으면 즉시 반환)
     *   3. 없으면 생성 후 캐시에 저장
     *
     * @param beanName 조회할 빈 이름
     * @return 빈 인스턴스 (싱글톤)
     * @throws NoSuchBeanException 빈이 등록되지 않은 경우
     */
    @Override
    public Object getBean(String beanName) {
        // ================================================================
        // 1단계: 빈이 등록되어 있는지 확인
        // ================================================================
        if (!containsBean(beanName)) {
            throw new NoSuchBeanException("No bean found with name: " + beanName);
        }

        // ================================================================
        // 2단계: 싱글톤 캐시 확인 (1차 체크)
        // ================================================================
        // 이미 생성된 빈이 있으면 즉시 반환
        // 이유: 싱글톤 - 같은 빈은 한 번만 생성
        Object cached = singletonCache.get(beanName);
        if (cached != null) {
            return cached;  // 캐시 히트! 빠른 반환
        }

        // ================================================================
        // 3단계: 빈이 캐시에 없음 → 생성 필요
        // ================================================================
        // synchronized 블록으로 스레드 안전성 보장
        synchronized (this) {

            // ============================================================
            // 3-1. Double-Checked Locking (2차 체크)
            // ============================================================
            // 왜 필요한가?
            //   1) 스레드 A가 synchronized 진입 전 대기
            //   2) 그 사이 스레드 B가 빈을 생성하고 캐시에 저장
            //   3) 스레드 A가 락 획득 후 다시 확인 필요
            //
            // 다시 체크하지 않으면?
            //   → 같은 빈이 중복 생성됨 (싱글톤 위반!)
            cached = singletonCache.get(beanName);
            if (cached != null) {
                return cached;  // 다른 스레드가 이미 생성했음
            }

            // ============================================================
            // 3-2. 빈 생성 (DependencyInjector에 위임)
            // ============================================================
            BeanDefinition definition = beanDefinitions.get(beanName);
            Object bean = createBean(definition);

            // ============================================================
            // 3-3. 생성된 빈을 캐시에 저장
            // ============================================================
            // 다음 요청부터는 2단계에서 즉시 반환됨
            singletonCache.put(beanName, bean);

            // ============================================================
            // 3-4. @PostConstruct 메서드 호출
            // ============================================================
            // 빈 생성 및 의존성 주입 완료 후 초기화 콜백 실행
            // 순서: 생성자 → 의존성 주입 → @PostConstruct
            lifecycleManager.invokePostConstruct(bean);

            return bean;
        }
    }

    /**
     * 타입으로 빈 조회 (싱글톤 보장)
     *
     * 동작 흐름:
     *   1. 모든 빈 정의를 순회하며 타입 매칭
     *   2. 매칭되는 빈이 정확히 1개인지 확인
     *   3. 이름으로 빈 조회 (getBean(String) 호출)
     *
     * @param type 조회할 빈의 타입
     * @param <T> 반환 타입
     * @return 타입에 매칭되는 빈 인스턴스
     * @throws NoSuchBeanException 해당 타입의 빈이 없는 경우
     * @throws NoUniqueBeanException 같은 타입의 빈이 2개 이상인 경우
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type) {
        List<String> matchingBeanNames = new ArrayList<>();

        // ================================================================
        // 1단계: 모든 빈 정의를 순회하며 타입이 일치하는 빈 찾기
        // ================================================================
        for (BeanDefinition definition : beanDefinitions.values()) {

            // ============================================================
            // 타입 매칭 확인: isAssignableFrom()
            // ============================================================
            // type.isAssignableFrom(definition.getBeanClass())의 의미:
            //   - "definition.getBeanClass()가 type에 할당 가능한가?"
            //   - 상속 관계와 인터페이스 구현도 고려
            //
            // 예시 1: 정확한 타입 매칭
            //   type = UserService.class
            //   definition.getBeanClass() = UserService.class
            //   → true (일치)
            //
            // 예시 2: 인터페이스 조회
            //   type = UserRepository.class (인터페이스)
            //   definition.getBeanClass() = UserRepositoryImpl.class
            //   → true (구현체)
            //
            // 예시 3: 부모 클래스 조회
            //   type = Animal.class
            //   definition.getBeanClass() = Dog.class
            //   → true (상속)
            if (type.isAssignableFrom(definition.getBeanClass())) {
                matchingBeanNames.add(definition.getBeanName());
            }
        }

        // ================================================================
        // 2단계: 매칭되는 빈의 개수 확인
        // ================================================================

        // Case 1: 매칭되는 빈이 없음
        if (matchingBeanNames.isEmpty()) {
            throw new NoSuchBeanException("No bean found with type: " + type.getName());
        }

        // Case 2: 매칭되는 빈이 2개 이상 (모호함)
        // 예:
        //   getBean(UserRepository.class)
        //   → UserRepositoryImpl1, UserRepositoryImpl2 둘 다 매칭
        //   → 어느 것을 반환해야 할지 모름!
        if (matchingBeanNames.size() > 1) {
            throw new NoUniqueBeanException(
                "Expected single bean but found " + matchingBeanNames.size() +
                " beans of type " + type.getName() + ": " + matchingBeanNames
            );
        }

        // ================================================================
        // 3단계: 유일한 매칭 빈을 이름으로 조회
        // ================================================================
        // getBean(String)을 재사용:
        //   - 싱글톤 보장 로직 활용
        //   - 코드 중복 제거
        return (T) getBean(matchingBeanNames.get(0));
    }

    /**
     * 빈 생성 로직 분기
     *
     * @param definition 생성할 빈의 메타데이터
     * @return 생성된 빈 인스턴스
     */
    private Object createBean(BeanDefinition definition) {
        // ================================================================
        // BeanMethodDefinition인 경우: @Bean 메서드 호출
        // ================================================================
        if (definition instanceof org.example.minispring.processor.ConfigurationClassProcessor.BeanMethodDefinition) {
            return createBeanFromMethod(
                (org.example.minispring.processor.ConfigurationClassProcessor.BeanMethodDefinition) definition
            );
        }

        // ================================================================
        // 일반 BeanDefinition: 생성자 주입 방식
        // ================================================================
        // DependencyInjector가:
        //   1. 생성자 선택
        //   2. 의존성 해결 (재귀)
        //   3. 순환 참조 감지
        //   4. 리플렉션으로 인스턴스 생성
        return dependencyInjector.createBean(definition);
    }

    /**
     * @Bean 메서드 호출하여 빈 생성
     *
     * @param beanMethodDef @Bean 메서드의 BeanDefinition
     * @return @Bean 메서드의 반환값 (빈 인스턴스)
     */
    private Object createBeanFromMethod(
        org.example.minispring.processor.ConfigurationClassProcessor.BeanMethodDefinition beanMethodDef
    ) {
        try {
            // ============================================================
            // 1단계: @Configuration 클래스 인스턴스 가져오기
            // ============================================================
            // 예: @Configuration 클래스가 "appConfig"라는 이름으로 등록됨
            //     → getBean("appConfig")로 인스턴스 획득
            Object configInstance = getBean(beanMethodDef.getConfigBeanName());

            // ============================================================
            // 2단계: @Bean 메서드 정보 가져오기
            // ============================================================
            java.lang.reflect.Method method = beanMethodDef.getMethod();
            method.setAccessible(true);  // private 메서드도 호출 가능하도록

            // ============================================================
            // 3단계: 메서드 파라미터 의존성 해결
            // ============================================================
            Class<?>[] parameterTypes = method.getParameterTypes();
            Object[] args = new Object[parameterTypes.length];

            for (int i = 0; i < parameterTypes.length; i++) {
                // 파라미터 타입으로 빈 조회 (의존성 주입)
                // 예: public DataSource dataSource(Config config)
                //     → config = getBean(Config.class)
                args[i] = getBean(parameterTypes[i]);
            }

            // ============================================================
            // 4단계: @Bean 메서드 호출하여 빈 생성
            // ============================================================
            // 예: configInstance.dataSource(args...)
            //     → HikariDataSource 인스턴스 반환
            return method.invoke(configInstance, args);

        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to create bean from @Bean method: " + beanMethodDef.getBeanName(),
                e
            );
        }
    }

    /**
     * 모든 싱글톤 빈 인스턴스 조회
     *
     * @return 캐시된 모든 빈 인스턴스의 컬렉션
     */
    @Override
    public Collection<Object> getAllBeans() {
        // 싱글톤 캐시에 저장된 모든 빈 인스턴스 반환
        // @PreDestroy 호출 시 사용
        return singletonCache.values();
    }
}
