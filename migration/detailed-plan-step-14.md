# Step 14 — Spring Boot Bootstrap + web.xml Removal

## Problem Statement

The `webcert-web` module runs as a traditional WAR on Gretty/Tomcat 10. After Steps 1–13,
all Spring XML configs have been converted to Java `@Configuration`, all JAX-RS endpoints
have been migrated to Spring MVC, and Camel XML routes have been converted to Java DSL.
The application still bootstraps via `web.xml`, which defines:

- A root Spring context via `ContextLoaderListener` pointing to `AppConfig`
- A `DispatcherServlet` (mapped to `/`) with `WebMvcConfiguration` as its child context
- A `CXFServlet` (mapped to `/services/*`) with `CxfEndpointConfig` as its child context
- A Prometheus `MetricsServlet` (mapped to `/metrics`)
- 13 servlet filters in a specific order with URL patterns and init-params
- 4 listeners (logback, Spring context, request context, session events)

**Goal:** Replace the entire `web.xml` with a `@SpringBootApplication` main class, programmatic
servlet/filter registration, and Spring Boot starters. After this step:

- `./gradlew bootRun` starts the application via embedded Tomcat
- All REST, SOAP, and metric endpoints respond identically to pre-migration
- All 13 filters execute in the same order with the same URL patterns
- OpenSAML security hardening (`OpenSamlConfig`) is preserved
- API documentation is available via SpringDoc OpenAPI (replacing Swagger JAX-RS)
- The Gretty plugin and `web.xml` are deleted

**Not in scope for this step:**
- Replacing JPA manual config with Spring Boot auto-configuration → Step 15
- Replacing JMS manual config with Spring Boot auto-configuration → Step 16
- Replacing Prometheus with Actuator/Micrometer → Step 17
- Replacing Redis/Mail/Logging config → Step 18
- Dockerfile update → Step 19

---

## Pre-Conditions (Step 13 Must Be Complete)

Verify each before beginning.

| Pre-condition | Verified by |
|---|---|
| Step 13 is fully done — no `@ImportResource` in production code | `grep -rn "@ImportResource" web/src/main/java/ notification-sender/src/main/java/` returns nothing |
| `AppConfig.java` has no `@ImportResource` | `grep -n "ImportResource" web/src/main/java/se/inera/intyg/webcert/web/config/AppConfig.java` returns nothing |
| No Spring XML config files remain in production resources (excluding logback, Liquibase, test fixtures) | `Get-ChildItem -Recurse -Filter "*.xml" web\src\main\resources | Where-Object { $_.Name -notmatch "logback\|changelog\|test\.xml" }` returns nothing |
| All tests pass | `./gradlew test` + `./gradlew :notification-sender:camelTest` |
| Application starts on Gretty | `./gradlew appRun` — confirms current state works |

---

## Current State

### web.xml Structure

| Element | Configuration | Spring Boot Replacement |
|---|---|---|
| **Root Context** | `ContextLoaderListener` → `AppConfig` | `@SpringBootApplication` auto-creates context |
| **DispatcherServlet** (`/`) | Child context → `WebMvcConfiguration` | Spring Boot auto-registers DispatcherServlet |
| **CXFServlet** (`/services/*`) | Child context → `CxfEndpointConfig` | `ServletRegistrationBean<CXFServlet>` |
| **MetricsServlet** (`/metrics`) | `io.prometheus.client.servlet.jakarta.exporter.MetricsServlet` | `ServletRegistrationBean<MetricsServlet>` |
| **Listeners** (4) | Logback, ContextLoader, RequestContext, HttpSessionEventPublisher | Spring Boot handles all except custom logback listener |
| **Filters** (13) | Explicit ordering via declaration order | `FilterRegistrationBean` with explicit `setOrder()` |

### Filter Chain (web.xml Declaration Order)

| Order | Filter Name | Class | URL Pattern | Dispatcher | Init-Params |
|---|---|---|---|---|---|
| 1 | `springSessionRepositoryFilter` | `DelegatingFilterProxy` | `/*` | REQUEST, ERROR | — |
| 2 | `requestContextHolderUpdateFilter` | `RequestContextHolderUpdateFilter` (direct) | `/*` | REQUEST | — |
| 3 | `MdcServletFilter` | `MdcServletFilter` (direct) | `/*` | REQUEST | — |
| 4 | `defaultCharacterEncodingFilter` | `DefaultCharacterEncodingFilter` (direct) | `/v2/visa/intyg/*` | REQUEST | — |
| 5 | `sessionTimeoutFilter` | `SessionTimeoutFilter` (direct) | `/*` | REQUEST | `skipRenewSessionUrls=/moduleapi/stat,/api/session-auth-check/ping` |
| 6 | `springSecurityFilterChain` | `DelegatingFilterProxy` | `/*` | REQUEST | — |
| 7 | `principalUpdatedFilter` | `PrincipalUpdatedFilter` (direct) | `/*` | REQUEST | — |
| 8 | `unitSelectedAssuranceFilter` | `DelegatingFilterProxy` (`targetFilterLifecycle=true`) | `/api/*`, `/moduleapi/*` | REQUEST | `ignoredUrls=/api/config,...` |
| 9 | `securityHeadersFilter` | `SecurityHeadersFilter` (direct) | `/*` | REQUEST | — |
| 10 | `MdcUserServletFilter` | `MdcUserServletFilter` (direct) | `/*` | REQUEST | — |
| 11 | `internalApiFilter` | `DelegatingFilterProxy` | `/internalapi/*` | REQUEST | — |
| 12 | `launchIdValidationFilter` | `DelegatingFilterProxy` | `/api/*`, `/moduleapi/*` | REQUEST | — |
| 13 | `allowCorsFilter` | `DelegatingFilterProxy` | `/api/v1/session/invalidate` | REQUEST | — |

### Three-Tier Context Architecture (Current)

```
Root Context (ContextLoaderListener → AppConfig)
├── All beans from @ComponentScans (18 packages)
├── All beans from @Import (14 config classes)
├── SpringBus, ObjectMapper, CookieSerializer, etc.
│
├── Child: DispatcherServlet (WebMvcConfiguration)
│   ├── @EnableWebMvc
│   ├── @ComponentScan("se.inera.intyg.webcert.web.web.controller")
│   ├── @ComponentScan("se.inera.intyg.webcert.notificationstub")
│   ├── @ComponentScan("se.inera.intyg.webcert.integration.fmb.stub")
│   ├── @ComponentScan("se.inera.intyg.webcert.integration.servicenow.stub.*")
│   └── Customizes Jackson HttpMessageConverter
│
└── Child: CXFServlet (CxfEndpointConfig)
    ├── 6 SOAP EndpointImpl beans
    ├── @Import(NotificationStubConfig.class)
    ├── @ComponentScan("se.inera.intyg.webcert.infra.srs.stub.config")
    └── @ComponentScan("se.inera.intyg.webcert.infra.ia.stub.config")
```

### Deferred Items from Previous Steps

| Item | Source | What to Do |
|---|---|---|
| **Swagger → SpringDoc** | Step 11 deferred | Remove `io.swagger:swagger-jaxrs`, replace 27 files of `io.swagger.annotations.*` with SpringDoc OpenAPI 3 annotations, add `springdoc-openapi-starter-webmvc-ui` |
| **Redundant `@EnableScheduling`** | Step 19 noted | Remove from `FmbServiceImpl.java` — `JobConfig.java` already enables it globally |
| **CXF child-context stub scans** | Step 11 deferred | Move SRS/IA stub `@ComponentScan` from `CxfEndpointConfig` to profile-gated main context config when CXF child context is dissolved |
| **External module `classpath*:` imports** | Step 12/14 | Certificate module configs (`classpath*:module-config.xml`) — add `@ImportResource` bridge or expand `@ComponentScan` |

### Existing Java Configs That Must Be Preserved

| Config Class | Key Concerns |
|---|---|
| `AppConfig` | `@DependsOn("dbUpdate")`, `TransactionManagementConfigurer`, 18 `@ComponentScans`, 14 `@Import`s, `@PropertySources` |
| `WebMvcConfiguration` | `@EnableWebMvc`, stub component scans, `extendMessageConverters` |
| `WebSecurityConfig` | `@EnableWebSecurity`, `@EnableRedisIndexedHttpSession`, SAML 2.0, CSRF |
| `OpenSamlConfig` | `@Component` + `InitializingBean`, XXE protection, parser pool |
| `JpaConfigBase` | Manual `EntityManagerFactory`, HikariCP, Liquibase `dbUpdate` bean |
| `JmsConfig` | Manual `ConnectionFactory`, `JmsListenerContainerFactory`, queue templates |
| `CacheConfig` | Redis cache config, profile-gated |
| `LoggingConfig` | `@EnablePrometheusTiming`, `MetricsServlet` bean, `LogMDCHelper` |
| `CxfEndpointConfig` | 6 SOAP endpoints, stub imports/scans — will lose child context |
| `CxfWsClientConfig` | 17 JAXWS clients, TLS conduit, profile `!dev` |
| `Fk7263WcCxfConfig` | 2 JAXWS client beans (replaces wc-module-cxf-servlet.xml) |
| `TsDiabetesWcCxfConfig` | 2 JAXWS client beans (replaces wc-module-cxf-servlet.xml) |

---

## Migration Strategy

