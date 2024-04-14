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
//设置运行的时候可见
@Target(ElementType.METHOD)
//作用对象是方法
public @interface Parameter {

    //参数，可以在注解上传回参数
    String argument();
    String help();
    ParameterSpecification.Type type() default ParameterSpecification.Type.Text;
    boolean nullable() default false;
}
