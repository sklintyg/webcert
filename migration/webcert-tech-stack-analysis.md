# Webcert — Technical Stack Analysis

*Generated: 2026-03-24 — Revised: 2026-03-24*

## Overview

**Webcert** is a Swedish healthcare certificate management web application, part of the **Inera/SKL Intyg** platform. It's a multi-module
**Java 21** server-side application packaged as a **WAR** and deployed on **Tomcat 10**. The application provides APIs for creating, signing,
sending, and managing medical certificates, integrating with national healthcare registries and external systems via SOAP and REST.

---

## Core Platform

| Aspect                 | Technology    | Version/Details                                                                     |
|------------------------|---------------|-------------------------------------------------------------------------------------|
| **Language**           | Java          | **21** (managed via Inera BOM toolchain)                                            |
| **Build System**       | Gradle        | Multi-module, Groovy DSL with version catalog from centralized BOM (`1.0.0.14`)     |
| **Application Server** | Apache Tomcat | **10** (Jakarta EE namespace)                                                       |
| **Packaging**          | WAR           | Deployed at configurable context path                                               |
| **Containerization**   | Docker        | Dockerfile deploys WAR into Catalina base image                                     |
| **Dev Server**         | Gretty        | **4.1.10** — embedded Tomcat 10 for local development                               |

---

## Framework Stack

### Web & Service Layer

- **Spring Framework** (spring-webmvc, spring-jms, spring-test) — Core application framework (**not Spring Boot**)
- **Apache CXF** (cxf-rt-frontend-jaxrs) — **JAX-RS** for all REST APIs (8 CXFServlet instances with separate Spring contexts); also
  used for SOAP client/server via JAX-WS
- **Jakarta EE** APIs (jakarta.ws.rs, jakarta.servlet, jakarta.jms, jakarta.persistence, jakarta.xml.ws, jakarta.xml.bind) — Full Jakarta
  EE 10+ migration (not javax)
- **Jackson** (jackson-core, jackson-databind, jackson-datatype-jsr310) — JSON serialization for REST endpoints; custom
  `ObjectMapper` configuration registered as CXF JSON provider
- **Spring MVC DispatcherServlet** — Registered at `/web/*` for page rendering only; **not used for REST APIs**
- **Apache HttpClient 5** (httpclient5) — HTTP client for external integrations
- **Spring `RestClient`** — Used in `integration-private-practitioner-service` for outbound REST calls
- **Jakarta Mail** (jakarta.mail-api) — Email sending via Spring `JavaMailSender` (configured in `mail-config.xml`);
  `MailNotificationServiceImpl` uses `@Async("threadPoolTaskExecutor")` for non-blocking email delivery via SMTPS with STARTTLS
- **Swagger** (swagger-jaxrs) — API documentation (non-prod only)

### Persistence Layer

- **Spring Data JPA** — Repository abstraction
- **Hibernate ORM** (hibernate-core) — JPA implementation
- **HikariCP** — Connection pooling (max pool: 20)
- **Liquibase** — Database schema migration and versioning
- **MySQL** (mysql-connector-j) — Production database
- **H2** — In-memory test database (MySQL compatibility mode)

### Messaging

- **Apache ActiveMQ** (activemq-spring) — JMS messaging via Spring JMS
- **Apache Camel** (camel-activemq, camel-core, camel-jaxb, camel-jms, camel-cxf) — Message routing in the notification-sender module;
  2 Camel contexts with XML-configured routes:
  - `webcertNotification` — Notification aggregation, transformation, and SOAP delivery
  - `webcertCertificateSender` — Certificate store/send/revoke/message processing
- **7 JMS queues** — certificate, notification, internal-notification, post-processing, ws-notification, aggregation, log

### Caching / Scheduling / Async

- **Redis** — Used for HTTP session store, application caching, and distributed locking
- **Spring Session Data Redis** (`spring-session-data-redis`) — Redis-backed HTTP session management
  (`@EnableRedisIndexedHttpSession`)
- **ShedLock** (shedlock-spring, shedlock-provider-redis-spring) — Distributed scheduled task locking via Redis
- **Inera Common Redis Cache** (common-redis-cache-core) — Shared Redis caching abstraction for HSA, PU, LaunchId,
  CertificatesForPatient caches with configurable TTLs