1. **Gradle first** — Add Spring Boot plugin, add starters, keep Gretty temporarily for
   rollback safety. Change packaging from `war` to Spring Boot executable JAR.
2. **Create `WebcertApplication.java`** — `@SpringBootApplication` with selective auto-config
   exclusions. Merge the `AppConfig` responsibility into this class or keep `AppConfig` as an
   imported `@Configuration`. Start minimal: exclude JPA, JMS, Redis, Mail auto-config so
   existing manual configs are not disturbed.
3. **Flatten context hierarchy** — Spring Boot uses a single application context (no
   parent/child split). Merge `WebMvcConfiguration` and `CxfEndpointConfig` into the root
   context. This means stub component scans from `CxfEndpointConfig` and
   `WebMvcConfiguration` must be profile-gated to avoid loading stubs in production.
4. **Servlet registration** — Register CXF and Prometheus servlets via `ServletRegistrationBean`.
5. **Filter registration** — Register all 13 filters via `FilterRegistrationBean` with explicit
   ordering, preserving URL patterns and init-params. Disable auto-registration for
   `@Component` filters to prevent double-registration.
6. **External module configs** — Add `@ImportResource("classpath*:module-config.xml")` bridge
   on `AppConfig` to preserve backward compatibility with certificate module JARs.
7. **Swagger → SpringDoc** — Remove Swagger JAX-RS, add SpringDoc, migrate annotations.
8. **Fix deferred items** — Remove redundant `@EnableScheduling`, clean up stubs.
9. **Remove web.xml + Gretty** — Final cleanup after everything works.

### Context Flattening Strategy

Spring Boot's embedded Tomcat creates a single `WebApplicationContext`. The current three-tier
hierarchy (root → DispatcherServlet child → CXF child) must be flattened:

**DispatcherServlet context (`WebMvcConfiguration`):**
- `@EnableWebMvc` conflicts with Spring Boot's `WebMvcAutoConfiguration`. Two options:
  - **Option A:** Remove `@EnableWebMvc` and use Spring Boot auto-config. Keep `WebMvcConfigurer`
    for `extendMessageConverters`. This is cleaner but changes MVC behavior slightly.
  - **Option B:** Keep `@EnableWebMvc` and exclude `WebMvcAutoConfiguration`. This preserves
    exact current behavior but opts out of Spring Boot MVC features.
- **Recommended: Option A** — Remove `@EnableWebMvc`, keep `WebMvcConfigurer`. Spring Boot's
  auto-config + `WebMvcConfigurer` provides the same functionality plus defaults.
- Stub component scans (`notificationstub`, `fmb.stub`, `servicenow.stub`) are currently in
  the child context. Move them to main context with `@Profile` guards if not already guarded.

**CXF servlet context (`CxfEndpointConfig`):**
- Currently runs as a child context of the CXF servlet.
- Under Spring Boot, CXF endpoints must be registered in the main context.
- The `CXFServlet` is registered via `ServletRegistrationBean` and will discover the `Bus`
  and endpoint beans from the main context automatically.
- Stub scans (`srs.stub.config`, `ia.stub.config`) and `@Import(NotificationStubConfig.class)`
  become part of the root context — verify they are profile-gated.

---

## Progress Tracker

| Sub-step | Title | Risk | Status |
|---|---|---|---|
| **Phase A: Gradle Build Configuration** | | | |
| 14.1 | Add Spring Boot plugin and starters to `web/build.gradle` | Medium | ⬜ |
| 14.2 | Update root `build.gradle` for Spring Boot BOM compatibility | Low | ⬜ |
| **Phase B: Create Spring Boot Application Class** | | | |
| 14.3 | Create `WebcertApplication.java` with `@SpringBootApplication` | Medium | ⬜ |
| 14.4 | Merge `WebMvcConfiguration` into single-context model | Medium | ⬜ |
| 14.5 | Merge `CxfEndpointConfig` into single-context model | Medium | ⬜ |
| **Phase C: Servlet and Filter Registration** | | | |
| 14.6 | Create `WebServletConfig.java` — register CXF and Prometheus servlets | Low | ⬜ |
| 14.7 | Create `WebFilterConfig.java` — register all 13 filters | High | ⬜ |
| **Phase D: Handle External Module Configs** | | | |
| 14.8 | Add `@ImportResource("classpath*:module-config.xml")` bridge | Low | ⬜ |
| **Phase E: Swagger → SpringDoc Migration** | | | |
| 14.9 | Add SpringDoc dependency, remove Swagger JAX-RS dependency | Low | ⬜ |
| 14.10 | Migrate Swagger annotations to OpenAPI 3 in all 27 files | Medium | ⬜ |
| **Phase F: Fix Deferred Items** | | | |
| 14.11 | Remove redundant `@EnableScheduling` from `FmbServiceImpl` | Low | ⬜ |
| 14.12 | Profile-gate stub component scans after context flattening | Medium | ⬜ |
| **Phase G: Remove web.xml and Gretty** | | | |
| 14.13 | Delete `web.xml`, `tomcat-gretty.xml`, remove Gretty plugin | Low | ⬜ |
| 14.14 | Final verification and cleanup | Low | ⬜ |

---

## Phase A: Gradle Build Configuration

### Sub-step 14.1 — Add Spring Boot plugin and starters to `web/build.gradle`

**What:** Add the Spring Boot Gradle plugin, Spring Boot starters, and remove the `war` and
Gretty plugins. Change the build to produce a Spring Boot executable JAR.

**Why:** Spring Boot manages embedded Tomcat, auto-configuration, and the application
lifecycle. The `war` plugin and Gretty are no longer needed.

**Changes to `web/build.gradle`:**

