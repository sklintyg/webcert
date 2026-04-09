# Step 14 — Spring Boot Bootstrap + `web.xml` Removal

## Problem Statement

After Steps 1–13, webcert runs as a WAR on Tomcat/Gretty with a pure Java Spring
configuration. The application has:

- No infra module dependencies (removed in Step 10)
- All controllers migrated to Spring MVC (Step 11)
- All XML bean config converted to Java `@Configuration` (Step 12)
- All Camel routes in Java DSL (Step 13)
- No `@ImportResource` remaining in `AppConfig.java`

**Goal:** Switch from WAR/Gretty to an embedded Spring Boot application by adding the Spring
Boot plugin directly to the existing `webcert-web` (`web/`) module. The module structure
stays exactly as it is — no new submodules are created.

**After this step, the application:**
- Starts with `./gradlew :webcert-web:bootRun`
- Has no `web.xml` (replaced by `FilterRegistrationBean` and `ServletRegistrationBean`)
- Serves all SOAP endpoints via CXF at `/services/*`
- Serves all REST endpoints via Spring MVC DispatcherServlet at `/`
- Exposes SpringDoc API docs at `/swagger-ui/index.html`
- Has OpenSAML security hardening active
- `application.properties` stays in `web/src/main/resources/` (no file moves)

**Not in scope for this step:**
- Module restructuring (e.g., creating a separate `app` module) → later
- JPA auto-configuration → Step 15
- JMS auto-configuration → Step 16
- Actuator / Micrometer → Step 17
- Redis / mail auto-configuration → Step 18
- Dockerfile update → Step 19

---

## Pre-Conditions (Step 13 Must Be Complete)

Verify each before beginning this step.

| Pre-condition | Verified by |
|---|---|
| No `@ImportResource` in `AppConfig.java` | `grep -n "ImportResource" web/src/main/java/.../AppConfig.java` returns nothing |
| No XML Spring configs remain in `notification-sender/` | `find notification-sender/src -name "*.xml" -path "*/resources/*"` returns nothing |
| All tests pass on current state | `./gradlew test` — all green |
| Application starts with Gretty | `./gradlew :webcert-web:appRun` — starts cleanly |
| JAX-RS fully removed | `grep "jakarta.ws.rs" web/build.gradle` returns nothing |
| Step 11 Swagger deferral confirmed | `grep "swagger-jaxrs" web/build.gradle` returns one match (removed in 14.10) |
| `devops/dev/config/application-dev.properties` exists | `ls devops/dev/config/application-dev.properties` — needed for dev logging override |

---

## Current State

### `web.xml` — Listeners, Servlets, Filters

| Element | Type | Class | Action in Step 14 |
|---|---|---|---|
| `ContextLoaderListener` | Listener | `o.s.web.context.ContextLoaderListener` | **Remove** — Spring Boot has no WAR-style root context |
| `RequestContextListener` | Listener | `o.s.web.context.request.RequestContextListener` | **Remove** — Spring Boot auto-configures `RequestContextFilter` |
| `HttpSessionEventPublisher` | Listener | `o.s.security.web.session.HttpSessionEventPublisher` | **Register as `@Bean`** in `WebSecurityConfig.java` |
| `LogbackConfiguratorContextListener` | Listener | `se.inera...LogbackConfiguratorContextListener` | **Remove** — replaced by `logging.config` system property |
| `web` (DispatcherServlet at `/`) | Servlet | `o.s.web.servlet.DispatcherServlet` | **Auto-configured** by Spring Boot — no action needed |
| `services` (CXFServlet at `/services/*`) | Servlet | `o.a.cxf.transport.servlet.CXFServlet` | **Register** via `ServletRegistrationBean` |
| `metrics` (MetricsServlet at `/metrics`) | Servlet | `io.prometheus...MetricsServlet` | **Register** via `ServletRegistrationBean` |
| `springSessionRepositoryFilter` (order 1) | Filter | `DelegatingFilterProxy` at `/*` + ERROR | **Auto-registered** by Spring Session — set order via property |
| `requestContextHolderUpdateFilter` (order 2) | Filter | `RequestContextHolderUpdateFilter` at `/*` | **Register** via `FilterRegistrationBean` |
| `MdcServletFilter` (order 3) | Filter | `MdcServletFilter` at `/*` | **Register** via `FilterRegistrationBean` |
| `defaultCharacterEncodingFilter` (order 4) | Filter | `DefaultCharacterEncodingFilter` at `/v2/visa/intyg/*` | **Register** via `FilterRegistrationBean` |
| `sessionTimeoutFilter` (order 5) | Filter | `SessionTimeoutFilter` at `/*` + init params | **Register** via `FilterRegistrationBean` |
| `springSecurityFilterChain` (order 6) | Filter | `DelegatingFilterProxy` at `/*` | **Auto-registered** by Spring Security — set order via property |
| `principalUpdatedFilter` (order 7) | Filter | `PrincipalUpdatedFilter` at `/*` | **Register** via `FilterRegistrationBean` |
| `unitSelectedAssuranceFilter` (order 8) | Filter | `DelegatingFilterProxy` at `/api/*,/moduleapi/*` + init params | **Register** via `FilterRegistrationBean` |
| `securityHeadersFilter` (order 9) | Filter | `SecurityHeadersFilter` at `/*` | **Register** via `FilterRegistrationBean` |
| `MdcUserServletFilter` (order 10) | Filter | `MdcUserServletFilter` at `/*` | **Register** via `FilterRegistrationBean` |
| `internalApiFilter` (order 11) | Filter | `DelegatingFilterProxy` at `/internalapi/*` | **Register** via `FilterRegistrationBean` |
| `launchIdValidationFilter` (order 12) | Filter | `DelegatingFilterProxy` at `/api/*,/moduleapi/*` | **Register** via `FilterRegistrationBean` |
| `allowCorsFilter` (order 13) | Filter | `DelegatingFilterProxy` at `/api/v1/session/invalidate` | **Register** via `FilterRegistrationBean` |

