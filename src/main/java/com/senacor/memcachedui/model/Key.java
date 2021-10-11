package com.senacor.memcachedui.model;

import com.senacor.memcachedui.utils.TimeFormatter;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class Key {

    private String name;
    private MemorySize memSize;
    private long timestamp;
    private String namespace;

    public Key(String name) {
        this(name, new MemorySize(0L), 0L, null);
    }

    public Key(String name, String namespace) {
        this(name, new MemorySize(0L), 0, namespace);
    }

    public Key(String name, MemorySize memSize, int timestamp) {
        this(name, memSize, timestamp, null);
    }

    public Key(String name, MemorySize memSize, long timestamp, String namespace) {
        this.name = name;
        this.memSize = memSize;
        this.timestamp = timestamp;
        this.namespace = namespace;
    }

    public String getMemSizeAsString() {
        return memSize.asString();
    }

    public void setMemSize(int bytes) {
        this.memSize = new MemorySize(bytes);
    }

    public String getAge() {
        long totalSeconds = (System.currentTimeMillis() - timestamp) / 1000;
        return TimeFormatter.getAgeFromSeconds(totalSeconds);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Key key = (Key) o;
        return name.equals(key.name) && Objects.equals(namespace, key.namespace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, namespace);
    }

    @Override
    public String toString() {
        return "Key{" +
                "name='" + name + '\'' +
                ", memSize=" + memSize +
                ", timestamp=" + timestamp +
                ", namespace=" + namespace +
                '}';
    }

    public static class Builder {
        private String name;
        private MemorySize memSize;
        private long timestamp;
        private String namespace = null;

        public Builder(String name) {
            this.name = name;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setMemSize(long bytes) {
            this.memSize = new MemorySize(bytes);
            return this;
        }

        public Builder setTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder setNamespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        public Key build() {
            return new Key(
                    this.name,
                    this.memSize == null ? new MemorySize(0L) : this.memSize,
                    this.timestamp,
                    this.namespace
            );
        }
    }
}
