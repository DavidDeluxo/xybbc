package com.xingyun.bbc.mallpc.model.validation.extensions;

import com.xingyun.bbc.mallpc.model.validation.extensions.annotations.NumberRange;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigInteger;
import java.util.Objects;

/**
 * 数字类型变量值域约束验证器
 *
 * @author penglu
 * @version 1.0.0
 * @date 2019-09-05
 * @copyright 本内容仅限于浙江云贸科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */
public class NumberRangeConstraintValidator implements ConstraintValidator<NumberRange, Number> {

    private long[] values;

    @Override
    public void initialize(NumberRange numberRange) {
        this.values = numberRange.values();
    }

    @Override
    public boolean isValid(Number value, ConstraintValidatorContext context) {
        if (Objects.isNull(value)) {
            return true;
        }
        for (long each : values) {
            if (value instanceof BigInteger) {
                if (((BigInteger) value).compareTo(BigInteger.valueOf(each)) == 0) {
                    return true;
                }
            } else {
                if (each == value.longValue()) {
                    return true;
                }
            }
        }
        return false;
    }

}
