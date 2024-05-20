package io.aoitori043.aoitoriproject.config;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: natsumi
 * @CreateTime: 2024-03-24  21:40
 * @Description: ?
 */
/*
对一个MAP进行映射，形如：
A:
 A1:
   XXX
 A2:
   XXX
 A3:
   XXX
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GetMapping {
//    @NotNull
//    Class<?> mapper();
}
