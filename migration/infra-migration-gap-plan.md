# Infra Migration — Gap Plan

## Problem Statement

The infra dependency removal (steps 2–10 of `incremental-migration-plan.md`) has been completed: all
`se.inera.intyg.infra:*` Gradle dependencies are removed, all imports rewritten to
`se.inera.intyg.webcert.infra.*`, and unused classes cleaned up. However, several items called for
in
the incremental plan were not fully addressed during that work. This plan identifies and orders
those
remaining gaps.

**Current state:** The project compiles, all tests pass, and the application starts. But several XML
Spring configurations that were supposed to be replaced with Java `@Configuration` during the
inlining still exist as XML files in `infra/src/main/resources/`.

---

## Gap Summary

| # | Gap                                                                                        | Severity | Effort  |
|---|--------------------------------------------------------------------------------------------|----------|---------|
| 1 | ✅ `basic-cache-config.xml` — migrated to Java `@Configuration`                             | Medium   | Small   |
| 2 | ✅ `xmldsig-config.xml` still imported in `webcert-config.xml` — should be `@Configuration` | Medium   | Small   |
| 3 | ✅ `ia-services-config.xml` still imported — service beans should be `@Configuration`       | Medium   | Small   |
| 4 | ✅ `srs-services-config.xml` still imported — JAXWS clients + service bean in XML           | Low      | Medium  |
| 5 | ✅ `srs-stub-context.xml` still imported in CXF servlet — stub endpoints in XML             | Low      | Medium  |
| 6 | ✅ `ia-stub-context.xml` still imported in CXF servlet — stub REST endpoint in XML          | Low      | Small   |
| 7 | `logback-ocp-base.xml` only used by infra test — consider removing or relocating           | Low      | Trivial |
| 8 | `MonitoringConfiguration` not loaded in production — dead `@Configuration` class           | Low      | Small   |
| 9 | Remaining infra test classes — verify they run and are valuable                            | Low      | Small   |

---

## Step 1 — Replace `basic-cache-config.xml` with Java `@Configuration`

**Goal:** Remove `@ImportResource({"classpath:basic-cache-config.xml"})` from `CacheConfig.java` and
the `<import resource="classpath:basic-cache-config.xml"/>` from `webcert-config.xml`. Replace with
equivalent Java configuration.

**Current XML** (`infra/src/main/resources/basic-cache-config.xml`):

```xml

<cache:annotation-driven cache-manager="cacheManager"/>

<beans profile="caching-enabled,prod">
<bean class="se.inera.intyg.webcert.infra.rediscache.core.BasicCacheConfiguration"/>
</beans>
<beans profile="!caching-enabled">
<beans profile="!prod">
  <bean id="cacheManager" class="org.springframework.cache.support.NoOpCacheManager"/>
</beans>
</beans>
```

**What to do:**

1. Edit `web/src/main/java/.../web/config/CacheConfig.java`:
    - Remove `@ImportResource({"classpath:basic-cache-config.xml"})`
    - Add `@EnableCaching` (replaces `<cache:annotation-driven/>`)
    - Add a `@Bean` for `NoOpCacheManager` with `@Profile("!caching-enabled & !prod")`
    - Add a `@Bean` (or `@Import`) for `BasicCacheConfiguration` with
      `@Profile({"caching-enabled", "prod"})`
    - Note: `BasicCacheConfiguration` is already a `@Configuration` class — it just needs to be
      conditionally loaded

2. Remove `<import resource="classpath:basic-cache-config.xml"/>` from `webcert-config.xml` (line
   36)

3. Delete `infra/src/main/resources/basic-cache-config.xml`

**Verify:** `./gradlew test` + start application. Redis caching still works in profiles with
`caching-enabled` or `prod`. NoOp cache used otherwise. Check that `@Cacheable` annotations still
trigger caching.

---

## Step 2 — Replace `xmldsig-config.xml` with Java `@Configuration`

**Goal:** Replace the XML bean definitions with a Java configuration class.

**Current XML** (`infra/src/main/resources/xmldsig-config.xml`):