1. Add the Spring Boot plugin (use BOM's version catalog — **do NOT hardcode version**):
```gradle
plugins {
    alias(libs.plugins.org.cyclonedx.bom)
    alias(libs.plugins.org.springframework.boot)
    // Do NOT add io.spring.dependency-management — the Inera platform BOM
    // already imports spring-boot-dependencies:3.5.10. Adding the plugin
    // would apply a SECOND layer of BOM version management, causing conflicts.
}
```

> ⚠️ **Spring Boot version:** The Inera platform BOM (`se.inera.intyg.bom:platform:1.0.0.14`)
> imports `spring-boot-dependencies:3.5.10` and the version catalog defines the Spring Boot
> plugin at `3.5.10`. Use `alias(libs.plugins.org.springframework.boot)` to stay in sync.
> Do NOT hardcode a version — it would create a version split between plugin and BOM.

2. Remove the Gretty plugin and its configuration block:
```gradle
// REMOVE:
// id "org.gretty" apply true
// ... entire gretty { ... } block
```

3. Remove the `war` plugin:
```gradle
// REMOVE:
// apply plugin: 'war'
```

4. Add Spring Boot starters and remove now-transitive dependencies:
```gradle
dependencies {
    // ADD:
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    // Do NOT add spring-boot-starter-data-jpa yet — defer to Step 15.
    // JpaConfigBase still manually creates EntityManagerFactory, DataSource,
    // and TransactionManager. The starter would add auto-config classes
    // to the classpath that could trigger unexpectedly. Individual JPA
    // deps (hibernate-core, spring-data-jpa) are already BOM-managed.

    // KEEP (not replaced by auto-config until later steps):
    // - All existing JMS, Redis, CXF, Camel dependencies
    // - All existing Spring Security SAML dependencies
    // - All existing Prometheus dependencies (replaced in Step 17)

    // REMOVE (now provided by starters — verify each exists in build.gradle first):
    // - implementation "org.springframework:spring-webmvc"           (line ~151)
    // - implementation "org.springframework.security:spring-security-config"  (line ~147)
    // NOTE: spring-web and spring-security-web are NOT explicitly declared,
    //       only pulled transitively — do not try to remove them.
    // KEEP: spring-security-saml2-service-provider (not in any starter)

    // ADD for SpringDoc:
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.6'
    // TODO: Move version to BOM when platform includes springdoc-openapi-starter-webmvc-ui
    // REMOVE Swagger JAX-RS:
    // implementation("io.swagger:swagger-jaxrs") { exclude(module: "jsr311-api") }
}
```

5. Migrate Gradle configuration exclusion:
```gradle
// BEFORE (legacy config name):
configurations.runtime { exclude group: 'xalan', module: 'xalan' }

// AFTER (Spring Boot-compatible):
configurations.runtimeClasspath { exclude group: 'xalan', module: 'xalan' }
```

6. Add `bootRun` configuration to replace Gretty JVM args:
```gradle
bootRun {
    jvmArgs = [
        "-Dspring.profiles.active=dev,testability-api,caching-enabled,ia-stub,certificate-analytics-service-active",
        "-Ddev.config.file=${rootProject.projectDir}/devops/dev/config/application-dev.properties",
    ]
}
```

> ⚠️ **Embedded Tomcat version:** `spring-boot-starter-web` includes Tomcat 10.1.x.
> The current Gretty config uses Tomcat 10. Verify Jakarta Servlet API version compatibility
> (should be fine — both use `jakarta.servlet` namespace).

**Verification:** `./gradlew :webcert-web:compileJava` — compiles without errors.

---

### Sub-step 14.2 — Update root `build.gradle` for Spring Boot compatibility

**What:** Remove the Gretty plugin declaration from the root `build.gradle`, since it is no
longer used in any subproject.

**Changes to `build.gradle` (root):**
```gradle
// REMOVE:
// id "org.gretty" version "4.1.10" apply false
```

> ⚠️ **`javaVersion` property:** The `java.toolchain.languageVersion` uses `javaVersion`
> from `gradle.properties`. Spring Boot 3.4 requires Java 17+. Verify `javaVersion >= 17`.
> If not set in `gradle.properties`, check the BOM or add it explicitly.

**Verification:** `./gradlew tasks` — no Gretty tasks listed.

---

## Phase B: Create Spring Boot Application Class

### Sub-step 14.3 — Create `WebcertApplication.java`

**What:** Create the Spring Boot main class that replaces the `web.xml`
`ContextLoaderListener` + `DispatcherServlet` + servlet container setup.

**Why:** Spring Boot's `@SpringBootApplication` combines `@Configuration`,
`@EnableAutoConfiguration`, and `@ComponentScan`. It creates an
`AnnotationConfigServletWebServerApplicationContext` that serves as both root and servlet
context (no parent/child split).

**Key decisions:**
- `@SpringBootApplication` auto-scans the package it's in. Place it in
  `se.inera.intyg.webcert` to auto-scan all webcert packages.
- **But**: `AppConfig` already has explicit `@ComponentScans` for 18 packages including
  packages outside `se.inera.intyg.webcert` (e.g., `se.inera.intyg.common`). To avoid
  duplicate scanning, **disable the default scan** on `@SpringBootApplication` and
  rely on `AppConfig`'s existing scans.
- **Exclude auto-configs** that conflict with manual beans: JPA (manual `EntityManagerFactory`),
  JMS (manual `ConnectionFactory`), Redis (manual config), Mail (manual config).

**Create** `web/src/main/java/se/inera/intyg/webcert/web/WebcertApplication.java`:
```java
package se.inera.intyg.webcert.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyAutoConfiguration;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.context.annotation.Import;
import se.inera.intyg.webcert.web.config.AppConfig;

@SpringBootApplication(
    scanBasePackages = {},  // Disable default scan — AppConfig handles all scanning
    exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class,
        LiquibaseAutoConfiguration.class,
        TransactionAutoConfiguration.class,       // AppConfig has @EnableTransactionManagement
        ActiveMQAutoConfiguration.class,
        RedisAutoConfiguration.class,
        SessionAutoConfiguration.class,
        MailSenderAutoConfiguration.class,
        Saml2RelyingPartyAutoConfiguration.class,
    }
)
@Import(AppConfig.class)
public class WebcertApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebcertApplication.class, args);
    }
}
```

> ⚠️ **`scanBasePackages = {}`:** This disables the default `@ComponentScan` that
> `@SpringBootApplication` would apply. Instead, `@Import(AppConfig.class)` pulls in
> `AppConfig`, which has all 18 `@ComponentScans` and 14 `@Import`s. This ensures the
> bean landscape is identical to the current `web.xml`-based bootstrap.

> ⚠️ **`@DependsOn("dbUpdate")`:** `AppConfig` has `@DependsOn("dbUpdate")` which ensures
> Liquibase runs before the config is processed. `JpaConfigBase` creates the `dbUpdate`
> (SpringLiquibase) bean. Since `JpaConfigBase` is imported by `AppConfig`, the dependency
> chain is: `JpaConfigBase` → creates `dbUpdate` → `AppConfig` waits for it. This should
> still work under Spring Boot because `@Import` is processed before `@DependsOn` beans
> are resolved. Verify at startup.

> ⚠️ **`Saml2RelyingPartyAutoConfiguration` exclusion:** Spring Boot's SAML auto-config
> would try to auto-configure relying party registrations from properties. The current
> `WebSecurityConfig` manually builds `RelyingPartyRegistrationRepository` with X.509
> credentials and metadata locations. Excluding the auto-config prevents conflicts.

> ⚠️ **`SessionAutoConfiguration` exclusion:** `WebSecurityConfig` already has
> `@EnableRedisIndexedHttpSession`. Spring Boot's session auto-config would conflict.
> Exclude it to preserve the current behavior.

> ⚠️ **Camel auto-configuration:** If any `camel-spring-boot-*` JARs are on the classpath
> (check with `./gradlew dependencies --configuration runtimeClasspath | grep camel-spring-boot`),
> Spring Boot will trigger `CamelAutoConfiguration`, conflicting with the manual
> `SpringCamelContext` beans in `NotificationCamelConfig` and `CertificateCamelConfig`.
> If found, add to the exclusion list or add to `application.properties`:
> ```properties
> spring.autoconfigure.exclude=org.apache.camel.spring.boot.CamelAutoConfiguration
> ```

> ⚠️ **OpenSamlConfig initialization order:** `OpenSamlConfig` (`@Component` +
> `InitializingBean`) must run before `WebSecurityConfig` parses SAML metadata via
> `RelyingPartyRegistrations.fromMetadataLocation(...)`. There is no explicit dependency
> between them. Add `@DependsOn("openSamlConfig")` to `WebSecurityConfig`:
> ```java
> @Configuration
> @DependsOn("openSamlConfig")
> @EnableWebSecurity
> @EnableRedisIndexedHttpSession
> public class WebSecurityConfig { ... }
> ```

> ⚠️ **`@EnableWebMvc` in `WebMvcConfiguration`:** This annotation disables Spring Boot's
> `WebMvcAutoConfiguration`. If we keep it, Spring Boot's MVC defaults (content negotiation,
> default message converters, static resource serving) will not apply. See sub-step 14.4
> for resolution.

**Verification:** `./gradlew :webcert-web:compileJava` — compiles.

---

### Sub-step 14.4 — Merge `WebMvcConfiguration` into single-context model

**What:** Adapt `WebMvcConfiguration` for the Spring Boot single-context model. Remove
`@EnableWebMvc` so Spring Boot auto-config can contribute defaults.

**Why:** Currently, `WebMvcConfiguration` is the DispatcherServlet's child context config.
Under Spring Boot, all beans live in one context. The `@EnableWebMvc` annotation disables
Spring Boot's `WebMvcAutoConfiguration`, which provides useful defaults (e.g., default
content negotiation, `Formatter` registration, `HttpMessageConverter` defaults).

Since `WebMvcConfiguration` only customizes the `ObjectMapper` on the Jackson converter
(via `extendMessageConverters`), removing `@EnableWebMvc` is safe — the
`WebMvcConfigurer.extendMessageConverters` callback still works under auto-config.

**Changes to `WebMvcConfiguration.java`:**

1. Remove `@EnableWebMvc`:
```java
// Before:
@Configuration
@EnableWebMvc
@ComponentScan("se.inera.intyg.webcert.web.web.controller")
// ...

// After:
@Configuration
@ComponentScan("se.inera.intyg.webcert.web.web.controller")
// ...
```

2. The controller component scan (`se.inera.intyg.webcert.web.web.controller`) is already
   covered by `AppConfig`'s `@ComponentScan("se.inera.intyg.webcert.web")`. However, keeping
   it on `WebMvcConfiguration` is harmless (Spring deduplicates). Leave it for clarity.

3. The stub component scans (`notificationstub`, `fmb.stub`, `servicenow.stub.*`) with
   `@Filter(type = FilterType.ANNOTATION, classes = Configuration.class)` exclusions — these
   load REST stub controllers for dev/test. They are already guarded by excluding
   `@Configuration` classes. Verify stubs are also profile-gated (see 14.12).

4. **Remove `mvcHandlerMappingIntrospector` from `WebSecurityConfig`:** With `@EnableWebMvc`
   removed, Spring Boot's `WebMvcAutoConfiguration` auto-registers a properly initialized
   `mvcHandlerMappingIntrospector` bean. The manual `new HandlerMappingIntrospector()` in
   `WebSecurityConfig` (line ~289) would conflict. Remove it:
   ```java
   // REMOVE from WebSecurityConfig:
   // @Bean(name = "mvcHandlerMappingIntrospector")
   // public HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
   //     return new HandlerMappingIntrospector();
   // }
   ```

> ⚠️ **Double-scan concern:** `AppConfig` scans `se.inera.intyg.webcert.web` which includes
> `se.inera.intyg.webcert.web.web.controller`. `WebMvcConfiguration` also scans it.
> Spring context deduplicates by bean name, so this is harmless. No action needed.

> ⚠️ **Static resource serving:** The current app serves static resources through the
> DispatcherServlet at `/`. Spring Boot's auto-config adds default resource handlers for
> `/static/**`, `/public/**`, etc. If the existing app serves static resources from
> `webapp/`, verify they're still accessible after removing `@EnableWebMvc`. Spring Boot's
> embedded Tomcat serves `src/main/resources/static/` by default. If static content is in
> `src/main/webapp/`, it must be moved or configured explicitly.

**Verification:** `./gradlew :webcert-web:compileJava` — compiles. Boot the app and verify
JSON serialization (dates as ISO strings, nulls excluded) matches pre-migration.

---

### Sub-step 14.5 — Merge `CxfEndpointConfig` into single-context model

**What:** Ensure `CxfEndpointConfig` works in the root context (no longer a CXF servlet
child context). Import it from `AppConfig` so its beans are visible application-wide.

