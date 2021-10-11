package com.senacor.memcachedui.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class KeyTest {

    @Test
    void testBuilderKeyId() {
        final String keyName = "key";
        Key key = new Key.Builder(keyName).build();
        Assertions.assertThat(key.getName()).isEqualTo(keyName);
        Assertions.assertThat(key.getNamespace()).isNull();
        Assertions.assertThat(key.getTimestamp()).isEqualTo(0L);
        Assertions.assertThat(key.getMemSize()).isEqualTo(new MemorySize(0));
    }

    @Test
    void testBuilderAllFields() {
        final String keyName = "key";
        final String namespace = "namespace";
        final long timestamp = 1633890000000L;
        final long bytes = 10 * 1024 * 1024;
        Key key = new Key.Builder("")
                .setName(keyName)
                .setNamespace(namespace)
                .setTimestamp(timestamp)
                .setMemSize(bytes)
                .build();
        Assertions.assertThat(key.getName()).isEqualTo(keyName);
        Assertions.assertThat(key.getNamespace()).isEqualTo(namespace);
        Assertions.assertThat(key.getTimestamp()).isEqualTo(timestamp);
        Assertions.assertThat(key.getMemSize()).isEqualTo(new MemorySize(bytes));
        Assertions.assertThat(key.getMemSize().getValue()).isEqualTo(10);
        Assertions.assertThat(key.getMemSize().getUnit()).isEqualTo(MemorySize.MemoryUnit.MB);
    }
}
