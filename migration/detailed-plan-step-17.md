# Step 17 — Replace Prometheus with Spring Boot Actuator + Micrometer

## Problem Statement

The application currently uses the legacy Prometheus Java client (`simpleclient` family) directly: it
registers a `MetricsServlet` at `/metrics`, exposes JVM metrics via `DefaultExports`, and implements
`HealthMonitor` as a `Collector` that pushes gauges to the default Prometheus `CollectorRegistry`.
Step 17 removes this stack and replaces it with Spring Boot Actuator + Micrometer (backed by
`micrometer-registry-prometheus`), which exposes metrics at `/actuator/prometheus` and uses Micrometer's
pull-based `Gauge` API.

---

## Pre-flight: What's Already Done

| Task from step 17 | Status |
|-------------------|--------|
| Remove `simpleclient_servlet_jakarta`, `simpleclient_hotspot` | ❌ Not done |
| Add `spring-boot-starter-actuator` + `micrometer-registry-prometheus` | ❌ Not done |
| Configure `management.endpoints.web.exposure.include` | ❌ Not done |
| Replace `@PrometheusTimeMethod` usages | ✅ **Already done** — annotation infrastructure exists in `infra` but `@PrometheusTimeMethod` is not applied to any production method; can be deleted as dead code |
| Convert `HealthMonitor` to Micrometer | ❌ Not done |

---

## Current State (Baseline)

### Files to Modify or Delete
| File | Location | Action |
|------|----------|--------|
| `web/build.gradle` | `web/` | Remove `simpleclient_hotspot`, `simpleclient_servlet_jakarta`; add Actuator + Micrometer |
| `infra/build.gradle` | `infra/` | Remove all 4 `simpleclient*` deps |
| `MethodTimer.java` | `infra/src/main/java/.../monitoring/annotation/` | **Delete** — dead code, uses `io.prometheus.client.Summary` |
| `PrometheusTimeMethod.java` | `infra/src/main/java/.../monitoring/annotation/` | **Delete** — annotation with zero usages |
| `EnablePrometheusTiming.java` | `infra/src/main/java/.../monitoring/annotation/` | **Delete** — meta-annotation that imports `MethodTimer` |
| `MonitoringConfiguration.java` | `infra/src/test/java/.../monitoring/` | Remove prometheus imports; remove `@EnablePrometheusTiming`, `DefaultExports`, `MetricsServlet` |
| `LoggingConfig.java` | `web/src/main/java/.../config/` | Remove `@EnablePrometheusTiming`, `metricsServlet()`, constructor with `DefaultExports.initialize()` |
| `WebServletConfig.java` | `web/src/main/java/.../config/` | Remove `MetricsServlet` field and `metricsServletRegistrationBean()` bean |
| `HealthMonitor.java` | `web/src/main/java/.../service/monitoring/` | Rewrite: replace `extends Collector` + static Prometheus `Gauge` fields with Micrometer `MeterRegistry` |
| `WebSecurityConfig.java` | `web/src/main/java/.../config/` | Update `/metrics` permit-all rule → `/actuator/**` |
| `application.properties` | `web/src/main/resources/` | Add `management.endpoints.web.exposure.include=health,info,prometheus` |

### Current Prometheus Dependencies
```gradle
# web/build.gradle
implementation "io.prometheus:simpleclient_hotspot"
implementation "io.prometheus:simpleclient_servlet_jakarta"

# infra/build.gradle
implementation "io.prometheus:simpleclient"
implementation "io.prometheus:simpleclient_common"
implementation "io.prometheus:simpleclient_hotspot"
implementation "io.prometheus:simpleclient_servlet_jakarta"
```

### Current `HealthMonitor.java` Structure
- `extends Collector` — registers itself as a Prometheus `CollectorRegistry` collector
- Five static `io.prometheus.client.Gauge` fields: `UPTIME`, `DB_ACCESSIBLE`, `JMS_ACCESSIBLE`,
  `IT_ACCESSIBLE`, `SIGNATURE_QUEUE_DEPTH`
- `@PostConstruct init()` — calls `this.register()` to register with Prometheus
- `collect()` — updates all gauges and returns empty `List<MetricFamilySamples>`
- Private `checkJmsConnection()`, `checkDbConnection()`, `checkSignatureQueue()`, `pingIntygstjanst()` helpers

### Current Metrics Endpoint
- `MetricsServlet` registered at `/metrics` (in `WebServletConfig.java`), secured as `permitAll()` in
  `WebSecurityConfig.java`
