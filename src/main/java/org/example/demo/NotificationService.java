package org.example.demo;

import org.example.minispring.annotation.Service;

@Service
public class NotificationService {

    private final UserService userService;

    public NotificationService(UserService userService) {
        this.userService = userService;
        System.out.println(">>> NotificationService 생성됨! (의존성: UserService)");
    }

    public void sendNotification(Long userId, String message) {
        String user = userService.getUser(userId);
        System.out.println("[알림] " + user + "에게 메시지: " + message);
    }

    public void printInfo() {
        System.out.println("✓ NotificationService 인스턴스: " + this.hashCode());
        System.out.println("  └─ 주입된 UserService: " + userService.hashCode());
    }
}
