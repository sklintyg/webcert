# Step 13 — Convert Camel XML Routes to Java DSL

## Problem Statement

The `notification-sender` module bootstraps its Apache Camel contexts, JMS infrastructure, and
outbound CXF WS clients entirely through **7 XML files** loaded via `@ImportResource` in
`AppConfig.java`. After Step 12, `AppConfig.java` still has:

```java
@ImportResource({
  "classpath:notification-sender-config.xml",
})
```

That root XML imports 6 child XML files, defines inline beans, and sets up two separate
`CamelContext` instances (`webcertNotification` and `webcertCertificateSender`).

**Goal:** Replace every `notification-sender` XML file with Java `@Configuration` classes.
After this step, `AppConfig.java` has no `@ImportResource` at all, and all Spring XML files
in the project are deleted. Both Camel contexts remain fully functional. Camel tests pass
without loading any XML.

**Not in scope for this step:**
- Upgrading Camel version
- Replacing JMS manual config with Spring Boot auto-configuration → Step 16
- Any changes to route logic in `NotificationRouteBuilder` or `CertificateRouteBuilder`
  beyond removing named-endpoint string references

---

## Pre-Conditions (Step 12 Must Be Complete)

Verify each before beginning.

| Pre-condition | Verified by |
|---|---|
| Step 12 is fully done — `webcert-config.xml` deleted, no more XML Spring configs in `web/` | `find web/src -name "*.xml" -path "*/resources/*" \| grep -v camel \| grep -v test.xml` returns nothing |
| `AppConfig.java` has `@ImportResource({"classpath:notification-sender-config.xml"})` and nothing else in that annotation | `grep -n "ImportResource" web/src/main/java/.../AppConfig.java` |
| `JmsConfig.java` defines `jmsConnectionFactory`, `jmsTransactionManager`, `jmsDestinationResolver` | Confirm beans present in `web/src/main/java/.../config/JmsConfig.java` |
| All tests pass on the current state before Step 13 begins | `./gradlew :notification-sender:test :notification-sender:camelTest` |

---

## Current State

### XML Files That Need Action

| XML File | Location | What It Does | Action |
|---|---|---|---|
| `notification-sender-config.xml` | `notification-sender/src/main/resources/` | Root config — `annotation-config`, component-scan for `notifications.*`, imports 6 child XMLs, defines `notificationPatientEnricher` bean | **Replace** → `NotificationSenderConfig.java` |
| `jms-context.xml` | `notification-sender/src/main/resources/` | `jms` ActiveMQComponent, `camelJmsConfiguration` JmsConfiguration, `txTemplate` SpringTransactionPolicy | **Replace** → `NotificationJmsConfig.java` |
| `notifications/ws-context.xml` | `notification-sender/src/main/resources/` | CXF bus with logging, `certificateStatusUpdateForCareClientV3` JAX-WS client (profile `!dev`) | **Replace** → `NotificationWsClientConfig.java` |
| `notifications/beans-context.xml` | `notification-sender/src/main/resources/` | `notificationTransformer`, `notificationAggregator`, `processNotificationRequestRouteBuilder`, `objectMapper` (CustomObjectMapper), `notificationMessageDataFormat` (JacksonDataFormat) | **Replace** → `NotificationCamelConfig.java` |
| `notifications/camel-context.xml` | `notification-sender/src/main/resources/` | CamelContext `webcertNotification` with 4 named endpoints + `processNotificationRequestRouteBuilder` reference | **Merge** into `NotificationCamelConfig.java` |
| `certificates/beans-context.xml` | `notification-sender/src/main/resources/` | `certificateStoreProcessor`, `certificateSendProcessor`, `certificateRevokeProcessor`, `sendMessageToRecipientProcessor`, `registerApprovedReceiversProcessor` | **Replace** → `CertificateCamelConfig.java` |
| `certificates/camel-context.xml` | `notification-sender/src/main/resources/` | `certificateRouteBuilder` bean + CamelContext `webcertCertificateSender` with 1 named endpoint | **Merge** into `CertificateCamelConfig.java` |

### Test XML Files That Need Action

| XML File | Location | Used By | Action |
|---|---|---|---|
| `notifications/unit-test-notification-sender-config.xml` | `src/test/resources/` | `NotificationCamelTestConfig.java` via `@ImportResource` | **Replace** — update `NotificationCamelTestConfig.java` to use Java config directly |
| `certificates/unit-test-certificate-sender-config.xml` | `src/test/resources/` | `CertificateCamelTestConfig.java` via `@ImportResource` | **Replace** — update `CertificateCamelTestConfig.java` to use Java config directly |
| `certificates/integration-test-certificate-sender-config.xml` | `src/test/resources/` | `CertificateCamelIntegrationTestConfig.java` via `@ImportResource` | **Replace** with Java config |
| `integration-test-broker-context.xml` | `src/test/resources/` | Imported by above | **Replace** → `EmbeddedBrokerConfig.java` |

### Existing Java Classes Already Present

| Class | Location | Annotations | Notes |
|---|---|---|---|
| `NotificationRouteBuilder` | `notifications/routes/` | None | `RouteBuilder` subclass; uses `@Value` for most endpoints but has 2 named-endpoint STRING LITERALS that must be refactored (see 13.1) |
| `CertificateRouteBuilder` | `certificatesender/routes/` | None | `RouteBuilder` subclass; uses ONE named-endpoint string literal |
| `NotificationTransformer` | `notifications/services/` | None | Currently only registered as XML bean — needs explicit `@Bean` in Java config |
| `NotificationAggregator` | `notifications/services/` | None | Same |
| `NotificationPatientEnricher` | `notifications/services/` | None | Only defined in root XML (intentionally separate from `beans-context.xml` so tests skip it) — keep as explicit `@Bean` |
| `NotificationPostProcessor` | `notifications/services/` | `@Component` | Auto-discovered by component scan |
| `NotificationWSSender` | `notifications/services/v3/` | `@Component` | Auto-discovered |
| `CertificateStoreProcessor` | `certificatesender/services/` | None | Currently only registered as XML bean |
| `CertificateSendProcessor` | `certificatesender/services/` | None | Same |
| `CertificateRevokeProcessor` | `certificatesender/services/` | None | Same |
| `SendMessageToRecipientProcessor` | `certificatesender/services/` | None | Same |
| `RegisterApprovedReceiversProcessor` | `certificatesender/services/` | None | Same |