```xml

<bean id="xmldSigService" class="...XMLDSigServiceImpl"/>
<bean id="prepareSignatureService" class="...PrepareSignatureServiceImpl"/>

<beans profile="!prod">
<bean id="fakeSignatureService" class="...FakeSignatureServiceImpl"/>
</beans>
<beans profile="prod">
<bean id="fakeSignatureService" class="...FakeSignatureServiceBlocked"/>
</beans>
```

**What to do:**

1. Create `infra/src/main/java/.../infra/xmldsig/config/XmlDSigConfiguration.java`:
   ```java
   @Configuration
   public class XmlDSigConfiguration {

     @Bean
     public XMLDSigService xmldSigService() {
       return new XMLDSigServiceImpl();
     }

     @Bean
     public PrepareSignatureService prepareSignatureService() {
       return new PrepareSignatureServiceImpl();
     }

     @Bean
     @Profile("!prod")
     public FakeSignatureService fakeSignatureServiceDev() {
       return new FakeSignatureServiceImpl();
     }

     @Bean
     @Profile("prod")
     public FakeSignatureService fakeSignatureServiceProd() {
       return new FakeSignatureServiceBlocked();
     }
   }
   ```

2. Remove `<import resource="classpath:xmldsig-config.xml"/>` from `webcert-config.xml` (line 39)

3. Add component scan or `@Import(XmlDSigConfiguration.class)` in `webcert-config.xml` or
   an existing Java config class (e.g. `AppConfig`). Alternatively, add the package to the existing
   component-scan in `webcert-config.xml`.

4. Delete `infra/src/main/resources/xmldsig-config.xml`

**Verify:** `./gradlew test` + start. Certificate signing (DSS, fake) still works. Check that
`FakeSignatureServiceBlocked` is used in `prod` profile and `FakeSignatureServiceImpl` otherwise.

---

## Step 3 — Replace `ia-services-config.xml` with Java `@Configuration`

**Goal:** Replace IA banner service XML bean definitions with Java configuration.

**Current XML** (`infra/src/main/resources/ia-services-config.xml`):

```xml

<beans profile="ia-stub">
  <bean class="...IABannerServiceStub"/>
</beans>
<beans profile="!ia-stub">
<bean class="...IABannerServiceImpl"/>
</beans>
<beans profile="qa,prod">
<bean class="...IaCacheConfiguration"/>
</beans>
<beans profile="!prod">
<beans profile="!qa">
  <beans profile="caching-enabled">
    <bean class="...IaCacheConfiguration"/>
  </beans>
</beans>
</beans>
```

**What to do:**

1. Create `infra/src/main/java/.../infra/ia/config/IAServicesConfiguration.java`:
   ```java
   @Configuration
   public class IAServicesConfiguration {

     @Bean
     @Profile("ia-stub")
     public IABannerService iaBannerServiceStub() {
       return new IABannerServiceStub();
     }

     @Bean
     @Profile("!ia-stub")
     public IABannerService iaBannerService() {
       return new IABannerServiceImpl();
     }
   }
   ```

2. `IaCacheConfiguration` is already a `@Configuration` class. Add `@Profile` annotations directly
   to it (or create an importing config):
    - Active for `qa`, `prod`, or (`!prod & !qa & caching-enabled`)
    - The nested profile logic in XML is complex — simplify using Spring's profile expression
      syntax:
      `@Profile({"qa", "prod", "caching-enabled"})`

3. Remove `<import resource="classpath:ia-services-config.xml"/>` from `webcert-config.xml` (line
   37)

4. Add the new `IAServicesConfiguration` to component scanning or `@Import`

5. Delete `infra/src/main/resources/ia-services-config.xml`

**Verify:** `./gradlew test` + start. IA banner service loads correctly for each profile. Cache
configuration activates in `qa`/`prod`/`caching-enabled` profiles.

---

## Step 4 — Replace `srs-services-config.xml` with Java `@Configuration`

**Goal:** Replace the 7 JAXWS client proxies and the `srsService` bean with Java configuration.

**Current XML** (`infra/src/main/resources/srs-services-config.xml`):

