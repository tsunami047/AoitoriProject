package io.aoitori043.aoitoriproject.database.orm.sign;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Deprecated //不应该有这种情况，数据结构是否存在问题？
public @interface OneToOne {

    String mapFieldName();
    Class mapEntity();
}
