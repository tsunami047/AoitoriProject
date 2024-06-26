package io.aoitori043.aoitoriproject.database.orm.sign;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//也要生成离散根索引
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ForeignAggregateRoot {

    Class mapEntity();
}
