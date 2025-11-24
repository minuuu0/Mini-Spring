package org.example.minispring.processor;

import org.example.minispring.annotation.Bean;
import org.example.minispring.annotation.Configuration;
import org.example.minispring.bean.BeanDefinition;
import org.example.minispring.container.BeanFactory;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * @Configuration 클래스를 처리하여 @Bean 메서드를 빈으로 등록
 *
 * 역할:
 *   - @Configuration 클래스 감지
 *   - @Bean 메서드 스캔
 *   - @Bean 메서드 호출하여 빈 생성 및 등록
 *
 * 동작 흐름:
 *   1. @Configuration 클래스 찾기
 *   2. 해당 클래스의 @Bean 메서드 스캔
 *   3. @Bean 메서드 호출 → 반환값을 빈으로 등록
 *   4. 메서드 파라미터는 의존성 주입
 */
public class ConfigurationClassProcessor {

    private final BeanFactory beanFactory;

    public ConfigurationClassProcessor(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * @Configuration 클래스를 처리하여 @Bean 메서드를 빈으로 등록
     *
     * @param beanDefinitions 스캔된 모든 빈 정의 (여기서 @Configuration 클래스 찾기)
     * @return 추가로 등록할 @Bean 메서드의 BeanDefinition
     */
    public Set<BeanDefinition> process(Set<BeanDefinition> beanDefinitions) {
        Set<BeanDefinition> beanMethodDefinitions = new HashSet<>();

        // ================================================================
        // 1단계: @Configuration 클래스 찾기
        // ================================================================
        for (BeanDefinition definition : beanDefinitions) {
            Class<?> beanClass = definition.getBeanClass();

            // @Configuration 어노테이션이 있는지 확인
            if (beanClass.isAnnotationPresent(Configuration.class)) {

                // ============================================================
                // 2단계: @Bean 메서드 찾기 및 BeanDefinition 생성
                // ============================================================
                beanMethodDefinitions.addAll(processBeanMethods(definition));
            }
        }

        return beanMethodDefinitions;
    }

    /**
     * @Configuration 클래스의 @Bean 메서드를 스캔하여 BeanDefinition 생성
     *
     * @param configDefinition @Configuration 클래스의 BeanDefinition
     * @return @Bean 메서드들의 BeanDefinition Set
     */
    private Set<BeanDefinition> processBeanMethods(BeanDefinition configDefinition) {
        Set<BeanDefinition> beanDefinitions = new HashSet<>();
        Class<?> configClass = configDefinition.getBeanClass();

        // ================================================================
        // 1단계: 클래스의 모든 메서드 스캔
        // ================================================================
        Method[] methods = configClass.getDeclaredMethods();

        for (Method method : methods) {

            // ============================================================
            // 2단계: @Bean 어노테이션이 있는 메서드만 처리
            // ============================================================
            if (method.isAnnotationPresent(Bean.class)) {

                // ========================================================
                // 3단계: 메서드 이름을 빈 이름으로 사용
                // ========================================================
                // 예: public DataSource dataSource() → 빈 이름 "dataSource"
                String beanName = method.getName();

                // ========================================================
                // 4단계: 메서드 반환 타입을 빈 클래스로 사용
                // ========================================================
                Class<?> beanClass = method.getReturnType();

                // ========================================================
                // 5단계: BeanMethodDefinition 생성
                // ========================================================
                // 일반 BeanDefinition과 다르게, @Bean 메서드 정보도 포함
                BeanMethodDefinition beanDefinition = new BeanMethodDefinition(
                    beanName,
                    beanClass,
                    configDefinition.getBeanName(),  // Configuration 클래스의 빈 이름
                    method
                );

                beanDefinitions.add(beanDefinition);
            }
        }

        return beanDefinitions;
    }

    /**
     * @Bean 메서드의 BeanDefinition
     *
     * 추가 정보:
     *   - configBeanName: @Configuration 클래스의 빈 이름
     *   - method: @Bean 메서드 객체
     */
    public static class BeanMethodDefinition extends BeanDefinition {
        private final String configBeanName;
        private final Method method;

        public BeanMethodDefinition(String beanName, Class<?> beanClass,
                                    String configBeanName, Method method) {
            super(beanName, beanClass);
            this.configBeanName = configBeanName;
            this.method = method;
        }

        public String getConfigBeanName() {
            return configBeanName;
        }

        public Method getMethod() {
            return method;
        }
    }
}