**Why:** Currently, `CxfEndpointConfig` runs in a child context created by the CXF servlet's
`contextConfigLocation` parameter. Under Spring Boot, the CXF servlet is registered via
`ServletRegistrationBean` and picks up the `Bus` bean from the main application context.
CXF endpoint beans must be in the main context.

**Changes to `AppConfig.java`:**

Add `CxfEndpointConfig.class` to the `@Import` list:
```java
@Import({
    LoggingConfig.class,
    JmsConfig.class,
    JpaConfigBase.class,
    CacheConfig.class,
    JobConfig.class,
    MailConfig.class,
    MailStubConfig.class,
    CxfWsClientConfig.class,
    FmbServicesConfig.class,
    ServiceNowIntegrationConfig.class,
    ServiceNowStubConfig.class,
    CertificateAnalyticsServiceIntegrationConfig.class,
    PrivatePractitionerRestClientConfig.class,
    AuthoritiesConfig.class,
    NotificationSenderConfig.class,
    CxfEndpointConfig.class,       // ← add
    Fk7263WcCxfConfig.class,       // ← add (if not already scanned)
    TsDiabetesWcCxfConfig.class,   // ← add (if not already scanned)
})
```

> ⚠️ **Stub beans in root context:** `CxfEndpointConfig` has:
> - `@Import(NotificationStubConfig.class)` — drags notification stub into root context
> - `@ComponentScan("se.inera.intyg.webcert.infra.srs.stub.config")`
> - `@ComponentScan("se.inera.intyg.webcert.infra.ia.stub.config")`
>
> These stub classes MUST be profile-gated (e.g., `@Profile("dev")` or
> `@Profile("testability-api")`). In the old model, the CXF child context isolated them.
> In the flat model, they're visible to the entire application. If stubs create beans that
> conflict with production beans (e.g., same interface, different implementation), the
> application will fail in non-dev profiles. **See sub-step 14.12 for resolution.**

> ⚠️ **`CxfEndpointConfig` component scan overlap:** The SRS/IA stub scans
> (`se.inera.intyg.webcert.infra.srs.stub.config`, `se.inera.intyg.webcert.infra.ia.stub.config`)
> may already be covered by `AppConfig`'s `@ComponentScan("se.inera.intyg.webcert.infra.srs")`
> and `@ComponentScan("se.inera.intyg.webcert.infra.ia.config")`. Verify no duplicate
> bean registration occurs.

> ⚠️ **`Fk7263WcCxfConfig` and `TsDiabetesWcCxfConfig`:** These configs define JAXWS client
> beans that replaced `wc-module-cxf-servlet.xml`. Verify they are not already picked up by
> `AppConfig`'s `@ComponentScan("se.inera.intyg.webcert.web")` (they are in
> `se.inera.intyg.webcert.web.config` package). If already scanned, the explicit `@Import`
> is redundant but harmless.

**Verification:** `./gradlew :webcert-web:compileJava` — compiles. Start app and verify all
6 SOAP endpoints respond at `/services/*`.

**Also in this sub-step — resolve bean name conflicts from context flattening:**

1. **`CookieSerializer` collision:** Both `AppConfig.cookieSerializer()` and
   `WebSecurityConfig.cookieSerializer()` define beans with the same name. They return
   different `IneraCookieSerializer` configurations (`useSameSiteNoneExclusion=false` vs
   `true`). The `WebSecurityConfig` version is correct (handles browser compatibility).
   **Remove** `cookieSerializer()` from `AppConfig.java`:
   ```java
   // REMOVE from AppConfig:
   // @Bean
   // public CookieSerializer cookieSerializer() { ... }
   // Also remove the @Value("${webcert.cookie.domain.name:}") field if only used here.
   ```
   Move the domain-name configuration into `WebSecurityConfig`'s `cookieSerializer()` method
   if needed.

2. **`ObjectMapper` collision:** Both `AppConfig.objectMapper()` (`@Primary`) and
   `NotificationCamelConfig.objectMapper()` define beans named `objectMapper`. `@Primary`
   controls injection preference but not registration — with
   `allow-bean-definition-overriding=false` (Spring Boot default) this causes
   `BeanDefinitionOverrideException`. **Rename** in `NotificationCamelConfig`:
   ```java
   // In NotificationCamelConfig:
   @Bean("notificationObjectMapper")
   public ObjectMapper notificationObjectMapper() {
       return new CustomObjectMapper();
   }

   @Bean
   public JacksonDataFormat notificationMessageDataFormat(
           @Qualifier("notificationObjectMapper") ObjectMapper objectMapper) {
       return new JacksonDataFormat(objectMapper, NotificationMessage.class);
   }
   ```

---

## Phase C: Servlet and Filter Registration

### Sub-step 14.6 — Create `WebServletConfig.java`

**What:** Register the CXF servlet and Prometheus MetricsServlet as Spring Boot
`ServletRegistrationBean` beans.

**Why:** Without `web.xml`, servlets must be registered programmatically. Spring Boot auto-
registers the `DispatcherServlet` at `/` — no manual registration needed for it.

**Create** `web/src/main/java/se/inera/intyg/webcert/web/config/WebServletConfig.java`:
```java
package se.inera.intyg.webcert.web.config;

import io.prometheus.client.servlet.jakarta.exporter.MetricsServlet;
import org.apache.cxf.Bus;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
public class WebServletConfig {

    @Bean
    public ServletRegistrationBean<CXFServlet> cxfServletRegistration(Bus bus) {
        CXFServlet cxfServlet = new CXFServlet();
        cxfServlet.setBus(bus);  // Explicitly inject the SpringBus from AppConfig
        ServletRegistrationBean<CXFServlet> registration =
            new ServletRegistrationBean<>(cxfServlet, "/services/*");
        registration.setName("services");
        registration.setLoadOnStartup(1);
        return registration;
    }

    // Inject the existing MetricsServlet bean from LoggingConfig rather than
    // creating a new instance. This also prevents Spring Boot's auto-registration
    // of the LoggingConfig bean at a default path.
    @Bean
    public ServletRegistrationBean<MetricsServlet> metricsServletRegistration(
            MetricsServlet metricsServlet) {
        ServletRegistrationBean<MetricsServlet> registration =
            new ServletRegistrationBean<>(metricsServlet, "/metrics");
        registration.setName("metrics");
        return registration;
    }

    // Required for Spring Security session concurrency control.
    // Previously registered via web.xml <listener>.
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    // Dual-port Tomcat connector: main port (server.port) + internal API port.
    // Required because InternalApiFilter gates /internalapi/* to the internal port.
    // Without this, all internal API requests on the main port return 403.
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory>
            internalPortCustomizer(
                @Value("${internal.api.port:8120}") int internalPort) {
        return factory -> {
            var connector = new org.apache.catalina.connector.Connector(
                TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
            connector.setPort(internalPort);
            factory.addAdditionalTomcatConnectors(connector);
        };
    }
}
```

> ⚠️ **CXF Bus injection:** `CXFServlet.setBus(bus)` ensures the servlet uses the
> `SpringBus` bean from `AppConfig` (named `Bus.DEFAULT_BUS_ID` = `"cxf"`). Without this,
> `CXFServlet.loadBus()` may create a NEW Bus via `BusFactory.newInstance().createBus()`,
> causing all 6 SOAP endpoints to be invisible. The explicit `setBus()` call is critical.

> ⚠️ **CXF endpoint registration:** The `EndpointImpl` beans in `CxfEndpointConfig` call
> `endpointImpl.publish("/path")` which registers endpoints on the default Bus. Since the
> Bus is now in the main context and the CXF servlet is mapped to `/services/*`, endpoints
> will be accessible at `/services/<path>` — same as before.

> ⚠️ **No `contextConfigLocation` on CXF servlet:** The old `web.xml` set
> `contextConfigLocation=CxfEndpointConfig` as the CXF servlet's init-param. With
> `ServletRegistrationBean`, no child context is created. All CXF beans live in the main
> context (moved there in 14.5).

**Verification:** Start app. `curl http://localhost:8080/services/` should list SOAP
endpoints. `curl http://localhost:8080/metrics` should return Prometheus metrics.

---

### Sub-step 14.7 — Create `WebFilterConfig.java`

**What:** Register all 13 filters from web.xml as `FilterRegistrationBean` beans, preserving
the exact filter order, URL patterns, dispatcher types, and init-params.

**Why:** Spring Boot auto-discovers `@Component` filters and registers them with `/*`
pattern and default order. This would break the precise filter chain. Using
`FilterRegistrationBean` gives full control. Filters that are `@Component` beans must
have their auto-registration disabled.

**Strategy for `@Component` filters:**
Some filters are `@Component` beans (auto-discovered by Spring): `MdcServletFilter`,
`MdcUserServletFilter`, `DefaultCharacterEncodingFilter`, `UnitSelectedAssuranceFilter`,
`InternalApiFilter`, `LaunchIdValidationFilter`, `AllowCorsFilter`. Spring Boot would
auto-register these with `/*` pattern. To prevent double registration, create a
`FilterRegistrationBean` for each with `setEnabled(false)` (to disable auto-registration),
OR add a `FilterRegistrationBean` per filter that REPLACES the auto-registration.

**Recommended approach:** For each `@Component` filter, create a `FilterRegistrationBean`
that references the Spring-managed bean instance and sets the correct URL patterns. Spring
Boot's `ServletContextInitializerBeans` deduplicates by checking if a
`FilterRegistrationBean` already exists for a given filter bean — if so, it skips
auto-registration. No need for `setEnabled(false)`.

