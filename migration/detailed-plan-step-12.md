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
| `defaultCharacterEncodingFilter` | `DefaultCharacterEncodingFilter` | Add `@Component` to class |
| `internalApiFilter` | `InternalApiFilter` | Add `@Component` to class |
| `objectMapper` | `CustomObjectMapper` | Already in `WebMvcConfiguration.java`; confirm `@Bean` present |
| `jacksonJsonProvider` | `JacksonJsonProvider` | **Delete** after stubs migrated (12.11) |
| `taskExecutor` (GRP) | `ThreadPoolTaskExecutor` | `GrpRestConfig.java` as `@Bean("grpTaskExecutor")` |
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
| 12.1 | Remove `repository-context.xml` | Low | ⬜ |
| 12.2 | Remove `webcert-common-config.xml` | Low | ⬜ |
| 12.3 | Remove integration module XML configs (4 files) | Low | ⬜ |
| 12.4 | Remove `webcert-testability-api-context.xml` | Low | ⬜ |
| **Phase B: Create Missing Java Configuration Classes** | | | |
| 12.5 | Create `MailConfig.java` replacing `mail-config.xml` | Low | ⬜ |
| 12.6 | Merge `web-servlet.xml` into `WebMvcConfiguration.java` | Medium | ⬜ |
| **Phase C: Convert Stub XML Contexts** | | | |
| 12.7 | Convert `notification-stub-context.xml` → `NotificationStubConfig.java` | Medium | ⬜ |
| 12.8 | Convert mail-stub XML contexts → `MailStubConfig.java` | Medium | ⬜ |
| 12.9 | Convert `fmb-stub-context.xml` → `FmbStubConfig.java` | Medium | ⬜ |
| 12.10 | Handle `servicenow-stub-context.xml` | Low | ⬜ |
| 12.11 | Remove `jacksonJsonProvider` bean and JAX-RS JSON provider dependency | Low | ⬜ |
| **Phase D: Convert CXF Configuration** | | | |
| 12.12 | Create `CxfWsClientConfig.java` replacing `ws-config.xml` | ⚠️ High | ⬜ |
| 12.13 | Create `CxfEndpointConfig.java` replacing `services-cxf-servlet.xml` | ⚠️ High | ⬜ |
| **Phase E: Migrate notification-sender Test Configs** | | | |
| 12.14 | Convert `unit-test-notification-sender-config.xml` to Java | Low | ⬜ |
| 12.15 | Convert `unit-test-certificate-sender-config.xml` to Java | Low | ⬜ |
| 12.16 | Convert integration-test certificate config + embedded broker to Java | Medium | ⬜ |
| **Phase F: Remove Root XML** | | | |
| 12.17 | Collapse `webcert-config.xml` into `AppConfig.java` | ⚠️ Critical | ⬜ |

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
2. Add `"se.inera.intyg.webcert.common"` to the existing `@ComponentScan` (or add a new
   `@ComponentScan("se.inera.intyg.webcert.common")` annotation — Java allows multiple).
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
<bean id="fmbConsumer" class="se.inera.intyg.webcert.integration.fmb.services.FmbConsumerImpl">
  <property name="fmbEndpointUrl" value="${fmb.endpoint.url}"/>
</bean>
```
If `FmbConsumerImpl` has no `@Service` annotation, add it and inject `@Value("${fmb.endpoint.url}")`
directly in the class, or create a `FmbServicesConfig.java` with the `@Bean` definition. Do not
delete the XML until this is resolved.

**Verification:** `./gradlew test` — FMB, ServiceNow, analytics, private-practitioner integrations
all start and resolve beans correctly.

---

### Sub-step 12.4 — Remove `webcert-testability-api-context.xml`

**What:** Remove the XML file that previously registered 12 testability controllers as JAX-RS beans.

**Why:** Step 11 converted all 12 testability controllers to `@RestController` beans with
`@Profile({"dev", "testability-api"})`. They are now auto-discovered by the DispatcherServlet's
component scan. The XML registration is obsolete — and would cause duplicate bean errors if left.

**Pre-check:** Confirm all 12 controllers have `@RestController` and `@Profile`:
```bash
grep -l "class ArendeResource\|class FragaSvarResource\|class LogResource\|class IntygResource\|class UserAgreementResource\|class FmbResource\|class IntegreradEnhetResource\|class CertificateTestabilityController\|class FakeLoginTestabilityController\|class ConfigurationResource\|class EventResource\|class ReferensResource" \
  web/src/main/java/ -r
