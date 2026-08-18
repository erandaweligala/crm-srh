package com.adl.et.telco.crm.securerequesthandler.domain.service.serviceinterface;


import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface AsyncAdaptorInterface {
    void runAll(Long timeOut,Runnable... tasks);

    void runAsync(Runnable task);

    CompletableFuture<Object>[] supplyAll(Long timeOut, Supplier<?>... tasks);

}
