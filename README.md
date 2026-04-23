# Smart Campus Sensor & Room Management API

A RESTful API built with **JAX-RS (Jersey)** and deployed on **Apache Tomcat**, fulfilling the 5COSC022W coursework specification.

---

## API Overview

This API manages campus **Rooms** and **Sensors** for the University's Smart Campus initiative.  
Base URL: `http://localhost:8080/SmartCampusAPI/api/v1`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Discovery — API metadata & resource links |
| GET | `/rooms` | List all rooms |
| POST | `/rooms` | Create a new room |
| GET | `/rooms/{roomId}` | Get a single room |
| DELETE | `/rooms/{roomId}` | Delete a room (blocked if sensors assigned) |
| GET | `/sensors` | List all sensors (optional `?type=` filter) |
| POST | `/sensors` | Register a new sensor |
| GET | `/sensors/{sensorId}` | Get a single sensor |
| DELETE | `/sensors/{sensorId}` | Delete a sensor |
| GET | `/sensors/{sensorId}/readings` | Get reading history for a sensor |
| POST | `/sensors/{sensorId}/readings` | Add a new reading for a sensor |

---

## How to Build & Run (NetBeans + Tomcat)

### Prerequisites
- **NetBeans IDE** (17 or later recommended)
- **Apache Tomcat 9.x** configured in NetBeans
- **JDK 11** or later
- **Maven** (bundled with NetBeans)

### Step-by-Step Setup in NetBeans

1. **Clone or copy the project** into a local folder (e.g., `C:/Projects/SmartCampusAPI`).

2. **Open NetBeans** → `File` → `Open Project` → navigate to the project folder → click **Open Project**.  
   NetBeans will detect it as a Maven project automatically.

3. **Add Tomcat to NetBeans** (if not already done):
   - Go to `Services` tab (bottom-left panel) → right-click `Servers` → `Add Server`
   - Select **Apache Tomcat or TomEE** → click Next
   - Browse to your Tomcat installation folder (e.g., `C:/apache-tomcat-9.0.x`) → Finish

4. **Set Tomcat as the project server**:
   - Right-click the project in the Projects panel → `Properties`
   - Go to `Run` category → set **Server** to your Tomcat instance
   - Set **Context Path** to `/SmartCampusAPI`

5. **Build the project**:
   - Right-click the project → `Build` (or press `F11`)
   - Maven will download all dependencies automatically on first build.

6. **Run the project**:
   - Right-click the project → `Run` (or press `F6`)
   - NetBeans will deploy the WAR to Tomcat and open a browser.

7. **Verify** the server is running by visiting:
   ```
   http://localhost:8080/SmartCampusAPI/api/v1
   ```
   You should see a JSON discovery response.

---

## Sample curl Commands

### 1. Discovery Endpoint
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1
```

### 2. List All Rooms
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms
```

### 3. Create a New Room
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"HALL-202","name":"Main Hall","capacity":200}'
```

### 4. Get a Specific Room
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms/LIB-301
```

### 5. Delete a Room (will fail if sensors are assigned — returns 409)
```bash
curl -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/HALL-202
```

### 6. Register a New Sensor
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-002","type":"Temperature","status":"ACTIVE","currentValue":21.0,"roomId":"HALL-202"}'
```

### 7. List Sensors Filtered by Type
```bash
curl -X GET "http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=CO2"
```

### 8. Post a Sensor Reading
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":23.7}'
```

### 9. Get Reading History for a Sensor
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001/readings
```

### 10. Attempt Reading on a MAINTENANCE Sensor (returns 403)
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/OCC-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":5.0}'
```

---

## Conceptual Report — Question Answers

### Part 1 — Service Architecture & Setup

**Q: Explain the default lifecycle of a JAX-RS Resource class. Is a new instance created per request or is it a singleton? How does this affect in-memory data management?**

By default, JAX-RS follows a **per-request lifecycle**: the runtime creates a fresh instance of each resource class for every incoming HTTP request, and discards it when the response is sent. This means resource class fields are not shared between requests — they are re-initialised on every call.

This has a critical implication for in-memory data management: if data (such as a list of rooms) were stored as an instance field on the resource class, it would be lost after every request. To maintain state across requests, all shared data must be stored **outside** the resource class in a **singleton** — in this project, the `DataStore` class holds a single static instance backed by `ConcurrentHashMap`. Using `ConcurrentHashMap` instead of `HashMap` is essential because multiple requests can arrive simultaneously (multi-threaded servlet environment), and a plain `HashMap` is not thread-safe, risking data corruption or race conditions.

---

**Q: Why is HATEOAS considered a hallmark of advanced RESTful design? How does it benefit client developers?**

HATEOAS (Hypermedia As The Engine Of Application State) means the API embeds **navigational links** inside its responses, telling clients what actions are available and where to find related resources — rather than requiring clients to hardcode URL patterns.

For example, a room response might include `"sensorsLink": "/api/v1/rooms/LIB-301/sensors"` rather than forcing the client to construct that URL from documentation. This benefits developers in several ways: clients are **decoupled from the URL structure** (URLs can change without breaking clients), the API becomes **self-documenting** at runtime, and it reduces the risk of clients constructing incorrect URLs. It also enables discoverability — a client that only knows the root endpoint can navigate the entire API by following links, just as a browser navigates the web.

---

### Part 2 — Room Management

**Q: What are the implications of returning only IDs versus full room objects in a list response?**

Returning **only IDs** minimises payload size — beneficial when there are thousands of rooms and the client only needs to display a list of names and then fetch details on demand. However, it requires the client to make N additional HTTP requests (one per room) to retrieve any meaningful data, leading to the "N+1 problem" and higher total latency.

