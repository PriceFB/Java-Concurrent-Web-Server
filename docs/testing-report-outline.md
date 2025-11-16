# Testing Report Outline

## 1. Testing Strategy Overview
- Goals: validate correctness of HTTP parsing, ensure static assets load, prove concurrent behavior via overlapping requests, and document performance observations.
- Tools: browser, curl, PowerShell, optional load generators (ApacheBench, hey, JMeter).

## 2. Environment Setup
- Hardware/software details (Windows version, Java version, network assumptions).
- Server configuration used during testing (port, thread count, doc root).

## 3. Functional Test Cases
| ID | Scenario | Steps | Expected Result |
|----|----------|-------|-----------------|
| F1 | GET / | Browser or curl request | 200 OK, HTML content, CSS loaded |
| F2 | GET /slow | Single request | Response after ~3 seconds |
| F3 | POST /echo | curl -X POST http://localhost:8080/echo -d "Hello" | Body echoed back |
| F4 | 404 handling | Request /missing.html | 404 Not Found text |
| F5 | Directory traversal | Request /../server.properties | 403 Forbidden |

## 4. Concurrency Tests
- **Parallel slow requests**: launch two or more /slow requests concurrently using PowerShell background jobs; record timestamps showing overlapping execution.
- **Thread pool saturation**: configure a small pool (e.g., --threads 2), fire 5 /slow requests, observe queueing behavior and completion order.
- Capture console logs demonstrating multiple thread names handling distinct clients simultaneously.

## 5. Performance/Load Snapshots (Optional but recommended)
- Run ab -n 100 -c 10 http://localhost:8080/ or hey -n 200 -c 20 http://localhost:8080/slow.
- Record throughput, mean latency, and CPU utilization observations.

## 6. Defect Tracking
- Document any issues discovered, root causes, fixes, and regression tests performed.

## 7. Test Evidence for Submission
- Screenshots of terminal output, curl sessions, and browser tabs.
- Include excerpt of server logs showing concurrent thread processing for insertion into the .docx report and final video narration.

*Use this outline as the backbone for the .docx testing report.*
