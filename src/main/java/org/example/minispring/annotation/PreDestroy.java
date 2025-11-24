package org.example.minispring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 빈 소멸 전 정리 메서드를 나타내는 어노테이션
 *
 * 역할:
 *   - 컨테이너 종료 시 빈이 소멸되기 전에 호출되는 메서드 표시
 *   - 리소스 정리 로직 실행 (커넥션 닫기, 파일 핸들 해제 등)
 *
 * 사용 예시:
 *   @Component
 *   public class DatabaseConnectionPool {
 *       private HikariDataSource dataSource;
 *
 *       @PostConstruct
 *       public void init() {
 *           dataSource = new HikariDataSource();
 *       }
 *
 *       @PreDestroy
 *       public void cleanup() {
 *           if (dataSource != null) {
 *               dataSource.close();
 *               System.out.println("Connection pool closed");
 *           }
 *       }
 *   }
 *
 * 특징:
 *   - 메서드 레벨 어노테이션
 *   - 파라미터가 없어야 함
 *   - void 반환 타입
 *   - 빈당 한 번만 호출됨
 *   - 컨테이너 종료 시 자동 호출
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PreDestroy {
}
