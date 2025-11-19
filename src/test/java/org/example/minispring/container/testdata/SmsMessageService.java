package org.example.minispring.container.testdata;

import org.example.minispring.annotation.Service;

@Service
public class SmsMessageService implements MessageService {
    @Override
    public String getMessage() {
        return "SMS message";
    }
}
