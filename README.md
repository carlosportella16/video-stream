# 🎬 Streaming Platform POC (Netflix-like)

A high-performance **video streaming platform proof of concept** built with Java, Kafka, and S3 (LocalStack), designed to simulate how modern platforms like Netflix deliver content at scale.

---

# 🚀 Overview

This project demonstrates how to build a **distributed, event-driven streaming system** capable of handling multiple concurrent users while maintaining performance and scalability.

### Key Features

* HLS video streaming (adaptive-ready)
* Event-driven architecture with Kafka
* Horizontal scaling with multiple instances
* Load balancing and caching via Nginx
* S3-compatible storage (LocalStack)
* Designed for performance and concurrency

---

# 🧠 Architecture

## High-Level Architecture

```text
                [Frontend - HLS Player]
                           ↓
                   [Nginx - Load Balancer + Cache]
                           ↓
        ┌──────────────────┴──────────────────┐
 [Streaming Service 1]               [Streaming Service 2]
        ↓                                      ↓
                 [LocalStack S3 - HLS Videos]

Events:
                   [Kafka Broker]
                         ↓
                [Analytics Service]
```

---

# 🧱 Components

## 🎬 Frontend

* Simple video catalog (2+ videos)
* HLS playback using `hls.js`
* Emits playback events:

  * PLAY
  * PAUSE
  * STOP
  * SPEED_CHANGE (V1)

---

## ⚖️ Nginx

* Load balancer across streaming instances
* Caching layer (simulates CDN behavior)
* Reduces backend load and latency

---

## ☕ Streaming Service (Java 21 + Spring Boot)

* Serves `.m3u8` playlists
* Streams `.ts` video chunks
* Produces Kafka events
* Stateless and horizontally scalable
* Uses **Virtual Threads** for high concurrency

---

## 🪣 S3 (LocalStack)

* Stores HLS video files
* Simulates cloud storage locally

```
videos/
 └── movie1/
      ├── playlist.m3u8
      ├── chunk0.ts
      ├── chunk1.ts
```

---

## 📡 Kafka

* Handles asynchronous event processing
* Decouples services

### Events:

```json
{
  "event": "PLAY",
  "videoId": "movie1",
  "timestamp": 123456,
  "sessionId": "abc123"
}
```

---

## 📊 Analytics Service

* Consumes Kafka events
* Logs user behavior
* Can be extended for real-time metrics

---

# ⚙️ How It Works

## 🎥 Streaming Flow

1. User selects a video
2. Frontend requests playlist:

   ```
   GET /video/{id}/playlist
   ```
3. Player downloads chunks sequentially:

   ```
   GET /video/{id}/chunk/{chunk}
   ```
4. Nginx distributes requests
5. Data is fetched from S3 or cache

---

## 📡 Event Flow

1. User interacts with player
2. Frontend sends event to backend
3. Streaming service publishes to Kafka
4. Analytics service consumes event

---

# ⚡ Performance Strategy

## 🔥 Efficient Streaming

* Uses `InputStream` (no full file loading)
* Reduces memory footprint

## 🔥 HTTP Range Support

* Enables seeking and buffering
* Prevents re-downloading entire video

## 🔥 Nginx Cache

* Caches video chunks
* Simulates CDN behavior

## 🔥 Virtual Threads (Java 21)

* Handles thousands of concurrent requests efficiently

## 🔥 Optimized HLS Segments

* Small chunks (4–6 seconds)
* Reduces buffering

---

# 🧪 Load Testing

Recommended tool:

* k6

Example:

```bash
k6 run --vus 20 --duration 60s test.js
```

Simulates multiple users accessing the system concurrently.

---

# 🚀 Versions

## 🟢 V0 — Core Streaming

* HLS streaming
* 2 videos in catalog
* Kafka events (PLAY, PAUSE, STOP)
* Load balancing (Nginx)
* S3 storage (LocalStack)

---

## 🟡 V1 — User Interaction

* Playback speed control:

  * 0.5x, 0.75x, 1x, 1.25x, 1.5x, 2x
* Kafka event:

  * `SPEED_CHANGE`

---

## 🔴 V2 — Performance Optimization

* Advanced caching (Nginx)
* Prefetching chunks
* Improved buffering strategy
* Multi-instance scaling optimization

---

# 🐳 Running Locally

## Requirements

* Docker
* Docker Compose
* Java 21

---

## Start the environment

```bash
docker-compose up --build
```

---

## Access

* Streaming API: http://localhost:8080
* Kafka: localhost:9092
* LocalStack S3: http://localhost:4566

---

# 📁 Project Structure

```
streaming-poc/
 ├── docker-compose.yml
 ├── nginx.conf
 ├── streaming-service/
 ├── analytics-service/
 ├── videos/
```

---

# ⚠️ Limitations

* No authentication (by design)
* No DRM
* No real CDN
* No global scaling

---

# 🧠 Key Learnings

* How video streaming works (HLS)
* How to design scalable systems
* Event-driven architecture with Kafka
* Load balancing and caching strategies
* Performance optimization in distributed systems

---

# 🚀 Future Improvements

* Cloud deployment (Render / Fly.io)
* Real CDN integration
* Observability (Prometheus + Grafana)
* Recommendation system (ML)

---

# 💡 Final Insight

This project is not just about video streaming.

It is about building:

> scalable, distributed, event-driven systems.

---

# 👨‍💻 Author

Built as a portfolio project to demonstrate backend engineering, distributed systems, and performance optimization skills.

---
