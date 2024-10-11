package io.aoitori043.aoitoriproject.command;


import java.util.ArrayList;
import java.util.List;

/**
 * @Author: natsumi
 * @CreateTime: 2024-10-11  22:17
 * @Description: ?
 */
public class ArgumentList<T> extends ArrayList<T> {

    public T get(int index) {
        if (super.size() < index + 1) {
            return null;
        } else {
            return super.get(index);
        }
    }
}
