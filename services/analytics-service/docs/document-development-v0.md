# 📊 Analytics Service — Development Guide (V0)

---

# 📌 Objective

This document provides a **step-by-step roadmap** to implement the **Analytics Service**, starting from the current state (project created and building) up to the **first functional deployment (V0)**.

The Analytics Service is responsible for:

* Consuming playback events from Kafka
* Processing and logging user behavior
* Serving as the foundation for observability and metrics

---

# 🧠 Current State

✔ Spring Boot project created
✔ Builds successfully
✔ Kafka dependency added
✔ Basic structure ready

---

# 🎯 Target (V0 Definition)

By the end of this phase, the Analytics Service must:

* Consume events from Kafka (`video-events`)
* Process PLAY, PAUSE, STOP events
* Log structured event data
* Handle multiple concurrent messages
* Run via Docker
* Integrate with the full local infrastructure

---

# 🧱 PHASE 1 — Internal Structure Definition

## 🎯 Goal

Create a clean and scalable architecture for event processing.

## 📌 Tasks

1. Define package structure:

    * consumer
    * service
    * config
    * model (event representation)

2. Separate responsibilities:

    * Consumer → Kafka listener
    * Service → event processing logic
    * Model → event structure

3. Define naming conventions:

    * `VideoEventConsumer`
    * `EventProcessingService`

## ⏱️ Estimated Time

**1–2 hours**

---

# 📡 PHASE 2 — Kafka Consumer Configuration

## 🎯 Goal

Enable the service to connect and consume messages from Kafka.

## 📌 Tasks

1. Configure Kafka connection:

    * Bootstrap server (Docker network: `kafka:9092`)
    * Consumer group ID (e.g., `analytics-group`)

2. Configure deserialization:

    * JSON → object mapping

3. Define topic:

    * `video-events`

4. Configure consumer behavior:

    * Auto offset reset (earliest)
    * Enable auto commit (initially acceptable)

## ⚠️ Important

* Ensure configuration works inside Docker (not localhost)
* Avoid hardcoded values

## ⏱️ Estimated Time

**1–2 hours**

---

# 🧾 PHASE 3 — Event Model Definition

## 🎯 Goal

Create a structured representation of incoming events.

## 📌 Tasks

1. Define event fields:

    * event (PLAY, PAUSE, STOP)
    * videoId
    * timestamp
    * sessionId

2. Create a model class representing the event

3. Ensure compatibility with JSON messages produced by streaming-service

## ⚠️ Important

* Keep model flexible for future events (V1, V2)
* Avoid tight coupling

## ⏱️ Estimated Time

**1 hour**

---

# 🔄 PHASE 4 — Kafka Consumer Implementation

## 🎯 Goal

Implement message consumption logic.

## 📌 Tasks

1. Create Kafka listener:

    * Subscribe to `video-events`

2. Handle incoming messages:

    * Deserialize into event model
    * Validate event

3. Delegate processing to service layer

## ⚠️ Important

* Consumer should be lightweight
* Avoid business logic inside listener

## ⏱️ Estimated Time

**2–3 hours**

---

# 🧠 PHASE 5 — Event Processing Logic

## 🎯 Goal

Process and log events in a structured way.

## 📌 Tasks

1. Create processing service:

    * Handle different event types

2. Implement logic:

    * Log structured output (JSON format recommended)
    * Include metadata (timestamp, event type)

3. Prepare for future expansion:

    * Metrics aggregation
    * Session tracking

## 🧠 Example behaviors:

* Count plays per video
* Track session lifecycle (future)

## ⏱️ Estimated Time

**2–3 hours**

---

# ⚡ PHASE 6 — Concurrency & Performance

## 🎯 Goal

Ensure the service handles high event throughput.

## 📌 Tasks

1. Configure Kafka consumer concurrency:

    * Multiple threads for consumption

2. Ensure processing is non-blocking:

    * Avoid heavy synchronous operations

3. Validate throughput:

    * Simulate multiple events

## ⚠️ Important

* Do not block Kafka listener thread
* Keep processing lightweight

## ⏱️ Estimated Time

**1–2 hours**

---

# 🧪 PHASE 7 — Functional Validation

## 🎯 Goal

Validate end-to-end event flow.

## 📌 Tasks

1. Trigger events from streaming-service:

    * PLAY, PAUSE, STOP

2. Validate:

    * Messages are consumed
    * Logs are generated correctly

3. Test concurrency:

    * Multiple events in sequence

## 🧠 Expected Outcome

* All events are processed
* No message loss
* Logs are consistent

## ⏱️ Estimated Time

**1–2 hours**

---

# 🐳 PHASE 8 — Dockerization

## 🎯 Goal

Prepare the service for container execution.

## 📌 Tasks

1. Create Dockerfile
2. Build application JAR
3. Run container
4. Validate connectivity with Kafka

## ⚠️ Important

* Ensure Kafka hostname is correct (`kafka:9092`)
* Validate logs inside container

## ⏱️ Estimated Time

**1–2 hours**

---

# 🌐 PHASE 9 — Integration with Full Infrastructure

## 🎯 Goal

Run analytics-service as part of the full system.

## 📌 Tasks

1. Add service to Docker Compose

2. Ensure network connectivity

3. Validate:

    * Kafka communication
    * Event consumption

4. Monitor logs:

    * Ensure events are flowing

## ⏱️ Estimated Time

**1–2 hours**

---

# 🚀 PHASE 10 — First Deployment (V0)

## 🎯 Goal

Have the Analytics Service fully integrated and running.

## 📌 Tasks

1. Start full environment:

    * docker-compose up

2. Trigger streaming events

3. Validate:

    * Events consumed in real time
    * Logs visible in container

4. Test resilience:

    * Restart analytics-service
    * Confirm it resumes consumption

## ⏱️ Estimated Time

**1 hour**

---

# 📊 TOTAL ESTIMATED TIME

| Phase            | Time |
| ---------------- | ---- |
| Structure        | 1–2h |
| Kafka Config     | 1–2h |
| Event Model      | 1h   |
| Consumer         | 2–3h |
| Processing Logic | 2–3h |
| Performance      | 1–2h |
| Validation       | 1–2h |
| Docker           | 1–2h |
| Integration      | 1–2h |
| Deployment       | 1h   |

### ⏱️ Total: **12–20 hours**

---

# ⚠️ Common Pitfalls

* Blocking Kafka consumer thread ❌
* Ignoring deserialization errors ❌
* Hardcoding Kafka configs ❌
* Not handling unknown events ❌
* Tight coupling with streaming-service ❌

---

# 🧠 Engineering Mindset

Focus on:

* Event-driven design
* Loose coupling
* High throughput processing
* Observability (structured logs)
* Scalability

---

# 🏁 Definition of Done (V0)

✔ Consumes Kafka events
✔ Processes PLAY, PAUSE, STOP
✔ Logs structured data
✔ Handles concurrent events
✔ Runs in Docker
✔ Integrated with full system

---

# 🚀 Next Steps (After V0)

* V1 → Track playback speed events
* V2 → Real-time metrics (dashboards)
* V3 → Session analytics + aggregation

---

# 💡 Final Insight

This service is the foundation of:

* user behavior tracking
* system observability
* future data-driven features

---

It transforms your platform from:

> "just streaming video"

into:

> "an intelligent, event-driven system"

---
