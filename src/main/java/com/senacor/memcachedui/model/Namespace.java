package com.senacor.memcachedui.model;

import java.util.ArrayList;
import java.util.List;

public class Namespace {

    public static final String DEFAULT_NAMESPACE = "default";

    private String name;
    private List<Key> keys;

    public Namespace() {
        this(DEFAULT_NAMESPACE, new ArrayList<>());
    }

    public Namespace(String name, List<Key> keys) {
        this.name = name;
        this.keys = keys;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Key getKey(String keyId) {
        return keys.stream()
                .filter(key -> key.getName().equals(keyId))
                .findFirst()
                .orElseGet(null);
    }

    public List<Key> getKeys() {
        return keys;
    }

    public void setKeys(List<Key> keys) {
        this.keys = keys;
    }

    public int getSize() {
        if (keys == null) {
            return 0;
        }
        return keys.size();
    }

    public MemorySize getMemSize() {
        return calculateMemorySize();
    }

    public String getMemSizeAsString() {
        return calculateMemorySize().asString();
    }

    public void removeKey(String keyId) {
        Key keyToRemove = null;
        for (Key key : keys) {
            if (key.getName().equals(keyId)) {
                keyToRemove = key;
                break;
            }
        }
        keys.remove(keyToRemove);
    }

    public void addKey(Key key) {
        keys.add(key);
    }

    private MemorySize calculateMemorySize() {
        return new MemorySize(
                keys.stream()
                        .map(Key::getMemSize)
                        .map(MemorySize::getBytes)
                        .reduce(0L, Long::sum)
        );
    }
}