**Create** `web/src/main/java/se/inera/intyg/webcert/web/config/WebFilterConfig.java`:
```java
package se.inera.intyg.webcert.web.config;

import io.prometheus.client.servlet.jakarta.exporter.MetricsServlet;
import jakarta.servlet.DispatcherType;
import java.util.EnumSet;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.inera.intyg.webcert.infra.security.filter.InternalApiFilter;
import se.inera.intyg.webcert.infra.security.filter.PrincipalUpdatedFilter;
import se.inera.intyg.webcert.infra.security.filter.RequestContextHolderUpdateFilter;
import se.inera.intyg.webcert.infra.security.filter.SecurityHeadersFilter;
import se.inera.intyg.webcert.infra.security.filter.SessionTimeoutFilter;
import se.inera.intyg.webcert.logging.MdcServletFilter;
import se.inera.intyg.webcert.web.logging.MdcUserServletFilter;
import se.inera.intyg.webcert.web.web.filter.AllowCorsFilter;
import se.inera.intyg.webcert.web.web.filter.DefaultCharacterEncodingFilter;
import se.inera.intyg.webcert.web.web.filter.LaunchIdValidationFilter;
import se.inera.intyg.webcert.web.web.filter.UnitSelectedAssuranceFilter;

@Configuration
public class WebFilterConfig {

    // Order constants — relative to Spring Security at -100 (SecurityProperties.DEFAULT_FILTER_ORDER).
    // Filters BEFORE security must have order < -100.
    // Filters AFTER security must have order > -100.
    // Spring Session's SessionRepositoryFilter runs at Integer.MIN_VALUE + 50 (auto-registered).
    private static final int ORDER_REQUEST_CONTEXT_HOLDER = -200;
    private static final int ORDER_MDC = -190;
    private static final int ORDER_CHARACTER_ENCODING = -180;
    private static final int ORDER_SESSION_TIMEOUT = -170;
    // Spring Security filter chain at -100 (automatic, do not register manually)
    private static final int ORDER_PRINCIPAL_UPDATED = 100;
    private static final int ORDER_UNIT_SELECTED = 110;
    private static final int ORDER_SECURITY_HEADERS = 120;
    private static final int ORDER_MDC_USER = 130;
    private static final int ORDER_INTERNAL_API = 140;
    private static final int ORDER_LAUNCH_ID = 150;
    private static final int ORDER_ALLOW_CORS = 160;

    // --- Filter 1: springSessionRepositoryFilter ---
    // Spring Session auto-registers this filter via @EnableRedisIndexedHttpSession.
    // It runs at Integer.MIN_VALUE + 50, well before our custom filters.
    // However, we must ensure it dispatches on both REQUEST and ERROR (matching web.xml).
    @Bean
    public FilterRegistrationBean<jakarta.servlet.Filter>
            sessionRepositoryFilterRegistration(
                    @Qualifier("springSessionRepositoryFilter") jakarta.servlet.Filter sessionFilter) {
        FilterRegistrationBean<jakarta.servlet.Filter> registration =
            new FilterRegistrationBean<>(sessionFilter);
        registration.setOrder(Integer.MIN_VALUE + 50);
        registration.addUrlPatterns("/*");
        registration.setDispatcherTypes(
            java.util.EnumSet.of(
                jakarta.servlet.DispatcherType.REQUEST,
                jakarta.servlet.DispatcherType.ERROR));
        return registration;
    }

    // --- Filters 2-5: Pre-security filters (order < -100) ---
    // NOTE: These 4 filter classes are NOT Spring beans (@Component/@Bean).
    // They must be instantiated directly via new XxxFilter().
    @Bean
    public FilterRegistrationBean<RequestContextHolderUpdateFilter>
            requestContextHolderUpdateFilterRegistration() {
        FilterRegistrationBean<RequestContextHolderUpdateFilter> registration =
            new FilterRegistrationBean<>(new RequestContextHolderUpdateFilter());
        registration.setOrder(ORDER_REQUEST_CONTEXT_HOLDER);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    public FilterRegistrationBean<MdcServletFilter> mdcServletFilterRegistration(
            MdcServletFilter filter) {
        FilterRegistrationBean<MdcServletFilter> registration =
            new FilterRegistrationBean<>(filter);
        registration.setOrder(ORDER_MDC);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    public FilterRegistrationBean<DefaultCharacterEncodingFilter>
            defaultCharacterEncodingFilterRegistration(
                    DefaultCharacterEncodingFilter filter) {
        FilterRegistrationBean<DefaultCharacterEncodingFilter> registration =
            new FilterRegistrationBean<>(filter);
        registration.setOrder(ORDER_CHARACTER_ENCODING);
        registration.addUrlPatterns("/v2/visa/intyg/*");
        return registration;
    }

    @Bean
    public FilterRegistrationBean<SessionTimeoutFilter> sessionTimeoutFilterRegistration() {
        FilterRegistrationBean<SessionTimeoutFilter> registration =
            new FilterRegistrationBean<>(new SessionTimeoutFilter());
        registration.setOrder(ORDER_SESSION_TIMEOUT);
        registration.addUrlPatterns("/*");
        // Preserve init-param from web.xml
        registration.addInitParameter(
            "skipRenewSessionUrls",
            "/moduleapi/stat,/api/session-auth-check/ping");
        return registration;
    }

    // --- Filter 6: springSecurityFilterChain ---
    // Spring Boot auto-registers the Spring Security filter chain at
    // SecurityProperties.DEFAULT_FILTER_ORDER (-100).
    // All pre-security filters have order < -100, all post-security filters > -100.
    // No manual registration needed.

    // --- Filters 7-13: Post-security filters (order > -100) ---
    // NOTE: PrincipalUpdatedFilter and SecurityHeadersFilter are NOT Spring beans.
    // They must be instantiated directly.
    @Bean
    public FilterRegistrationBean<PrincipalUpdatedFilter>
            principalUpdatedFilterRegistration() {
        FilterRegistrationBean<PrincipalUpdatedFilter> registration =
            new FilterRegistrationBean<>(new PrincipalUpdatedFilter());
        registration.setOrder(ORDER_PRINCIPAL_UPDATED);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    public FilterRegistrationBean<UnitSelectedAssuranceFilter>
            unitSelectedAssuranceFilterRegistration(
                    UnitSelectedAssuranceFilter filter) {
        FilterRegistrationBean<UnitSelectedAssuranceFilter> registration =
            new FilterRegistrationBean<>(filter);
        registration.setOrder(ORDER_UNIT_SELECTED);
        registration.addUrlPatterns("/api/*", "/moduleapi/*");
        // Preserve init-param from web.xml
        registration.addInitParameter(
            "ignoredUrls",
            "/api/config,/api/anvandare,/api/anvandare/andraenhet,"
            + "/api/jslog,/moduleapi/stat,/api/user");
        return registration;
    }

    @Bean
    public FilterRegistrationBean<SecurityHeadersFilter>
            securityHeadersFilterRegistration() {
        FilterRegistrationBean<SecurityHeadersFilter> registration =
            new FilterRegistrationBean<>(new SecurityHeadersFilter());
        registration.setOrder(ORDER_SECURITY_HEADERS);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    public FilterRegistrationBean<MdcUserServletFilter>
            mdcUserServletFilterRegistration(MdcUserServletFilter filter) {
        FilterRegistrationBean<MdcUserServletFilter> registration =
            new FilterRegistrationBean<>(filter);
        registration.setOrder(ORDER_MDC_USER);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    public FilterRegistrationBean<InternalApiFilter>
            internalApiFilterRegistration(InternalApiFilter filter) {
        FilterRegistrationBean<InternalApiFilter> registration =
            new FilterRegistrationBean<>(filter);
        registration.setOrder(ORDER_INTERNAL_API);
        registration.addUrlPatterns("/internalapi/*");
        return registration;
    }

    @Bean
    public FilterRegistrationBean<LaunchIdValidationFilter>
            launchIdValidationFilterRegistration(LaunchIdValidationFilter filter) {
        FilterRegistrationBean<LaunchIdValidationFilter> registration =
            new FilterRegistrationBean<>(filter);
        registration.setOrder(ORDER_LAUNCH_ID);
        registration.addUrlPatterns("/api/*", "/moduleapi/*");
        return registration;
    }

    @Bean
    public FilterRegistrationBean<AllowCorsFilter> allowCorsFilterRegistration(
            AllowCorsFilter filter) {
        FilterRegistrationBean<AllowCorsFilter> registration =
            new FilterRegistrationBean<>(filter);
        registration.setOrder(ORDER_ALLOW_CORS);
        registration.addUrlPatterns("/api/v1/session/invalidate");
        return registration;
    }
}
```

> ⚠️ **`springSessionRepositoryFilter` (Filter 1):** Spring Session's
> `@EnableRedisIndexedHttpSession` creates a `SessionRepositoryFilter` bean named
> `springSessionRepositoryFilter`. Spring Boot auto-registers it as a filter with
> `SessionRepositoryFilter.DEFAULT_ORDER` (`Integer.MIN_VALUE + 50`). This is already
> lower than our custom filter orders (1–13), so it runs first. The `DelegatingFilterProxy`
> wrapper from web.xml is no longer needed — Spring Boot registers the filter directly.
> The `REQUEST` + `ERROR` dispatcher types from web.xml should be the default. Verify.