- 7 `<jaxws:client>` beans (CXF JAXWS proxy factories)
- 1 `<bean>` for `SrsInfraServiceImpl`

**What to do:**

1. Create `infra/src/main/java/.../infra/srs/config/SrsServicesConfiguration.java`:
   ```java
   @Configuration
   public class SrsServicesConfiguration {

     @Bean
     public GetSRSInformationResponderInterface srsClient(
         @Value("${srs.getsrsinformation.endpoint.url}") String address) {
       JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
       factory.setServiceClass(GetSRSInformationResponderInterface.class);
       factory.setAddress(address);
       return (GetSRSInformationResponderInterface) factory.create();
     }

     // Repeat for the other 6 JAXWS clients:
     // prediktionQuestionBean, getConsentBean, setConsentBean,
     // getDiagnosisCodesBean, getSrsForDiagnosisBean, setOwnOpinionBean

     @Bean
     public SrsInfraService srsService() {
       return new SrsInfraServiceImpl();
     }
   }
   ```

2. Remove `<import resource="classpath:srs-services-config.xml"/>` from `webcert-config.xml`
   (line 38)

3. Add the new configuration to component scanning or `@Import`

4. Delete `infra/src/main/resources/srs-services-config.xml`

**Verify:** `./gradlew test` + start. SRS integration works — predictions, consent, diagnosis codes
all function correctly.

---

## Step 5 — Replace `srs-stub-context.xml` with Java `@Configuration`

**Goal:** Replace the SRS stub JAXWS/JAXRS endpoint registrations with Java configuration.

**Current XML** (`infra/src/main/resources/srs-stub-context.xml`):

- 6 `<jaxws:endpoint>` stubs (profiles: `dev,wc-all-stubs,wc-srs-stub`)
- 1 `<jaxrs:server>` for statistics image stub
- 2 supporting beans (`consentRepository`, `statisticsImageStub`)

**What to do:**

1. Create `infra/src/main/java/.../infra/srs/stub/config/SrsStubConfiguration.java`:
   ```java
   @Configuration
   @Profile({"dev", "wc-all-stubs", "wc-srs-stub"})
   public class SrsStubConfiguration {

     @Bean
     public Endpoint getConsentEndpoint(Bus bus) {
       EndpointImpl ep = new EndpointImpl(bus, new GetConsentStub());
       ep.publish("/stubs/get-consent");
       return ep;
     }
     // ... repeat for 5 other JAXWS endpoints

     @Bean
     public Server srsStatisticsStubServer(Bus bus, JacksonJsonProvider jacksonJsonProvider) {
       JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
       factory.setBus(bus);
       factory.setAddress("/stubs/srs-statistics-stub");
       factory.setServiceBean(new StatisticsImageStub());
       factory.setProviders(List.of(jacksonJsonProvider));
       return factory.create();
     }

     @Bean
     public ConsentRepository consentRepository() {
       return new ConsentRepository();
     }
   }
   ```

2. Remove `<import resource="classpath:srs-stub-context.xml"/>` from
   `services-cxf-servlet.xml` (line 39)

3. Delete `infra/src/main/resources/srs-stub-context.xml`

**Verify:** `./gradlew test` + start with `dev` profile. SRS stub endpoints respond at
`/services/stubs/getsrs`, etc.

---

## Step 6 — Replace `ia-stub-context.xml` with Java `@Configuration`

**Goal:** Replace the IA stub JAX-RS endpoint registration with Java configuration.

**Current XML** (`infra/src/main/resources/ia-stub-context.xml`):

- 1 `<jaxrs:server>` at `/api/ia-api` (profiles: `dev,ia-stub`)
- 1 bean `IAStubRestApi`

**What to do:**

1. Create `infra/src/main/java/.../infra/ia/stub/config/IAStubConfiguration.java`:
   ```java
   @Configuration
   @Profile({"dev", "ia-stub"})
   public class IAStubConfiguration {

     @Bean
     public Server iaStubServer(Bus bus, JacksonJsonProvider jacksonJsonProvider) {
       JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
       factory.setBus(bus);
       factory.setAddress("/api/ia-api");
       factory.setServiceBean(new IAStubRestApi());
       factory.setProviders(List.of(jacksonJsonProvider));
       return factory.create();
     }
   }
   ```

