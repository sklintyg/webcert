# Webcert — First Migration Scope

*Target: Spring Boot application with zero XML configuration, no `se.inera.intyg.infra` dependencies, Spring MVC REST APIs, JUnit 5 only,
Apache Camel routes in Java DSL, and Spring Boot structured ECS logging.*

---

## 1. Objective

Complete the first migration step towards the [goal tech stack](goal-tech-stack.md). After this migration, Webcert will:

1. **Run as a Spring Boot application** — executable JAR with embedded Tomcat (no external WAR deployment).
2. **Use Spring Boot starters and auto-configuration** — replacing manual bean wiring for JPA, JMS, Redis, and metrics.
3. **Have zero XML-based Spring configuration** — all XML config files (local and infra-imported) eliminated and replaced with Java
   `@Configuration` classes or Spring Boot auto-configuration.
4. **Expose REST APIs via Spring MVC** — all ~52 JAX-RS controllers migrated to `@RestController`/`@RequestMapping`.
5. **Have no dependencies on `se.inera.intyg.infra`** — all infra functionality either inlined into the project, replaced with Spring Boot
   equivalents, or accessed via direct REST API calls. The `infraVersion` property is removed from `build.gradle`.
6. **Use JUnit Jupiter exclusively** — no JUnit 4 tests or vintage engine.
7. **Use Spring Boot structured logging in ECS format** — replacing the manual logback-ecs-encoder setup.
8. **Configure Apache Camel routes via Java DSL** — replacing Camel XML contexts with `RouteBuilder` beans managed by Spring Boot Camel
   auto-configuration.

> **Explicitly out of scope for this migration:** module restructuring (hexagonal/domain module), Testcontainers, MapStruct, Gradle Kotlin
> DSL migration, WSDL2Java plugin migration, `@Value` → `@ConfigurationProperties` migration, library removals (Vavr, Commons IO/Lang3),
> REST client consolidation, and any changes to `se.inera.intyg.common` or schema library dependencies.

---

## 2. Current State Summary

| Aspect                         | Current State                                                                                                                                                                                                                                                  |
|--------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Application type**           | Traditional Spring Framework WAR deployed on external Tomcat 10 via Gretty; bootstrapped by `web.xml` with `ContextLoaderListener` loading `webcert-config.xml` as root Spring context; 9 servlets (1 DispatcherServlet + 7 CXF + 1 Prometheus), 13 filters, 4 listeners |
| **REST APIs**                  | JAX-RS (`jakarta.ws.rs` via Apache CXF) — ~52 controllers across 8 CXFServlet contexts (`/api/*`, `/moduleapi/*`, `/internalapi/*`, `/visa/*`, `/webcert/web/user/*`, `/testability/*`, `/authtestability/*`). Only 1 existing `@RestController` (UnansweredCommunicationController) |
| **SOAP endpoints**             | Apache CXF (`jaxws:endpoint`) — 6 server endpoints configured in `services-cxf-servlet.xml` at `/services/*`; 15+ JAX-WS outbound clients configured in `ws-config.xml` with TLS/NTJP |
| **Bean configuration**         | 24+ XML files + 18 Java `@Configuration` classes. Root context loads `webcert-config.xml` which imports module configs, infra classpath XMLs, and Camel contexts. Each CXF servlet has its own XML application context under `WEB-INF/` |
| **`se.inera.intyg.infra` deps**| **24 modules** across 4 build.gradle files: web (24), notification-sender (5), common (1), stubs/notification-stub (1). Heaviest usage: security-common (167 files), security-authorities (96 files), monitoring (34 files) |
| **Test framework**             | Mix of JUnit 4 (~162 files, ~33%) and JUnit 5 (~332 files) across web and persistence modules; `junit-vintage-engine` present |
| **Logging**                    | Logback + `co.elastic.logging:logback-ecs-encoder` with infra's `LogbackConfiguratorContextListener` and custom `logback-spring-base.xml`; local `MdcServletFilter` + `MdcUserServletFilter` for MDC |
| **Metrics**                    | Prometheus `simpleclient_servlet` with `MetricsServlet` mapped to `/metrics` in web.xml; `@EnablePrometheusTiming` from infra monitoring for method-level timing; custom `HealthMonitor` with Prometheus Gauge metrics |
| **Persistence config**         | Manual `JpaConfig` with `@EnableJpaRepositories`; `repository-context.xml` with `tx:annotation-driven`; explicit HikariCP dependency |
| **JMS config**                 | Manual `JmsConfig` Java class + `jms-context.xml` with ActiveMQ `ConnectionFactory`, `PooledConnectionFactory`, `JmsTemplate` beans |
| **Redis config**               | Inera `common-redis-cache-core` via `basic-cache-config.xml`; `@EnableRedisIndexedHttpSession`; `RedisLaunchIdCacheConfiguration` + `CertificatesForPatientCacheConfiguration` with custom TTLs |
| **Camel**                      | 2 XML-configured Camel contexts (`webcertNotification`, `webcertCertificateSender`) in notification-sender module with RouteBuilder classes |
| **Security**                   | Spring Security 6+ with SAML 2.0 (3 IdPs) — already Java-based (`WebSecurityConfig`); infra security modules provide filters, authorities, user models |

