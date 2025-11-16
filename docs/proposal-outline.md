# Project Proposal Outline

## 1. Problem Statement
- Traditional sequential web servers cannot sustain throughput once multiple clients attempt to access shared resources simultaneously.
- The course context (database systems) emphasizes server-side concurrency, worker pools, and resource governance; this project demonstrates those principles in an HTTP setting.

## 2. Proposed Solution
- Implement a blocking-I/O HTTP/1.1 server with a dedicated listener thread and a fixed-size worker pool.
- Each accepted connection is processed by a ClientHandler runnable that parses the request, routes it to the proper handler, and returns a response.
- Static file serving plus custom endpoints (/slow, /echo, /health) showcase correctness and concurrency.

## 3. Technology Choices
- **Language**: Java 17 for mature threading primitives (Executors), robust I/O libraries, and straightforward deployment.
- **Build**: Plain javac compilation (no heavy framework) to keep the code focused on concurrency concepts.
- **Testing tools**: curl, browser dev tools, and JMeter/hey (optional) for load testing.

## 4. Concurrency Model Justification
- Fixed thread pool (ExecutorService) prevents unbounded thread creation and mirrors DBMS worker pool designs.
- Blocking sockets maintain simplicity while still allowing simultaneous processing thanks to dedicated threads per connection.

## 5. Milestones & Timeline
1. Week 1: finalize architecture, set up skeleton project, implement HttpServer + Config.
2. Week 2: implement request parsing, router, and static file serving.
3. Week 3: add demo endpoints, logging, and properties/config support.
4. Week 4: testing (functional + concurrency), polish docs, record demo video.

## 6. Evaluation Plan
- Functional tests: curl/browser requests for GET/POST/static assets.
- Concurrency tests: parallel /slow requests and observation of overlapping logs/response times.
- Stress tests: optional load testing (e.g., ApacheBench) to validate thread pool sizing.

*Convert this outline to .docx for submission using your preferred editor.*
