package org.example.minispring.bean;

/**
 * 빈의 메타데이터를 담는 불변 객체
 * <p>
 * 역할:
 * - 빈의 정의 정보(이름, 타입)를 보관
 * - 실제 빈 인스턴스와 분리된 설계 (메타데이터 vs 실제 객체)
 *
 */
public class BeanDefinition {

    // ================================================================
    // 빈의 고유 이름 (컨테이너 내에서 유일)
    // ================================================================
    // 생성 규칙: 클래스명의 camelCase
    // 예: UserService → "userService"
    //     OrderRepository → "orderRepository"
    private final String beanName;

    // ================================================================
    // 빈의 실제 클래스 타입
    // ================================================================
    // 용도:
    //   - 리플렉션으로 인스턴스 생성
    //   - 타입 기반 빈 조회
    //   - 생성자 파라미터 타입 확인
    private final Class<?> beanClass;

    public BeanDefinition(String beanName, Class<?> beanClass) {
        this.beanName = beanName;
        this.beanClass = beanClass;
    }

    public String getBeanName() {
        return beanName;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }
}
