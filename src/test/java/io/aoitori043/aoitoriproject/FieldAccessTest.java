package io.aoitori043.aoitoriproject;

import com.esotericsoftware.reflectasm.FieldAccess;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * @Author: natsumi
 * @CreateTime: 2024-11-02  18:59
 * @Description: ?
 */

public class FieldAccessTest {

    @Test
    public void startTest(){
        FieldAccess fieldAccess = FieldAccess.get(AoitoriProject.class);
        for (Field field : fieldAccess.getFields()) {
            System.out.println(field.getName());
        }
        System.out.println(Arrays.toString(fieldAccess.getFieldNames()));
        for (Field declaredField : AoitoriProject.class.getDeclaredFields()) {
            System.out.println(declaredField.getName());
        }
    }
}
