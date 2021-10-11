package com.senacor.memcachedui.connector;

import com.senacor.memcachedui.exception.MemcachedConnectorException;
import com.senacor.memcachedui.web.StringTranscoder;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import net.rubyeye.xmemcached.KeyIterator;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.exception.MemcachedException;
import net.rubyeye.xmemcached.utils.AddrUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * Used to communicate with the memcached instance.
 */
@Component("xMemcached")
public class XMemcachedConnector extends BaseMemcachedConnector implements MemcachedConnector {

    @Value("${memcached.reconnect.interval.millis}")
    private int healSessionIntervalMillis;

    @Value("${memcached.reconnect.enabled}")
    private boolean healSession;

    private MemcachedClient client;

    public XMemcachedConnector(@Value("${memcached.hosts}") List<String> hosts) {
        super(hosts);
    }

    /**
     * Connect to the memcached instances.
     */
    @Override
    public void connect() throws IOException {
        MemcachedClientBuilder builder = new XMemcachedClientBuilder(
                AddrUtil.getAddresses(getAddressesAsString()));
        builder.setTranscoder(new StringTranscoder());
        builder.setHealSessionInterval(healSessionIntervalMillis);
        builder.setEnableHealSession(healSession);
        client = builder.build();
    }

    @Override
    public Map<SocketAddress, Map<String, String>> getStats(String command) {
        // not supported
        return new HashMap<>();
    }

    /**
     * Retrieve a value for the specified key.
     * @param key Key for the entry
     * @return Entry value
     */
    @Override
    public String getValue(String key) throws MemcachedConnectorException {
        try {
            return client.get(key);
        } catch (ArrayIndexOutOfBoundsException | InterruptedException | MemcachedException | TimeoutException e) {
            throw new MemcachedConnectorException("Failed to retrieve value for key \"" + key + "\"");
        }
    }

    /**
     * Returns all keys from all memcached instances.
     * !CAUTION: Deprecated method used, because at the time of development no
     * alternative could be found. The KeyIterator relies on the command
     * stats cachedump, which will be removed in the future.
     * The KeyIterator is also not very reliable and does not
     * always return all available keys.
     * @return Set of all keys
     */
    @Override
    public Map<String, Tuple2<Long, Long>> getKeys() throws MemcachedConnectorException {
        Map<String, Tuple2<Long, Long>> keys = new HashMap<>();
        try {
            for (String address: getAddresses()) {
                KeyIterator it = client.getKeyIterator(AddrUtil.getOneAddress(address));
                while(it.hasNext()) {
                    keys.put(it.next(), Tuple.of(0L, 0L));
                }
            }
        } catch (ArrayIndexOutOfBoundsException | InterruptedException | MemcachedException | TimeoutException e) {
            throw new MemcachedConnectorException("Failed to retrieve keys from memcached instance");
        }
        return keys;
    }

    /**
     * Delete a single entry using the provided key.
     * @param key Key for the entry
     */
    @Override
    public void deleteKey(String key) throws MemcachedConnectorException {
        try {
            client.delete(key);
        } catch (ArrayIndexOutOfBoundsException | InterruptedException | MemcachedException | TimeoutException e) {
            throw new MemcachedConnectorException("Failed to delete key \"" + key + "\"");
        }
    }

    /**
     * Delete all entries in all connected memcached instances.
     */
    @Override
    public void flush() throws MemcachedConnectorException {
        try {
            client.flushAll();
        } catch (ArrayIndexOutOfBoundsException | InterruptedException | MemcachedException | TimeoutException e) {
            throw new MemcachedConnectorException("Failed to flush cache");
        }
    }
}
