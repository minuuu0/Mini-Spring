package org.example.minispring.injector;

import org.example.minispring.annotation.Autowired;

import java.lang.reflect.Constructor;

public class ConstructorResolver {

    public Constructor<?> resolve(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();

        // 1. @Autowired가 붙은 생성자 찾기
        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(Autowired.class)) {
                return constructor;
            }
        }

        // 2. 생성자가 1개만 있으면 그것 선택
        if (constructors.length == 1) {
            return constructors[0];
        }

        // 3. 여러 개면 기본 생성자 선택
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() == 0) {
                return constructor;
            }
        }

        throw new IllegalStateException("No suitable constructor found for " + clazz.getName());
    }
}
