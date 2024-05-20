package io.aoitori043.aoitoriproject.utils;

import lombok.Data;

import java.util.Objects;

/**
 * @Author: natsumi
 * @CreateTime: 2024-05-17  20:53
 * @Description: ?
 */
@Data
public class Pair<T,V> {
    private T key;
    private V value;
    public Pair(T key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair)) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(key, pair.key);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key);
    }
}
