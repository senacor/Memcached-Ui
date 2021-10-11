package com.senacor.memcachedui.web;

import com.senacor.memcachedui.model.Key;
import io.vavr.Tuple2;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.logging.log4j.LogManager.getLogger;

@Component
public class KeyMapper {

    private enum Tag {
        KEY("(.*)"),
        NAMESPACE("(.*)"),
        TIMESTAMP("(\\\\d*)");

        private final String regex;

        Tag(String regex) {
            this.regex = regex;
        }
    }

    private static final Logger LOGGER = getLogger(MemcachedUIService.class);

    private final String keyStructure;
    private final String keyStructureRegex;
    private final Map<Tag, Integer> tagsOrder = new HashMap<>();


    public KeyMapper(@Value("${memcached-ui.key.structure}") String keyStructure) {
        if (initIndexing(keyStructure)) {
            this.keyStructure = keyStructure;
        } else {
            this.keyStructure = Tag.KEY.name();
        }
        keyStructureRegex = getKeyStructureRegex(keyStructure);
    }

    private boolean initIndexing(String keyStructure) {
        Map<Integer, Tag> tagsIndices = new HashMap<>();
        for (Tag tag: Tag.values()) {
            int index = keyStructure.indexOf(tag.name());
            int lastIndex = keyStructure.lastIndexOf(tag.name());
            if (index != lastIndex) {
                LOGGER.warn("Tag \"" + tag.name() + "\" is present multiple times.");
                tagsIndices.clear();
                break;
            }
            if (index >= 0) {
                tagsIndices.put(index, tag);
            }
        }
        if (tagsIndices.isEmpty()) {
            return false;
        }
        List<Integer> sortedIndices = new ArrayList<>(tagsIndices.keySet());
        Collections.sort(sortedIndices);
        var iterator = sortedIndices.iterator();
        int place = 1;
        while (iterator.hasNext()) {
            tagsOrder.put(tagsIndices.get(iterator.next()), place++);
        }
        if (!tagsOrder.containsKey(Tag.KEY)) {
            tagsOrder.clear();
            LOGGER.warn("\"" + Tag.KEY.name() + "\" tag is not defined");
            return false;
        }
        return true;
    }

    public String getFullKey(Key key) {
        if (key.getNamespace() == null) {
            return key.getName();
        }
        return keyStructure
                .replace(Tag.KEY.name(), key.getName())
                .replace(Tag.NAMESPACE.name(), key.getNamespace())
                .replace(Tag.TIMESTAMP.name(), Long.toString(key.getTimestamp()));
    }

    public List<Key> mapKeys(Map<String, Tuple2<Long, Long>> rawKeys) {
        List<Key> keys = new ArrayList<>();
        rawKeys.forEach((keyString, tuple) -> {
            keys.add(mapKey(keyString, tuple._1, tuple._2));
        });
        return keys;
    }

    public Key mapKey(String keyString, Long bytes, Long timestamp) {
        Matcher matcher = Pattern.compile(keyStructureRegex)
                .matcher(keyString);
        Key.Builder builder = new Key.Builder(keyString)
                .setMemSize(bytes)
                .setTimestamp(timestamp);
        if (matcher.find() && !tagsOrder.isEmpty()) {
            String key = matcher.group(tagsOrder.get(Tag.KEY));
            builder.setName(key);
            parseKeyString(builder, matcher);
            return builder.build();
        } else {
            LOGGER.warn("Key \"" + keyString + "\" does not match custom key structure");
        }
        return builder.build();
    }

    private void parseKeyString(Key.Builder builder, Matcher matcher) {
        tagsOrder.keySet()
                .forEach(tag -> {
                    String value = matcher.group(tagsOrder.get(tag));
                    switch(tag) {
                        case NAMESPACE:
                            builder.setNamespace(value);
                            break;
                        case TIMESTAMP:
                            builder.setTimestamp(Long.parseLong(value));
                            break;
                        default:
                    }
                });
    }

    private String getKeyStructureRegex(String keyStructure) {
        String regex = "^" + keyStructure
                .replaceAll("\\(", "\\\\(")
                .replaceAll("\\)", "\\\\)");
        for (Tag tag: Tag.values()) {
            regex = regex.replaceAll(tag.name(), tag.regex);
        }
        return regex;
    }
}
