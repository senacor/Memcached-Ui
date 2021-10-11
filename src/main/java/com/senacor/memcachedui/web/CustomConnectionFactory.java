package com.senacor.memcachedui.web;

import net.spy.memcached.CachedData;
import net.spy.memcached.DefaultConnectionFactory;
import net.spy.memcached.transcoders.SerializingTranscoder;
import net.spy.memcached.transcoders.Transcoder;

public class CustomConnectionFactory extends DefaultConnectionFactory {

    class CustomStringTranscoder extends SerializingTranscoder {
        @Override
        public Object decode(CachedData d) {
            return new String(d.getData());
        }
    }

    @Override
    public Transcoder<Object> getDefaultTranscoder() {

        return new CustomStringTranscoder();
    }
}
