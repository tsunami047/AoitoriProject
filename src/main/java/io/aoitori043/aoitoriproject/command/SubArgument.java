package io.aoitori043.aoitoriproject.command;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: natsumi
 * @CreateTime: 2024-03-23  13:36
 * @Description: ?
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SubArgument {
    @NotNull
    String argument();
    int minLength() default -1;
    int weight() default -1;
    String help();
}