- **`@EnableScheduling`** — Declared in `JobConfig` and `FmbServiceImpl` (redundant dual declaration)
- **`@EnableSchedulerLock`** (`defaultLockAtMostFor = "PT10M"`) — ShedLock integration in `JobConfig`
- **`@EnableAsync`** — Enabled in `JobConfig`; `ThreadPoolTaskExecutor` (pool size: 10, queue capacity: 100) used for async email
  delivery (`@Async("threadPoolTaskExecutor")` on `MailNotificationServiceImpl`)

### Monitoring & Observability

- **Prometheus** (simpleclient_hotspot, simpleclient_servlet_jakarta) — Metrics endpoint (`/metrics` via `MetricsServlet` in web.xml)
- **Logback** with **Elastic ECS encoder** (logback-ecs-encoder) — Structured JSON logging for ELK/Elastic stack
- **SLF4J** — Logging API
- **AspectJ** (aspectjweaver) — AOP for `@PerformanceLogging` cross-cutting concerns

### Code Generation & Utilities

- **Lombok** — Annotation-based boilerplate reduction (`@Slf4j`, `@RequiredArgsConstructor`, `@Builder`, `@Data`)
- **JAXB2 Basics** (jaxb2-basics) — XML/SOAP schema code generation
- **XJC** (jaxb-xjc) — XSD-to-Java code generation for DSS (Digital Signing Service) schemas at build time
- **Guava** — General utility library
- **Commons IO** — File/IO utilities
- **Commons Lang3** — String/object utilities (used in notification-sender, fmb-integration, notification-stub)
- **Spring OXM** (spring-oxm) — XML marshalling in notification-sender
- **Vavr** — Functional programming library (used in FMB integration)
- **Apache Lucene** (lucene-analysis-common) — Text analysis (diagnosis code search)
- **jxls-poi** — Excel export functionality
- **Helger Schematron** (ph-schematron-xslt) — XML validation using Schematron rules (test scope)

### Security

- **Spring Security** (spring-security-config, spring-security-web) — Core security framework; `WebSecurityConfig` uses the modern
  Spring Security 6+ `SecurityFilterChain` bean pattern (not the deprecated `WebSecurityConfigurerAdapter`)
- **Spring Security SAML 2.0** (spring-security-saml2-service-provider) — SAML2 authentication with 3 identity providers:
  - `eleg` — Swedish e-legitimation (BankID)
  - `siths` — SITHS smart card (elevated LoA)
  - `sithsNormal` — SITHS normal mode
- **OpenSAML 4** — Underlying SAML library; initialized via `OpenSamlConfig` (implements `InitializingBean`)
- **CSRF Protection** — `CookieCsrfTokenRepository`
- **Spring `@EventListener`** — `AuthenticationEventListener` handles `InteractiveAuthenticationSuccessEvent` and
  `LogoutSuccessEvent` for audit logging via `MonitoringLogService`
- **Inera Security** (security-authorities, security-common, security-filter, security-siths) — Custom security filters, authority
  resolution, SITHS integration, and YAML-based role/permission configuration
- **DSS (Digital Signing Service)** — Integration for qualified electronic signatures via SAML-based signing service

---

## Domain-Specific / Inera Ecosystem Dependencies

This is a significant part of the stack — the app relies heavily on internal Inera libraries:

