# Webcert — Goal Tech Stack

*Based on the established patterns in **certificate-service** and **intyg-proxy-service**.*

---

## Rationale

The target services (**certificate-service** and **intyg-proxy-service**) represent the modern standard for the Inera Intyg platform. This
document defines what Webcert's tech stack should look like after migration, derived from what those two services already use and the
acceptance criteria for this migration.

### Acceptance Criteria

1. Running as an executable Spring Boot application (JAR with embedded Tomcat)
2. Using Spring Boot starters and auto-configuration
3. Java-only Spring configuration (no XML)
4. REST API implemented with Spring MVC (no JAX-RS)
5. No dependencies on `se.inera.intyg.infra`
6. JUnit Jupiter only (no JUnit 4 or Vintage Engine)
7. Structured logging in ECS format via Spring Boot
8. Apache Camel routes configured via Java instead of XML

---

## Core Platform

| Aspect               | Current (Webcert)                              | Goal                                                            |
|----------------------|------------------------------------------------|-----------------------------------------------------------------|
| **Language**         | Java 21                                        | **Java 21** — no change                                         |
| **Framework**        | Spring Framework (no Boot)                     | **Spring Boot** — align with both target services               |
| **Build System**     | Gradle (Groovy DSL)                            | **Gradle with Kotlin DSL version catalog** from centralized BOM |
| **Packaging**        | WAR deployed on external Tomcat 10             | **Executable JAR** with embedded server (Spring Boot)           |
| **Containerization** | Docker (WAR into Catalina base image)          | **Docker** (JAR-based Spring Boot image)                        |
| **Dependency Mgmt**  | Inera BOM (`se.inera.intyg.bom:platform`)      | **Inera BOM** — no change (already aligned)                     |
| **Dev Server**       | Gretty 4.1.10 (embedded Tomcat 10)             | **Spring Boot DevTools** — replace Gretty; remove plugin        |

---

## Web & API Layer

| Concern                | Current                                                        | Goal                                                                         |
|------------------------|----------------------------------------------------------------|------------------------------------------------------------------------------|
| **REST API**           | JAX-RS via CXF (~52 controllers, 8 CXFServlet contexts)       | **Spring Boot Starter Web** (Spring MVC) + Jackson — migrate all `@Path`/`@GET`/`@POST`/`@Consumes`/`@Produces` to `@RestController`/`@RequestMapping`/`@GetMapping`/`@PostMapping` |
| **Servlet Architecture** | web.xml with 10 servlets, 13 filters, 4 listeners            | **Single embedded Tomcat** — Spring Boot auto-configured; filters registered as `@Bean FilterRegistrationBean`; eliminate web.xml entirely |
| **SOAP Services**      | Apache CXF (cxf-rt-frontend-jaxws) — 6 server endpoints       | **Apache CXF** — retained, consistent with both target services; configured via Java `@Configuration` instead of XML |
| **SOAP Clients**       | 15+ JAX-WS clients (ws-config.xml, TLS/NTJP)                  | **Apache CXF** JAX-WS clients — configured via Java `@Configuration` with programmatic TLS setup |
| **SOAP Codegen**       | JAXB2 Basics + XJC (manual/pre-generated)                      | **WSDL2Java** plugin (`com.yupzip.wsdl2java`) + JAXB Runtime                 |
| **Outbound REST**      | Spring `RestClient`, `RestTemplate`, Apache HttpClient 5       | **Spring Boot Starter WebFlux** (`WebClient`) — for outbound HTTP integrations; consolidate all REST clients |
| **Health/Metrics**     | Prometheus `simpleclient_servlet` (manual MetricsServlet)      | **Spring Boot Actuator** — standardized health checks, metrics, management   |
| **API Documentation**  | Swagger (swagger-jaxrs, non-prod)                              | **SpringDoc OpenAPI** — Spring MVC native OpenAPI documentation              |

---

## Data & Persistence