### Named Endpoint String Literals in RouteBuilders (Must Refactor)

`NotificationRouteBuilder.configure()` has two hard-coded string literals that reference named
Camel endpoints defined only in `notifications/camel-context.xml`:

| String Literal in Code | Named Endpoint ID in XML | URI Property |
|---|---|---|
| `from("receiveNotificationRequestEndpoint")` | `receiveNotificationRequestEndpoint` | `${receiveNotificationRequestEndpointUri}` |
| `.to("sendNotificationWSEndpoint")` and `from("sendNotificationWSEndpoint")` | `sendNotificationWSEndpoint` | `${sendNotificationWSEndpointUri}` |

`CertificateRouteBuilder.configure()` has one:

| String Literal in Code | Named Endpoint ID in XML | URI Property |
|---|---|---|
| `from("receiveCertificateTransferEndpoint")` | `receiveCertificateTransferEndpoint` | `${receiveCertificateTransferEndpointUri}` |

---

## Migration Strategy

1. **Refactor RouteBuilders first** — remove the named-endpoint string literals by adding
   `@Value` fields. This eliminates the need to programmatically register named endpoints
   in the Java CamelContext config, greatly simplifying steps 13.4 and 13.5.
2. **JMS config next** — `NotificationJmsConfig` depends on beans already in `JmsConfig.java`
   (web module). Confirm those are accessible via the shared Spring context.
3. **Per-context configs** — create Java config classes for notifications and certificates
   separately, mirroring the XML structure.
4. **Root config last** — once all child configs exist, create `NotificationSenderConfig.java`
   that imports them and add the component scan.
5. **Update `AppConfig.java`** — remove `@ImportResource`, add `@Import(NotificationSenderConfig.class)`.
6. **Test configs** — update `@ImportResource`-based test configs to pure Java. The
   test/production separation (`notificationPatientEnricher` only in production config) must
   be preserved.
7. **Delete XML files** — once tests pass.

### CamelContext Java Configuration Strategy

With Spring (non-Boot), `SpringCamelContext` from `camel-spring` provides Spring lifecycle
integration (`InitializingBean` → `start()`, `DisposableBean` → `stop()`). Since
`camel-spring` is currently `runtimeOnly` in `notification-sender/build.gradle`, it must be
changed to `implementation` before writing Java code that directly instantiates
`SpringCamelContext`.

Pattern for each CamelContext `@Bean`:
```java
@Bean
public SpringCamelContext webcertNotification(
        ApplicationContext applicationContext,
        NotificationRouteBuilder routeBuilder) {
    SpringCamelContext context = new SpringCamelContext(applicationContext);
    context.setId("webcertNotification");
    try {
        context.addRoutes(routeBuilder);
    } catch (Exception e) {
        throw new BeanCreationException("Failed to configure webcertNotification", e);
    }
    return context;
}
```

`SpringCamelContext(ApplicationContext)` registers all Spring beans as a Camel registry,
so the `jms` ActiveMQComponent bean, `txTemplate`, and `bean:notificationTransformer`
references in routes are all resolved automatically.

`afterPropertiesSet()` is called by Spring after the `@Bean` factory method returns, which
triggers context startup and route building. No additional init-method configuration is needed.

### Test/Production Separation for `notificationPatientEnricher`

The original XML intentionally separates `notificationPatientEnricher` from `beans-context.xml`:
> *"Declaring this bean outside the beans-context.xml so unit/integration-tests doesn't have to
> configure the full PU-service including Ignite"*

This separation must be preserved in Java:
- `NotificationCamelConfig.java` = beans needed for tests (equivalent of `beans-context.xml`)
- `NotificationSenderConfig.java` = production root config including `notificationPatientEnricher`
- Test configs import `NotificationCamelConfig.java` directly, not `NotificationSenderConfig.java`

---

## Progress Tracker

| Sub-step | Title | Risk | Status |
|---|---|---|---|
| **Phase A: Prepare RouteBuilders** | | | |
| 13.1 | Add `@Value` endpoint URI fields to `NotificationRouteBuilder` and `CertificateRouteBuilder` | Low | ⬜ |
| **Phase B: Change Build Config** | | | |
| 13.2 | Change `camel-spring` from `runtimeOnly` to `implementation` in build.gradle | Low | ⬜ |
| **Phase C: Create Production Java Configs** | | | |
| 13.3 | Create `NotificationJmsConfig.java` replacing `jms-context.xml` | Low | ⬜ |
| 13.4 | Create `NotificationWsClientConfig.java` replacing `notifications/ws-context.xml` | Low | ⬜ |
| 13.5 | Create `NotificationCamelConfig.java` replacing `notifications/beans-context.xml` + `notifications/camel-context.xml` | Medium | ⬜ |
| 13.6 | Create `CertificateCamelConfig.java` replacing `certificates/beans-context.xml` + `certificates/camel-context.xml` | Medium | ⬜ |
| 13.7 | Create `NotificationSenderConfig.java` replacing `notification-sender-config.xml` | Low | ⬜ |
| 13.8 | Update `AppConfig.java` — remove `@ImportResource`, add `@Import(NotificationSenderConfig.class)` | Low | ⬜ |
| **Phase D: Replace Test XML Configs** | | | |
| 13.9 | Update `NotificationCamelTestConfig.java` — remove `@ImportResource` | Low | ⬜ |
| 13.10 | Update `CertificateCamelTestConfig.java` — remove `@ImportResource` | Low | ⬜ |
| 13.11 | Create `EmbeddedBrokerConfig.java` and update `CertificateCamelIntegrationTestConfig.java` | Medium | ⬜ |
| **Phase E: Delete XML Files** | | | |
| 13.12 | Delete all 7 production XML files and 4 test XML files | Low | ⬜ |

---

## Phase A: Prepare RouteBuilders