- `DefaultExports.initialize()` called in `LoggingConfig` constructor — registers JVM metrics into the
  legacy `CollectorRegistry`

---

## Approach

Replace the manual Prometheus stack in **8 ordered sub-steps**. The application must compile and tests
must pass after each sub-step.

---

## Critical Complications (Must Address)

### 1. Two Separate Prometheus Registries — Old Metrics Disappear at Endpoint Change
The `simpleclient` family uses `CollectorRegistry.defaultRegistry`. Micrometer's
`PrometheusMeterRegistry` is a completely separate registry. After migration:
- Old `/metrics` endpoint (served by `MetricsServlet`) is removed.
- New `/actuator/prometheus` endpoint only serves Micrometer-registered metrics.
- **If `HealthMonitor` gauges are not migrated to Micrometer, they will disappear from the metrics
  endpoint.** This is why sub-step 17.6 is mandatory, not optional.

### 2. `HealthMonitor` — Gauge Naming Convention Must Be Preserved
Existing dashboards and alert rules likely target the current metric names:
- `health_uptime_value`
- `health_db_accessible_normal`
- `health_jms_accessible_normal`
- `health_intygstjanst_accessible_normal`
- `health_signature_queue_depth_value`

Micrometer's `PrometheusNamingConvention` converts `.` to `_` and lowercases names, but if the
Micrometer metric name already uses `_` (no dots), it passes through unchanged. Register with the
exact legacy names using the `name()` builder method to preserve metric names.

### 3. Micrometer `Gauge` is Pull-based — Remove `collect()` Update Pattern
The current `HealthMonitor.collect()` method imperatively pushes values into Prometheus `Gauge` objects.
Micrometer's `Gauge` is pull-based: you register a **supplier lambda** that is called on each scrape.
The `collect()` method and `Collector` superclass must be removed entirely; the health-check logic
moves into the supplier lambdas registered in `@PostConstruct`.

### 4. JVM Metrics — No `DefaultExports.initialize()` Needed
`DefaultExports.initialize()` (from `simpleclient_hotspot`) registers JVM metrics into the legacy
`CollectorRegistry`. With `spring-boot-starter-actuator` + `micrometer-registry-prometheus`, Micrometer
auto-configures JVM metrics (via `JvmMetricsAutoConfiguration`) automatically. No explicit
initialization is needed.

### 5. `@EnablePrometheusTiming` Still Referenced in `LoggingConfig.java`
`LoggingConfig.java` carries `@EnablePrometheusTiming`, which imports `MethodTimer`. After deleting
`MethodTimer.java`, this import must also be removed from `LoggingConfig.java`. Failure to do so
causes a `ClassNotFoundException` at runtime and compile error (since `EnablePrometheusTiming`
imports `MethodTimer` via `@Import`).

### 6. Security — Endpoint URL Changes from `/metrics` to `/actuator/prometheus`
`WebSecurityConfig.java` currently permits unauthenticated access to `/metrics`. After migration:
- Remove the `antMatcher("/metrics")` permit-all rule.
- Add `antMatcher("/actuator/**")` permit-all rule (or restrict to a network-level firewall; allowing
  at the Spring Security level is simplest for ops tooling).

---

## Sub-Steps

### Sub-step 17.1 — Update `web/build.gradle`

Remove the two Prometheus servlet/hotspot deps, and add Actuator + Micrometer Prometheus registry:

```gradle
// REMOVE:
implementation "io.prometheus:simpleclient_hotspot"
implementation "io.prometheus:simpleclient_servlet_jakarta"

// ADD:
implementation "org.springframework.boot:spring-boot-starter-actuator"
implementation "io.micrometer:micrometer-registry-prometheus"
```

Note: `micrometer-core` is brought in transitively by `spring-boot-starter-actuator`. The
`micrometer-registry-prometheus` artifact must be explicit — it bridges Micrometer to the
`io.prometheus:prometheus-metrics-*` (Prometheus Java client v1) or `simpleclient` (v0) backend
depending on the Micrometer version used. Spring Boot's BOM manages the version.

**Verify:** `./gradlew :webcert-web:compileJava` — compile only; full runtime test in sub-step 17.8.

---

### Sub-step 17.2 — Update `infra/build.gradle`

Remove all four Prometheus client deps from the `infra` module. The `aspectjweaver` dep stays:

```gradle
// REMOVE:
implementation "io.prometheus:simpleclient"
implementation "io.prometheus:simpleclient_common"
implementation "io.prometheus:simpleclient_hotspot"
implementation "io.prometheus:simpleclient_servlet_jakarta"
```

