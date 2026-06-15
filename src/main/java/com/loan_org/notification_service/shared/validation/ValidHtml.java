package com.loan_org.notification_service.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = HtmlValidator.class)
@Target({ ElementType.FIELD, ElementType.RECORD_COMPONENT })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidHtml {
    String message() default "Invalid HTML structure or syntax detected";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] classes() default {};
}