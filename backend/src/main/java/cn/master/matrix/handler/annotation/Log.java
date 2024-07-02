package cn.master.matrix.handler.annotation;

import cn.master.matrix.constants.OperationLogType;

import java.lang.annotation.*;

/**
 * @author Created by 11's papa on 06/28/2024
 **/
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    OperationLogType type() default OperationLogType.SELECT;

    String expression();

    Class[] mmClass() default {};
}