---

## 3. Migration Work Items

### 3.1 Spring Boot Application Bootstrap

**What changes:**

- Create a new `app` module with `@SpringBootApplication` main class (`WebcertApplication`).
- Add the Spring Boot Gradle plugin to the `app` module's `build.gradle`.
- Convert the `web` module from WAR (Gretty plugin) to a library JAR.
- Replace `web.xml` servlet/filter/listener declarations with Spring Boot auto-configuration and `FilterRegistrationBean`/
  `ServletRegistrationBean` where needed.
- Remove the Gretty plugin configuration from `web/build.gradle`.
- Update the `Dockerfile` to use a Spring Boot JAR base image instead of deploying a WAR into Catalina.

**Files affected:**

- New: `app/build.gradle` — Spring Boot plugin, `bootJar` configuration, dependencies on all other modules
- New: `app/src/main/java/.../WebcertApplication.java` — `@SpringBootApplication` main class
- `web/build.gradle` — remove `war` + `org.gretty` plugins; becomes a plain `java-library`
- `build.gradle` — add Spring Boot plugin to root project (apply false)
- `settings.gradle` — add `app` module
- Remove: `web/src/main/webapp/WEB-INF/web.xml`
- `Dockerfile` — change from WAR/Catalina to JAR-based

**Spring Boot starters to add:**

- `spring-boot-starter-web` (embedded Tomcat + Spring MVC + Jackson)
- `spring-boot-starter-data-jpa` (replaces manual JPA/Hibernate/HikariCP config)
- `spring-boot-starter-activemq` (replaces manual ActiveMQ connection factory config)
- `spring-boot-starter-actuator` (replaces manual Prometheus servlet)
- `spring-boot-starter-data-redis` (replaces manual Redis config)
- `spring-boot-starter-mail` (replaces manual JavaMailSender config)
- `spring-boot-starter-security` (auto-configured Spring Security)
- `spring-boot-starter-session` (auto-configured Spring Session)
- `camel-spring-boot-starter` (auto-configured Apache Camel)

**Dependencies to remove (replaced by starters):**

- `org.springframework:spring-webmvc` (provided by `starter-web`)
- `org.springframework:spring-jms` (provided by `starter-activemq`)
- `org.springframework.data:spring-data-jpa` (provided by `starter-data-jpa`)
- `com.zaxxer:HikariCP` (auto-configured by `starter-data-jpa`)
- `io.prometheus:simpleclient_servlet_jakarta` / `simpleclient_hotspot` (replaced by Actuator + Micrometer)
- `ch.qos.logback:logback-classic` (provided by `starter-web`)
- `co.elastic.logging:logback-ecs-encoder` (replaced by Spring Boot native ECS)
- `com.fasterxml.jackson.jakarta.rs:jackson-jakarta-rs-json-provider` (replaced by Spring MVC Jackson auto-config)
- `jakarta.ws.rs:jakarta.ws.rs-api` (no longer needed after JAX-RS removal)

---

### 3.2 Eliminate All XML Bean Configuration

**What changes:**

Each XML configuration file must be converted to Java `@Configuration` classes or removed entirely when Spring Boot auto-configuration
covers the concern.

#### Local XML files:

| XML File                                               | Action                                                                                                   |
|--------------------------------------------------------|----------------------------------------------------------------------------------------------------------|
| `webcert-config.xml`                                   | **Remove.** Replace with component scanning via `@SpringBootApplication`, `@Import`, and Java config.    |
| `ws-config.xml`                                        | **Convert** to Java `@Configuration` — programmatic CXF endpoint/client registration with TLS setup.     |
| `mail-config.xml`                                      | **Remove.** Replaced by `spring-boot-starter-mail` auto-configuration via `spring.mail.*` properties.    |
| `test.xml`                                             | **Convert** to Java `@Configuration` with `@Profile("dev")` or equivalent profile.                       |
| `swagger-api-context.xml`                              | **Remove.** Swagger JAX-RS replaced by SpringDoc OpenAPI (auto-discovered from Spring MVC controllers).  |
| `webcert-testability-api-context.xml`                  | **Convert** to Java `@Configuration` with profile activation.                                             |
| `repository-context.xml`                               | **Remove.** Replaced by Spring Boot JPA auto-configuration + `@EnableJpaRepositories`.                    |
| `webcert-common-config.xml`                            | **Remove.** Component scanning handled by `@SpringBootApplication`.                                       |
| `notification-sender-config.xml`                       | **Convert** — import Camel and JMS config via Java `@Configuration` + `@Import`.                          |
| `jms-context.xml`                                      | **Remove.** Replaced by `spring-boot-starter-activemq` auto-configuration.                                |
| `notifications/camel-context.xml`                      | **Convert** to Java DSL — `RouteBuilder` beans managed by Camel Spring Boot starter.                      |
| `notifications/beans-context.xml`                      | **Convert** to Java `@Configuration` — register notification processing beans.                             |
| `notifications/ws-context.xml`                         | **Convert** to Java `@Configuration` — CXF WS client beans.                                               |
| `certificates/camel-context.xml`                       | **Convert** to Java DSL — `RouteBuilder` beans.                                                            |
| `certificates/beans-context.xml`                       | **Convert** to Java `@Configuration` — register certificate sender beans.                                  |
| `fmb-services-config.xml`                              | **Convert** to Java `@Configuration` or component scanning.                                                |
| `fmb-stub-context.xml`                                 | **Convert** to Java `@Configuration` with `@Profile`.                                                      |
| `servicenow-services-config.xml`                       | **Convert** to Java `@Configuration` or component scanning.                                                |
| `servicenow-stub-context.xml`                          | **Convert** to Java `@Configuration` with `@Profile`.                                                      |
| `integration-certificate-analytics-service-config.xml` | **Convert** to Java `@Configuration` or component scanning.                                                |
| `integration-private-practitioner-service-config.xml`  | **Convert** to Java `@Configuration` or component scanning.                                                |
| `mail-stub-context.xml`                                | **Convert** to Java `@Configuration` with `@Profile`.                                                      |
| `mail-stub-testability-api-context.xml`                | **Convert** to Java `@Configuration` with `@Profile`.                                                      |
| `notification-stub-context.xml`                        | **Convert** to Java `@Configuration` with `@Profile`.                                                      |
| `logback/logback-spring-base.xml`                      | **Remove.** Replaced by Spring Boot native ECS support (see §3.7).                                         |

#### CXF servlet context XML files (under WEB-INF/):

| XML File                            | Action                                                                         |
|-------------------------------------|--------------------------------------------------------------------------------|
| `api-cxf-servlet.xml`               | **Remove.** Controllers migrated to Spring MVC `@RestController` (see §3.3).  |
| `moduleapi-cxf-servlet.xml`         | **Remove.** Controllers migrated to Spring MVC.                                |
| `internalapi-cxf-servlet.xml`       | **Remove.** Controllers migrated to Spring MVC.                                |
| `integration-cxf-servlet.xml`       | **Remove.** Controllers migrated to Spring MVC.                                |
| `uthopp-integration-cxf-servlet.xml`| **Remove.** Controllers migrated to Spring MVC.                                |
| `testability-cxf-servlet.xml`       | **Remove.** Controllers migrated to Spring MVC.                                |
| `authtestability-cxf-servlet.xml`   | **Remove.** Controllers migrated to Spring MVC.                                |
| `services-cxf-servlet.xml`          | **Convert** to Java `@Configuration` — SOAP endpoints remain as CXF.          |

#### Imported classpath XML files (from infra/common dependencies):

| XML Import                                              | Action                                                                                 |
|---------------------------------------------------------|----------------------------------------------------------------------------------------|
| `classpath:basic-cache-config.xml`                      | **Remove.** Replaced by `spring-boot-starter-data-redis` auto-configuration.           |
| `classpath:ia-services-config.xml`                      | **Remove.** IA integration internalized into project (see §3.5).                       |
| `classpath:srs-services-config.xml`                     | **Remove.** SRS integration internalized into project (see §3.5).                      |
| `classpath:xmldsig-config.xml`                          | **Remove.** XML signature service internalized into project (see §3.5).                |
| `classpath:ia-stub-context.xml`                         | **Remove.** Internalized with profile activation.                                       |
| `classpath:srs-stub-context.xml`                        | **Remove.** Internalized with profile activation.                                       |
| `classpath:/hsa-integration-intyg-proxy-service-config.xml` | **Remove.** Replaced by local REST client (see §3.5).                             |
| `classpath:/pu-integration-intyg-proxy-service-config.xml`  | **Remove.** Replaced by local REST client (see §3.5).                             |
| `classpath:common-config.xml`                           | **Evaluate** contents; inline needed beans.                                             |
| `classpath*:module-config.xml`                          | **Evaluate** contents; inline needed beans.                                             |
| `classpath*:wc-module-cxf-servlet.xml`                  | **Evaluate** contents; inline needed beans or convert to Java.                          |

