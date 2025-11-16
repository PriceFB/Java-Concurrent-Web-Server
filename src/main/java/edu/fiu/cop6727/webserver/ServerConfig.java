package edu.fiu.cop6727.webserver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Holds runtime configuration such as port and thread pool size.
 */
public final class ServerConfig {
    private final int port;
    private final Path documentRoot;
    private final int maxThreads;
    private final int socketTimeoutMillis;

    public ServerConfig(int port, Path documentRoot, int maxThreads, int socketTimeoutMillis) {
        this.port = port;
        this.documentRoot = documentRoot.toAbsolutePath().normalize();
        this.maxThreads = maxThreads;
        this.socketTimeoutMillis = socketTimeoutMillis;
    }

    public int getPort() {
        return port;
    }

    public Path getDocumentRoot() {
        return documentRoot;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public int getSocketTimeoutMillis() {
        return socketTimeoutMillis;
    }

    public static ServerConfig fromArgs(String[] args) {
        Map<String, String> cliOptions = parseArgs(args);

        Properties properties = new Properties();
        if (cliOptions.containsKey("config")) {
            Path configPath = Paths.get(cliOptions.get("config"));
            try (InputStream in = Files.newInputStream(configPath)) {
                properties.load(in);
            } catch (IOException e) {
                throw new IllegalArgumentException("Unable to read config file: " + configPath, e);
            }
        } else {
            Path defaultConfig = Paths.get("src", "main", "resources", "server.properties");
            if (Files.exists(defaultConfig)) {
                try (InputStream in = Files.newInputStream(defaultConfig)) {
                    properties.load(in);
                } catch (IOException ignored) {
                    // Fall back to defaults if we cannot read optional file.
                }
            }
        }

        int defaultThreads = Math.max(4, Runtime.getRuntime().availableProcessors() * 2);
        int port = parseInt(cliOptions.getOrDefault("port", properties.getProperty("server.port", "8080")), 8080);
        Path documentRoot = Paths.get(cliOptions.getOrDefault("docRoot",
                properties.getProperty("server.documentRoot", "wwwroot")));
        int maxThreads = parseInt(cliOptions.getOrDefault("threads",
                properties.getProperty("server.maxThreads", String.valueOf(defaultThreads))), defaultThreads);
        int socketTimeout = parseInt(cliOptions.getOrDefault("socketTimeout",
                properties.getProperty("server.socketTimeoutMillis", "15000")), 15000);

        if (!Files.exists(documentRoot)) {
            throw new IllegalArgumentException("Document root does not exist: " + documentRoot);
        }

        return new ServerConfig(port, documentRoot, maxThreads, socketTimeout);
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> parsed = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (!arg.startsWith("--")) {
                continue;
            }
            String key;
            String value;
            int equals = arg.indexOf('=');
            if (equals > 2) {
                key = arg.substring(2, equals);
                value = arg.substring(equals + 1);
            } else {
                key = arg.substring(2);
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException("Missing value for argument: " + arg);
                }
                value = args[++i];
            }
            parsed.put(key, value);
        }
        return parsed;
    }

    private static int parseInt(String rawValue, int fallback) {
        try {
            return Integer.parseInt(rawValue);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
