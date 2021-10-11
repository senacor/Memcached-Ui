package com.senacor.memcachedui.connector.utils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MemcachedUtils {

    public static Set<Integer> getSlabIds(Collection<Map<String, String>> stats) {
        return stats.stream()
                .flatMap(slab -> slab.keySet().stream())
                .map(key -> key.split(":"))
                .filter(keySplit -> keySplit.length == 2)
                .map(keySplit -> keySplit[0])
                .map(Integer::valueOf)
                .collect(Collectors.toSet());
    }
}
