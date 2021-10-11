package com.senacor.memcachedui.web;

import com.senacor.memcachedui.connector.MemcachedConnector;
import com.senacor.memcachedui.exception.MemcachedConnectorException;
import com.senacor.memcachedui.model.Key;
import com.senacor.memcachedui.model.MemorySize;
import com.senacor.memcachedui.model.Namespace;
import io.vavr.Tuple2;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.SocketAddress;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.senacor.memcachedui.utils.TimeFormatter.*;
import static org.apache.logging.log4j.LogManager.getLogger;

@Service
public class MemcachedUIService {

    private static final Logger LOGGER = getLogger(MemcachedUIService.class);

    private static final int RETRY = 5;

    private long nextCacheUpdateMillis = 0L;

    private static final long CACHE_UPDATE_WAIT_TIME_MILLIS = 5 * 60 * 1000L;

    public static final int KEYS_PER_PAGE = 100;

    public static final int PAGINATION_LENGTH = 7;

    private static final int PAGINATION_HALF_LENGTH = (int)Math.floor((double)PAGINATION_LENGTH/2);

    private final MemcachedConnector connector;

    private final LocalKeyCache localKeyCache;

    private final KeyMapper keyMapper;

    @Autowired
    public MemcachedUIService(@Qualifier("netSpy") MemcachedConnector connector, LocalKeyCache localKeyCache, KeyMapper keyMapper) {
        this.connector = connector;
        this.localKeyCache = localKeyCache;
        this.keyMapper = keyMapper;
    }

    @PostConstruct
    void init() {
        try {
            connector.connect();
            // check connection
            updateLocalCache(true);
            LOGGER.info("Successfully connected to " + connector.getAddresses().toString());
        } catch (IOException | NoSuchElementException e) {
            LOGGER.error("Connection to " + connector.getAddresses().toString() + " failed");
        }
    }

    /**
     * Updates the local key cache by retrieving keys from connected memcached instance.
     */
    public void updateLocalCache(boolean force) {
        long currentTimeMillis = System.currentTimeMillis();
        if (force || currentTimeMillis > nextCacheUpdateMillis) {
            try {
                Map<String, Tuple2<Long, Long>> keys;
                keys = connector.getKeys();
                LOGGER.info(keys.size() + " keys retrieved from cache");
                if (keys.size() > 0) {
                    LOGGER.info("Key sample: " + keys.keySet().iterator().next());
                }
                var keyList = keyMapper.mapKeys(keys);
                localKeyCache.storeKeys(keyList);
                LOGGER.info("Updated local cache");
            } catch (MemcachedConnectorException e) {
                LOGGER.error("Failed to retrieve keys from cache");
            }
            nextCacheUpdateMillis = currentTimeMillis + CACHE_UPDATE_WAIT_TIME_MILLIS;
        }
    }

    /**
     * Returns a set of namespaces used in the memcached instances.
     * NOTE: Memcached does not support namespaces. This method splits
     * the keys in the cache using the defined namespace separator.
     * @return Set of namespaces
     */
    public Set<Namespace> getNamespaces() {
        Set<Namespace> namespaces = localKeyCache.getNamespaces();
        LOGGER.info(String.format("Returning %d namespaces", namespaces.size()));
        return namespaces;
    }

    /**
     * Returns the keys for the specified namespace.
     * @param namespace Namespace
     * @return Set of keys in specified namespace
     */
    public List<Key> getKeysForNamespace(String namespace, Integer page) {
        List<Key> keys = localKeyCache.getKeysForNamespace(namespace);
        int startIndex = KEYS_PER_PAGE * page - KEYS_PER_PAGE;
        int endIndex = Math.min(keys.size(), KEYS_PER_PAGE * page);
        if (page < 1 || startIndex > keys.size()) {
            return List.of();
        }
        List<Key> keysToReturn = keys.subList(startIndex, endIndex);
        LOGGER.info(String.format("Returning %d keys for namespace \"%s\"", keysToReturn.size(), namespace));
        return keysToReturn;
    }

    public Map<Integer, Boolean> getPagination(String namespace, Integer page) {
        Map<Integer, Boolean> pagination = new TreeMap<>();
        List<Key> keys = localKeyCache.getKeysForNamespace(namespace);
        int totalPages = (int)Math.ceil((double) keys.size()/KEYS_PER_PAGE);
        if (page < 1 || page > totalPages) {
            pagination.put(page, true);
            return pagination;
        }
        int startPage;
        int endPage;
        if (totalPages > PAGINATION_LENGTH) {
            if (page - PAGINATION_HALF_LENGTH < 1) {
                startPage = 1;
                endPage = PAGINATION_LENGTH;
            } else if (page + PAGINATION_HALF_LENGTH > totalPages) {
                startPage = totalPages - PAGINATION_LENGTH + 1;
                endPage = totalPages;
            } else {
                startPage = page - PAGINATION_HALF_LENGTH;
                endPage = page + PAGINATION_HALF_LENGTH;
            }
        } else {
            startPage = 1;
            endPage = totalPages;
        }
        for (int i = startPage; i <= endPage; i++) {
            boolean active = i == page;
            pagination.put(i, active);
        }
        return pagination;
    }

