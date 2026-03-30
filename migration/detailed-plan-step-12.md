# Step 12 — Convert XML Bean Configuration to Java

## Problem Statement

Webcert's Spring context is still bootstrapped from **`webcert-config.xml`** loaded by `web.xml`.
That root XML file imports 13 other XML files and defines ~30 beans inline. The CXF SOAP servlet
is configured via `services-cxf-servlet.xml`. The notification-sender module and its test suite
each carry their own XML config trees.

**Goal:** Replace every Spring XML configuration file (except the Camel files deferred to Step 13
and the SAML test fixture `test.xml`) with Java `@Configuration` classes. After this step, `web.xml`
loads a single `AnnotationConfigWebApplicationContext` rooted at `AppConfig.java`.

**Not in scope for this step:**
- `notifications/camel-context.xml`, `certificates/camel-context.xml` → Step 13
- `notifications/beans-context.xml`, `certificates/beans-context.xml` → Step 13
- `notifications/ws-context.xml`, `jms-context.xml`, `notification-sender-config.xml` → Step 13
- `test.xml` — is a SAML Response XML test fixture, **not** a Spring config. Retain as-is.

---

## Pre-Conditions (Steps 1–11 Must Be Complete)

The following must be true before starting Step 12. Verify each before beginning.

| Pre-condition | Verified by |
|---|---|
| All tests run on JUnit 5 (Step 1) | `grep -r "import org.junit\." --include="*.java" \| grep -v jupiter` returns nothing |
| `InternalApiFilter` exists as a local class (Step 8 — security inlining) | `find . -name "InternalApiFilter.java"` under `web/src/main/java` |
| `SoapFaultToSoapResponseTransformerInterceptor` exists as a local class (Step 8 — infra inlining) | `find . -name "SoapFaultToSoapResponseTransformerInterceptor.java"` |
| All JAX-RS controllers converted to `@RestController` (Step 11) | `grep -r "@Path" --include="*.java" web/src/main/java` returns nothing |
| `basic-cache-config.xml` import removed from `webcert-config.xml` (Step 10) | `grep "basic-cache-config" web/src/main/resources/webcert-config.xml` returns nothing |
| `xmldsig-config.xml` import removed from `webcert-config.xml` (Step 5) | `grep "xmldsig-config" web/src/main/resources/webcert-config.xml` returns nothing |
| All infra HSA/PU XML imports removed from `webcert-config.xml` (Step 9) | `grep "hsa-integration\|pu-integration" web/src/main/resources/webcert-config.xml` returns nothing |
| All infra SRS/IA XML imports removed from `webcert-config.xml` (Step 7/6) | `grep "srs-services-config\|ia-services-config" web/src/main/resources/webcert-config.xml` returns nothing |

> ⚠️ **Note on `AppConfig.java` current state:** At the time of writing, `AppConfig.java` has **no `@ComponentScan`
> annotation** — all component scanning is driven by `webcert-config.xml`. Every sub-step that says "add to
> the existing `@ComponentScan`" means **create a new one** if it does not yet exist. Similarly, `AppConfig.java`
> currently has `@Import({LoggingConfig.class, JmsConfig.class, CacheConfig.class, JobConfig.class})` — additional
> `@Import` entries must be appended, not replace this list.

---

## Current State

### XML Files That Need Action

| XML File | Location | What It Does | Action |
|---|---|---|---|
| `repository-context.xml` | `persistence/src/main/resources/` | `tx:annotation-driven` + `@ComponentScan` for `se.inera.intyg.webcert.persistence` | **Remove** — covered by `JpaConfig.java` |
| `webcert-common-config.xml` | `common/src/main/resources/` | `@ComponentScan` for `se.inera.intyg.webcert.common` | **Remove** — add `@ComponentScan` to `AppConfig.java` |
| `fmb-services-config.xml` | `integration/fmb-integration/src/main/resources/` | `@ComponentScan` for FMB services + `fmbConsumer` bean | **Remove** — Java config already exists |
| `servicenow-services-config.xml` | `integration/servicenow-integration/src/main/resources/` | `@ComponentScan` for ServiceNow config | **Remove** — `ServiceNowIntegrationConfig.java` exists |
| `integration-certificate-analytics-service-config.xml` | `integration-certificate-analytics-service/src/main/resources/` | Component scan | **Remove** — `CertificateAnalyticsServiceIntegrationConfig.java` exists |
| `integration-private-practitioner-service-config.xml` | `integration-private-practitioner-service/src/main/resources/` | Component scan | **Remove** — `PrivatePractitionerRestClientConfig.java` exists |
| `webcert-testability-api-context.xml` | `web/src/main/resources/` | JAX-RS `<jaxrs:server>` for 12 testability controllers | **Remove** — controllers are now `@RestController` (Step 11) |
| `mail-config.xml` | `web/src/main/resources/` | `JavaMailSenderImpl`, `TaskScheduler`, `TaskExecutor` | **Convert** → `MailConfig.java` |
| `web-servlet.xml` | `web/src/main/webapp/WEB-INF/` | `<mvc:annotation-driven/>` + component scan for controllers | **Merge** into `WebMvcConfiguration.java` |
| `notification-stub-context.xml` | `stubs/notification-stub/src/main/resources/` | JAXWS SOAP endpoint + JAXRS REST `/api/notification-api` | **Convert** → `NotificationStubConfig.java` + `@RestController` |
| `mail-stub-testability-api-context.xml` | `stubs/mail-stub/src/main/resources/` | JAXRS REST `/api/mail-api` | **Convert** → `MailStubConfig.java` + `@RestController` |
| `mail-stub-context.xml` | `stubs/mail-stub/src/main/resources/` | Mail stub beans | **Convert** → merge into `MailStubConfig.java` |
| `fmb-stub-context.xml` | `integration/fmb-integration/src/main/resources/` | JAXRS REST `/stubs/fmbstubs` | **Convert** → `FmbStubConfig.java` + `@RestController` |
| `servicenow-stub-context.xml` | `integration/servicenow-integration/src/main/resources/` | ServiceNow stub | **Verify** or create `ServiceNowStubConfig.java` |
| `ws-config.xml` | `web/src/main/resources/` | 12–15 outbound CXF JAX-WS clients | **Convert** → `CxfWsClientConfig.java` |
| `services-cxf-servlet.xml` | `web/src/main/webapp/WEB-INF/` | SOAP server endpoints (6) + stub imports + CXF bus | **Convert** → `CxfEndpointConfig.java` |
| `webcert-config.xml` | `web/src/main/resources/` | Root context — 13 XML imports + ~30 inline beans + component scans | **Collapse** into `AppConfig.java`, then delete |

### Notification-Sender Test XML (no runtime impact)

| XML File | Location | Used By | Action |
|---|---|---|---|
| `unit-test-notification-sender-config.xml` | `notification-sender/src/test/resources/notifications/` | `NotificationCamelTestConfig.java` via `@ImportResource` | **Replace** with Java `@Bean` methods |
| `unit-test-certificate-sender-config.xml` | `notification-sender/src/test/resources/certificates/` | `CertificateCamelTestConfig.java` via `@ImportResource` | **Replace** with Java config |
| `integration-test-certificate-sender-config.xml` | `notification-sender/src/test/resources/certificates/` | `CertificateCamelIntegrationTestConfig.java` via `@ImportResource` | **Replace** with Java config |
| `integration-test-broker-context.xml` | `notification-sender/src/test/resources/` | Imported by above | **Replace** with embedded broker `@Bean` |

### Existing Java @Configuration Classes (already in place)

| Class | Module | Purpose |
|---|---|---|
| `AppConfig.java` | `web.config` | Root config — `@Import`s JmsConfig, CacheConfig, JobConfig, LoggingConfig |
| `JmsConfig.java` | `web.config` | ActiveMQ connection factory, JMS templates, transaction manager |
| `CacheConfig.java` | `web.config` | Redis caching |
| `JobConfig.java` | `web.config` | Scheduled jobs (`@EnableAsync`, `@EnableScheduling`) |
| `LoggingConfig.java` | `web.config` | Prometheus metrics, MDC helper |
| `WebSecurityConfig.java` | `web.config` | SAML2 + Spring Security |
| `WebMvcConfiguration.java` | `web.config` | `WebMvcConfigurer` — Jackson ObjectMapper, parameter converters |
| `OpenSamlConfig.java` | `web.config` | OpenSAML initialization |
| `JpaConfig.java` | `persistence.config` | `@ComponentScan`, `@EnableJpaRepositories`, JPA setup |
| `ServiceNowIntegrationConfig.java` | `integration.servicenow.config` | ServiceNow beans |
| `ServiceNowStubConfig.java` | `integration.servicenow.stub.config` | ServiceNow stub |
| `CertificateAnalyticsServiceIntegrationConfig.java` | `integration.analytics.config` | Analytics integration |
| `PrivatePractitionerRestClientConfig.java` | `integration.privatepractitioner.config` | Private practitioner REST client |
| `SrsServicesConfiguration.java` | `infra.srs.config` | SRS services |
| `SrsStubConfiguration.java` | `infra.srs.stub.config` | SRS stub |
| `IAServicesConfiguration.java` | `infra.ia.config` | IA services |
| `IAStubConfiguration.java` | `infra.ia.stub.config` | IA stub |

