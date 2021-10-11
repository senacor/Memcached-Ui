package com.senacor.memcachedui.web;

import com.senacor.memcachedui.connector.MemcachedConnector;
import com.senacor.memcachedui.model.Key;
import com.senacor.memcachedui.model.Namespace;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

public class MemcachedUIServiceTest {

    private static MemcachedUIService underTest;

    private static LocalKeyCache localKeyCache = new LocalKeyCache();

    private static MemcachedConnector connector;

    private static KeyMapper keyMapper;

    @BeforeEach
    void setup() {
        connector = mock(MemcachedConnector.class);
        keyMapper = mock(KeyMapper.class);
        underTest = new MemcachedUIService(connector, localKeyCache, keyMapper);
    }

    @Test
    void testGetPaginationPageZero() {
        List<Key> keys = new ArrayList<>();
        for (int i = 0; i < 10 * MemcachedUIService.KEYS_PER_PAGE; i++) {
            keys.add(new Key.Builder("key-" + i).setNamespace(Namespace.DEFAULT_NAMESPACE).build());
        }
        localKeyCache.storeKeys(keys);

        Map<Integer, Boolean> pagination = underTest.getPagination(Namespace.DEFAULT_NAMESPACE, 0);

        Assertions.assertThat(pagination).containsExactly(
                Map.entry(0, true)
        );
    }

    @Test
    void testGetPaginationPageToBig() {
        List<Key> keys = new ArrayList<>();
        for (int i = 0; i < 10 * MemcachedUIService.KEYS_PER_PAGE; i++) {
            keys.add(new Key.Builder("key-" + i).setNamespace(Namespace.DEFAULT_NAMESPACE).build());
        }
        localKeyCache.storeKeys(keys);

        Map<Integer, Boolean> pagination = underTest.getPagination(Namespace.DEFAULT_NAMESPACE, 11);

        Assertions.assertThat(pagination).containsExactly(
                Map.entry(11, true)
        );
    }

    @Test
    void testGetPaginationFirstPage() {
        List<Key> keys = new ArrayList<>();
        for (int i = 0; i < 100 * MemcachedUIService.KEYS_PER_PAGE; i++) {
            keys.add(new Key.Builder("key-" + i).setNamespace(Namespace.DEFAULT_NAMESPACE).build());
        }
        localKeyCache.storeKeys(keys);

        Map<Integer, Boolean> pagination = underTest.getPagination(Namespace.DEFAULT_NAMESPACE, 1);

        Assertions.assertThat(pagination).containsExactly(
                Map.entry(1, true),
                Map.entry(2, false),
                Map.entry(3, false),
                Map.entry(4, false),
                Map.entry(5, false),
                Map.entry(6, false),
                Map.entry(7, false)
        );
    }

    @Test
    void testGetPaginationPageInMiddle() {
        List<Key> keys = new ArrayList<>();
        for (int i = 0; i < 100 * MemcachedUIService.KEYS_PER_PAGE; i++) {
            keys.add(new Key.Builder("key-" + i).setNamespace(Namespace.DEFAULT_NAMESPACE).build());
        }
        localKeyCache.storeKeys(keys);

        Map<Integer, Boolean> pagination = underTest.getPagination(Namespace.DEFAULT_NAMESPACE, 50);

        Assertions.assertThat(pagination).containsExactly(
                Map.entry(47, false),
                Map.entry(48, false),
                Map.entry(49, false),
                Map.entry(50, true),
                Map.entry(51, false),
                Map.entry(52, false),
                Map.entry(53, false)
        );
    }

    @Test
    void testGetPaginationLastPage() {
        List<Key> keys = new ArrayList<>();
        for (int i = 0; i < 100 * MemcachedUIService.KEYS_PER_PAGE; i++) {
            keys.add(new Key.Builder("key-" + i).setNamespace(Namespace.DEFAULT_NAMESPACE).build());
        }
        localKeyCache.storeKeys(keys);

        Map<Integer, Boolean> pagination = underTest.getPagination(Namespace.DEFAULT_NAMESPACE, 100);

        Assertions.assertThat(pagination).containsExactly(
                Map.entry(94, false),
                Map.entry(95, false),
                Map.entry(96, false),
                Map.entry(97, false),
                Map.entry(98, false),
                Map.entry(99, false),
                Map.entry(100, true)
        );
    }
}
