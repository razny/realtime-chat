# Real-Time Chat App

[Watch the demo](https://www.dropbox.com/scl/fi/i5fnttj0kz5duo5hl5im1/demo_chat.mp4?rlkey=3dxylqx1uwy21fjqh9kyhudgz&st=6c90pfr4&dl=0)

A full-stack, real-time chat application built with **Spring Boot**, **WebSocket (STOMP/SockJS)**, **RabbitMQ**, and **MongoDB**.  
Features include real-time messaging, message history, active user tracking, and bot integrations.

---

## Features

- Real-time messaging via WebSocket (STOMP/SockJS)
- RESTful CRUD for chat messages
- Message persistence with MongoDB
- RabbitMQ for decoupled message processing
- Active user tracking and broadcast
- Edit/Delete messages (with client/server ID support)
- Cat/Dog bot commands (`/cat`, `/dog`)
- Responsive frontend (HTML/CSS/JS, Bootstrap)
- Unit & integration tests

---

## Architecture

- **gateway** — Handles WebSocket connections, REST APIs, active users, and bots
- **message-processor** — Processes messages asynchronously and persists them via MongoDB

---

## Quick Start

**Requirements:**  
Java 17+, Maven, RabbitMQ, MongoDB

**1. Build everything:**

```sh
mvn clean install
```

**2. Start RabbitMQ and MongoDB** (locally or via Docker):

```sh
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
docker run -d --name mongo -p 27017:27017 mongo
```

**3. Run the backend:**

```sh
cd message-processor && mvn spring-boot:run
# In another terminal:
cd gateway && mvn spring-boot:run
```

**4. Open the app:**  
Go to [http://localhost:8080/chat](http://localhost:8080/chat)

---

## Testing

```sh
mvn test
```

Covers both unit and integration tests.

---

## Configuration

Edit `application.properties` in each module to adjust ports, database, or RabbitMQ settings.

---

## API Endpoints

- `GET /api/messages` — List all messages
- `POST /api/messages` — Create a message
- `GET /api/messages/{id}` — Get a message by ID
- `PUT /api/messages/{id}` — Update a message
- `DELETE /api/messages/{id}` — Delete a message
- `GET /api/messages/recent?since=timestamp` — Recent messages
- `GET /activeUsers` — List active users
- `GET /api/cat` — Get a cat image (bot)
- `GET /api/dog` — Get a dog image (bot)

---

## Screenshots

**Normal chat**  
![Normal chat](https://github.com/user-attachments/assets/413eacc5-5f94-46d7-bad6-73368aeeadc0)

**Editing a message**  
![Editing a message](https://github.com/user-attachments/assets/50a3fd7e-86d2-4a54-9689-c7b9bc0bfe94)

**Deleting a message**  
![Deleting a message](https://github.com/user-attachments/assets/ac7fdc1b-bbfe-430e-b2fb-520af57ab76d)
