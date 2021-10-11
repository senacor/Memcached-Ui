package com.senacor.memcachedui.web;

import com.senacor.memcachedui.model.Key;
import com.senacor.memcachedui.model.MemorySize;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class KeyMapperTest {

    //TODO multiple keys

    private final static String TEST_KEY = "test-key";
    private final static String TEST_NAMESPACE = "test-namespace";
    private final static long TEST_TIMESTAMP = 1629758336433L;

    @ParameterizedTest
    @MethodSource("com.senacor.memcachedui.web.KeyMapperTest#validMapKeyData")
    void testMapKeyKeyStructure_validKeyString(
            String keyStructure,
            String keyString,
            String key,
            String namespace,
            long timestamp
    ) {
        KeyMapper keyMapper = new KeyMapper(keyStructure);
        var result = keyMapper.mapKey(keyString, 0L, 0L);
        Assertions.assertThat(result.getName()).isEqualTo(key);
        Assertions.assertThat(result.getNamespace()).isEqualTo(namespace);
        Assertions.assertThat(result.getTimestamp()).isEqualTo(timestamp);
    }

    public static Stream<Arguments> validMapKeyData() {
        return Stream.of(
                Arguments.of(
                        "prefix:NAMESPACE:TIMESTAMP:KEY",
                        String.format("prefix:%s:%d:%s", TEST_NAMESPACE, TEST_TIMESTAMP, TEST_KEY),
                        TEST_KEY,
                        TEST_NAMESPACE,
                        TEST_TIMESTAMP
                ),
                Arguments.of(
                        "prefix:NAMESPACE:KEY:TIMESTAMP",
                        String.format("prefix:%s:%s:%d", TEST_NAMESPACE, TEST_KEY, TEST_TIMESTAMP),
                        TEST_KEY,
                        TEST_NAMESPACE,
                        TEST_TIMESTAMP
                ),
                Arguments.of(
                        "prefix:TIMESTAMP:NAMESPACE:KEY",
                        String.format("prefix:%d:%s:%s", TEST_TIMESTAMP, TEST_NAMESPACE, TEST_KEY),
                        TEST_KEY,
                        TEST_NAMESPACE,
                        TEST_TIMESTAMP
                ),
                Arguments.of(
                        "prefix:KEY:TIMESTAMP:NAMESPACE",
                        String.format("prefix:%s:%d:%s", TEST_KEY, TEST_TIMESTAMP, TEST_NAMESPACE),
                        TEST_KEY,
                        TEST_NAMESPACE,
                        TEST_TIMESTAMP
                ),
                Arguments.of(
                        "prefix:NAMESPACE:KEY",
                        String.format("prefix:%s:%s", TEST_NAMESPACE, TEST_KEY),
                        TEST_KEY,
                        TEST_NAMESPACE,
                        0L
                ),
                Arguments.of(
                        "NAMESPACE:KEY",
                        String.format("%s:%s", TEST_NAMESPACE, TEST_KEY),
                        TEST_KEY,
                        TEST_NAMESPACE,
                        0L
                ),
                Arguments.of(
                        "prefix-KEY",
                        String.format("prefix-%s", TEST_KEY),
                        TEST_KEY,
                        null,
                        0L
                ),
                Arguments.of(
                        "KEY",
                        String.format("%s", TEST_KEY),
                        TEST_KEY,
                        null,
                        0L
                )
        );
    }

    @ParameterizedTest
    @MethodSource("com.senacor.memcachedui.web.KeyMapperTest#invalidMapKeyData")
    void testMapKeyKeyStructure_invalidKeyString(String keyStructure, String keyString) {
        KeyMapper keyMapper = new KeyMapper(keyStructure);
        var result = keyMapper.mapKey(keyString, 0L, 0L);
        Assertions.assertThat(result.getName()).isNotEqualTo(TEST_KEY);
        Assertions.assertThat(result.getName()).isEqualTo(keyString);
    }

    public static Stream<Arguments> invalidMapKeyData() {
        return Stream.of(
                Arguments.of(
                        "prefix:NAMESPACE:TIMESTAMP:KEY",
                        String.format("prefix:%s:%s", TEST_NAMESPACE, TEST_KEY)
                ),
                Arguments.of(
                        "prefix:NAMESPACE:NAMESPACE:KEY",
                        String.format("prefix:%s:%s:%s", TEST_NAMESPACE, TEST_NAMESPACE, TEST_KEY)
                ),
                Arguments.of(
                        "prefix:NAMESPACE",
                        String.format("prefix:%s:%s:%s", TEST_NAMESPACE, TEST_NAMESPACE, TEST_KEY)
                ),
                Arguments.of(
                        "prefix:NAMESPACE:TIMESTAMP:KEY",
                        String.format("prefix:%d:%s", TEST_TIMESTAMP, TEST_KEY)
                ),
                Arguments.of(
                        "prefix:NAMESPACE:TIMESTAMP:KEY",
                        String.format("prefix:%s:%d", TEST_NAMESPACE, TEST_TIMESTAMP)
                ),
                Arguments.of(
                        "prefix:NAMESPACE:TIMESTAMP:KEY",
                        String.format("prefix:%s", TEST_NAMESPACE)
                ),
                Arguments.of(
                        "prefix:NAMESPACE:TIMESTAMP:KEY",
                        String.format("invalid-prefix:%s:%d:%s", TEST_NAMESPACE, TEST_TIMESTAMP, TEST_KEY)
                ),
                Arguments.of(
                        "prefix:NAMESPACE:TIMESTAMP:KEY",
                        ""
                )
        );
    }

    @Test
    void testMapKey_keyTimestampOverrideMemcachedTimestamp() {
        String keyStructure = "prefix:TIMESTAMP:KEY";
        String keyString = "prefix:1629758336433:key1";
        KeyMapper keyMapper = new KeyMapper(keyStructure);
        var result = keyMapper.mapKey(keyString, 0L, 1629758336435L);
        Assertions.assertThat(result.getName()).isEqualTo("key1");
        Assertions.assertThat(result.getTimestamp()).isEqualTo(1629758336433L);
    }

    @Test
    void testMapKeys_allKeysValid() {
        String keyStructure = "prefix:NAMESPACE:TIMESTAMP:KEY";
        Map<String, Tuple2<Long, Long>> keys = new HashMap<>();
        keys.put("prefix:namespace1:1629758336433:key1", Tuple.of(0L, 0L));
        keys.put("prefix:namespace1:1629758336433:key2", Tuple.of(0L, 0L));
        keys.put("prefix:namespace2:1629758336433:key3", Tuple.of(0L, 0L));
        keys.put("prefix:namespace2:1629758336433:key4", Tuple.of(0L, 0L));
        keys.put("prefix:namespace2:1629758336433:key5", Tuple.of(0L, 0L));
        keys.put("prefix:namespace3:1629758336433:key6", Tuple.of(0L, 0L));
        keys.put("prefix:namespace3:1629758336433:key7", Tuple.of(0L, 0L));

        KeyMapper keyMapper = new KeyMapper(keyStructure);
        List<Key> result = keyMapper.mapKeys(keys);

        Assertions.assertThat(result).containsExactlyInAnyOrder(
                new Key("key1", new MemorySize(0L), 1629758336433L, "namespace1"),
                new Key("key2", new MemorySize(0L), 1629758336433L, "namespace1"),
                new Key("key3", new MemorySize(0L), 1629758336433L, "namespace2"),
                new Key("key4", new MemorySize(0L), 1629758336433L, "namespace2"),
                new Key("key5", new MemorySize(0L), 1629758336433L, "namespace2"),
                new Key("key6", new MemorySize(0L), 1629758336433L, "namespace3"),
                new Key("key7", new MemorySize(0L), 1629758336433L, "namespace3")
        );
    }

    @Test
    void testMapKeys_someKeysInvalidStructure() {
        String keyStructure = "prefix:NAMESPACE:TIMESTAMP:KEY";
        Map<String, Tuple2<Long, Long>> keys = new HashMap<>();
        keys.put("prefix:namespace1:1629758336433:key1", Tuple.of(0L, 0L));
        keys.put("prefix:namespace1:1629758336433:key2", Tuple.of(0L, 0L));
        keys.put("prefix:namespace2:1629758336433:key3", Tuple.of(0L, 0L));
        keys.put("prefix:namespace2:key4", Tuple.of(0L, 0L));
        keys.put("prefix:namespace2:1629758336433:key5", Tuple.of(0L, 0L));
        keys.put("prefix:namespace3:1629758336433:key6", Tuple.of(0L, 0L));
        keys.put("prefix:1629758336433:key7", Tuple.of(0L, 0L));

        KeyMapper keyMapper = new KeyMapper(keyStructure);
        List<Key> result = keyMapper.mapKeys(keys);

        Assertions.assertThat(result).containsExactlyInAnyOrder(
                new Key("key1", new MemorySize(0L), 1629758336433L, "namespace1"),
                new Key("key2", new MemorySize(0L), 1629758336433L, "namespace1"),
                new Key("key3", new MemorySize(0L), 1629758336433L, "namespace2"),
                new Key("prefix:namespace2:key4", new MemorySize(0L), 0L, null),
                new Key("key5", new MemorySize(0L), 1629758336433L, "namespace2"),
                new Key("key6", new MemorySize(0L), 1629758336433L, "namespace3"),
                new Key("prefix:1629758336433:key7", new MemorySize(0L), 0L, null)
        );
    }
}