| Category                                      | Libraries                                                                                                                                                                                                               | Purpose                                                                                                       |
|-----------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------|
| **Inera Infra** (`infraVersion: 4.1.0`)       | certificate, common-redis-cache-core, driftbanner-dto, dynamiclink, hsa-integration-api, hsa-integration-intyg-proxy-service, ia-integration, integreradeenheter, intyginfo, log-messages, message, monitoring, postnummerservice-integration, privatepractitioner, pu-integration-api, pu-integration-intyg-proxy-service (runtime), security-authorities, security-common, security-filter, security-siths, sjukfall-engine, srs-integration, testcertificate, xmldsig | **24 modules** — HSA/PU integration, security, signing, monitoring, caching, sick leave engine                |
| **Inera Common** (`commonVersion: 4.1.0`)     | af00213, af00251, ag114, ag7804, common-schemas, common-services, common-support, db, doi, fk-parent, fk7263, integration-util, lisjp, logging-util, luae_fs, luae_na, luse, ts-bas, ts-diabetes, tstrk1009, tstrk1062; **test-only:** ag-parent, sos_parent, ts-parent | **21 implementation + 3 test-scope modules** — Certificate type definitions (14 types) plus shared support   |
| **Schema Libraries**                          | intyg-clinicalprocess-healthcond-certificate, intyg-clinicalprocess-healthcond-rehabilitation, intyg-clinicalprocess-healthcond-srs, schemas-contract, clinicalprocess-healthcond-certificate (RIV-TA), insuranceprocess-healthreporting (RIV-TA) | **6 schema libraries** — RIV-TA SOAP contract schemas (Swedish national interoperability standards)           |
| **Reference Data** (`refDataVersion: 2.0.22`) | refdata                                                                                                                                                                                                                 | ICD-10 codes, code systems, diagnosis codes                                                                   |
| **Inera BOM** (`intygBomVersion: 1.0.0.14`)   | platform, catalog                                                                                                                                                                                                       | Centralized dependency version management                                                                     |

---

## Module Structure

| Module                                       | Directory              | Purpose                                                                        |
|----------------------------------------------|------------------------|--------------------------------------------------------------------------------|
| **`webcert-web`**                            | `web/`                 | Main WAR module — controllers, services, CXF endpoints, Spring config, security |
| **`webcert-common`**                         | `common/`              | Shared DTOs, exceptions, log templates, client interfaces                      |
| **`webcert-persistence`**                    | `persistence/`         | JPA entities (78 files), repositories, Liquibase migrations                    |
| **`webcert-logging`**                        | `logging/`             | Cross-cutting logging (MDC filters, performance logging, AOP)                  |
| **`notification-sender`**                    | `notification-sender/` | Apache Camel routes for notification/certificate processing via ActiveMQ       |
| **`fmb-integration`**                        | `integration/fmb-integration/` | FMB (Functional Disability Database) integration with ShedLock scheduling |
| **`integration-api`**                        | `integration/integration-api/` | Shared DTO/interface contracts for integrations                          |
| **`servicenow-integration`**                 | `integration/servicenow-integration/` | ServiceNow subscription management (CXF JAX-RS)                    |
| **`integration-certificate-analytics-service`** | `integration-certificate-analytics-service/` | Certificate analytics event publishing via JMS              |
| **`integration-private-practitioner-service`** | `integration-private-practitioner-service/` | Private practitioner validation via Spring `RestClient`      |
| **`mail-stub`**                              | `stubs/mail-stub/`     | Mock email service for development (AspectJ AOP interception)                  |
| **`notification-stub`**                      | `stubs/notification-stub/` | Mock notification service with error injection (Redis-backed state)        |
| **`liquibase-runner`**                       | `tools/liquibase-runner/`  | Standalone Liquibase migration execution utility                           |

---

## Servlet & Filter Architecture

### Web Application Deployment (web.xml — 345 lines)

The application is bootstrapped via `web.xml` (not `WebApplicationInitializer` or Spring Boot). It defines:

- **4 listeners**: `LogbackConfiguratorContextListener`, `ContextLoaderListener`, `RequestContextListener`,
  `HttpSessionEventPublisher`
- **10 servlets**: 1 Spring MVC `DispatcherServlet` + 8 `CXFServlet` instances + 1 Prometheus `MetricsServlet`
- **13 servlet filters** in this order:

| Order | Filter                           | Scope                    | Source       |
|-------|----------------------------------|--------------------------|--------------|
| 1     | `springSessionRepositoryFilter`  | `/*`                     | Spring Session |
| 2     | `requestContextHolderUpdateFilter` | `/*`                   | Inera Infra  |
| 3     | `MdcServletFilter`               | `/*`                     | Webcert      |
| 4     | `defaultCharacterEncodingFilter` | `/v2/visa/intyg/*`       | Webcert      |
| 5     | `sessionTimeoutFilter`           | `/*`                     | Inera Infra  |
| 6     | `springSecurityFilterChain`      | `/*`                     | Spring Security |
| 7     | `principalUpdatedFilter`         | `/*`                     | Inera Infra  |
| 8     | `unitSelectedAssuranceFilter`    | `/api/*`, `/moduleapi/*` | Webcert      |
| 9     | `securityHeadersFilter`          | `/*`                     | Inera Infra  |
| 10    | `MdcUserServletFilter`           | `/*`                     | Webcert      |
| 11    | `internalApiFilter`              | `/internalapi/*`         | Webcert      |
| 12    | `launchIdValidationFilter`       | `/api/*`, `/moduleapi/*` | Webcert      |
| 13    | `allowCorsFilter`                | `/api/v1/session/invalidate` | Webcert  |

### Servlet URL Mapping

| Servlet                | URL Pattern                           | Type     | Purpose                            |
|------------------------|---------------------------------------|----------|------------------------------------|
| `web`                  | `/web/*`                              | Spring MVC | Page rendering                   |
| `api`                  | `/api/*`                              | CXF JAX-RS | Main GUI REST APIs (19 controllers) |
| `moduleapi`            | `/moduleapi/*`                        | CXF JAX-RS | Module-specific REST APIs (3 controllers) |
| `internalapi`          | `/internalapi/*`                      | CXF JAX-RS | Inter-service REST APIs (8 controllers) |
| `services`             | `/services/*`                         | CXF JAX-WS | SOAP endpoints + stubs           |
| `testability`          | `/testability/*`                      | CXF JAX-RS | Test/debug REST APIs             |
| `authtestability`      | `/authtestability/*`                  | CXF JAX-RS | Auth test REST APIs              |
| `uthoppintegrationapi` | `/webcert/web/user/*`                 | CXF JAX-RS | Legacy Webcert 0.5/Medcert integration |
| `integrationapi`       | `/visa/*`, `/v2/visa/*`               | CXF JAX-RS | Webcert 2.0+ EHR integration    |
| `metrics`              | `/metrics`                            | Prometheus | Metrics endpoint                 |

---

## REST API Surface (JAX-RS via Apache CXF)

All REST APIs use `@Path` annotations (JAX-RS) served through CXF, **not** Spring MVC `@RestController`.

**Total JAX-RS controllers: ~52** (across all servlet contexts)

### GUI API (`/api/*`) — 19+ controllers

`JsLogApiController`, `UserApiController`, `FmbApiController`, `SrsApiController`, `ConfigApiController`, `SignatureApiController`,
`SessionStatusController`, `SubscriptionController`, `CertificateController` (21 endpoints), `QuestionController` (11 endpoints),
`PatientController`, `ListController`, `ListConfigController`, `CertificateTypeController`, `IcfController`, `FMBController`,
`LogController`, `ConfigController`, `UserController`, `InvalidateSessionApiController`, `FakeSignatureApiController` (non-prod),
`PrivatePractitionerApiController`

### Module API (`/moduleapi/*`) — 3 controllers

`IntygModuleApiController`, `StatModuleApiController`, `DiagnosModuleApiController`

### Internal API (`/internalapi/*`) — 8 controllers

`IntegratedUnitsApiController`, `IntygInfoApiController`, `TestCertificateController`, `TermsApiController`, `EraseApiController`,
`UnansweredCommunicationController` (1 `@RestController` — the only Spring MVC controller in the project),
`CertificateInternalApiController`, `NotificationController`

### Integration API (`/visa/*`, `/v2/visa/*`) — 4 controllers

`IntygIntegrationController`, `UserIntegrationController`, `LaunchIntegrationController`, `CertificateIntegrationController`

### Legacy Integration (`/webcert/web/user/*`) — 2 controllers

`FragaSvarUthoppController`, `PrivatePractitionerFragaSvarUthoppController`

### Testability API (`/testability/*`) — 12 controllers

`ArendeResource`, `FragaSvarResource`, `IntygResource`, `EventResource`, `FmbResource`, `IntegreradEnhetResource`, `LogResource`,
`ReferensResource`, `UserAgreementResource`, `ConfigurationResource`, `CertificateTestabilityController`,
`FakeLoginTestabilityController`

---

## SOAP Endpoints & Clients

### Server-Side SOAP Endpoints (6)