Returning **full room objects** increases the payload size but allows the client to render a complete list in a single round-trip, reducing latency and client-side complexity. For most use cases — especially paginated lists — returning full objects is preferred. A compromise is to return a "summary" object (id + name only), reserving the full detail for the individual `GET /rooms/{id}` endpoint.

---

**Q: Is the DELETE operation idempotent in your implementation? Justify with what happens on repeated DELETE calls.**

Strictly speaking, the implementation is **not fully idempotent** in the classic HTTP sense. The first `DELETE /rooms/{roomId}` succeeds and returns `200 OK`. A second identical request — for a room that no longer exists — returns `404 Not Found`. The server state is identical after both calls (the room is absent either way), so the **side effect is idempotent**. However, because the HTTP status code differs, the **response is not identical**, which some definitions consider a violation of idempotency. A stricter idempotent implementation would return `200 OK` or `204 No Content` on all subsequent DELETE calls for the same resource, regardless of whether it still exists.

---

### Part 3 — Sensor Operations & Filtering

**Q: What happens if a client sends data in a format other than `application/json` to a method annotated with `@Consumes(MediaType.APPLICATION_JSON)`?**

JAX-RS checks the `Content-Type` header of the incoming request against the `@Consumes` annotation before invoking the method. If a client sends `text/plain` or `application/xml`, the runtime finds no matching resource method and automatically returns **HTTP 415 Unsupported Media Type**. The resource method is never invoked. This is handled entirely by the JAX-RS framework without any manual code — it is part of the content negotiation mechanism built into the specification.

---

**Q: Why is `@QueryParam` preferred over path-based filtering (e.g., `/sensors/type/CO2`) for searching and filtering collections?**

Query parameters (`?type=CO2`) are semantically correct for **filtering an existing collection** — they modify *how* the collection is returned without implying a different resource hierarchy. Path segments (`/sensors/type/CO2`) imply a distinct, addressable resource at that path, which is misleading and pollutes the URL space.

Query parameters are also more flexible: multiple filters can be combined easily (`?type=CO2&status=ACTIVE`), optional filters require no path changes, and search parameters are inherently transient (not bookmarkable as canonical resource identifiers). Path parameters should be reserved for identifying specific resources (e.g., `/sensors/TEMP-001`), not for narrowing collection results.

---

### Part 4 — Deep Nesting with Sub-Resources

**Q: Discuss the architectural benefits of the Sub-Resource Locator pattern.**

The Sub-Resource Locator pattern allows a resource class to **delegate handling of a nested path** to a separate, dedicated class. In this project, `SensorResource` handles `/sensors` and `/sensors/{id}`, while `SensorReadingResource` exclusively handles `/sensors/{id}/readings`. The locator method `getReadingsResource()` is the bridge.

This approach has several benefits: it enforces the **Single Responsibility Principle** — each class manages one resource's logic only. It dramatically improves **maintainability** as the API grows; adding new sub-resources does not bloat the parent class. It enables **independent testing** of each resource class. Contrast this with a monolithic approach where one class handles `/sensors`, `/sensors/{id}`, `/sensors/{id}/readings`, and `/sensors/{id}/readings/{rid}` — the class would quickly become hundreds of lines of unrelated logic, making it brittle and hard to navigate.

---

### Part 5 — Advanced Error Handling, Exception Mapping & Logging

**Q: Why is HTTP 422 more semantically accurate than 404 when a referenced resource (e.g., roomId) is missing inside a valid JSON payload?**

`404 Not Found` specifically means the **requested URL endpoint** was not found on the server. When a client `POST`s to `/api/v1/sensors`, the endpoint itself exists and is reachable — so 404 is factually incorrect and misleading.

`422 Unprocessable Entity` means the server understood the request syntax and found the endpoint, but **could not process the payload** because it contains a semantic error — in this case, a `roomId` that references a non-existent resource. This is a data integrity validation failure, not a routing failure. Using 422 gives client developers a precise signal: "your request was well-formed but the content was logically invalid," which enables better error handling and debugging on the client side.

---

**Q: From a cybersecurity standpoint, explain the risks of exposing Java stack traces to external API consumers.**

Stack traces expose sensitive internal information that attackers can exploit for reconnaissance:

- **Class and package names** reveal the internal architecture and framework versions (e.g., `org.glassfish.jersey`, `com.smartcampus`), helping attackers identify the technology stack.
- **Library versions** allow attackers to cross-reference known CVEs (Common Vulnerabilities and Exposures) for that exact version.
- **Method signatures and line numbers** reveal code structure, making it easier to reverse-engineer logic and find injection points.
- **Database or file paths** (from I/O exceptions) can expose server directory structures.
- **NullPointerException traces** can hint at data validation gaps that attackers can deliberately trigger to destabilise the service.

The Global Exception Mapper prevents all of this by intercepting every unhandled error, logging the full detail server-side (where only administrators can see it), and returning only a generic `500` message to the client.

---

**Q: Why is it better to use JAX-RS filters for cross-cutting concerns like logging, rather than adding Logger statements inside every resource method?**

Filters implement the **Separation of Concerns** principle. Logging is a cross-cutting concern — it applies to every endpoint — and has nothing to do with the business logic of room or sensor management. Embedding `Logger.info()` calls inside every resource method:

- **Violates DRY**: the same boilerplate is repeated across dozens of methods.
- **Couples concerns**: business logic is tangled with infrastructure concerns.
- **Is error-prone**: new endpoints may be added without remembering to add logging.
- **Reduces readability**: resource methods should express domain logic only.

A single `LoggingFilter` class automatically intercepts every request and response in the application with zero modifications to resource classes. It can be enabled, disabled, or modified in one place. This is the same philosophy behind Spring AOP, servlet filters, and middleware in modern frameworks — infrastructure logic belongs in dedicated layers, not in business code.
