package org.example.demo;

import org.example.minispring.annotation.Controller;

@Controller
public class OrderController {

    private final UserService userService;
    private final NotificationService notificationService;

    public OrderController(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
        System.out.println(">>> OrderController 생성됨! (의존성: UserService, NotificationService)");
    }

    public void createOrder(Long userId) {
        String user = userService.getUser(userId);
        System.out.println("📦 주문 생성: " + user);
        notificationService.sendNotification(userId, "주문이 완료되었습니다!");
    }

    public void printInfo() {
        System.out.println("✓ OrderController 인스턴스: " + this.hashCode());
        System.out.println("  ├─ 주입된 UserService: " + userService.hashCode());
        System.out.println("  └─ 주입된 NotificationService: " + notificationService.hashCode());
    }
}
