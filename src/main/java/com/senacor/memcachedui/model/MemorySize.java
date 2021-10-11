package com.senacor.memcachedui.model;

import java.util.Objects;

public class MemorySize {

    public enum MemoryUnit {
        B, KB, MB, GB
    }

    private static final int CONVERSION_FACTOR = 1024;

    private final long bytes;
    private MemoryUnit unit;
    private double value;

    public MemorySize(long bytes) {
        this.bytes = bytes;
        calculateSize(bytes);
    }

    public long getBytes() {
        return bytes;
    }

    public double getValue() {
        return value;
    }

    public MemoryUnit getUnit() {
        return unit;
    }

    private void calculateSize(double bytes) {

        double kiloBytes = bytes / CONVERSION_FACTOR;
        double megaBytes = kiloBytes / CONVERSION_FACTOR;
        double gigaBytes = megaBytes / CONVERSION_FACTOR ;

        if (gigaBytes >= 1) {
            value = gigaBytes;
            unit = MemoryUnit.GB;
        } else if (megaBytes >= 1) {
            value = megaBytes;
            unit = MemoryUnit.MB;
        } else if (kiloBytes >= 1) {
            value = kiloBytes;
            unit = MemoryUnit.KB;
        } else {
            value = bytes;
            unit = MemoryUnit.B;
        }
    }

    public String asString() {
        return String.format("%.2f %s", value, unit.toString());
    }

    @Override
    public String toString() {
        return "MemorySize{" +
                "value=" + value +
                ", unit=" + unit +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemorySize that = (MemorySize) o;
        return bytes == that.bytes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bytes);
    }
}
