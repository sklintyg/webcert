# Incremental Migration Plan — Keeping the Application Working at Every Step

Based on the `first-migration-scope.md`, here is a reordering of the work into **small, independently verifiable increments**. After each
step, the application must: **compile ✅, pass all tests ✅, start ✅, and be deployable ✅**.

The key insight is that the migration scope's phases bundle too much together. Below, we break it into ~20 atomic steps. Steps 1–14 keep
the application running as a WAR on Tomcat/Gretty. Step 15 is the actual Spring Boot switch — small because all the preparation is done.
Steps 16–20 progressively replace manual bean config with Spring Boot auto-configuration.

---

## Step 1 — Migrate all tests to JUnit 5 *(~162 files, zero runtime impact)*

**Why first:** This is the only work item with **zero risk to the running application**. It only touches test code. It removes `junit:junit`
and `junit-vintage-engine` from all `build.gradle` files. You can do it module-by-module, running `./gradlew test` after each batch.

**What to do:**

1. Start with `persistence` module (~18 tests using `@RunWith(SpringJUnit4ClassRunner.class)` with XML `@ContextConfiguration`).
2. Then `web` module (~144 files) — convert in batches by package.
3. Then `notification-sender` module — update any remaining JUnit 4 tests.
4. Remove `junit-vintage-engine` and `junit:junit` from all `build.gradle` files.
5. Replace test XML contexts (`DiagnosServiceTest-context.xml`, `DiagnosRepositoryFactoryTest-context.xml`) with `@Configuration` inner
   classes or direct bean setup.

**Migration pattern:**
- `org.junit.Test` → `org.junit.jupiter.api.Test`
- `@Before` → `@BeforeEach`, `@After` → `@AfterEach`
- `@RunWith(MockitoJUnitRunner.class)` → `@ExtendWith(MockitoExtension.class)`
- `@RunWith(SpringJUnit4ClassRunner.class)` → `@ExtendWith(SpringExtension.class)`
- `@Rule ExpectedException` → `assertThrows(...)`
- `Assert.assertEquals(...)` → `Assertions.assertEquals(...)` (or keep AssertJ where already used)

**Verify:** `./gradlew test` — all tests pass. Application starts normally (no production code changed).
`grep -r "import org.junit\." --include="*.java" | grep -v jupiter` returns no results.

---

## Step 2 — Inline simple DTO-only infra modules *(add code, don't remove deps yet)*

Copy the required classes from infra into the project **without removing the infra dependencies yet**. Change imports in your code to point
to the local copies. This is purely additive — the infra deps are still on the classpath but unused.

Start with the simplest, DTO-only modules:

- **testcertificate** → copy `TestCertificateEraseRequest`, `TestCertificateEraseResult` (~2 files using it)
- **message** → copy `MessageFromIT` (~4 files)
- **integreradeenheter** → copy `IntegratedUnitDTO` (~2 files)
- **driftbanner-dto** → copy `Application`, `Banner` DTOs (~5 files)
- **dynamiclink** → copy `DynamicLink`, `DynamicLinkService` (~3 files)
- **certificate** → copy `CertificateListEntry`, `CertificateListRequest`, `CertificateListResponse` etc. (~12 files)
- **intyginfo** → copy `WcIntygInfo`, `ItIntygInfo`, `IntygInfoEvent`, `IntygInfoEventType` (~22 files)

**Verify:** `./gradlew test` + start the application. Everything still works, just using local copies.

---

## Step 3 — Inline `log-messages` *(~17 files across web and common)*

Copy PDL logging DTOs (`PdlLogMessage`, `ActivityType`, `ActivityPurpose`, `Patient`, `Enhet`, `PdlResource`, `ResourceType`) into the
`common` module (since both `web` and `common` use it). Update all imports.

**Verify:** `./gradlew test` + start. PDL audit logging still works.

---

## Step 4 — Inline `monitoring` *(~34 files)*

Copy `@PrometheusTimeMethod` annotation and its AOP aspect, `@EnablePrometheusTiming`, `UserAgentParser`, `UserAgentInfo`, and
`LogMDCHelper` into the project. Update `LoggingConfig.java` to reference local classes.

**Verify:** `./gradlew test` + start. Monitoring annotations still work.

---

## Step 5 — Inline `xmldsig` *(~11 files)*

Copy XML digital signature interfaces and DTOs (`IntygXMLDSignature`, `IntygSignature`, `ValidationResponse`, `PrepareSignatureService`,
`XMLDSigService`, `FakeSignatureService`, `PartialSignatureFactory`) into the project. Replace the `classpath:xmldsig-config.xml` import
in `webcert-config.xml` with component scanning or explicit bean definitions.