| Concern              | Current                                       | Goal                                                       |
|----------------------|-----------------------------------------------|------------------------------------------------------------|
| **ORM**              | Spring Data JPA + Hibernate ORM               | **Spring Data JPA** (via Spring Boot Starter Data JPA)     |
| **Connection Pool**  | HikariCP (explicit dependency, manual config) | **HikariCP** (auto-configured by Spring Boot)              |
| **JPA Config**       | `repository-context.xml` + `JpaConfig` Java   | **Spring Boot auto-configuration** — `spring.datasource.*` and `spring.jpa.*` properties |
| **Schema Migration** | Liquibase                                     | **Liquibase** — auto-configured by Spring Boot             |
| **Production DB**    | MySQL (mysql-connector-j)                     | **MySQL** — no change                                      |
| **Test DB**          | H2 (in-memory, MySQL mode)                    | **Testcontainers MySQL** — real database in tests          |

---

## Messaging

| Concern              | Current                                                              | Goal                                                               |
|----------------------|----------------------------------------------------------------------|--------------------------------------------------------------------|
| **JMS Broker**       | ActiveMQ (activemq-spring, manual config)                            | **Spring Boot Starter ActiveMQ** — auto-configured                 |
| **JMS Config**       | `JmsConfig` Java + `jms-context.xml`                                 | **Spring Boot auto-configuration** — `spring.activemq.*` properties |
| **Camel Routing**    | Apache Camel with XML-configured routes (2 Camel contexts)           | **Spring Boot Starter Camel** with Java DSL routes — `RouteBuilder` beans, no XML contexts |
| **Camel Contexts**   | `webcertNotification` + `webcertCertificateSender` (XML)             | **Single Camel context** managed by Spring Boot Camel auto-configuration |
| **7 JMS Queues**     | Hardcoded queue names in XML and properties                          | **Externalized via `application.yml`** — same queues, Spring Boot managed |
| **JMS Testing**      | —                                                                    | **Testcontainers ActiveMQ** — real broker in integration tests     |

---

## Caching & Scheduling

| Concern              | Current                                                          | Goal                                                                             |
|----------------------|------------------------------------------------------------------|----------------------------------------------------------------------------------|
| **Session Store**    | Spring Session Data Redis (`@EnableRedisIndexedHttpSession`)     | **Spring Session Data Redis** — auto-configured via Spring Boot                  |
| **App Caching**      | Inera Common Redis Cache (common-redis-cache-core, manual)       | **Spring Data Redis** (`spring-boot-starter-data-redis`) — auto-configured       |
| **Cache TTLs**       | Per-cache expiry in application.properties (HSA, PU, LaunchId)   | **Spring Cache abstraction** with Redis — `@Cacheable`/`@CacheEvict` where applicable |
| **Distributed Lock** | ShedLock (shedlock-spring + redis provider)                      | **ShedLock** — retain; required for FMB sync and notification redelivery jobs    |
| **Scheduling**       | `@EnableScheduling` + `@EnableAsync` (manual ThreadPoolTaskExecutor) | **Spring Boot auto-configured** — `spring.task.scheduling.*` and `spring.task.execution.*` |
| **Async Execution**  | `@Async("threadPoolTaskExecutor")` (manual executor bean)        | **`@Async`** with Spring Boot auto-configured `TaskExecutor`                     |

> **Note:** ShedLock is not used in either target service. However, Webcert's scheduled job requirements (FMB data sync, notification
> redelivery, draft locking) with clustered deployment justify keeping it. If these jobs can be redesigned to be idempotent/safe for
> concurrent execution, ShedLock could be removed.

---

## Email

| Concern              | Current                                                     | Goal                                                                     |
|----------------------|-------------------------------------------------------------|--------------------------------------------------------------------------|
| **Mail Sending**     | JavaMailSender (manual config in `mail-config.xml`)         | **Spring Boot Starter Mail** — auto-configured via `spring.mail.*`       |
| **Async Delivery**   | `@Async("threadPoolTaskExecutor")` on MailNotificationServiceImpl | **`@Async`** with Spring Boot auto-configured executor               |
| **Mail Stub**        | Custom mail-stub module (AspectJ AOP interception)          | **Retain** or replace with profile-based no-op implementation            |

---

## Security