#### Test XML files:

| XML File                                                    | Action                                                                              |
|-------------------------------------------------------------|--------------------------------------------------------------------------------------|
| `DiagnosServiceTest-context.xml`                            | **Remove.** Replace with `@Configuration` inner class or `@SpringBootTest`.          |
| `DiagnosRepositoryFactoryTest-context.xml`                  | **Remove.** Replace with `@Configuration` inner class or `@SpringBootTest`.          |
| `unit-test-notification-sender-config.xml`                  | **Remove.** Replace with Java test configuration.                                     |
| `unit-test-certificate-sender-config.xml`                   | **Remove.** Replace with Java test configuration.                                     |
| `integration-test-certificate-sender-config.xml`            | **Remove.** Replace with Java test configuration.                                     |
| `integration-test-broker-context.xml`                       | **Remove.** Replace with Java test configuration.                                     |

---

### 3.3 REST APIs: JAX-RS → Spring MVC

**What changes:**

All REST controllers currently using JAX-RS annotations must be converted to Spring MVC annotations.

| JAX-RS                                  | Spring MVC                                                         |
|-----------------------------------------|--------------------------------------------------------------------|
| `@Path("/...")`                         | `@RestController` + `@RequestMapping("/...")`                      |
| `@GET`                                  | `@GetMapping`                                                      |
| `@POST`                                 | `@PostMapping`                                                     |
| `@PUT`                                  | `@PutMapping`                                                      |
| `@DELETE`                               | `@DeleteMapping`                                                   |
| `@Produces(MediaType.APPLICATION_JSON)` | `produces = MediaType.APPLICATION_JSON_VALUE` (or rely on default) |
| `@Consumes(MediaType.APPLICATION_JSON)` | `consumes = MediaType.APPLICATION_JSON_VALUE` (or rely on default) |
| `@PathParam`                            | `@PathVariable`                                                    |
| `@QueryParam`                           | `@RequestParam`                                                    |
| `jakarta.ws.rs.core.Response`           | `ResponseEntity<T>`                                                |

**Controllers to migrate (~52 files across 8 CXF servlet contexts):**

*API controllers (`/api/*`) — 11 controllers:*
- `JsLogApiController`, `UserApiController`, `FmbApiController`, `SrsApiController`, `ConfigApiController`,
  `SignatureApiController`, `SessionStatusController`, `SubscriptionController`, `InvalidateSessionApiController`,
  `FakeSignatureApiController`, `PrivatePractitionerApiController`

*Facade controllers (`/api/*` — facade layer) — 11 controllers:*
- `CertificateController`, `QuestionController`, `UserController`, `IcfController`, `FMBController`, `LogController`,
  `ConfigController`, `PatientController`, `ListController`, `ListConfigController`, `CertificateTypeController`

*Module API controllers (`/moduleapi/*`) — 3 controllers:*
- `IntygModuleApiController`, `StatModuleApiController`, `DiagnosModuleApiController`

*Internal API controllers (`/internalapi/*`) — 8 controllers:*
- `IntegratedUnitsApiController`, `IntygInfoApiController`, `TestCertificateController`, `TermsApiController`,
  `EraseApiController`, `UnansweredCommunicationController`, `CertificateInternalApiController`, `NotificationController`

*Integration controllers (`/visa/*`, `/v2/visa/*`) — 2 controllers:*
- `IntygIntegrationController`, `UserIntegrationController`

*Legacy integration controllers (`/webcert/web/user/*`) — 4 controllers:*
- `FragaSvarUthoppController`, `PrivatePractitionerFragaSvarUthoppController`, `CertificateIntegrationController`,
  `LaunchIntegrationController`

*Testability controllers (`/testability/*`) — 12 controllers:*
- `UserAgreementResource`, `ReferensResource`, `LogResource`, `IntygResource`, `IntegreradEnhetResource`,
  `FragaSvarResource`, `FmbResource`, `FakeLoginTestabilityController`, `CertificateTestabilityController`,
  `EventResource`, `ConfigurationResource`, `ArendeResource`

*Auth testability (`/authtestability/*`) — 1 controller:*
- `UserResource`

**Notes:**
- The SOAP endpoints (CXF `jaxws:endpoint` at `/services/*`) are **not** being converted to Spring MVC. They remain as CXF endpoints but
  configured via Java instead of XML.
- After migration, the CXF servlet is only needed for `/services/*` (SOAP). All REST traffic goes through Spring MVC `DispatcherServlet`.
- Filters currently scoped to specific CXF servlet patterns (e.g., `unitSelectedAssuranceFilter` on `/api/*`, `/moduleapi/*`) must be
  re-registered as `FilterRegistrationBean` with the same URL patterns.

