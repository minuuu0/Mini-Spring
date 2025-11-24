package org.example.minispring.scanner;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * 클래스패스에서 특정 패키지의 모든 클래스를 찾는 스캐너
 *
 * 역할: 파일 시스템을 재귀적으로 탐색하여 .class 파일을 찾고
 *       Class 객체로 로드
 */
public class ClassPathScanner {

    /**
     * 지정된 패키지에서 모든 클래스를 스캔
     *
     * @param basePackage 스캔할 패키지 (예: "org.example.demo")
     * @return 찾은 모든 Class 객체의 Set
     */
    public Set<Class<?>> scan(String basePackage) {
        Set<Class<?>> classes = new HashSet<>();

        try {
            // ============================================================
            // 1단계: 패키지 이름을 파일 경로로 변환
            // ============================================================
            // "org.example.demo" → "org/example/demo"
            // 이유: 클래스 로더는 디렉토리 구조로 리소스를 찾음
            String path = basePackage.replace('.', '/');

            // ============================================================
            // 2단계: 현재 스레드의 클래스 로더 획득
            // ============================================================
            // ClassLoader: .class 파일을 메모리로 로드하는 JVM 컴포넌트
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            // ============================================================
            // 3단계: 패키지 경로에 해당하는 실제 파일 시스템 경로 찾기
            // ============================================================
            // 예: "org/example/demo" → "C:/project/build/classes/org/example/demo"
            URL resource = classLoader.getResource(path);

            // 패키지가 존재하지 않으면 빈 Set 반환
            if (resource == null) {
                return classes;
            }

            // ============================================================
            // 4단계: URL을 File 객체로 변환
            // ============================================================
            // URL: file:/C:/project/build/classes/org/example/demo
            // File: C:/project/build/classes/org/example/demo
            File directory = new File(resource.getFile());

            if (!directory.exists()) {
                return classes;
            }

            // ============================================================
            // 5단계: 재귀적으로 디렉토리 탐색 시작
            // ============================================================
            findClasses(directory, basePackage, classes);

        } catch (Exception e) {
            // 예외 발생 시 빈 Set 반환 (조용히 실패)
        }

        return classes;
    }

    /**
     * 디렉토리를 재귀적으로 탐색하여 .class 파일 찾기
     *
     * @param directory 현재 탐색 중인 디렉토리
     * @param packageName 현재 패키지 이름
     * @param classes 찾은 클래스들을 저장할 Set (출력 파라미터)
     */
    private void findClasses(File directory, String packageName, Set<Class<?>> classes) {
        // ============================================================
        // 1단계: 현재 디렉토리의 모든 파일/폴더 목록 가져오기
        // ============================================================
        File[] files = directory.listFiles();

        if (files == null) {
            return;  // 디렉토리가 비어있거나 읽을 수 없음
        }

        // ============================================================
        // 2단계: 각 파일/폴더를 순회하며 처리
        // ============================================================
        for (File file : files) {

            // ------------------------------------------------------------
            // Case 1: 하위 디렉토리인 경우 → 재귀 호출
            // ------------------------------------------------------------
            if (file.isDirectory()) {
                // 예: directory = "org/example/demo/service"
                //     packageName = "org.example.demo"
                //     file.getName() = "service"
                // → 재귀 호출: findClasses(..., "org.example.demo.service", ...)
                findClasses(file, packageName + "." + file.getName(), classes);
            }

            // ------------------------------------------------------------
            // Case 2: .class 파일인 경우 → Class 객체로 로드
            // ------------------------------------------------------------
            else if (file.getName().endsWith(".class")) {

                // ========================================================
                // 2-1. 파일명에서 .class 확장자 제거하고 완전한 클래스 이름 생성
                // ========================================================
                // 예: file.getName() = "UserService.class"
                //     packageName = "org.example.demo"
                //
                // 계산 과정:
                //   1) file.getName().length() = 18
                //   2) 18 - 6 = 12 (뒤에서 ".class" 제거)
                //   3) substring(0, 12) = "UserService"
                //   4) packageName + '.' + "UserService"
                //      = "org.example.demo.UserService"
                String className = packageName + '.' +
                                   file.getName().substring(0, file.getName().length() - 6);

                try {
                    // ====================================================
                    // 2-2. Class.forName()으로 클래스를 JVM에 로드
                    // ====================================================
                    // 동작:
                    //   1) 클래스 로더가 "org.example.demo.UserService.class" 파일 찾기
                    //   2) 바이트코드를 읽어서 메모리에 로드
                    //   3) Class 객체 생성 (메타데이터 포함)
                    //   4) 정적 초기화 블록 실행
                    classes.add(Class.forName(className));

                } catch (ClassNotFoundException e) {
                    // 클래스를 찾을 수 없으면 무시하고 계속 진행
                }
            }
        }
    }
}
