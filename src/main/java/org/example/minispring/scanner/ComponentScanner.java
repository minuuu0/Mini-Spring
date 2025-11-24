package org.example.minispring.scanner;

import org.example.minispring.annotation.Component;
import org.example.minispring.annotation.Configuration;
import org.example.minispring.annotation.Controller;
import org.example.minispring.annotation.Repository;
import org.example.minispring.annotation.Service;
import org.example.minispring.bean.BeanDefinition;

import java.util.HashSet;
import java.util.Set;

/**
 * 스프링 컴포넌트 어노테이션이 붙은 클래스를 찾아서 BeanDefinition으로 변환
 *
 * 역할:
 *   1. ClassPathScanner를 사용하여 모든 클래스 찾기
 *   2. 컴포넌트 어노테이션 필터링
 *   3. BeanDefinition(빈 메타데이터) 생성
 */
public class ComponentScanner {

    private final ClassPathScanner classPathScanner;

    public ComponentScanner() {
        this.classPathScanner = new ClassPathScanner();
    }

    /**
     * 패키지를 스캔하여 컴포넌트 빈의 정의 정보 수집
     *
     * @param basePackage 스캔할 패키지 (예: "org.example.demo")
     * @return BeanDefinition의 Set (빈의 메타데이터 모음)
     */
    public Set<BeanDefinition> scan(String basePackage) {
        Set<BeanDefinition> beanDefinitions = new HashSet<>();

        // ================================================================
        // 1단계: ClassPathScanner로 패키지 내 모든 클래스 로드
        // ================================================================
        // 결과 예시:
        //   - org.example.demo.UserService
        //   - org.example.demo.UserRepository
        //   - org.example.demo.NotificationService
        //   - org.example.demo.OrderController
        //   - org.example.demo.SomeUtilClass (어노테이션 없음)
        Set<Class<?>> classes = classPathScanner.scan(basePackage);

        // ================================================================
        // 2단계: 각 클래스를 순회하며 컴포넌트 어노테이션 체크
        // ================================================================
        for (Class<?> clazz : classes) {

            // ------------------------------------------------------------
            // 2-1. 컴포넌트 어노테이션이 있는지 확인
            // ------------------------------------------------------------
            // @Component, @Service, @Repository, @Controller 중 하나라도 있으면
            if (isComponent(clazz)) {

                // --------------------------------------------------------
                // 2-2. 빈 이름 생성 (클래스명의 camelCase)
                // --------------------------------------------------------
                // 예: UserService → "userService"
                //     OrderController → "orderController"
                String beanName = generateBeanName(clazz);

                // --------------------------------------------------------
                // 2-3. BeanDefinition 생성 및 추가
                // --------------------------------------------------------
                // BeanDefinition: 빈의 메타데이터
                //   - beanName: "userService"
                //   - beanClass: UserService.class
                //
                // 주의: 아직 실제 객체(인스턴스)는 생성하지 않음!
                //       나중에 getBean() 호출 시점에 생성됨 (Lazy Loading)
                beanDefinitions.add(new BeanDefinition(beanName, clazz));
            }
        }

        return beanDefinitions;
    }

    /**
     * 클래스에 컴포넌트 어노테이션이 있는지 확인
     *
     * @param clazz 검사할 클래스
     * @return 컴포넌트 어노테이션이 하나라도 있으면 true
     */
    private boolean isComponent(Class<?> clazz) {
        // ================================================================
        // 스프링 스테레오타입 어노테이션 확인
        // ================================================================
        // @Component: 일반 컴포넌트
        // @Service: 비즈니스 로직 레이어 (내부적으로 @Component 포함)
        // @Repository: 데이터 액세스 레이어 (내부적으로 @Component 포함)
        // @Controller: 프레젠테이션 레이어 (내부적으로 @Component 포함)
        // @Configuration: 설정 클래스 (내부적으로 @Component 포함)
        //
        // isAnnotationPresent(): 런타임에 리플렉션으로 어노테이션 확인
        //   - 클래스 바이트코드에 어노테이션이 남아있어야 함
        //   - 어노테이션에 @Retention(RetentionPolicy.RUNTIME) 필요
        return clazz.isAnnotationPresent(Component.class) ||
                clazz.isAnnotationPresent(Service.class) ||
                clazz.isAnnotationPresent(Repository.class) ||
                clazz.isAnnotationPresent(Controller.class) ||
                clazz.isAnnotationPresent(Configuration.class);
    }

    /**
     * 클래스 이름으로부터 빈 이름 생성 (camelCase 규칙)
     *
     * @param clazz 빈으로 등록할 클래스
     * @return camelCase 형식의 빈 이름
     */
    private String generateBeanName(Class<?> clazz) {
        // ================================================================
        // 빈 이름 생성 규칙 (Spring 규칙과 동일)
        // ================================================================
        // 1. 클래스의 단순 이름 가져오기 (패키지 제외)
        //    org.example.demo.UserService → "UserService"
        String simpleName = clazz.getSimpleName();

        // 2. 첫 글자를 소문자로 변환
        //    "UserService" → "userService"
        //
        // 계산 과정:
        //   1) simpleName.charAt(0) = 'U'
        //   2) Character.toLowerCase('U') = 'u'
        //   3) simpleName.substring(1) = "serService"
        //   4) 'u' + "serService" = "userService"
        //
        // 예시:
        //   OrderController → orderController
        //   UserRepository → userRepository
        //   APIService → aPIService (두 번째 글자도 대문자면 그대로)
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }
}