### `AppConfig.java` — State Entering Step 14

| Element | Action |
|---|---|
| `@DependsOn("dbUpdate")` | **Keep** — `dbUpdate` is `@Bean(name = "dbUpdate")` in `JpaConfigBase` (not the Spring Boot auto-config bean). Valid as-is. |
| `@PropertySource("classpath:application.properties")` | **Remove** — Spring Boot auto-loads this |
| `@PropertySource("classpath:version.properties")` | **Keep** — not auto-loaded by Spring Boot |
| `@PropertySource("classpath:webcert-notification-route-params.properties")` | **Keep** — not auto-loaded by Spring Boot |
| `@PropertySource("file:${dev.config.file:-}")` | **Remove** — replaced by `spring.config.additional-location` in `bootRun` |
| `PropertySourcesPlaceholderConfigurer @Bean` (static) | **Remove** — Spring Boot auto-configures placeholder resolution |
| `@ComponentScans` with `se.inera.intyg.webcert.infra.*` paths | **Verify** — confirm paths were updated to local packages in Step 8 |
| `CookieSerializer` bean (`IneraCookieSerializer`) | **Keep** — verify import is the local inlined class, not infra |

### `web/build.gradle` — Changes Required

| Element | Current | Action |
|---|---|---|
| `apply plugin: 'war'` | Present | **Remove** |
| `apply plugin: 'org.gretty'` | Present | **Remove** |
| `gretty {}` block | Present | **Remove** |
| `war.duplicatesStrategy` | Present | **Remove** |
| `apply plugin: 'org.cyclonedx.bom'` | Present | **Keep** |
| Spring Boot plugin | Absent | **Add** `alias(libs.plugins.org.springframework.boot)` |
| `bootJar {}` config | Absent | **Add** — set `archiveFileName` |
| `bootRun {}` config | Absent | **Add** — replaces Gretty `jvmArgs` |
| `co.elastic.logging:logback-ecs-encoder` dep | Present | **Remove** — replaced by Spring Boot native ECS logging |
| `springdoc-openapi-starter-webmvc-ui` | Absent | **Add** (in 14.10) |

### New Java Files to Create

| File | Location | Purpose |
|---|---|---|
| `WebcertApplication.java` | `web/src/main/java/se/inera/intyg/webcert/` | Spring Boot main class |
| `WebServletConfig.java` | `web/src/main/java/.../webcert/web/config/` | CXF + Metrics servlet registration |
| `WebFilterConfig.java` | `web/src/main/java/.../webcert/web/config/` | 11 explicit filter registrations |

### Files to Modify

| File | Change |
|---|---|
| `web/build.gradle` | Add Spring Boot plugin; remove WAR + Gretty; add `bootRun`/`bootJar` |
| `build.gradle` (root) | Remove Gretty `apply false`; add Spring Boot plugin `apply false` |
| `web/src/main/resources/application.properties` | Add filter order properties; add SpringDoc config |
| `AppConfig.java` | Remove 2 `@PropertySource` entries; remove `PropertySourcesPlaceholderConfigurer` |
| `WebMvcConfiguration.java` | Remove `@EnableWebMvc` |
| `WebSecurityConfig.java` | Add `HttpSessionEventPublisher @Bean` |

### Files to Delete

| File | Reason |
|---|---|
| `web/src/main/webapp/WEB-INF/web.xml` | Replaced by Java config (14.11) |
| `web/tomcat-gretty.xml` | Gretty-specific, no longer needed (14.11) |
| `web/src/main/resources/logback/logback-spring-base.xml` | Replaced by Spring Boot native ECS logging (14.8) |
| `devops/dev/config/logback-spring.xml` | No longer loaded; level overrides migrated to `application.properties` (14.8) |

---

## Migration Strategy

The sub-steps are ordered so that each one can be compile-verified before proceeding, and so
that Gretty remains functional until the very last moment (the point of no return is 14.12):

1. **Gradle first (14.1)** — add Spring Boot plugin; remove WAR + Gretty from `web/build.gradle`.
   Verify the module still compiles. `bootRun` becomes available.
2. **Main class (14.2)** — create `WebcertApplication.java`. Attempt `bootRun` — it will likely
   fail with context errors, but this is expected. Use it to discover missing configuration.
3. **Servlet registration (14.3)** — register CXF and Prometheus servlets.
4. **Filter registration (14.4)** — register the 11 explicit filters. Add 2 filter-order
   properties to `application.properties`.
5. **`HttpSessionEventPublisher` bean (14.5)** — add to `WebSecurityConfig.java`.
6. **`AppConfig.java` cleanup (14.6)** — remove redundant `@PropertySource` entries and
   `PropertySourcesPlaceholderConfigurer`. Rerun `bootRun`.
7. **`WebMvcConfiguration` cleanup (14.7)** — remove `@EnableWebMvc`.
8. **Dev configuration (14.8)** — configure `bootRun` to replace Gretty `jvmArgs`;
   set `logging.config` to pick up the Logback config file.
9. **Module config wildcards (14.9)** — handle `classpath*:module-config.xml` if needed.
10. **SpringDoc + remove `swagger-jaxrs` (14.10)** — add SpringDoc; replace `io.swagger.annotations`.
11. **Remove `web.xml` + Gretty files (14.11)** — the point of no return. Only when `bootRun` is
    clean.
12. **OpenSamlConfig verification (14.12)** — confirm XXE protection active.
13. **Final verification (14.13)** — full endpoint + security checklist.

**Critical ordering note:** Steps 14.1–14.10 can be run with `bootRun` at any point to check
progress. Do NOT delete `web.xml` (step 14.11) until `bootRun` starts cleanly.

---

## Progress Tracker

