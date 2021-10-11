package com.senacor.memcachedui.connector;

import org.apache.commons.lang3.StringUtils;
import java.util.List;

public abstract class BaseMemcachedConnector {

    private final List<String> hosts;

    public BaseMemcachedConnector(List<String> hosts) {
        this.hosts = hosts;
    }

    public String getAddressesAsString() {
        String addresses = StringUtils.join(hosts, " ");
        return addresses.strip();
    }

    /**
     * Returns a list of memcached host addresses.
     * @return List of host addresses
     */
    public List<String> getAddresses() {
        return hosts;
    }
}
