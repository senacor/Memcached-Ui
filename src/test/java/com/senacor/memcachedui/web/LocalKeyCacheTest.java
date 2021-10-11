package com.senacor.memcachedui.web;

import com.senacor.memcachedui.model.Key;
import com.senacor.memcachedui.model.Namespace;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

public class LocalKeyCacheTest {

    private LocalKeyCache underTest;

    @BeforeEach
    void setup() {
        underTest = new LocalKeyCache();
    }

    @Test
    void testStoreKeysDefaultNamespace() {
        List<Key> testSet = List.of(
                new Key.Builder("key1").build(),
                new Key.Builder("key2").build(),
                new Key.Builder("key3").build()
        );
        underTest.storeKeys(testSet);

        testSet.forEach(key -> key.setNamespace(Namespace.DEFAULT_NAMESPACE));

        Assertions.assertThat(underTest.getKeysForNamespace(Namespace.DEFAULT_NAMESPACE)).isEqualTo(testSet);
        Assertions.assertThat(underTest.getNamespaces().getClass()).isEqualTo(HashSet.class);
        Assertions.assertThat(underTest.getNamespaces().stream().map(Namespace::getName).collect(Collectors.toSet()))
                .isEqualTo(Set.of(Namespace.DEFAULT_NAMESPACE));
    }

    @Test
    void testStoreKeysCustomNamespaces() {
        List<Key> testSet = List.of(
                new Key.Builder("key1").setNamespace("namespace1").build(),
                new Key.Builder("key2").setNamespace("namespace1").build(),
                new Key.Builder("key3").setNamespace("namespace2").build()
        );

        underTest.storeKeys(testSet);

        Assertions.assertThat(underTest.getKeysForNamespace("namespace1"))
                .containsExactlyInAnyOrder(new Key("key1", "namespace1"), new Key("key2", "namespace1"));
        Assertions.assertThat(underTest.getKeysForNamespace("namespace2"))
                .containsExactlyInAnyOrder(new Key("key3", "namespace2"));
        Assertions.assertThat(underTest.getNamespaces().getClass()).isEqualTo(HashSet.class);
        Assertions.assertThat(underTest.getNamespaces().stream().map(Namespace::getName).collect(Collectors.toSet()))
                .isEqualTo(Set.of("namespace1", "namespace2"));
    }

    @Test
    void testStoreKeysDefaultAndCustomNamespaces() {
        List<Key> testSet = List.of(
                new Key.Builder("key1").setNamespace("namespace1").build(),
                new Key.Builder("key2").setNamespace("namespace1").build(),
                new Key.Builder("key3").setNamespace("namespace2").build(),
                new Key.Builder("key4").setNamespace(Namespace.DEFAULT_NAMESPACE).build()
        );
        underTest.storeKeys(testSet);

        Assertions.assertThat(underTest.getKeysForNamespace("namespace1"))
                .containsExactlyInAnyOrder(new Key("key1", "namespace1"), new Key("key2", "namespace1"));
        Assertions.assertThat(underTest.getKeysForNamespace("namespace2"))
                .containsExactlyInAnyOrder(new Key("key3", "namespace2"));
        Assertions.assertThat(underTest.getKeysForNamespace(Namespace.DEFAULT_NAMESPACE))
                .containsExactlyInAnyOrder(new Key("key4", Namespace.DEFAULT_NAMESPACE));
        Assertions.assertThat(underTest.getNamespaces().getClass()).isEqualTo(HashSet.class);
        Assertions.assertThat(underTest.getNamespaces().stream().map(Namespace::getName).collect(Collectors.toSet()))
                .isEqualTo(Set.of("namespace1", "namespace2", Namespace.DEFAULT_NAMESPACE));
    }

    @Test
    void testOverrideExistingKeys() {
        List<Key> testSet1 = List.of(
                new Key.Builder("key1").setNamespace("namespace1").build(),
                new Key.Builder("key2").setNamespace("namespace1").build(),
                new Key.Builder("key3").setNamespace("namespace2").build(),
                new Key.Builder("key4").setNamespace("namespace3").build()
        );
        List<Key> testSet2 = List.of(
                new Key.Builder("key5").setNamespace("namespace4").build(),
                new Key.Builder("key6").setNamespace("namespace5").build(),
                new Key.Builder("key7").setNamespace("namespace5").build(),
                new Key.Builder("key8").setNamespace(Namespace.DEFAULT_NAMESPACE).build()
        );
        underTest.storeKeys(testSet1);
        underTest.storeKeys(testSet2);

        Assertions.assertThat(underTest.getKeysForNamespace("namespace4"))
                .containsExactlyInAnyOrder(new Key("key5", "namespace4"));
        Assertions.assertThat(underTest.getKeysForNamespace("namespace5"))
                .containsExactlyInAnyOrder(new Key("key6", "namespace5"), new Key("key7", "namespace5"));
        Assertions.assertThat(underTest.getKeysForNamespace(Namespace.DEFAULT_NAMESPACE))
                .containsExactlyInAnyOrder(new Key("key8", Namespace.DEFAULT_NAMESPACE));
        Assertions.assertThat(underTest.getNamespaces().getClass()).isEqualTo(HashSet.class);
        Assertions.assertThat(underTest.getNamespaces().stream().map(Namespace::getName).collect(Collectors.toSet()))
                .isEqualTo(Set.of("namespace4", "namespace5", Namespace.DEFAULT_NAMESPACE));

    }
}
