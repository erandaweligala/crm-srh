package com.adl.et.telco.crm.securerequesthandler.application.filter.responsefilter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResponseFiltering {
    int actionId();

    boolean enable() default true;
}
