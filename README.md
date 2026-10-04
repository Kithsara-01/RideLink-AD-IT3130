# RideLink - IT3130 Application Development

RideLink is a backend ride-sharing system developed for the IT3130 Application Development group assignment. The project uses a microservices architecture with Java, Spring Boot, MongoDB, REST APIs, JWT-based security, Swagger/OpenAPI documentation, Postman API verification, automated Maven tests, and GitHub Actions CI.

## Microservices

RideLink contains four independent backend services:

| Service | Port | Java | Main Responsibility |
|---|---:|---:|---|
| Account Service | 8081 | 25 | User accounts, authentication, profiles, and roles |
| Driver & Vehicle Service | 8082 | 21 | Driver profiles, vehicles, availability, location, and operational status |
| Ride Management Service | 8083 | 17 | Ride requests, driver assignment, ride lifecycle, and ride status management |
| Fare & Payment Service | 8084 | 25 | Fare calculation, final fare, payments, and receipts |

Each service owns its own MongoDB data and communicates with other services through REST where required.

## Prerequisites

Install the following before running the project:

- Java versions required by the individual services
- Maven, or use the included Maven Wrapper
- MongoDB / MongoDB Atlas
- Postman for API testing
- Git

## Environment Variables

Do not commit real credentials or secrets to the repository.

The application configuration uses the following environment variables where applicable:

```text
MONGODB_URI
MONGODB_URI_DRIVER
MONGODB_URI_RIDE
MONGODB_URI_PAYMENT

JWT_SECRET
JWT_ISSUER
JWT_AUDIENCE

INTERNAL_SERVICE_KEY

ACCOUNT_SERVICE_URL
DRIVER_SERVICE_URL
FARE_SERVICE_URL
RIDE_SERVICE_URL

ADMIN_BOOTSTRAP_ENABLED
ADMIN_FULL_NAME
ADMIN_EMAIL
ADMIN_PASSWORD
```

Some service URL and configuration properties have local defaults in the application configuration. Sensitive values such as MongoDB credentials, JWT signing secrets, internal service keys, and passwords must not be committed.

## Running the Services

Run each service from its own directory.

### Account Service

```powershell
cd account-service
.\mvnw.cmd spring-boot:run
```

Runs on:

```text
http://localhost:8081
```

### Driver & Vehicle Service

```powershell
cd driver-vehicle-service
.\mvnw.cmd spring-boot:run
```

Runs on:

```text
http://localhost:8082
```

### Ride Management Service

```powershell
cd ride-management-service
.\mvnw.cmd spring-boot:run
```

Runs on:

```text
http://localhost:8083
```

### Fare & Payment Service

```powershell
cd fare-payment-service
.\mvnw.cmd spring-boot:run
```

Runs on:

```text
http://localhost:8084
```

The required environment variables must be configured before starting services that depend on them.

## Running Automated Tests

Run the Maven test command inside each service directory:

```powershell
.\mvnw.cmd test
```

The final local verification produced:

| Service | Tests |
|---|---:|
| Account Service | 18 |
| Driver & Vehicle Service | 38 |
| Ride Management Service | 43 |
| Fare & Payment Service | 64 |
| **Total** | **163** |

Final local verification: **163 tests passed with 0 failures and 0 errors.**

## Swagger / OpenAPI

When the corresponding service is running, Swagger UI is available at:

```text
Account Service
http://localhost:8081/swagger-ui/index.html

Driver & Vehicle Service
http://localhost:8082/swagger-ui/index.html

Ride Management Service
http://localhost:8083/swagger-ui/index.html

Fare & Payment Service
http://localhost:8084/swagger-ui/index.html
```

Protected API operations require valid authentication and authorization.

## Postman Verification

The repository contains the final Postman collection and safe exported environment:

```text
postman/RideLink IT3130 Final Verification.postman_collection.json

postman/RideLink IT3130 Final Export.postman_environment.json
```

The exported environment is intended for safe repository storage and does not contain runtime JWT tokens, passwords, credentials, or runtime IDs.

The Postman collection covers the integrated RideLink workflow and authorization/security verification.

## Main Workflow

The main integrated workflow is:

```text
Rider Authentication
        ↓
Driver Authentication and Preparation
        ↓
Ride Request
        ↓
Driver Assignment
        ↓
Ride Acceptance
        ↓
Ride Start
        ↓
Ride Completion
        ↓
Final Fare
        ↓
Payment
        ↓
Receipt
```

The services communicate through REST APIs during the workflow while maintaining separate service responsibilities and databases.

## Security

RideLink uses JWT-based authentication and role-based authorization.

Supported application roles include:

```text
RIDER
DRIVER
ADMIN
```

Security controls include authentication of protected API requests, role-based access control, ownership checks for protected operations, and internal service authentication where required.

Sensitive information must never be committed, including:

- MongoDB connection credentials
- JWT signing secrets
- Internal service keys
- User passwords
- Raw JWT tokens
- Other API credentials

## Continuous Integration

GitHub Actions CI is defined in:

```text
.github/workflows/ci.yml
```

The workflow runs the Maven test suite for all four microservices using the Java version required by each service:

```text
Account Service          Java 25
Driver & Vehicle Service Java 21
Ride Management Service  Java 17
Fare & Payment Service   Java 25
```

The workflow uses a matrix-based test job, Temurin Java, Maven dependency caching, and each service's Maven Wrapper.

CI does not require production credentials to be stored in the workflow.

## Branch Strategy

Development work is performed on feature or task branches and integrated through pull requests.

The main branch flow is:

```text
Feature / Task Branch
        ↓
develop
        ↓
main
```

The `develop` branch is used to integrate and verify project changes before the final verified version is merged into `main`.

## Team

RideLink was developed as a group project for the IT3130 Application Development module.

Verified service responsibilities:

- Deshan - Account Service
- Thakshi - Driver & Vehicle Service
- Tasun - Ride Management Service
- Dulina - Fare & Payment Service

## Academic Project

This repository contains the backend implementation developed for the IT3130 Application Development group assignment.