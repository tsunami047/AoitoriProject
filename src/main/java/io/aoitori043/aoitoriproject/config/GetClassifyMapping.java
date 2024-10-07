package io.aoitori043.aoitoriproject.config;

import io.aoitori043.aoitoriproject.AoitoriProject;
import lombok.Data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: natsumi
 * @CreateTime: 2024-10-03  12:39
 * @Description: ?
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface GetClassifyMapping {

    String anchor();
    ClassDesignation[] appoint();


    @interface ClassDesignation {
        String key();
        Class value();
    }
}
