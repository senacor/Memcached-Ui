package com.senacor.memcachedui.web;

import net.rubyeye.xmemcached.transcoders.CachedData;
import net.rubyeye.xmemcached.transcoders.CompressionMode;
import net.rubyeye.xmemcached.transcoders.Transcoder;

public class StringTranscoder implements Transcoder<String> {

    @Override
    public CachedData encode(String s) {
        var data = new CachedData();
        data.setData(s.getBytes());
        return data;
    }

    @Override
    public String decode(CachedData cachedData) {
        return new String(cachedData.getData());
    }

    @Override
    public void setPrimitiveAsString(boolean b) {

    }

    @Override
    public void setPackZeros(boolean b) {

    }

    @Override
    public void setCompressionThreshold(int i) {

    }

    @Override
    public boolean isPrimitiveAsString() {
        return false;
    }

    @Override
    public boolean isPackZeros() {
        return false;
    }

    @Override
    public void setCompressionMode(CompressionMode compressionMode) {

    }
}
