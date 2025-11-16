package edu.fiu.cop6727.webserver;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Runnable that manages a single client connection lifecycle.
 */
public final class ClientHandler implements Runnable {
    private static final int MAX_HEADER_BYTES = 16 * 1024; // 16KB
    private static final int MAX_BODY_BYTES = 1 * 1024 * 1024; // 1MB

    private final Socket socket;
    private final RequestRouter router;
    private final ServerLogger logger;
    private final int socketTimeoutMillis;

    public ClientHandler(Socket socket, RequestRouter router, ServerLogger logger, int socketTimeoutMillis) {
        this.socket = socket;
        this.router = router;
        this.logger = logger;
        this.socketTimeoutMillis = socketTimeoutMillis;
    }

    @Override
    public void run() {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            socket.setSoTimeout(socketTimeoutMillis);
            inputStream = new BufferedInputStream(socket.getInputStream());
            outputStream = socket.getOutputStream();

            HttpRequest request = parseRequest(inputStream);
            HttpResponse response = router.route(request);
            response.write(outputStream);
            logger.info(request.getMethod() + " " + request.getPath() + " -> " + response.getStatus());
        } catch (BadRequestException e) {
            logger.warn("Bad request: " + e.getMessage());
            respondWithError(HttpStatus.BAD_REQUEST, e.getMessage(), outputStream);
        } catch (IOException e) {
            logger.error("I/O error handling client", e);
        } finally {
            closeQuietly(inputStream);
            closeQuietly(outputStream);
            closeQuietly(socket);
        }
    }

    private void respondWithError(HttpStatus status, String message, OutputStream outputStream) {
        if (outputStream == null) {
            return;
        }
        try {
            HttpResponse response = HttpResponse.text(status, message == null ? status.reason() : message);
            response.write(outputStream);
        } catch (IOException ioException) {
            logger.error("Failed to send error response", ioException);
        }
    }

    private HttpRequest parseRequest(InputStream inputStream) throws IOException, BadRequestException {
        byte[] headerBytes = readHeaders(inputStream);
        String headerText = new String(headerBytes, StandardCharsets.US_ASCII);
        String[] lines = headerText.split("\r\n");
        if (lines.length == 0 || lines[0].isBlank()) {
            throw new BadRequestException("Empty request");
        }
        String[] parts = lines[0].split(" ");
        if (parts.length < 3) {
            throw new BadRequestException("Malformed request line");
        }
        String method = parts[0].trim();
        String path = parts[1].trim();
        String version = parts[2].trim();

        HttpHeaders headers = new HttpHeaders();
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line == null || line.isEmpty()) {
                break;
            }
            int colonIndex = line.indexOf(':');
            if (colonIndex <= 0) {
                throw new BadRequestException("Malformed header line");
            }
            String name = line.substring(0, colonIndex).trim();
            String value = line.substring(colonIndex + 1).trim();
            headers.set(name, value);
        }

        int contentLength = 0;
        String contentLengthHeader = headers.get("content-length");
        if (contentLengthHeader != null) {
            try {
                contentLength = Integer.parseInt(contentLengthHeader);
            } catch (NumberFormatException ex) {
                throw new BadRequestException("Invalid Content-Length header");
            }
        }
        if (contentLength < 0 || contentLength > MAX_BODY_BYTES) {
            throw new BadRequestException("Payload too large");
        }

        byte[] body = new byte[contentLength];
        int totalRead = 0;
        while (totalRead < contentLength) {
            int read = inputStream.read(body, totalRead, contentLength - totalRead);
            if (read == -1) {
                throw new BadRequestException("Unexpected end of stream while reading body");
            }
            totalRead += read;
        }
        return new HttpRequest(method, path, version, headers, body);
    }

    private byte[] readHeaders(InputStream inputStream) throws IOException, BadRequestException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int matched = 0;
        while (true) {
            int value = inputStream.read();
            if (value == -1) {
                throw new BadRequestException("Unexpected end of stream");
            }
            buffer.write(value);
            if (buffer.size() > MAX_HEADER_BYTES) {
                throw new BadRequestException("Headers too large");
            }
            switch (matched) {
                case 0 -> matched = value == '\r' ? 1 : 0;
                case 1 -> matched = value == '\n' ? 2 : (value == '\r' ? 1 : 0);
                case 2 -> matched = value == '\r' ? 3 : 0;
                case 3 -> {
                    if (value == '\n') {
                        byte[] headerBytes = buffer.toByteArray();
                        // Remove the trailing CRLFCRLF sequence.
                        if (headerBytes.length >= 4) {
                            byte[] trimmed = new byte[headerBytes.length - 4];
                            System.arraycopy(headerBytes, 0, trimmed, 0, trimmed.length);
                            return trimmed;
                        }
                        return headerBytes;
                    }
                    matched = value == '\r' ? 1 : 0;
                }
                default -> matched = 0;
            }
        }
    }

    private void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignored) {
        }
    }
}
