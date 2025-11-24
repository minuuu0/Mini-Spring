package org.example.minispring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 빈 생성 후 초기화 메서드를 나타내는 어노테이션
 *
 * 역할:
 *   - 빈의 생성자 호출 및 의존성 주입 완료 후 호출되는 메서드 표시
 *   - 초기화 로직 실행 (캐시 로드, 커넥션 풀 생성 등)
 *
 * 사용 예시:
 *   @Component
 *   public class CacheManager {
 *       private Map<String, Object> cache;
 *
 *       @PostConstruct
 *       public void init() {
 *           cache = new HashMap<>();
 *           System.out.println("Cache initialized");
 *       }
 *   }
 *
 * 특징:
 *   - 메서드 레벨 어노테이션
 *   - 파라미터가 없어야 함
 *   - void 반환 타입
 *   - 빈당 한 번만 호출됨
 *   - 생성자 → 의존성 주입 → @PostConstruct 순서로 실행
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PostConstruct {
}
