package io.aoitori043.aoitoriproject.database.orm.sign;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Drop {

    enum Period{
        DAILY,
        WEEKLY,
        MONTHLY
    }

    Period period() default Period.DAILY;
    int hour() default 0;
    int week() default 1;
    int day() default 1;
}
