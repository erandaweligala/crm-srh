package com.adl.et.telco.crm.securerequesthandler.application.config;

import com.rabbitmq.client.ShutdownSignalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.FatalExceptionStrategy;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for RabbitMQ setup.
 * Configures queues, exchanges, bindings, and connection factory with proper logging.
 */
@Configuration
public class RabbitMqConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMqConfiguration.class);
    private static final String LOG_PREFIX = "SRH|RabbitMqConfig|";

    @Value("${rabbit.exchange-name}")
    private String exchangeName;

    @Value("${rabbit.queue-name}")
    private String queueName;

    @Value("${rabbit.routing-key}")
    private String routingKey;

    @Value("${rabbit.user-name}")
    private String userName;

    @Value("${rabbit.user-password}")
    private String password;

    @Value("${rabbit.host-name}")
    private String hostName;

    @Value("${rabbit.port}")
    private Integer port;

    /**
     * Creates a durable queue for message processing.
     * @return Configured Queue instance
     */
    @Bean
    public Queue actionLogQueue() {
        logger.debug("{}actionLogQueue|Start|QueueName: {}", LOG_PREFIX, queueName);
        Queue queue = new Queue(queueName, true);
        logger.info("{}actionLogQueue|End|Queue created: {}", LOG_PREFIX, queue);
        return queue;
    }

    /**
     * Creates a direct exchange for message routing.
     * @return Configured DirectExchange instance
     */
    @Bean
    public DirectExchange actionLogExchange() {
        logger.debug("{}actionLogExchange|Start|ExchangeName: {}", LOG_PREFIX, exchangeName);
        DirectExchange exchange = new DirectExchange(exchangeName);
        logger.info("{}actionLogExchange|End|Exchange created: {}", LOG_PREFIX, exchange);
        return exchange;
    }

    /**
     * Binds the queue to the exchange with the specified routing key.
     * @param queue The queue to bind
     * @param exchange The exchange to bind to
     * @return Configured Binding instance
     */
    @Bean
    public Binding actionLogBinding(Queue actionLogQueue, DirectExchange actionLogExchange) {
        logger.debug("{}actionLogBinding|Start|Queue: {}|Exchange: {}|RoutingKey: {}", 
            LOG_PREFIX, queueName, exchangeName, routingKey);
        Binding binding = BindingBuilder
                .bind(actionLogQueue)
                .to(actionLogExchange)
                .with(routingKey);
        logger.info("{}actionLogBinding|End|Binding created: {}", LOG_PREFIX, binding);
        return binding;
    }

    /**
     * Creates a JSON message converter for message serialization.
     * @return Configured MessageConverter instance
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        logger.debug("{}jsonMessageConverter|Start", LOG_PREFIX);
        MessageConverter converter = new Jackson2JsonMessageConverter();
        logger.info("{}jsonMessageConverter|End|Converter created", LOG_PREFIX);
        return converter;
    }

    /**
     * Creates and configures the RabbitMQ connection factory.
     * @return Configured ConnectionFactory instance
     */
    @Bean
    public ConnectionFactory connectionFactory() {
        logger.debug("{}connectionFactory|Start|Host: {}|Port: {}", LOG_PREFIX, hostName, port);
        
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(hostName);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(userName);
        connectionFactory.setPassword(password);
        
        connectionFactory.addConnectionListener(new ConnectionListener() {
            @Override
            public void onCreate(Connection connection) {
                logger.info("{}connectionFactory|ConnectionCreated|Connection: {}", 
                    LOG_PREFIX, connection);
            }

            @Override
            public void onClose(Connection connection) {
                logger.info("{}connectionFactory|ConnectionClosed|Connection: {}", 
                    LOG_PREFIX, connection);
            }

            @Override
            public void onShutDown(ShutdownSignalException signal) {
                logger.warn("{}connectionFactory|ConnectionShutdown|Signal: {}", 
                    LOG_PREFIX, signal.getMessage());
            }
        });

        logger.info("{}connectionFactory|End|Factory created", LOG_PREFIX);
        return connectionFactory;
    }

    /**
     * Creates and configures the AMQP template for message operations.
     * @param connectionFactory The connection factory to use
     * @return Configured AmqpTemplate instance
     */
    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        logger.debug("{}amqpTemplate|Start", LOG_PREFIX);
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        logger.info("{}amqpTemplate|End|Template created", LOG_PREFIX);
        return rabbitTemplate;
    }

    /**
     * Creates a custom exception strategy for handling fatal errors.
     * @return Configured FatalExceptionStrategy instance
     */
    @Bean
    public FatalExceptionStrategy customExceptionStrategy() {
        logger.debug("{}customExceptionStrategy|Start", LOG_PREFIX);
        FatalExceptionStrategy strategy = new CustomFatalExceptionStrategy();
        logger.info("{}customExceptionStrategy|End|Strategy created", LOG_PREFIX);
        return strategy;
    }
}
