package io.aoitori043.aoitoriproject.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author: natsumi
 * @CreateTime: 2024-11-05  23:22
 * @Description: ?
 */
public class RLUCache<K, V> extends LinkedHashMap<K, V> {
    private final int maxCapacity;

    public RLUCache(int maxCapacity) {
        super(maxCapacity + 1, 0.75f, true);
        this.maxCapacity = maxCapacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxCapacity;
    }
}