> ⚠️ **`springSecurityFilterChain` (Filter 6):** Spring Boot registers the security filter
> chain at `SecurityProperties.DEFAULT_FILTER_ORDER` (-100). With our custom filter orders
> of 1–13, the security filter chain would actually run BEFORE all our custom filters
> (since -100 < 1). **This changes the filter execution order.**
>
> **Fix:** Adjust the order constants so that filters 1–5 (before security) have negative
> orders below -100, and filters 7–13 (after security) have positive orders above -100.
> For example:
> ```
> ORDER_SESSION_REPOSITORY  = not needed (Spring Session auto-handles)
> ORDER_REQUEST_CONTEXT     = -200
> ORDER_MDC                 = -190
> ORDER_CHARACTER_ENCODING  = -180
> ORDER_SESSION_TIMEOUT     = -170
> // Spring Security at -100 (default)
> ORDER_PRINCIPAL_UPDATED   = 100
> ORDER_UNIT_SELECTED       = 110
> ORDER_SECURITY_HEADERS    = 120
> ORDER_MDC_USER            = 130
> ORDER_INTERNAL_API        = 140
> ORDER_LAUNCH_ID           = 150
> ORDER_ALLOW_CORS          = 160
> ```
> This preserves the relative ordering: pre-security filters → security → post-security filters.

> ⚠️ **Init-param handling:** `SessionTimeoutFilter` reads `skipRenewSessionUrls` via
> `FilterConfig.getInitParameter()` in its `init()` method. With `FilterRegistrationBean`,
> init-params are passed via `addInitParameter()`. Verify `SessionTimeoutFilter.init()` is
> called by the container — Spring Boot calls `Filter.init(FilterConfig)` for
> `FilterRegistrationBean`-registered filters. Similarly for `UnitSelectedAssuranceFilter`
> and its `ignoredUrls` init-param.

> ⚠️ **`DelegatingFilterProxy` removal:** In web.xml, some filters used
> `DelegatingFilterProxy` to delegate to a Spring bean by name. Under Spring Boot, all
> filter beans are in the Spring context. `FilterRegistrationBean` directly wraps the
> bean instance — no `DelegatingFilterProxy` needed.

> ⚠️ **`LogbackConfiguratorContextListener` (Listener 1):** This listener reads the
> `logback.file` context-param from web.xml and configures Logback. Under Spring Boot,
> use `logging.config` property in `application.properties` instead. If the external logback
> path must be preserved, add:
> ```properties
> logging.config=file:${logback.file:classpath:logback-spring.xml}
> ```
> Alternatively, if `LogbackConfiguratorContextListener` is a `ServletContextListener`,
> register it via `ServletListenerRegistrationBean`.

> ⚠️ **`RequestContextListener` (Listener 3):** Spring Boot auto-registers a
> `RequestContextFilter` which serves the same purpose. No manual registration needed.

> ⚠️ **`HttpSessionEventPublisher` (Listener 4):** This is needed for Spring Security
> session management. Register via a `@Bean`:
> ```java
> @Bean
> public HttpSessionEventPublisher httpSessionEventPublisher() {
>     return new HttpSessionEventPublisher();
> }
> ```
> Add this to `WebFilterConfig` or `WebSecurityConfig`.

**Verification:** Start the app. Verify filter execution order by adding temporary debug
logging or checking the `FilterChainProxy` debug output. All endpoints must work:
- `/api/*` endpoints require authenticated session
- `/internalapi/*` requires internal port
- `/services/*` SOAP endpoints respond
- `/metrics` returns Prometheus data

---

## Phase D: Handle External Module Configs

### Sub-step 14.8 — Add `@ImportResource("classpath*:module-config.xml")` bridge

**What:** The external certificate modules (from `se.inera.intyg.common`) provide
`module-config.xml` files on the classpath. These 17 XML files define component scans and
bean wiring for each certificate type (e.g., lisjp, luse, luae_na, fk7263, etc.).

Under the old `web.xml` model, these were imported via `webcert-config.xml`:
```xml
<import resource="classpath*:module-config.xml"/>
```

After Step 12 removed `webcert-config.xml`, these imports need a new home.

**Why a bridge rather than full replacement:** Each certificate module JAR owns its
`module-config.xml`. Replacing all 17 with `@ComponentScan` entries requires knowing every
package, which is fragile and breaks when modules are added/removed. The `@ImportResource`
bridge lets module JARs continue to self-register. Full Java migration of external modules
is out of scope for webcert's migration.

**Changes to `AppConfig.java`:**

Add `@ImportResource`:
```java
@Configuration
@DependsOn("dbUpdate")
@RequiredArgsConstructor
@EnableTransactionManagement
@ImportResource("classpath*:module-config.xml")  // ← add
@PropertySources({ ... })
@ComponentScans({ ... })
@Import({ ... })
public class AppConfig implements TransactionManagementConfigurer {
```

> ⚠️ **`classpath*:` under Spring Boot:** `@ImportResource` supports wildcards
> (`classpath*:`) in Spring Boot. The embedded classloader scans all JARs on the classpath
> for matching resources. Verify that all 17 `module-config.xml` files are found:
> ```bash
> # At startup, enable debug logging:
> # logging.level.org.springframework.context.annotation=DEBUG
> # Check logs for "Loaded X bean definitions from..."
> ```

> ⚠️ **`wc-module-cxf-servlet.xml` NOT needed:** The two `wc-module-cxf-servlet.xml` files
> (from fk7263 and ts-diabetes) defined JAXWS clients that have already been replaced by
> `Fk7263WcCxfConfig.java` and `TsDiabetesWcCxfConfig.java` in a previous step. Do NOT add
> `@ImportResource("classpath*:wc-module-cxf-servlet.xml")`.

**Verification:** Start app. Certificate modules initialize correctly — verify by calling
a certificate-related endpoint (e.g., creating a draft certificate). Check logs for
module registration.

---

## Phase E: Swagger → SpringDoc Migration

### Sub-step 14.9 — Add SpringDoc dependency, remove Swagger JAX-RS

**What:** Replace `io.swagger:swagger-jaxrs` with `org.springdoc:springdoc-openapi-starter-webmvc-ui`.

**Why:** `swagger-jaxrs` is a Swagger 2 / JAX-RS integration. Since all controllers are now
Spring MVC `@RestController`s, SpringDoc (which integrates with Spring MVC natively) is the
correct replacement. SpringDoc generates OpenAPI 3 specs from Spring MVC annotations.

**Changes to `web/build.gradle`:**
```gradle
// REMOVE:
// implementation("io.swagger:swagger-jaxrs") { exclude(module: "jsr311-api") }

// ADD (already added in 14.1, confirm):
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.6'
```

**Add SpringDoc configuration to `application.properties`:**
```properties
# SpringDoc OpenAPI
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

**Verification:** `./gradlew :webcert-web:compileJava` — expect ~27 compilation errors
from `io.swagger.annotations.*` imports. These are fixed in sub-step 14.10.

---

### Sub-step 14.10 — Migrate Swagger annotations to OpenAPI 3

**What:** Replace `io.swagger.annotations.*` imports with `io.swagger.v3.oas.annotations.*`
(SpringDoc/OpenAPI 3) in all 27 affected files.

**Annotation mapping:**

| Swagger 2 (`io.swagger.annotations`) | OpenAPI 3 (`io.swagger.v3.oas.annotations`) |
|---|---|
| `@Api(description = "...")` | `@Tag(name = "...", description = "...")` |
| `@ApiOperation(value = "...", notes = "...")` | `@Operation(summary = "...", description = "...")` |
| `@ApiParam(value = "...")` | `@Parameter(description = "...")` |
| `@ApiResponse(code = 200, message = "...")` | `@ApiResponse(responseCode = "200", description = "...")` |
| `@ApiResponses({...})` | `@ApiResponses({...})` (same name, different package) |
| `@ApiModel(value = "...")` | `@Schema(name = "...", description = "...")` |
| `@ApiModelProperty(value = "...")` | `@Schema(description = "...")` |

**Approach:**
1. Do a global search-and-replace of import statements first.
2. Then fix each annotation usage based on the mapping above.
3. Files affected: ~27 files (controllers and DTOs).

**File list (representative):**
- Controllers: `UserAgreementResource`, `LogResource`, `IntygResource`, `ConfigApiController`,
  `SrsApiController`, `IntegreradEnhetResource`, and others
- DTOs: `ConfigResponse`, `FmbResponse`, and others

**Post-migration verification:**
```bash
grep -r "io.swagger.annotations" --include="*.java" web/src/main/java/
# Must return no results