---

### 3.4 Auto-Configuration Replacements

**What changes:**

Replace manually configured beans with Spring Boot auto-configuration.

#### 3.4.1 JPA / DataSource (replaces `JpaConfig` + `repository-context.xml`)

- **Remove:** `JpaConfig` (persistence module), `repository-context.xml`.
- **Replace with:** `spring-boot-starter-data-jpa` auto-configuration.
- **Configure via `application.properties`:**
  ```properties
  spring.datasource.url=...
  spring.datasource.username=...
  spring.datasource.password=...
  spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
  spring.datasource.hikari.maximum-pool-size=...
  spring.jpa.hibernate.ddl-auto=...
  spring.jpa.properties.hibernate.dialect=...
  ```
- **Liquibase:** Auto-configured by Spring Boot when Liquibase is on the classpath. Set
  `spring.liquibase.change-log=classpath:changelog/changelog.xml`.
- **Entity scanning:** Use `@EntityScan` on the main application class.

#### 3.4.2 JMS / ActiveMQ (replaces `JmsConfig` + `jms-context.xml`)

- **Remove:** The manual `ConnectionFactory`, `PooledConnectionFactory`, `JmsTemplate`, `JmsTransactionManager` beans.
- **Replace with:** `spring-boot-starter-activemq` auto-configuration.
- **Configure via `application.properties`:**
  ```properties
  spring.activemq.broker-url=...
  spring.activemq.user=...
  spring.activemq.password=...
  spring.activemq.pool.enabled=true
  ```
- **JMS listener factory:** Auto-configured by Spring Boot. Custom settings via properties.

#### 3.4.3 Metrics / Health (replaces Prometheus servlet + `HealthMonitor`)

- **Remove:** `io.prometheus:simpleclient_servlet_jakarta`, `simpleclient_hotspot` dependencies and the `MetricsServlet` in web.xml.
- **Remove:** `@EnablePrometheusTiming` from `LoggingConfig` (infra monitoring dependency).
- **Replace with:** `spring-boot-starter-actuator` with Micrometer Prometheus registry.
- **Configure via `application.properties`:**
  ```properties
  management.endpoints.web.exposure.include=health,info,prometheus
  management.metrics.export.prometheus.enabled=true
  ```
- **Custom `HealthMonitor` metrics:** Retain and convert to use Micrometer `MeterRegistry` instead of Prometheus `simpleclient`.

#### 3.4.4 Redis / Caching (replaces `CacheConfig` + `basic-cache-config.xml`)

- **Remove:** `CacheConfig` (if only wrapping infra XML import), `basic-cache-config.xml` import.
- **Replace with:** `spring-boot-starter-data-redis` auto-configuration.
- **Configure via `application.properties`:**
  ```properties
  spring.data.redis.host=...
  spring.data.redis.port=...
  spring.data.redis.password=...
  ```
- **Retain:** `RedisLaunchIdCacheConfiguration` and `CertificatesForPatientCacheConfiguration` — these are custom cache beans with specific
  TTLs that go beyond auto-configuration. They will use the auto-configured `RedisConnectionFactory`.

#### 3.4.5 Mail (replaces `mail-config.xml`)

- **Remove:** `mail-config.xml`.
- **Replace with:** `spring-boot-starter-mail` auto-configuration.
- **Configure via `application.properties`:**
  ```properties
  spring.mail.host=...
  spring.mail.port=...
  ```
- **Retain:** Existing `MailNotificationServiceImpl` and mail stub module — these are business logic, not configuration.

---

### 3.5 Remove All `se.inera.intyg.infra` Dependencies

**What changes:**

Every `se.inera.intyg.infra` dependency must be removed. The functionality they provide is either inlined, replaced with standard
Spring Boot equivalents, or accessed via REST API calls to `intyg-proxy-service`.