# Then for each file:
grep -n "@RestController\|@Profile" <file>
```

**Changes:**
1. `services-cxf-servlet.xml` imports `webcert-testability-api-context.xml`. Remove that import line
   from `services-cxf-servlet.xml`.
2. Delete `web/src/main/resources/webcert-testability-api-context.xml`.
   (Also check `web/src/main/webapp/WEB-INF/` for a copy; delete both if present.)

**Verification:** `./gradlew test` + start with profile `testability-api`; hit a testability endpoint.

---

## Phase B: Create Missing Java Configuration Classes

### Sub-step 12.5 — Create `MailConfig.java` replacing `mail-config.xml`

**What:** Replace `mail-config.xml` with a Java `@Configuration` class that defines the same beans.

**Why:** `mail-config.xml` currently defines:
- `<task:annotation-driven/>` — enables `@Async` and `@Scheduled` on beans
- `<task:scheduler id="mailTaskScheduler" pool-size="1"/>`
- `<task:executor id="mailTaskExecutor" pool-size="10" queue-capacity="100"/>`
- `<bean id="mailSender" class="JavaMailSenderImpl">` — SMTP configuration

**⚠️ Bean name collision:** `webcert-config.xml` also has a `taskExecutor` bean (for BankID GRP).
Name the mail executor clearly to avoid conflict: use `mailTaskExecutor` as the `@Bean` name and
`@Qualifier("mailTaskExecutor")` where it is injected. The GRP executor is moved in sub-step 12.17.

**Current `mail-config.xml` properties (verify exact property names in `application.properties`):**
```xml
<bean id="mailSender" class="org.springframework.mail.javamail.JavaMailSenderImpl">
  <property name="host" value="${mail.host}"/>
  <property name="protocol" value="smtps"/>
  <property name="username" value="${mail.username}"/>
  <property name="password" value="${mail.password}"/>
  <property name="defaultEncoding" value="UTF-8"/>
  <property name="javaMailProperties">
    <props>
      <prop key="mail.smtps.auth">${mail.smtps.auth}</prop>
      <prop key="mail.smtps.starttls.enable">${mail.smtps.starttls.enable}</prop>
      <prop key="mail.debug">${mail.debug:false}</prop>
    </props>
  </property>