### Inline Beans Remaining in `webcert-config.xml` (must move to Java)

| Bean ID | Class | Target Java Config |
|---|---|---|
| `propertyConfigurer` | `PropertySourcesPlaceholderConfigurer` | `AppConfig.java` (must be `static`) |
| `parserPool` | `net.shibboleth.utilities.java.support.xml.BasicParserPool` | `OpenSamlConfig.java` |
| `userAgentParser` | `UserAgentParser` | `LoggingConfig.java` |
| `securityConfigurationLoader` | `SecurityConfigurationLoader` | new `AuthoritiesConfig.java` |
| `commonAuthoritiesResolver` | `CommonAuthoritiesResolver` | `AuthoritiesConfig.java` |
| `authoritiesHelper` | `AuthoritiesHelper` | `AuthoritiesConfig.java` |
| `befattningService` | `BefattningService` (common) | `AppConfig.java` |
| `summaryConverter` | `SummaryConverter` (common) | `AppConfig.java` |
| `moduleRegistry` | `IntygModuleRegistryImpl` (origin=WEBCERT) | new `ModuleConfig.java` |
| `intygTextsService` | `IntygTextsServiceImpl` | `ModuleConfig.java` |
| `intygTextsRepository` | `IntygTextsRepositoryImpl` | `ModuleConfig.java` |
| `messageSource` | `ResourceBundleMessageSource` (ui, version) | `ModuleConfig.java` |
| `avtalService` | `AvtalServiceImpl` | Add `@Service` to class |
| `copyCompletionUtkastBuilder` | `CopyCompletionUtkastBuilder` | Add `@Component` to class |
| `createRenewalUtkastBuilder` | `CreateRenewalCopyUtkastBuilder` | Add `@Component` to class |
| `createReplacementUtkastBuilder` | `CreateReplacementUtkastBuilder` | Add `@Component` to class |
| `createUtkastFromTemplateBuilder` | `CreateUtkastFromTemplateBuilder` | Add `@Component` to class |
| `createUtkastCopyBuilder` | `CreateUtkastCopyBuilder` | Add `@Component` to class |
| `patientDetailsResolver` | `PatientDetailsResolverImpl` | Add `@Service` to class |
| `defaultCharacterEncodingFilter` | `DefaultCharacterEncodingFilter` | Already has `@Component(value = "defaultCharacterEncodingFilter")` — no change needed |
| `internalApiFilter` | `InternalApiFilter` | Must be inlined from `se.inera.intyg.infra.security.filter` in Step 8; add `@Component` there |
| `objectMapper` | `CustomObjectMapper` | **Create `@Bean` in `AppConfig.java`** — `WebMvcConfiguration.java` instantiates `new CustomObjectMapper()` inline but does NOT expose a named `@Bean`. Also update `WebMvcConfiguration.extendMessageConverters()` to inject the bean instead of newing it inline. |
| `jacksonJsonProvider` | `JacksonJsonProvider` | **Delete** after stubs migrated (12.11) |
| `taskExecutor` (GRP) | `ThreadPoolTaskExecutor` | Add to `GrpRestConfig.java` as `@Bean("grpTaskExecutor")` — **note:** `GrpRestConfig.java` currently only defines `grpRestClient`; this new bean must be added. Verify all GRP classes injecting `taskExecutor` by name are updated to use `grpTaskExecutor`. |
| `FragaSvarBootstrapBean` | `FragaSvarBootstrapBean` | `@Bean @Profile("dev,wc-init-data")` in `AppConfig.java` |
| `IntegreradeEnheterBootstrapBean` | `IntegreradeEnheterBootstrapBean` | `@Bean @Profile("dev,wc-init-data")` in `AppConfig.java` |
| `UtkastBootstrapBean` | `UtkastBootstrapBean` | `@Bean @Profile({"dev","wc-init-data","test","demo"})` in `AppConfig.java` |

---

## Migration Strategy

1. **Smallest wins first** — remove XML files that already have Java equivalents; zero code to write,
   only deletions
2. **Create new config classes** before removing the XML that drove them
3. **Stubs next** — the stub XML files are the last users of `jacksonJsonProvider`; once converted,
   `jacksonJsonProvider` and its transitive dependency can be deleted
4. **CXF after stubs** — convert outbound clients (`ws-config.xml`) then inbound server endpoints
   (`services-cxf-servlet.xml`)
5. **Test configs in parallel** — notification-sender test XML has zero runtime impact; can be done
   any time after 12.1
6. **Root XML last** — collapse `webcert-config.xml` into `AppConfig.java` once every import target is gone

### Wildcard Import Strategy

`webcert-config.xml` has two wildcard imports that load XML from external JARs on the classpath:
```xml
<import resource="classpath*:module-config.xml"/>
<import resource="classpath*:wc-module-cxf-servlet.xml"/>
```
These cannot be removed — they load configuration from external `se.inera.intyg.common` certificate
modules. Preserve them by adding `@ImportResource` annotations on `AppConfig.java` when the root XML
is removed:
```java
@ImportResource({"classpath*:module-config.xml", "classpath*:wc-module-cxf-servlet.xml"})
```
The `classpath:common-config.xml` import (from the intyg-common JAR) is handled the same way:
```java
// add to the @ImportResource array above
"classpath:common-config.xml"
```
Removing these wildcard imports entirely is deferred to Step 14 (coordinate with external modules).

### Child Context Strategy

Currently `web.xml` runs two child contexts:
- **`web` DispatcherServlet** → `web-servlet.xml` (MVC controllers)
- **`services` CXF servlet** → `services-cxf-servlet.xml` (SOAP endpoints)

After Step 12:
- `web` DispatcherServlet → `WebMvcConfiguration.java` (set `contextClass` to
  `AnnotationConfigWebApplicationContext`, `contextConfigLocation` to the class name)
- `services` CXF servlet → `CxfEndpointConfig.java` (same technique)
- Root context → `AppConfig.java` (replace `classpath:webcert-config.xml`)

---

## Progress Tracker

| Sub-step | Title | Risk | Status |
|---|---|---|---|
| **Phase A: Remove Redundant XML Files** | | | |
| 12.1 | Remove `repository-context.xml` | Low | ✅ DONE |
| 12.2 | Remove `webcert-common-config.xml` | Low | ✅ DONE |
| 12.3 | Remove integration module XML configs (4 files) | Low | ✅ DONE |
| 12.4 | Remove `webcert-testability-api-context.xml` | Low | ✅ DONE |
| **Phase B: Create Missing Java Configuration Classes** | | | |
| 12.5 | Create `MailConfig.java` replacing `mail-config.xml` | Low | ✅ DONE |
| 12.6 | Merge `web-servlet.xml` into `WebMvcConfiguration.java` | Medium | ✅ DONE |
| **Phase C: Convert Stub XML Contexts** | | | |
| 12.7 | Convert `notification-stub-context.xml` → `NotificationStubConfig.java` | Medium | ✅ DONE |
| 12.8 | Convert mail-stub XML contexts → `MailStubConfig.java` | Medium | ✅ |
| 12.9 | Convert `fmb-stub-context.xml` → `FmbStubConfig.java` | Medium | ✅ |
| 12.10 | Handle `servicenow-stub-context.xml` | Low | ✅ |
| 12.11 | Remove `jacksonJsonProvider` bean and JAX-RS JSON provider dependency | Low | ✅ |
| **Phase D: Convert CXF Configuration** | | | |
| 12.12 | Create `CxfWsClientConfig.java` replacing `ws-config.xml` | ⚠️ High | ✅ |
| 12.13 | Create `CxfEndpointConfig.java` replacing `services-cxf-servlet.xml` | ⚠️ High | ✅ |
| **Phase E: Remove Root XML** | | | |
| 12.14 | Collapse `webcert-config.xml` into `AppConfig.java` | ⚠️ Critical | ⬜ |
| **Phase F: Profile Cleanup** | | | |
| 12.15 | Simplify all multi-value `@Profile` annotations containing `"dev"` to `@Profile("dev")` | Low | ⬜ |

---

## Phase A: Remove Redundant XML Files

### Sub-step 12.1 — Remove `repository-context.xml`

**What:** Delete the `repository-context.xml` file and its import in `webcert-config.xml`.

**Why:** The file contains only `<tx:annotation-driven/>` and
`<context:component-scan base-package="se.inera.intyg.webcert.persistence"/>`. Both are
already handled:
- `@EnableTransactionManagement` is on `AppConfig.java`
- `@ComponentScan` for persistence is in `JpaConfig.java`

**Pre-check:**
```bash
grep -n "EnableTransactionManagement" web/src/main/java/se/inera/intyg/webcert/web/config/AppConfig.java
grep -n "ComponentScan" persistence/src/main/java/se/inera/intyg/webcert/persistence/config/JpaConfig.java
```
Both must return results before proceeding.

**Changes:**
1. Open `web/src/main/resources/webcert-config.xml`.
2. Delete the line: `<import resource="classpath:repository-context.xml"/>`.
3. Delete `persistence/src/main/resources/repository-context.xml`.

**Verification:** `./gradlew :persistence:test :web:test`

