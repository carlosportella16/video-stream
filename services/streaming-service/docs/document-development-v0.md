# 🎬 Streaming Service — Development Guide (V0)

---

# 📌 Objective

This document provides a **step-by-step development roadmap** for implementing the **Streaming Service** from its current state (project created and building successfully) to the **first functional deployment (V0)**.

The focus is:

* Performance
* Clean architecture
* Realistic streaming behavior (HLS)
* Production-like mindset (even running locally)

---

# 🧠 Current State

✔ Spring Boot project created
✔ Builds successfully
✔ Base structure exists
✔ Dependencies added (Web, Kafka, etc.)

---

# 🎯 Target (V0 Definition)

By the end of this phase, the Streaming Service must:

* Serve `.m3u8` playlist files
* Stream `.ts` chunks from S3 (LocalStack)
* Support multiple concurrent users (≥5)
* Be stateless and scalable
* Integrate with Kafka (produce events)
* Run via Docker
* Be accessible through Nginx

---

# 🧱 PHASE 1 — Internal Structure Definition

## 🎯 Goal

Organize the codebase to support scalability and clarity.

## 📌 Tasks

1. Define package structure:

   * controller
   * service
   * config
   * integration (for S3/Kafka)
   * domain (optional but recommended)

2. Separate responsibilities:

   * Controller → HTTP layer
   * Service → business logic
   * Integration → external systems (S3, Kafka)

3. Define naming conventions:

   * Clear and explicit (e.g., `VideoStreamingService`, `S3ClientService`)

## ⏱️ Estimated Time

**1–2 hours**

---

# 🌐 PHASE 2 — S3 Integration (LocalStack)

## 🎯 Goal

Enable the service to fetch video files from S3.

## 📌 Tasks

1. Configure S3 client:

   * Endpoint: LocalStack
   * Static credentials
   * Region configuration

2. Externalize configuration:

   * application.yml (endpoint, bucket name)

3. Create S3 integration layer:

   * Method to fetch playlist
   * Method to fetch chunk (streaming)

4. Validate connectivity:

   * Test with existing files in bucket

## ⚠️ Important

* Do NOT load entire file into memory
* Use streaming (InputStream)

## ⏱️ Estimated Time

**2–3 hours**

---

# 🎥 PHASE 3 — Streaming Endpoints

## 🎯 Goal

Expose HTTP endpoints for video streaming.

## 📌 Tasks

1. Create endpoints:

   * `/video/{id}/playlist`
   * `/video/{id}/chunk/{chunk}`

2. Define responsibilities:

   * Controller → delegate only
   * Service → fetch from S3

3. Ensure correct headers:

   * Playlist → `application/vnd.apple.mpegurl`
   * Chunk → `video/MP2T`

4. Implement streaming response:

   * Return data as stream (not byte array)

## ⚠️ Critical

* Avoid buffering entire file
* Response must be incremental

## ⏱️ Estimated Time

**2–4 hours**

---

# ⚡ PHASE 4 — Performance Foundations

## 🎯 Goal

Ensure the service behaves efficiently under load.

## 📌 Tasks

1. Validate streaming approach:

   * Confirm no memory overload

2. Enable Virtual Threads (Java 21)

3. Tune server:

   * Thread pool configuration
   * Connection limits

4. Prepare for concurrency:

   * Stateless design
   * No shared mutable state

## 🧠 Validation

* Simulate multiple requests (manual or script)
* Observe CPU/memory

## ⏱️ Estimated Time

**1–2 hours**

---

# 📡 PHASE 5 — Kafka Integration (Producer)

## 🎯 Goal

Emit playback events.

## 📌 Tasks

1. Configure Kafka connection:

   * Bootstrap server (Docker network)

2. Create event model:

   * PLAY
   * PAUSE
   * STOP

3. Implement producer layer:

   * Async sending

4. Expose endpoint for events:

   * `/video/{id}/play`
   * `/video/{id}/pause`
   * `/video/{id}/stop`

5. Validate:

   * Messages arriving in Kafka

## ⚠️ Important

* Kafka must NOT block streaming
* Fire-and-forget approach

## ⏱️ Estimated Time

**2–3 hours**

---

# 🧪 PHASE 6 — Functional Validation

## 🎯 Goal

Ensure the service works end-to-end.

## 📌 Tasks

1. Validate playlist loading
2. Validate chunk streaming
3. Test with browser/player (HLS)
4. Open multiple sessions (tabs)
5. Validate event emission

## 🧠 Expected Outcome

* Video plays smoothly
* No blocking or freezing
* Events are produced correctly

## ⏱️ Estimated Time

**1–2 hours**

---

# 🐳 PHASE 7 — Dockerization

## 🎯 Goal

Prepare the service for containerized execution.

## 📌 Tasks

1. Create Dockerfile
2. Build application JAR
3. Create container image
4. Validate container execution
5. Ensure environment variables work

## 🧠 Validation

* Service starts correctly
* Endpoints accessible

## ⏱️ Estimated Time

**1–2 hours**

---

# 🌐 PHASE 8 — Integration with Infrastructure

## 🎯 Goal

Run the service within the full environment.

## 📌 Tasks

1. Integrate with Docker Compose:

   * Kafka
   * LocalStack
   * Nginx

2. Adjust configurations:

   * Hostnames (container network)
   * Ports

3. Validate:

   * Nginx routing
   * Multiple instances

## ⏱️ Estimated Time

**2–3 hours**

---

# 🚀 PHASE 9 — First Deployment (V0)

## 🎯 Goal

Achieve a working system accessible via Nginx.

## 📌 Tasks

1. Start full stack:

   * docker-compose up

2. Access via:

   * `http://localhost:8080`

3. Validate:

   * Video playback
   * Multiple users
   * Kafka events

4. Test failover:

   * Stop one instance
   * Confirm continuity

## ⏱️ Estimated Time

**1–2 hours**

---

# 📊 TOTAL ESTIMATED TIME

| Phase               | Time |
| ------------------- | ---- |
| Structure           | 1–2h |
| S3 Integration      | 2–3h |
| Streaming Endpoints | 2–4h |
| Performance         | 1–2h |
| Kafka               | 2–3h |
| Validation          | 1–2h |
| Docker              | 1–2h |
| Infra Integration   | 2–3h |
| Deployment          | 1–2h |

### ⏱️ Total: **13–23 hours**

---

# ⚠️ Common Pitfalls

* Loading entire video into memory ❌
* Blocking streaming with Kafka ❌
* Hardcoding endpoints ❌
* Ignoring concurrency ❌
* Not testing multiple users ❌

---

# 🧠 Engineering Mindset

Focus on:

* Stateless design
* Efficient I/O
* Separation of concerns
* Observability (logs)
* Incremental validation

---

# 🏁 Definition of Done (V0)

✔ Video plays via HLS
✔ Multiple users supported
✔ Kafka events emitted
✔ Runs via Docker
✔ Accessible via Nginx
✔ No buffering issues

---

# 🚀 Next Steps (After V0)

* V1 → Playback speed + events
* V2 → Advanced performance (cache, prefetch, tuning)

---

# 💡 Final Insight

You are not just building a streaming service.

You are building:

> a scalable, distributed, event-driven system with real-world architecture patterns.

---