| Sub-step | Title | Risk | Status |
|---|---|---|---|
| **Phase A: Gradle Setup** | | | |
| 14.1 | Add Spring Boot plugin to `web/build.gradle`; remove WAR + Gretty | Low | ⬜ |
| **Phase B: Java Bootstrap** | | | |
| 14.2 | Create `WebcertApplication.java` | Low | ⬜ |
| **Phase C: Servlet + Filter Registration** | | | |
| 14.3 | Create `WebServletConfig.java` (CXF + Metrics servlets) | Low | ⬜ |
| 14.4 | Create `WebFilterConfig.java` (11 explicit filters) | Medium | ⬜ |
| 14.5 | Add `HttpSessionEventPublisher @Bean` to `WebSecurityConfig` | Low | ⬜ |
| **Phase D: AppConfig + MVC Cleanup** | | | |
| 14.6 | Clean up `AppConfig.java` (remove 2 `@PropertySource` + `PropertySourcesPlaceholderConfigurer`) | Low | ⬜ |
| 14.7 | Remove `@EnableWebMvc` from `WebMvcConfiguration` | Low | ⬜ |
| **Phase E: Dev Config + Logging** | | | |
| 14.8 | Configure `bootRun` block; set `logging.config` | Low | ⬜ |
| **Phase F: Module Config + API Docs** | | | |
| 14.9 | Handle `classpath*:module-config.xml` wildcards (if present) | Medium | ⬜ |
| 14.10 | Add SpringDoc OpenAPI; remove `swagger-jaxrs` | Medium | ⬜ |
| **Phase G: Point of No Return** | | | |
| 14.11 | Delete `web.xml` + `tomcat-gretty.xml` | High | ⬜ |
| **Phase H: Verification** | | | |
| 14.12 | Verify `OpenSamlConfig` security settings | High | ⬜ |
| 14.13 | Final verification checklist | High | ⬜ |

**Status legend:** ⬜ Pending | 🔄 In Progress | ✅ Done | ❌ Blocked

---

## Phase A: Gradle Setup

### Sub-step 14.1 — Add Spring Boot Plugin to `web/build.gradle`; Remove WAR + Gretty

#### 1. Update `web/build.gradle` — plugin section

Change from:
```groovy
apply plugin: 'org.cyclonedx.bom'
apply plugin: 'org.gretty'
apply plugin: 'war'
```

To:
```groovy
apply plugin: 'org.cyclonedx.bom'
alias(libs.plugins.org.springframework.boot)
```

> **Note:** If the `alias(...)` syntax isn't available in an `apply`-style file, use:
> ```groovy
> apply plugin: 'org.springframework.boot'
> ```
> This works as long as the Spring Boot plugin is declared in root `build.gradle` as `apply false`.

#### 2. Add `bootJar` configuration (after plugins)

Following the `intygstjanst` field ordering — `plugins` → `bootJar config` → rest:

```groovy
tasks.named('bootJar') {
    archiveFileName.set('webcert.jar')
}
```

#### 3. Remove `gretty {}` block entirely

Delete lines 13–40 of the current `web/build.gradle`.

#### 4. Remove `war.duplicatesStrategy`

```groovy
// Remove:
war.duplicatesStrategy = DuplicatesStrategy.EXCLUDE
```

#### 5. Update root `build.gradle`

Remove Gretty from the root plugins block:
```groovy
// Remove:
id "org.gretty" version "4.1.10" apply false
```

Add Spring Boot plugin declared as `apply false`:
```groovy
alias(libs.plugins.org.springframework.boot) apply false
```

> **Note:** The Spring Boot plugin must be in the version catalog (`libs.versions.toml` or
> `configureIntygBom.gradle`). Check if `org.springframework.boot` is already available in
> `libs.plugins`. If not, declare it:
> ```groovy
> id "org.springframework.boot" version "<version>" apply false
> ```

#### 6. Set Spring Boot main class

Add the `springBoot` block in `web/build.gradle` so the plugin knows the entry point:

```groovy
springBoot {
    mainClass = 'se.inera.intyg.webcert.WebcertApplication'
}
```

### Verify (14.1)

```bash
./gradlew :webcert-web:compileJava        # still compiles
./gradlew :webcert-web:bootJar --dry-run  # task exists
grep -r "org.gretty" --include="*.gradle" .   # returns nothing
```

---

## Phase B: Java Bootstrap

### Sub-step 14.2 — Create `WebcertApplication.java`

**File:** `web/src/main/java/se/inera/intyg/webcert/WebcertApplication.java`

```java
package se.inera.intyg.webcert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication(
    scanBasePackages = {
      "se.inera.intyg.webcert",
      "se.inera.intyg.common.support.modules.support.api",
      "se.inera.intyg.common.services",
      "se.inera.intyg.common",
      "se.inera.intyg.common.support.services",
      "se.inera.intyg.common.util.integration.json"
    })
public class WebcertApplication {

  public static void main(String[] args) {
    SpringApplication.run(WebcertApplication.class, args);
  }
}
```

**Key decisions:**
- Do NOT extend `SpringBootServletInitializer` — this is an executable JAR, not a WAR.
- `scanBasePackages` covering `se.inera.intyg.webcert` picks up all submodule packages
  (web, persistence, common, notification-sender, integrations) because they all share that
  root package.
- The `se.inera.intyg.common.*` entries ensure module registry, services, and
  `CustomObjectMapper` are discovered from the common JARs on the classpath.
- `@ConfigurationPropertiesScan` is needed if any `@ConfigurationProperties` beans exist
  without explicit `@EnableConfigurationProperties`.
- Do NOT add `@EnableJpaRepositories` here — `JpaConfigBase` (kept as-is via `@Import` in
  `AppConfig`) handles repository scanning. (Relevant only in Step 15.)

**`AppConfig.java` coexistence:** `AppConfig` is already in `se.inera.intyg.webcert.web.config`
and is picked up by the component scan. Its `@ComponentScans`, `@Import`, and `@Bean` methods
continue to wire the application exactly as they did under Gretty.

