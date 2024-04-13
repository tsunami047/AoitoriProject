package io.aoitori043.aoitoriproject.command;

import java.lang.annotation.*;

/**
 * @Author: natsumi
 * @CreateTime: 2024-03-23  14:26
 * @Description: ?
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(value = ParameterSpecifications.class)
public @interface ParameterSpecification {

    public static enum Type{
        Player,
        Int,
        Double,
        Text
    }

    int index();
    String tip();
    Type type() default Type.Text;
    boolean nullable() default false;
}

