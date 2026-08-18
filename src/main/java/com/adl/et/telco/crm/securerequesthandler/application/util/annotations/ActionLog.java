package com.adl.et.telco.crm.securerequesthandler.application.util.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ActionLog {
    int actionID();
    String subjectType() default "";
    String pathToSubjectValue() default "";
    boolean subjectIsAParam() default false;
    int requestBodyParamIndex() default 0;
    int httpRequestParamIndex() default 1;

}
