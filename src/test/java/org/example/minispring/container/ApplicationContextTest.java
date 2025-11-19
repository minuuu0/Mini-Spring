package org.example.minispring.container;

import org.example.minispring.container.testdata.DependentService;
import org.example.minispring.container.testdata.MessageService;
import org.example.minispring.container.testdata.TestComponent;
import org.example.minispring.container.testdata.TestService;
import org.example.minispring.exception.NoUniqueBeanException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationContextTest {

    @Test
    void shouldScanAndRegisterComponentsFromPackage() {
        // Given & When
        ApplicationContext context = new AnnotationConfigApplicationContext(
            "org.example.minispring.container.testdata"
        );

        // Then
        Object component = context.getBean("testComponent");
        assertNotNull(component);
        assertInstanceOf(TestComponent.class, component);

        Object service = context.getBean("testService");
        assertNotNull(service);
        assertInstanceOf(TestService.class, service);
    }

    @Test
    void shouldInjectDependenciesInScannedBeans() {
        // Given & When
        ApplicationContext context = new AnnotationConfigApplicationContext(
            "org.example.minispring.container.testdata"
        );

        // Then
        DependentService dependentService = (DependentService) context.getBean("dependentService");
        assertNotNull(dependentService);
        assertNotNull(dependentService.getTestService());
        assertInstanceOf(TestService.class, dependentService.getTestService());
    }

    @Test
    void shouldReturnSameInstanceForSingleton() {
        // Given
        ApplicationContext context = new AnnotationConfigApplicationContext(
            "org.example.minispring.container.testdata"
        );

        // When
        Object bean1 = context.getBean("testService");
        Object bean2 = context.getBean("testService");

        // Then
        assertSame(bean1, bean2);
    }

    @Test
    void shouldGetBeanByType() {
        // Given
        ApplicationContext context = new AnnotationConfigApplicationContext(
            "org.example.minispring.container.testdata"
        );

        // When
        TestService service = context.getBean(TestService.class);

        // Then
        assertNotNull(service);
        assertInstanceOf(TestService.class, service);
    }

    @Test
    void shouldGetBeanByTypeWithDependencies() {
        // Given
        ApplicationContext context = new AnnotationConfigApplicationContext(
            "org.example.minispring.container.testdata"
        );

        // When
        DependentService service = context.getBean(DependentService.class);

        // Then
        assertNotNull(service);
        assertNotNull(service.getTestService());
    }

    @Test
    void shouldThrowNoUniqueBeanExceptionWhenMultipleBeansOfSameType() {
        // Given
        ApplicationContext context = new AnnotationConfigApplicationContext(
            "org.example.minispring.container.testdata"
        );

        // When & Then
        assertThrows(NoUniqueBeanException.class, () -> {
            context.getBean(MessageService.class);
        });
    }
}
