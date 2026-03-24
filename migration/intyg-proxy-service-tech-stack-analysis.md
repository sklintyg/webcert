# Intyg Proxy Service — Technical Stack Analysis

## 🏗️ Project Overview

A **multi-module Gradle project** acting as a **proxy/integration layer** for Swedish healthcare (Inera) services. It mediates SOAP-based
integrations (HSA directory services, PU population registry, Elva77 user profiles) and exposes them via a REST API.

---

## 🧱 Core Framework & Language

| Area                      | Technology                                                                    |
|---------------------------|-------------------------------------------------------------------------------|
| **Language**              | Java (version managed via Inera BOM toolchain)                                |
| **Framework**             | Spring Boot (version managed via Inera BOM)                                   |
| **Build System**          | Gradle (multi-module, Kotlin DSL-style settings, Groovy build scripts)        |
| **Dependency Management** | Centralized via `se.inera.intyg.bom:platform` (version catalog + BOM pattern) |

---

## 📦 Module Architecture

| Module                    | Purpose                                                                    |
|---------------------------|----------------------------------------------------------------------------|
| **`app`**                 | Main Spring Boot application — REST controllers, services, config          |
| **`integration-api`**     | Shared DTOs/interfaces for all integrations                                |
| **`integration-common`**  | Shared SOAP/CXF infrastructure                                             |
| **`integration-hsa`**     | Real HSA (Hälso- och Sjukvårdens Adressregister) integration via SOAP/WSDL |
| **`integration-pu-v5`**   | Real PU (Personuppgiftstjänsten) v5 integration via SOAP/WSDL              |
| **`integration-elva77`**  | Real Elva77 (1177/MVK user profile) integration via SOAP/WSDL              |
| **`integration-fakehsa`** | Fake/stub HSA for local dev & testing                                      |
| **`integration-fakepu`**  | Fake/stub PU for local dev & testing                                       |
| **`integration-test`**    | Integration tests (separate test suite)                                    |
| **`logging`**             | Cross-cutting logging/observability (AOP-based)                            |

---

## 🔧 Key Frameworks & Libraries

### Web & API

- **Spring Boot Starter Web** — REST API layer (controllers for person, organization, authorization, employee, citizen)
- **Spring Boot Starter Actuator** — Health checks, metrics, management endpoints

### SOAP/WS Integration

- **Apache CXF** (`cxf-rt-frontend-jaxws`, `cxf-rt-transports-http`) — SOAP client for HSA, PU, Elva77
- **Spring Boot Starter Web Services** — WS support
- **WSDL2Java** (`com.yupzip.wsdl2java` plugin) — Code generation from WSDL contracts (RIV-TA profiles)
- **JAXB** (`jaxb-impl`, `jaxb2-basics`, `jaxb2-namespace-prefix`) — XML binding for SOAP payloads
- **Jakarta XML WS API** — JAX-WS standard API

### Caching

- **Spring Data Redis** (`spring-boot-starter-data-redis`) — Caching layer (used for PU person lookups with configurable TTL)

### Data & Serialization

- **Jackson** (`jackson-databind`, `JavaTimeModule`) — JSON serialization
- **MapStruct** — Type-safe DTO ↔ domain mapping (with Lombok binding)
- **Guava** (`guava-gwt`) — Utility library

### Developer Productivity

- **Lombok** — `@Slf4j`, `@RequiredArgsConstructor`, `@Value`, builders, etc.
- **Lombok-MapStruct Binding** — Interop between Lombok and MapStruct

### Logging & Observability

- **Logback** (`logback-classic`) — Logging implementation
- **SLF4J** — Logging API
- **AspectJ** (`aspectjweaver`) — AOP for `@PerformanceLogging` and cross-cutting concerns
- **ECS structured logging** (`logging.structured.format.console=ecs`) — Elastic Common Schema format

### Testing

- **JUnit 5** (Jupiter) — Unit & integration testing
- **Mockito** (with `mockito-junit-jupiter`) — Mocking framework (uses Java agent mode)
- **Spring Boot Test** — Integration test support
- **Microcks Testcontainers** (`io.github.microcks:microcks-testcontainers`) — Contract testing for SOAP/REST via Testcontainers

### Code Quality & CI

- **JaCoCo** — Code coverage (aggregated across modules)
- **SonarQube** (`org.sonarqube` plugin) — Static analysis
- **CycloneDX** (`org.cyclonedx.bom`) — SBOM generation
- **Ben Manes Versions** plugin — Dependency update checking

### Containerization

- **Docker** — Parameterized `Dockerfile` (JAR-based deployment)
- **Jenkins** — CI/CD (Jenkins.properties present)

---

## 🌐 Integration Landscape

The service proxies these Swedish national healthcare services (RIV-TA SOAP):

| Service           | Protocol  | Description                                                                           |
|-------------------|-----------|---------------------------------------------------------------------------------------|
| **HSA**           | SOAP/WSDL | Directory lookups — credentials, employees, healthcare units/providers, organizations |
| **PU v5**         | SOAP/WSDL | Population registry — person lookups (including protected persons), batched           |
| **Elva77 (1177)** | SOAP/WSDL | User profile service (MVK)                                                            |

All connect via **NTJP** (Nationella TjänstePlattformen) base URLs.

---

## 🧩 Architecture Pattern

- **Proxy/Facade pattern** — translates SOAP backends into a REST API
- **Feature-based packaging** — `person`, `organization`, `authorization`, `employee`, `citizen`
- **Fake/stub modules** for local development (profile-activated: `fakepu`, `fakehsa`)
- **Shared integration API module** for clean contracts between app and integration modules

---

## 💡 Suggested Deep-Dive Analyses

1. **Dependency versions audit** — Since all versions come from the Inera BOM, it would be useful to inspect actual resolved versions (
   Spring Boot, CXF, Jackson, etc.) for security/CVE concerns.
2. **Redis caching strategy** — Review cache configuration, TTL policies, and serialization to ensure correctness (especially for person
   data with protected status).
3. **WSDL contract review** — Examine the generated SOAP clients and their error handling to ensure robustness against upstream service
   failures.
4. **Test coverage analysis** — Review JaCoCo reports to identify under-tested modules, especially the integration modules.
5. **Profile & configuration management** — Audit how `dev`, `testability`, `fakepu`, `fakehsa` profiles are activated and whether any fake
   services could leak into production.
6. **Observability completeness** — Check if all controller endpoints have `@PerformanceLogging` and if MDC context propagates correctly
   through async/SOAP calls.
