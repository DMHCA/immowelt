# 🏡 Immowelt Munich Apartment Notifier

This is a pet project built with **Java 21** and **Spring Boot 3.4**, designed to automatically scan the Munich apartments listings on [immowelt.de](https://www.immowelt.de) and notify about new 3+ room apartments via Telegram. It also stores apartment data in a PostgreSQL database.

## 🚀 Features

- 🕵️‍♂️ Periodic scraping of Munich apartments with 3 or more rooms from Immowelt
- 📲 Instant notifications via Telegram when a new apartment matching the filter appears
- 🗄️ Persistent storage of apartment data in PostgreSQL
- 🐳 Fully dockerized application and database for easy deployment
- ☁️ Automatic deployment to AWS EC2 through GitHub Actions CI/CD pipeline
- ✅ Integration tests with Testcontainers for database reliability
- 🔧 Code style enforcement with Spotless and Google Java Format

## 📦 Technology Stack

| Layer        | Technology                                    |
|--------------|----------------------------------------------|
| Language     | Java 21                                      |
| Framework    | Spring Boot 3.4 (Web, WebFlux, Data JPA)     |
| Scraping     | Jsoup 1.17                                   |
| Database     | PostgreSQL (Docker + Testcontainers)          |
| Notification | Telegram Bot API                             |
| Testing      | JUnit 5, Spring Boot Test, Testcontainers    |
| Build Tool   | Maven                                        |
| Formatting   | Spotless Maven Plugin (Google Java Format)   |

## 🔧 How It Works

- The app scrapes the Immowelt broker profile GraphQL API for new apartment listings filtered by 3+ rooms in Munich.
- When a new matching apartment appears, the app:
    - Sends a notification with apartment details to a configured Telegram chat.
    - Saves the apartment data into the PostgreSQL database for tracking and avoiding duplicate alerts.
- The scraping runs periodically (e.g., via a scheduled Spring task).
- The whole system runs inside Docker containers — one for the Spring Boot app and one for PostgreSQL.
- The repository contains GitHub Actions workflows that build the Docker images and deploy everything to an AWS EC2 instance automatically on push.

## 🚀 Quick Start

### Prerequisites

- Java 21 SDK
- Docker and Docker Compose
- Maven
- AWS EC2 instance (or use local Docker Compose for testing)
- Telegram bot token and chat ID for notifications

### Local Setup

```bash
# Start PostgreSQL database with Docker Compose
docker-compose up -d

# Run the application locally
./mvnw spring-boot:run
```
### Environment Variables
Set the following environment variables (or configure in application.yml/application.properties):

```bash
TELEGRAM_BOT_TOKEN=your_bot_token
TELEGRAM_CHAT_ID=your_chat_id
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/yourdb
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password
```

### Run Tests
```bash
./mvnw V2__add_id_primary_key_to_estates.sql
```

### Project Structure
```bash
src
├── controller      # REST controllers (if any)
├── service         # Business logic, scraping, notifications
├── dto             # Data transfer objects
├── repository      # Spring Data JPA repositories
├── config          # Spring configuration classes
└── ...
```

### Deployment

- Fully automated via GitHub Actions:
    - Builds Docker images
    - Pushes to AWS EC2 instance
    - Runs Docker Compose to start the app and database

You can find the deployment workflow under `.github/workflows/deploy.yml`

### Code Style

- Enforced using Spotless Maven Plugin with Google Java Format
- Run checks locally before pushing:

```bash
./mvnw spotless:check
./mvnw spotless:apply
```

### Planned Improvements
- Web UI for setting up filters and managing tracked apartments

- More advanced Telegram bot commands for user interaction

- Support for additional cities or real estate platforms

- Docker multi-stage builds for smaller image sizes

- More comprehensive error handling and retry logic

### License
This project is licensed under the MIT License.

---

### Contact

For questions or feedback, feel free to reach out:  
📧 [contact@romantrippel.com](mailto:contact@romantrippel.com)  
👤 Roman Trippel