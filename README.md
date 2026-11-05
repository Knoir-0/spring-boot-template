[![License: LGPL v3](https://img.shields.io/badge/License-LGPL_v3-blue.svg)](https://www.gnu.org/licenses/lgpl-3.0)

# Spring Boot Template

A lightweight, ready-to-use template for spinning up a **Spring Boot 4** API server written in **Java**.

## Features

- **Spring Boot 4 & Java 21+** — Configured with Gradle (Kotlin DSL) and Spring Web MVC.
- **Preconfigured Filters & Server Settings** — Out-of-the-box setup for CORS (`CorsConfig`), response compression (`CompressionConfig`), cookies (`access_token`), and JSON/text request body limits (up to `2MB` via `RequestBodySizeLimitFilter`).
- **Public & Private Routing** — Clean separation between unauthenticated public endpoints and JWT-protected private endpoints under `/api`.
- **JWT Authentication** — Built-in token verification filter (`JwtAuthenticationFilter`) supporting both `Authorization: Bearer <token>` headers and `access_token` cookies.
- **Centralized Error Handling** — Custom `ApiError` exception class and global `@RestControllerAdvice` (`GlobalExceptionHandler`) that includes stack traces in `local` and `dev` environments while keeping `prd` responses clean.
- **Health Check Endpoint** — Built-in `/health` route reporting application status, uptime, Java/JVM runtime info, and OS/memory metrics.

## Project Structure

```text
├── src/main/java/com/knoir/spring_boot_template/
│   ├── config/
│   │   ├── CompressionConfig.java        # Response compression configuration
│   │   └── CorsConfig.java               # CORS filter configuration
│   ├── controller/
│   │   ├── HealthCheckController.java    # System & process health check handler
│   │   ├── PrivateRouteController.java   # Protected routes (require valid JWT)
│   │   └── PublicRouteController.java    # Public routes (no authentication required)
│   ├── dto/
│   │   └── HealthCheckResponse.java      # Typed response record for /health
│   ├── enumeration/
│   │   └── Environment.java              # Environment enum (local, dev, prd)
│   ├── exception/
│   │   ├── ApiError.java                 # Custom HTTP error exception (status, message, data)
│   │   └── GlobalExceptionHandler.java   # Global error-handling controller advice
│   ├── filter/
│   │   ├── JwtAuthenticationFilter.java  # JWT authentication & verification filter
│   │   └── RequestBodySizeLimitFilter.java # 2MB request body limit filter
│   └── SpringBootTemplateApplication.java # Spring Boot entry point & startup logger
├── src/main/resources/
│   └── application.properties            # Server, compression, & environment configuration
├── src/test/java/com/knoir/spring_boot_template/
│   └── SpringBootTemplateApplicationTests.java
├── .env                                  # Environment variables
├── build.gradle.kts
└── settings.gradle.kts
```

## Getting Started

### Prerequisites

- Java 21+
- Gradle (or use the included `./gradlew` wrapper)

### Environment Variables

Configure your environment variables in `.env` at the project root (automatically imported via `spring.config.import=optional:file:.env[.properties]`):

```dotenv
PORT=8080
APP_ENV=local
JWT_SECRET=local
```

| Variable | Description | Default |
| :--- | :--- | :--- |
| `PORT` | Port the Spring Boot server listens on | `8080` |
| `APP_ENV` | Runtime environment (`local`, `dev`, or `prd`) | `local` |
| `JWT_SECRET` | Secret key used to verify signed JWTs | `local` |

### Running the Server

```bash
# Start the Spring Boot server
./gradlew bootRun

# Run tests
./gradlew test

# Build executable JAR
./gradlew build
```

## Endpoints & Filter Flow

Requests pass through the Spring Boot filter and controller pipeline in the following order:

1. **Global Filters & Server Config**: CORS (`CorsConfig`), Response Compression (`CompressionConfig`), Cookie parsing, and 2MB Request Body Limit (`RequestBodySizeLimitFilter`).
2. **Health Check**:
   - `GET /health` — Returns system, JVM, and process health metrics.
3. **Public Routes** (`/api`):
   - `GET /api/public-route-example`
   - `GET /api/public-route-example/{id}`
4. **JWT Authentication Filter** (`JwtAuthenticationFilter`):
   - Verifies JWT from `Authorization: Bearer <token>` or the `access_token` cookie on protected `/api/**` routes and attaches the decoded `DecodedJWT` payload to `request.getAttribute("token")`.
   - Returns `401 Unauthorized` if missing/expired, or `403 Forbidden` if invalid/not yet active (`nbf`).
5. **Private Routes** (`/api`):
   - `GET /api/private-route-example`
   - `GET /api/private-route-example/{id}`
6. **404 Fallback & Error Handler** (`GlobalExceptionHandler`):
   - Unmatched routes return `404 Not Found`.
   - Exceptions thrown in controllers are formatted by `GlobalExceptionHandler`. Throw `new ApiError(status, message, data)` to return custom HTTP status codes and error messages.

## License

This project is licensed under the **GNU Lesser General Public License v3.0 or later (LGPL-3.0-or-later)**. See the [COPYING](COPYING) and [COPYING.LESSER](COPYING.LESSER) files for details.