| Concern                  | Current                                                                     | Goal                                                                                  |
|--------------------------|-----------------------------------------------------------------------------|---------------------------------------------------------------------------------------|
| **Framework**            | Spring Security 6+ (SecurityFilterChain bean pattern)                       | **Spring Boot Starter Security** — auto-configured, same SecurityFilterChain pattern   |
| **SAML 2.0**             | spring-security-saml2-service-provider (3 IdPs: eleg, siths, sithsNormal)  | **Spring Boot SAML 2.0** — same library, auto-configured via `spring.security.saml2.*` |
| **OpenSAML**             | OpenSAML 4 (manual `OpenSamlConfig` InitializingBean)                       | **OpenSAML 4** — auto-initialized by Spring Security SAML starter                     |
| **CSRF**                 | CookieCsrfTokenRepository                                                   | **CookieCsrfTokenRepository** — no change, configured in SecurityFilterChain          |
| **Session Management**   | Redis-backed sessions (Spring Session)                                       | **Spring Session Data Redis** — auto-configured                                       |
| **Security Filters**     | Inera Infra filters (security-filter, security-siths, etc.) + custom        | **Internalize** — rewrite needed filters as local `@Component` beans; remove infra dependency |
| **Authorities/Roles**    | Inera security-authorities + YAML-based role config                          | **Internalize** — local authority resolution; retain YAML config format               |
| **Feature Flags**        | features.yaml (custom loading)                                               | **Retain** — load via `@ConfigurationProperties` or dedicated configuration bean      |
| **Auth Events**          | `@EventListener` for login/logout (AuthenticationEventListener)              | **`@EventListener`** — no change, already a modern pattern                            |

---

## Logging & Observability

| Concern             | Current                                              | Goal                                                                                                              |
|---------------------|------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------|
| **Logging Backend** | Logback + ECS encoder (logback-ecs-encoder)          | **Logback with ECS structured format** (`logging.structured.format.console=ecs`) — Spring Boot native ECS support |
| **Log Bridging**    | SLF4J (manual)                                       | **SLF4J** (auto-configured by Spring Boot)                                                                        |
| **AOP Logging**     | AspectJ (custom PerformanceLoggingAdvice)            | **AspectJ** with `@PerformanceLogging` — same pattern as target services                                          |
| **MDC**             | Custom MdcServletFilter + MdcUserServletFilter       | **MDC** — aligned with target services' logging module pattern                                                    |
| **Metrics**         | Prometheus simpleclient_servlet (manual)             | **Spring Boot Actuator** (Micrometer) — standard metrics endpoint                                                 |
| **Custom Logback**  | LogbackConfiguratorContextListener (external config) | **Spring Boot Logback** — `logback-spring.xml` with Spring profile support                                        |

---

## XML / SOAP Integration

| Concern             | Current                                  | Goal                                                                           |
|---------------------|------------------------------------------|--------------------------------------------------------------------------------|
| **SOAP Framework**  | Apache CXF (JAX-WS server + client)     | **Apache CXF** — no change; configured via Java `@Configuration`               |
| **CXF Bus**         | XML-configured CXF Bus + LoggingFeature  | **Java-configured** CXF Bus bean with logging feature                          |
| **XML Binding**     | JAXB2 Basics + XJC build-time gen        | **JAXB Runtime** (GlassFish) + **Jakarta XML WS**                              |
| **Code Generation** | XJC Ant task + pre-generated code        | **WSDL2Java plugin** (`com.yupzip.wsdl2java`) — build-time generation          |
| **XML Validation**  | Helger Schematron (test scope)           | **Helger Schematron** — retain if needed (from cert-service)                   |
| **XML Signing**     | xmldsig (Inera Infra module)             | **Internalize** — extract XML signing logic; remove infra dependency           |

---

## Utilities & Developer Productivity

| Concern              | Current                                          | Goal                                                                  |
|----------------------|--------------------------------------------------|-----------------------------------------------------------------------|
| **Boilerplate**      | Lombok                                           | **Lombok** — no change                                                |
| **DTO Mapping**      | Manual                                           | **MapStruct** (with Lombok binding) — from intyg-proxy-service        |
| **General Utils**    | Guava + Commons IO + Commons Lang3               | **Guava** — retain; drop Commons IO and Commons Lang3 if replaceable by standard Java/Guava |
| **Functional**       | Vavr                                             | **Evaluate** — not used in either target service; replace with standard Java Optional/Stream if possible |
| **Search/Analysis**  | Apache Lucene (diagnosis code search)            | **Retain** if diagnosis search is still needed                        |
| **Excel Export**     | jxls-poi                                         | **Retain** if Excel export is still needed                            |
| **JSON**             | Jackson (manual ObjectMapper config as CXF provider) | **Jackson** — auto-configured by Spring Boot; customize via `Jackson2ObjectMapperBuilderCustomizer` |

