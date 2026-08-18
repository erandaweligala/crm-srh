package com.adl.et.telco.crm.securerequesthandler.application.config;

import com.adl.et.telco.crm.securerequesthandler.application.handler.RequestDetailInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.Executors;

/**
 * Web configuration class that sets up interceptors and async task executor.
 * Configures request detail interceptor and thread pool for async operations.
 */
@Component
@Slf4j
public class WebConfiguration implements WebMvcConfigurer {
    private static final String LOG_PREFIX = "SRH|WebConfiguration|";
    private static final String START = "START";
    private static final String END = "END";
    private static final String ERROR = "ERROR";
    private static final String SUCCESS = "SUCCESS";

    private final RequestDetailInterceptor requestDetailInterceptor;
    private final int threadCount;

    public WebConfiguration(
            RequestDetailInterceptor requestDetailInterceptor,
            @Value("${thread.count.dbapiexecutor}") int threadCount) {
        this.requestDetailInterceptor = requestDetailInterceptor;
        this.threadCount = threadCount;
        log.info("{}|{}|Initialized with thread count: {}", LOG_PREFIX, SUCCESS, threadCount);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.debug("{}|{}|Adding interceptors", LOG_PREFIX, START);
        try {
            registry.addInterceptor(requestDetailInterceptor);
            log.info("{}|{}|Successfully added RequestDetailInterceptor", LOG_PREFIX, SUCCESS);
        } catch (Exception e) {
            log.error("{}|{}|Failed to add interceptors: {}", LOG_PREFIX, ERROR, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Creates a task executor with request context propagation.
     * The executor maintains request context across async operations.
     *
     * @return Configured TaskExecutor instance with request context propagation
     */
    @Bean(name = "asyncExecutor")
    public TaskExecutor transactionalExecutor() {
        log.debug("{}|{}|Creating task executor with thread count: {}", LOG_PREFIX, START, threadCount);
        
        try {
            ConcurrentTaskExecutor executor = new ConcurrentTaskExecutor(
                Executors.newFixedThreadPool(threadCount));
            
            executor.setTaskDecorator(runnable -> {
                RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
                log.debug("{}|Captured request attributes for async operation", LOG_PREFIX);

                return () -> {
                    try {
                        RequestContextHolder.setRequestAttributes(requestAttributes);
                        runnable.run();
                        log.debug("{}|Successfully executed async task", LOG_PREFIX);
                    } catch (Exception e) {
                        log.error("{}|{}|Error executing async task: {}", 
                            LOG_PREFIX, ERROR, e.getMessage(), e);
                        throw e;
                    } finally {
                        RequestContextHolder.resetRequestAttributes();
                        log.debug("{}|Reset request attributes after async operation", LOG_PREFIX);
                    }
                };
            });

            log.info("{}|{}|Successfully created task executor with {} threads", 
                LOG_PREFIX, SUCCESS, threadCount);
            return executor;
        } catch (Exception e) {
            log.error("{}|{}|Failed to create task executor: {}", 
                LOG_PREFIX, ERROR, e.getMessage(), e);
            throw e;
        }
    }
}