| Endpoint Path                                    | Implementor                                    | Version |
|--------------------------------------------------|------------------------------------------------|---------|
| `/create-draft-certificate`                      | `CreateDraftCertificateResponderImpl`           | v3.0    |
| `/receive-question`                              | `ReceiveQuestionResponderImpl`                  | v1.0    |
| `/receive-answer`                                | `ReceiveAnswerResponderImpl`                    | v1.0    |
| `/send-message-to-care`                          | `SendMessageToCareResponderImpl`                | v2.0    |
| `/list-certificates-for-care-with-qa`            | `ListCertificatesForCareWithQAResponderImpl`    | v3.0    |
| `/get-certificate-additions`                     | `GetCertificateAdditionsResponderImpl`          | v1.1    |

### SOAP Clients (ws-config.xml — 15+ JAX-WS clients)

Certificate operations (send, register, revoke, get, list), Q&A operations (sendQuestion, sendAnswer), receiver management
(listApproved, listPossible, registerApproved), sick leave operations, certificate type info, message routing — all connecting to
Intygstjänst via NTJP with mutual TLS.

---

## Integration Landscape

| Service                    | Protocol  | Description                                                |
|----------------------------|-----------|------------------------------------------------------------|
| **Intygstjänst**           | SOAP/WSDL | Certificate store — register, send, revoke, list, get      |
| **HSA** (via proxy)        | REST      | Healthcare address registry — organization/unit lookups    |
| **PU** (via proxy)         | REST      | Population registry — person data lookups                  |
| **SRS**                    | SOAP/REST | Risk prediction — diagnosis risk assessment                |
| **FMB**                    | REST      | Functional disability database — scheduled data sync       |
| **ServiceNow**             | REST      | Provider subscription management                           |
| **Private Practitioner**   | REST      | Practitioner validation and registration                   |
| **Certificate Analytics**  | JMS       | Analytics event publishing                                 |
| **IA**                     | REST      | Internal application integration                           |
| **DSS**                    | SAML/SOAP | Digital Signing Service for qualified electronic signatures |
| **Certificate Service**    | REST      | Modern certificate service (csintegration — 240 files)     |
| **EHR Systems**            | REST      | External EHR integration via `/visa/*` deep-link endpoints |
| **FK (Försäkringskassan)** | SOAP/WSDL | Insurance agency — Q&A for FK7263 certificates             |
| **ActiveMQ**               | JMS       | Message broker for async notification/certificate delivery |

---

## Configuration Architecture

### Spring XML Configuration Files (24 application-owned + external)

**Master config loaded by `web.xml`:**
```
web.xml (ContextLoaderListener)
  └─> webcert-config.xml [MASTER — component scanning, property placeholders, beans]
       ├─> webcert-common-config.xml
       ├─> repository-context.xml [JPA, transactions, Spring Data repositories]
       ├─> ws-config.xml [15+ JAX-WS SOAP clients, TLS config]
       ├─> mail-config.xml [JavaMailSender, task scheduling]
       ├─> notification-sender-config.xml
       │    ├─> jms-context.xml [ActiveMQ, Camel component, JMS config]
       │    ├─> notifications/camel-context.xml [Camel notification routes]
       │    ├─> notifications/beans-context.xml [Notification transformer, aggregator]
       │    ├─> notifications/ws-context.xml [CertificateStatusUpdate SOAP client]
       │    ├─> certificates/camel-context.xml [Camel certificate routes]
       │    └─> certificates/beans-context.xml [Certificate processors]
       ├─> fmb-services-config.xml
       ├─> servicenow-services-config.xml
       ├─> integration-certificate-analytics-service-config.xml
       ├─> integration-private-practitioner-service-config.xml
       ├─> basic-cache-config.xml [external — Redis cache infra from Inera infra]
       ├─> ia-services-config.xml [external — IA integration from Inera infra]
       ├─> srs-services-config.xml [external — SRS integration from Inera infra]
       ├─> xmldsig-config.xml [external — XML digital signing from Inera infra]
       ├─> hsa-integration-intyg-proxy-service-config.xml [external]
       ├─> pu-integration-intyg-proxy-service-config.xml [external]
       └─> classpath*:module-config.xml, wc-module-cxf-servlet.xml [external — per-certificate-type configs]
```

