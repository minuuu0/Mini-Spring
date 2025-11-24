package org.example.demo;

import org.example.minispring.annotation.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    // 생성자 주입 (생성자가 1개뿐이므로 @Autowired 생략 가능)
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        System.out.println(">>> UserService 생성됨! (의존성: UserRepository)");
    }

    public String getUser(Long id) {
        return userRepository.findUserById(id);
    }

    public void printInfo() {
        System.out.println("✓ UserService 인스턴스: " + this.hashCode());
        System.out.println("  └─ 주입된 UserRepository: " + userRepository.hashCode());
    }
}
