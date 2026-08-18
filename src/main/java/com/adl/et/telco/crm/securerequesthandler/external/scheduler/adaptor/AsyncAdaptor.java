package com.adl.et.telco.crm.securerequesthandler.external.scheduler.adaptor;

import com.adl.et.telco.crm.securerequesthandler.domain.service.serviceinterface.AsyncAdaptorInterface;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class AsyncAdaptor implements AsyncAdaptorInterface {
    private final TaskExecutor executor;
    @Value("${executor.default.timeout}")
    private Long defaultTimeOut;

    public AsyncAdaptor(@Qualifier("asyncExecutor") TaskExecutor executor) {
        this.executor = executor;
    }

    /**
     * Execute provided tasks in parallel. Http Request context will be propagated.
     * Complete when all tasks are completed.
     *
     * @param tasks Tasks to run.
     */
    @Override
    @SneakyThrows
    public void runAll(Long timeOut,Runnable... tasks) {

        CompletableFuture<?>[] completableFutures = Arrays.stream(tasks)
                .map(task -> CompletableFuture.runAsync(task, executor)).collect(Collectors.toList())
                .toArray(new CompletableFuture[]{});

        try {
            CompletableFuture.allOf(completableFutures).get(Objects.nonNull(timeOut) ? timeOut : defaultTimeOut, TimeUnit.MILLISECONDS);
        } catch ( CancellationException | TimeoutException ex) {
            throw ex;
        } catch (ExecutionException ex) {
            throw ex.getCause();
        }
    }

    /**
     * Run provided Task in Async.
     *
     * @param task Task to run.
     */
    @Override
    public void runAsync(Runnable task) {

        executor.execute(task);
    }

    /**
     * Execute provided tasks in parallel using supplyAsync. Http Request context will be propagated.
     * Complete when all tasks are completed.
     *
     * @param tasks Tasks to run.
     */
    @Override
    @SneakyThrows
    public CompletableFuture<Object>[] supplyAll(Long timeOut, Supplier<?>... tasks) {
        CompletableFuture<Object>[] completableFutures = Arrays.stream(tasks)
                .map(task -> CompletableFuture.supplyAsync(task, executor)).collect(Collectors.toList())
                .toArray(new CompletableFuture[]{});

        try {
            CompletableFuture.allOf(completableFutures).get(Objects.nonNull(timeOut) ? timeOut : defaultTimeOut, TimeUnit.MILLISECONDS);
            return completableFutures;
        } catch (ExecutionException e) {

            throw e.getCause();
        } catch (CancellationException | TimeoutException ex) {

            throw ex;
        }
    }
}