**CXF servlet configs (8 files in WEB-INF/):**
`api-cxf-servlet.xml`, `moduleapi-cxf-servlet.xml`, `internalapi-cxf-servlet.xml`, `services-cxf-servlet.xml`,
`integration-cxf-servlet.xml`, `testability-cxf-servlet.xml`, `authtestability-cxf-servlet.xml`,
`uthopp-integration-cxf-servlet.xml`

**Stub configs (profile-activated):**
`mail-stub-context.xml`, `mail-stub-testability-api-context.xml`, `notification-stub-context.xml`, `fmb-stub-context.xml`,
`servicenow-stub-context.xml`, `swagger-api-context.xml`, `webcert-testability-api-context.xml`

### Java @Configuration Classes (18)

**Web module (11):**
`WebSecurityConfig`, `AppConfig`, `JmsConfig`, `JobConfig`, `CacheConfig`, `LoggingConfig`,
`GrpRestConfig`, `RedisLaunchIdCacheConfiguration`, `CertificatesForPatientCacheConfiguration`,
`CertificateServiceRestTemplateConfiguration`, `CertificateServiceRestClientConfiguration`

**Other modules (7):**
`JpaConfig` (persistence — `@Profile("!h2")`), `CertificateAnalyticsServiceIntegrationConfig` (analytics),
`PrivatePractitionerRestClientConfig` (pp-service), `ServiceNowIntegrationConfig`, `ServiceNowIntegrationRestConfig`,
`ServiceNowStubConfig`, `ServiceNowStubBeanConfig` (servicenow)

### Property Configuration

**`application.properties`** — ~365 properties covering: database, mail, PDL logging, integration URLs (50+), NTJP TLS,
diagnosis codes, notification queues, SAML/security, Redis caching, scheduled jobs, DSS signing, ServiceNow.

**`@Value` usage**: ~165 injection points across ~57 Java files (highest concentration in `WebSecurityConfig` with 16,
`DssMetadataService` with 12, `JmsConfig` with 9, `ConfigApiController` with 10, `ConfigController` with 6).

### Feature Flags

**`features.yaml`** (located at `devops/dev/config/features.yaml`) — YAML-based feature flag system for toggling application
capabilities at runtime. Uses anchor/alias YAML patterns to avoid duplication across role configurations.

### Development Configuration (devops/)

The `devops/dev/config/` directory contains development-time configuration not bundled in the WAR:
- `application-dev.properties` — Dev-specific property overrides (loaded via `dev.config.file` JVM argument)
- SAML IdP/SP metadata XML files (`idp-eleg.xml`, `idp-inera.xml`, `sp-*.xml`)
- SSL certificates and keystores (`localhost.p12`, `truststore.jks`)
- `dss-metadata.xml` — DSS signing service metadata
- `care-provider-mapping-config.json`, `unit-notification-config.json` — Runtime configuration files

### Spring Profiles

| Profile                               | Purpose                                           |
|---------------------------------------|---------------------------------------------------|
| `dev`                                 | Development mode (stubs, embedded broker)          |
| `!prod`                               | Enables fake services (FakeLoginService, FakeSignatureApiController, FakeUnderskriftServiceImpl) |
| `!h2`                                 | Production JPA/database config (`JpaConfig`)       |
| `caching-enabled`                     | Activates Redis caching                            |
| `wc-security-test`                    | Test security configuration                        |
| `wc-all-stubs`                        | Enable all service stubs                           |
| `wc-mail-stub`                        | Mock email service                                 |
| `wc-notificationsender-stub`          | Mock notification service                          |
| `wc-fmb-stub`                         | Mock FMB service                                   |
| `wc-servicenow-stub`                  | Mock ServiceNow service                            |
| `servicenow-integration-stub`         | ServiceNow stub variant                            |
| `ia-stub`                             | Mock IA service                                    |
| `testability-api`                     | Enable test manipulation APIs                      |
| `certificate-analytics-service-active`| Enable analytics publishing                        |

---

## Testing Stack

### Test Framework Distribution

