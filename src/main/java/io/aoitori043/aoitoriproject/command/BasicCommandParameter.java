package io.aoitori043.aoitoriproject.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: natsumi
 * @CreateTime: 2024-03-23  14:43
 * @Description: ?
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BasicCommandParameter {

    String name() default "usePluginName";
    String description() default "none";

}