---

## Inera Ecosystem Dependencies

| Concern              | Current                                                     | Goal                                                                                                                        |
|----------------------|-------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| **Inera Infra**      | **24 modules** (security, HSA, PU, certificate, monitoring, etc.) | **Zero modules** — remove all `se.inera.intyg.infra` dependencies per acceptance criteria                                  |
| **Inera Common**     | **21 impl + 3 test-scope** certificate type modules         | **Retain as needed** — certificate type support is core business logic; these are `se.inera.intyg.common`, not infra        |
| **Schema Libraries** | 6 RIV-TA schema libraries                                   | **Retain** — required for SOAP interoperability                                                                             |
| **Reference Data**   | refdata                                                     | **Retain** — required for ICD-10 codes and code systems                                                                     |

### Infra Module Replacement Strategy

Each of the 24 infra modules must be handled individually:

| Infra Module                            | Strategy                                                                                 |
|-----------------------------------------|------------------------------------------------------------------------------------------|
| **hsa-integration-api**                 | **Replace** — call HSA via intyg-proxy-service REST APIs directly                        |
| **hsa-integration-intyg-proxy-service** | **Replace** — internalize as local REST client using Spring `WebClient`                  |
| **pu-integration-api**                  | **Replace** — call PU via intyg-proxy-service REST APIs directly                         |
| **pu-integration-intyg-proxy-service**  | **Replace** — internalize as local REST client using Spring `WebClient`                  |
| **security-authorities**                | **Internalize** — extract authority resolution logic into local module                   |
| **security-common**                     | **Internalize** — extract common security utilities into local module                    |
| **security-filter**                     | **Internalize** — rewrite filters as local Spring Security components                    |
| **security-siths**                      | **Internalize** — extract SITHS authentication logic into local module                   |
| **certificate**                         | **Internalize** — extract certificate operation utilities into local module              |
| **xmldsig**                             | **Internalize** — extract XML digital signature logic into local module                  |
| **common-redis-cache-core**             | **Replace** — use Spring Data Redis auto-configuration directly                          |
| **log-messages**                        | **Internalize** — extract PDL logging DTOs/enums into local module                       |
| **message**                             | **Internalize** — extract message handling into local module                             |
| **monitoring**                          | **Replace** — use Spring Boot Actuator for monitoring; internalize LogbackConfigurator    |
| **integreradeenheter**                  | **Internalize** — extract integrated unit management into local module                   |
| **intyginfo**                           | **Internalize** — extract certificate info aggregation into local module                 |
| **ia-integration**                      | **Internalize** — extract IA integration client into local module                        |
| **srs-integration**                     | **Internalize** — extract SRS client into local integration module                       |
| **sjukfall-engine**                     | **Internalize** — extract sick leave calculation into local module                       |
| **privatepractitioner**                 | **Internalize** — extract private practitioner logic into local module                   |
| **postnummerservice-integration**       | **Internalize** — extract postal code service client into local module                   |
| **driftbanner-dto**                     | **Internalize** — extract operational banner DTOs into local module                      |
| **dynamiclink**                         | **Internalize** — extract dynamic link generation into local module                      |
| **testcertificate**                     | **Internalize** — extract test certificate utilities into local module (test scope only) |

---

## Testing Stack

| Concern               | Current                                           | Goal                                                                          |
|-----------------------|---------------------------------------------------|-------------------------------------------------------------------------------|
| **Unit Testing**      | JUnit 5 (Jupiter) + JUnit 4 (~33% legacy)         | **JUnit 5 only** — migrate all JUnit 4 tests; remove vintage engine           |
| **Mocking**           | Mockito (Java agent + mockito-junit-jupiter)       | **Mockito** (Java agent) — no change                                          |
| **Assertions**        | AssertJ + JUnit assertions                         | **AssertJ** — standardize on fluent assertions                                |
| **Integration Tests** | Spring Test + H2 + @ContextConfiguration           | **Testcontainers** (MySQL, ActiveMQ, MockServer) — real dependencies in tests |
| **Spring Testing**    | Spring Test (manual context, no @SpringBootTest)   | **Spring Boot Test** (`@SpringBootTest`) — auto-configured test context       |
| **HTTP Mocking**      | —                                                  | **MockServer** and/or **OkHttp MockWebServer** — for external service mocking |
| **Async Testing**     | Awaitility (notification-sender only)              | **Awaitility** — expand usage across all async/JMS test assertions            |
| **Camel Testing**     | camel-test-spring-junit5                           | **Camel Test Spring Boot** — aligned with Spring Boot Camel starter           |
| **Contract Testing**  | —                                                  | **Microcks Testcontainers** — if SOAP contract testing is needed              |
| **XML Assertions**    | XMLUnit                                            | **XMLUnit** — retain if XML comparison is still needed                        |