### Sub-step 13.1 — Add `@Value` endpoint URI fields to RouteBuilders

**What:** Two `RouteBuilder` classes use string literals to reference named Camel endpoints
defined only in the Camel XML contexts. Replace these with `@Value`-injected fields so the
Java CamelContext config does not need to programmatically register named endpoints.

**Why:** Named Camel endpoints in XML (e.g., `<camel:endpoint id="receiveNotificationRequestEndpoint" uri="..."/>`)
register an endpoint in the Camel registry by ID. The `from("receiveNotificationRequestEndpoint")`
call in Java looks up this ID. Without the XML, the lookup fails. Adding `@Value` fields makes
the route self-contained.

#### Changes to `NotificationRouteBuilder.java`

Add two `@Value` fields:
```java
@Value("${receiveNotificationRequestEndpointUri}")
private String notificationReceiveQueue;  // replaces the "receiveNotificationRequestEndpoint" string literal

@Value("${sendNotificationWSEndpointUri}")
private String notificationWSQueue;  // replaces the "sendNotificationWSEndpoint" string literal
```

Replace in `configure()`:
```java
// Before:
from("receiveNotificationRequestEndpoint")
    // ...
    .to("sendNotificationWSEndpoint");

from("sendNotificationWSEndpoint")

// After:
from(notificationReceiveQueue)
    // ...
    .to(notificationWSQueue);

from(notificationWSQueue)
```

**Pre-check:** Confirm the exact property names exist in `application.properties` or
`webcert-notification-route-params.properties`:
```bash
grep "receiveNotificationRequestEndpointUri\|sendNotificationWSEndpointUri" \
  web/src/main/resources/application.properties \
  web/src/main/resources/webcert-notification-route-params.properties \
  notification-sender/src/test/resources/notifications/unit-test.properties
```
All three property names must resolve. In tests, `unit-test.properties` maps them to `direct:` URIs.

#### Changes to `CertificateRouteBuilder.java`

Add one `@Value` field:
```java
@Value("${receiveCertificateTransferEndpointUri}")
private String receiveCertificateTransferUri;  // replaces "receiveCertificateTransferEndpoint" literal
```

Replace in `configure()`:
```java
// Before:
from("receiveCertificateTransferEndpoint")

// After:
from(receiveCertificateTransferUri)
```

**Pre-check:** Confirm the property exists:
```bash
grep "receiveCertificateTransferEndpointUri" \
  web/src/main/resources/webcert-notification-route-params.properties \
  notification-sender/src/test/resources/certificates/unit-test.properties \
  notification-sender/src/test/resources/certificates/integration-test.properties
```

**Verification:** `./gradlew :notification-sender:test :notification-sender:camelTest` — all
route tests pass (no `ResolveEndpointFailedException` for the old endpoint names).

---

## Phase B: Change Build Config

### Sub-step 13.2 — Change `camel-spring` scope in `notification-sender/build.gradle`

**What:** Change `runtimeOnly "org.apache.camel:camel-spring"` to `implementation`.

**Why:** `SpringCamelContext` (from `camel-spring`) will now be directly instantiated in Java
`@Configuration` code. With `runtimeOnly`, the class is not available on the compile classpath
and the Java config will not compile.

**Changes:**
Open `notification-sender/build.gradle` and change:
```gradle
// Before:
runtimeOnly "org.apache.camel:camel-spring"

// After:
implementation "org.apache.camel:camel-spring"
```

**Verification:** `./gradlew :notification-sender:compileJava` — clean compile, no missing
class errors.

---

## Phase C: Create Production Java Configs

### Sub-step 13.3 — Create `NotificationJmsConfig.java` replacing `jms-context.xml`

**What:** Replace `jms-context.xml` with a `@Configuration` class. This file defines the
Camel JMS bridge — the `jms` Camel component and its backing `JmsConfiguration`.

**Why:** `jms-context.xml` defines:
- `jms` (`ActiveMQComponent`) — Camel JMS transport backed by the Spring connection factory
- `camelJmsConfiguration` (`JmsConfiguration`) — wraps `jmsConnectionFactory` and
  `jmsDestinationResolver` (both from `JmsConfig.java` in `web`)
- `txTemplate` (`SpringTransactionPolicy`) — wraps `jmsTransactionManager` for Camel
  transacted routes (`.transacted("txTemplate")`)

The existing `JmsConfig.java` in `web` already defines `jmsConnectionFactory`,
`jmsTransactionManager`, and `jmsDestinationResolver`. When `notification-sender-config.xml`
is loaded in the same Spring context as `AppConfig.java`, those beans are shared. The same
is true after migration to Java config.

**Create** `notification-sender/src/main/java/se/inera/intyg/webcert/notification_sender/config/NotificationJmsConfig.java`:
```java
@Configuration
public class NotificationJmsConfig {

    @Bean
    public JmsConfiguration camelJmsConfiguration(
            ConnectionFactory jmsConnectionFactory,
            DestinationResolver jmsDestinationResolver) {
        JmsConfiguration config = new JmsConfiguration();
        config.setErrorHandlerLoggingLevel(LoggingLevel.OFF);
        config.setErrorHandlerLogStackTrace(false);
        config.setConnectionFactory(jmsConnectionFactory);
        config.setDestinationResolver(jmsDestinationResolver);
        return config;
    }

    @Bean
    public ActiveMQComponent jms(JmsConfiguration camelJmsConfiguration) {
        ActiveMQComponent component = new ActiveMQComponent();
        component.setConfiguration(camelJmsConfiguration);
        component.setTransacted(true);
        component.setCacheLevelName("CACHE_CONSUMER");
        return component;
    }

    @Bean
    public SpringTransactionPolicy txTemplate(JmsTransactionManager jmsTransactionManager) {
        return new SpringTransactionPolicy(jmsTransactionManager);
    }
}
```

> ⚠️ **Bean name `txTemplate` is fixed:** The `NotificationRouteBuilder` calls
> `.transacted("txTemplate")` — Camel resolves this by looking up a Spring bean named
> `txTemplate` of type `SpringTransactionPolicy`. The `@Bean` name **must** match exactly.