| Infra Module                            | Files Using | Replacement Strategy                                                                                        |
|-----------------------------------------|-------------|-------------------------------------------------------------------------------------------------------------|
| **security-common** (~167 files)        | web         | **Internalize.** Copy user models (`HoSPerson`, `CareUnit`, `CareProvider`, `UserOrigin`, etc.) into local module. This is the largest and most pervasive dependency. |
| **security-authorities** (~96 files)    | web, notif. | **Internalize.** Copy `SecurityConfigurationLoader`, `CommonAuthoritiesResolver`, `AuthoritiesHelper`, role/privilege enums into local module. Retain YAML config loading. |
| **monitoring** (~34 files)              | web, notif. | **Internalize** `@PrometheusTimeMethod` annotation and AOP aspect. Convert to Micrometer `@Timed` or keep as local annotation. Internalize `UserAgentParser`, `LogMDCHelper`. |
| **srs-integration** (~28 files)         | web         | **Internalize.** Copy SRS DTOs and `SrsInfraService` into local module. Replace XML config (`srs-services-config.xml`) with Java `@Configuration`. |
| **intyginfo** (~22 files)               | web         | **Internalize.** Copy `WcIntygInfo`, `ItIntygInfo`, `IntygInfoEvent`, `IntygInfoEventType` into local module. |
| **log-messages** (~17 files)            | web, common | **Internalize.** Copy PDL logging DTOs (`PdlLogMessage`, `ActivityType`, `ActivityPurpose`, `Patient`, `Enhet`, `PdlResource`) into local module. |
| **certificate** (~12 files)             | web         | **Internalize.** Copy `CertificateListResponse`, `CertificateListRequest`, `CertificateListEntry` into local module. |
| **xmldsig** (~11 files)                 | web         | **Internalize.** Copy XML signature interfaces and DTOs (`IntygXMLDSignature`, `PrepareSignatureService`, `XMLDSigService`, `FakeSignatureService`) into local module. Replace XML config with Java `@Configuration`. |
| **sjukfall-engine** (~4 files)          | web         | **Internalize.** Copy DTOs (`IntygData`, `SjukfallEnhet`, `Formaga`, `DiagnosKod`) and `SjukfallEngineService` into local module. Replace component scan with `@Configuration`. |
| **driftbanner-dto** (~5 files)          | web         | **Internalize.** Copy `Application`, `Banner` DTOs into local module. |
| **ia-integration** (~4 files)           | web         | **Internalize.** Copy `IABannerService`, `BannerJob` into local module. Replace XML config with Java `@Configuration`. |
| **message** (~4 files)                  | web         | **Internalize.** Copy `MessageFromIT` DTO into local module. |
| **dynamiclink** (~3 files)              | web         | **Internalize.** Copy `DynamicLink`, `DynamicLinkService` into local module. |
| **security-filter** (~3 files)          | web         | **Internalize.** Copy `SessionTimeoutFilter`, `InternalApiFilter`, `RequestContextHolderUpdateFilter`, `PrincipalUpdatedFilter`, `SecurityHeadersFilter` into local module. Register as `FilterRegistrationBean`. |
| **integreradeenheter** (~2 files)       | web         | **Internalize.** Copy `IntegratedUnitDTO` into local module. |
| **testcertificate** (~2 files)          | web         | **Internalize.** Copy `TestCertificateEraseRequest`, `TestCertificateEraseResult` into local module. |
| **security-siths** (~1 file)            | web         | **Internalize.** Copy `BaseUserDetailsService` abstract class or refactor `WebcertUserDetailsService` to not extend it. |
| **hsa-integration-api**                 | web, notif. | **Replace** — internalize as local REST client calling intyg-proxy-service directly. |
| **hsa-integration-intyg-proxy-service** | web (runtime) | **Replace** — internalized into local REST client. |
| **pu-integration-api**                  | web, notif. | **Replace** — internalize as local REST client calling intyg-proxy-service directly. |
| **pu-integration-intyg-proxy-service**  | web (runtime) | **Replace** — internalized into local REST client. |
| **common-redis-cache-core**             | web, stubs  | **Replace** — use `spring-boot-starter-data-redis` auto-configuration directly. |
| **privatepractitioner**                 | web         | **Evaluate** — verify actual usage; internalize if needed or remove if unused. |
| **postnummerservice-integration**       | web (scan)  | **Internalize** — copy postal code service into local module (loaded via component scan). |

**Dependency lines to remove:**

From `web/build.gradle` (~24 lines), `notification-sender/build.gradle` (~5 lines), `common/build.gradle` (~1 line),
`stubs/notification-stub/build.gradle` (~1 line).

**The `infraVersion` property is removed from `build.gradle`.**

---

### 3.6 Migrate All Tests to JUnit Jupiter

**What changes:**

All remaining JUnit 4 tests (~162 files) must be migrated to JUnit 5 (Jupiter).

**Migration pattern per file:**

| JUnit 4                                   | JUnit 5                                                   |
|-------------------------------------------|-----------------------------------------------------------|
| `import org.junit.Test`                   | `import org.junit.jupiter.api.Test`                       |
| `import org.junit.Before`                 | `import org.junit.jupiter.api.BeforeEach`                 |
| `import org.junit.After`                  | `import org.junit.jupiter.api.AfterEach`                  |
| `import org.junit.BeforeClass`            | `import org.junit.jupiter.api.BeforeAll`                  |
| `import org.junit.Assert.*`               | `import org.junit.jupiter.api.Assertions.*` (or AssertJ)  |
| `import org.junit.runner.RunWith`         | `import org.junit.jupiter.api.extension.ExtendWith`       |
| `@RunWith(MockitoJUnitRunner.class)`      | `@ExtendWith(MockitoExtension.class)`                     |
| `@RunWith(SpringJUnit4ClassRunner.class)` | `@ExtendWith(SpringExtension.class)` or `@SpringBootTest` |
| `@Rule ExpectedException`                 | `assertThrows(...)`                                       |

