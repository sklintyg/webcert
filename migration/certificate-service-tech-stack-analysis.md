# Technical Stack Analysis — Certificate Service

## Overview

A **multi-module Java/Spring Boot** application for managing clinical/health certificates, part of the Swedish **Intyg** (certificate)
ecosystem (`se.inera.intyg`).

---

## Core Platform

| Area                 | Technology                  | Details                                                                               |
|----------------------|-----------------------------|---------------------------------------------------------------------------------------|
| **Language**         | Java 21                     | Confirmed via builder/runtime images (`21.0.6` / `21.0.2`)                            |
| **Framework**        | Spring Boot                 | Web application with REST APIs                                                        |
| **Build System**     | Gradle (Kotlin DSL catalog) | Multi-module project with version catalog from centralized BOM (`se.inera.intyg.bom`) |
| **Containerization** | Docker                      | Runs on a Spring Boot base image                                                      |

---

## Frameworks & Libraries by Purpose

### Web & API

- **Spring Boot Starter Web** — REST API layer (synchronous)
- **Spring Boot Starter WebFlux** — Reactive HTTP client for external service integrations (print service, proxy service)
- **Apache CXF** — SOAP/WSDL integration via `wsdl2java` for clinical process interoperability (RIV-TA)

### Data & Persistence

- **Spring Data JPA** — ORM / repository layer
- **MySQL** (`mysql-connector-j`) — Production database
- **Liquibase** — Database schema migration and versioning

### Messaging

- **Spring Boot Starter ActiveMQ** — JMS messaging for certificate events (`certificate.event.queue`)

### PDF Generation

- **Apache PDFBox** — PDF document generation for certificates

### XML / SOAP Integration

- **Jakarta XML WS** — JAX-WS for SOAP web services
- **JAXB Runtime** (GlassFish) — XML binding for SOAP message marshalling
- **Helger Schematron** (`ph-schematron-xslt`) — XML validation using Schematron rules
- **WSDL2Java** plugin (`com.yupzip.wsdl2java`) — Code generation from WSDL/XSD schemas

### Cross-Cutting Concerns

- **Lombok** — Boilerplate reduction (`@Slf4j`, `@RequiredArgsConstructor`, `@Builder`, etc.)
- **AspectJ** — AOP for performance logging (`@PerformanceLogging`)
- **Logback** — Logging backend with ECS structured format (`logging.structured.format.console=ecs`)
- **Google Guava** — Utility library (used in logging module)
- **Spring Boot Actuator** — Health checks, metrics, and observability endpoints
- **Jackson** — JSON serialization/deserialization

### Testing

- **JUnit 5 (Jupiter)** — Unit & integration testing framework
- **Mockito** — Mocking (with Java agent for modern JVM support)
- **Testcontainers** — Integration tests with real MySQL, ActiveMQ, and MockServer containers
- **OkHttp MockWebServer** — HTTP mocking for WebFlux client tests
- **MockServer** — HTTP mocking for integration tests
- **Awaitility** — Async testing support
- **Spring Boot Test** — Context loading and test utilities

### Quality & CI/CD

- **JaCoCo** — Code coverage reporting
- **SonarQube** — Static code analysis
- **CycloneDX** — Software Bill of Materials (SBOM) generation
- **Jenkins** — CI/CD pipeline (confirmed by `Jenkins.properties`)

---

## Module Architecture

```
certificate-service (root)
├── app                               → Spring Boot application (controllers, JPA, config)
├── domain                            → Pure business logic (minimal dependencies: Jackson, SLF4J)
├── logging                           → Cross-cutting logging/AOP (AspectJ, Logback)
├── pdfbox-generator                  → PDF generation with Apache PDFBox
├── clinicalprocess-certificate-v4    → SOAP/XML integration (RIV-TA clinical process)
├── integration-certificate-print-service → External print service client (WebFlux)
├── integration-intyg-proxy-service   → External proxy service client (WebFlux)
└── integration-test                  → Full integration tests (Testcontainers)
```

The **domain** module is notably clean — it depends only on Jackson and SLF4J, following a proper **hexagonal/clean architecture** where the
domain has no framework dependencies.

---

## Centralized Dependency Management

All versions are managed through `se.inera.intyg.bom:platform` (version `1.0.0.11`), a centralized BOM and version catalog. This means exact
library versions are not pinned in this project — they're inherited from the organization-wide BOM.

---

## Suggested Additional Analysis

1. **Dependency Version Audit** — Since versions come from the centralized BOM, it could be valuable to inspect the resolved dependency
   tree (`./gradlew dependencies`) to check for outdated or vulnerable libraries.
2. **API Surface Analysis** — Review the controller layer to understand the full API surface (REST endpoints + SOAP endpoints).
3. **Database Schema Review** — Analyze Liquibase changelogs to understand the data model.
4. **Integration Points Deep Dive** — Map out all external service integrations (print service, proxy service, ActiveMQ
   consumers/producers).
5. **Security Configuration** — Check for Spring Security setup, authentication/authorization mechanisms (the stack currently shows no
   `spring-boot-starter-security` — security may be handled at infrastructure level).
6. **Performance/Observability** — Review the AOP logging setup and Actuator configuration to understand what's being monitored.
