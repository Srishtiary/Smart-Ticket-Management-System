# Smart Ticket Management System

A RESTful Spring Boot application for managing support tickets. It features JWT-based authentication, role-based access control (RBAC), and integrates with MongoDB Atlas for persistent storage. 

## 🚀 Tech Stack

*   **Java 21**
*   **Spring Boot 3.x** (Web, Security, Validation)
*   **MongoDB Atlas** (Spring Data MongoDB)
*   **JSON Web Tokens (JWT)** for stateless authentication
*   **Lombok** to reduce boilerplate code
*   **Maven** for dependency management

## ✨ Features

*   **User Registration & Authentication**: Secure user signup and login using JWT.
*   **Role-Based Access Control**:
    *   **USER**: Can create tickets and view their own tickets.
    *   **ADMIN**: Can view all tickets, update ticket statuses, assign tickets to specific users, and delete tickets.
*   **Global Exception Handling**: Returns clean, consistent JSON error messages for invalid requests, unauthorized access, and missing resources.
*   **Validation**: Built-in DTO validation for secure data entry.

## 🛠️ Prerequisites

*   Java 21 installed (`java -version`)
*   Maven installed (`mvn -version`)
*   A [MongoDB Atlas](https://www.mongodb.com/atlas/database) account and cluster.

## ⚙️ Setup and Installation

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/Srishtiary/Smart-Ticket-Management-System.git
    cd Smart-Ticket-Management-System
    ```

2.  **Configure MongoDB & JWT:**
    Open `src/main/resources/application.yaml` and configure your database URI and a secure JWT secret:
    ```yaml
    spring:
      application:
        name: smart-ticket-management-system
      mongodb:
        uri: mongodb+srv://<username>:<password>@cluster0.xxxxx.mongodb.net/ticketapp?appName=Cluster0
    
    jwt:
      secret: my-super-secret-key-that-is-at-least-32-characters-long
      expiration: 600000 # 10 minutes
    ```

3.  **Run the application:**
    ```bash
    mvn spring-boot:run
    ```
    The server will start on `http://localhost:8080`.

## 📡 API Endpoints

### Authentication
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Register a new user | Public |
| `POST` | `/api/auth/login` | Login & receive JWT | Public |

### Tickets
*Requires `Authorization: Bearer <token>` header.*

| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/tickets` | Create a new ticket | USER, ADMIN |
| `GET` | `/api/tickets` | Get your tickets (or all if admin) | USER, ADMIN |
| `GET` | `/api/tickets/{id}` | Get ticket details | USER (own only), ADMIN |
| `PATCH` | `/api/tickets/{id}/status` | Update ticket status | Assigned User, ADMIN |
| `PATCH` | `/api/tickets/{id}/assign` | Assign ticket to user | ADMIN only |
| `DELETE` | `/api/tickets/{id}` | Delete a ticket | ADMIN only |

## 🛡️ Testing with Postman

1.  Register a user to get a JWT token.
2.  In Postman, go to the **Authorization** tab, select **Bearer Token**, and paste your token.
3.  Make requests to the `/api/tickets` endpoints!