---

## Quality & CI/CD

| Concern         | Current                  | Goal                                |
|-----------------|--------------------------|-------------------------------------|
| **Coverage**    | JaCoCo                   | **JaCoCo** — no change              |
| **Analysis**    | SonarQube                | **SonarQube** — no change           |
| **SBOM**        | CycloneDX                | **CycloneDX** — no change           |
| **Formatting**  | Spotless (Google Format) | **Spotless** — no change            |
| **CI/CD**       | Jenkins                  | **Jenkins** — no change             |
| **Dep Updates** | Ben Manes Versions       | **Ben Manes Versions** — no change  |

---

## Configuration Architecture (Goal)

| Concern                | Current                                                       | Goal                                                                                |
|------------------------|---------------------------------------------------------------|-------------------------------------------------------------------------------------|
| **Spring Config**      | 24+ XML files + 18 Java @Configuration classes                | **Java @Configuration only** — zero XML; use `@Import`, `@ConditionalOnProfile`     |
| **Properties**         | `application.properties` (~365 entries) + external overrides  | **`application.yml`** with profile-specific variants (`application-dev.yml`, etc.)   |
| **Property Injection** | ~165 `@Value` injection points                                | **`@ConfigurationProperties`** records — type-safe, immutable, IDE-friendly         |
| **Profiles**           | 14+ profiles (some via XML activation)                        | **Spring Boot profiles** — `spring.profiles.active` in application.yml              |
| **Feature Flags**      | features.yaml (custom loading)                                | **Dedicated `@ConfigurationProperties`** bean loading features.yaml                 |
| **Authorities**        | authorities.yaml (custom SecurityConfigurationLoader)         | **Retain YAML** — load via `@ConfigurationProperties` or `@Value` with SnakeYAML    |
| **External Config**    | `dev.config.file` JVM arg, `application.dir` file paths       | **Spring Boot externalized config** — `spring.config.additional-location`, ConfigMaps in K8s |

---

## Module Architecture (Goal)

The target module structure should follow the patterns established in certificate-service (hexagonal/clean architecture with separated
domain) and intyg-proxy-service (dedicated integration modules):

```
webcert (goal)
├── app                                    → Spring Boot application (@SpringBootApplication, controllers, services, config)
├── common                                 → Shared DTOs, exceptions, constants (retain, modernize)
├── domain                                 → Pure business logic (minimal deps: Jackson, SLF4J only)
├── logging                                → Cross-cutting logging/AOP (AspectJ, Logback) — already exists
├── persistence                            → JPA entities, repositories, Liquibase — already exists
├── notification-sender                    → Camel routes (Java DSL), notification/certificate processing
├── integration-intygstjanst               → Intygstjänst SOAP client (CXF)
├── integration-intyg-proxy-service        → HSA/PU via REST (replaces hsa-integration-api + pu-integration-api)
├── integration-fmb                        → FMB REST client (already exists, modernize)
├── integration-servicenow                 → ServiceNow REST client (already exists, modernize)
├── integration-private-practitioner       → Private practitioner REST client (already exists)
├── integration-certificate-analytics      → Analytics JMS publisher (already exists, modernize)
├── integration-certificate-service        → Certificate Service REST client (consolidate csintegration)
├── integration-srs                        → SRS client (extracted from infra)
├── stubs                                  → Stub implementations for local dev (retain, modernize)
└── integration-test                       → Full integration tests with Testcontainers
```

