package com.example.learnvalidation.constraints;

import com.example.learnvalidation.validators.CapitalizedValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CapitalizedValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface CapitalizedConstraint {
    String message() default "Chữ đầu tiên phải được in hoa!";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
