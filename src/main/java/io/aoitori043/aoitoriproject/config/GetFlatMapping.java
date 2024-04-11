package io.aoitori043.aoitoriproject.config;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: natsumi
 * @CreateTime: 2024-03-24  21:49
 * @Description:
 * 平面映射，可以指定枚举常量为Key进行映射，避免拼写错误
 * 枚举常量要求全大写，如果要分隔不同词语使用下划线，在映射时，下划线会被略去
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GetFlatMapping {
    @NotNull
    Class<?> mapper();
    String[] stringKeys() default {};
    @NotNull
    Class<? extends Enum> enumKeys();
}
