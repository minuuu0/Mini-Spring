package org.example.minispring.container;

import org.example.minispring.bean.BeanDefinition;

/**
 * 빈 생성 및 관리를 담당하는 핵심 인터페이스
 *
 * 역할:
 *   - 빈의 등록, 조회, 생성을 추상화
 *   - DIP(의존성 역전 원칙) 준수: 구현체가 아닌 인터페이스에 의존
 *
 * 구현체:
 *   - SimpleBeanFactory: 기본 구현체 (싱글톤 + 의존성 주입)
 *
 * Spring의 BeanFactory와의 차이:
 *   - Spring: 다양한 스코프 지원 (singleton, prototype, request, session 등)
 *   - 여기서는: 싱글톤만 지원 (단순화)
 */
public interface BeanFactory {

    /**
     * 빈의 메타데이터를 등록
     *
     * @param definition 등록할 빈의 정의 정보
     */
    void registerBeanDefinition(BeanDefinition definition);

    /**
     * 특정 이름의 빈이 등록되어 있는지 확인
     *
     * @param beanName 확인할 빈 이름
     * @return 빈이 존재하면 true, 아니면 false
     */
    boolean containsBean(String beanName);

    /**
     * 이름으로 빈 조회 (싱글톤 보장)
     *
     * @param beanName 조회할 빈 이름
     * @return 빈 인스턴스
     * @throws org.example.minispring.exception.NoSuchBeanException 빈이 없는 경우
     */
    Object getBean(String beanName);

    /**
     * 타입으로 빈 조회 (싱글톤 보장)
     *
     * @param type 조회할 빈의 타입
     * @param <T> 반환 타입
     * @return 타입에 매칭되는 빈 인스턴스
     * @throws org.example.minispring.exception.NoSuchBeanException 해당 타입의 빈이 없는 경우
     * @throws org.example.minispring.exception.NoUniqueBeanException 같은 타입의 빈이 2개 이상인 경우
     */
    <T> T getBean(Class<T> type);
}