---

### Sub-step 12.2 — Remove `webcert-common-config.xml`

**What:** Add `@ComponentScan` for the `common` module to `AppConfig.java`, then remove the XML.

**Why:** `webcert-common-config.xml` contains only:
```xml
<context:component-scan base-package="se.inera.intyg.webcert.common"/>
```
`AppConfig.java` already scans `se.inera.intyg.webcert.web` broadly, but `se.inera.intyg.webcert.common`
is a different root package that must be explicitly listed.

**Changes:**
1. Open `web/src/main/java/se/inera/intyg/webcert/web/config/AppConfig.java`.
2. Add a `@ComponentScan("se.inera.intyg.webcert.common")` annotation to `AppConfig.java`.
   **Note:** `AppConfig.java` currently has **no `@ComponentScan` annotations** — this is the
   first one being added. Do not look for an existing one to amend.
3. Delete `<import resource="classpath:webcert-common-config.xml"/>` from `webcert-config.xml`.
4. Delete `common/src/main/resources/webcert-common-config.xml`.

**Verification:** `./gradlew :common:test :web:test`

---

### Sub-step 12.3 — Remove integration module XML configs (4 files)

**What:** Remove four XML files whose Java `@Configuration` equivalents are already active.

**Why:** Each XML only contains a `<context:component-scan>` that is redundant because the Java
`@Configuration` classes in those packages are discovered by AppConfig's broader component scan.
Verify this before deleting.

**Pre-check — confirm each Java config is component-scanned:**
```bash
# Check AppConfig's @ComponentScan covers these packages
grep -n "ComponentScan\|component.scan" \
  web/src/main/java/se/inera/intyg/webcert/web/config/AppConfig.java \
  web/src/main/resources/webcert-config.xml
```
Expected: `se.inera.intyg.webcert.web` (or broader) is present, which covers `web.*` but NOT
`integration.*` or `infra.*`. This means `ServiceNowIntegrationConfig.java` and others must be
either:
- In a package that IS component-scanned, or
- Explicitly `@Import`-ed into `AppConfig.java`

**Changes:**
1. For each of the 4 modules, confirm the Java config is reachable. If it isn't component-scanned,
   add an explicit `@Import` to `AppConfig.java`:
   - `@Import({ServiceNowIntegrationConfig.class, ServiceNowStubConfig.class})`
   - `@Import(CertificateAnalyticsServiceIntegrationConfig.class)`
   - `@Import(PrivatePractitionerRestClientConfig.class)`
   - For FMB: check if a `FmbServicesConfig.java` exists; if the XML still has the `fmbConsumer`
     bean definition AND no Java equivalent, create `FmbServicesConfig.java` first (see below).
2. Once all Java configs are confirmed reachable, remove these 4 import lines from `webcert-config.xml`:
   ```xml
   <import resource="classpath:fmb-services-config.xml"/>
   <import resource="classpath:servicenow-services-config.xml"/>
   <import resource="classpath:integration-certificate-analytics-service-config.xml"/>
   <import resource="classpath:integration-private-practitioner-service-config.xml"/>
   ```
3. Delete the 4 XML files.

**FMB edge case:** `fmb-services-config.xml` defines a `fmbConsumer` bean explicitly:
```xml
<bean id="fmbConsumer" class="se.inera.intyg.webcert.integration.fmb.consumer.FmbConsumerImpl">
  <constructor-arg name="baseUrl" value="${fmb.endpoint.url}"/>
</bean>
```
Note: the XML uses `constructor-arg name="baseUrl"` (not a property setter). `FmbConsumerImpl`
currently has **no `@Service` annotation** and takes `baseUrl` via constructor. To resolve:
- Option A: Add `@Service` to `FmbConsumerImpl` and annotate the constructor parameter with
  `@Value("${fmb.endpoint.url}")`.
- Option B: Create `FmbServicesConfig.java`:
  ```java
  @Configuration
  public class FmbServicesConfig {
      @Bean
      public FmbConsumerImpl fmbConsumer(@Value("${fmb.endpoint.url}") String baseUrl) {
          return new FmbConsumerImpl(baseUrl);
      }
  }
  ```
Do not delete `fmb-services-config.xml` until one of these is implemented and verified.

**Verification:** `./gradlew test` — FMB, ServiceNow, analytics, private-practitioner integrations
all start and resolve beans correctly.

---

### Sub-step 12.4 — Remove `webcert-testability-api-context.xml`

> ✅ **Already done:** `webcert-testability-api-context.xml` no longer exists in
> `web/src/main/resources/` (confirmed by filesystem search). The import of this file has also
> already been removed from `services-cxf-servlet.xml` (confirmed by reading the actual file).

**What:** Verify that both conditions are true before proceeding.

**Why:** Step 11 converted all 12 testability controllers to `@RestController` beans with
`@Profile({"dev", "testability-api"})`. They are now auto-discovered by the DispatcherServlet's
component scan. The XML registration is obsolete.

**Pre-check:**
```bash
# Confirm file is gone
find web/src -name "webcert-testability-api-context.xml"
# Must return nothing

# Confirm no import in services-cxf-servlet.xml
grep "testability-api-context" web/src/main/webapp/WEB-INF/services-cxf-servlet.xml
# Must return nothing
```

**Changes:** None required — already complete. If either pre-check fails, delete the file and/or
remove the import line as appropriate.

**Verification:** `./gradlew test`

---

## Phase B: Create Missing Java Configuration Classes

### Sub-step 12.5 — Create `MailConfig.java` replacing `mail-config.xml`

**What:** Replace `mail-config.xml` with a Java `@Configuration` class that defines the same beans.

**Why:** `mail-config.xml` currently defines:
- `<task:annotation-driven scheduler="scheduler" executor="threadPoolTaskExecutor"/>` — wires
  the scheduler and async executor; already replaced by `@EnableAsync`/`@EnableScheduling` in `JobConfig.java`
- `<task:scheduler id="scheduler" pool-size="1"/>`
- `<task:executor id="threadPoolTaskExecutor" pool-size="10" queue-capacity="100" rejection-policy="CALLER_RUNS"/>`
- `<bean id="mailSender" class="JavaMailSenderImpl">` — SMTP configuration

**⚠️ Bean name is fixed by the XML — do not rename:** The executor ID `threadPoolTaskExecutor`
is the name used by `@Async("threadPoolTaskExecutor")` in `MailNotificationServiceImpl`. The
`@Bean` name in `MailConfig.java` **must** match exactly.

**Current `mail-config.xml` properties (verify exact property names in `application.properties`):**
```xml
<bean id="mailSender" class="org.springframework.mail.javamail.JavaMailSenderImpl">
  <property name="host" value="${mail.host}"/>
  <property name="protocol" value="${mail.protocol}"/>
  <property name="username" value="${mail.username}"/>
  <property name="password" value="${mail.password}"/>
  <property name="defaultEncoding" value="${mail.defaultEncoding}"/>
  <property name="javaMailProperties">
    <props>
      <prop key="mail.smtps.auth">${mail.smtps.auth}</prop>
      <prop key="mail.smtps.starttls.enable">${mail.smtps.starttls.enable}</prop>
      <prop key="mail.smtps.debug">${mail.smtps.debug}</prop>
    </props>
  </property>
</bean>
```

> ⚠️ **Critical:** The task executor in `mail-config.xml` is named **`threadPoolTaskExecutor`**
> (not `mailTaskExecutor`). `MailNotificationServiceImpl` uses `@Async("threadPoolTaskExecutor")`
> — renaming the bean would silently break async mail sending. The `<task:annotation-driven
> scheduler="scheduler" executor="threadPoolTaskExecutor"/>` also sets these beans as the default
> scheduler/async-executor; `@EnableAsync` and `@EnableScheduling` are already in `JobConfig.java`
> so the `<task:annotation-driven>` line needs no Java equivalent.

**Changes:**
1. Create `web/src/main/java/se/inera/intyg/webcert/web/config/MailConfig.java`:
   ```java
   @Configuration
   public class MailConfig {

       @Value("${mail.host}") private String mailHost;
       @Value("${mail.protocol}") private String mailProtocol;
       @Value("${mail.username}") private String mailUsername;
       @Value("${mail.password}") private String mailPassword;
       @Value("${mail.defaultEncoding}") private String mailDefaultEncoding;
       @Value("${mail.smtps.auth}") private String smtpsAuth;
       @Value("${mail.smtps.starttls.enable}") private String startTls;
       @Value("${mail.smtps.debug}") private String smtpsDebug;

       @Bean(name = "scheduler")
       public ThreadPoolTaskScheduler mailTaskScheduler() {
           ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
           scheduler.setPoolSize(1);
           return scheduler;
       }

       // Bean name MUST match @Async("threadPoolTaskExecutor") in MailNotificationServiceImpl
       @Bean(name = "threadPoolTaskExecutor")
       public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
           ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
           executor.setCorePoolSize(10);
           executor.setQueueCapacity(100);
           executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
           executor.initialize();
           return executor;
       }

       @Bean
       public JavaMailSenderImpl mailSender() {
           JavaMailSenderImpl sender = new JavaMailSenderImpl();
           sender.setHost(mailHost);
           sender.setProtocol(mailProtocol);
           sender.setUsername(mailUsername);
           sender.setPassword(mailPassword);
           sender.setDefaultEncoding(mailDefaultEncoding);
           Properties props = new Properties();
           props.setProperty("mail.smtps.auth", smtpsAuth);
           props.setProperty("mail.smtps.starttls.enable", startTls);
           props.setProperty("mail.smtps.debug", smtpsDebug);
           sender.setJavaMailProperties(props);
           return sender;
       }
   }
   ```