</bean>
```

**Changes:**
1. Create `web/src/main/java/se/inera/intyg/webcert/web/config/MailConfig.java`:
   ```java
   @Configuration
   public class MailConfig {

       @Value("${mail.host}") private String mailHost;
       @Value("${mail.username}") private String mailUsername;
       @Value("${mail.password}") private String mailPassword;
       @Value("${mail.smtps.auth}") private String smtpsAuth;
       @Value("${mail.smtps.starttls.enable}") private String startTls;
       @Value("${mail.debug:false}") private String mailDebug;

       @Bean(name = "mailTaskScheduler")
       public ThreadPoolTaskScheduler mailTaskScheduler() {
           ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
           scheduler.setPoolSize(1);
           return scheduler;
       }

       @Bean(name = "mailTaskExecutor")
       public ThreadPoolTaskExecutor mailTaskExecutor() {
           ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
           executor.setCorePoolSize(10);
           executor.setQueueCapacity(100);
           executor.initialize();
           return executor;
       }

       @Bean
       public JavaMailSenderImpl mailSender() {
           JavaMailSenderImpl sender = new JavaMailSenderImpl();
           sender.setHost(mailHost);
           sender.setProtocol("smtps");
           sender.setUsername(mailUsername);
           sender.setPassword(mailPassword);
           sender.setDefaultEncoding("UTF-8");
           Properties props = new Properties();
           props.setProperty("mail.smtps.auth", smtpsAuth);
           props.setProperty("mail.smtps.starttls.enable", startTls);
           props.setProperty("mail.debug", mailDebug);
           sender.setJavaMailProperties(props);
           return sender;
       }
   }
   ```
2. Add `@Import(MailConfig.class)` to `AppConfig.java`.
3. Confirm `@EnableAsync` and `@EnableScheduling` are already present (they're in `JobConfig.java`).
   `<task:annotation-driven/>` is therefore already covered.
4. Remove `<import resource="mail-config.xml"/>` from `webcert-config.xml`.
5. Delete `web/src/main/resources/mail-config.xml`.

**Verification:** `./gradlew test` — mail-sending tests pass; no `NoSuchBeanDefinitionException`
for `mailSender`.

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
1. Create `stubs/notification-stub/src/main/java/.../config/NotificationStubConfig.java`:
   ```java
   @Configuration
   @Profile({"dev", "wc-all-stubs", "wc-notificationsender-stub", "testability-api"})
   public class NotificationStubConfig {

       @Bean
       public NotificationStoreV3Impl notificationStoreV3() {
           return new NotificationStoreV3Impl();
       }

       @Bean
       public NotificationStubStateBean notificationStubStateBean() {
           return new NotificationStubStateBean();
       }

       // CXF JAXWS server endpoint
       @Bean
       public Endpoint notificationStubSoapEndpoint(
               CertificateStatusUpdateForCareResponderInterface impl) {
           EndpointImpl endpoint = new EndpointImpl(impl);
           endpoint.publish(
               "/clinicalprocess/healthcond/certificate/CertificateStatusUpdateForCare/3/rivtabp21");
           return endpoint;
       }
   }
   ```
2. Identify the JAX-RS REST controller class currently registered in the XML (look in the XML for
   the class reference in `<jaxrs:serviceBeans>`).
3. Convert that class to a Spring MVC `@RestController` with
   `@Profile({"dev", "wc-all-stubs", "wc-notificationsender-stub", "testability-api"})`.
   Replicate all REST endpoint methods using `@GetMapping`, `@PostMapping`, etc.
4. Remove the import of `notification-stub-context.xml` from `services-cxf-servlet.xml`.
5. Delete `stubs/notification-stub/src/main/resources/notification-stub-context.xml`.

**Verification:** Start with profile `dev`. Hit the notification stub REST API at
`/api/notification-api/...`. Verify the SOAP stub endpoint still responds at its WSDL URL.

---

### Sub-step 12.8 — Convert mail-stub XML contexts → `MailStubConfig.java`

**What:** Two mail-stub XML files:
- `mail-stub-context.xml` — defines mail stub beans, likely with `JavaMailSender` replacement
- `mail-stub-testability-api-context.xml` (profile: `dev,testability-api`) — registers a JAXRS
  REST server at `/api/mail-api` for testing mail sending

**Changes:**
1. Read both XML files to catalogue all beans.
2. Create `stubs/mail-stub/src/main/java/.../config/MailStubConfig.java` combining both:
   ```java
   @Configuration
   @Profile({"dev", "testability-api"})
   public class MailStubConfig {
       // All beans from mail-stub-context.xml and mail-stub-testability-api-context.xml
       @Bean MailStore mailStore() { ... }
   }
   ```
3. Convert the JAX-RS REST controller class (from `mail-stub-testability-api-context.xml`'s
   `<jaxrs:serviceBeans>`) to a Spring MVC `@RestController` with
   `@Profile({"dev", "testability-api"})`.
4. Remove `<import resource="classpath:mail-stub-context.xml"/>` from `webcert-config.xml`.
5. Remove the import of both mail-stub XMLs from `services-cxf-servlet.xml`.
6. Delete both `mail-stub-context.xml` and `mail-stub-testability-api-context.xml`.

**Verification:** Start with `dev` profile. Hit the mail stub API at `/api/mail-api/...`.

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
    @Value("${sendquestion.recipient.service.url}") String address) {
    JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
    factory.setServiceClass(SendMedicalCertificateQuestionResponderInterface.class);
    factory.setAddress(address);
    factory.getFeatures().add(new LoggingFeature());
    return (SendMedicalCertificateQuestionResponderInterface) factory.create();
}
```

**Clients to convert** (read `ws-config.xml` to get exact class names and URL property keys):

| Bean ID | Service Interface | URL Property |
|---|---|---|
| `sendQuestionToFKClient` | `SendMedicalCertificateQuestionResponderInterface` | `sendquestion.recipient.service.url` |
| `sendAnswerToFKClient` | `SendMedicalCertificateAnswerResponderInterface` | `sendanswer.recipient.service.url` |
| `listCertificatesForCareResponderV3` | `ListCertificatesForCareResponderInterface` (v3) | `intygstjanst.listsickleave.url` |
| `sendCertificateClient` | `SendCertificateToRecipientResponderInterface` | `intygstjanst.send.url` |
| `revokeCertificateClient` | (old NTJP interface) | `revokecertificate.url` |
| `revokeCertificateClientRivta` | (new NTJP v2 interface) | `revokecertificaterivta.url` |
| `sendMessageToRecipientClient` | `SendMessageToRecipientResponderInterface` | `sendmessagetorecipient.url` |
| `registerCertificateClient` | `RegisterCertificateResponderInterface` (v3) | `intygstjanst.register.url` |
| `getCertificateClient` | `GetCertificateResponderInterface` (v2) | `intygstjanst.getcertificate.url` |
| `ListActiveSickLeavesForCareUnitClient` | `ListActiveSickLeavesForCareUnitResponderInterface` | `listactivesickleavesforcareunit.url` |
| `getCertificateTypeInfoClient` | `GetCertificateTypeInfoResponderInterface` | `getcertificatetypeinfo.url` |
| `listRelationsForCertificateClient` | `ListRelationsForCertificateResponderInterface` | `listrelationsforcertificate.url` |
| Approved receivers clients (3) | Various | Various |

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