**Review: `@ComponentScans` in `AppConfig`** — After Step 8, infra packages were inlined.
Confirm every `@ComponentScan` entry points to a local `se.inera.intyg.webcert.*` package
(not `se.inera.intyg.infra.*`). Scans already covered by `scanBasePackages = "se.inera.intyg.webcert"`
are redundant but harmless.

### Verify (14.2)

```bash
./gradlew :webcert-web:compileJava   # compiles without error
```

Attempt `bootRun` to see where the context fails:
```bash
./gradlew :webcert-web:bootRun
# Expected: fails (missing filter/servlet config not yet done)
# Look at the error — it will guide what to fix next
```

---

## Phase C: Servlet + Filter Registration

### Sub-step 14.3 — Create `WebServletConfig.java`

**File:** `web/src/main/java/se/inera/intyg/webcert/web/config/WebServletConfig.java`

```java
package se.inera.intyg.webcert.web.config;

import io.prometheus.client.servlet.jakarta.exporter.MetricsServlet;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebServletConfig {

  @Bean
  public ServletRegistrationBean<CXFServlet> cxfServlet() {
    var registration = new ServletRegistrationBean<>(new CXFServlet(), "/services/*");
    registration.setName("services");
    registration.setLoadOnStartup(1);
    return registration;
  }

  @Bean
  public ServletRegistrationBean<MetricsServlet> metricsServlet() {
    var registration = new ServletRegistrationBean<>(new MetricsServlet(), "/metrics");
    registration.setName("metrics");
    return registration;
  }
}
```

> **Prometheus note:** `simpleclient_hotspot` registers JVM metrics via `DefaultExports.initialize()`.
> Check if this is already called somewhere in the codebase (search for `DefaultExports`). If not,
> add a `@PostConstruct` or `ApplicationListener<ApplicationReadyEvent>` to call it. The full
> Prometheus setup is replaced in Step 17 (Micrometer/Actuator).

### Verify (14.3)

```bash
./gradlew :webcert-web:compileJava   # no error
# After bootRun is working (Phase G):
curl http://localhost:8020/services/    # CXF services page
curl http://localhost:8020/metrics      # Prometheus text output
```

---

### Sub-step 14.4 — Create `WebFilterConfig.java`

**Critical design decision — do NOT register `springSecurityFilterChain` or
`springSessionRepositoryFilter` as `FilterRegistrationBean`s.** Spring Boot auto-registers
both of these:
- Spring Security auto-registers `springSecurityFilterChain`.
- Spring Session auto-registers `springSessionRepositoryFilter`.

Double-registering them causes a startup failure (`IllegalStateException`) or broken behavior.
Instead, set their order via properties (added in sub-step 14.8):

```properties
spring.session.servlet.filter-order=1
spring.security.filter.order=6
```

Register only the 11 filters that are NOT auto-managed:

**File:** `web/src/main/java/se/inera/intyg/webcert/web/config/WebFilterConfig.java`

```java
package se.inera.intyg.webcert.web.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.DelegatingFilterProxy;
// Adjust these imports to the actual local package paths set during Step 8 inlining:
import se.inera.intyg.webcert.web.security.filter.PrincipalUpdatedFilter;
import se.inera.intyg.webcert.web.security.filter.RequestContextHolderUpdateFilter;
import se.inera.intyg.webcert.web.security.filter.SecurityHeadersFilter;
import se.inera.intyg.webcert.web.security.filter.SessionTimeoutFilter;
import se.inera.intyg.webcert.logging.MdcServletFilter;
import se.inera.intyg.webcert.web.logging.MdcUserServletFilter;
import se.inera.intyg.webcert.web.web.filter.DefaultCharacterEncodingFilter;

@Configuration
public class WebFilterConfig {

  // Order 2 — requestContextHolderUpdateFilter
  @Bean
  public FilterRegistrationBean<RequestContextHolderUpdateFilter> requestContextHolderUpdateFilter() {
    var reg = new FilterRegistrationBean<>(new RequestContextHolderUpdateFilter());
    reg.addUrlPatterns("/*");
    reg.setOrder(2);
    return reg;
  }

  // Order 3 — MdcServletFilter
  @Bean
  public FilterRegistrationBean<MdcServletFilter> mdcServletFilter() {
    var reg = new FilterRegistrationBean<>(new MdcServletFilter());
    reg.addUrlPatterns("/*");
    reg.setOrder(3);
    return reg;
  }

  // Order 4 — defaultCharacterEncodingFilter (specific URL pattern, not /*)
  @Bean
  public FilterRegistrationBean<DefaultCharacterEncodingFilter> defaultCharacterEncodingFilter() {
    var reg = new FilterRegistrationBean<>(new DefaultCharacterEncodingFilter());
    reg.addUrlPatterns("/v2/visa/intyg/*");
    reg.setOrder(4);
    return reg;
  }

  // Order 5 — sessionTimeoutFilter (with init params from web.xml)
  @Bean
  public FilterRegistrationBean<SessionTimeoutFilter> sessionTimeoutFilter() {
    var reg = new FilterRegistrationBean<>(new SessionTimeoutFilter());
    reg.addUrlPatterns("/*");
    reg.addInitParameter("skipRenewSessionUrls", "/moduleapi/stat,/api/session-auth-check/ping");
    reg.setOrder(5);
    return reg;
  }

  // Order 7 — principalUpdatedFilter
  @Bean
  public FilterRegistrationBean<PrincipalUpdatedFilter> principalUpdatedFilter() {
    var reg = new FilterRegistrationBean<>(new PrincipalUpdatedFilter());
    reg.addUrlPatterns("/*");
    reg.setOrder(7);
    return reg;
  }

  // Order 8 — unitSelectedAssuranceFilter (DelegatingFilterProxy; specific patterns + init params)
  @Bean
  public FilterRegistrationBean<DelegatingFilterProxy> unitSelectedAssuranceFilter() {
    var reg = new FilterRegistrationBean<>(new DelegatingFilterProxy("unitSelectedAssuranceFilter"));
    reg.addUrlPatterns("/api/*", "/moduleapi/*");
    reg.addInitParameter("targetFilterLifecycle", "true");
    reg.addInitParameter(
        "ignoredUrls",
        "/api/config,/api/anvandare,/api/anvandare/andraenhet,/api/jslog,/moduleapi/stat,/api/user");
    reg.setOrder(8);
    return reg;
  }

  // Order 9 — securityHeadersFilter
  @Bean
  public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilter() {
    var reg = new FilterRegistrationBean<>(new SecurityHeadersFilter());
    reg.addUrlPatterns("/*");
    reg.setOrder(9);
    return reg;
  }

  // Order 10 — MdcUserServletFilter
  @Bean
  public FilterRegistrationBean<MdcUserServletFilter> mdcUserServletFilter() {
    var reg = new FilterRegistrationBean<>(new MdcUserServletFilter());
    reg.addUrlPatterns("/*");
    reg.setOrder(10);
    return reg;
  }

  // Order 11 — internalApiFilter (DelegatingFilterProxy; /internalapi/* only)
  @Bean
  public FilterRegistrationBean<DelegatingFilterProxy> internalApiFilter() {
    var reg = new FilterRegistrationBean<>(new DelegatingFilterProxy("internalApiFilter"));
    reg.addUrlPatterns("/internalapi/*");
    reg.setOrder(11);
    return reg;
  }

  // Order 12 — launchIdValidationFilter (DelegatingFilterProxy; /api/* + /moduleapi/*)
  @Bean
  public FilterRegistrationBean<DelegatingFilterProxy> launchIdValidationFilter() {
    var reg = new FilterRegistrationBean<>(new DelegatingFilterProxy("launchIdValidationFilter"));
    reg.addUrlPatterns("/api/*", "/moduleapi/*");
    reg.setOrder(12);
    return reg;
  }

  // Order 13 — allowCorsFilter (DelegatingFilterProxy; specific URL only)
  @Bean
  public FilterRegistrationBean<DelegatingFilterProxy> allowCorsFilter() {
    var reg = new FilterRegistrationBean<>(new DelegatingFilterProxy("allowCorsFilter"));
    reg.addUrlPatterns("/api/v1/session/invalidate");
    reg.setOrder(13);
    return reg;
  }
}
```