2. Add `@Import(MailConfig.class)` to `AppConfig.java`.
3. `@EnableAsync` and `@EnableScheduling` are already in `JobConfig.java` — the
   `<task:annotation-driven>` line is already covered.
4. Remove `<import resource="mail-config.xml"/>` from `webcert-config.xml`.
5. Delete `web/src/main/resources/mail-config.xml`.

**Verification:** `./gradlew test` — mail-sending tests pass; no `NoSuchBeanDefinitionException`
for `mailSender` or `threadPoolTaskExecutor`.

---

### Sub-step 12.6 — Merge `web-servlet.xml` into `WebMvcConfiguration.java`

**What:** `web-servlet.xml` is the DispatcherServlet's child context config. It only has:
```xml
<mvc:annotation-driven/>
<context:annotation-config/>
<context:component-scan base-package="se.inera.intyg.webcert.web.web.controller"/>
```

After Step 11, `WebMvcConfiguration.java` already exists as a `WebMvcConfigurer`. Merge
`web-servlet.xml`'s responsibilities into it and update `web.xml` to use a Java config.

**⚠️ `web.xml` DispatcherServlet has no `contextConfigLocation`:** The current `web` servlet
definition has no `contextConfigLocation` init-param — Spring auto-discovers `WEB-INF/web-servlet.xml`
by convention (servlet name "web" → `WEB-INF/web-servlet.xml`). This step must explicitly add
`contextClass` and `contextConfigLocation` init-params to switch to Java config.

**⚠️ Child context consideration:** Currently `web-servlet.xml` is a *child* context of the root.
After this change, `WebMvcConfiguration.java` becomes the DispatcherServlet's dedicated config.
Beans defined here override root-context beans of the same type in the DispatcherServlet's scope.
No duplicate bean errors are expected since the controller scan is already present in the root
context's broad `se.inera.intyg.webcert.web` scan — but verify there are no `BeanDefinitionOverrideException`
errors at startup.

**Changes:**
1. Open `web/src/main/java/se/inera/intyg/webcert/web/config/WebMvcConfiguration.java`.
2. Add `@EnableWebMvc` if it is not already present (needed to activate Spring MVC in a
   non-Spring-Boot, XML-based app when switching away from `<mvc:annotation-driven/>`).
3. Add the component scan for the controller package if not covered by a parent context import:
   ```java
   @ComponentScan("se.inera.intyg.webcert.web.web.controller")
   ```
   This is technically redundant (the root context scans `se.inera.intyg.webcert.web`) but makes
   the DispatcherServlet context self-describing and explicit.
4. Update `web.xml` — change the `web` servlet definition to use Java config:
   ```xml
   <servlet>
     <servlet-name>web</servlet-name>
     <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
     <init-param>
       <param-name>contextClass</param-name>
       <param-value>org.springframework.web.context.support.AnnotationConfigWebApplicationContext</param-value>
     </init-param>
     <init-param>
       <param-name>contextConfigLocation</param-name>
       <param-value>se.inera.intyg.webcert.web.config.WebMvcConfiguration</param-value>
     </init-param>
     <load-on-startup>1</load-on-startup>
   </servlet>
   ```
5. Delete `web/src/main/webapp/WEB-INF/web-servlet.xml`.

**Verification:** `./gradlew test` — all web layer tests pass; no 404s on existing routes.

---

## Phase C: Convert Stub XML Contexts

### Sub-step 12.7 — Convert `notification-stub-context.xml` → `NotificationStubConfig.java`

**What:** `notification-stub-context.xml` (profile: `dev,wc-all-stubs,wc-notificationsender-stub,testability-api`)
registers two things:
1. A **JAXWS SOAP endpoint** at `/stubs/clinicalprocess/.../CertificateStatusUpdateForCare/3/rivtabp21`
   — this stays as a CXF endpoint
2. A **JAXRS REST server** at `/api/notification-api` — this must become a Spring MVC `@RestController`

**Changes:**
1. The XML has **three separate `<beans profile="...">` blocks with distinct profiles** — a single
   `@Configuration` class cannot model this. Use **two separate classes** plus a `@RestController`:

   **Class 1 — data beans:**
   ```java
   @Configuration
   @Profile("dev")
   public class NotificationStubDataConfig {

       @Bean
       public NotificationStoreV3Impl notificationStoreV3() {
           return new NotificationStoreV3Impl();
       }

       @Bean
       public NotificationStubStateBean notificationStubStateBean() {
           return new NotificationStubStateBean();
       }
   }
   ```

   **Class 2 — JAXWS SOAP endpoint:**
   ```java
   @Configuration
   @Profile("dev")
   public class NotificationStubConfig {

       // CXF JAXWS server endpoint — replicates the jaxws:schemaLocations from XML
       @Bean
       public Endpoint notificationStubSoapEndpoint(
               CertificateStatusUpdateForCareResponderStub impl, Bus bus) {
           EndpointImpl endpoint = new EndpointImpl(bus, impl);
           endpoint.setSchemaLocations(List.of(
               "classpath:/core_components/clinicalprocess_healthcond_certificate_3.3.xsd",
               "classpath:/core_components/clinicalprocess_healthcond_certificate_3.2_ext.xsd",
               "classpath:/core_components/clinicalprocess_healthcond_certificate_types_3.2.xsd",
               "classpath:/core_components/xmldsig-core-schema_0.1.xsd",
               "classpath:/core_components/xmldsig-filter2.xsd",
               "classpath:/interactions/CertificateStatusUpdateForCareInteraction/CertificateStatusUpdateForCareResponder_3.1.xsd"
           ));
           endpoint.publish(
               "/clinicalprocess/healthcond/certificate/CertificateStatusUpdateForCare/3/rivtabp21");
           return endpoint;
       }
   }
   ```

   **REST controller** (`NotificationStubRestApi`):
   Add `@RestController` and `@Profile("dev")` to the class.
   Map all methods from the JAX-RS annotations to Spring MVC equivalents.

2. Remove the import of `notification-stub-context.xml` from `services-cxf-servlet.xml`.
3. Delete `stubs/notification-stub/src/main/resources/notification-stub-context.xml`.

**Verification:** Start with profile `dev`. Hit the notification stub REST API at
`/api/notification-api/...`. Verify the SOAP stub endpoint still responds at its WSDL URL.
Start with profile `testability-api` alone — confirm REST API works but SOAP endpoint is NOT created.

---

### Sub-step 12.8 — Convert mail-stub XML contexts → Java config

**What:** Two mail-stub XML files with **different profiles**:
- `mail-stub-context.xml` (profile: `dev,wc-all-stubs,wc-mail-stub`) — component-scans
  `se.inera.intyg.webcert.mailstub` and defines one explicit bean: `mailAdvice`
  (`JavaMailSenderAroundAdvice`) with `mailHost` property. This is an AOP around-advice that
  intercepts `JavaMailSender` calls in the dev/stub profile.
- `mail-stub-testability-api-context.xml` (profile: `dev,testability-api`) — registers
  `MailStore` (anonymous bean), `MailStubRestApi`, and was the JAXRS server at `/api/mail-api`.

> ⚠️ **These have different profiles and must NOT be merged into one `@Configuration` class
> with a single `@Profile`.** `mail-stub-context.xml` activates on `wc-mail-stub` (no testability);
> `mail-stub-testability-api-context.xml` activates on `testability-api` (no wc-mail-stub).

**Changes:**
1. Create `stubs/mail-stub/src/main/java/.../config/MailStubConfig.java`
   (replaces `mail-stub-context.xml`):
   ```java
   @Configuration
   @Profile("dev")
   public class MailStubConfig {

       @Value("${mail.host}") private String mailHost;

       @Bean
       public JavaMailSenderAroundAdvice mailAdvice() {
           JavaMailSenderAroundAdvice advice = new JavaMailSenderAroundAdvice();
           advice.setMailHost(mailHost);
           return advice;
       }
   }
   ```
   The `<context:component-scan base-package="se.inera.intyg.webcert.mailstub"/>` in the XML
   means other beans in that package are auto-discovered by existing component scans. Verify
   whether any `@Component` classes in `se.inera.intyg.webcert.mailstub` exist that need to
   remain discoverable when `dev` is active.

2. Create `stubs/mail-stub/src/main/java/.../config/MailStubTestabilityConfig.java`
   (replaces `mail-stub-testability-api-context.xml`):
   ```java
   @Configuration
   @Profile("dev")
   public class MailStubTestabilityConfig {

       @Bean
       public MailStore mailStore() {
           return new MailStore();
       }

       @Bean
       public MailStubRestApi mailStubRestApi() {
           return new MailStubRestApi();
       }
   }
   ```
   Also add `@RestController` and `@Profile("dev")` to `MailStubRestApi`
   itself, and map all JAX-RS methods to Spring MVC equivalents.