> ⚠️ **`ConnectionFactory` injection:** The `jmsConnectionFactory` bean is defined in
> `JmsConfig.java` (web module). It is of type `CachingConnectionFactory` wrapping an
> `ActiveMQConnectionFactory`. The `JmsTransactionManager` is also defined there.
> Both are available in the shared Spring context.

> ⚠️ **`LoggingLevel.OFF`:** Import from `org.apache.camel.LoggingLevel`, not from
> any SLF4J or Logback class.

**Verification:** `./gradlew :notification-sender:test` — no `NoSuchBeanDefinitionException`
for `txTemplate` or `jms`.

---

### Sub-step 13.4 — Create `NotificationWsClientConfig.java` replacing `notifications/ws-context.xml`

**What:** Replace `notifications/ws-context.xml` with a Java `@Configuration` that defines
the outbound CXF JAX-WS client and bus logging.

**Why:** `notifications/ws-context.xml` defines:
- CXF bus with `<cxf:logging/>` feature (applies to all CXF clients in this context)
- `certificateStatusUpdateForCareClientV3` JAX-WS client — **profile `!dev`** only

**⚠️ Profile is `!dev` (not `!prod`):** The XML uses `<beans profile="!dev">` which means
the client is created in ALL profiles EXCEPT `dev`. The `@Profile("!dev")` Java annotation
maps directly to this. Do not change it to `!prod`.

**Create** `notification-sender/src/main/java/se/inera/intyg/webcert/notification_sender/config/NotificationWsClientConfig.java`:
```java
@Configuration
public class NotificationWsClientConfig {

    @Autowired
    void configureBus(Bus bus) {
        bus.getFeatures().add(new LoggingFeature());
    }

    @Bean
    @Profile("!dev")
    public CertificateStatusUpdateForCareResponderInterface certificateStatusUpdateForCareClientV3(
            @Value("${certificatestatusupdateforcare.ws.endpoint.v3.url}") String address) {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(CertificateStatusUpdateForCareResponderInterface.class);
        factory.setAddress(address);
        factory.getProperties().put("schema-validation-enabled", true);
        return (CertificateStatusUpdateForCareResponderInterface) factory.create();
    }
}
```

**Pre-check:** Confirm the property name matches:
```bash
grep "certificatestatusupdateforcare.ws.endpoint.v3.url" \
  web/src/main/resources/application.properties
```

**Verification:** `./gradlew :notification-sender:test` — `certificateStatusUpdateForCareClientV3`
bean is absent with profile `dev`, present with default profile.

> ⚠️ **`CxfEndpointConfig` double-load hazard:** `AppConfig` has
> `@ComponentScan("se.inera.intyg.webcert.web")` which would also pick up `CxfEndpointConfig`
> (in `se.inera.intyg.webcert.web.config`) into the **root** Spring context. `CxfEndpointConfig`
> has `@Import(NotificationStubConfig.class)`, which drags the stub bean into the root context.
> With `dev` profile the `@Profile("!dev")` HTTP proxy is suppressed, leaving the stub as the
> only `CertificateStatusUpdateForCareResponderInterface` bean — so `NotificationWSSender`
> autowires the stub directly and no HTTP call reaches the mock service.
>
> Fix: exclude `CxfEndpointConfig` from the root-context component scan in `AppConfig`:
> ```java
> @ComponentScan(
>     value = "se.inera.intyg.webcert.web",
>     excludeFilters =
>         @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CxfEndpointConfig.class))
> ```
> `CxfEndpointConfig` must only be loaded by the CXF servlet child context via `web.xml`
> `contextConfigLocation`, exactly as `services-cxf-servlet.xml` was in the main branch.

---

### Sub-step 13.5 — Create `NotificationCamelConfig.java`

**What:** Combine `notifications/beans-context.xml` and `notifications/camel-context.xml` into
a single Java `@Configuration` that defines the notification beans and creates the
`webcertNotification` CamelContext.

**Why:** `notifications/beans-context.xml` defines explicit beans for classes that have no
`@Component` annotation (`NotificationTransformer`, `NotificationAggregator`,
`NotificationRouteBuilder`), plus library beans (`CustomObjectMapper`, `JacksonDataFormat`).
`notifications/camel-context.xml` wraps these in a named Camel context.

**After sub-step 13.1**, no named endpoint registration is needed in the Java CamelContext
because the route builder uses `@Value` URIs directly.

**Create** `notification-sender/src/main/java/se/inera/intyg/webcert/notification_sender/notifications/config/NotificationCamelConfig.java`:
```java
@Configuration
public class NotificationCamelConfig {

    @Bean
    public NotificationTransformer notificationTransformer() {
        return new NotificationTransformer();
    }

    @Bean
    public NotificationAggregator notificationAggregator() {
        return new NotificationAggregator();
    }

    @Bean
    public NotificationRouteBuilder processNotificationRequestRouteBuilder() {
        return new NotificationRouteBuilder();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new CustomObjectMapper();
    }

    @Bean
    public JacksonDataFormat notificationMessageDataFormat(ObjectMapper objectMapper) {
        return new JacksonDataFormat(objectMapper,
            NotificationMessage.class);
    }

    @Bean
    public SpringCamelContext webcertNotification(
            ApplicationContext applicationContext,
            NotificationRouteBuilder processNotificationRequestRouteBuilder) {
        SpringCamelContext context = new SpringCamelContext(applicationContext);
        context.setId("webcertNotification");
        try {
            context.addRoutes(processNotificationRequestRouteBuilder);
        } catch (Exception e) {
            throw new BeanCreationException("webcertNotification", "Failed to add routes", e);
        }
        return context;
    }
}
```

> ⚠️ **`objectMapper` bean name:** The `notificationMessageDataFormat` bean (JacksonDataFormat)
> previously referenced a bean named `objectMapper`. The Java config exposes it via `@Bean`
> with the default name `objectMapper` (method name). Confirm no other bean in the shared
> Spring context also defines `objectMapper` at this time. If `AppConfig.java` has one, this
> will conflict — check and deduplicate.

