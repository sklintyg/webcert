# Webcert — Goal Tech Stack

*Based on the established patterns in **certificate-service** and **intyg-proxy-service**.*

---

## Rationale

The target services (**certificate-service** and **intyg-proxy-service**) represent the modern standard for the Inera Intyg platform. This
document defines what Webcert's tech stack should look like after migration, derived from what those two services already use and the
acceptance criteria for this migration.

> **Migration Philosophy:** This migration is a **functional-equivalent lift** to Spring Boot. The goal is to make the existing application
> work under Spring Boot with the same behavior, not to optimize or restructure the code. Existing business logic, service implementations,
> and test code should remain structurally unchanged unless directly required by an acceptance criterion. Avoid introducing new libraries,
> patterns, or architectural changes beyond what the acceptance criteria demand.

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
| **Build System**     | Gradle (Groovy DSL)                            | **Gradle (Groovy DSL)** — no change; Kotlin DSL migration deferred                 |
| **Packaging**        | WAR deployed on external Tomcat 10             | **Executable JAR** with embedded server (Spring Boot)           |
| **Containerization** | Docker (WAR into Catalina base image)          | **Docker** (JAR-based Spring Boot image)                        |
| **Dependency Mgmt**  | Inera BOM (`se.inera.intyg.bom:platform`)      | **Inera BOM** — no change (already aligned)                     |
| **Dev Server**       | Gretty 4.1.10 (embedded Tomcat 10)             | **Spring Boot embedded Tomcat** — replace Gretty; run via `./gradlew bootRun`       |

---

## Web & API Layer

| Concern                | Current                                                        | Goal                                                                         |
|------------------------|----------------------------------------------------------------|------------------------------------------------------------------------------|
| **REST API**           | JAX-RS via CXF (~52 controllers, 8 CXFServlet contexts)       | **Spring Boot Starter Web** (Spring MVC) + Jackson — migrate all `@Path`/`@GET`/`@POST`/`@Consumes`/`@Produces` to `@RestController`/`@RequestMapping`/`@GetMapping`/`@PostMapping` |
| **Servlet Architecture** | web.xml with 10 servlets, 13 filters, 4 listeners            | **Single embedded Tomcat** — Spring Boot auto-configured; filters registered as `@Bean FilterRegistrationBean`; eliminate web.xml entirely |
| **SOAP Services**      | Apache CXF (cxf-rt-frontend-jaxws) — 6 server endpoints       | **Apache CXF** — retained, consistent with both target services; configured via Java `@Configuration` instead of XML |
| **SOAP Clients**       | 15+ JAX-WS clients (ws-config.xml, TLS/NTJP)                  | **Apache CXF** JAX-WS clients — configured via Java `@Configuration` with programmatic TLS setup |
| **SOAP Codegen**       | JAXB2 Basics + XJC (manual/pre-generated)                      | **JAXB2 Basics + XJC** — retain existing code generation approach; WSDL2Java migration deferred |
| **Outbound REST**      | Spring `RestClient`, `RestTemplate`, Apache HttpClient 5       | **Retain existing clients** — no consolidation in this migration; each client continues to work as-is |
| **Health/Metrics**     | Prometheus `simpleclient_servlet` (manual MetricsServlet)      | **Spring Boot Actuator** — standardized health checks, metrics, management   |
| **API Documentation**  | Swagger (swagger-jaxrs, non-prod)                              | **SpringDoc OpenAPI** — replace Swagger JAX-RS with Spring MVC native; required since JAX-RS is removed |

---

## Data & Persistence

| Concern              | Current                                       | Goal                                                       |
|----------------------|-----------------------------------------------|------------------------------------------------------------|
| **ORM**              | Spring Data JPA + Hibernate ORM               | **Spring Data JPA** (via Spring Boot Starter Data JPA)     |
| **Connection Pool**  | HikariCP (explicit dependency, manual config) | **HikariCP** (auto-configured by Spring Boot)              |
| **JPA Config**       | `repository-context.xml` + `JpaConfig` Java   | **Spring Boot auto-configuration** — `spring.datasource.*` and `spring.jpa.*` properties |
| **Schema Migration** | Liquibase                                     | **Liquibase** — auto-configured by Spring Boot             |
| **Production DB**    | MySQL (mysql-connector-j)                     | **MySQL** — no change                                      |
| **Test DB**          | H2 (in-memory, MySQL mode)                    | **H2 (in-memory, MySQL mode)** — retained; Testcontainers deferred to future iteration |

---

## Messaging

| Concern              | Current                                                              | Goal                                                               |
|----------------------|----------------------------------------------------------------------|--------------------------------------------------------------------|
| **JMS Broker**       | ActiveMQ (activemq-spring, manual config)                            | **Spring Boot Starter ActiveMQ** — auto-configured                 |
| **JMS Config**       | `JmsConfig` Java + `jms-context.xml`                                 | **Spring Boot auto-configuration** — `spring.activemq.*` properties |
| **Camel Routing**    | Apache Camel with XML-configured routes (2 Camel contexts)           | **Spring Boot Starter Camel** with Java DSL routes — `RouteBuilder` beans, no XML contexts |
| **Camel Contexts**   | `webcertNotification` + `webcertCertificateSender` (XML)             | **Single Camel context** managed by Spring Boot Camel auto-configuration |
| **7 JMS Queues**     | Hardcoded queue names in XML and properties                          | **Externalized via `application.properties`** — same queues, Spring Boot managed |
| **JMS Testing**      | —                                                                    | **Existing test setup retained** — no new test infrastructure in this migration          |