**Verify:** `./gradlew test` + start. Digital signing (DSS, GRP, fake) still works.

---

## Step 6 — Inline `sjukfall-engine` + `ia-integration` + `postnummerservice-integration` *(smaller service modules)*

- **sjukfall-engine:** Copy DTOs (`IntygData`, `SjukfallEnhet`, `Formaga`, `DiagnosKod`) and `SjukfallEngineService`. Replace component scan
  in `webcert-config.xml` with local `@Service`/`@Component`.
- **ia-integration:** Copy `IABannerService`, `BannerJob`. Replace `classpath:ia-services-config.xml` import with Java `@Configuration`.
- **postnummerservice-integration:** Copy postal code service. Replace component scan reference.

**Verify:** `./gradlew test` + start. Sick leave calculations, IA banners, and postal code lookups still work.

---

## Step 7 — Inline `srs-integration` *(~28 files)*

Copy SRS DTOs (`SrsCertificate`, `SrsResponse`, `SrsQuestion`, `SrsPrediction`, etc.) and `SrsInfraService` into the project. Replace
`classpath:srs-services-config.xml` import with Java `@Configuration`. Also handle SRS stub (`classpath:srs-stub-context.xml`).

**Verify:** `./gradlew test` + start. SRS integration still works.

---

## Step 8 — Inline `security-common` + `security-authorities` + `security-siths` + `security-filter` *(largest, most critical)*

This is the largest inlining step (~267 files affected). It must be done carefully:

1. **security-common** (~167 files): Copy user models (`HoSPerson`, `CareUnit`, `CareProvider`, `User`, `UserOrigin`,
   `AuthenticationMethod`, etc.) into a local security package. This is the most pervasive dependency.
2. **security-authorities** (~96 files): Copy `SecurityConfigurationLoader`, `CommonAuthoritiesResolver`, `AuthoritiesHelper`,
   role/privilege enums. Retain `authorities.yaml` loading.
3. **security-siths** (~1 file): Copy `BaseUserDetailsService` or refactor `WebcertUserDetailsService` to not extend it.
4. **security-filter** (~3 files): Copy `SessionTimeoutFilter`, `InternalApiFilter`, `RequestContextHolderUpdateFilter`,
   `PrincipalUpdatedFilter`, `SecurityHeadersFilter` into local module.

**Approach:** Do this in sub-steps:
- First: copy all security classes into local packages.
- Second: update imports across the entire codebase (automated search-and-replace).
- Third: verify compilation and test suite.

**Verify:** `./gradlew test` + start. Login via SITHS, eleg, and fake auth all work. Session management works. Role-based access works.

---

## Step 9 — Replace HSA and PU integrations with REST clients

Replace `hsa-integration-api` + `hsa-integration-intyg-proxy-service` and `pu-integration-api` + `pu-integration-intyg-proxy-service` with
direct REST calls to `intyg-proxy-service`. Remove the `classpath:` XML imports for these services.

