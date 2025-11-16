package edu.fiu.cop6727.webserver;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable HTTP request data holder.
 */
public final class HttpRequest {
    private final String method;
    private final String path;
    private final String httpVersion;
    private final HttpHeaders headers;
    private final byte[] body;

    public HttpRequest(String method, String path, String httpVersion, HttpHeaders headers, byte[] body) {
        this.method = Objects.requireNonNull(method, "method");
        this.path = Objects.requireNonNull(path, "path");
        this.httpVersion = Objects.requireNonNull(httpVersion, "httpVersion");
        this.headers = Objects.requireNonNull(headers, "headers");
        this.body = body == null ? new byte[0] : body;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return Arrays.copyOf(body, body.length);
    }

    public boolean hasBody() {
        return body.length > 0;
    }
}