    public List<Key> searchForKeysInNamespace(String namespaceId, String searchTerm) {
        Map<Key, Integer> result = new HashMap<>();
        int minScore = 100;
        int maxScore = 0;
        for (Key key: localKeyCache.getKeysForNamespace(namespaceId)) {
            int score = FuzzySearch.ratio(searchTerm, key.getName());
            minScore = Math.min(score, minScore);
            maxScore = Math.max(score, maxScore);
            result.put(key, score);
        }
        int threshold = maxScore - (int)((maxScore - minScore) * 0.2);

        return result.entrySet().stream()
                .filter(e -> e.getValue() > threshold)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public Map<String, String> getStats() {
        Map<String, String> stats = new HashMap<>();
        var statsGeneral = connector.getStats("");

        long uptime = 0;
        String version = "";

        long currentItems = 0;
        long totalItems = 0;
        long totalMemory = 0;
        long bytes = 0;

        int currConnections = 0;
        int totalConnections = 0;
        int maxConnections = 0;

        for (SocketAddress address: statsGeneral.keySet()) {
            uptime = Long.parseLong(statsGeneral.get(address).get("uptime"));
            version = statsGeneral.get(address).get("version");

            currentItems += Long.parseLong(statsGeneral.get(address).get("curr_items"));
            totalItems += Long.parseLong(statsGeneral.get(address).get("total_items"));
            totalMemory += Long.parseLong(statsGeneral.get(address).get("limit_maxbytes"));
            bytes += Long.parseLong(statsGeneral.get(address).get("bytes"));
            currConnections = Integer.parseInt(statsGeneral.get(address).get("curr_connections"));
            totalConnections = Integer.parseInt(statsGeneral.get(address).get("total_connections"));
            maxConnections = Integer.parseInt(statsGeneral.get(address).get("max_connections"));
        }

        double usedMemoryPercentage = ((double)bytes * 100) / totalMemory;
        double freeMemoryPercentage = 100 - usedMemoryPercentage;

        stats.put("uptime", getAgeFromSeconds(uptime));
        stats.put("version", String.valueOf(version));

        DecimalFormat formatter = new DecimalFormat("#,###");
        stats.put("current_items", formatter.format(currentItems));
        stats.put("total_items", formatter.format(totalItems));
        stats.put("curr_connections", String.valueOf(currConnections));
        stats.put("total_connections", String.valueOf(totalConnections));
        stats.put("max_connections", String.valueOf(maxConnections));
        stats.put("total_memory", new MemorySize(totalMemory).asString());
        stats.put("used_memory", new MemorySize(bytes).asString());
        stats.put("used_memory_percentage", String.format("%.2f", usedMemoryPercentage).replace(",", "."));
        stats.put("free_memory", new MemorySize(totalMemory - bytes).asString());
        stats.put("free_memory_percentage", String.format("%.2f", freeMemoryPercentage).replace(",", "."));

        LOGGER.info("Returning stats: " + stats);
        return stats;
    }

    /**
     * Get the value for the specified namespace and key.
     * @param namespace Namespace
     * @param keyId Key
     * @return Value for namespace and key
     */
    public String getValue(String namespace, String keyId) {
        try {
            Key key = localKeyCache.getKeyFromNamespace(keyId, namespace);
            if (key == null) {
                String message = String.format("Key \"%s\" not found", keyId);
                LOGGER.error(message);
                return "ERROR: " + message;
            }
            String fullKeyName = keyMapper.getFullKey(key);
            LOGGER.info(String.format("Get value for key \"%s\"", fullKeyName));
            String value = connector.getValue(fullKeyName);
            LOGGER.info(String.format("Value: %s", value));
            return value;
        } catch (MemcachedConnectorException e) {
            String message = String.format("Failed to retrieve value for key \"%s\" in namespace \"%s\"", keyId, namespace);
            LOGGER.error(message);
            return "ERROR: " + message;
        }
    }

    /**
     * Delete all keys in specified namespace.
     * @param namespace Namespace to delete the keys from
     */
    public void deleteNamespace(String namespace) {
        new HashSet<>(localKeyCache.getKeysForNamespace(namespace))
                .forEach(key -> {
                    try {
                        connector.deleteKey(keyMapper.getFullKey(key));
                    } catch (MemcachedConnectorException e) {
                        LOGGER.error(String.format("Failed to delete key \"%s\"", key.getName()));
                    }
                });
        localKeyCache.deleteNamespace(namespace);
        LOGGER.info(String.format("Namespace \"%s\" deleted", namespace));
    }

    /**
     * Delete a key from specified namespace.
     * @param namespace Namespace to delete the key from
     * @param keyId Key to delete
     */
    public void deleteKey(String namespace, String keyId) {
        try {
            Key key = localKeyCache.getKeyFromNamespace(keyId, namespace);
            if (key == null) {
                LOGGER.error(String.format("Key \"%s\" not found", keyId));
                return;
            }
            localKeyCache.deleteKey(namespace, keyId);
            connector.deleteKey(keyMapper.getFullKey(key));
            LOGGER.info(String.format("Key \"%s\" deleted", keyId));
        } catch (MemcachedConnectorException e) {
            LOGGER.error(String.format("Failed to delete key \"%s\"", keyId));
        }
    }

    /**
     * Delete all keys.
     */
    public void deleteKeys() {
        try {
            localKeyCache.flush();
            connector.flush();
            LOGGER.info("All keys deleted");
        } catch (MemcachedConnectorException e) {
            LOGGER.info(e.getMessage());
        }
    }
}
