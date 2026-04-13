# Step 16 — Replace JMS Manual Config with Spring Boot Auto-Configuration

## Problem Statement

The `web` module manually creates a `ConnectionFactory` (a `CachingConnectionFactory` wrapping
`ActiveMQConnectionFactory`), a `JmsTransactionManager`, and six named `JmsTemplate` beans in
`JmsConfig.java`, using legacy `activemq.broker.*` property keys. Step 16 replaces the manual
`ConnectionFactory` with Spring Boot's ActiveMQ auto-configuration, migrates property keys to
`spring.activemq.*` conventions, and keeps all custom `JmsTemplate` beans and the custom
`JmsListenerContainerFactory` because their per-queue semantics cannot be auto-configured.

---

## Current State (Baseline)

### Files to Modify
| File | Location | Action |
|------|----------|--------|
| `JmsConfig.java` | `web/src/main/java/.../web/config/` | Remove manual `ConnectionFactory` bean; keep all other beans |
| `application.properties` | `web/src/main/resources/` | Rename `activemq.broker.*` → `spring.activemq.*` |
| `application-dev.properties` | `devops/dev/config/` | Rename `activemq.broker.*` → `spring.activemq.*` |
| `web/build.gradle` | `web/` | Replace `activemq-spring` + standalone `spring-jms` with `spring-boot-starter-activemq` |

### Files That Stay Unchanged
| File | Reason |
|------|--------|
| `NotificationJmsConfig.java` | `notification-sender` module; already wires off the shared `ConnectionFactory` |
| `CertificateAnalyticsServiceIntegrationConfig.java` | `integration-certificate-analytics-service` module; same pattern |
| `notification-sender/build.gradle` | Camel's `camel-activemq` component is kept as-is |
| `integration-certificate-analytics-service/build.gradle` | Compiles against `jakarta.jms-api` and `spring-jms`; still valid |
| `HealthMonitor.java` | Autowires `ConnectionFactory` by type; auto-configured bean satisfies this |

### Manual Beans in `JmsConfig.java` (current)
- `jmsConnectionFactory()` → `CachingConnectionFactory(ActiveMQConnectionFactory(url, user, pass))` — **DELETE**
- `jmsTransactionManager(ConnectionFactory)` → `JmsTransactionManager` — **KEEP** (Spring Boot does not auto-configure this)
- `jmsListenerContainerFactory(JmsTransactionManager)` → custom `DefaultJmsListenerContainerFactory` — **KEEP**
- `jmsDestinationResolver()` → `DynamicDestinationResolver` — **KEEP**
- `jmsPDLLogTemplate(ConnectionFactory)` → `JmsTemplate` for `log.queueName` — **KEEP**
- `jmsPDLLogTemplateNoTx(ConnectionFactory)` → `@Profile("dev","testability-api")` non-transacted override — **KEEP**
- `jmsNotificationTemplateForAggregation(ConnectionFactory)` → `JmsTemplate` for aggregation queue — **KEEP**
- `jmsTemplateNotificationPostProcessing(ConnectionFactory)` → `JmsTemplate` for post-processing queue — **KEEP**
- `jmsTemplateNotificationWSSender(ConnectionFactory)` → `JmsTemplate` for WS queue — **KEEP**
- `jmsCertificateSenderTemplate(ConnectionFactory)` → `JmsTemplate` for certificate sender queue — **KEEP**

### Current Properties
```properties
# web/src/main/resources/application.properties
activemq.broker.url=vm://localhost?broker.persistent=false
activemq.broker.username=
activemq.broker.password=
jms.connection.factory.cache.level.name=CACHE_SESSION
```
```properties
# devops/dev/config/application-dev.properties
activemq.broker.url=tcp://localhost:61616\
     ?jms.nonBlockingRedelivery=true\
     &jms.redeliveryPolicy.maximumRedeliveries=3\
     &jms.redeliveryPolicy.maximumRedeliveryDelay=6000\
     &jms.redeliveryPolicy.initialRedeliveryDelay=4000\
     &jms.redeliveryPolicy.useExponentialBackOff=true\
     &jms.redeliveryPolicy.backOffMultiplier=2
activemq.broker.username=activemqUser
activemq.broker.password=activemqPassword
```

### Current Dependencies (`web/build.gradle`)
```gradle
implementation "org.apache.activemq:activemq-spring"   // provides ActiveMQConnectionFactory + embedded broker
implementation "org.springframework:spring-jms"         // spring JMS support
```

---

## Approach

Replace only the `jmsConnectionFactory()` bean — the single bean that Spring Boot auto-config can
fully own. All other JMS beans (transaction manager, listener factory, queue templates) are kept
as explicit beans because they carry application-specific configuration. This is a five sub-step
migration, each independently verifiable.