2. Remove `<import resource="classpath:ia-stub-context.xml"/>` from `services-cxf-servlet.xml`
   (line 38)

3. Delete `infra/src/main/resources/ia-stub-context.xml`

**Verify:** `./gradlew test` + start with `dev` profile. IA stub API responds at
`/services/api/ia-api`.

---

## Step 7 — Clean up `logback-ocp-base.xml` and infra-only monitoring classes

**Goal:** Remove or relocate the logback base config that is only used by infra tests.

**Current state:**

- `infra/src/main/resources/logback-ocp-base.xml` — defines 6 appenders using infra monitoring
  classes (`MarkerFilter`, `UserPatternLayout`)
- Only referenced by `infra/src/test/resources/logback-test.xml`
- NOT used by production logging (web module uses `logback-spring-base.xml` with ECS encoder)

**What to do:**

1. Move `logback-ocp-base.xml` from `infra/src/main/resources/` to `infra/src/test/resources/`
   (it's only used for tests)

2. Evaluate whether the infra monitoring logging classes (`MarkerFilter`, `UserPatternLayout`,
   `UserConverter`) are still needed at all:
    - If only for infra tests → keep but acknowledge they're test-only
    - If the test behavior they validate is adequately covered by web module tests → consider
      deleting the infra tests and these classes

**Verify:** `./gradlew test` — all tests still pass. Production logging unchanged.

---

## Step 8 — Clean up `MonitoringConfiguration` (dead `@Configuration`)

**Goal:** Remove or properly integrate `MonitoringConfiguration`.

**Current state:**

- `infra/src/main/java/.../monitoring/MonitoringConfiguration.java` is a `@Configuration` class
  defining `MetricsServlet`, `LogMDCServletFilter`, and `LogMDCHelper` beans
- It is NOT loaded by any component scan or XML import in production
- Only referenced in 3 infra test classes (`LogbackTest`, `MarkerFilterTest`, `TimeMethodTest`)
- The production app uses `LoggingConfig.java` (in web module) which separately enables
  `@EnablePrometheusTiming` and has its own beans

**What to do:**

1. Verify that the beans defined in `MonitoringConfiguration` are not needed in production:
    - `LogMDCServletFilter` — web.xml registers `se.inera.intyg.webcert.logging.MdcServletFilter`
      (a different class in the `logging` module), NOT the infra one. Confirmed not needed.
    - `LogMDCHelper` — check if it's `@Autowired` anywhere outside infra. If only used in infra
      tests, it's test-only.
    - `MetricsServlet` — Prometheus servlet. Check if metrics work without it (they should if
      `LoggingConfig` handles Prometheus setup).

2. If confirmed dead: delete `MonitoringConfiguration.java` and update the 3 test classes that
   reference it (or delete those tests if they only test the configuration loading itself).

3. If any beans are actually needed: add proper component scanning or `@Import` to load them.

**Verify:** `./gradlew test` + start. Prometheus metrics still available. MDC logging still works.

---

## Step 9 — Audit and clean up infra test classes

**Goal:** Ensure infra test classes are valuable and not just testing dead code.

**Current state:** 82 test classes in `infra/src/test/java/`. These include:

- Monitoring/logging tests (4) — test `MarkerFilter`, `UserAgentParser`, `TimeMethod`, logback
- Security filter tests (2) — test `SessionTimeoutFilter`, `PrincipalUpdatedFilter`
- HSA integration tests (37) — test REST clients, converters, services
- PU integration tests — test PU REST clients
- Sjukfall engine tests — test calculation logic
- SRS tests — test SRS service
- XML signature tests — test signing logic

**What to do:**

1. Run `./gradlew :infra:test` in isolation to confirm all infra tests pass

2. Review tests for classes deleted in step 10 cleanup (e.g. `DiagnosedCertificateBuilder`,
   `SickLeaveCertificateBuilder`) — delete corresponding test classes if they exist

3. For tests referencing `MonitoringConfiguration` (step 8): update or remove as decided above

4. Verify no test depends on deleted `logback-dev-base.xml` (already removed)

**Verify:** `./gradlew :infra:test` — all remaining tests pass.

---

## Execution Order

Steps 1–3 are independent and can be done in any order (or in parallel).
Steps 4–6 involve CXF endpoint configuration and are more complex.
Steps 7–9 are cleanup tasks.

**Recommended order:** 1 → 2 → 3 → 4 → 5 → 6 → 7 → 8 → 9

After each step: `./gradlew test` + verify the application starts.

---

## Risk Assessment

| Step                        | Risk    | Notes                                                                                 |
|-----------------------------|---------|---------------------------------------------------------------------------------------|
| 1 (basic-cache-config)      | Medium  | Profile logic must be preserved exactly — wrong profiles = no caching or missing NoOp |
| 2 (xmldsig-config)          | Low     | Simple bean definitions, no complex wiring                                            |
| 3 (ia-services-config)      | Low     | Simple profile-conditional beans; IaCacheConfiguration already Java                   |
| 4 (srs-services-config)     | Medium  | 7 JAXWS client proxies — must get `JaxWsProxyFactoryBean` wiring right                |
| 5 (srs-stub-context)        | Medium  | CXF endpoint registration in Java is verbose; profile-gated                           |
| 6 (ia-stub-context)         | Low     | Single JAX-RS endpoint, profile-gated                                                 |
| 7 (logback-ocp-base)        | Trivial | File relocation only                                                                  |
| 8 (MonitoringConfiguration) | Low     | Confirmed not loaded in production                                                    |
| 9 (infra tests)             | Low     | Audit only                                                                            |

---

## Files Created/Modified Per Step

### Step 1

- **Modify:** `web/src/main/java/.../web/config/CacheConfig.java`
- **Modify:** `web/src/main/resources/webcert-config.xml` (remove line 36)
- **Delete:** `infra/src/main/resources/basic-cache-config.xml`

### Step 2

- **Create:** `infra/src/main/java/.../infra/xmldsig/config/XmlDSigConfiguration.java`
- **Modify:** `web/src/main/resources/webcert-config.xml` (remove line 39)
- **Delete:** `infra/src/main/resources/xmldsig-config.xml`

### Step 3

- **Create:** `infra/src/main/java/.../infra/ia/config/IAServicesConfiguration.java`
- **Modify:** `infra/src/main/java/.../infra/ia/cache/IaCacheConfiguration.java` (add `@Profile`)
- **Modify:** `web/src/main/resources/webcert-config.xml` (remove line 37)
- **Delete:** `infra/src/main/resources/ia-services-config.xml`

### Step 4

- **Create:** `infra/src/main/java/.../infra/srs/config/SrsServicesConfiguration.java`
- **Modify:** `web/src/main/resources/webcert-config.xml` (remove line 38)
- **Delete:** `infra/src/main/resources/srs-services-config.xml`

### Step 5

- **Create:** `infra/src/main/java/.../infra/srs/stub/config/SrsStubConfiguration.java`
- **Modify:** `web/src/main/webapp/WEB-INF/services-cxf-servlet.xml` (remove line 39)
- **Delete:** `infra/src/main/resources/srs-stub-context.xml`

### Step 6

- **Create:** `infra/src/main/java/.../infra/ia/stub/config/IAStubConfiguration.java`
- **Modify:** `web/src/main/webapp/WEB-INF/services-cxf-servlet.xml` (remove line 38)
- **Delete:** `infra/src/main/resources/ia-stub-context.xml`

### Step 7

- **Move:** `infra/src/main/resources/logback-ocp-base.xml` → `infra/src/test/resources/`

### Step 8

- **Delete or modify:** `infra/src/main/java/.../monitoring/MonitoringConfiguration.java`
- **Modify:** Affected test classes

### Step 9

- **Delete:** Any orphaned test classes
- **Modify:** Tests referencing deleted classes