---

## Caching & Scheduling

| Concern              | Current                                                          | Goal                                                                             |
|----------------------|------------------------------------------------------------------|----------------------------------------------------------------------------------|
| **Session Store**    | Spring Session Data Redis (`@EnableRedisIndexedHttpSession`)     | **Spring Session Data Redis** — auto-configured via Spring Boot                  |
| **App Caching**      | Inera Common Redis Cache (common-redis-cache-core, manual)       | **Spring Data Redis** (`spring-boot-starter-data-redis`) — replace infra module; retain existing cache logic as-is |
| **Cache TTLs**       | Per-cache expiry in application.properties (HSA, PU, LaunchId)   | **Retain existing cache pattern** — same TTL properties, configured via Spring Boot Redis auto-configuration      |
| **Distributed Lock** | ShedLock (shedlock-spring + redis provider)                      | **ShedLock** — retain; required for FMB sync and notification redelivery jobs    |
| **Scheduling**       | `@EnableScheduling` + `@EnableAsync` (manual ThreadPoolTaskExecutor) | **Retain existing scheduling** — `@EnableScheduling`, `@EnableAsync`, same `ThreadPoolTaskExecutor` bean         |
| **Async Execution**  | `@Async("threadPoolTaskExecutor")` (manual executor bean)        | **Retain** — same `@Async` usage; executor bean defined in Java `@Configuration`                                 |

> **Note:** ShedLock is not used in either target service. However, Webcert's scheduled job requirements (FMB data sync, notification
> redelivery, draft locking) with clustered deployment justify keeping it. If these jobs can be redesigned to be idempotent/safe for
> concurrent execution, ShedLock could be removed.

---

## Email

| Concern              | Current                                                     | Goal                                                                     |
|----------------------|-------------------------------------------------------------|--------------------------------------------------------------------------|
| **Mail Sending**     | JavaMailSender (manual config in `mail-config.xml`)         | **Spring Boot Starter Mail** — auto-configured via `spring.mail.*`       |
| **Async Delivery**   | `@Async("threadPoolTaskExecutor")` on MailNotificationServiceImpl | **Retain** — same `@Async` usage; executor bean defined in Java `@Configuration` |
| **Mail Stub**        | Custom mail-stub module (AspectJ AOP interception)          | **Retain** — keep existing stub implementation; activate via Spring profile   |

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
| **Authorities/Roles**    | Inera security-authorities + YAML-based role config                          | **Internalize** — extract authority resolution logic into local module; retain YAML config format |
| **Feature Flags**        | features.yaml (custom loading)                                               | **Retain** — keep existing loading mechanism; convert XML config to Java if applicable |
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
| **XML Binding**     | JAXB2 Basics + XJC build-time gen        | **JAXB2 Basics + XJC** — retain existing code generation; no change            |
| **Code Generation** | XJC Ant task + pre-generated code        | **Retain** — keep existing XJC/Ant-based code generation approach              |
| **XML Validation**  | Helger Schematron (test scope)           | **Retain** — keep if used in tests                                             |
| **XML Signing**     | xmldsig (Inera Infra module)             | **Internalize** — extract XML signing logic; remove infra dependency           |

---

## Utilities & Developer Productivity

