package org.example.minispring.container.testdata;

import org.example.minispring.annotation.Service;

@Service
public class EmailMessageService implements MessageService {
    @Override
    public String getMessage() {
        return "Email message";
    }
}
