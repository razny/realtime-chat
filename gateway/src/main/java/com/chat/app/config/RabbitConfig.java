// java
package com.chat.app.config;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Queue;

@Configuration
public class RabbitConfig {
    public static final String CHAT_QUEUE = "chatMessages";
    public static final String EDIT_QUEUE = "chat.edit.queue";
    public static final String EDITED_QUEUE = "chat.edited.queue";
    public static final String DELETE_QUEUE = "chat.delete.queue";
    public static final String SAVED_QUEUE = "chat.saved.queue";
    public static final String DELETED_QUEUE = "chat.deleted.queue";

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    @Bean
    public Queue chatQueue() {
        return new Queue(CHAT_QUEUE, true);
    }

    @Bean
    public Queue editQueue() {
        return new Queue(EDIT_QUEUE, true);
    }

    @Bean
    public Queue editedQueue() {
        return new Queue(EDITED_QUEUE, true);
    }

    @Bean
    public Queue deleteQueue() {
        return new Queue(DELETE_QUEUE, true);
    }

    @Bean
    public Queue savedQueue() {
        return new Queue(SAVED_QUEUE, true);
    }

    @Bean
    public Queue deletedQueue() {
        return new Queue(DELETED_QUEUE, true);
    }
}