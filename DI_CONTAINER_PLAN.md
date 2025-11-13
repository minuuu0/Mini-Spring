# Mini Spring DI Container 개발 계획

## 📋 프로젝트 개요
객체지향 원칙을 준수하며 TDD 방식으로 개발하는 경량 DI 컨테이너

## 🎯 핵심 기능

### 1. 어노테이션 정의
- `@Component` - 일반 컴포넌트 빈
- `@Service` - 비즈니스 로직 레이어
- `@Repository` - 데이터 접근 레이어
- `@Controller` - 프레젠테이션 레이어
- `@Autowired` - 생성자 주입 표시 (생략 가능, 생성자가 1개면 자동 적용)
- `@Configuration` - 설정 클래스
- `@Bean` - 수동 빈 등록
- `@ComponentScan` - 컴포넌트 스캔 대상 패키지 지정

### 2. 컨테이너 핵심 기능
- **컴포넌트 스캔**: 지정된 패키지에서 어노테이션 스캔
- **생성자 주입**: 생성자 기반 의존성 주입만 지원
- **싱글톤 보장**: 모든 빈은 싱글톤으로 관리
- **타입 기반 조회**: 클래스 타입으로 빈 조회
- **이름 기반 조회**: 빈 이름으로 빈 조회
- **순환 참조 감지**: 생성자 주입 시 순환 참조 방지
- **빈 생명주기 관리**: 초기화/소멸 콜백 지원

### 3. 추가 권장 기능
- **프로파일**: 환경별 빈 등록 (`@Profile`)
- **조건부 빈 등록**: 특정 조건에 따른 빈 등록 (`@Conditional`)
- **빈 후처리**: BeanPostProcessor 인터페이스
- **의존성 주입 검증**: 필수 의존성 누락 검증

## 🏗️ 아키텍처 설계

### 패키지 구조
```
org.example.minispring
├── annotation           # 어노테이션 정의
│   ├── Component
│   ├── Service
│   ├── Repository
│   ├── Controller
│   ├── Autowired
│   ├── Configuration
│   ├── Bean
│   └── ComponentScan
├── container            # 컨테이너 핵심
│   ├── BeanFactory      # 빈 생성/관리 인터페이스
│   ├── ApplicationContext  # 최상위 컨테이너 인터페이스
│   └── AnnotationConfigApplicationContext  # 구현체
├── bean                 # 빈 메타데이터
│   ├── BeanDefinition   # 빈 정의 정보
│   └── BeanScope        # 빈 스코프 (싱글톤만)
├── scanner              # 스캐닝
│   ├── ComponentScanner # 컴포넌트 스캔 담당
│   └── ClassPathScanner # 클래스패스 스캔
├── injector             # 의존성 주입
│   ├── DependencyInjector  # 주입 담당
│   └── ConstructorResolver  # 생성자 분석
└── exception            # 예외
    ├── BeanCreationException
    ├── NoSuchBeanException
    ├── CircularDependencyException
    └── NoUniqueBeanException
```

## 📐 객체 책임 분리

### 1. ApplicationContext
- **책임**: 전체 컨테이너의 진입점 및 라이프사이클 관리
- **의존**: BeanFactory, ComponentScanner
- **주요 메서드**:
  - `<T> T getBean(Class<T> type)`
  - `<T> T getBean(String name, Class<T> type)`
  - `void refresh()` - 컨테이너 초기화

### 2. BeanFactory
- **책임**: 빈 저장소 및 생성 관리
- **의존**: DependencyInjector
- **주요 메서드**:
  - `void registerBeanDefinition(BeanDefinition definition)`
  - `Object createBean(BeanDefinition definition)`
  - `Object getBean(String name)`
  - `boolean containsBean(String name)`

### 3. ComponentScanner
- **책임**: 클래스패스에서 어노테이션이 붙은 클래스 찾기
- **의존**: ClassPathScanner
- **주요 메서드**:
  - `Set<BeanDefinition> scan(String basePackage)`
  - `boolean isComponent(Class<?> clazz)`

### 4. DependencyInjector
- **책임**: 생성자를 통한 의존성 주입 수행
- **의존**: BeanFactory, ConstructorResolver
- **주요 메서드**:
  - `Object inject(BeanDefinition definition)`
  - `void detectCircularDependency(Class<?> beanClass)`

### 5. BeanDefinition
- **책임**: 빈의 메타데이터 보관 (불변 객체)
- **속성**:
  - `String beanName`
  - `Class<?> beanClass`
  - `BeanScope scope`
  - `Constructor<?> constructor`

## 🔄 의존 방향

```
ApplicationContext
    ↓ (의존)
BeanFactory ← ComponentScanner
    ↓              ↓
DependencyInjector ClassPathScanner
    ↓
ConstructorResolver
```

**의존 원칙**:
- 상위 레벨(ApplicationContext)이 하위 레벨(BeanFactory, Scanner)에 의존
- 각 레이어는 인터페이스를 통해 의존 (DIP 준수)
- 순환 의존 없음

## 🧪 TDD 개발 순서

### Phase 1: 기반 구조 (빨강 → 초록 → 리팩토링)
1. **어노테이션 정의**
   - 테스트: 어노테이션이 클래스에 적용 가능한지 확인
   - 구현: `@Component`, `@Service`, `@Repository`, `@Controller`

2. **BeanDefinition**
   - 테스트: 빈 메타데이터 생성 및 조회
   - 구현: 불변 빈 정의 객체

3. **BeanFactory - 단일 빈 등록/조회**
   - 테스트: 빈을 수동으로 등록하고 이름으로 조회
   - 구현: 기본 빈 저장소 (Map 사용)

