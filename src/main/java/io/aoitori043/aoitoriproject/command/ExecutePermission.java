package io.aoitori043.aoitoriproject.command;

import org.jetbrains.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: natsumi
 * @CreateTime: 2024-03-23  13:42
 * @Description: ?
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExecutePermission {
    boolean isOp() default false;
    @Nullable
    String permission() default "nope.";
}