**Dependency changes:**

In all `build.gradle` files:
```groovy
// Remove:
testRuntimeOnly "org.junit.vintage:junit-vintage-engine"
testImplementation "junit:junit"

// Ensure present:
testImplementation "org.junit.jupiter:junit-jupiter"
testImplementation "org.mockito:mockito-junit-jupiter"
```

**Test XML contexts:** Replace `@ContextConfiguration(locations = "classpath:...")` XML-based test configurations with
`@SpringBootTest` or `@Configuration` inner classes in test classes.

---

### 3.7 Logging: Spring Boot Structured ECS Logging

**What changes:**

Replace the manual Logback ECS encoder setup with Spring Boot 3.4+'s native structured logging support.

**Remove:**

- `co.elastic.logging:logback-ecs-encoder` dependency from `web/build.gradle`
- `web/src/main/resources/logback/logback-spring-base.xml` (defines the ECS_JSON_CONSOLE appender)
- `devops/dev/config/logback-spring.xml` (manual Logback configuration)
- `LogbackConfiguratorContextListener` usage (infra monitoring module — removed with infra)
- The `logbackConfigParameter` context-param from `web.xml` (removed with web.xml)

**Replace with `application.properties`:**

```properties
logging.structured.format.console=ecs
logging.structured.ecs.service.name=webcert
logging.structured.ecs.service.environment=${spring.profiles.active:default}
```

**Retain:**

- The `logging` module's `MdcServletFilter`, `MdcHelper`, `MdcLogConstants`, `PerformanceLogging`, and `PerformanceLoggingAdvice` — these
  are local to the project. Register `MdcServletFilter` as a `FilterRegistrationBean`.
- `MdcUserServletFilter` — register as `FilterRegistrationBean`.
- Test logback configs (`logback-test.xml`) — these are fine as-is.

---

### 3.8 Apache Camel: XML → Java DSL

**What changes:**

Convert the 2 Camel XML contexts to Java DSL routes managed by Spring Boot Camel auto-configuration.

| XML File                                          | Replacement                                                              |
|---------------------------------------------------|--------------------------------------------------------------------------|
| `notifications/camel-context.xml`                 | `RouteBuilder` bean using Java DSL — retain `ProcessNotificationRequestRouteBuilder` pattern |
| `certificates/camel-context.xml`                  | `RouteBuilder` bean using Java DSL — retain `CertificateRouteBuilder` pattern |
| `notifications/beans-context.xml`                 | Java `@Configuration` class with bean definitions                         |
| `certificates/beans-context.xml`                  | Java `@Configuration` class with bean definitions                         |
| `notifications/ws-context.xml`                    | Java `@Configuration` class for CXF WS clients                           |

**Camel context consolidation:** The 2 separate Camel contexts (`webcertNotification`, `webcertCertificateSender`) merge into a single
Camel context managed by `camel-spring-boot-starter` auto-configuration. Each set of routes remains as separate `RouteBuilder` beans.

**Configure via `application.properties`:**

```properties
camel.springboot.name=webcert
```

---

## 4. Migration Order

The work items have dependencies. The recommended execution order is:

```
┌─────────────────────────────────────────────────────┐
│ Phase 0: Test Modernization (zero runtime risk)     │
│                                                     │
│  3.6  Migrate all tests to JUnit Jupiter            │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│ Phase 1: Inline Infra Dependencies                  │
│                                                     │
│  3.5  Inline/replace infra modules (incremental)    │
│       DTO-only modules first, then services,        │
│       then security, then HSA/PU REST replacement   │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│ Phase 2: Pre-Boot Cleanup                           │
│                                                     │
│  3.3  JAX-RS → Spring MVC (~52 controllers)         │
│  3.2  XML → Java configuration (partial)            │
│  3.8  Camel XML → Java DSL                          │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│ Phase 3: Spring Boot Switch                         │
│                                                     │
│  3.1  Spring Boot bootstrap (the big switch)        │
│  3.2  Remaining XML elimination                     │
│  3.7  Spring Boot ECS logging                       │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│ Phase 4: Auto-Configuration Swaps                   │
│                                                     │
│  3.4  JPA auto-config                               │
│  3.4  JMS auto-config                               │
│  3.4  Actuator/Micrometer                           │
│  3.4  Redis auto-config                             │
│  3.4  Mail auto-config                              │
│       Dockerfile update                             │
└─────────────────────────────────────────────────────┘
```