> ⚠️ **`NotificationTransformer` dependencies:** `NotificationTransformer` likely has
> `@Autowired` fields that are populated by Spring after construction. Since it is not annotated
> `@Component`, Spring will still process `@Autowired` fields when the bean is created via a
> `@Bean` factory method (Spring calls `AutowiredAnnotationBeanPostProcessor` on `@Bean`
> returned objects). Verify by running tests after this sub-step.

> ⚠️ **`SpringCamelContext` auto-start:** `SpringCamelContext` implements `InitializingBean`
> and `DisposableBean`. Spring calls `afterPropertiesSet()` automatically, which starts the
> context. No `initMethod`/`destroyMethod` on `@Bean` is needed. Do NOT add them, as it would
> cause double-start.

> ⚠️ **Multiple CamelContexts:** Since there are two `SpringCamelContext` beans
> (`webcertNotification` and `webcertCertificateSender`), Camel's auto-discovery of
> `RouteBuilder` beans in the Spring context must be suppressed. By calling
> `context.addRoutes(routeBuilder)` explicitly AND NOT adding `@Component` to the
> RouteBuilders, only the explicitly registered builders will be added to each context.
> If `SpringCamelContext` still auto-discovers all builders (Camel version dependent),
> set `context.setAutoStartup(false)` and call start manually, or set
> `context.setUseNewSpringApplicationContextForTests(false)`.
>
> **Verify:** After startup, `webcertNotification` should have exactly 5 routes
> (aggregateNotification, transformNotification, sendNotificationToWS,
> notificationPostProcessing, errorLogging/temporaryErrorLogging) and
> `webcertCertificateSender` should have exactly 3 routes
> (transferCertificate, permanentErrorLogging, temporaryErrorLogging).

**Verification:** `./gradlew :notification-sender:test` — `NotificationRouteTest` passes.

---

### Sub-step 13.6 — Create `CertificateCamelConfig.java`

**What:** Combine `certificates/beans-context.xml` and `certificates/camel-context.xml` into
a Java `@Configuration` that defines certificate processor beans and creates the
`webcertCertificateSender` CamelContext.

**Why:** `certificates/beans-context.xml` defines 5 processor beans (none have `@Component`).
`certificates/camel-context.xml` creates the Camel context with the `CertificateRouteBuilder`.

**Create** `notification-sender/src/main/java/se/inera/intyg/webcert/notification_sender/certificatesender/config/CertificateCamelConfig.java`:
```java
@Configuration
public class CertificateCamelConfig {

    @Bean
    public CertificateStoreProcessor certificateStoreProcessor() {
        return new CertificateStoreProcessor();
    }

    @Bean
    public CertificateSendProcessor certificateSendProcessor() {
        return new CertificateSendProcessor();
    }

    @Bean
    public CertificateRevokeProcessor certificateRevokeProcessor() {
        return new CertificateRevokeProcessor();
    }

    @Bean
    public SendMessageToRecipientProcessor sendMessageToRecipientProcessor() {
        return new SendMessageToRecipientProcessor();
    }

    @Bean
    public RegisterApprovedReceiversProcessor registerApprovedReceiversProcessor() {
        return new RegisterApprovedReceiversProcessor();
    }

    @Bean
    public CertificateRouteBuilder certificateRouteBuilder() {
        return new CertificateRouteBuilder();
    }

    @Bean
    public SpringCamelContext webcertCertificateSender(
            ApplicationContext applicationContext,
            CertificateRouteBuilder certificateRouteBuilder) {
        SpringCamelContext context = new SpringCamelContext(applicationContext);
        context.setId("webcertCertificateSender");
        try {
            context.addRoutes(certificateRouteBuilder);
        } catch (Exception e) {
            throw new BeanCreationException("webcertCertificateSender", "Failed to add routes", e);
        }
        return context;
    }
}
```

> ⚠️ **Processor `@Autowired` fields:** Same caveat as `NotificationTransformer` above —
> Spring will process `@Autowired` on returned `@Bean` objects. Verify processors receive
> their injected dependencies (e.g., `CertificateSendProcessor` may inject a WS client).

**Verification:** `./gradlew :notification-sender:test` — `RouteTest` (certificate route
unit test) passes.

---

### Sub-step 13.7 — Create `NotificationSenderConfig.java` replacing `notification-sender-config.xml`

**What:** Create the root Java `@Configuration` for the notification-sender module, replacing
`notification-sender-config.xml`.

**Why:** The root XML:
1. `<context:annotation-config/>` — equivalent to `@Configuration` itself + bean post processors
   that are always active with `@ComponentScan`
2. `<context:component-scan base-package="se.inera.intyg.webcert.notification_sender.notifications"/>`
   — discovers `@Component`/`@Service` beans in the notifications package
3. Imports 6 child XMLs — now handled by `@Import` of the new Java configs
4. Defines `notificationPatientEnricher` inline bean — must remain as explicit `@Bean`
   (separated from `NotificationCamelConfig` so tests can exclude it)

**Create** `notification-sender/src/main/java/se/inera/intyg/webcert/notification_sender/config/NotificationSenderConfig.java`:
```java
@Configuration
@ComponentScan("se.inera.intyg.webcert.notification_sender.notifications")
@Import({
    NotificationJmsConfig.class,
    NotificationWsClientConfig.class,
    NotificationCamelConfig.class,
    CertificateCamelConfig.class,
})
public class NotificationSenderConfig {

    // Declared here (not in NotificationCamelConfig) so tests can load NotificationCamelConfig
    // alone without pulling in PU-service dependencies.
    @Bean
    public NotificationPatientEnricher notificationPatientEnricher() {
        return new NotificationPatientEnricher();
    }
}
```

> ⚠️ **`@ComponentScan` scope:** The scan covers `se.inera.intyg.webcert.notification_sender.notifications`
> (matching the XML). This picks up `@Component`/`@Service` beans like `NotificationPostProcessor`,
> `NotificationWSSender`, `NotificationRedeliveryStrategyFactory`, etc. It does NOT scan
> `certificatesender` — those are explicit `@Bean` methods in `CertificateCamelConfig`.

