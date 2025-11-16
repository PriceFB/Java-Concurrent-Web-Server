package edu.fiu.cop6727.webserver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

/**
 * Routes requests to handlers (static files, echo, slow, etc.).
 */
public final class RequestRouter {
    private static final Set<String> SUPPORTED_METHODS = Set.of("GET", "POST");

    private final Path documentRoot;
    private final ServerLogger logger;

    public RequestRouter(Path documentRoot, ServerLogger logger) {
        this.documentRoot = documentRoot.toAbsolutePath().normalize();
        this.logger = logger;
    }

    public HttpResponse route(HttpRequest request) {
        if (!SUPPORTED_METHODS.contains(request.getMethod())) {
            HttpResponse response = HttpResponse.text(HttpStatus.METHOD_NOT_ALLOWED,
                    "Method not allowed");
            response.setHeader("allow", String.join(", ", SUPPORTED_METHODS));
            return response;
        }

        try {
            return switch (request.getMethod()) {
                case "GET" -> handleGet(request);
                case "POST" -> handlePost(request);
                default -> HttpResponse.text(HttpStatus.NOT_IMPLEMENTED, "Not implemented");
            };
        } catch (IOException e) {
            logger.error("I/O error routing request", e);
            return HttpResponse.text(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return HttpResponse.text(HttpStatus.INTERNAL_SERVER_ERROR, "Server interrupted");
        }
    }

    private HttpResponse handleGet(HttpRequest request) throws IOException, InterruptedException {
        String path = stripQuery(request.getPath());
        if ("/slow".equals(path)) {
            return handleSlow();
        }
        if ("/health".equals(path)) {
            return HttpResponse.text(HttpStatus.OK, "OK");
        }
        if ("/".equals(path)) {
            return serveStaticFile("index.html");
        }
        return serveStaticFile(path.startsWith("/") ? path.substring(1) : path);
    }

    private HttpResponse handlePost(HttpRequest request) {
        String path = stripQuery(request.getPath());
        if ("/echo".equals(path)) {
            String payload = request.hasBody()
                    ? new String(request.getBody(), StandardCharsets.UTF_8)
                    : "";
            HttpResponse response = new HttpResponse(HttpStatus.OK);
            response.setBodyText("Echo: " + payload, "text/plain");
            return response;
        }
        return HttpResponse.text(HttpStatus.NOT_FOUND, "POST endpoint not found");
    }

    private HttpResponse handleSlow() throws InterruptedException {
        Thread.sleep(3000);
        HttpResponse response = new HttpResponse(HttpStatus.OK);
        response.setBodyText("Slow endpoint completed after 3 seconds", "text/plain");
        return response;
    }

    private HttpResponse serveStaticFile(String relativePath) throws IOException {
        Path sanitized = sanitizePath(relativePath);
        if (sanitized == null) {
            return HttpResponse.text(HttpStatus.FORBIDDEN, "Invalid path");
        }
        Path absolutePath = documentRoot.resolve(sanitized).normalize();
        if (!absolutePath.startsWith(documentRoot) || Files.isDirectory(absolutePath)) {
            return HttpResponse.text(HttpStatus.NOT_FOUND, "File not found");
        }
        if (!Files.exists(absolutePath)) {
            return HttpResponse.text(HttpStatus.NOT_FOUND, "File not found");
        }
        byte[] content = Files.readAllBytes(absolutePath);
        HttpResponse response = new HttpResponse(HttpStatus.OK);
        response.setHeader("content-type", MimeTypes.probe(absolutePath.toString()));
        response.setBody(content);
        return response;
    }

    private Path sanitizePath(String rawPath) {
        String path = stripQuery(rawPath);
        path = path.replace('\\', '/');
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.contains("..")) {
            return null;
        }
        if (path.isBlank()) {
            path = "index.html";
        }
        return Paths.get(path);
    }

    private String stripQuery(String path) {
        int queryIndex = path.indexOf('?');
        return queryIndex >= 0 ? path.substring(0, queryIndex) : path;
    }
}