**What to do:**
1. Create local HSA REST client using Spring `RestClient` (or retain `RestTemplate` since we're not on Spring Boot yet).
2. Create local PU REST client using the same approach.
3. Define local DTOs for the responses.
4. Remove the 4 infra dependency lines.
5. Remove XML imports: `classpath:/hsa-integration-intyg-proxy-service-config.xml`,
   `classpath:/pu-integration-intyg-proxy-service-config.xml`.

**Verify:** `./gradlew test` + start. HSA/PU lookups still work (now via REST instead of infra module).

---

## Step 10 — Inline remaining infra modules + remove all infra dependencies

Handle any remaining infra modules:
- **privatepractitioner** — verify usage; internalize or remove if unused.
- **common-redis-cache-core** — replace `classpath:basic-cache-config.xml` import with direct Redis configuration (keep existing
  `CacheConfig`, `RedisLaunchIdCacheConfiguration`, `CertificatesForPatientCacheConfiguration` beans but remove infra dependency).

Then remove **all** `se.inera.intyg.infra` dependency lines from:
- `web/build.gradle` (~24 lines)
- `notification-sender/build.gradle` (~5 lines)
- `common/build.gradle` (~1 line)
- `stubs/notification-stub/build.gradle` (~1 line)

Remove the `infraVersion` property from `build.gradle`.

**Verify:** `./gradlew build` — compiles, all tests pass. `grep -r "se.inera.intyg.infra" --include="build.gradle"` returns nothing.
Application starts.

---

## Step 11 — Convert JAX-RS controllers to Spring MVC *(~52 files)*

Convert all `@Path`/`@GET`/`@POST` controllers to `@RestController`/`@GetMapping`/`@PostMapping`. Replace `Response` with
`ResponseEntity<T>`. Migrate one CXF servlet context at a time:

1. **`/internalapi/*`** (8 controllers) — start here, least user-facing.
2. **`/api/*` facade** (11 controllers) — the main facade layer.
3. **`/api/*` legacy** (11 controllers) — older API controllers.
4. **`/moduleapi/*`** (3 controllers) — module API.
5. **`/visa/*`, `/v2/visa/*`** (2 controllers) — integration.
6. **`/webcert/web/user/*`** (4 controllers) — legacy uthopp integration.
7. **`/testability/*`** (12 controllers) — testability, gated by profile.
8. **`/authtestability/*`** (1 controller) — auth testability.

For each group:
- Convert JAX-RS annotations to Spring MVC.
- Remove the corresponding CXF servlet XML file.
- Update `web.xml` to remove the CXF servlet mapping (narrow CXF to `/services/*` only).
- The new Spring MVC controllers are picked up by the existing `DispatcherServlet` component scan.

After all groups are done:
- Remove `jakarta.ws.rs:jakarta.ws.rs-api` dependency.
- Remove `com.fasterxml.jackson.jakarta.rs:jackson-jakarta-rs-json-provider` dependency.
- Remove `swagger-jaxrs` dependency (replace with SpringDoc OpenAPI later).

**Verify:** `./gradlew test` + start. Hit every REST endpoint — same responses.

---

## Step 12 — Convert XML bean configuration to Java *(except web.xml)*

Convert each remaining XML file to Java `@Configuration`:

1. **`webcert-config.xml`** → move `@Import`s and component scans to `AppConfig.java` or new config classes.
   Remove all `<import resource="classpath:...">` (already handled by infra removal and Java configs).
2. **`ws-config.xml`** → `CxfEndpointConfig.java` — programmatic CXF endpoint/client registration.
3. **`test.xml`** → Java `@Configuration` with `@Profile("dev")`.
4. **`webcert-testability-api-context.xml`** → Java `@Configuration` with profile.
5. **`repository-context.xml`** → remove (JPA config handled by `JpaConfig` or future Spring Boot auto-config).
6. **`webcert-common-config.xml`** → remove (component scanning via `@SpringBootApplication` or `@ComponentScan`).
7. **`mail-config.xml`** → Java `@Configuration` or Spring Boot mail auto-config (in step 15+).
8. **Module XML configs** (`fmb-services-config.xml`, `servicenow-services-config.xml`, etc.) → convert to `@Configuration` or
   `@ComponentScan`.
9. **Stub XML configs** → convert to `@Configuration` with `@Profile`.
10. **Test XML contexts** (notification-sender test configs) → replace with Java test configurations.

Update `web.xml`'s `contextConfigLocation` to point to the Java config class instead of `webcert-config.xml`.

**Verify:** `./gradlew test` + start. All SOAP endpoints respond. All REST endpoints respond. No XML import failures.

---

## Step 13 — Convert Camel XML routes to Java DSL

Convert the 2 Camel XML contexts to Java DSL:

1. **`notifications/camel-context.xml`** → Java DSL `RouteBuilder` bean. The existing `ProcessNotificationRequestRouteBuilder` class already
   exists — configure it programmatically instead of via XML.
2. **`certificates/camel-context.xml`** → Java DSL `RouteBuilder` bean. Same for `CertificateRouteBuilder`.
3. **`notifications/beans-context.xml`** → Java `@Configuration` with `@Bean` definitions.
4. **`certificates/beans-context.xml`** → Java `@Configuration` with `@Bean` definitions.
5. **`notifications/ws-context.xml`** → Java `@Configuration` for CXF WS clients.
6. **`notification-sender-config.xml`** → Java `@Configuration` with `@Import`.
7. **`jms-context.xml`** → Java `@Configuration` (JMS beans; will be replaced by auto-config in step 17).

Remove all notification-sender XML files.

**Verify:** `./gradlew test` (including Camel tests) + start. Notification sending and certificate transfer still work.

---

## Step 14 — Remove web.xml + resolve classpath XML imports

At this point, all Spring bean configuration is in Java. The only remaining XML file is `web.xml`.

1. Register all filters as `FilterRegistrationBean` beans in a new `WebFilterConfig.java`, preserving the exact filter order from web.xml:
   - `springSessionRepositoryFilter` (order 1)
   - `requestContextHolderUpdateFilter` (order 2)
   - `MdcServletFilter` (order 3)
   - `defaultCharacterEncodingFilter` (order 4, specific URL pattern)
   - `sessionTimeoutFilter` (order 5)
   - `springSecurityFilterChain` (order 6)
   - `principalUpdatedFilter` (order 7)
   - `unitSelectedAssuranceFilter` (order 8, specific URL patterns)
   - `securityHeadersFilter` (order 9)
   - `MdcUserServletFilter` (order 10)
   - `internalApiFilter` (order 11, `/internalapi/*`)
   - `launchIdValidationFilter` (order 12, specific URL patterns)
   - `allowCorsFilter` (order 13, specific URL)

2. Register remaining servlets as `ServletRegistrationBean`:
   - `CXFServlet` at `/services/*` (SOAP only).
   - The existing `DispatcherServlet` at `/` is auto-configured by Spring Boot (in step 15).

3. Handle the `classpath*:module-config.xml` and `classpath*:wc-module-cxf-servlet.xml` wildcard imports from common modules — evaluate
   contents and provide Java equivalents.

4. Remove `web.xml`.

**Note:** At this point the app still runs on Gretty/WAR, but `web.xml` is gone — the `WebApplicationInitializer` pattern or Gretty's
`serverConfigFile` handles servlet initialization.

**Verify:** `./gradlew test` + start via Gretty. All endpoints respond. Filter chain works correctly.

---

## Step 15 — Spring Boot bootstrap *(the big switch)*

This is the most critical step. All preparation is done — the switch should be small:

1. Add `org.springframework.boot` plugin to `app/build.gradle` (or `web/build.gradle` if keeping single module).
2. Create `WebcertApplication.java` with `@SpringBootApplication`.
3. Add Spring Boot starters: `spring-boot-starter-web`, `spring-boot-starter-security`.
4. Remove Gretty plugin.
5. Configure `@EntityScan` and `@EnableJpaRepositories` on the main application class.
6. Move `application.properties` to the app module's `src/main/resources/`.
7. Ensure CXF servlet is registered via `ServletRegistrationBean` at `/services/*`.

**Do NOT change JPA, JMS, Redis, mail, or metrics config in this step.** Keep existing `JpaConfig`, `JmsConfig`, `CacheConfig`, etc.
as-is. They will still work under Spring Boot — Spring Boot auto-config backs off when it finds existing beans.

**Verify:** `./gradlew bootRun` — application starts. All endpoints respond. `./gradlew test` passes.

---

## Step 16 — Replace JPA manual config with Spring Boot auto-configuration

1. Remove `JpaConfig`, `repository-context.xml` remnants.
2. Add `spring-boot-starter-data-jpa` (if not already pulled transitively).
3. Move DB properties to Spring Boot conventions (`spring.datasource.*`, `spring.jpa.*`, `spring.liquibase.*`).
4. Remove explicit `HikariCP`, `hibernate-core` dependencies from `persistence/build.gradle` (now provided by starter).

**Verify:** `./gradlew bootRun` — starts, connects to DB, Liquibase runs. `./gradlew test` passes.

---

## Step 17 — Replace JMS manual config with Spring Boot auto-configuration

1. Remove manual `ConnectionFactory`, `PooledConnectionFactory`, `JmsTemplate`, `JmsTransactionManager` beans.
2. Add `spring-boot-starter-activemq`.
3. Move properties to `application.properties` (`spring.activemq.*`).
4. Keep custom `Queue` beans and `JmsListenerContainerFactory` customization if needed.

**Verify:** `./gradlew bootRun` — JMS listeners start. `./gradlew test` passes.

---

## Step 18 — Replace Prometheus with Spring Boot Actuator + Micrometer

1. Remove `io.prometheus:simpleclient_servlet_jakarta`, `simpleclient_hotspot`.
2. Add `spring-boot-starter-actuator` + `micrometer-registry-prometheus`.
3. Configure `management.endpoints.web.exposure.include=health,info,prometheus`.
4. Replace local `@PrometheusTimeMethod` with Micrometer `@Timed` or keep as local annotation backed by Micrometer.
5. Convert `HealthMonitor` Prometheus Gauge metrics to Micrometer `MeterRegistry`.

**Verify:** `/actuator/health` responds. `/actuator/prometheus` serves metrics. `./gradlew test` passes.

---

## Step 19 — Replace Redis/caching manual config + Spring Boot ECS logging

1. **Redis:** Add `spring-boot-starter-data-redis` + `spring-session-data-redis`. Remove manual Redis config. Configure via
   `spring.data.redis.*`. Retain `RedisLaunchIdCacheConfiguration` and `CertificatesForPatientCacheConfiguration` (they'll use the
   auto-configured `RedisConnectionFactory`).
2. **Mail:** Add `spring-boot-starter-mail`. Remove manual mail config. Configure via `spring.mail.*`.
3. **Logging:** Remove `logback-ecs-encoder`, any remaining `logback-spring-base.xml`. Add
   `logging.structured.format.console=ecs` to `application.properties`.

**Verify:** Redis caching works. Mail sending works. ECS JSON logs on stdout. All endpoints respond.

---

## Step 20 — Dockerfile update + SpringDoc OpenAPI + final cleanup

1. **Dockerfile:** Change from `ADD *.war $CATALINA_HOME/webapps/` to a Spring Boot JAR-based image:
   ```dockerfile
   COPY app/build/libs/*.jar app.jar
   ENTRYPOINT ["java", "-jar", "app.jar"]
   ```
2. **SpringDoc OpenAPI:** Add `springdoc-openapi-starter-webmvc-ui` to replace the removed Swagger JAX-RS.
3. **Final cleanup:**
   - Remove empty XML directories (`webapp/WEB-INF/`).
   - Remove Gretty-related configuration files.
   - Update `devops/` configuration for Spring Boot conventions.
   - Verify all Spring profiles still work (dev, test, prod).

**Verify:** Docker build + run. Application starts in container. All endpoints respond. API documentation at `/swagger-ui.html`.

---

## Summary: When Is the App Working?

| After Step | What You Can Verify                  | Runs On         | App Broken? |
|------------|--------------------------------------|-----------------|-------------|
| **1**      | All tests on JUnit 5                 | WAR / Gretty    | ❌ No       |
| **2**      | Simple infra DTOs inlined            | WAR / Gretty    | ❌ No       |
| **3**      | log-messages inlined                 | WAR / Gretty    | ❌ No       |
| **4**      | monitoring inlined                   | WAR / Gretty    | ❌ No       |
| **5**      | xmldsig inlined                      | WAR / Gretty    | ❌ No       |
| **6**      | sjukfall + IA + postnummer inlined   | WAR / Gretty    | ❌ No       |
| **7**      | SRS inlined                          | WAR / Gretty    | ❌ No       |
| **8**      | Security modules inlined             | WAR / Gretty    | ❌ No       |
| **9**      | HSA/PU via REST                      | WAR / Gretty    | ❌ No       |
| **10**     | All infra deps removed               | WAR / Gretty    | ❌ No       |
| **11**     | All REST on Spring MVC               | WAR / Gretty    | ❌ No       |
| **12**     | All XML config → Java                | WAR / Gretty    | ❌ No       |
| **13**     | Camel routes in Java DSL             | WAR / Gretty    | ❌ No       |
| **14**     | web.xml removed                      | WAR / Gretty    | ❌ No       |
| **15**     | **Spring Boot runs**                 | **Spring Boot** | ❌ No       |
| **16**     | JPA auto-configured                  | Spring Boot     | ❌ No       |
| **17**     | JMS auto-configured                  | Spring Boot     | ❌ No       |
| **18**     | Actuator/Micrometer live             | Spring Boot     | ❌ No       |
| **19**     | Redis + Mail + Logging               | Spring Boot     | ❌ No       |
| **20**     | Docker + SpringDoc + cleanup         | Spring Boot     | ❌ No       |

**Every step is a deployable, verifiable checkpoint.** Steps 1–14 don't change how the application runs (still WAR/Tomcat/Gretty). Step
15 is the actual Spring Boot switch, and it's small because all the preparation is done. Steps 16–20 are safe because Spring Boot
auto-config backs off gracefully when existing beans are present, so you swap one concern at a time.

---

## Highest-Risk Steps

**Step 8** (Security inlining) is the riskiest inlining step due to the pervasive usage of `security-common` (167 files) and
`security-authorities` (96 files). To de-risk:
- Copy classes first, then update imports in batches.
- Test each authentication path (SITHS, eleg, fake) after each batch.
- Test role-based access control after authorities are inlined.

**Step 11** (JAX-RS → Spring MVC) is the largest API migration (~52 controllers). To de-risk:
- Migrate one CXF servlet context at a time (8 groups).
- Verify endpoint responses match before/after for each group.
- Pay special attention to error handling (JAX-RS `WebApplicationException` → Spring MVC `@ExceptionHandler`).

**Step 15** (Spring Boot bootstrap) is the most critical single step. To de-risk:
- Consider doing a quick spike first: create a branch, add the Spring Boot plugin, create the main class, and see if it starts.
- This validates CXF + Spring Boot coexistence early.
- If it works, proceed with confidence. If not, you'll know what to fix before investing in the rest.