> **Package path note:** The import paths for `RequestContextHolderUpdateFilter`,
> `PrincipalUpdatedFilter`, `SecurityHeadersFilter`, and `SessionTimeoutFilter` depend on
> where they were placed during Step 8 inlining. Adjust the imports to the actual local paths.

> **`DelegatingFilterProxy` bean name matching:** `unitSelectedAssuranceFilter`,
> `internalApiFilter`, `launchIdValidationFilter`, and `allowCorsFilter` resolve beans by
> name from the Spring context. Verify that Spring beans with exactly these names exist.

### Verify (14.4)

```bash
./gradlew :webcert-web:compileJava   # no error
```

---

### Sub-step 14.5 — Add `HttpSessionEventPublisher @Bean`

**Why:** `web.xml` registered `HttpSessionEventPublisher` as a servlet context listener. It
publishes `HttpSessionCreatedEvent` and `HttpSessionDestroyedEvent` to keep the Spring Security
`SessionRegistry` synchronized (used to track active sessions). Under Spring Boot, listeners
must be registered as Spring beans.

**Where:** Add to `WebSecurityConfig.java`.

```java
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Bean
public HttpSessionEventPublisher httpSessionEventPublisher() {
    return new HttpSessionEventPublisher();
}
```

### Verify (14.5)

```bash
./gradlew :webcert-web:compileJava   # no error
# After bootRun: log in + log out → confirm session correctly removed from registry
```

---

## Phase D: AppConfig + MVC Cleanup

### Sub-step 14.6 — Clean Up `AppConfig.java`

Three targeted changes — nothing else in `AppConfig` is touched:

#### 1. Remove `@PropertySource("classpath:application.properties")`

```java
// Remove this line only:
@PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = true),
```

Spring Boot auto-loads `application.properties` from the classpath. Keep the other two:
```java
// Keep both of these:
@PropertySource(value = "classpath:version.properties", ignoreResourceNotFound = true),
@PropertySource(value = "classpath:webcert-notification-route-params.properties", ignoreResourceNotFound = true),
```

#### 2. Remove `@PropertySource("file:${dev.config.file:-}")`

```java
// Remove this line:
@PropertySource(value = "file:${dev.config.file:-}", ignoreResourceNotFound = true),
```

This is replaced by `spring.config.additional-location` in the `bootRun` block (sub-step 14.8).

#### 3. Remove `PropertySourcesPlaceholderConfigurer @Bean`

Spring Boot registers its own automatically.

```java
// Remove entirely:
@Bean
public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
    PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
    configurer.setIgnoreUnresolvablePlaceholders(true);
    configurer.setIgnoreResourceNotFound(true);
    return configurer;
}
```

> **⚠️ Warning:** Spring Boot's `PropertySourcesPlaceholderConfigurer` uses
> `setIgnoreUnresolvablePlaceholders(false)` by default. If the application has optional
> `@Value` expressions that reference undefined properties, add to `application.properties`:
> ```properties
> spring.config.use-legacy-processing=false
> ```
> Or annotate with `@Value("${property:defaultValue}")` to supply defaults.

### Verify (14.6)

```bash
./gradlew :webcert-web:compileJava   # no error
./gradlew :webcert-web:bootRun
# No "Could not resolve placeholder" errors in startup log
```

---

### Sub-step 14.7 — Remove `@EnableWebMvc` from `WebMvcConfiguration`

**Why:** `@EnableWebMvc` disables Spring Boot's MVC auto-configuration entirely. This breaks
auto-configured `ContentNegotiationStrategy`, `ViewResolver`, static resource handling, and more.
The `WebMvcConfigurer` interface (which `WebMvcConfiguration` already implements) is the correct
way to customise MVC under Spring Boot without disabling auto-config.

