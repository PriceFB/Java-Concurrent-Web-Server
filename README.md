# COP6727 Concurrent Web Server

A blocking I/O HTTP/1.1 server written in Java for the COP6727 Advanced Database Systems project. The server uses a classic listener + worker thread model with a fixed-size thread pool and demonstrates concurrency by serving static assets alongside special endpoints such as /slow and /echo.

## Features
- Listens on a configurable port and document root (defaults: 8080, ./wwwroot).
- Uses a fixed thread pool so each accepted connection is handled by its own worker thread.
- Serves static files with accurate Content-Type headers and basic directory traversal protection.
- Implements demo endpoints:
  - GET /slow: sleeps for 3 seconds to visualize concurrency.
  - POST /echo: echoes request bodies back to the client.
  - GET /health: lightweight readiness probe returning 200 OK.
- Simple logging with timestamps and thread names for traceability.

## Building & Running
1. Ensure you have Java 17+ installed and on your PATH (java -version).
2. Compile sources (PowerShell example):
   ```powershell
   if (Test-Path out) { Remove-Item out -Recurse -Force }
   New-Item -ItemType Directory -Force -Path out | Out-Null
   javac -d out (Get-ChildItem -Recurse -Filter *.java src/main/java | ForEach-Object { $_.FullName })
   ```
3. Run the server (override port/docRoot/threads via CLI flags as needed):
   ```powershell
   java -cp out edu.fiu.cop6727.webserver.HttpServer --port 8080 --docRoot wwwroot --threads 16
   ```
4. Open a browser at http://localhost:8080 or fire concurrent curl requests:
   ```bash
   curl http://localhost:8080/slow &
   curl http://localhost:8080/slow &
   ```
   You should see both slow requests complete in roughly the same time, illustrating concurrency.

## Configuration Options
| CLI Flag | Description | Default |
|----------|-------------|---------|
| --port | TCP port to bind | 8080 |
| --docRoot | Static file root directory | wwwroot |
| --threads | Number of worker threads | 2 * CPU cores |
| --socketTimeout | Per-connection read timeout in ms | 15000 |
| --config | Path to a .properties file with the above keys (e.g., server.properties) | optional |

## Demo Endpoints
- GET / - Homepage describing the project
- GET /slow - Sleeps 3 seconds then responds
- GET /health - Used for readiness checks
- POST /echo - Echoes request body text
- Static assets in wwwroot/ (HTML/CSS/JS) served via path translation

## Testing Tips
1. **Concurrency**: Run multiple /slow curl calls simultaneously; the timestamps in the console logs should show overlapping processing.
2. **Static Files**: Add your own HTML/CSS under wwwroot/ and hit them via the browser.
3. **Echo Endpoint**: Use the wwwroot/echo.html form or curl -X POST http://localhost:8080/echo -d "hello".