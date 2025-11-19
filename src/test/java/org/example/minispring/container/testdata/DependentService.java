package org.example.minispring.container.testdata;

import org.example.minispring.annotation.Service;

@Service
public class DependentService {
    private final TestService testService;

    public DependentService(TestService testService) {
        this.testService = testService;
    }

    public TestService getTestService() {
        return testService;
    }
}
