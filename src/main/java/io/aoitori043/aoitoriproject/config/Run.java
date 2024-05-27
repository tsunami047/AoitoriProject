package io.aoitori043.aoitoriproject.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: natsumi
 * @CreateTime: 2024-03-25  22:35
 * @Description: ?
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Run {
    String after() default "%%%default";
}