3. Remove `<import resource="classpath:mail-stub-context.xml"/>` from `webcert-config.xml`.
4. Remove the import of both mail-stub XMLs from `services-cxf-servlet.xml`.
5. Delete `mail-stub-context.xml` and `mail-stub-testability-api-context.xml`.

**Verification:** Start with `dev` profile. Hit the mail stub API at `/api/mail-api/...`.
Confirm `mailAdvice` bean is active (mail is intercepted).

---

### Sub-step 12.9 — Convert `fmb-stub-context.xml` → `FmbStubConfig.java`

**What:** `fmb-stub-context.xml` (profile: `dev,wc-all-stubs,wc-fmb-stub`) registers a JAXRS
server at `/stubs/fmbstubs` serving `FmbStub`.

**Changes:**
1. Create `integration/fmb-integration/src/main/java/.../stub/config/FmbStubConfig.java`:
   ```java
   @Configuration
   @Profile({"dev", "wc-all-stubs", "wc-fmb-stub"})
   public class FmbStubConfig {
       @Bean
       public FmbStub fmbStub() {
           return new FmbStub();
       }
   }
   ```
2. Convert `FmbStub` to a Spring MVC `@RestController` with the same
   `@Profile({"dev", "wc-all-stubs", "wc-fmb-stub"})` and `@RequestMapping("/stubs/fmbstubs")`.
3. Remove the import of `fmb-stub-context.xml` from `services-cxf-servlet.xml`.
4. Delete `integration/fmb-integration/src/main/resources/fmb-stub-context.xml`.

**Verification:** Start with profile `dev` or `wc-fmb-stub`. Hit `/stubs/fmbstubs/...`.

---

### Sub-step 12.10 — Handle `servicenow-stub-context.xml`

**What:** Verify whether `ServiceNowStubConfig.java` and `ServiceNowStubBeanConfig.java` (both
already exist per current state analysis) already cover everything in `servicenow-stub-context.xml`.

**Changes:**
1. Read `servicenow-stub-context.xml` and compare with the two existing Java stub config classes.
2. If the Java configs already cover all beans and no JAX-RS endpoint registration exists in the
   XML (ServiceNow uses REST, not JAXRS CXF), simply delete the XML and remove its import from
   `services-cxf-servlet.xml`.
3. If any bean is missing from the Java configs, add it before deleting the XML.
4. If `servicenow-stub-context.xml` has a `<jaxrs:server>`, convert its REST controller to a
   Spring MVC `@RestController` first.

**Verification:** Start with profile `dev`. ServiceNow stub endpoints respond.

---

### Sub-step 12.11 — Remove `jacksonJsonProvider` bean and JAX-RS JSON provider dependency

**What:** After sub-steps 12.7–12.10, no XML file references `jacksonJsonProvider` by name.
Remove the bean definition and its transitive dependency.

**Why:** `jacksonJsonProvider` (`JacksonJsonProvider`) was a CXF JAX-RS provider. With all JAX-RS
REST servers removed (Step 11) and all stub XML converted (12.7–12.10), it is no longer needed.

**Pre-check:** Confirm no remaining references:
```bash
grep -r "jacksonJsonProvider" --include="*.xml" --include="*.java" \
  web/src/main/webapp/ web/src/main/resources/ \
  stubs/ integration/fmb-integration/src/
# Must return no results
```

**Changes:**
1. Remove from `webcert-config.xml`:
   ```xml
   <bean id="jacksonJsonProvider" class="com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider">
     <property name="mapper">
       <bean class="se.inera.intyg.common.util.integration.json.CustomObjectMapper"/>
     </property>
   </bean>
   ```
2. Remove from `web/build.gradle`:
   ```gradle
   implementation("com.fasterxml.jackson.jakarta.rs:jackson-jakarta-rs-json-provider")
   ```
3. Run `./gradlew :web:dependencies | grep jackson-jakarta-rs` to confirm it's gone from the
   classpath (it should not be pulled in transitively).

**Verification:** `./gradlew test` — no `ClassNotFoundException` for `JacksonJsonProvider`.

---

## Phase D: Convert CXF Configuration

### Sub-step 12.12 — Create `CxfWsClientConfig.java` replacing `ws-config.xml` ⚠️ High Risk

**What:** Convert 12–15 outbound `<jaxws:client>` beans in `ws-config.xml` to programmatic
`JaxWsProxyFactoryBean` definitions.

**Why:** `ws-config.xml` uses CXF XML namespace (`<jaxws:client>`) which requires loading the
file as XML. Moving to `JaxWsProxyFactoryBean` (Spring-CXF programmatic API) preserves the same
runtime behaviour without XML.

**Pattern for each client:**
```java
// Example for sendQuestionToFKClient
@Bean
public SendMedicalCertificateQuestionResponderInterface sendQuestionToFKClient(
    @Value("${sendquestiontofk.endpoint.url}") String address) {
    JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
    factory.setServiceClass(SendMedicalCertificateQuestionResponderInterface.class);
    factory.setAddress(address);
    factory.getFeatures().add(new LoggingFeature());
    return (SendMedicalCertificateQuestionResponderInterface) factory.create();
}
```

**Clients to convert** (verified against the actual `ws-config.xml`):

| Bean ID | Service Interface | URL Property |
|---|---|---|
| `sendQuestionToFKClient` | `SendMedicalCertificateQuestionResponderInterface` | `sendquestiontofk.endpoint.url` |
| `sendAnswerToFKClient` | `SendMedicalCertificateAnswerResponderInterface` | `sendanswertofk.endpoint.url` |
| `listCertificatesForCareResponderV3` | `ListCertificatesForCareResponderInterface` (v3) | `intygstjanst.listcertificatesforcare.v3.endpoint.url` |
| `sendCertificateClient` | `SendCertificateToRecipientResponderInterface` | `intygstjanst.sendcertificate.endpoint.url` |
| `revokeCertificateClient` | `RevokeMedicalCertificateResponderInterface` | `intygstjanst.revokecertificate.endpoint.url` |
| `revokeCertificateClientRivta` | `RevokeCertificateResponderInterface` (v2) | `intygstjanst.revokecertificaterivta.endpoint.url` |
| `sendMessageToRecipientClient` | `SendMessageToRecipientResponderInterface` | `intygstjanst.sendmessagetorecipient.endpoint.url` |
| `registerCertificateClient` | `RegisterCertificateResponderInterface` (v3) | `intygstjanst.registercertificate.v3.endpoint.url` |
| `getCertificateClient` | `GetCertificateResponderInterface` (v2) | `intygstjanst.getcertificate.endpoint.url` |
| `ListActiveSickLeavesForCareUnitClient` | `ListActiveSickLeavesForCareUnitResponderInterface` | `intygstjanst.listactivesickleavesforcareunit.v1.endpoint.url` |
| `getCertificateTypeInfoClient` | `GetCertificateTypeInfoResponderInterface` | `intygstjanst.getcertificatetypeinfo.endpoint.url` |
| `listRelationsForCertificateClient` | `ListRelationsForCertificateResponderInterface` | `intygstjanst.listrelationsforcertificate.endpoint.url` |
| `listApprovedReceiversClient` | `ListApprovedReceiversResponderInterface` | `intygstjanst.listapprovedreceivers.endpoint.url` |
| `listPossibleReceiversClient` | `ListPossibleReceiversResponderInterface` | `intygstjanst.listpossiblereceivers.endpoint.url` |
| `registerApprovedReceiversClient` | `RegisterApprovedReceiversResponderInterface` | `intygstjanst.registerapprovedreceivers.endpoint.url` |

> ⚠️ Some clients in `ws-config.xml` have additional child elements (schema validation, handlers).
> Read the full `ws-config.xml` for each client before implementing — clients using `<jaxws:handlers>`
> need them added to the `JaxWsProxyFactoryBean` handlers list.

**TLS configuration (profile `!prod` → `!dev` in dev, `!prod` in others):** `ws-config.xml` has
`<http:conduit>` settings for TLS. Replicate programmatically:
```java
@Bean
@Profile("!dev")
public TLSClientParameters tlsConfig() { ... }

// In each factory bean, when TLS is active:
// factory.getConduitSelector() ... or use CXF Bus TLS configuration
```
The exact TLS approach depends on the existing `http:conduit` settings in `ws-config.xml`
(key store, trust store paths). Read the XML carefully before implementing.

**Changes:**
1. Create `web/src/main/java/se/inera/intyg/webcert/web/config/CxfWsClientConfig.java`.
2. Define one `@Bean` method per client (see pattern above).
3. Handle TLS configuration with a `@Bean @Profile("!dev")` approach.
4. Add `@Import(CxfWsClientConfig.class)` to `AppConfig.java`.
5. Remove `<import resource="ws-config.xml"/>` from `webcert-config.xml`.
6. Delete `web/src/main/resources/ws-config.xml`.

**Verification:** `./gradlew test` — all tests using CXF client mocks pass.
In a dev environment with network access, confirm outbound WS calls succeed.

---