## Phase E: Migrate notification-sender Test Configs

> **Note:** These sub-steps affect test code only. They have zero runtime impact and can be
> done in parallel with any Phase D work. The Camel XML files (`camel-context.xml`,
> `beans-context.xml`) are NOT removed here — that is Step 13. The goal is only to remove
> the outer test wrapper XMLs that add beans on top of the Camel XMLs.

### Sub-step 12.14 — Convert `unit-test-notification-sender-config.xml` to Java

**What:** `NotificationCamelTestConfig.java` currently loads:
```java
@ImportResource(locations = "classpath:notifications/unit-test-notification-sender-config.xml")
```
That XML file defines non-Camel test beans and imports `camel-context.xml`.

**Changes:**
1. Open `NotificationCamelTestConfig.java`.
2. Remove the `@ImportResource` for `unit-test-notification-sender-config.xml`.
3. Add a direct `@ImportResource` for the Camel context only:
   ```java
   @ImportResource("classpath:notifications/camel-context.xml")
   ```
4. Add `@TestPropertySource("classpath:notifications/unit-test.properties")`.
5. Add `@Bean` methods for the non-Camel beans previously defined in the XML:
   - `processNotificationRequestRouteBuilder` (if it's not already a Spring-managed `@Component`)
   - `notificationAggregator`
   - `objectMapper` (CustomObjectMapper)
   - `notificationMessageDataFormat` (JacksonDataFormat)
6. Delete `notification-sender/src/test/resources/notifications/unit-test-notification-sender-config.xml`.

**Verification:** `./gradlew :notification-sender:test --tests "*NotificationRouteTest*"`

---

### Sub-step 12.15 — Convert `unit-test-certificate-sender-config.xml` to Java

**What:** `CertificateCamelTestConfig.java` currently loads the unit test XML via `@ImportResource`.

**Changes:**
1. Open `CertificateCamelTestConfig.java`.
2. Remove the `@ImportResource` for `unit-test-certificate-sender-config.xml`.
3. Add direct `@ImportResource` references for the Camel XML files only:
   ```java
   @ImportResource({
       "classpath:certificates/beans-context.xml",
       "classpath:certificates/camel-context.xml"
   })
   ```
4. Add `@ComponentScan("se.inera.intyg.webcert.notification_sender.certificatesender")`.
5. Add `@TestPropertySource("classpath:certificates/unit-test.properties")`.
6. Delete `notification-sender/src/test/resources/certificates/unit-test-certificate-sender-config.xml`.

**Verification:** `./gradlew :notification-sender:test --tests "*RouteTest*"`

---

### Sub-step 12.16 — Convert integration-test certificate config + embedded broker to Java

**What:** `CertificateCamelIntegrationTestConfig.java` loads:
```java
@ImportResource(locations = "classpath:certificates/integration-test-certificate-sender-config.xml")
```
That XML imports `integration-test-broker-context.xml` (embedded ActiveMQ) plus the Camel XMLs.

**Changes:**
1. Open `CertificateCamelIntegrationTestConfig.java`.
2. Remove the `@ImportResource` for `integration-test-certificate-sender-config.xml`.
3. Add direct `@ImportResource` references for the Camel XML files only (same as 12.15).
4. Create an embedded ActiveMQ broker `@Bean` replacing `integration-test-broker-context.xml`.
   Read the broker XML to identify queue names, connection factory settings, and transaction
   manager configuration. Then:
   ```java
   @Bean(initMethod = "start", destroyMethod = "stop")
   public BrokerService embeddedBroker() throws Exception {
       BrokerService broker = new BrokerService();
       broker.setBrokerName("test-broker");
       broker.setPersistent(false);
       broker.addConnector("vm://localhost");
       // Add queues from integration-test-broker-context.xml
       return broker;
   }
   ```
5. Define connection factories and transaction manager for the integration test context, matching
   the configuration in `integration-test-broker-context.xml`.
6. Delete `integration-test-certificate-sender-config.xml`.
7. Delete `integration-test-broker-context.xml`.

**Verification:** `./gradlew :notification-sender:test --tests "*RouteIT*"`

---

## Phase F: Remove Root XML

### Sub-step 12.17 — Collapse `webcert-config.xml` into `AppConfig.java` ⚠️ Critical

**What:** Move all remaining content of `webcert-config.xml` to Java, then update `web.xml` to
point directly to `AppConfig.java` instead of `webcert-config.xml`. Delete the XML file.

#### 12.17a — Audit remaining content

At this point, `webcert-config.xml` should contain only:
- `<context:annotation-config/>`
- `<context:component-scan>` entries (8 packages)
- `<import resource="classpath*:module-config.xml"/>` (wildcard — external JARs)
- `<import resource="classpath*:wc-module-cxf-servlet.xml"/>` (wildcard — external JARs)
- `<import resource="classpath:common-config.xml"/>` (intyg-common JAR)
- `<import resource="classpath:notification-sender-config.xml"/>` (deferred to Step 13)
- ~30 inline bean definitions (see table in Current State section)

#### 12.17b — Move inline beans to Java configs

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
```java
@Bean(initMethod = "initialize")
public BasicParserPool parserPool() {
    return new BasicParserPool();
}
```
The XML had `init-method="initialize"` — replicate with `initMethod = "initialize"` in `@Bean`.

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
        return new CommonAuthoritiesResolver();
    }

    @Bean
    public AuthoritiesHelper authoritiesHelper() {
        return new AuthoritiesHelper();
    }
}
```
Add `@Import(AuthoritiesConfig.class)` to `AppConfig.java`.

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
Check each class for existing `@Service` or `@Component` annotation:
```bash
grep -n "@Service\|@Component" \
  web/src/main/java/se/inera/intyg/webcert/web/service/privatlakaravtal/AvtalServiceImpl.java \
  web/src/main/java/se/inera/intyg/webcert/web/service/utkast/CopyCompletionUtkastBuilder.java \
  web/src/main/java/se/inera/intyg/webcert/web/service/patient/PatientDetailsResolverImpl.java \
  web/src/main/java/se/inera/intyg/webcert/web/web/filter/DefaultCharacterEncodingFilter.java \
  "web/src/main/java/.../InternalApiFilter.java"
