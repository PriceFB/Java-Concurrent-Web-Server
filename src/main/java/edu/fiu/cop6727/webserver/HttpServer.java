package edu.fiu.cop6727.webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main server entry point. Listens for incoming connections and hands them to worker threads.
 */
public final class HttpServer {
    private final ServerConfig config;
    private final ServerLogger logger;
    private final RequestRouter router;
    private final ExecutorService workerPool;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private ServerSocket serverSocket;

    public HttpServer(ServerConfig config, ServerLogger logger) {
        this.config = config;
        this.logger = logger;
        this.router = new RequestRouter(config.getDocumentRoot(), logger);
        this.workerPool = Executors.newFixedThreadPool(config.getMaxThreads());
    }

    public void start() throws IOException {
        ensureDocumentRoot();
        serverSocket = new ServerSocket(config.getPort());
        serverSocket.setReuseAddress(true);
        running.set(true);
        logger.info("Server listening on port " + config.getPort());

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

        while (running.get()) {
            try {
                Socket clientSocket = serverSocket.accept();
                workerPool.submit(new ClientHandler(clientSocket, router, logger, config.getSocketTimeoutMillis()));
            } catch (SocketException e) {
                if (running.get()) {
                    logger.error("Socket exception in accept loop", e);
                }
                break;
            }
        }
    }

    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }
        logger.info("Shutting down server");
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.error("Error closing server socket", e);
        }
        workerPool.shutdown();
        try {
            if (!workerPool.awaitTermination(5, TimeUnit.SECONDS)) {
                workerPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            workerPool.shutdownNow();
        }
    }

    private void ensureDocumentRoot() throws IOException {
        if (!Files.isDirectory(config.getDocumentRoot())) {
            throw new IOException("Document root does not exist: " + config.getDocumentRoot());
        }
    }

    public static void main(String[] args) {
        ServerLogger logger = new ServerLogger();
        try {
            ServerConfig config = ServerConfig.fromArgs(args);
            HttpServer server = new HttpServer(config, logger);
            server.start();
        } catch (Exception e) {
            logger.error("Failed to start server", e);
        }
    }
}