### Phase 2: 스캐닝
4. **ClassPathScanner**
   - 테스트: 특정 패키지의 모든 클래스 찾기
   - 구현: 리플렉션 기반 클래스 스캔

5. **ComponentScanner**
   - 테스트: 어노테이션이 붙은 클래스만 필터링
   - 구현: 컴포넌트 어노테이션 검사

### Phase 3: 의존성 주입
6. **ConstructorResolver**
   - 테스트: 주입 가능한 생성자 찾기
   - 구현: 생성자 선택 로직 (우선순위: @Autowired > 단일 생성자 > 기본 생성자)

7. **DependencyInjector - 의존성 없는 빈**
   - 테스트: 기본 생성자로 빈 생성
   - 구현: 단순 인스턴스화

8. **DependencyInjector - 의존성 있는 빈**
   - 테스트: 다른 빈을 의존하는 빈 생성
   - 구현: 재귀적 의존성 해결

9. **순환 참조 감지**
   - 테스트: A → B → A 순환 의존 시 예외 발생
   - 구현: 생성 중인 빈 추적 (ThreadLocal Set)

### Phase 4: 싱글톤
10. **싱글톤 보장**
    - 테스트: 같은 빈 요청 시 동일 인스턴스 반환
    - 구현: 캐시 메커니즘

### Phase 5: ApplicationContext
11. **AnnotationConfigApplicationContext**
    - 테스트: 패키지 스캔 후 모든 빈 사용 가능
    - 구현: BeanFactory + ComponentScanner 조합

12. **타입 기반 조회**
    - 테스트: 클래스 타입으로 빈 조회
    - 구현: 타입 매칭 로직

13. **타입 조회 시 중복 처리**
    - 테스트: 같은 타입의 빈이 여러 개면 예외
    - 구현: NoUniqueBeanException

### Phase 6: @Configuration & @Bean
14. **@Configuration 클래스 처리**
    - 테스트: @Bean 메서드로 빈 등록
    - 구현: 메서드 호출을 통한 빈 생성

15. **@Bean 싱글톤 보장**
    - 테스트: @Bean 메서드 여러 번 호출해도 같은 인스턴스
    - 구현: CGLIB 프록시 또는 메서드 인터셉션

### Phase 7: 생명주기 (선택)
16. **초기화/소멸 콜백**
    - 테스트: 빈 생성 후 초기화 메서드 호출
    - 구현: `@PostConstruct`, `@PreDestroy` 지원

## ✅ 테스트 전략

### 단위 테스트
- 각 클래스의 책임 범위 내 기능 테스트
- Mock 객체 활용 (의존성 격리)

### 통합 테스트
- ApplicationContext를 통한 전체 플로우 테스트
- 실제 클래스로 컴포넌트 스캔 및 주입 검증

### 테스트 시나리오 예시
```java
// 1. 단순 빈 등록/조회
@Test
void shouldRegisterAndRetrieveBean() {
    // Given: BeanFactory에 빈 등록
    // When: 빈 조회
    // Then: 올바른 인스턴스 반환
}

// 2. 의존성 주입
@Test
void shouldInjectDependencies() {
    // Given: 의존 관계가 있는 두 빈
    // When: 상위 빈 조회
    // Then: 하위 빈이 주입된 상태
}

// 3. 순환 참조
@Test
void shouldDetectCircularDependency() {
    // Given: A → B → A 의존
    // When: 빈 생성
    // Then: CircularDependencyException
}

// 4. 싱글톤
@Test
void shouldReturnSameInstance() {
    // Given: 빈 등록
    // When: 같은 빈 두 번 조회
    // Then: 동일 인스턴스 (==)
}
```

## 🎓 객체지향 원칙 적용

### SOLID 원칙
- **SRP**: 각 클래스는 단일 책임 (Scanner는 스캔만, Injector는 주입만)
- **OCP**: 인터페이스 기반 확장 (BeanFactory 인터페이스)
- **LSP**: 하위 타입 치환 가능 (ApplicationContext → BeanFactory)
- **ISP**: 클라이언트별 인터페이스 분리
- **DIP**: 추상화에 의존 (구현체가 아닌 인터페이스)

### 디자인 패턴
- **Factory Pattern**: BeanFactory (빈 생성 캡슐화)
- **Singleton Pattern**: 빈 인스턴스 관리
- **Strategy Pattern**: 생성자 선택 전략
- **Template Method**: 빈 생성 플로우

## 🚀 개발 우선순위

### 필수 (MVP)
1. 어노테이션 정의
2. 컴포넌트 스캔
3. 생성자 주입
4. 싱글톤 보장
5. 타입/이름 기반 빈 조회

### 우선 (Core)
6. 순환 참조 감지
7. @Configuration & @Bean
8. 중복 빈 예외 처리

### 선택 (Advanced)
9. 빈 생명주기 콜백
10. 프로파일
11. 조건부 빈 등록

## 📝 구현 시 주의사항

1. **리플렉션 사용 시 예외 처리** - ReflectiveOperationException 적절히 처리
2. **타입 안전성** - 제네릭 활용하여 타입 캐스팅 최소화
3. **스레드 안전성** - 싱글톤 빈 생성 시 동시성 고려 (synchronized)
4. **명확한 예외 메시지** - 무엇이 잘못되었는지 명확히 전달
5. **불변 객체** - BeanDefinition 등은 불변으로 설계
6. **패키지 프라이빗** - 외부 노출 불필요한 클래스는 접근 제한

## 🔍 참고 사항

- Java Reflection API 활용
- ClassLoader를 통한 동적 클래스 로딩
- 생성자 파라미터 타입으로 의존성 해결
- 빈 이름 기본값: 클래스명의 camelCase (UserService → userService)
