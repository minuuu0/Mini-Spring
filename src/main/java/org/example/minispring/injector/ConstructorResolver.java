package org.example.minispring.injector;

import org.example.minispring.annotation.Autowired;

import java.lang.reflect.Constructor;

/**
 * 클래스에서 의존성 주입에 사용할 생성자를 선택하는 리졸버
 *
 * 선택 우선순위 (Spring과 동일):
 *   1순위: @Autowired 어노테이션이 붙은 생성자
 *   2순위: 생성자가 1개만 있으면 자동 선택
 *   3순위: 기본 생성자 (파라미터 없는 생성자)
 */
public class ConstructorResolver {

    /**
     * 클래스에서 사용할 생성자를 선택
     *
     * @param clazz 생성자를 찾을 클래스
     * @return 선택된 생성자
     * @throws IllegalStateException 적합한 생성자를 찾을 수 없는 경우
     */
    public Constructor<?> resolve(Class<?> clazz) {
        // ================================================================
        // 0단계: 클래스의 모든 생성자 가져오기
        // ================================================================
        // getDeclaredConstructors(): public, private, protected 모두 포함
        //
        // 예시 클래스:
        //   class UserService {
        //       public UserService() {}              // 생성자1
        //       @Autowired
        //       public UserService(UserRepository r) {}  // 생성자2
        //   }
        // → constructors.length = 2
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();

        // ================================================================
        // 1순위: @Autowired 어노테이션이 붙은 생성자 찾기
        // ================================================================
        // 명시적으로 @Autowired를 붙인 생성자를 최우선으로 사용
        //
        // 사용 이유:
        //   - 개발자가 명시적으로 지정한 의도를 존중
        //   - 여러 생성자가 있을 때 어느 것을 쓸지 명확히 지정 가능
        //
        // 예시:
        //   @Autowired
        //   public UserService(UserRepository repo, EmailService email) {
        //       // 이 생성자 사용
        //   }
        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(Autowired.class)) {
                return constructor;  // 즉시 반환 (우선순위 1)
            }
        }

        // ================================================================
        // 2순위: 생성자가 1개만 있으면 그것을 자동 선택
        // ================================================================
        // Spring Boot 2.x 이후의 동작:
        //   - 생성자가 하나뿐이면 @Autowired 없어도 자동으로 의존성 주입
        //   - 보일러플레이트 코드 감소
        //
        // 예시:
        //   class UserService {
        //       public UserService(UserRepository repo) {  // @Autowired 생략 가능
        //           this.repo = repo;
        //       }
        //   }
        if (constructors.length == 1) {
            return constructors[0];  // 유일한 생성자 반환
        }

        // ================================================================
        // 3순위: 여러 생성자가 있으면 기본 생성자 선택
        // ================================================================
        // 기본 생성자: 파라미터가 없는 생성자
        //
        // 사용 사례:
        //   - 의존성이 없는 단순 빈 (Util 클래스 등)
        //   - 여러 생성자가 있지만 @Autowired 명시 안 한 경우
        //
        // 예시:
        //   class ConfigProperties {
        //       public ConfigProperties() {}  // 이것 선택
        //       public ConfigProperties(String profile) {}
        //   }
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() == 0) {
                return constructor;
            }
        }

        // ================================================================
        // 적합한 생성자를 찾지 못한 경우 예외 발생
        // ================================================================
        // 발생 조건:
        //   - 여러 생성자가 있고
        //   - @Autowired도 없고
        //   - 기본 생성자도 없는 경우
        //
        // 예시:
        //   class BadService {
        //       public BadService(String a) {}
        //       public BadService(int b) {}
        //       // 어느 것을 써야 할지 알 수 없음!
        //   }
        throw new IllegalStateException("No suitable constructor found for " + clazz.getName());
    }
}