**Change:**
```java
// Remove:
@EnableWebMvc
```

Keep everything else in `WebMvcConfiguration.java`:
- `@Configuration`
- `@ComponentScan` annotations
- `WebMvcConfigurer` implementation
- `configureMessageConverters()` (registers `CustomObjectMapper` — still valid)

### Verify (14.7)

```bash
./gradlew :webcert-web:compileJava   # no error
./gradlew :webcert-web:bootRun
# JSON API responses use CustomObjectMapper settings (ISO dates, no nulls)
```

---

## Phase E: Dev Config + Logging

### Sub-step 14.8 — Configure `bootRun` Block + Replace Logback with Spring Boot ECS Logging

#### 1. Remove `logback-ecs-encoder` from `web/build.gradle`

Spring Boot 3.4+ has native ECS structured logging support that requires no external encoder.

```groovy
// Remove from dependencies:
implementation "co.elastic.logging:logback-ecs-encoder"
```

#### 2. Add `bootRun` block to `web/build.gradle`

The Gretty `jvmArgs` become `bootRun` system properties. Following the `intygstjanst` pattern,
`bootRun` is placed after the `dependencies` block. Note: no `logging.config` system property
— Spring Boot's built-in ECS logging replaces the custom Logback file entirely.

```groovy
def applicationDir = "${rootProject.projectDir}/devops/dev"

bootRun {
    systemProperty("application.dir", applicationDir)
    systemProperty("spring.profiles.active",
        "dev,testability-api,caching-enabled,ia-stub,certificate-analytics-service-active")
    systemProperty("spring.config.additional-location", "file:${applicationDir}/config/")
    systemProperty("java.awt.headless", "true")
    systemProperty("file.encoding", "UTF-8")
    systemProperty("xml.catalog.cacheEnabled", "false")
}

tasks.register("appRunDebug") {
    println("######## Running in Debug mode ########")
    doFirst {
        bootRun.configure {
            jvmArgs = ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8820"]
        }
    }
    finalizedBy("bootRun")
}

tasks.register("appRun") {
    println("######## Running in normal mode ########")
    finalizedBy("bootRun")
}
```

> **Dropped Gretty properties:**
> - `dev.http.port` / `dev.http.port.internal` — Gretty-specific. Spring Boot uses `server.port`.
> - `catalina.base` — Gretty-specific, no longer needed.
> - `dev.config.file` — replaced by `spring.config.additional-location`.
> - `logback.file` — replaced by Spring Boot native ECS logging (no custom logback file).

#### 3. Update `web/src/main/resources/application.properties`

```properties
# Server port (was controlled by Gretty dev.http.port)
server.port=8020

# Filter ordering (replaces web.xml filter declaration order)
spring.session.servlet.filter-order=1
spring.security.filter.order=6

# ECS structured logging (production default)
logging.structured.format.console=ecs

# Logger level overrides (migrated from devops/dev/config/logback-spring.xml)
logging.level.org.apache.cxf.interceptor=WARN
logging.level.org.apache.cxf.services=WARN
logging.level.org.apache.cxf.ws.addressing=WARN

# SpringDoc API documentation
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/v3/api-docs
```

#### 4. Disable ECS logging in dev (plain text output)

In `devops/dev/config/application-dev.properties`, set an empty value to override the
production default and revert to Spring Boot's plain console format:

```properties
# In devops/dev/config/application-dev.properties:
logging.structured.format.console=
```

An empty value disables ECS logging. Developers see readable plain-text logs locally.
The production environment keeps `logging.structured.format.console=ecs` from
`application.properties` (overrides must be supplied via environment variable or ConfigMap).

#### 5. Delete old Logback files

The custom Logback setup is fully replaced by Spring Boot's built-in logging:

```bash
rm web/src/main/resources/logback/logback-spring-base.xml
rmdir web/src/main/resources/logback   # if now empty
```

`devops/dev/config/logback-spring.xml` — this file is no longer loaded (no `logging.config`
system property points to it). Delete it to avoid confusion:

```bash
rm devops/dev/config/logback-spring.xml
```

> **Note:** If `devops/dev/config/logback-spring.xml` still has logger level configurations
> not yet migrated to `application.properties`, move them first (step 4 above covers the
> CXF suppressions). Then delete.

### Verify (14.8)

```bash
./gradlew :webcert-web:bootRun
# Application starts on port 8020
# Log output is plain text (not JSON) in dev
grep "co.elastic.logging" web/build.gradle   # returns nothing
curl http://localhost:8020/api/config         # no CXF interceptor noise in console
```

---

## Phase F: Module Config + API Docs

### Sub-step 14.9 — Handle `classpath*:module-config.xml` Wildcards

**Check first:**
```bash
grep -n "ImportResource" web/src/main/java/se/inera/intyg/webcert/web/config/AppConfig.java
```

After Step 13, `AppConfig.java` should have **no `@ImportResource`**. If this is confirmed,
this sub-step is a no-op. Skip to 14.10.

If `@ImportResource("classpath*:module-config.xml")` is still present, apply the strategy
from Step 0:

**Option A — Keep `@ImportResource` (safe, zero risk):** If `se.inera.intyg.common:*` JARs
still ship `module-config.xml` files, the `@ImportResource` bridge is valid under Spring Boot.

**Option B — Remove `@ImportResource` (if common modules provide `@Configuration` classes):**
`@SpringBootApplication(scanBasePackages = "se.inera.intyg.common")` already discovers
`@Configuration` classes from all JARs. If `module-config.xml` only wires things that
`@Configuration` classes can provide, the annotation is redundant.

### Verify (14.9)

```bash
# All certificate type modules are available:
curl http://localhost:8020/api/modules/map
```

---

### Sub-step 14.10 — Add SpringDoc OpenAPI; Remove `swagger-jaxrs`

