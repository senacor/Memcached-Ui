package com.senacor.memcachedui.connector;

import com.senacor.memcachedui.connector.utils.MemcachedUtils;
import com.senacor.memcachedui.web.CustomConnectionFactory;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.LogManager.getLogger;

@Component("netSpy")
public class NetSpyMemcachedConnector extends BaseMemcachedConnector implements MemcachedConnector {

    private static final Logger LOGGER = getLogger(NetSpyMemcachedConnector.class);

    // 0 stands for all keys
    private static final int KEYS_TO_DUMP = 0;

    private final KeyReader keyReader;

    private List<MemcachedClient> clients;

    public NetSpyMemcachedConnector(@Value("${memcached.hosts}") List<String> hosts, KeyReader keyReader) {
        super(hosts);
        this.keyReader = keyReader;
        clients = new ArrayList<>();
    }

    /**
     * Connect to the memcached instances.
     */
    @Override
    public void connect() throws IOException {
        keyReader.setHosts(getAddresses());
        for (String address: getAddresses()) {
            clients.add(new MemcachedClient(
                    new CustomConnectionFactory(),
                    AddrUtil.getAddresses(address))
            );
        }
    }

    @Override
    public Map<SocketAddress, Map<String, String>> getStats(String command) {
        return clients.stream()
                .flatMap(client -> client.getStats().entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Retrieve a value for the specified key.
     * @param key Key for the entry
     * @return Entry value
     */
    @Override
    public String getValue(String key) {
        for (var client: clients) {
            String value = (String) client.get(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /**
     * Returns all keys from all memcached instances.
     * !CAUTION: The command "stats cachedump" is  not very reliable
     * and does not always return all available keys. At the time of
     * development no alternative could be found.
     * @return Set of all keys
     */
    @Override
    public Map<String, Tuple2<Long, Long>> getKeys() {
        try {
            return keyReader.getKeys();
        } catch (IOException e) {
            LOGGER.error("Failed to retrieve all keys");
        }
        return clients.stream()
                .flatMap(client -> getKeys(client).entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<String, Tuple2<Long, Long>> getKeys(MemcachedClient client) {
        Map<String, Tuple2<Long, Long>> keys = new HashMap<>();
        Set<Integer> slabIds = MemcachedUtils.getSlabIds(client.getStats("slabs").values());
        slabIds.stream()
                .map(id -> client.getStats(String.format("cachedump %d %d", id, KEYS_TO_DUMP)))
                .flatMap(dump -> dump.values().stream())
                .forEach(dumpEntries -> {
                    dumpEntries.keySet()
                            .forEach(key -> {
                                Matcher matcher = Pattern.compile("\\[(\\d*) b; (\\d*) s\\]").matcher(dumpEntries.get(key));
                                if (matcher.find()) {
                                    final long bytes = Long.parseLong(matcher.group(1));
                                    final long timestampMillis = Long.parseLong(matcher.group(2)) * 1000L;
                                    keys.put(key, Tuple.of(bytes, timestampMillis));
                                } else {
                                    keys.put(key, Tuple.of(0L, 0L));
                                }
                            });
                });
        return keys;
    }

    @Override
    public void deleteKey(String key) {
        clients.forEach(client -> client.delete(key));
    }

    @Override
    public void flush() {
        clients.forEach(MemcachedClient::flush);
    }

    public void close() {
        clients.forEach(MemcachedClient::shutdown);
    }
}
