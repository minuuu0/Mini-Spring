package org.example.minispring.container;

/**
 * 스프링 컨테이너의 최상위 인터페이스
 *
 * 역할:
 *   - 애플리케이션 전체의 빈 컨테이너 대표
 *   - BeanFactory의 기능을 사용자에게 노출
 *   - 애플리케이션 컨텍스트의 표준 계약 정의
 *
 * BeanFactory와의 차이:
 *   - BeanFactory: 빈의 생성/관리에 집중 (내부 구현)
 *   - ApplicationContext: 사용자가 사용하는 진입점 (외부 API)
 *   - 실제로는 BeanFactory를 내부적으로 사용
 *
 * 구현체:
 *   - AnnotationConfigApplicationContext: 어노테이션 기반 설정
 */
public interface ApplicationContext {

    /**
     * 이름으로 빈 조회
     *
     * @param beanName 빈 이름
     * @return 빈 인스턴스
     */
    Object getBean(String beanName);

    /**
     * 타입으로 빈 조회
     *
     * @param type 빈 타입
     * @param <T> 반환 타입
     * @return 타입에 매칭되는 빈 인스턴스
     */
    <T> T getBean(Class<T> type);

    /**
     * 빈 존재 여부 확인
     *
     * @param beanName 빈 이름
     * @return 빈이 존재하면 true
     */
    boolean containsBean(String beanName);
}
