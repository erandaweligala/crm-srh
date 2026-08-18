package com.adl.et.telco.crm.securerequesthandler.application.client;

import com.adl.et.telco.crm.securerequesthandler.domain.dto.ActionLogDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for publishing messages to RabbitMQ queues.
 * Handles the publishing of action logs with proper logging and error handling.
 */
@Service
@Slf4j
public class QueuePublisher {
    private static final String LOG_PREFIX = "SRH|QueuePublisher|";
    private static final String INITIALIZED = "Initialized";
    private static final String START = "Start";
    private static final String SUCCESS = "Success";
    private static final String ERROR = "Error";

    private final AmqpTemplate amqpTemplate;
    private final String routingKey;
    private final String exchangeName;

    public QueuePublisher(
            AmqpTemplate amqpTemplate,
            Queue queue,
            @Value("${rabbit.routing-key}") String routingKey,
            @Value("${rabbit.exchange-name}") String exchangeName) {
        this.amqpTemplate = amqpTemplate;
        this.routingKey = routingKey;
        this.exchangeName = exchangeName;
        log.info("{}|{}|Exchange: {}|RoutingKey: {}|Queue: {}", 
            LOG_PREFIX, INITIALIZED, exchangeName, routingKey, queue.getName());
    }

    /**
     * Sends an action log message to the configured RabbitMQ exchange.
     * @param actionLogDTO The action log to be published
     * @throws RuntimeException if publishing fails
     */
    public void send(ActionLogDTO actionLogDTO) throws Exception {
        log.debug("{}|{}|ActionLog: {}", LOG_PREFIX, START, actionLogDTO);
        try {
            amqpTemplate.convertAndSend(exchangeName, routingKey, actionLogDTO);
            log.info("{}|{}|ActionLogId: {}|Exchange: {}|RoutingKey: {}", 
                LOG_PREFIX, SUCCESS, actionLogDTO.getId(), exchangeName, routingKey);
        } catch (Exception e) {
            log.error("{}|{}|ActionLogId: {}|Error: {}", 
                LOG_PREFIX, ERROR, actionLogDTO.getId(), e.getMessage(), e);
            throw e;
        }
    }
}
