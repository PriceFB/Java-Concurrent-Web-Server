# Implementation Report Outline

## 1. Introduction
- Restate project goals and tie them to concurrency in database systems.
- Briefly summarize chosen architecture (listener + worker pool) and supporting components.

## 2. Code Structure
- HttpServer: Initializes server socket, worker pool, and graceful shutdown logic.
- ClientHandler: Per-connection worker parsing the HTTP request and invoking the router.
- RequestRouter: Routes to static file handler, /slow, /echo, and /health endpoints.
- Helper classes: HttpRequest, HttpResponse, HttpHeaders, HttpStatus, ServerConfig, ServerLogger, MimeTypes, BadRequestException.
- Directory layout: src/main/java for code, wwwroot for static assets, docs for reports, demo for scripts.

## 3. Concurrency Model Details
- Listener thread accepts on ServerSocket and dispatches to ExecutorService (fixed thread pool).
- Justify sizing strategy (2x CPU cores default, configurable via CLI/properties).
- Discuss blocking I/O assumption and how dedicated threads keep throughput high despite blocking operations.
- Describe worker lifecycle, per-connection timeouts, and how ClientHandler avoids shared mutable state.

## 4. HTTP Request Handling
- Parsing strategy: read raw bytes until \r\n\r\n, validate headers, enforce payload limits, support Content-Length for POST.
- Response generation: HttpResponse centralizes header formatting, Content-Length, and body serialization.
- Static file serving: path normalization, MIME type detection, directory traversal protection.

## 5. Error Handling & Logging
- BadRequestException for malformed requests -> 400 Bad Request.
- Generic exceptions -> 500 Internal Server Error responses and console logging.
- Logger includes timestamp + thread name to illustrate concurrency in logs.

## 6. Configuration & Deployment
- CLI flags (--port, --docRoot, --threads, --socketTimeout, --config).
- Optional server.properties for repeatable deployments.
- Instructions for compilation (javac) and execution.

## 7. Challenges & Lessons Learned
- Examples: ensuring thread-safe logging, handling partial reads, preventing directory traversal, balancing pool size vs. blocking work.
- Future improvements: asynchronous NIO, HTTPS, HTTP/1.1 keep-alive, observability hooks.

## 8. Conclusion
- Summarize how the implementation demonstrates concurrent request handling aligned with DBMS concepts.

*Copy this outline into a .docx template for the final submission.*
