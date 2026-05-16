# 🦆 Duck Social Network

> An interactive social network, built with Java and JavaFX, dedicated to the interaction between People and... Ducks! The project demonstrates the application of advanced Object-Oriented Programming (OOP) concepts, Design Patterns, Graph Algorithms, and relational databases.

---

## 🌟 Main Features

* **Dual User System:** Supports two types of entities - `People` (Persoane) and `Ducks` (Rațe) (with specific abilities like `FLYING`, `SWIMMING`, or both).
* **Friendship Management:** Full system for sending, accepting, or rejecting friend requests.
* **Private Messaging (Chat):** Real-time chat system (updated via the *Observer Pattern*), supporting the *Reply* functionality and keeping track of read/unread messages.
* **Events and Competitions:** Users can create events (e.g., duck races), register as spectators or participants, and the system automatically generates race times and rankings.
* **Advanced Algorithms:** Implementation of graph algorithms (DFS/BFS) to identify the number of communities (connected components) and determine the most sociable community.
* **Custom Pagination:** A pagination system developed from scratch, optimized for large databases, with a responsive graphical interface.
* **Graphical User Interface (GUI):** Modern UI built with JavaFX, FXML, and styled using CSS.

---

## 🛠️ Technologies and Architecture

This project was developed with a strong focus on *Clean Code* and a layered architecture.

* **Backend:** Java 17+
* **Frontend (GUI):** JavaFX (FXML, CSS)
* **Database:** PostgreSQL
* **DB Communication:** JDBC (with PreparedStatement to prevent SQL Injections)
* **Security:** Password encryption (AES/BCrypt)
* **Design Patterns used:**
    * **Factory Pattern:** For dynamic instantiation of different duck types.
    * **Decorator Pattern:** For adding specific behaviors (`FlyingDuck`, `SwimmingDuck`) at runtime.
    * **Observer Pattern:** To ensure the reactive architecture of the graphical interface (the UI updates automatically upon receiving a new message).
    * **Repository / DAO Pattern:** For decoupling business logic from data access.

---

## ⚙️ How to run the project locally

If you want to test the application on your own computer, follow the steps below.

### 1. Prerequisites
* **Java Development Kit (JDK):** Version 17 or newer.
* **PostgreSQL:** Installed and running on the default port `5432`.
* **IDE:** IntelliJ IDEA (recommended) or Eclipse, configured for JavaFX/Maven projects.

### 2. Database Setup
The application requires a PostgreSQL database to run. I have prepared an initialization script that creates the tables, ENUM types, and inserts some mock data.

1. Open pgAdmin (or the psql terminal).
2. Create an empty database named `useri`.
3. Run the `init_db.sql` script located in the root of this repository.

### 3. Environment Variables (Optional, but recommended)
For security reasons, the application reads the database credentials from environment variables. If they are not set, it will fallback to the default values (`postgres` / `postgres`).

If your local database has a different password, set the following environment variables in your system or in your IDE's Run Configuration:
* `DB_URL` (e.g., `jdbc:postgresql://localhost:5432/useri`)
* `DB_USER` (e.g., `postgres`)
* `DB_PASSWORD` (your PostgreSQL password)

### 4. Launching the Application
To start the application with the graphical interface, run the main class:
`src/main/java/org/example/ducksocialnetworkm/Main.java`

*Note for testing:* You can use the following test accounts included in the SQL script:
* **Username:** `MARIAC` | **Password:** `MYPASS`
* **Username:** `DANV` | **Password:** `DANPASS`
  *(Note: Make sure the passwords in your local database match the encryption algorithm in the source code).*

---

## 📂 Project Structure

The project is organized into packages following the Separation of Concerns principle:

* `controller/` - Contains the JavaFX classes that handle UI events.
* `depozit/` - Implementation of the Repository pattern for data access (DB). It also contains the `paging` subpackage for pagination logic.
* `domeniu/` - The core entities of the application (`User`, `Rata`, `Mesaj`, `Event`), logically organized.
* `factory/` - The logic for creating complex objects.
* `interfata/` - UI components (for the transition from console to GUI).
* `serviciu/` - The Business Logic layer, containing validation algorithms, friendship management, and the community system (Graphs).
* `utils/` - Global utilities (password encryption, event types for the Observer pattern).
* `resources/` - The `.fxml` and `style.css` files for the application's appearance.

---

## 🚀 Future Plans (Roadmap)
* [ ] Extract hardcoded texts into an internationalization (i18n) system.
* [ ] Transition to a Connection Pool (e.g., HikariCP) to optimize database connections.
* [ ] Implement a "Dark Mode" by manipulating the CSS file.