**Why:** `io.swagger:swagger-jaxrs` was intentionally kept in Step 11 because controllers
still used `io.swagger.annotations.*` imports. Now that we are on Spring Boot, we add
SpringDoc OpenAPI 3 and remove the old library.

#### 1. Update `web/build.gradle` — dependencies

```groovy
// Remove:
implementation("io.swagger:swagger-jaxrs") { exclude(module: "jsr311-api") }

// Add (in the third-party implementation group, alphabetical order):
implementation "org.springdoc:springdoc-openapi-starter-webmvc-ui"
```

Following the `intygstjanst` field ordering, the `dependencies` block groups are:
1. Project module deps
2. Spring Boot starters
3. Inera schema libraries (by schema family)
4. Inera `se.inera.intyg.common:*` modules
5. Third-party `implementation` (alphabetical) ← `springdoc` goes here
6. `annotationProcessor`
7. `compileOnly`
8. `runtimeOnly`
9. Test dependencies

#### 2. Replace `io.swagger.annotations.*` imports in all Java files

Check the count:
```bash
grep -r "io.swagger.annotations" --include="*.java" web/src/main/java/ | wc -l
```

Replace with OpenAPI 3 equivalents:

| Old (`io.swagger.annotations`) | New (`io.swagger.v3.oas.annotations`) |
|---|---|
| `@Api(tags = "...")` | `@Tag(name = "...")` |
| `@ApiOperation(value = "...")` | `@Operation(summary = "...")` |
| `@ApiParam` | `@Parameter` |
| `@ApiModel` | `@Schema` |
| `@ApiModelProperty` | `@Schema` |
| `@ApiResponse` | `@io.swagger.v3.oas.annotations.responses.ApiResponse` |

If API documentation is being rebuilt from scratch, simply remove all old annotations —
SpringDoc generates documentation from Spring MVC annotations alone.

#### 3. Verify zero old annotations remain

```bash
grep -r "io.swagger.annotations" --include="*.java" web/src/main/java/
# Must return no output
```

### Verify (14.10)

```bash
./gradlew :webcert-web:compileJava   # no error; no swagger-jaxrs on classpath
# After bootRun:
curl http://localhost:8020/swagger-ui/index.html   # SpringDoc UI loads
curl http://localhost:8020/v3/api-docs             # OpenAPI 3 JSON
```

---

## Phase G: Point of No Return

### Sub-step 14.11 — Delete `web.xml` + `tomcat-gretty.xml`

**Only perform this sub-step after** `./gradlew :webcert-web:bootRun` starts cleanly and the
full Phase H verification (14.13) passes.

#### 1. Delete `web.xml`

```bash
rm web/src/main/webapp/WEB-INF/web.xml
rmdir web/src/main/webapp/WEB-INF   # if now empty
rmdir web/src/main/webapp            # if now empty
```

#### 2. Delete `tomcat-gretty.xml`

```bash
rm web/tomcat-gretty.xml
```

### Verify (14.11)

```bash
find . -name "web.xml"           # returns nothing
find . -name "tomcat-gretty.xml" # returns nothing
./gradlew :webcert-web:bootRun   # still starts cleanly
./gradlew test                   # all tests pass
```

---

## Phase H: Verification

### Sub-step 14.12 — Verify `OpenSamlConfig` Security Settings

**Background:** `OpenSamlConfig.java` implements `InitializingBean`. Its `afterPropertiesSet()`
is called by the Spring container during context initialization and sets up OpenSAML with:
- XXE protection enabled
- Entity expansion prevention
- Parser pool size: 100

**Risk:** Spring Boot's `Saml2AutoConfiguration` may call `OpenSamlInitializationService.initialize()`
which could reset settings.

**Actions:**
1. Confirm `OpenSamlConfig` is found by component scan — it is in
   `se.inera.intyg.webcert.web.config`, covered by `scanBasePackages = "se.inera.intyg.webcert"`.
2. Check startup logs for the OpenSAML initialization message.
3. If Spring Boot SAML auto-config overrides the settings, use one of:
   - Add `@Order(Ordered.HIGHEST_PRECEDENCE)` to `OpenSamlConfig`
   - Or exclude: `@SpringBootApplication(exclude = {Saml2AutoConfiguration.class})`
4. Test: trigger a SAML authentication via browser. If it succeeds, OpenSAML is correctly
   initialized (XXE vulnerability would cause XML parsing errors, not silent auth failure).

### Verify (14.12)

```bash
grep "OpenSAML\|ParserPool\|parser pool" logs/application.log
# Manual: SAML login succeeds via browser
```

---

### Sub-step 14.13 — Final Verification Checklist

#### Compilation and tests

```bash
./gradlew :webcert-web:compileJava   # BUILD SUCCESSFUL
./gradlew test                        # all tests pass
```

#### Startup

```bash
./gradlew :webcert-web:bootRun
```

Confirm in the startup log:
- `Started WebcertApplication in X.XXX seconds` ✓
- Liquibase runs without errors ✓
- No `BeanDefinitionException` or `NoSuchBeanDefinitionException` ✓
- No `IllegalStateException` from filter double-registration ✓
- CXF endpoints registered at `/services/` ✓

#### Endpoint checklist

```bash
# Security — unauthenticated should redirect, not 404
curl -v http://localhost:8020/api/anvandare
# → 302 redirect to SAML login

# CXF SOAP services
curl http://localhost:8020/services/
# → CXF services HTML or WSDL listing

# Prometheus metrics
curl http://localhost:8020/metrics
# → Prometheus text format; jvm_* metrics present

# SpringDoc
curl http://localhost:8020/swagger-ui/index.html
# → SpringDoc HTML loads
curl http://localhost:8020/v3/api-docs
# → OpenAPI 3 JSON

# Session ping (used by sessionTimeoutFilter skip logic)
curl http://localhost:8020/api/session-auth-check/ping
# → some response (not 404)

# Internal API (should be 401/403, not 404)
curl http://localhost:8020/internalapi/
```