| Concern              | Current                                          | Goal                                                                  |
|----------------------|--------------------------------------------------|-----------------------------------------------------------------------|
| **Boilerplate**      | Lombok                                           | **Lombok** — no change                                                |
| **DTO Mapping**      | Manual                                           | **Manual** — no MapStruct introduced in this migration                |
| **General Utils**    | Guava + Commons IO + Commons Lang3               | **Retain all** — no library removals in this migration                |
| **Functional**       | Vavr                                             | **Retain** — keep existing usage; removal deferred to future iteration |
| **Search/Analysis**  | Apache Lucene (diagnosis code search)            | **Retain** — no change                                                |
| **Excel Export**     | jxls-poi                                         | **Retain** — no change                                                |
| **JSON**             | Jackson (manual ObjectMapper config as CXF provider) | **Jackson** — auto-configured by Spring Boot; retain existing ObjectMapper customizations as `@Bean` in Java `@Configuration` |

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
| **hsa-integration-intyg-proxy-service** | **Replace** — internalize as local REST client using Spring `RestClient`                 |
| **pu-integration-api**                  | **Replace** — call PU via intyg-proxy-service REST APIs directly                         |
| **pu-integration-intyg-proxy-service**  | **Replace** — internalize as local REST client using Spring `RestClient`                 |
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
| **Assertions**        | AssertJ + JUnit assertions                         | **Retain both** — no assertion library standardization in this migration      |
| **Integration Tests** | Spring Test + H2 + @ContextConfiguration           | **Spring Boot Test** (`@SpringBootTest`) — auto-configured test context; H2 retained; existing test structure preserved |
| **Spring Testing**    | Spring Test (manual context, no @SpringBootTest)   | **Spring Boot Test** (`@SpringBootTest`) — auto-configured test context       |
| **HTTP Mocking**      | —                                                  | **No change** — no new test infrastructure introduced in this migration       |
| **Async Testing**     | Awaitility (notification-sender only)              | **Retain** — keep existing Awaitility usage as-is                             |
| **Camel Testing**     | camel-test-spring-junit5                           | **Retain** — keep existing camel-test-spring-junit5; verify compatibility     |
| **Contract Testing**  | —                                                  | **Evaluate separately** — out of scope for this migration                     |
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
| **Properties**         | `application.properties` (~365 entries) + external overrides  | **`application.properties`** — retain properties format; add profile-specific variants (`application-dev.properties`, etc.) |
| **Property Injection** | ~165 `@Value` injection points                                | **Retain `@Value`** — keep existing injection pattern; `@ConfigurationProperties` migration deferred |
| **Profiles**           | 14+ profiles (some via XML activation)                        | **Spring Boot profiles** — `spring.profiles.active`; same profile names retained |
| **Feature Flags**      | features.yaml (custom loading)                                | **Retain** — keep existing YAML loading mechanism                               |
| **Authorities**        | authorities.yaml (custom SecurityConfigurationLoader)         | **Retain** — keep existing YAML loading mechanism                               |
| **External Config**    | `dev.config.file` JVM arg, `application.dir` file paths       | **Spring Boot externalized config** — `spring.config.additional-location`, ConfigMaps in K8s |

---

## Module Architecture (Goal)

The target module structure retains the existing layout with minimal changes. The key change is introducing an `app` module
for the Spring Boot application bootstrap and converting the `web` module into a library dependency:

```
webcert (goal)
├── app                                    → NEW: Spring Boot application (@SpringBootApplication, main class, boot config)
├── web                                    → Retained: controllers (migrated to Spring MVC), services, config (Java only)
├── common                                 → Retained: shared DTOs, exceptions, constants — no change
├── logging                                → Retained: cross-cutting logging/AOP — no change
├── persistence                            → Retained: JPA entities, repositories, Liquibase — no change
├── notification-sender                    → Retained: Camel routes (converted to Java DSL), notification processing
├── integration                            → Retained: existing integration submodules — no change
├── integration-fmb                        → Retained — no change
├── integration-servicenow                 → Retained — no change
├── integration-private-practitioner       → Retained — no change (already separate module)
├── integration-certificate-analytics      → Retained — no change (already separate module)
├── stubs                                  → Retained: stub implementations for local dev — no change
└── tools                                  → Retained: build/migration tooling — no change
```

**Key changes:**
1. **New `app` module** — Spring Boot bootstrap (`@SpringBootApplication`, `main()`, Dockerfile, Spring Boot config).
2. **`web` module becomes a library** — no longer produces WAR; controllers migrated from JAX-RS to Spring MVC; all XML config eliminated.
3. **Single application context** — eliminate the 8 CXF servlet contexts and web.xml-based wiring.
4. **Existing module boundaries preserved** — no domain extraction, no csintegration restructuring, no integration module reorganization in this iteration.

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
| 11 | **H2 retained for tests** — no Testcontainers in this migration     | Low    |                     |
| 12 | **Manual Spring config → Spring Boot auto-configuration**           | Medium | ✅ AC-2             |
| 13 | **CXF SOAP XML config → Java-based CXF config**                    | Medium | ✅ AC-3             |
| 14 | **Swagger JAX-RS → SpringDoc OpenAPI** (required by JAX-RS removal) | Low    |                     |
| 15 | **Docker image: WAR/Catalina → JAR/Spring Boot base**               | Medium |                     |

---

## Items Requiring Decision

These items are unique to Webcert or have multiple reasonable approaches for this migration:

1. **ShedLock** — Keep or remove? Not used in either target service, but Webcert has 5+ scheduled jobs that run in clustered environments.
   Recommendation: **Keep** until jobs can be redesigned.
2. **Apache Lucene** — Keep or remove? Used for diagnosis code search. Not in target services.
   Recommendation: **Keep** — diagnosis search is existing functionality.
3. **jxls-poi** — Keep or remove? Used for Excel export. Not in target services.
   Recommendation: **Keep** — Excel export is existing functionality.
4. **DSS (Digital Signing Service)** — Complex integration with ~12 `@Value` properties. Not in target services.
   Recommendation: **Keep** — this is core business functionality for qualified electronic signatures.
5. **Certificate type modules (Inera Common)** — 21 implementation modules for 14 certificate types.
   Recommendation: **Retain** — these are `se.inera.intyg.common` (not infra) and represent core business logic.
6. **Camel consolidation** — Currently 2 separate Camel contexts. Merge into one or keep separate?
   Recommendation: **Merge** into a single Spring Boot-managed Camel context with separate `RouteBuilder` beans.