### Sub-step 12.13 — Create `CxfEndpointConfig.java` replacing `services-cxf-servlet.xml` ⚠️ High Risk

**What:** `services-cxf-servlet.xml` is the CXF servlet's context config. It defines:
1. CXF bus with logging feature
2. Six JAXWS server endpoints (`<jaxws:endpoint>`) for incoming SOAP requests
3. Stub imports (already removed in 12.7–12.10)
4. Component scans for SRS and IA stub packages (already covered by Java configs)

**Why:** Moving to a Java config eliminates the last XML-loaded child Spring context in the
`services` CXF servlet.

**SOAP Endpoints to convert:**

| Address | Implementor | Notes |
|---|---|---|
| `/create-draft-certificate/v3.0` | `CreateDraftCertificateResponderImpl` | Schema locations (7) + `SoapFaultToSoapResponseTransformerInterceptor` |
| `/receive-question/v1.0` | `ReceiveQuestionResponderImpl` | Fault interceptor only |
| `/receive-answer/v1.0` | `ReceiveAnswerResponderImpl` | Fault interceptor only |
| `/send-message-to-care/v2.0` | `SendMessageToCareResponderImpl` | Schema locations (7) + fault interceptor |
| `/list-certificates-for-care-with-qa/v3.0` | `ListCertificatesForCareWithQAResponderImpl` | Schema locations (7) |
| `/get-certificate-additions/v1.1` | `GetCertificateAdditionsResponderImpl` | Schema locations (6) |

**Pattern for each endpoint with schema validation:**
```java
@Bean
public Endpoint createDraftCertificateEndpoint(
        CreateDraftCertificateResponderImpl implementor, Bus bus) {
    EndpointImpl endpoint = new EndpointImpl(bus, implementor);
    endpoint.setSchemaLocations(List.of(
        "classpath:/core_components/clinicalprocess_healthcond_certificate_3.3.xsd",
        // ... remaining schema locations
    ));
    endpoint.getOutFaultInterceptors().add(
        new SoapFaultToSoapResponseTransformerInterceptor(
            "transform/clinicalprocess-healthcond-3/create-draft-certificate.xslt"));
    endpoint.publish("/create-draft-certificate/v3.0");
    return endpoint;
}
```

**CXF Bus configuration:**
```java
@Autowired
void configureBus(Bus bus) {
    bus.getFeatures().add(new LoggingFeature());
}
```

**Changes:**
1. Create `web/src/main/java/se/inera/intyg/webcert/web/config/CxfEndpointConfig.java`.
2. Add CXF bus logging configuration.
3. Define one `@Bean Endpoint` per SOAP service (6 beans).
4. Update `web.xml` — change the `services` CXF servlet to use Java config:
   ```xml
   <servlet>
     <servlet-name>services</servlet-name>
     <servlet-class>org.apache.cxf.transport.servlet.CXFServlet</servlet-class>
     <init-param>
       <param-name>contextClass</param-name>
       <param-value>org.springframework.web.context.support.AnnotationConfigWebApplicationContext</param-value>
     </init-param>
     <init-param>
       <param-name>contextConfigLocation</param-name>
       <param-value>se.inera.intyg.webcert.web.config.CxfEndpointConfig</param-value>
     </init-param>
     <load-on-startup>1</load-on-startup>
   </servlet>
   ```
5. Delete `web/src/main/webapp/WEB-INF/services-cxf-servlet.xml`.

**Verification:** `./gradlew test`
Run application and verify each SOAP WSDL is accessible:
- `GET /services/create-draft-certificate/v3.0?wsdl` → returns WSDL
- `GET /services/receive-question/v1.0?wsdl` → returns WSDL
- `GET /services/receive-answer/v1.0?wsdl` → returns WSDL
- `GET /services/send-message-to-care/v2.0?wsdl` → returns WSDL
- `GET /services/list-certificates-for-care-with-qa/v3.0?wsdl` → returns WSDL
- `GET /services/get-certificate-additions/v1.1?wsdl` → returns WSDL

---

## Phase E: Remove Root XML

### Sub-step 12.14 — Collapse `webcert-config.xml` into `AppConfig.java` ⚠️ Critical

**What:** Move all remaining content of `webcert-config.xml` to Java, then update `web.xml` to
point directly to `AppConfig.java` instead of `webcert-config.xml`. Delete the XML file.

#### 12.14a — Audit remaining content

At this point, `webcert-config.xml` should contain only:
- `<context:annotation-config/>`
- `<context:component-scan>` entries (8 packages)
- `<import resource="classpath*:module-config.xml"/>` (wildcard — external JARs)
- `<import resource="classpath*:wc-module-cxf-servlet.xml"/>` (wildcard — external JARs)
- `<import resource="classpath:common-config.xml"/>` (intyg-common JAR)
- `<import resource="classpath:notification-sender-config.xml"/>` (deferred to Step 13)
- ~30 inline bean definitions (see table in Current State section)

#### 12.14b — Move inline beans to Java configs

Work through the beans in the Current State table. For each:

**`PropertySourcesPlaceholderConfigurer` → `AppConfig.java`:**
```java
@Bean
public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
    PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
    configurer.setIgnoreUnresolvablePlaceholders(true);
    configurer.setIgnoreResourceNotFound(true);
    configurer.setLocations(
        new ClassPathResource("application.properties"),
        new ClassPathResource("version.properties"),
        new ClassPathResource("webcert-notification-route-params.properties")
    );
    return configurer;
}
```
⚠️ This bean **must be `static`** — `PropertySourcesPlaceholderConfigurer` is a
`BeanFactoryPostProcessor` and Spring cannot apply it to the class it is defined in unless it is
static.

The `file:${dev.config.file:-}` override location from the XML can be added using
`@PropertySource(value = "file:${dev.config.file:-}", ignoreResourceNotFound = true)` on `AppConfig.java`.

**`parserPool` → `OpenSamlConfig.java`:**

> ⚠️ **Current state:** `OpenSamlConfig.java` is already a `@Component` that creates and initializes
> its own `BasicParserPool` internally (in a private `getParserPool()` method called from
> `afterPropertiesSet()`). The XML `parserPool` bean may be injected into SAML-related beans
> by name. First verify what consumes the `parserPool` bean:
> ```bash
> grep -rn "parserPool\|ParserPool" web/src/main/java/ --include="*.java" | grep -v "OpenSamlConfig"
> ```
> If nothing else references `parserPool` by name, the XML bean is already replaced by
> `OpenSamlConfig`'s internal pool — skip creating a `@Bean` for it.
> If something does inject `parserPool` by name, refactor `OpenSamlConfig.java` to expose it as a
> `@Bean` and reference it internally:
> ```java
> @Bean(initMethod = "initialize")
> public BasicParserPool parserPool() {
>     BasicParserPool pool = new BasicParserPool();
>     pool.setMaxPoolSize(100);
>     pool.setCoalescing(true);
>     pool.setIgnoreComments(true);
>     pool.setIgnoreElementContentWhitespace(true);
>     pool.setNamespaceAware(true);
>     pool.setExpandEntityReferences(false);
>     pool.setXincludeAware(false);
>     pool.setBuilderFeatures(getOpenSamlBuilderFeatures());
>     pool.setBuilderAttributes(new HashMap<>());
>     return pool;
> }
> ```
> Apply all security settings from `OpenSamlConfig.getParserPool()` — do not use a bare
> `new BasicParserPool()` as that omits the XXE protections.

**`userAgentParser` → `LoggingConfig.java`:**
```java
@Bean
public UserAgentParser userAgentParser() {
    return new UserAgentParser();
}
```

**`securityConfigurationLoader`, `commonAuthoritiesResolver`, `authoritiesHelper` → new `AuthoritiesConfig.java`:**
```java
@Configuration
public class AuthoritiesConfig {

    @Value("${authorities.configuration.file}")
    private String authoritiesFile;

    @Value("${features.configuration.file}")
    private String featuresFile;

    @Value("${max.aliases.for.collections:300}")
    private int maxAliases;

    @Bean
    public SecurityConfigurationLoader securityConfigurationLoader() {
        return new SecurityConfigurationLoader(authoritiesFile, featuresFile, maxAliases);
    }

    @Bean
    public CommonAuthoritiesResolver commonAuthoritiesResolver() {
        // CommonAuthoritiesResolver has @Autowired SecurityConfigurationLoader — Spring
        // will inject via field injection after construction since it is annotated @Service.
        // If it has been inlined without @Service, use constructor injection here instead:
        //   return new CommonAuthoritiesResolver(securityConfigurationLoader());
        return new CommonAuthoritiesResolver();
    }

    @Bean
    public AuthoritiesHelper authoritiesHelper() {
        // AuthoritiesHelper requires CommonAuthoritiesResolver via @Autowired constructor.
        // Call the @Bean method directly — Spring's CGLIB proxy ensures the singleton:
        return new AuthoritiesHelper(commonAuthoritiesResolver());
    }
}
```
Add `@Import(AuthoritiesConfig.class)` to `AppConfig.java`.

> ⚠️ `CommonAuthoritiesResolver` has an `@Autowired SecurityConfigurationLoader` field, and
> `AuthoritiesHelper` has an `@Autowired CommonAuthoritiesResolver` constructor argument.
> If these classes are inlined without `@Service`/`@Component`, the constructor call pattern
> above handles injection explicitly. If they retain `@Service`/`@Component`, let Spring
> discover and wire them instead of defining explicit `@Bean` methods here.