---

## Critical Complications (Must Address)

### 1. `CachingConnectionFactory` — Spring Boot Default Matches Current Behavior
Spring Boot's `ActiveMQAutoConfiguration` creates an `ActiveMQConnectionFactory` and wraps it in a
`CachingConnectionFactory` by default (`spring.jms.cache.enabled=true`). This matches the current
manual setup. **No extra configuration needed**, but verify that `spring.jms.cache.session-cache-size`
is adequate (default is `1`; the current manual `CachingConnectionFactory` also defaults to `1`).

### 2. `activemq-spring` vs `activemq-client-jakarta`
`spring-boot-starter-activemq` brings `activemq-client-jakarta` (Jakarta EE 9+) as its client.
`activemq-spring` (which is currently in `web/build.gradle`) also includes `activemq-broker` — the
embedded broker needed for `vm://localhost` (used as the default `activemq.broker.url`). After removing
`activemq-spring`, the embedded broker will no longer be on the classpath unless explicitly added.
**Resolution:** Add `runtimeOnly "org.apache.activemq:activemq-broker"` to `web/build.gradle` to
preserve `vm://` support for local dev/test without an external broker.

### 3. `jms.connection.factory.cache.level.name` is a Custom Property
This property (`CACHE_SESSION`) is read by `JmsConfig.jmsListenerContainerFactory()` via
`@Value("${jms.connection.factory.cache.level.name}")`. It is **not** a Spring Boot key and must
stay in `application.properties` unchanged. Do NOT attempt to map it to `spring.*`.

### 4. `jmsPDLLogTemplateNoTx` Profile Override
There are two beans potentially qualifying for injection as `jmsPDLLogTemplate`: the normal bean and the
`@Profile("dev","testability-api")` non-transacted override named `jmsPDLLogTemplateNoTx`. These are
**different bean names** — not a conflict. No changes needed here.

### 5. `NotificationJmsConfig` (`notification-sender`) — Depends on Shared `ConnectionFactory`
`NotificationJmsConfig.camelJmsConfiguration()` and `NotificationJmsConfig.jms()` (the Camel ActiveMQ
component) both accept the `ConnectionFactory` from `JmsConfig`. After the migration, the auto-configured
`CachingConnectionFactory` (bean name `jmsConnectionFactory` is no longer explicitly declared — the
auto-configured bean has type `CachingConnectionFactory` but the primary bean registered is of type
`ConnectionFactory`). Since `NotificationJmsConfig` wires by **type** (`ConnectionFactory`), the
auto-configured bean satisfies the injection. **No changes needed** in `notification-sender`.

### 6. `HealthMonitor` Wires `ConnectionFactory` Directly
`HealthMonitor.java` has `@Autowired private ConnectionFactory connectionFactory;`. The auto-configured
bean satisfies this injection by type. **No changes needed** in `HealthMonitor`.

### 7. Dev URL Multi-Line Continuation
The dev `application-dev.properties` uses `\` for multi-line URL continuation. This is a
`.properties` file feature. When migrating to `spring.activemq.broker-url`, collapse the URL to a
single line (`.properties` multi-line continuation is fragile and can break with trailing spaces).

---

## Sub-Steps

### Sub-step 16.1 — Update `web/build.gradle`

Replace the manual ActiveMQ + spring-jms dependencies with the Spring Boot starter. Add
`activemq-broker` as `runtimeOnly` to preserve embedded broker support for `vm://localhost`.

```gradle
// REMOVE:
implementation "org.apache.activemq:activemq-spring"
implementation "org.springframework:spring-jms"

// ADD:
implementation "org.springframework.boot:spring-boot-starter-activemq"
runtimeOnly "org.apache.activemq:activemq-broker"
```

`spring-jms` is now provided transitively by the starter. `activemq-broker` provides the embedded
broker used by the `vm://localhost` default URL in `application.properties`.

**Verify:** `./gradlew :web:compileJava` — no compile errors.

---

### Sub-step 16.2 — Migrate Properties in `application.properties`

In `web/src/main/resources/application.properties`, rename the three ActiveMQ connection properties:

```properties
# REMOVE:
activemq.broker.url=vm://localhost?broker.persistent=false
activemq.broker.username=
activemq.broker.password=

# ADD:
spring.activemq.broker-url=vm://localhost?broker.persistent=false
spring.activemq.user=
spring.activemq.password=
```

Keep `jms.connection.factory.cache.level.name=CACHE_SESSION` — this is a custom property still
consumed by `JmsConfig.jmsListenerContainerFactory()`.

**Verify:** `./gradlew :web:compileJava` — should compile (no runtime check yet).

---

### Sub-step 16.3 — Migrate Properties in `application-dev.properties`