#### Negative checks

```bash
# No old Swagger annotations
grep -r "io.swagger.annotations" --include="*.java" web/src/main/java/
# → no output

# No Gretty
grep -r "org.gretty" --include="*.gradle" .
# → no output

# No web.xml
find . -name "web.xml"
# → no output

# No WAR plugin
grep -r "apply plugin: 'war'" --include="*.gradle" .
# → no output

# No swagger-jaxrs
grep -r "swagger-jaxrs" --include="*.gradle" .
# → no output
```

#### Manual verification

- [ ] SITHS login via browser → authentication completes → session created
- [ ] Session timeout: idle → redirected to login page
- [ ] Role-based access: normal user blocked from admin endpoint
- [ ] `IneraCookieSerializer`: correct domain on session cookie (check DevTools)
- [ ] JSON format: dates as ISO strings, null fields absent from response body
- [ ] SOAP operation at `/services/` → correct response (invoke via SoapUI or curl)

#### Performance baseline

Record before proceeding to Step 15:
- Startup time: `Started WebcertApplication in X.XXX seconds`
- Heap after startup: `curl http://localhost:8020/metrics | grep jvm_memory_used`
- `GET /api/config` latency: 10 requests, p50/p95

---

## Common Pitfalls

| Pitfall | Symptom | Mitigation |
|---|---|---|
| `@EnableWebMvc` left in `WebMvcConfiguration` | Static resource serving broken; auto-config disabled | Remove `@EnableWebMvc` (sub-step 14.7) |
| `springSecurityFilterChain` in `FilterRegistrationBean` | `IllegalStateException` on startup — double registration | Use `spring.security.filter.order=6` property; remove from `WebFilterConfig` (sub-step 14.4) |
| `springSessionRepositoryFilter` in `FilterRegistrationBean` | Session handling broken or startup failure | Use `spring.session.servlet.filter-order=1` property; remove from `WebFilterConfig` (sub-step 14.4) |
| `PropertySourcesPlaceholderConfigurer` duplicate | `@Value` placeholder resolution errors | Remove explicit `@Bean` from `AppConfig` (sub-step 14.6) |
| `classpath*:module-config.xml` not loaded | Certificate type modules not registered | Keep `@ImportResource` or verify component scan covers common packages (sub-step 14.9) |
| OpenSAML security settings overridden | XXE protection silently lost | Add `@Order(Ordered.HIGHEST_PRECEDENCE)` to `OpenSamlConfig` (sub-step 14.12) |
| `version.properties` or `webcert-notification-route-params.properties` not loaded | Missing property values at runtime | Keep `@PropertySource` entries for these in `AppConfig` — only remove `application.properties` and `dev.config.file` entries (sub-step 14.6) |
| Logback config not applied | Default console logging format | Use Spring Boot native ECS logging: `logging.structured.format.console=ecs` in `application.properties`; set empty in dev to get plain text (sub-step 14.8) |
| `HttpSessionEventPublisher` missing | Sessions not removed from `SessionRegistry`; potential memory leak | Add `@Bean` to `WebSecurityConfig.java` (sub-step 14.5) |
| Spring Boot plugin not in version catalog | `Plugin with id 'org.springframework.boot' not found` | Declare in root `build.gradle` with explicit version: `id "org.springframework.boot" version "3.4.x" apply false` |
| `server.port` not set | Application starts on port 8080 instead of 8020 | Add `server.port=8020` to `application.properties` (sub-step 14.8) |

---

## Rollback Plan

All sub-steps up to 14.11 (delete `web.xml`) are reversible via Git. If any phase fails,
revert the specific changes and run `./gradlew :webcert-web:appRun` to confirm Gretty still works.

After 14.11 is done, rollback requires:
```bash
git checkout -- web/src/main/webapp/WEB-INF/web.xml
git checkout -- web/tomcat-gretty.xml
git checkout -- web/build.gradle   # restore gretty + war plugins
./gradlew :webcert-web:appRun
```

**Key principle:** Keep `web.xml` and `tomcat-gretty.xml` in Git until `bootRun` is fully
verified. Only delete them in 14.11 after the complete Phase H verification passes.

---

## Files Changed Summary

| File | Action |
|---|---|
| `web/src/main/java/se/inera/intyg/webcert/WebcertApplication.java` | **Create** |
| `web/src/main/java/.../config/WebServletConfig.java` | **Create** |
| `web/src/main/java/.../config/WebFilterConfig.java` | **Create** |
| `web/build.gradle` | **Modify** — add Spring Boot plugin + `bootJar` + `bootRun`; remove WAR, Gretty, `war.duplicatesStrategy`; remove `logback-ecs-encoder` dep; add `springdoc-openapi-starter-webmvc-ui` |
| `build.gradle` (root) | **Modify** — remove Gretty `apply false`; add Spring Boot `apply false` |
| `web/src/main/resources/application.properties` | **Modify** — add `server.port`, filter order props, ECS logging, logger levels, SpringDoc config |
| `web/src/main/java/.../config/AppConfig.java` | **Modify** — remove 2 `@PropertySource` + `PropertySourcesPlaceholderConfigurer` |
| `web/src/main/java/.../config/WebMvcConfiguration.java` | **Modify** — remove `@EnableWebMvc` |
| `web/src/main/java/.../config/WebSecurityConfig.java` | **Modify** — add `HttpSessionEventPublisher @Bean` |
| `devops/dev/config/application-dev.properties` | **Modify** — add `logging.structured.format.console=` (empty, disables ECS in dev) |
| `web/src/main/webapp/WEB-INF/web.xml` | **Delete** (in 14.11) |
| `web/tomcat-gretty.xml` | **Delete** (in 14.11) |
| `web/src/main/resources/logback/logback-spring-base.xml` | **Delete** (in 14.8) |
| `devops/dev/config/logback-spring.xml` | **Delete** (in 14.8, after migrating level overrides) |