**`befattningService`, `summaryConverter` → `AppConfig.java`:**
```java
@Bean
public BefattningService befattningService() {
    return new BefattningService();
}

@Bean
public SummaryConverter summaryConverter() {
    return new SummaryConverter();
}
```

**`objectMapper` → `AppConfig.java`:**

> ⚠️ `WebMvcConfiguration.java` does **NOT** define `objectMapper` as a `@Bean` — it calls
> `new CustomObjectMapper()` inline inside `extendMessageConverters()`. The named `objectMapper`
> bean from `webcert-config.xml` must be explicitly recreated in Java.

```java
@Bean
public ObjectMapper objectMapper() {
    return new CustomObjectMapper();
}
```

Also update `WebMvcConfiguration.extendMessageConverters()` to inject this bean rather than
instantiating inline:
```java
@Autowired private ObjectMapper objectMapper;

@Override
public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.removeIf(c -> c instanceof MappingJackson2HttpMessageConverter);
    converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
}
```

**`moduleRegistry`, `intygTextsService`, `intygTextsRepository`, `messageSource` → new `ModuleConfig.java`:**
```java
@Configuration
public class ModuleConfig {

    @Bean
    public IntygModuleRegistry moduleRegistry() {
        IntygModuleRegistryImpl registry = new IntygModuleRegistryImpl();
        registry.setOrigin("WEBCERT");
        return registry;
    }

    @Bean
    public IntygTextsService intygTextsService() {
        return new IntygTextsServiceImpl();
    }

    @Bean
    public IntygTextsRepository intygTextsRepository() {
        return new IntygTextsRepositoryImpl();
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setDefaultEncoding("UTF-8");
        source.setBasenames("ui", "version");
        return source;
    }
}
```
Add `@Import(ModuleConfig.class)` to `AppConfig.java`.

**Service/component beans (avtalService, builders, patientDetailsResolver, filters):**

> ⚠️ `DefaultCharacterEncodingFilter` **already has** `@Component(value = "defaultCharacterEncodingFilter")` — no action needed.
> `InternalApiFilter` does NOT exist locally yet; it must be inlined from the infra `security-filter` module in Step 8. Confirm it exists before this sub-step.

Check each remaining class for existing `@Service` or `@Component` annotation:
```bash
grep -n "@Service\|@Component" \
  web/src/main/java/se/inera/intyg/webcert/web/service/privatlakaravtal/AvtalServiceImpl.java \
  web/src/main/java/se/inera/intyg/webcert/web/service/utkast/CopyCompletionUtkastBuilder.java \
  web/src/main/java/se/inera/intyg/webcert/web/service/patient/PatientDetailsResolverImpl.java
```
If any class lacks the annotation, add it. Do not create `@Bean` methods for these — annotating
the class directly is cleaner and consistent with the rest of the codebase.

**`taskExecutor` for GRP → `GrpRestConfig.java`:**

> ⚠️ `GrpRestConfig.java` currently defines only a `grpRestClient` bean — the `grpTaskExecutor`
> bean does not exist yet and must be added to this class.

```java
@Bean(name = "grpTaskExecutor")
public ThreadPoolTaskExecutor grpTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(25);
    executor.initialize();
    return executor;
}
```
Find all injections of the old `taskExecutor` bean in GRP-related classes and confirm they are
updated to use `@Qualifier("grpTaskExecutor")` (the XML bean was named `taskExecutor`; that name
is taken by `threadPoolTaskExecutor` transitively — using it would create a clash).

**Profile-specific bootstrap beans → `AppConfig.java`:**
```java
@Bean
@Profile({"dev", "wc-init-data"})
public FragaSvarBootstrapBean fragaSvarBootstrapBean() {
    return new FragaSvarBootstrapBean();
}

@Bean
@Profile({"dev", "wc-init-data"})
public IntegreradeEnheterBootstrapBean integreradeEnheterBootstrapBean() {
    return new IntegreradeEnheterBootstrapBean();
}

@Bean
@Profile({"dev", "wc-init-data", "test", "demo"})
public UtkastBootstrapBean utkastBootstrapBean() {
    return new UtkastBootstrapBean();
}
```

#### 12.14c — Migrate component scans to `AppConfig.java`

`webcert-config.xml` component-scans 8 infra/integration packages in addition to
`se.inera.intyg.webcert.web`. Replicate these in `AppConfig.java`:

```java
@ComponentScans({
    @ComponentScan("se.inera.intyg.webcert.web"),
    @ComponentScan("se.inera.intyg.webcert.common"),          // from 12.2
    @ComponentScan("se.inera.intyg.webcert.infra.xmldsig.config"),
    @ComponentScan("se.inera.intyg.webcert.infra.dynamiclink"),
    @ComponentScan("se.inera.intyg.webcert.infra.postnummer"),
    @ComponentScan("se.inera.intyg.webcert.infra.sjukfall.services"),
    @ComponentScan("se.inera.intyg.webcert.infra.integration.intygproxyservice"),
    @ComponentScan("se.inera.intyg.webcert.infra.pu.integration.intygproxyservice"),
    @ComponentScan("se.inera.intyg.webcert.infra.ia.config"),
    @ComponentScan("se.inera.intyg.webcert.infra.ia.cache"),
    @ComponentScan("se.inera.intyg.webcert.infra.srs.config")
})
```

#### 12.14d — Add `@ImportResource` for wildcard and deferred imports

```java
@ImportResource({
    "classpath*:module-config.xml",          // external certificate module configs
    "classpath*:wc-module-cxf-servlet.xml",  // external module CXF configs
    "classpath:common-config.xml",            // intyg-common JAR config
    "classpath:notification-sender-config.xml" // deferred to Step 13
})
```

#### 12.14e — Update `web.xml` root context configuration

Change the root context loading from XML to Java:
```xml
<!-- Replace this: -->
<context-param>
  <param-name>contextConfigLocation</param-name>
  <param-value>classpath:webcert-config.xml</param-value>
</context-param>

<!-- With this: -->
<context-param>
  <param-name>contextClass</param-name>
  <param-value>org.springframework.web.context.support.AnnotationConfigWebApplicationContext</param-value>
</context-param>
<context-param>
  <param-name>contextConfigLocation</param-name>
  <param-value>se.inera.intyg.webcert.web.config.AppConfig</param-value>
</context-param>
```

#### 12.14f — Delete `webcert-config.xml`

After verifying the application starts (see verification below):
```bash
rm web/src/main/resources/webcert-config.xml
```

**Full verification for 12.14:**
- `./gradlew test` — all tests pass
- `./gradlew appRun` (Gretty) — application starts without `BeanCreationException` or
  `NoSuchBeanDefinitionException`
- Startup log contains all expected bean names
- Hit SOAP WSDL: `GET /services/create-draft-certificate/v3.0?wsdl`
- Hit REST endpoint: `GET /api/...`
- Start with `dev` profile; verify stub endpoints respond
- Verify: `grep -rn "webcert-config.xml" web/src/` returns no results

---

## Phase F: Profile Cleanup

### Sub-step 12.15 -- Simplify multi-value @Profile annotations to @Profile("dev")

**What:** Any @Profile annotation that lists multiple values including "dev" is replaced
with the single-value @Profile("dev"). This consolidates stub and testability activation
under one well-known profile name.

**Why:** The various stub-specific profile names (wc-all-stubs, wc-notificationsender-stub,
wc-mail-stub, wc-fmb-stub, 	estability-api, etc.) add complexity with no benefit for a
local development setup. Using only dev makes the profile strategy uniform and easier to
maintain.

**Affected files (confirmed by codebase scan):**

| File | Current @Profile | New @Profile |
|---|---|---|
| SrsStubConfiguration.java | {"dev", "wc-all-stubs", "wc-srs-stub"} | "dev" |
| IAStubConfiguration.java | {"dev", "ia-stub"} | "dev" |
| JmsConfig.java (stub bean) | {"dev", "testability-api"} | "dev" |
| ArendeResource.java | {"dev", "testability-api"} | "dev" |
| FragaSvarResource.java | {"dev", "testability-api"} | "dev" |
| LogResource.java | {"dev", "testability-api"} | "dev" |
| IntygResource.java | {"dev", "testability-api"} | "dev" |
| UserAgreementResource.java | {"dev", "testability-api"} | "dev" |
| FmbResource.java | {"dev", "testability-api"} | "dev" |
| IntegreradEnhetResource.java | {"dev", "testability-api"} | "dev" |
| CertificateTestabilityController.java | {"dev", "testability-api"} | "dev" |
| FakeLoginTestabilityController.java | {"dev", "testability-api"} | "dev" |
| ConfigurationResource.java | {"dev", "testability-api"} | "dev" |
| EventResource.java | {"dev", "testability-api"} | "dev" |
| ReferensResource.java | {"dev", "testability-api"} | "dev" |
| NotificationStubDataConfig.java (new) | {"dev", ...} | "dev" |
| NotificationStubConfig.java (new) | {"dev", ...} | "dev" |
| MailStubConfig.java (new) | {"dev", ...} | "dev" |
| MailStubTestabilityConfig.java (new) | {"dev", ...} | "dev" |
| FmbStubConfig.java (new) | {"dev", ...} | "dev" |