In `devops/dev/config/application-dev.properties`, replace the three ActiveMQ dev overrides.
Collapse the multi-line URL to a single line:

```properties
# REMOVE:
activemq.broker.url=tcp://localhost:61616\
     ?jms.nonBlockingRedelivery=true\
     &jms.redeliveryPolicy.maximumRedeliveries=3\
     &jms.redeliveryPolicy.maximumRedeliveryDelay=6000\
     &jms.redeliveryPolicy.initialRedeliveryDelay=4000\
     &jms.redeliveryPolicy.useExponentialBackOff=true\
     &jms.redeliveryPolicy.backOffMultiplier=2
activemq.broker.username=activemqUser
activemq.broker.password=activemqPassword

# ADD:
spring.activemq.broker-url=tcp://localhost:61616?jms.nonBlockingRedelivery=true&jms.redeliveryPolicy.maximumRedeliveries=3&jms.redeliveryPolicy.maximumRedeliveryDelay=6000&jms.redeliveryPolicy.initialRedeliveryDelay=4000&jms.redeliveryPolicy.useExponentialBackOff=true&jms.redeliveryPolicy.backOffMultiplier=2
spring.activemq.user=activemqUser
spring.activemq.password=activemqPassword
```

**Verify:** No build check; this is a runtime config. Check during sub-step 16.5.

---

### Sub-step 16.4 — Refactor `JmsConfig.java`

Remove the manual `ConnectionFactory` bean and its three `@Value` fields. The auto-configured
`ConnectionFactory` (a `CachingConnectionFactory` wrapping `ActiveMQConnectionFactory`) is injected
by Spring Boot. All other beans remain.

**Remove from `JmsConfig.java`:**
```java
// Remove these @Value fields:
@Value("${activemq.broker.url}")
private String activeMqBrokerUrl;

@Value("${activemq.broker.username}")
private String activeMqBrokerUsername;

@Value("${activemq.broker.password}")
private String activeMqBrokerPassword;

// Remove this entire bean:
@Bean
public ConnectionFactory jmsConnectionFactory() {
    return new CachingConnectionFactory(
        new ActiveMQConnectionFactory(
            activeMqBrokerUsername, activeMqBrokerPassword, activeMqBrokerUrl));
}
```

**Remove these imports:**
```java
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
```

**Keep unchanged:**
- `@Configuration`, `@EnableJms`
- All `@Value` fields for queue names and `jmsConnectionFactoryCacheLevelName`
- `jmsTransactionManager(ConnectionFactory)` — Spring Boot injects the auto-configured `ConnectionFactory`
- `jmsListenerContainerFactory(JmsTransactionManager)`
- `jmsDestinationResolver()`
- All `JmsTemplate` beans
- `jmsPDLLogTemplateNoTx` profile override

The resulting class still has `import jakarta.jms.ConnectionFactory;` — this is correct. Spring Boot's
auto-configured bean satisfies all `ConnectionFactory` injection points.

**Verify:** `./gradlew :web:compileJava` — no compile errors.

---

### Sub-step 16.5 — Full Integration Verify

Run the full test suite and start the application to verify end-to-end JMS behavior.

```bash
./gradlew test
./gradlew bootRun   # Start with dev profile; check JMS listeners start
```

**Checklist:**
- `./gradlew test` — all tests pass (no `activemq.broker.url` `@Value` injection failures in test contexts)
- `./gradlew :notification-sender:camelTest` — Camel integration tests pass
- Application starts with `vm://localhost` embedded broker (default profile)
- Application starts with `tcp://localhost:61616` when `application-dev.properties` is loaded
- JMS listeners in `notification-sender` activate (check logs for `Started Camel routes`)
- `HealthMonitor` JMS check passes (visible via `/actuator/health` or Prometheus endpoint)
- No `NoSuchBeanDefinitionException` for `ConnectionFactory`

---

## Property Mapping Summary

| Old key | New key | Notes |
|---------|---------|-------|
| `activemq.broker.url` | `spring.activemq.broker-url` | URL value unchanged; collapse multi-line in dev |
| `activemq.broker.username` | `spring.activemq.user` | Value unchanged |
| `activemq.broker.password` | `spring.activemq.password` | Value unchanged |
| `jms.connection.factory.cache.level.name` | *(keep as-is)* | Custom property; consumed by `JmsConfig` |

---

## Dependency Change Summary

| Module | Change |
|--------|--------|
| `web/build.gradle` | Remove `activemq-spring` + `spring-jms`; add `spring-boot-starter-activemq` + `runtimeOnly activemq-broker` |
| `notification-sender/build.gradle` | No change |
| `integration-certificate-analytics-service/build.gradle` | No change |
