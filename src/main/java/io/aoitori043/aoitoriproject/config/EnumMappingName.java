package io.aoitori043.aoitoriproject.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: natsumi
 * @CreateTime: 2024-05-21  21:59
 * @Description: ?
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EnumMappingName {
}
