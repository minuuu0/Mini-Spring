package org.example.minispring.lifecycle;

import org.example.minispring.annotation.PostConstruct;
import org.example.minispring.annotation.PreDestroy;

import java.lang.reflect.Method;

/**
 * 빈의 생명주기 콜백 메서드를 관리
 *
 * 역할:
 *   - @PostConstruct 메서드 호출 (빈 생성 후)
 *   - @PreDestroy 메서드 호출 (컨테이너 종료 시)
 *
 * 동작:
 *   1. 빈 생성 완료 후 → @PostConstruct 메서드 찾아서 호출
 *   2. 컨테이너 종료 시 → @PreDestroy 메서드 찾아서 호출
 */
public class BeanLifecycleManager {

    /**
     * 빈 생성 후 @PostConstruct 메서드 호출
     *
     * @param bean 초기화할 빈 인스턴스
     */
    public void invokePostConstruct(Object bean) {
        // ================================================================
        // 1단계: 빈 클래스의 모든 메서드 스캔
        // ================================================================
        Class<?> beanClass = bean.getClass();
        Method[] methods = beanClass.getDeclaredMethods();

        // ================================================================
        // 2단계: @PostConstruct 어노테이션이 있는 메서드 찾기
        // ================================================================
        for (Method method : methods) {
            if (method.isAnnotationPresent(PostConstruct.class)) {

                // ============================================================
                // 3단계: 메서드 검증
                // ============================================================
                // @PostConstruct 메서드 요구사항:
                //   - 파라미터가 없어야 함
                //   - void 반환 타입 (권장)
                if (method.getParameterCount() != 0) {
                    throw new IllegalStateException(
                        "@PostConstruct method must have no parameters: " +
                        method.getName() + " in " + beanClass.getName()
                    );
                }

                // ============================================================
                // 4단계: 메서드 호출
                // ============================================================
                try {
                    method.setAccessible(true);  // private 메서드도 호출 가능
                    method.invoke(bean);
                    System.out.println("@PostConstruct called: " +
                        beanClass.getSimpleName() + "." + method.getName() + "()");

                } catch (Exception e) {
                    throw new RuntimeException(
                        "Failed to invoke @PostConstruct method: " +
                        method.getName() + " in " + beanClass.getName(),
                        e
                    );
                }

                // @PostConstruct는 하나만 허용 (Spring 규칙)
                // 여러 개 있어도 첫 번째만 호출하고 종료
                break;
            }
        }
    }

    /**
     * 컨테이너 종료 시 @PreDestroy 메서드 호출
     *
     * @param bean 정리할 빈 인스턴스
     */
    public void invokePreDestroy(Object bean) {
        // ================================================================
        // 1단계: 빈 클래스의 모든 메서드 스캔
        // ================================================================
        Class<?> beanClass = bean.getClass();
        Method[] methods = beanClass.getDeclaredMethods();

        // ================================================================
        // 2단계: @PreDestroy 어노테이션이 있는 메서드 찾기
        // ================================================================
        for (Method method : methods) {
            if (method.isAnnotationPresent(PreDestroy.class)) {

                // ============================================================
                // 3단계: 메서드 검증
                // ============================================================
                // @PreDestroy 메서드 요구사항:
                //   - 파라미터가 없어야 함
                //   - void 반환 타입 (권장)
                if (method.getParameterCount() != 0) {
                    throw new IllegalStateException(
                        "@PreDestroy method must have no parameters: " +
                        method.getName() + " in " + beanClass.getName()
                    );
                }

                // ============================================================
                // 4단계: 메서드 호출
                // ============================================================
                try {
                    method.setAccessible(true);  // private 메서드도 호출 가능
                    method.invoke(bean);
                    System.out.println("@PreDestroy called: " +
                        beanClass.getSimpleName() + "." + method.getName() + "()");

                } catch (Exception e) {
                    // PreDestroy 실패 시 로그만 남기고 계속 진행
                    // (다른 빈들의 정리도 수행해야 함)
                    System.err.println(
                        "Failed to invoke @PreDestroy method: " +
                        method.getName() + " in " + beanClass.getName()
                    );
                    e.printStackTrace();
                }

                // @PreDestroy는 하나만 허용 (Spring 규칙)
                // 여러 개 있어도 첫 번째만 호출하고 종료
                break;
            }
        }
    }
}
