package org.example.demo;

import org.example.minispring.annotation.Repository;

@Repository
public class UserRepository {

    public String findUserById(Long id) {
        return "User-" + id;
    }

    public void printInfo() {
        System.out.println("✓ UserRepository 인스턴스: " + this.hashCode());
    }
}
