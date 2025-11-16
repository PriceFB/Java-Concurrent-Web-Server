package edu.fiu.cop6727.webserver;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Locale;

/**
 * Represents an HTTP response ready to be serialized on a socket.
 */
public final class HttpResponse {
    private static final DateTimeFormatter HTTP_DATE =
            DateTimeFormatter.RFC_1123_DATE_TIME;

    private HttpStatus status;
    private final HttpHeaders headers = new HttpHeaders();
    private byte[] body = new byte[0];

    public HttpResponse(HttpStatus status) {
        this.status = Objects.requireNonNull(status, "status");
        headers.set("server", "COP6727-ConcurrentServer/1.0");
        headers.set("connection", "close");
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public void setHeader(String name, String value) {
        headers.set(name, value);
    }

    public void setBody(byte[] data) {
        body = data == null ? new byte[0] : data;
        headers.set("content-length", String.valueOf(body.length));
    }

    public void setBodyText(String text, String contentType) {
        byte[] data = text == null ? new byte[0] : text.getBytes(StandardCharsets.UTF_8);
        setBody(data);
        setHeader("content-type", contentType + "; charset=utf-8");
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void write(OutputStream outputStream) throws IOException {
        headers.set("date", HTTP_DATE.format(ZonedDateTime.now()));
        if (!headers.contains("content-length")) {
            headers.set("content-length", String.valueOf(body.length));
        }
        StringBuilder builder = new StringBuilder();
        builder.append("HTTP/1.1 ")
                .append(status.code())
                .append(' ')
                .append(status.reason())
                .append("\r\n");
        headers.asMap().forEach((name, value) ->
                builder.append(capitalizeHeaderName(name))
                        .append(':')
                        .append(' ')
                        .append(value)
                        .append("\r\n"));
        builder.append("\r\n");
        outputStream.write(builder.toString().getBytes(StandardCharsets.US_ASCII));
        outputStream.write(body);
        outputStream.flush();
    }

    private String capitalizeHeaderName(String header) {
        String[] parts = header.split("-");
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                formatted.append('-');
            }
            if (parts[i].isEmpty()) {
                continue;
            }
            formatted.append(Character.toUpperCase(parts[i].charAt(0)));
            if (parts[i].length() > 1) {
                formatted.append(parts[i].substring(1).toLowerCase(Locale.ROOT));
            }
        }
        return formatted.toString();
    }

    public static HttpResponse text(HttpStatus status, String body) {
        HttpResponse response = new HttpResponse(status);
        response.setBodyText(body, "text/plain");
        return response;
    }
}
