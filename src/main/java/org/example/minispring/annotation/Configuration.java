package org.example.minispring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 설정 클래스를 나타내는 어노테이션
 * <p>
 * 역할:
 * - 빈 설정을 담당하는 클래스 표시
 * - @Bean 메서드를 포함하여 수동으로 빈 등록
 * - 자바 기반 설정 방식 지원
 * <p>
 * 사용 예시:
 *
 * @Configuration public class AppConfig {
 * @Bean public DataSource dataSource() {
 * return new HikariDataSource();
 * }
 * }
 * <p>
 * 특징:
 * - @Component를 포함하므로 자동으로 빈으로 등록됨
 * - @Bean 메서드들을 스캔하여 반환값을 빈으로 등록
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Configuration {
}
