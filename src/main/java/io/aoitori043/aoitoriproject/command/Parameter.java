package io.aoitori043.aoitoriproject.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: natsumi
 * @CreateTime: 2024-04-13  16:33
 * @Description: ?
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Parameter {

    String argument();
    String help();
    ParameterSpecification.Type type() default ParameterSpecification.Type.Text;
    boolean nullable() default false;
}