**Key architectural shifts:**
1. **Introduce a domain module** with no framework dependencies (following certificate-service's hexagonal pattern), separating business
   logic from infrastructure concerns.
2. **Dedicated integration modules** for each external service — replaces monolithic web module and infra dependencies.
3. **Consolidate csintegration** (~240 files currently scattered in web module) into a dedicated `integration-certificate-service` module.
4. **Single application context** — eliminate the 8 CXF servlet contexts and web.xml-based wiring.

---

## Summary of Key Changes

| #  | Change                                                              | Impact | Acceptance Criteria |
|----|---------------------------------------------------------------------|--------|---------------------|
| 1  | **Spring Framework → Spring Boot** (executable JAR)                 | Major  | ✅ AC-1, AC-2       |
| 2  | **WAR + external Tomcat → embedded Tomcat JAR**                     | Major  | ✅ AC-1             |
| 3  | **JAX-RS (CXF) → Spring MVC** for ~52 REST controllers             | Major  | ✅ AC-4             |
| 4  | **24 XML config files → Java @Configuration only**                  | Major  | ✅ AC-3             |
| 5  | **web.xml elimination** (10 servlets, 13 filters, 4 listeners)      | Major  | ✅ AC-3             |
| 6  | **Remove all 24 se.inera.intyg.infra modules**                      | Major  | ✅ AC-5             |
| 7  | **JUnit 4 → JUnit 5 only** (~162 tests to migrate)                  | Medium | ✅ AC-6             |
| 8  | **logback-ecs-encoder → Spring Boot native ECS logging**            | Medium | ✅ AC-7             |
| 9  | **Camel XML routes → Java DSL routes**                              | Medium | ✅ AC-8             |
| 10 | **Prometheus manual → Spring Boot Actuator**                        | Medium |                     |
| 11 | **H2 test DB → Testcontainers**                                     | Medium |                     |
| 12 | **Manual Spring config → Spring Boot auto-configuration**           | Medium | ✅ AC-2             |
| 13 | **`@Value` → `@ConfigurationProperties`** records                   | Medium |                     |
| 14 | **CXF SOAP XML config → Java-based CXF config**                    | Medium | ✅ AC-3             |
| 15 | **Swagger JAX-RS → SpringDoc OpenAPI**                              | Low    |                     |
| 16 | **Add MapStruct** for DTO mapping                                   | Low    |                     |
| 17 | **Docker image: WAR/Catalina → JAR/Spring Boot base**               | Medium |                     |
| 18 | **Introduce domain module** (hexagonal architecture)                | Major  |                     |

---

## Items Requiring Decision

These items differ between the current state and the target services, or are unique to Webcert, and need a deliberate decision:

1. **ShedLock** — Keep or remove? Not used in either target service, but Webcert has 5+ scheduled jobs that run in clustered environments.
   Recommendation: **Keep** until jobs can be redesigned.
2. **Vavr** — Keep or remove? Not used in either target service. Used in FMB integration.
   Recommendation: **Remove** — replace with standard Java Optional/Stream.
3. **Commons IO / Commons Lang3** — Keep or replace? Not used in target services.
   Recommendation: **Remove** — replace with standard Java/Guava equivalents.
4. **Apache Lucene** — Keep or remove? Used for diagnosis code search. Not in target services.
   Recommendation: **Evaluate** — keep if search functionality is required.
5. **jxls-poi** — Keep or remove? Used for Excel export. Not in target services.
   Recommendation: **Evaluate** — keep if Excel export is a required feature.
6. **DSS (Digital Signing Service)** — Complex integration with ~12 `@Value` properties. Not in target services.
   Recommendation: **Keep** — this is core business functionality for qualified electronic signatures.
7. **Certificate type modules (Inera Common)** — 21 implementation modules for 14 certificate types.
   Recommendation: **Retain** — these are `se.inera.intyg.common` (not infra) and represent core business logic.
8. **Camel consolidation** — Currently 2 separate Camel contexts. Merge into one or keep separate?
   Recommendation: **Merge** into a single Spring Boot-managed Camel context with separate `RouteBuilder` beans.
9. **Mail stub** — Custom AspectJ-based mail interception. Replace with simpler approach?
   Recommendation: **Replace** with profile-activated no-op `JavaMailSender` or Testcontainers MailHog.
10. **csintegration layer** — ~240 files for certificate-service REST integration. Restructure?
    Recommendation: **Extract** into dedicated `integration-certificate-service` module.
