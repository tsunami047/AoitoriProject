package io.aoitori043.aoitoriproject.config;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: natsumi
 * @CreateTime: 2024-03-24  21:47
 * @Description: ?
 */
/*
折叠映射，对单个对象进行映射
对一个MAP进行映射，形如：
A:
 XXX
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GetFoldMapping {

//    @NotNull
//    Class<?> mapper();
}