| Metric                                  | Count  |
|-----------------------------------------|--------|
| **Total test files**                    | **~494** (`*Test.java` + `*IT.java`) |
| **JUnit 5 (Jupiter) test files**        | ~329 (66.6%) |
| **JUnit 4 test files**                  | ~162 (32.8%) |
| **Files using both JUnit 4 & 5**        | 1      |
| **Mockito usage**                       | ~362 files |

### Test Distribution by Module

| Module                  | Test Files | Percentage |
|-------------------------|-----------|------------|
| **webcert-web**         | 454       | 86.3%      |
| **notification-sender** | 34        | 6.5%       |
| **persistence**         | 18        | 3.4%       |
| **integration**         | 13        | 2.5%       |
| **common**              | 3         | 0.6%       |
| **logging**             | 2         | 0.4%       |
| **stubs**               | 2         | 0.4%       |

### Spring Test Patterns

- **`@RunWith(SpringJUnit4ClassRunner.class)`** — 18 persistence repository tests (JUnit 4 legacy)
- **`@RunWith(SpringRunner.class)`** — 1 test
- **`@ExtendWith(SpringExtension.class)`** — 2 tests
- **`@SpringBootTest`** — **0** (not used — no Spring Boot application class exists)
- **`@ContextConfiguration`** — 18+ persistence tests with XML context locations

### Test Infrastructure

- **JUnit 5** (Jupiter) — Primary test framework
- **JUnit 4** (via junit-vintage-engine) — Legacy test support (testRuntimeOnly)
- **Mockito** (with Java agent and mockito-junit-jupiter) — Mocking
- **AssertJ** — Fluent assertions
- **Spring Test** (spring-test) — Integration testing
- **Spring Boot Test** (spring-boot-test) — Test utilities only (no `@SpringBootTest` usage)
- **XMLUnit** (xmlunit-legacy) — XML assertion/comparison
- **Awaitility** — Async testing support (notification-sender module)
- **Apache Camel Test** (camel-test-spring-junit5) — Camel route integration testing (notification-sender module)
- **H2** — In-memory database for persistence tests (MySQL compatibility mode)
- **Testcontainers** — **Not used**

### Base Test Classes

- `AbstractBuilderTest` — Draft builder test base
- `AbstractIntygServiceTest` — Certificate service test base
- `BaseCreateDraftCertificateTest` — Draft creation test base
- `BaseCreateDraftCertificateValidatorTest` — Validation test base

### Test Utilities

- `UtkastTestUtil` — Draft entity test data builder
- `FragaSvarTestUtil` — Q&A entity test data builder
- `CertificateEventTestUtil` — Event entity test data builder
- `UnderskriftTestUtil` — Signature test data builder

---

## Quality & CI/CD

- **SonarQube** — Static code analysis (project: `intyg-webcert`)
- **JaCoCo** — Code coverage (HTML + XML reports, excludes testability/integration-test packages)
- **CycloneDX** — Software Bill of Materials (SBOM) generation
- **Spotless** — Code formatting (Google Java Format, license headers)
- **Ben Manes Versions** — Dependency update checking
- **Jenkins** — CI/CD (Jenkins.properties present)

---

## Codebase Statistics

### Source Code

| Metric                              | Count    |
|-------------------------------------|----------|
| **Total production Java files**     | ~1,150   |
| **Total test Java files**           | ~494     |
| **web module production files**     | ~1,031   |
| **persistence module entity files** | 78       |
| **csintegration layer files**       | ~240     |

### Configuration

| Metric                                   | Count   |
|------------------------------------------|---------|
| **Spring XML config files (owned)**      | 24      |
| **Spring XML config files (external)**   | 6+      |
| **CXF servlet config files (WEB-INF)**   | 8       |
| **Java @Configuration classes**          | 18      |
| **web.xml filters**                      | 13      |
| **web.xml servlets**                     | 10      |
| **@Value injection points**              | ~165    |
| **application.properties entries**       | ~365    |
| **Spring profiles**                      | 14+     |

### Dependencies

