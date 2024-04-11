package io.aoitori043.aoitoriproject.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: natsumi
 * @CreateTime: 2024-01-17  06:31
 * @Description: ?
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DefaultValue {

    String stringValue() default "";
    int intValue() default 0;
    double doubleValue() default 0.0;
    float floatValue() default 0.0f;
    boolean booleanValue() default false;
    String[] listValue() default {};
}
