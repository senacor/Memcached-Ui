package com.senacor.memcachedui.web;

import com.senacor.memcachedui.model.Key;
import com.senacor.memcachedui.model.Namespace;
import io.vavr.control.Option;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class LocalKeyCache {

    private final Map<String, Namespace> cachedKeys = new HashMap<>();

    public void storeKeys(List<Key> keys) {
        cachedKeys.clear();
        keys.forEach(
                key -> {
                    String namespaceId = Objects.requireNonNullElse(key.getNamespace(), Namespace.DEFAULT_NAMESPACE);
                    cachedKeys.computeIfAbsent(namespaceId, k -> new Namespace(namespaceId, new ArrayList<>()));
                    cachedKeys.get(namespaceId).addKey(key);
                }
        );
    }

    public Set<Namespace> getNamespaces() {
        return new HashSet<>(cachedKeys.values());
    }

    public Key getKeyFromNamespace(String keyId, String namespace) {
        return cachedKeys.get(namespace).getKey(keyId);
    }

    public List<Key> getKeysForNamespace(String namespace) {
        return Option.of(cachedKeys.get(namespace))
                .map(Namespace::getKeys)
                .getOrElse(List.of());
    }

    public void deleteNamespace(String namespace) {
        cachedKeys.remove(namespace);
    }

    public void deleteKey(String namespace, String keyId) {
        cachedKeys.get(namespace).removeKey(keyId);
    }

    public void flush() {
        cachedKeys.clear();
    }
}
