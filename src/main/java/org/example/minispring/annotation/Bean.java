package org.example.minispring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 메서드가 빈을 생성함을 나타내는 어노테이션
 *
 * 역할:
 *   - @Configuration 클래스 내의 메서드에 사용
 *   - 메서드의 반환값을 빈으로 등록
 *   - 수동 빈 등록 방식 지원
 *
 * 사용 예시:
 *   @Configuration
 *   public class DatabaseConfig {
 *       @Bean
 *       public DataSource dataSource() {
 *           HikariDataSource ds = new HikariDataSource();
 *           ds.setUrl("jdbc:mysql://localhost:3306/db");
 *           return ds;
 *       }
 *
 *       @Bean
 *       public JdbcTemplate jdbcTemplate(DataSource dataSource) {
 *           return new JdbcTemplate(dataSource);
 *       }
 *   }
 *
 * 특징:
 *   - 메서드 이름이 빈 이름이 됨 (예: dataSource() → "dataSource")
 *   - 메서드 파라미터는 자동으로 의존성 주입됨
 *   - 싱글톤으로 관리됨 (같은 메서드를 여러 번 호출해도 동일 인스턴스)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Bean {
}
