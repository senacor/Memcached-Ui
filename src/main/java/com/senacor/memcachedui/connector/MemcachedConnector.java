package com.senacor.memcachedui.connector;

import com.senacor.memcachedui.exception.MemcachedConnectorException;
import io.vavr.Tuple2;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;

public interface MemcachedConnector {

    void connect() throws IOException;

    Map<SocketAddress, Map<String, String>> getStats(String command);

    Map<String, Tuple2<Long, Long>> getKeys() throws MemcachedConnectorException;

    String getValue(String key) throws MemcachedConnectorException;

    void deleteKey(String key) throws MemcachedConnectorException;

    void flush() throws MemcachedConnectorException;

    String getAddressesAsString();

    List<String> getAddresses();
}