**Verify:** `./gradlew :infra:compileJava` — should fail until sub-step 17.3 (the source files still
reference `io.prometheus`). That's expected — do 17.2 and 17.3 together.

---

### Sub-step 17.3 — Delete Unused `@PrometheusTimeMethod` Infrastructure

Delete the three dead annotation classes from `infra/src/main/java/.../monitoring/annotation/`:

- **Delete** `MethodTimer.java`
- **Delete** `PrometheusTimeMethod.java`
- **Delete** `EnablePrometheusTiming.java`

Also clean up `infra/src/test/java/.../monitoring/MonitoringConfiguration.java` — remove all
prometheus-specific parts (class stays because `LogbackTest` and `MarkerFilterTest` use its other
beans):

```java
// REMOVE from MonitoringConfiguration.java:
import io.prometheus.client.hotspot.DefaultExports;
import io.prometheus.client.servlet.jakarta.exporter.MetricsServlet;
import se.inera.intyg.webcert.infra.monitoring.annotation.EnablePrometheusTiming;
@EnablePrometheusTiming                    // remove annotation
public MonitoringConfiguration() {        // remove constructor entirely
    DefaultExports.initialize();
}
@Bean
public MetricsServlet metricsServlet() {  // remove bean
    return new MetricsServlet();
}
```

The result is a `@Configuration @EnableAspectJAutoProxy` class with only `logMDCServletFilter()` and
`logMDCHelper()` beans.

**Verify:** `./gradlew :infra:compileJava` — compiles cleanly.

---

### Sub-step 17.4 — Refactor `LoggingConfig.java`

Remove the Prometheus bootstrapping from `LoggingConfig.java`:

```java
// REMOVE these imports:
import io.prometheus.client.hotspot.DefaultExports;
import io.prometheus.client.servlet.jakarta.exporter.MetricsServlet;
import se.inera.intyg.webcert.infra.monitoring.annotation.EnablePrometheusTiming;

// REMOVE this annotation from class:
@EnablePrometheusTiming

// REMOVE the entire constructor:
public LoggingConfig() {
    DefaultExports.initialize();
}

// REMOVE this bean:
@Bean
public MetricsServlet metricsServlet() {
    return new MetricsServlet();
}
```

Keep: `@Configuration`, `@EnableAspectJAutoProxy`, `@ComponentScan("se.inera.intyg.webcert.logging")`,
`logMDCHelper()` bean, `userAgentParser()` bean.

**Verify:** `./gradlew :webcert-web:compileJava` — should compile. `WebServletConfig` will fail until
17.5.

---

### Sub-step 17.5 — Refactor `WebServletConfig.java`

Remove the `MetricsServlet` registration. After this, `WebServletConfig` only contains the CXF servlet:

```java
// REMOVE:
import io.prometheus.client.servlet.jakarta.exporter.MetricsServlet;
import lombok.RequiredArgsConstructor;
@RequiredArgsConstructor                              // remove (no fields left)
private final MetricsServlet metricsServlet;          // remove field

@Bean
public ServletRegistrationBean<MetricsServlet> metricsServletRegistrationBean() {  // remove entire method
    var registration = new ServletRegistrationBean<>(metricsServlet, "/metrics");
    registration.setName("metrics");
    return registration;
}
```

Keep: `@Configuration`, `cxfServlet()` bean.

**Verify:** `./gradlew :webcert-web:compileJava` — compiles cleanly.

---

### Sub-step 17.6 — Rewrite `HealthMonitor.java` with Micrometer

This is the most substantial change. Replace the Prometheus `Collector`-based approach with Micrometer
`MeterRegistry` pull-based gauges.

**Remove:**
- `extends Collector`
- All `io.prometheus.client.*` imports (`Collector`, `Gauge`)
- Static `Gauge` fields (`UPTIME`, `DB_ACCESSIBLE`, `JMS_ACCESSIBLE`, `IT_ACCESSIBLE`,
  `SIGNATURE_QUEUE_DEPTH`)
- `@PostConstruct init()` with `this.register()`
- `collect()` method and its `@Override`

**Add:**
- Field: `private final MeterRegistry meterRegistry;` (constructor-inject via `@Autowired` or
  `@RequiredArgsConstructor`)
- `@PostConstruct init()` that registers Micrometer gauges with supplier lambdas:

```java
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

@Autowired
private MeterRegistry meterRegistry;

@PostConstruct
public void init() {
    Gauge.builder("health_uptime_value", this, m ->
            (double) ((System.currentTimeMillis() - START_TIME) / MILLIS_PER_SECOND))
        .description("Current uptime in seconds")
        .register(meterRegistry);

    Gauge.builder("health_db_accessible_normal", this, m -> m.checkDbConnection() ? 0.0 : 1.0)
        .description("0 == OK 1 == NOT OK")
        .register(meterRegistry);

    Gauge.builder("health_jms_accessible_normal", this, m -> m.checkJmsConnection() ? 0.0 : 1.0)
        .description("0 == OK 1 == NOT OK")
        .register(meterRegistry);

    Gauge.builder("health_intygstjanst_accessible_normal", this, m -> m.pingIntygstjanst() ? 0.0 : 1.0)
        .description("0 == OK 1 == NOT OK")
        .register(meterRegistry);

    Gauge.builder("health_signature_queue_depth_value", this, m -> (double) m.checkSignatureQueue())
        .description("Number of waiting messages")
        .register(meterRegistry);
}
```

**Keep unchanged:**
- All existing `@Autowired` / `@Value` / `@PersistenceContext` fields (queue templates, connection
  factory, redis template, IT metrics URL)
- All private helper methods: `checkJmsConnection()`, `checkDbConnection()`, `checkSignatureQueue()`,
  `pingIntygstjanst()`, `invoke(Tester)`, `Tester` interface
- `START_TIME` and `MILLIS_PER_SECOND` constants (still used by the uptime gauge lambda)

**Note:** The private helper methods currently called from `collect()` must now be accessible from the
gauge supplier lambdas. They are already `private` on `this`, and the lambdas reference `this` (the
`HealthMonitor` instance passed to `Gauge.builder`), so this works correctly.

**Verify:** `./gradlew :webcert-web:compileJava` — compiles cleanly.

---

### Sub-step 17.7 — Update `application.properties` and `WebSecurityConfig.java`

#### `web/src/main/resources/application.properties`

Add Actuator endpoint exposure configuration (no existing `management.*` properties to remove):

```properties
# Actuator
management.endpoints.web.exposure.include=health,info,prometheus
management.endpoint.health.show-details=never
```

#### `WebSecurityConfig.java`

Change the `/metrics` permit-all security rule to `/actuator/**`:

```java
// CHANGE:
.requestMatchers(antMatcher("/metrics"))
.permitAll()

// TO:
.requestMatchers(antMatcher("/actuator/**"))
.permitAll()
```

**Verify:** `./gradlew :webcert-web:compileJava` — compiles cleanly.

---

### Sub-step 17.8 — Full Integration Verify

```bash
./gradlew test
```

**Checklist:**
- `./gradlew test` — all tests pass, no `NoClassDefFoundError` for `io.prometheus.client.*`
- `./gradlew :infra:test` — `MonitoringConfiguration`-backed tests (`LogbackTest`, `MarkerFilterTest`) pass
- Application starts cleanly (`./gradlew bootRun`)
- `/actuator/health` → returns `{"status":"UP"}` (or similar)
- `/actuator/prometheus` → serves Prometheus-format text including `health_uptime_value`,
  `health_db_accessible_normal`, JVM metrics (`jvm_*`, `process_*`)
- `/metrics` → **404** (old endpoint removed)
- `./gradlew :webcert-web:test` — Spring Boot context tests that inject `MeterRegistry` pass

---

## Dependency Change Summary

| Module | Change |
|--------|--------|
| `web/build.gradle` | Remove `simpleclient_hotspot`, `simpleclient_servlet_jakarta`; add `spring-boot-starter-actuator`, `micrometer-registry-prometheus` |
| `infra/build.gradle` | Remove `simpleclient`, `simpleclient_common`, `simpleclient_hotspot`, `simpleclient_servlet_jakarta` |

## Files Changed Summary

| File | Change |
|------|--------|
| `web/build.gradle` | Dependency swap |
| `infra/build.gradle` | Remove 4 prometheus deps |
| `MethodTimer.java` | **Deleted** |
| `PrometheusTimeMethod.java` | **Deleted** |
| `EnablePrometheusTiming.java` | **Deleted** |
| `MonitoringConfiguration.java` (infra test) | Remove prometheus parts |
| `LoggingConfig.java` | Remove `@EnablePrometheusTiming`, constructor, `metricsServlet()` bean |
| `WebServletConfig.java` | Remove `MetricsServlet` field and registration bean |
| `HealthMonitor.java` | Replace `extends Collector` + static Gauges with Micrometer `MeterRegistry` |
| `WebSecurityConfig.java` | `/metrics` → `/actuator/**` in permit-all rule |
| `application.properties` | Add `management.endpoints.web.exposure.include` |
