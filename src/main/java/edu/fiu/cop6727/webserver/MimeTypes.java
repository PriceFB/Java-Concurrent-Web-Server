package edu.fiu.cop6727.webserver;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Very small mapping of file extensions to MIME types.
 */
public final class MimeTypes {
    private static final Map<String, String> TYPES = new HashMap<>();

    static {
        TYPES.put("html", "text/html");
        TYPES.put("htm", "text/html");
        TYPES.put("css", "text/css");
        TYPES.put("js", "application/javascript");
        TYPES.put("json", "application/json");
        TYPES.put("txt", "text/plain");
        TYPES.put("png", "image/png");
        TYPES.put("jpg", "image/jpeg");
        TYPES.put("jpeg", "image/jpeg");
        TYPES.put("gif", "image/gif");
        TYPES.put("svg", "image/svg+xml");
        TYPES.put("ico", "image/x-icon");
        TYPES.put("pdf", "application/pdf");
    }

    private MimeTypes() {
    }

    public static String probe(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == filename.length() - 1) {
            return "application/octet-stream";
        }
        String ext = filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        return TYPES.getOrDefault(ext, "application/octet-stream");
    }
}
