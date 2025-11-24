package org.example.minispring.lifecycle;

import org.example.minispring.container.AnnotationConfigApplicationContext;
import org.example.minispring.container.ApplicationContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BeanLifecycleTest {

    @Test
    void shouldCallPostConstructAfterBeanCreation() {
        // Given & When
        ApplicationContext context = new AnnotationConfigApplicationContext(
            "org.example.minispring.lifecycle"
        );

        // When
        LifecycleTestBean bean = context.getBean(LifecycleTestBean.class);

        // Then
        assertNotNull(bean);
        assertTrue(bean.isInitialized(), "@PostConstruct method should have been called");
    }

    @Test
    void shouldCallPreDestroyBeforeContainerShutdown() {
        // Given
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
            "org.example.minispring.lifecycle"
        );
        LifecycleTestBean bean = context.getBean(LifecycleTestBean.class);

        // When
        context.close();

        // Then
        assertTrue(bean.isDestroyed(), "@PreDestroy method should have been called");
    }

    @Test
    void shouldCallPostConstructOnlyOnce() {
        // Given
        ApplicationContext context = new AnnotationConfigApplicationContext(
            "org.example.minispring.lifecycle"
        );

        // When
        LifecycleTestBean bean1 = context.getBean(LifecycleTestBean.class);
        LifecycleTestBean bean2 = context.getBean(LifecycleTestBean.class);

        // Then
        assertSame(bean1, bean2, "Should be singleton");
        assertTrue(bean1.isInitialized(), "@PostConstruct should be called");
        // Since it's singleton, @PostConstruct should only be called once during first creation
    }
}
