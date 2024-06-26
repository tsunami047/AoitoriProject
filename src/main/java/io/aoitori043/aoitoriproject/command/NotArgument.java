package io.aoitori043.aoitoriproject.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: natsumi
 * @CreateTime: 2024-04-13  21:03
 * @Description: ?
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NotArgument {

    String help();
}