**Do NOT change these profiles** (no "dev", or have non-stub semantics):
- CacheConfig.java: {"caching-enabled", "prod"} and !(caching-enabled | prod) -- stay
- IaCacheConfiguration.java: {"qa", "prod", "caching-enabled"} -- stay
- JpaConfig.java: "!h2" -- stay
- ServiceNowStubConfig.java: complex boolean expression -- evaluate separately
- @Profile("!prod"), @Profile("prod") beans -- stay
- @Profile("ia-stub") / @Profile("!ia-stub") in IAServicesConfiguration.java -- stay
- @Profile("certificate-analytics-service-active") -- stay

**Changes:**
For each file in the table above, replace the multi-value array with "dev":
`java
// Before
@Profile({"dev", "testability-api"})
// After
@Profile("dev")
`

**Verification:** ./gradlew test -- all tests pass. Start with profile dev; confirm all
stubs and testability endpoints respond.

---


## Final Verification — After Complete Step 12

```bash
./gradlew test
```
All tests must pass.

```bash
./gradlew appRun
```
Application starts on Gretty. Then verify:

- [ ] `grep -rn "classpath:webcert-config.xml" web/src/` returns no results
- [ ] `find web/src -name "*.xml" -path "*/resources/*" | grep -v camel | grep -v test.xml | grep -v "\.properties"` returns only XSD schemas and XSLT transforms
- [ ] `grep -rn "@ImportResource" web/src/main/java/` returns only `AppConfig.java` (wildcard + notification-sender bridge) and the Camel test configs
- [ ] All 6 SOAP endpoints respond at `/services/...?wsdl`
- [ ] All REST endpoints respond normally
- [ ] Dev profile stubs work: `/api/notification-api`, `/api/mail-api`, `/stubs/fmbstubs`
- [ ] Testability profile works: `/testability/...`
- [ ] No XML import failures in startup logs
- [ ] `web.xml` `contextConfigLocation` contains the Java class name, not a `.xml` file

---

## Risk Notes

**`parserPool` initialization order** — The XML had `init-method="initialize"`. In Java config,
use `@Bean(initMethod = "initialize")`. Verify `OpenSamlConfig` depends on `parserPool` via
`@DependsOn` or constructor injection, not field injection that could race with initialization.

**Child context bean visibility** — `CxfEndpointConfig` (child of `services` CXF servlet) can
access root-context beans (the SOAP implementors like `CreateDraftCertificateResponderImpl`)
because child contexts inherit from parent. This works automatically — no changes needed.

**DispatcherServlet double scan** — After Step 11, the root context's broad scan
(`se.inera.intyg.webcert.web`) already picks up all `@RestController` beans. When
`WebMvcConfiguration.java` is set as the DispatcherServlet's config with its own controller
scan, beans exist in both contexts. Spring MVC handler resolution correctly picks the
DispatcherServlet-context registration. Accept this during Step 12; consolidation to a single
context is a Step 14 concern (Spring Boot auto-configuration).

**Bean name clashes** — Some XML bean IDs match names auto-assigned to Java `@Bean` methods.
Watch for `BeanDefinitionOverrideException` at startup. Common culprit: `taskExecutor` (used
by Spring's `@Async` infrastructure AND by the GRP thread pool). Renaming to `grpTaskExecutor`
in 12.14b resolves this — but confirm all injection sites are updated.

**`@EnableAsync` and the task executor** — The `threadPoolTaskExecutor` bean (from `MailConfig.java`)
is explicitly referenced by name in `@Async("threadPoolTaskExecutor")` in `MailNotificationServiceImpl`.
Spring's `@EnableAsync` default executor lookup is for a bean named `taskExecutor` — since there
is none (the GRP executor is now `grpTaskExecutor`), Spring falls back to `SimpleAsyncTaskExecutor`
for any `@Async` calls without an explicit executor name. Verify no other `@Async` methods exist
that expect the 5-thread GRP pool as their default executor.

**`FmbConsumerImpl` uses constructor-arg, not property setter** — `fmb-services-config.xml`
creates `FmbConsumerImpl` with `<constructor-arg name="baseUrl">`. The class has no `@Service`.
Both this and the wrong class path (`services` vs `consumer` package) are corrected in sub-step
12.3. Handle before deleting the XML.

**`SoapFaultToSoapResponseTransformerInterceptor` source** — This class is referenced in
`services-cxf-servlet.xml` but does not exist in the local codebase. It must come from an
infra module inlined in Step 8. If it is not yet a local class when executing sub-step 12.13,
the `CxfEndpointConfig.java` will not compile. Verify it exists before starting Phase D.

**`InternalApiFilter` source** — Similarly, `InternalApiFilter` is listed in the inline bean
table but does not exist locally. It must be inlined from `se.inera.intyg.infra.security.filter`
in Step 8 before this step.

---

## Gap Analysis Summary (Applied to This Plan)

The following gaps were identified by comparing the plan against the actual codebase and corrected
inline above. This section documents them for traceability.

| # | Severity | Gap | Fixed In |
|---|---|---|---|
| G1 | 🔴 Critical | `MailConfig.java` used wrong bean names: executor was `mailTaskExecutor` (should be `threadPoolTaskExecutor`), scheduler was `mailTaskScheduler` (should be `scheduler`). `MailNotificationServiceImpl.@Async("threadPoolTaskExecutor")` would have broken. | §12.5 |
| G2 | 🔴 Critical | `MailConfig.java` hardcoded `protocol="smtps"` and `encoding="UTF-8"` — both are configurable properties (`${mail.protocol}`, `${mail.defaultEncoding}`). Debug key was also wrong (`mail.debug` vs `mail.smtps.debug`). | §12.5 |
| G3 | 🔴 Critical | `MailConfig.java` missing `rejection-policy="CALLER_RUNS"` — default would throw `RejectedExecutionException` instead of blocking the caller. | §12.5 |
| G4 | 🔴 Critical | `NotificationStubConfig.java` used a single `@Profile` for all beans, but the XML uses 3 distinct profile groups. SOAP endpoint would activate in `testability-api` profile, REST API would not activate in `wc-notificationsender-stub` profile. | §12.7 |
| G5 | 🟠 High | `mail-stub-context.xml` `mailAdvice` bean (`JavaMailSenderAroundAdvice`) was completely omitted from 12.8. Without it the mail stub cannot intercept outgoing mail. | §12.8 |
| G6 | 🟠 High | `mail-stub-context.xml` has profile `dev,wc-all-stubs,wc-mail-stub`, not `dev,testability-api`. Plan incorrectly merged two contexts with different profiles into one class. | §12.8 |
| G7 | 🟠 High | `AppConfig.java` has **no existing `@ComponentScan`** — plan said "add to existing". Every scan must be created from scratch. | §12.2, Pre-Conditions |
| G8 | 🟠 High | `objectMapper` `@Bean` is **not** in `WebMvcConfiguration.java` — plan said "confirm @Bean present". It must be created explicitly, and `WebMvcConfiguration` must be updated to inject it. | §12.14b, Inline Beans Table |
| G9 | 🟠 High | `FmbConsumerImpl` uses `constructor-arg name="baseUrl"` (not a property), and has no `@Service`. The correct class package is `consumer` not `services`. | §12.3 |
| G10 | 🟡 Medium | All 12 URL property keys in the `CxfWsClientConfig.java` client table were wrong (e.g., `sendquestion.recipient.service.url` vs actual `sendquestiontofk.endpoint.url`). | §12.12 |
| G11 | 🟡 Medium | `parserPool` bare `new BasicParserPool()` omitted all XXE/security settings. `OpenSamlConfig` already owns this pool internally — plan needed to check before adding duplicate `@Bean`. | §12.14b |
| G12 | 🟡 Medium | `CommonAuthoritiesResolver` has `@Autowired SecurityConfigurationLoader`, and `AuthoritiesHelper` has `@Autowired` constructor. Plain `new X()` would miss injections. | §12.14b |
| G13 | 🟡 Medium | `GrpRestConfig.java` currently only has `grpRestClient` — plan implied the `grpTaskExecutor` bean already existed there. | §12.14b, Inline Beans Table |
| G14 | 🟡 Medium | `DefaultCharacterEncodingFilter` already has `@Component` — no action needed, but plan said "Add @Component". | Inline Beans Table |
| G15 | 🟡 Medium | `SoapFaultToSoapResponseTransformerInterceptor` and `InternalApiFilter` don't exist locally — they must come from infra (Step 8 pre-condition). | Pre-Conditions, Risk Notes |
| G16 | 🟢 Low | `web.xml` `web` servlet has no `contextConfigLocation` — auto-discovers `web-servlet.xml` by convention. Step 12.6 correctly adds it but the reason was not explained. | §12.6 |
| G17 | 🟢 Low | `notification-stub-context.xml` SOAP endpoint has 6 `jaxws:schemaLocations` omitted from the plan's `NotificationStubConfig.java` code. | §12.7 |
