package edu.fiu.cop6727.webserver;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Lightweight case-insensitive header map.
 */
public final class HttpHeaders {
    private final Map<String, String> headers = new LinkedHashMap<>();

    public void set(String name, String value) {
        headers.put(normalize(name), value.trim());
    }

    public String get(String name) {
        return headers.get(normalize(name));
    }

    public Optional<String> find(String name) {
        return Optional.ofNullable(get(name));
    }

    public boolean contains(String name) {
        return headers.containsKey(normalize(name));
    }

    public Map<String, String> asMap() {
        return Collections.unmodifiableMap(headers);
    }

    public int size() {
        return headers.size();
    }

    private String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
