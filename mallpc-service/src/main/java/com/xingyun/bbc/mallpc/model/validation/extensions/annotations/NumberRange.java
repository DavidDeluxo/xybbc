package com.xingyun.bbc.mallpc.model.validation.extensions.annotations;

import com.xingyun.bbc.mallpc.model.validation.extensions.NumberRangeConstraintValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 数字类型变量值域约束
 *
 * @author penglu
 * @version 1.0.0
 * @date 2019-09-05
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
@Documented
@Constraint(validatedBy = NumberRangeConstraintValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NumberRange {

    String message() default "{field value is invalid}";

    long[] values();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
