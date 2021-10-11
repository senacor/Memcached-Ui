package com.senacor.memcachedui.connector;

import com.senacor.memcachedui.web.MemcachedUIService;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.apache.logging.log4j.LogManager.getLogger;


@Component
public class KeyReader {
    private List<String> hosts;
    private final TelnetClient telnetClient;

    private static final long TIMEOUT = 15L;
    private static final int RESPONSE_STRING_SIZE = 7;
    private static final int RESPONSE_STRING_KEY_INDEX = 0;
    private static final int RESPONSE_STRING_TIMESTAMP_INDEX = 2;
    private static final int RESPONSE_STRING_SIZE_INDEX = 6;

    private static final Logger LOGGER = getLogger(MemcachedUIService.class);

    public KeyReader(List<String> hosts) {
        this.hosts = hosts;
        this.telnetClient = new TelnetClient();
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public Map<String, Tuple2<Long, Long>> getKeys() throws IOException {
        java.util.List<String> rawKeys = new ArrayList<>();
        for (String hostString: hosts) {
            String[] host = hostString.split(":");
            telnetClient.connect(host[0],Integer.parseInt(host[1]));
            LOGGER.info("Connected to " + hostString);

            PrintStream out = new PrintStream(telnetClient.getOutputStream());
            InputStream in = telnetClient.getInputStream();
            out.println("lru_crawler metadump all");
            out.flush();

            ExecutorService es = Executors.newSingleThreadExecutor();
            TimeLimiter tl = SimpleTimeLimiter.create(es);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            try {
                tl.callWithTimeout(() -> {
                    String line = java.net.URLDecoder.decode(reader.readLine(), StandardCharsets.UTF_8.name());
                    while (!line.startsWith("END")) {
                        rawKeys.add(line);
                        line = java.net.URLDecoder.decode(reader.readLine(), StandardCharsets.UTF_8.name());
                    }
                    return null;
                }, TIMEOUT, TimeUnit.SECONDS);
            } catch (ExecutionException | TimeoutException | InterruptedException e) {
                LOGGER.error("Error occurred: " + e.getMessage());
            } finally {
                es.shutdown();
                in.close();
                telnetClient.disconnect();
            }
        }
        return parseKeys(rawKeys);
    }

    private Map<String, Tuple2<Long, Long>> parseKeys(List<String> rawKeys) {
        Map<String, Tuple2<Long, Long>> keys = new HashMap<>();
        try {
            rawKeys.stream()
                    .filter(rawKey -> rawKey.startsWith("key"))
                    .map(rawKey -> rawKey.split(" "))
                    .filter(rawKey -> rawKey.length == RESPONSE_STRING_SIZE)
                    .forEach(rawKey -> {
                        String key = rawKey[RESPONSE_STRING_KEY_INDEX].substring("key=".length());
                        Long timestamp = Long.parseLong(rawKey[RESPONSE_STRING_TIMESTAMP_INDEX].substring("la=".length()));
                        Long size = Long.parseLong(rawKey[RESPONSE_STRING_SIZE_INDEX].substring("size=".length()));
                        keys.put(key, Tuple.of(size, timestamp));
                    });
        } catch (ArrayIndexOutOfBoundsException e) {
            LOGGER.error("Failed to parse keys: " + e.getMessage());
        }
        return keys;
    }
}