> ⚠️ **`NotificationPatientEnricher` dependencies:** `NotificationPatientEnricher` is injected
> with PU-service beans. Those are found via `AppConfig`'s `@ComponentScan("se.inera.intyg.webcert.infra.pu.integration.intygproxyservice")`.
> Confirm those scans are still active in `AppConfig.java`.

**Verification:** `./gradlew :notification-sender:test :notification-sender:camelTest` —
all tests pass.

---

### Sub-step 13.8 — Update `AppConfig.java`

**What:** Remove `@ImportResource({"classpath:notification-sender-config.xml"})` and replace
with `@Import(NotificationSenderConfig.class)`.

**Why:** With `NotificationSenderConfig.java` created, the `@ImportResource` is obsolete.
After removing it, `AppConfig.java` will have no `@ImportResource` at all — the application
no longer loads any Spring XML configuration.

**Changes to `web/src/main/java/.../config/AppConfig.java`:**

1. Remove the `@ImportResource` annotation entirely.
2. Add `NotificationSenderConfig.class` to the `@Import` list:
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
       NotificationSenderConfig.class,  // ← add this
   })
   // remove @ImportResource entirely
   ```
3. Add the import for `NotificationSenderConfig`:
   ```java
   import se.inera.intyg.webcert.notification_sender.config.NotificationSenderConfig;
   ```

**Post-change verification:**
```bash
grep -rn "@ImportResource" web/src/main/java/
# Must return no results
grep -rn "notification-sender-config.xml" .
# Must return no results
```

**Verification:** `./gradlew :web:test :notification-sender:test` — all tests pass.
Application starts: `./gradlew appRun` — Camel contexts start, notification and certificate
queues connect.

---

## Phase D: Replace Test XML Configs

### Sub-step 13.9 — Update `NotificationCamelTestConfig.java`

**What:** Remove `@ImportResource(locations = "classpath:notifications/unit-test-notification-sender-config.xml")`
and replace with direct Java configuration.

**Why:** `unit-test-notification-sender-config.xml` does:
1. Loads `classpath:notifications/unit-test.properties` via `<context:property-placeholder>`
2. Imports `notifications/camel-context.xml` (the CamelContext XML)
3. Defines `processNotificationRequestRouteBuilder`, `notificationAggregator`,
   `customObjectMapper`, `notificationMessageDataFormat` explicitly for test use

After migration, the production `NotificationCamelConfig.java` provides these beans. The test
config just needs to:
1. Load test properties via `@TestPropertySource`
2. Import `NotificationCamelConfig.java`
3. Provide mocked beans for dependencies not in `NotificationCamelConfig`

**Changes to `NotificationCamelTestConfig.java`:**
```java
// Before:
@ImportResource(locations = "classpath:notifications/unit-test-notification-sender-config.xml")
public class NotificationCamelTestConfig {

// After:
@Configuration
@TestPropertySource("classpath:notifications/unit-test.properties")
@Import(NotificationCamelConfig.class)
public class NotificationCamelTestConfig {
```

The existing `@Bean` methods in `NotificationCamelTestConfig` provide mocks for
`notificationTransformer`, `notificationWSSender`, `notificationPostProcessor`,
`transactionManager`, `txTemplate`. These remain — they override the real beans from
`NotificationCamelConfig` via `@Bean` override (last definition wins for non-primary beans).

> ⚠️ **Bean override:** Spring by default allows definition override (last wins). If
> `NotificationCamelConfig` defines `notificationTransformer()` returning a real instance,
> and `NotificationCamelTestConfig` defines `notificationTransformer()` returning `null`,
> the test version wins. But this relies on definition ordering — if tests fail with
> "bean already defined", add `spring.main.allow-bean-definition-overriding=true` to
> `src/test/resources/application.properties` in the notification-sender module, or use
> `@Primary` on the mock beans.

> ⚠️ **`txTemplate` conflict:** `NotificationJmsConfig.java` defines `txTemplate`.
> `NotificationCamelTestConfig` also defines it. Tests don't import `NotificationJmsConfig`
> (it's only in `NotificationSenderConfig`). No conflict — the test's `txTemplate` is the
> only one.

**Verification:** `./gradlew :notification-sender:test` — `NotificationRouteTest` passes.

---

### Sub-step 13.10 — Update `CertificateCamelTestConfig.java`

**What:** Remove `@ImportResource(locations = "classpath:certificates/unit-test-certificate-sender-config.xml")`
and replace with Java configuration.

**Why:** `unit-test-certificate-sender-config.xml` does:
1. Imports `certificates/beans-context.xml` (processor beans)
2. Imports `certificates/camel-context.xml` (CamelContext)
3. Loads `classpath:certificates/unit-test.properties`
4. `<context:component-scan base-package="se.inera.intyg.webcert.notification_sender.certificatesender"/>` — was a workaround to discover processors; now handled by `CertificateCamelConfig`

**Changes to `CertificateCamelTestConfig.java`:**
```java
// Before:
@ImportResource(locations = "classpath:certificates/unit-test-certificate-sender-config.xml")
public class CertificateCamelTestConfig {

// After:
@Configuration
@TestPropertySource("classpath:certificates/unit-test.properties")
@Import(CertificateCamelConfig.class)
public class CertificateCamelTestConfig {
```

The existing mocked `@Bean` methods (`intygModuleRegistry`, `sendMessageToRecipientResponderInterface`,
`registerApprovedReceiversResponderInterface`, `transactionManager`, `txTemplate`,
`mockSendCertificateServiceClient`) remain — they satisfy dependencies of processor beans.

**Verification:** `./gradlew :notification-sender:test` — `RouteTest` passes.

---

### Sub-step 13.11 — Create `EmbeddedBrokerConfig.java` and update `CertificateCamelIntegrationTestConfig.java`

**What:** `integration-test-broker-context.xml` defines a full embedded ActiveMQ broker for
integration testing. Replace it with Java `@Bean` configuration. Then update
`CertificateCamelIntegrationTestConfig.java` to remove its `@ImportResource`.

**Why:** `integration-test-broker-context.xml` defines:
- `jmsConnectionFactory` (`ActiveMQConnectionFactory`) connecting to `tcp://localhost:61618`
  with a configured `RedeliveryPolicy`
- `cachingConnectionFactory` (`CachingConnectionFactory`) wrapping the above
- `jmsTemplate` (`JmsTemplate`)
- `txTemplate` and `jmsTransactionManager` (`SpringTransactionPolicy` + `JmsTransactionManager`)
- `jms` (`ActiveMQComponent`)
- `PROPAGATION_REQUIRED` (`SpringTransactionPolicy`)
- An embedded `<broker>` with destination policies for `certificateQueue` and
  `sendNotificationToWS` queues
- `certificateQueue`, `dlq`, `notificationQueue`, `notificationQueueForAggregation`
  ActiveMQ queue beans

**Create** `notification-sender/src/test/java/.../testconfig/EmbeddedBrokerConfig.java`:
```java
@Configuration
public class EmbeddedBrokerConfig {

    @Value("${errorhandling.maxRedeliveries}")
    private int maxRedeliveries;

    @Value("${errorhandling.maxRedeliveryDelay}")
    private long maxRedeliveryDelay;

    @Value("${errorhandling.redeliveryDelay}")
    private long redeliveryDelay;

    @Bean(destroyMethod = "stop")
    public BrokerService embeddedBroker() throws Exception {
        BrokerService broker = new BrokerService();
        broker.setPersistent(false);
        broker.addConnector("tcp://localhost:61618");

        // Dead-letter strategy for certificateQueue
        IndividualDeadLetterStrategy dlStrategy = new IndividualDeadLetterStrategy();
        dlStrategy.setQueuePrefix("DLQ.");
        dlStrategy.setUseQueueForQueueMessages(true);

        PolicyEntry certEntry = new PolicyEntry();
        certEntry.setQueue("certificateQueue");
        certEntry.setDeadLetterStrategy(dlStrategy);

        PolicyEntry notifEntry = new PolicyEntry();
        notifEntry.setQueue("sendNotificationToWS");
        notifEntry.setDeadLetterStrategy(dlStrategy);

        PolicyMap policyMap = new PolicyMap();
        policyMap.setPolicyEntries(List.of(certEntry, notifEntry));
        broker.setDestinationPolicy(policyMap);
        broker.start();
        return broker;
    }

    @Bean
    public ActiveMQConnectionFactory jmsConnectionFactory() {
        RedeliveryPolicy policy = new RedeliveryPolicy();
        policy.setMaximumRedeliveries(maxRedeliveries);
        policy.setMaximumRedeliveryDelay(maxRedeliveryDelay);
        policy.setInitialRedeliveryDelay(redeliveryDelay);
        policy.setUseExponentialBackOff(true);
        policy.setBackOffMultiplier(2);

        ActiveMQConnectionFactory factory =
            new ActiveMQConnectionFactory("tcp://localhost:61618");
        factory.setRedeliveryPolicy(policy);
        factory.setNonBlockingRedelivery(true);
        return factory;
    }

    @Bean
    public CachingConnectionFactory cachingConnectionFactory(
            ActiveMQConnectionFactory jmsConnectionFactory) {
        return new CachingConnectionFactory(jmsConnectionFactory);
    }

    @Bean
    public JmsTemplate jmsTemplate(CachingConnectionFactory cachingConnectionFactory) {
        JmsTemplate template = new JmsTemplate();
        template.setConnectionFactory(cachingConnectionFactory);
        return template;
    }

    @Bean
    public JmsTransactionManager jmsTransactionManager(
            CachingConnectionFactory cachingConnectionFactory) {
        return new JmsTransactionManager(cachingConnectionFactory);
    }

    @Bean
    public SpringTransactionPolicy txTemplate(JmsTransactionManager jmsTransactionManager) {
        return new SpringTransactionPolicy(jmsTransactionManager);
    }

    @Bean
    public SpringTransactionPolicy PROPAGATION_REQUIRED(
            JmsTransactionManager jmsTransactionManager) {
        SpringTransactionPolicy policy = new SpringTransactionPolicy(jmsTransactionManager);
        policy.setPropagationBehaviorName("PROPAGATION_REQUIRED");
        return policy;
    }

    @Bean
    public ActiveMQComponent jms(CachingConnectionFactory cachingConnectionFactory,
                                  JmsTransactionManager jmsTransactionManager) {
        ActiveMQComponent component = new ActiveMQComponent();
        component.setConnectionFactory(cachingConnectionFactory);
        component.setTransactionManager(jmsTransactionManager);
        component.setTransacted(true);
        component.setCacheLevelName("CACHE_CONSUMER");
        return component;
    }

    @Bean
    public ActiveMQQueue certificateQueue() {
        return new ActiveMQQueue("certificateQueue");
    }

    @Bean
    public ActiveMQQueue dlq() {
        return new ActiveMQQueue("DLQ.certificateQueue");
    }

    @Bean
    public ActiveMQQueue notificationQueue() {
        return new ActiveMQQueue("notificationQueue");
    }

    @Bean
    public ActiveMQQueue notificationQueueForAggregation() {
        return new ActiveMQQueue("notificationQueueForAggregation");
    }
}
```

**Update** `CertificateCamelIntegrationTestConfig.java`:
```java
// Before:
@ImportResource(locations = "classpath:certificates/integration-test-certificate-sender-config.xml")
public class CertificateCamelIntegrationTestConfig {

// After:
@Configuration
@TestPropertySource("classpath:certificates/integration-test.properties")
@Import({CertificateCamelConfig.class, EmbeddedBrokerConfig.class})
public class CertificateCamelIntegrationTestConfig {
```

The existing mocked `@Bean` methods remain.

> ⚠️ **`BrokerService` import:** `BrokerService` is from `org.apache.activemq.broker`.
> `IndividualDeadLetterStrategy` and `PolicyEntry` are from
> `org.apache.activemq.broker.region.policy`. These are available via the existing
> `testImplementation "org.apache.activemq:activemq-spring"` dependency.

> ⚠️ **Port conflict:** The embedded broker uses `tcp://localhost:61618`. If tests run in
> parallel, a port conflict can occur. Verify this is acceptable (the existing XML used
> the same port).

> ⚠️ **`@DependsOn` broker startup:** The `jmsConnectionFactory` must be created AFTER
> the broker starts. Spring processes `@Bean` methods in dependency order — since
> `jmsConnectionFactory` doesn't inject the `BrokerService`, Spring may create them in
> any order. Add `@DependsOn("embeddedBroker")` to `jmsConnectionFactory()` and `jms()`.

**Verification:** `./gradlew :notification-sender:camelTest` — `RouteIT` integration test passes.

---

## Phase E: Delete XML Files

### Sub-step 13.12 — Delete all notification-sender XML files

**Pre-checks (run ALL before deleting):**
```bash
# All production tests pass with the new Java configs
./gradlew :notification-sender:test :notification-sender:camelTest

# No remaining @ImportResource referencing these XMLs
grep -rn "ImportResource" notification-sender/src/ --include="*.java"
# Must return nothing

# No remaining <import resource= referencing notification-sender XMLs in the web module
grep -rn "notification-sender-config\|jms-context\|ws-context\|beans-context\|camel-context" \
  web/src/main/resources/ web/src/test/resources/
# Must return nothing

# AppConfig has no @ImportResource
grep -n "ImportResource" web/src/main/java/se/inera/intyg/webcert/web/config/AppConfig.java
# Must return nothing
```

**Production XML files to delete:**
```
notification-sender/src/main/resources/notification-sender-config.xml
notification-sender/src/main/resources/jms-context.xml
notification-sender/src/main/resources/notifications/ws-context.xml
notification-sender/src/main/resources/notifications/beans-context.xml
notification-sender/src/main/resources/notifications/camel-context.xml
notification-sender/src/main/resources/certificates/beans-context.xml
notification-sender/src/main/resources/certificates/camel-context.xml
```

**Test XML files to delete:**
```
notification-sender/src/test/resources/notifications/unit-test-notification-sender-config.xml
notification-sender/src/test/resources/certificates/unit-test-certificate-sender-config.xml
notification-sender/src/test/resources/certificates/integration-test-certificate-sender-config.xml
notification-sender/src/test/resources/integration-test-broker-context.xml
```

**Verification:**
```bash
./gradlew build
```
Must compile and all tests pass. Then verify:
```bash
find notification-sender/src -name "*.xml" -not -name "logback-test.xml"
# Must return nothing
```

---

## Final Verification — After Complete Step 13

```bash
./gradlew test
```
All tests pass (including `notification-sender:camelTest`).

```bash
./gradlew appRun
```
Application starts on Gretty. Then verify:

- [ ] `grep -rn "@ImportResource" web/src/main/java/` returns no results
- [ ] `grep -rn "@ImportResource" notification-sender/src/main/java/` returns no results
- [ ] `find . -name "*.xml" -path "*/notification-sender/src/main/resources/*"` returns nothing
- [ ] `find . -name "*.xml" -path "*/notification-sender/src/test/resources/*" | grep -v logback` returns nothing
- [ ] Application log shows `CamelContext webcertNotification started` and `CamelContext webcertCertificateSender started`
- [ ] No `NoSuchBeanDefinitionException` for `txTemplate`, `jms`, `processNotificationRequestRouteBuilder`, `certificateRouteBuilder`
- [ ] Sending a notification message to the notification JMS queue → message processed and WS call made
- [ ] Sending a certificate transfer message → processed by appropriate processor
- [ ] `NotificationRouteTest` passes — all 5 routes work correctly
- [ ] `RouteTest` passes — certificate route handles all message types
- [ ] `RouteIT` integration test passes — real ActiveMQ broker integration

---

## Risk Notes

**Multiple CamelContexts and RouteBuilder auto-discovery** — When two `SpringCamelContext`
beans are present, Camel's Spring integration may attempt to add ALL `RouteBuilder` beans
to BOTH contexts (depending on the Camel version). After 13.1, the RouteBuilders have no
`@Component`, so they must NOT be discovered via component scan. They are explicitly
instantiated as `@Bean` methods in the config classes. Verify after startup that each
context has only its own routes — check context route counts in startup logs.

**`objectMapper` bean collision** — `NotificationCamelConfig.java` defines an `objectMapper`
bean. If `AppConfig.java` also defines one (added in step 12.14), a `BeanDefinitionOverrideException`
or silent override will occur. Before creating the `NotificationCamelConfig` bean, run:
```bash
grep -rn "objectMapper\|ObjectMapper" web/src/main/java/ --include="*.java" | grep "@Bean"
```
If a conflict exists, use a unique qualifier: `@Bean("notificationObjectMapper")` and update
the injection site in `notificationMessageDataFormat`.

**`txTemplate` bean collision** — `JmsConfig.java` (web) does NOT define `txTemplate`.
`NotificationJmsConfig.java` defines it for Camel. Verify no conflict:
```bash
grep -rn "txTemplate" web/src/main/java/ --include="*.java" | grep "@Bean"
# Must return nothing — txTemplate is only in NotificationJmsConfig
```

**`SpringCamelContext` lifecycle and test `@DirtiesContext`** — Route tests use
`@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)`. This destroys and
recreates the Spring context for each test. `SpringCamelContext.destroy()` must properly
stop the Camel context. If the embedded broker (in integration tests) is also recreated,
a port 61618 conflict can occur. If this happens, use a random available port or
`@DirtiesContext` at class level only.

**`camel-spring` version compatibility** — `SpringCamelContext` API has changed across
Camel 3.x versions. Verify the Camel version in use:
```bash
grep "apache.camel" build.gradle  # check root or notification-sender build.gradle
```
If Camel 3.18+, `SpringCamelContext` is still available. If Camel 4.x was already adopted,
confirm API compatibility.

**Test property loading** — `unit-test.properties` and `integration-test.properties` are
currently loaded via `<context:property-placeholder>` in the XML. With `@TestPropertySource`,
the properties are loaded differently. Confirm property precedence is correct — `@TestPropertySource`
properties override all others, which is the desired behavior for test URIs overriding
production AMQ endpoints.
