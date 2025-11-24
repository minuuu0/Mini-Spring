package org.example.minispring.lifecycle;

import org.example.minispring.annotation.Component;
import org.example.minispring.annotation.PostConstruct;
import org.example.minispring.annotation.PreDestroy;

@Component
public class LifecycleTestBean {

    private boolean initialized = false;
    private boolean destroyed = false;

    public LifecycleTestBean() {
        System.out.println("Constructor called");
    }

    @PostConstruct
    public void init() {
        System.out.println("@PostConstruct init() called");
        initialized = true;
    }

    @PreDestroy
    public void cleanup() {
        System.out.println("@PreDestroy cleanup() called");
        destroyed = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isDestroyed() {
        return destroyed;
    }
}
