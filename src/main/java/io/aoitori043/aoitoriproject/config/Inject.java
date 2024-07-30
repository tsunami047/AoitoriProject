package io.aoitori043.aoitoriproject.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-19  04:13
 * @Description: ?
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Inject {

    InjectionType type();

    enum InjectionType {
        PARENT_NAME
    }
}