grep -r "io.swagger.v3" --include="*.java" web/src/main/java/
# Should return results from the migrated files
```

> ⚠️ **Alternative: Remove annotations entirely.** If API documentation will be rebuilt
> from scratch using SpringDoc's automatic detection (which generates docs from
> `@RestController`, `@RequestMapping`, `@RequestParam`, etc.), the Swagger annotations
> can simply be deleted rather than migrated. SpringDoc generates reasonable docs without
> any annotations. Add OpenAPI annotations later as needed for enrichment.
> **Recommendation:** Delete the annotations rather than migrating them — less work, cleaner
> code, and SpringDoc auto-generates from Spring MVC annotations. Add `@Tag` on controllers
> only for grouping.

**Verification:** Start app. Navigate to `/swagger-ui.html` — API documentation renders.
Verify endpoints are listed and grouped correctly.

---

## Phase F: Fix Deferred Items

### Sub-step 14.11 — Remove redundant `@EnableScheduling` from `FmbServiceImpl`

**What:** Remove the `@EnableScheduling` annotation from `FmbServiceImpl.java`.

**Why:** `@EnableScheduling` is already present on `JobConfig.java` (which also has
`@EnableAsync` and `@EnableSchedulerLock`). Having it on `FmbServiceImpl` is redundant and
violates the principle of infrastructure annotations being on `@Configuration` classes.

**Changes to** `integration/fmb-integration/src/main/java/.../fmb/services/FmbServiceImpl.java`:
```java
// Before:
@Service
@Transactional
@EnableScheduling
@RequiredArgsConstructor
public class FmbServiceImpl implements FmbService {

// After:
@Service
@Transactional
@RequiredArgsConstructor
public class FmbServiceImpl implements FmbService {
```

**Verification:** `./gradlew :fmb-integration:test` — scheduled tasks still run.

---

### Sub-step 14.12 — Profile-gate stub component scans after context flattening

**What:** Ensure all stub beans from `CxfEndpointConfig` and `WebMvcConfiguration` are
properly profile-gated so they don't load in production.

**Why:** Under the old three-tier context model, stubs loaded in child contexts were
somewhat isolated. In the flat Spring Boot context, ALL beans are in one context. Stub
beans that provide alternative implementations of production interfaces could cause
`NoUniqueBeanDefinitionException` or silently replace production beans.

**Audit and fix:**

**Verified: All stub configs ARE already profile-gated.** No code changes needed for profile
annotations. However, scan overlaps should be cleaned up:

| Config/Scan Source | Classes Loaded | Profile-Gated? |
|---|---|---|
| `CxfEndpointConfig → @Import(NotificationStubConfig)` | `NotificationStubConfig` + 3 beans | ✅ `@Profile({"wc-notificationsender-stub", "dev"})` |
| `CxfEndpointConfig → @ComponentScan("...srs.stub.config")` | `SrsStubConfiguration` | ✅ `@Profile("dev")` |
| `CxfEndpointConfig → @ComponentScan("...ia.stub.config")` | `IAStubConfiguration` | ✅ `@Profile("dev")` |
| `WebMvcConfiguration → @ComponentScan("...notificationstub")` | `NotificationStubRestApi` | ✅ `@Profile("dev")` |
| `WebMvcConfiguration → @ComponentScan("...fmb.stub")` | `FmbStub` | ✅ `@Profile({"dev", "wc-fmb-stub"})` |
| `WebMvcConfiguration → @ComponentScan("...servicenow.stub.*")` | `ServiceNowStubRestApi` etc. | ✅ Profile-gated |

**Scan overlap cleanup:** `AppConfig` scans `se.inera.intyg.webcert.infra.srs` which includes
`se.inera.intyg.webcert.infra.srs.stub.config` as a subpackage. The SRS stub scan on
`CxfEndpointConfig` is redundant (Spring deduplicates). Consider removing it from
`CxfEndpointConfig` for clarity, but this is optional.

> ⚠️ **`NotificationStubConfig` and `@Profile("dev")`:** If `NotificationStubConfig` creates
> a `CertificateStatusUpdateForCareResponderInterface` bean (notification WS stub),
> and `NotificationWsClientConfig` creates the real bean with `@Profile("!dev")`, they
> should coexist correctly. Verify with both `dev` and non-dev profiles.

**Verification:** Start app with production-like profile (no `dev`). Verify no stub beans
are loaded. Start with `dev` profile. Verify stubs are active.

---

## Phase G: Remove web.xml and Gretty

### Sub-step 14.13 — Delete `web.xml`, `tomcat-gretty.xml`, remove Gretty

**Pre-checks (run ALL before deleting):**
```bash
# Application starts via Spring Boot
./gradlew bootRun
# Verify endpoints respond

# No remaining references to web.xml elements
grep -rn "ContextLoaderListener\|CXFServlet\|MetricsServlet" web/src/main/java/ --include="*.java"
# Should show only ServletRegistrationBean references, not web.xml-style usage

# No remaining Gretty references
grep -rn "gretty\|appRun" build.gradle web/build.gradle
# Must return nothing
```

**Files to delete:**
```
web/src/main/webapp/WEB-INF/web.xml
web/tomcat-gretty.xml
```

**Also remove the `webapp` directory** if `web.xml` was the only file:
```
web/src/main/webapp/WEB-INF/  (directory)
web/src/main/webapp/          (directory, if empty)
```

> ⚠️ **Static resources in `webapp/`:** If there are static resources (HTML, CSS, JS) in
> `web/src/main/webapp/`, they must be moved to `web/src/main/resources/static/` before
> deleting the `webapp` directory. Spring Boot serves static content from `classpath:/static/`.

> ⚠️ **`LogbackConfiguratorContextListener`:** This listener was defined in `web.xml`. If
> external logback configuration is still needed, configure it via `application.properties`:
> ```properties
> logging.config=file:${logback.file:classpath:logback-spring.xml}
> ```
> Or register the listener as a `@Bean`:
> ```java
> @Bean
> public ServletListenerRegistrationBean<LogbackConfiguratorContextListener>
>         logbackListener() {
>     return new ServletListenerRegistrationBean<>(
>         new LogbackConfiguratorContextListener());
> }
> ```
> Add this to `WebServletConfig.java` or `LoggingConfig.java`.

**Verification:**
```bash
./gradlew bootRun
```
Application starts without `web.xml`. Then verify all endpoints.

---

### Sub-step 14.14 — Final verification and cleanup

**Add `application.properties` settings for Spring Boot:**
```properties
# Server configuration (replaces Gretty config)
server.port=8020
server.servlet.context-path=/

# Allow bean definition overriding (may be needed during transition)
spring.main.allow-bean-definition-overriding=true

# Preserve existing behavior
spring.mvc.throw-exception-if-no-handler-found=true
```

**Verification checklist:**

```bash
./gradlew test
```
All tests pass (including `notification-sender:camelTest`).

```bash
./gradlew bootRun
```
Application starts via Spring Boot embedded Tomcat. Then verify:

- [ ] Application starts without errors (no `BeanDefinitionOverrideException`, no `NoSuchBeanDefinitionException`)
- [ ] `grep -rn "@ImportResource" web/src/main/java/` returns only the `classpath*:module-config.xml` bridge on `AppConfig`
- [ ] `find web/src/main/webapp` returns nothing (directory removed)
- [ ] No `web.xml` exists in the project
- [ ] No Gretty configuration exists
- [ ] `/swagger-ui.html` renders API documentation
- [ ] `/api-docs` returns OpenAPI 3 JSON spec
- [ ] `grep -r "io.swagger.annotations" --include="*.java" web/src/main/java/` returns no results
- [ ] `/services/` lists all 6 SOAP endpoints
- [ ] `/services/create-draft-certificate/v3.0?wsdl` returns WSDL
- [ ] `/metrics` returns Prometheus metrics
- [ ] REST API endpoints respond (test `/api/config`, `/internalapi/...`, `/moduleapi/...`)
- [ ] SAML authentication works (SITHS, eleg)
- [ ] OpenSAML security hardening is active (XXE protection, parser pool size 100)
- [ ] Filter chain executes in correct order (check with debug logging)
- [ ] Session management via Redis works
- [ ] Certificate module operations work (create draft, sign, etc.)
- [ ] Notification sending works (message to JMS queue → WS call)
- [ ] `FmbServiceImpl` scheduled tasks still execute
- [ ] No stub beans loaded in non-dev profile

---

## Risk Notes

**Context flattening and bean conflicts** — The biggest risk in Step 14. When three separate
contexts merge into one, bean name collisions, duplicate component scans, and stub/production
bean conflicts can occur. Mitigation:
- Start the application after each sub-step, not just at the end.
- Enable `spring.main.allow-bean-definition-overriding=true` initially to surface problems
  as warnings rather than startup failures.
- After stabilization, set it to `false` and fix all overrides.

**Filter order preservation** — The Spring Security filter chain runs at order -100 by
default. All pre-security filters must have order < -100, and post-security filters must
have order > -100. Getting this wrong could disable security entirely or break session
management. Verify with a request trace or debug logging.

**`@EnableWebMvc` removal** — Removing `@EnableWebMvc` lets Spring Boot's
`WebMvcAutoConfiguration` contribute defaults. This could change content negotiation,
message converter ordering, or error handling behavior. Verify:
- JSON responses have identical format (date serialization, null handling)
- Error responses (404, 500) have the expected format
- Content-Type headers are correct

**`@DependsOn("dbUpdate")` under Spring Boot** — `AppConfig` depends on the Liquibase
`dbUpdate` bean from `JpaConfigBase`. Spring Boot normally handles Liquibase via
auto-config, but we excluded `LiquibaseAutoConfiguration`. The manual `SpringLiquibase`
bean should still work, but verify Liquibase runs before entity manager creation.

**OpenSamlConfig preservation** — `OpenSamlConfig` is a `@Component` implementing
`InitializingBean`. It initializes OpenSAML with XXE protection and parser pool settings.
Spring Boot's SAML auto-config is excluded (`Saml2RelyingPartyAutoConfiguration`), so
there should be no conflict. Verify by checking logs for OpenSAML initialization messages.

**Embedded Tomcat vs external Tomcat** — The switch from Gretty (external Tomcat) to
Spring Boot (embedded Tomcat) may change:
- Default thread pool size (Tomcat default: 200)
- Connector configuration (previously in `tomcat-gretty.xml`: two connectors for main and
  internal ports)
- The `tomcat-gretty.xml` defines two HTTP connectors (main port and internal port). Spring
  Boot's embedded Tomcat has one connector by default. If the internal port is needed for
  `InternalApiFilter`, configure an additional connector via `WebServerFactoryCustomizer`:
  ```java
  @Bean
  public WebServerFactoryCustomizer<TomcatServletWebServerFactory>
          internalPortCustomizer(
              @Value("${internal.api.port:8120}") int internalPort) {
      return factory -> factory.addAdditionalTomcatConnectors(
          createConnector(internalPort));
  }
  ```
  This is critical — without the internal port connector, `InternalApiFilter` will reject
  all requests on the main port.

**External deployment (WAR) capability** — After this step, WAR deployment to external
Tomcat is no longer supported. The application runs as a standalone JAR. Coordinate with
the operations team to update deployment procedures.

**`spring-boot-starter-data-jpa` transitive version conflicts** — The starter may pull in
a different Hibernate version than what `persistence/build.gradle` explicitly declares.
Since JPA auto-config is excluded, the manually configured `EntityManagerFactory` should
still use the Hibernate version from the BOM. Verify with
`./gradlew dependencies --configuration runtimeClasspath | grep hibernate`.

**Swagger annotation removal vs migration** — If annotations are deleted (recommended),
API documentation coverage will decrease initially. SpringDoc still generates docs from
Spring MVC annotations, but descriptions, parameter docs, and response codes won't be as
rich. This is acceptable for an incremental migration — annotations can be added back
incrementally using OpenAPI 3 syntax.

**Test classpath changes** — `spring-boot-test` and `spring-boot-starter-test` add test
infrastructure. Some tests may pick up Spring Boot auto-config unintentionally (e.g., if
using `@SpringBootTest`). Existing tests using `@ExtendWith(SpringExtension.class)` with
explicit `@ContextConfiguration` should be unaffected. Verify by running the full test suite.
Add to verification: `grep -r "@SpringBootTest" --include="*.java"` returns nothing.

---

## Addendum — Review Findings (Post-Review Corrections)

This section documents findings from a 4-agent independent review of the original plan.
Critical and high-severity items have been **integrated into the sub-steps above**. Remaining
medium/low items are documented here for implementer awareness.

### Corrections Already Applied Above

| Finding | Severity | Where Fixed |
|---|---|---|
| Spring Boot version wrong (3.4.4 → BOM's 3.5.10) | 🔴 CRITICAL | Sub-step 14.1 |
| `io.spring.dependency-management` plugin conflicts with BOM | 🔴 CRITICAL | Sub-step 14.1 |
| Filter order code used 1–13 instead of relative to -100 | 🔴 CRITICAL | Sub-step 14.7 |
| 4 non-bean filters injected as Spring beans | 🔴 CRITICAL | Sub-step 14.7 |
| `CookieSerializer` bean name collision | 🔴 CRITICAL | Sub-step 14.5 |
| Dual-port Tomcat connector promoted to sub-step | 🔴 CRITICAL | Sub-step 14.6 |
| `spring-boot-starter-data-jpa` premature — defer to Step 15 | 🔴 HIGH | Sub-step 14.1 |
| `springSessionRepositoryFilter` ERROR dispatcher type | 🔴 HIGH | Sub-step 14.7 |
| `ObjectMapper` bean name collision | 🔴 HIGH | Sub-step 14.5 |
| CXF Bus discovery — use `setBus()` | 🔴 HIGH | Sub-step 14.6 |
| Duplicate MetricsServlet — inject existing bean | 🟡 MEDIUM | Sub-step 14.6 |
| `HandlerMappingIntrospector` conflict | 🟡 MEDIUM | Sub-step 14.4 |
| `TransactionAutoConfiguration` not excluded | 🟡 MEDIUM | Sub-step 14.3 |
| `HttpSessionEventPublisher` not in code | 🟡 MEDIUM | Sub-step 14.6 |
| OpenSamlConfig initialization order | 🟡 MEDIUM | Sub-step 14.3 |
| `bootRun` JVM args missing | 🟡 MEDIUM | Sub-step 14.1 |
| Sub-step 14.12 stubs verified profile-gated | 🟡 MEDIUM | Sub-step 14.12 |

### Remaining Items — Implementer Awareness

**`RemoteIpValve` equivalent (HIGH):** `tomcat-gretty.xml` configures a `RemoteIpValve`
with `remoteIpHeader="X-Forwarded-For"` and `protocolHeader="X-Forwarded-Proto"`. This is
essential for correct client IP logging and HTTPS detection behind a reverse proxy. SAML
assertions are scheme-sensitive and may fail without this. Add to `application.properties`:
```properties
server.forward-headers-strategy=native
```

**`LogbackConfiguratorContextListener` (MEDIUM):** Commit to using Spring Boot's native
`logging.config` property rather than the custom listener. The listener reads servlet
context init-params that are not available under embedded Tomcat without additional
`WebServerFactoryCustomizer` setup. Add to `application.properties`:
```properties
logging.config=${logback.file:classpath:logback-spring.xml}
```
The `LogbackConfiguratorContextListener` class is no longer needed and should NOT be
registered as a `ServletListenerRegistrationBean`.

**`Jenkins.properties` and Dockerfile (MEDIUM):** `Jenkins.properties` declares
`runtime.image=tomcat-base:10.1.52.1`. After Step 14 removes WAR packaging, CI/CD expects
a WAR in Tomcat but gets a Spring Boot JAR. Options:
- Update Dockerfile in Step 14 (minimal: `FROM eclipse-temurin:21-jre` + `COPY *.jar app.jar`)
- OR keep producing WAR alongside bootJar during transition (add `id 'war'` plugin and
  have `WebcertApplication` extend `SpringBootServletInitializer`)
This must be coordinated before merging Step 14 to avoid CI breakage.

**`common-config.xml` verification (MEDIUM):** The plan adds `classpath*:module-config.xml`
bridge but does not verify `classpath:common-config.xml` (from `se.inera.intyg.common:common-support`).
Verify what beans it defines and confirm they are covered by `AppConfig`'s
`@ComponentScan("se.inera.intyg.common")`. If `common-config.xml` imports sub-configs not
reachable by component scan, add it to the `@ImportResource` bridge.

**Camel auto-configuration (MEDIUM):** Check if any `camel-spring-boot-*` JARs are on the
classpath: `./gradlew dependencies --configuration runtimeClasspath | grep camel-spring-boot`.
If found, add `CamelAutoConfiguration.class` to the exclusion list in `WebcertApplication`.

**`spring.main.allow-bean-definition-overriding` (MEDIUM):** With the CookieSerializer,
ObjectMapper, and HandlerMappingIntrospector conflicts resolved (see above), this flag
should NOT be needed. Set to `false` (Spring Boot default). If startup fails with
`BeanDefinitionOverrideException`, investigate and fix the specific conflict rather than
enabling the flag. Known remaining potential conflict: `PropertySourcesPlaceholderConfigurer`
(both `AppConfig` and Spring Boot create one) — this is typically harmless as Spring Boot
backs off, but verify.

**`@PropertySource` duplication (LOW):** `AppConfig` loads `application.properties` via
`@PropertySource`. Spring Boot also loads it automatically. The `@PropertySource` is
redundant for `application.properties` but harmless. Keep the other property sources
(`version.properties`, `webcert-notification-route-params.properties`, `file:${dev.config.file}`).
Consider removing `@PropertySource("classpath:application.properties")` in a follow-up.

**SpringDoc stub API exposure (LOW):** SpringDoc auto-discovers ALL controllers including
dev stubs. Add group configuration to separate production APIs:
```properties
springdoc.group-configs[0].group=production
springdoc.group-configs[0].paths-to-match=/api/**,/moduleapi/**,/internalapi/**
springdoc.group-configs[0].paths-to-exclude=/stubs/**
```

**JSON MIME mapping (LOW):** web.xml defined `json → application/json;charset=utf-8`.
Embedded Tomcat's default for `.json` is `application/json` without charset. The webapp
directory has no static JSON files, so this is a non-issue in practice.

**`RequestContextFilter` vs `RequestContextHolderUpdateFilter` (LOW):** Spring Boot
auto-registers `RequestContextFilter`. The existing `RequestContextHolderUpdateFilter`
exists specifically to fix a Spring Session issue (updating `RequestContextHolder` with
the session-wrapped request). Both can coexist — `RequestContextFilter` runs first (high
precedence), then `RequestContextHolderUpdateFilter` updates the holder after session
wrapping. If tests show unexpected behavior, disable the auto-registered filter via
`FilterRegistrationBean` with `setEnabled(false)`.

**`runtime` configuration exclusion (LOW):** `web/build.gradle` has
`configurations.runtime { exclude group: 'xalan' }`. The `runtime` config name is legacy.
Migrate to `configurations.runtimeClasspath` in sub-step 14.1.
