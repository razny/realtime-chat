package com.chat.app.test;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@ConditionalOnProperty(prefix = "app.rabbit", name = "test-enabled", havingValue = "true", matchIfMissing = false)
public class RabbitTest {
    private static final Logger log = LoggerFactory.getLogger(RabbitTest.class);
    private final RabbitTemplate rabbitTemplate;

    public RabbitTest(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostConstruct
    public void sendTest() {
        try {
            rabbitTemplate.convertAndSend("chat.exchange", "chat.routing", "startup-test");
            log.info("Wysłano wiadomość testową do RabbitMQ");
        } catch (Exception e) {
            log.warn("Testowe połączenie z RabbitMQ nie powiodło się: {}", e.getMessage());
        }
    }
}