| Metric                             | Count   |
|------------------------------------|---------|
| **Inera Infra modules**            | 24      |
| **Inera Common modules**           | 21 (+3 test) |
| **Schema libraries**               | 6       |
| **JAX-RS controllers (@Path)**     | ~52     |
| **Spring MVC controllers**         | 1       |
| **SOAP server endpoints**          | 6       |
| **SOAP client endpoints**          | 15+     |
| **JMS queues**                     | 7       |
| **Camel route builders**           | 2       |
| **Scheduled jobs**                 | 5+      |

---

## Key Observations

1. **Not Spring Boot** — This is a traditional Spring Framework + WAR deployment, not Spring Boot. Uses Gretty plugin for local Tomcat 10
   development. The `web.xml` bootstraps the entire application with `ContextLoaderListener` loading `webcert-config.xml`.
2. **JAX-RS-first architecture** — Unlike Rehabstöd (which uses Spring MVC), Webcert's REST APIs are entirely JAX-RS via CXF (52+
   controllers across 8 CXFServlet instances). Only 1 controller uses `@RestController`. This is a **major** migration surface.
3. **Complex multi-servlet design** — 10 servlets with separate Spring contexts create a segmented architecture. Each CXF servlet has its
   own XML config defining which controllers are exposed. This must be consolidated into a single Spring Boot application context.
4. **Heavy XML configuration** — 24+ XML config files define the application wiring. Combined with 18 Java `@Configuration` classes
   (11 in web, 7 in other modules), this creates a mixed configuration approach that needs full Java-based migration.
5. **Apache Camel dependency** — The notification-sender module uses Camel (XML-configured routes) for message routing and aggregation.
   These must be migrated to Java DSL configuration.
6. **Jakarta EE migration completed** — All namespaces use `jakarta.*`, not `javax.*`, indicating a successful migration to Jakarta EE 10+.
7. **Heavy coupling to Inera ecosystem** — 24 infra modules + 21 common modules (+ 3 test-scope) + 6 schema libraries = **51+
   internal Inera dependencies**. This is the largest dependency surface of any Intyg application.
8. **Certificate Service integration layer** — The `csintegration` package (~240 files) represents a significant modern integration with
   `certificate-service` via REST, suggesting an ongoing architectural transition.
9. **Redis-centric session and caching** — Redis serves three purposes: HTTP session store (`@EnableRedisIndexedHttpSession`),
   application caching (HSA, PU, LaunchId, CertificatesForPatient), and distributed lock provider (ShedLock).
10. **SAML 2.0 authentication** — Healthcare-grade authentication via SITHS and e-legitimation with 3 identity providers, custom
    authentication handlers, and YAML-based authority configuration.
11. **JUnit 4 legacy** — ~33% of tests still use JUnit 4 (~162 files), primarily in the web module. The persistence module's 18 repository
    tests use `@RunWith(SpringJUnit4ClassRunner.class)` with XML context configuration.
12. **Stub-based development** — Each external service integration includes a stub implementation activated via Spring profiles, enabling
    local development without external dependencies.
13. **DSS signing integration** — Complex Digital Signing Service integration with SAML-based signing, PKCS12 keystores, and custom
    metadata management (~12 `@Value` properties in `DssMetadataService` alone).
14. **No Testcontainers** — All integration tests use H2 in-memory database. No containerized test infrastructure exists.
15. **Large API surface** — 52+ JAX-RS controllers expose a significant REST API surface that must be migrated to Spring MVC.

---

## Suggested Additional Analysis

1. **Dependency vulnerability scan** — Check the third-party libraries for known CVEs (CycloneDX BOM is already generated).
2. **Database schema review** — Analyze the Liquibase changelogs to understand the data model complexity.
3. **API surface mapping** — Create a complete catalog of all REST and SOAP endpoints with HTTP methods and paths.
4. **Inera Infra usage audit** — For each of the 24 infra modules, determine if they can be replaced by intyg-proxy-service REST APIs
   or internalized.
5. **Certificate type module analysis** — Understand how the 14 certificate type modules (common) integrate and whether they bring
   transitive XML configuration.
6. **Camel route complexity** — Deep-dive into the 2 Camel contexts to understand aggregation strategies, error handling, and
   redelivery patterns.
7. **Redis configuration audit** — Review cache TTL policies, session serialization, and Sentinel/HA setup.
8. **Security filter chain analysis** — Map how the 13 filters interact with each CXF servlet context and Spring Security.
