package com.senacor.memcachedui.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class MemorySizeTest {

    @Test
    void testCalculateSizeBytes() {
        int bytes = 5;
        MemorySize ms = new MemorySize(bytes);
        Assertions.assertThat(ms.getValue()).isEqualTo(5);
        Assertions.assertThat(ms.getUnit()).isEqualTo(MemorySize.MemoryUnit.B);
    }

    @Test
    void testCalculateSizeKiloBytes() {
        int bytes = 1024 + 1;
        MemorySize ms = new MemorySize(bytes);
        Assertions.assertThat(ms.getValue()).isEqualTo((double)bytes/1024);
        Assertions.assertThat(ms.getUnit()).isEqualTo(MemorySize.MemoryUnit.KB);
    }

    @Test
    void testCalculateSizeMegaBytes() {
        int bytes = 1024 * 1024 + 1;
        MemorySize ms = new MemorySize(bytes);
        Assertions.assertThat(ms.getValue()).isEqualTo(((double)bytes/1024)/1024);
        Assertions.assertThat(ms.getUnit()).isEqualTo(MemorySize.MemoryUnit.MB);
    }

    @Test
    void testCalculateSizeGigaBytes() {
        int bytes = 1024 * 1024 * 1024 + 1;
        MemorySize ms = new MemorySize(bytes);
        Assertions.assertThat(ms.getValue()).isEqualTo((((double)bytes/1024)/1024)/1024);
        Assertions.assertThat(ms.getUnit()).isEqualTo(MemorySize.MemoryUnit.GB);
    }
}