**Rationale:** JUnit 5 migration has zero runtime impact and can go first. Infra inlining must happen before the infra dependencies can be
removed. JAX-RS → Spring MVC and XML → Java are prerequisites for the Spring Boot switch. Auto-configuration swaps are safest after Spring
Boot is running (auto-config backs off when existing beans are present).

---

## 5. Verification Criteria

The migration is complete when:

- [ ] The application starts as a Spring Boot executable JAR (`java -jar webcert.jar`).
- [ ] No XML files are used for Spring bean configuration (data XML files like Liquibase changelogs, XSLT, SAML metadata are fine).
- [ ] All REST endpoints respond correctly using Spring MVC (`@RestController`).
- [ ] All SOAP endpoints respond correctly via CXF (configured in Java).
- [ ] `grep -r "se.inera.intyg.infra" --include="build.gradle"` returns no results.
- [ ] `grep -r "import org.junit\." --include="*.java" src/ | grep -v jupiter` returns no results.
- [ ] `junit-vintage-engine` is not in any `build.gradle`.
- [ ] `junit:junit` is not in any `build.gradle`.
- [ ] Structured ECS JSON logs are produced when `logging.structured.format.console=ecs` is set.
- [ ] Spring Boot Actuator health endpoint responds at `/actuator/health`.
- [ ] Prometheus metrics are available at `/actuator/prometheus`.
- [ ] Apache Camel routes start and process messages correctly (no XML Camel contexts).
- [ ] All existing tests pass.
- [ ] The Docker image builds and runs successfully with the new JAR packaging.

---

## 6. Out of Scope

The following items from the [goal tech stack](goal-tech-stack.md) are **intentionally deferred** to later iterations:

| Item                                         | Reason for Deferral                                               |
|----------------------------------------------|-------------------------------------------------------------------|
| Hexagonal/domain module architecture         | Requires significant refactoring beyond the Spring Boot migration |
| Gradle Kotlin DSL migration                  | Independent concern; can be done separately                       |
| WSDL2Java plugin for code generation         | Independent concern; existing generated code works                |
| Testcontainers (MySQL, ActiveMQ, Redis)      | Can be adopted after Spring Boot is in place                      |
| MapStruct for DTO mapping                    | Additive improvement; not blocking                                |
| `@Value` → `@ConfigurationProperties`        | Functional-equivalent lift; existing `@Value` works               |
| `application.properties` → `application.yml` | Independent concern; .properties works with Spring Boot           |
| Vavr removal                                 | Library still works; removal requires usage analysis              |
| Commons IO / Commons Lang3 removal           | Libraries still work; removal requires usage analysis             |
| REST client consolidation                    | Existing clients work as-is under Spring Boot                     |
| csintegration module extraction              | Module restructuring deferred                                     |
| `se.inera.intyg.common` dependency reduction | Core business dependency; requires separate analysis              |
| MockServer / MockWebServer                   | Additive test infrastructure; not blocking                        |
| AssertJ standardization                      | Mixed assertions work; standardization is cosmetic                |

---

## 7. Risk Assessment

| Risk                                               | Likelihood | Impact | Mitigation                                                                                              |
|----------------------------------------------------|------------|--------|---------------------------------------------------------------------------------------------------------|
| CXF SOAP + Spring Boot compatibility issues        | Medium     | High   | CXF has documented Spring Boot support; test early with a spike                                         |
| security-common inlining breaks auth flows         | Medium     | High   | Incremental: copy classes first, update imports, test each auth path (SITHS, eleg, fake)                |
| JAX-RS → Spring MVC changes response format        | Medium     | Medium | Compare JSON responses before/after for each controller; test error handling                            |
| Camel context consolidation breaks routing         | Low        | High   | Test notification and certificate sending flows end-to-end                                              |
| `se.inera.intyg.common` modules expect XML config  | Medium     | Medium | Test each common module's Spring context requirements; provide Java config equivalents                  |
| JUnit 4 → 5 migration breaks test behavior         | Low        | Medium | Mechanical migration; run full test suite after each batch                                              |
| Spring Boot auto-config conflicts with CXF servlet | Medium     | Medium | Configure CXF servlet path explicitly to avoid clash with DispatcherServlet                             |
| Filter ordering changes break security             | Medium     | High   | Document current filter order from web.xml; reproduce exact order via `FilterRegistrationBean.setOrder` |
| SAML 2.0 config changes under Spring Boot          | Low        | High   | SAML config is already Java-based; Spring Boot auto-config should be compatible                         |
| Property name changes (Spring Boot conventions)    | Medium     | Low    | Create property mapping; update deployment configurations                                               |
| 52 controller migration surface                    | Medium     | Medium | Migrate one CXF context at a time; verify each group independently                                     |