```
If any class lacks the annotation, add it. Do not create `@Bean` methods for these — annotating
the class directly is cleaner and consistent with the rest of the codebase.

**`taskExecutor` for GRP → `GrpRestConfig.java`:**
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
Update the existing `GrpRestConfig.java` (which already exists). Find all injections of this
bean in GRP-related classes and confirm they use `@Qualifier("grpTaskExecutor")` (or the bean name
matches how it was injected via XML's `ref="taskExecutor"`).

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

#### 12.17c — Migrate component scans to `AppConfig.java`

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

#### 12.17d — Add `@ImportResource` for wildcard and deferred imports

```java
@ImportResource({
    "classpath*:module-config.xml",          // external certificate module configs
    "classpath*:wc-module-cxf-servlet.xml",  // external module CXF configs
    "classpath:common-config.xml",            // intyg-common JAR config
    "classpath:notification-sender-config.xml" // deferred to Step 13
})
```

#### 12.17e — Update `web.xml` root context configuration

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

#### 12.17f — Delete `webcert-config.xml`

After verifying the application starts (see verification below):
```bash
rm web/src/main/resources/webcert-config.xml
```

**Full verification for 12.17:**
- `./gradlew test` — all tests pass
- `./gradlew appRun` (Gretty) — application starts without `BeanCreationException` or
  `NoSuchBeanDefinitionException`
- Startup log contains all expected bean names
- Hit SOAP WSDL: `GET /services/create-draft-certificate/v3.0?wsdl`
- Hit REST endpoint: `GET /api/...`
- Start with `dev` profile; verify stub endpoints respond
- Verify: `grep -rn "webcert-config.xml" web/src/` returns no results

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
in 12.17b resolves this — but confirm all injection sites are updated.

**`@EnableAsync` and the task executor** — Spring's `@EnableAsync` looks for a bean named
`taskExecutor` of type `Executor`. If this bean is renamed, Spring falls back to a default
`SimpleAsyncTaskExecutor`. If any `@Async` methods relied on the 5-thread GRP pool, this changes
their threading behaviour. Verify by checking which classes use `@Async` and whether they were
intended to use the GRP executor or a general executor.

**FMB `fmbConsumer` bean** — `fmb-services-config.xml` has an explicit `fmbConsumer` bean
definition with a `fmbEndpointUrl` property. If `FmbConsumerImpl` doesn't have `@Service` plus
`@Value("${fmb.endpoint.url}")`, this bean will be missing after the XML is deleted. Handle
in sub-step 12.3 before deleting the XML.
