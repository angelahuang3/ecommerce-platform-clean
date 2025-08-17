# 🛠️ E-Commerce Microservices Project

This project is a microservices-based e-commerce application built with **Spring Boot**, **Spring Data JPA**, **Spring Security**, **OpenFeign**, and **Kafka**. It provides end-to-end functionality for user management, product catalog, and order/payment workflows.

---

### 🚀 Features

- **User Management**
  - Register, login, and update user details
  - Secure authentication and authorization
- **Product Catalog**
    - CRUD operations for items
    - Fetch single or all items
- **Order Management**
    - Create, update, and cancel orders
    - View orders by user
- **Payment Processing**
    - Submit or refund payment with idempotency control
- **Service Communication**
    - Uses **Spring Cloud OpenFeign** for inter-service REST calls
- **Event-Driven Architecture**
    - **Apache Kafka** integration for asynchronous order and payment events
- **Resiliency**
    - Idempotency key handling for payment APIs
    - Decoupled services to improve fault isolation

---

## 📂 System Design

### Architecture
The system is composed of the following microservices:

- **Account Service**  
  Handles user registration, authentication, and profile updates.

- **Item Service**  
  Manages product catalog (CRUD for items).

- **Order Service**  
  Coordinates order creation, updates, and cancellations.  
  Communicates with Item Service and Payment Service using **OpenFeign**.  


- **Payment Service**  
  Processes payments, refunds, and enforces **idempotency**.  
  Listens to order events through **Kafka** for asynchronous processing.

### Inter-Service Communication
- **Synchronous:** REST calls via **OpenFeign** (e.g., Order → Item / Payment).
- **Asynchronous:** Event-driven communication with **Kafka** for order/payment events. 

---

## 📌 API Endpoints

### 👤 Account Service
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/account/register` | Register a new user |
| `POST` | `/api/account/login` | Authenticate user |
| `GET`  | `/api/account/current` | Get current logged-in user |
| `PUT`  | `/api/account/update` | Update user details |

---

### 📦 Item Service
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET`  | `/api/items` | Get all items |
| `GET`  | `/api/items/{id}` | Get item by ID |
| `POST` | `/api/items` | Add new item |
| `PUT`  | `/api/items/{id}` | Update existing item |
| `DELETE` | `/api/items/{id}` | Delete item |

---

### 🛒 Order Service
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/orders` | Create new order |
| `GET`  | `/api/orders` | Get all orders |
| `GET`  | `/api/orders/{id}` | Get order by ID |
| `PUT`  | `/api/orders/{id}` | Update order |
| `DELETE` | `/api/orders/{id}` | Cancel order |

---

### 💳 Payment Service
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/payments/submit` | Submit a payment |
| `GET`  | `/api/payments/{orderId}` | Get payment by order ID |
| `POST` | `/api/payments/refund` | Refund payment |

> **Note**: Payment endpoints require an **Idempotency-Key** header to ensure safe retries.
>           All endpoints require bear token or it will get 401 or 402 forbidden.


---

## 🛠️ Technologies

- **Java 17**, **Spring Boot**
- **Spring Data JPA**, **Hibernate**
- **Spring Security / JWT**
- **Spring Cloud OpenFeign**
- **Apache Kafka**
- **PostgreSQL / MySQL / MongoDB**
- **Docker & Docker Compose**
- **Postman** for API testing

---

## ▶️ Running the Project

1. **Build services**

```bash
   mvn clean package
   mvn clean install
```

2. Start Dockerized environment

```bash
docker compose build -t
docker compose up
```

3. Access services
- Account Service: `http://localhost:8081`
- Item Service: `http://localhost:8082`
- Order Service: `http://localhost:8083`
- Payment Service: `http://localhost:8084`