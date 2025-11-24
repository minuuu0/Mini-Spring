package org.example.minispring.container;

import org.example.minispring.bean.BeanDefinition;
import org.example.minispring.scanner.ComponentScanner;

import java.util.Set;

/**
 * 어노테이션 기반 스프링 컨테이너 구현체
 *
 * 역할:
 *   - 애플리케이션의 진입점 (사용자가 직접 생성하는 클래스)
 *   - 컴포넌트 스캔 + BeanFactory 조합
 *   - 전체 DI 컨테이너 라이프사이클 관리
 *
 * 동작 흐름:
 *   1. 생성자 호출 시 자동으로 컨테이너 초기화 (refresh)
 *   2. ComponentScanner로 패키지 스캔 → BeanDefinition 수집
 *   3. BeanFactory에 모든 BeanDefinition 등록
 *   4. 이후 getBean() 호출 시 필요한 빈 생성
 *
 * 사용 예시:
 *   ApplicationContext context =
 *       new AnnotationConfigApplicationContext("org.example.demo");
 *   UserService service = context.getBean(UserService.class);
 */
public class AnnotationConfigApplicationContext implements ApplicationContext {

    // ================================================================
    // 실제 빈 생성/관리를 담당하는 팩토리
    // ================================================================
    // Delegation 패턴:
    //   - ApplicationContext는 외부 API 제공
    //   - 실제 로직은 BeanFactory에 위임
    private final BeanFactory beanFactory;

    // ================================================================
    // 컴포넌트 스캔 담당 객체
    // ================================================================
    // 역할: 패키지에서 @Component 등 어노테이션이 붙은 클래스 찾기
    private final ComponentScanner componentScanner;

    /**
     * 애플리케이션 컨텍스트 생성 및 초기화
     *
     * @param basePackage 컴포넌트 스캔할 패키지 (예: "org.example.demo")
     */
    public AnnotationConfigApplicationContext(String basePackage) {
        // ================================================================
        // 1단계: 핵심 컴포넌트 생성
        // ================================================================
        this.beanFactory = new SimpleBeanFactory();
        this.componentScanner = new ComponentScanner();

        // ================================================================
        // 2단계: 컨테이너 초기화 (refresh)
        // ================================================================
        // Spring의 refresh()와 동일한 개념:
        //   - 컴포넌트 스캔
        //   - BeanDefinition 등록
        //   - 빈 생성 준비 완료
        refresh(basePackage);
    }

    /**
     * 컨테이너 초기화 (컴포넌트 스캔 + 빈 등록)
     *
     * @param basePackage 스캔할 패키지
     */
    private void refresh(String basePackage) {
        // ================================================================
        // 1단계: 컴포넌트 스캔 - BeanDefinition 수집
        // ================================================================
        // ComponentScanner가:
        //   1) ClassPathScanner로 모든 .class 파일 찾기
        //   2) @Component 등 어노테이션 필터링
        //   3) BeanDefinition 생성 (메타데이터만!)
        //
        // 예시 결과:
        //   [
        //     BeanDefinition("userService", UserService.class),
        //     BeanDefinition("userRepository", UserRepository.class),
        //     BeanDefinition("orderService", OrderService.class)
        //   ]
        Set<BeanDefinition> beanDefinitions = componentScanner.scan(basePackage);

        // ================================================================
        // 2단계: 모든 BeanDefinition을 BeanFactory에 등록
        // ================================================================
        // 주의: 아직 실제 빈 인스턴스는 생성하지 않음!
        //       getBean() 호출 시점에 Lazy하게 생성됨
        for (BeanDefinition definition : beanDefinitions) {
            beanFactory.registerBeanDefinition(definition);
        }
    }

    /**
     * 이름으로 빈 조회 (BeanFactory에 위임)
     *
     * @param beanName 빈 이름
     * @return 빈 인스턴스 (싱글톤)
     */
    @Override
    public Object getBean(String beanName) {
        return beanFactory.getBean(beanName);
    }

    /**
     * 타입으로 빈 조회 (BeanFactory에 위임)
     *
     * @param type 빈 타입
     * @param <T> 반환 타입
     * @return 타입에 매칭되는 빈 인스턴스 (싱글톤)
     */
    @Override
    public <T> T getBean(Class<T> type) {
        return beanFactory.getBean(type);
    }

    /**
     * 빈 존재 여부 확인 (BeanFactory에 위임)
     *
     * @param beanName 빈 이름
     * @return 빈이 등록되어 있으면 true
     */
    @Override
    public boolean containsBean(String beanName) {
        return beanFactory.containsBean(beanName);
    }
}
