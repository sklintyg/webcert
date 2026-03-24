# External Module Analysis — Webcert Step 0 Investigation

*Prerequisite investigation for the Spring Boot migration. No code changes — documentation only.*

---

## 1. Introduction

Webcert's root Spring context is bootstrapped by `webcert-config.xml`, which imports XML configurations from **external JARs** not part
of the webcert project. These imports use `classpath:` (single match) and `classpath*:` (wildcard/multi-JAR match) prefixes. Before
migration work begins, every external config must be documented so we know exactly what beans, scans, and endpoints they contribute —
and how to handle each one during the Spring Boot transition.

**Scope of this investigation:**

| Import Pattern | Source | Files Found |
|---------------|--------|-------------|
| `classpath*:module-config.xml` | Certificate modules (`se.inera.intyg.common`) | 17 |
| `classpath*:wc-module-cxf-servlet.xml` | Certificate modules (`se.inera.intyg.common`) | 2 |
| `classpath:common-config.xml` | `se.inera.intyg.common:common-support` | 1 |
| Various `classpath:` imports | `se.inera.intyg.infra` modules | 8 |

**Source projects** (available locally):
- `se.inera.intyg.common` → `C:\GIT\Inera\Intyg\common\`
- `se.inera.intyg.infra` → `C:\GIT\Inera\Intyg\infra\`

---

## 2. Certificate Module Configs (`classpath*:module-config.xml`)

### 2.1 Overview

The wildcard import `<import resource="classpath*:module-config.xml"/>` in `webcert-config.xml` (line 44) loads Spring XML configs from
**every certificate module JAR** on the classpath. Webcert depends on 15 certificate modules plus 2 parent modules (fk-parent,
ts-parent), totaling **17 module-config.xml files**.

### 2.2 Template Pattern

**14 of 17 files follow an identical template:**

```xml
<beans ...>
  <context:annotation-config/>
  <import resource="[module]-beans.xml"/>
  <import resource="[module]-ws-stub.xml"/>
</beans>
```

The imported `-beans.xml` files each contain a **single component scan** of the module's package. The `-ws-stub.xml` files are
**mostly empty shells** — only fk7263 and ts-diabetes had actual JAXWS stub endpoints, and these have already been **migrated to
Java `@Configuration` classes** in the common project (e.g., `Fk7263StubConfig.java`, `FkParentStubConfig.java`).

### 2.3 Per-Module Details

#### Standard Modules (component-scan only)

| Module | Gradle Dependency | Component Scan Package | Explicit Beans |
|--------|------------------|----------------------|----------------|
| af00213 | `se.inera.intyg.common:af00213` | `se.inera.intyg.common.af00213` | None |
| af00251 | `se.inera.intyg.common:af00251` | `se.inera.intyg.common.af00251` | None |
| lisjp | `se.inera.intyg.common:lisjp` | `se.inera.intyg.common.lisjp` | None |
| luae_fs | `se.inera.intyg.common:luae_fs` | `se.inera.intyg.common.luae_fs` | None |
| luae_na | `se.inera.intyg.common:luae_na` | `se.inera.intyg.common.luae_na` | None |
| luse | `se.inera.intyg.common:luse` | `se.inera.intyg.common.luse` | None |
| ag114 | `se.inera.intyg.common:ag114` | `se.inera.intyg.common.ag114` | `ValidatorUtilSKL` (inline) |
| ag7804 | `se.inera.intyg.common:ag7804` | `se.inera.intyg.common.ag7804` | None |
| db | `se.inera.intyg.common:db` | `se.inera.intyg.common.db` | None |
| doi | `se.inera.intyg.common:doi` | `se.inera.intyg.common.doi` | None |
| ts-bas | `se.inera.intyg.common:ts-bas` | `se.inera.intyg.common.ts_bas` | None |
| ts-diabetes | `se.inera.intyg.common:ts-diabetes` | `se.inera.intyg.common.ts_diabetes` | None (has redundant scan) |
| tstrk1009 | `se.inera.intyg.common:tstrk1009` | `se.inera.intyg.common.tstrk1009` | None |
| tstrk1062 | `se.inera.intyg.common:tstrk1062` | `se.inera.intyg.common.tstrk1062` | None |

#### Variant: fk7263 (legacy explicit bean wiring)

`fk7263-beans.xml` is the **only module** that defines beans explicitly instead of component scanning:

| Bean ID | Class | Purpose |
|---------|-------|---------|
| *(anonymous)* | `WebcertModelFactoryImpl` | Model factory |
| `moduleapi.fk7263.v1` | `Fk7263ModuleApi` | REST API for FK7263 |
| *(anonymous)* | `Fk7263ModelCompareUtil` | Model comparison |
| *(anonymous)* | `InternalDraftValidator` | Draft validation |
| *(anonymous)* | `Fk7263EntryPoint` | Module entry point |
| `unitMappingConfigLoader` | `UnitMappingConfigLoader` | Unit mapping config |
| `unitMapperUtil` | `UnitMapperUtil` | Unit mapping utility |
| `internalConverterUtil` | `InternalConverterUtil` | Internal converter |
| `transportToInternal` | `TransportToInternal` | Transport model converter |

#### Variant: Parent Modules (fk-parent, ts-parent)

These define infrastructure beans and profile-activated stubs directly in `module-config.xml`:

**fk-parent:**
- `ValidatorUtilFK` bean
- JAXWS stub endpoint (profile: `dev,it-fk-stub`)

**ts-parent:**
- `RegisterCertificateResponderStub` bean
- `TSCertificateStore` bean
- `TSCertificateStoreRestApi` bean
- JAXWS/JAXRS endpoints (profile: `dev,it-ts-stub,testability-api`)

### 2.4 Migration Recommendation

**Strategy:** Replace `<import resource="classpath*:module-config.xml"/>` with explicit `@ComponentScan` of all certificate module
packages. Since all 14 standard modules just do component scanning, this is a direct 1:1 replacement.

**Option A — Temporary bridge (recommended for initial migration):**
```java
@ImportResource("classpath*:module-config.xml")
```
Keep the XML import via `@ImportResource` during the Spring Boot bootstrap step (Step 14). This preserves backward compatibility
with the common modules and avoids a coordinated release. Remove it later when the common modules provide Java `@Configuration`.

**Option B — Full replacement:**
```java
@ComponentScan(basePackages = {
    "se.inera.intyg.common.af00213",
    "se.inera.intyg.common.af00251",
    "se.inera.intyg.common.ag114",
    "se.inera.intyg.common.ag7804",
    "se.inera.intyg.common.db",
    "se.inera.intyg.common.doi",
    "se.inera.intyg.common.fk7263",
    "se.inera.intyg.common.lisjp",
    "se.inera.intyg.common.luae_fs",
    "se.inera.intyg.common.luae_na",
    "se.inera.intyg.common.luse",
    "se.inera.intyg.common.ts_bas",
    "se.inera.intyg.common.ts_diabetes",
    "se.inera.intyg.common.tstrk1009",
    "se.inera.intyg.common.tstrk1062"
})
```
This requires also handling the fk7263 explicit beans (convert to `@Component`/`@Service` annotations or a `@Configuration` class)
and the parent module beans (validator utilities + stub endpoints under dev profiles).

**Recommendation:** Use Option A initially, then migrate to Option B as part of a coordinated common-modules update.

---

## 3. CXF Servlet Configs (`classpath*:wc-module-cxf-servlet.xml`)

### 3.1 Overview

The wildcard import `<import resource="classpath*:wc-module-cxf-servlet.xml"/>` in `webcert-config.xml` (line 45) loads CXF
servlet configurations from certificate module JARs. Despite 15+ modules on the classpath, **only 2 provide this file:**

- `se.inera.intyg.common:fk7263`
- `se.inera.intyg.common:ts-diabetes`

### 3.2 fk7263 — `wc-module-cxf-servlet.xml`

Defines **2 JAXWS client beans** for calling intygstjänst:

| Client Bean ID | Service Interface | Endpoint URL Property |
|---------------|-------------------|----------------------|
| `registerMedicalCertificateClient` | `RegisterMedicalCertificateResponderInterface` | `${intygstjanst.registermedicalcertificate.endpoint.url}` |
| `getMedicalCertificateResponder` | `GetMedicalCertificateResponderInterface` | `${intygstjanst.getmedicalcertificate.endpoint.url}` |

### 3.3 ts-diabetes — `wc-module-cxf-servlet.xml`

Defines **2 JAXWS client beans** for calling intygstjänst:

| Client Bean ID | Service Interface | Endpoint URL Property |
|---------------|-------------------|----------------------|
| `diabetesGetClient` | `GetTSDiabetesResponderInterface` | `${intygstjanst.gettsdiabetes.endpoint.url}` |
| `diabetesRegisterClient` | `RegisterTSDiabetesResponderInterface` | `${intygstjanst.registertsdiabetes.endpoint.url}` |

### 3.4 Migration Recommendation

These 4 JAXWS client beans use CXF's `<jaxws:client>` namespace element, which requires CXF on the classpath. They **will break**
when CXF REST is removed in Step 11 of the incremental migration plan.

**Migration path:**

1. **Before Step 11:** Convert these to programmatic JAXWS client creation using `JaxWsProxyFactoryBean` in a Java `@Configuration`
   class within the webcert project (not in the common modules):

   ```java
   @Configuration
   public class IntygstjanstWsClientConfig {
       @Bean
       public RegisterMedicalCertificateResponderInterface registerMedicalCertificateClient(
               @Value("${intygstjanst.registermedicalcertificate.endpoint.url}") String url) {
           JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
           factory.setServiceClass(RegisterMedicalCertificateResponderInterface.class);
           factory.setAddress(url);
           return (RegisterMedicalCertificateResponderInterface) factory.create();
       }
       // ... similar for other 3 clients
   }
   ```

2. **Remove** `<import resource="classpath*:wc-module-cxf-servlet.xml"/>` from `webcert-config.xml`.

3. **Coordinate with common team:** These XML files in the common modules become dead code after webcert stops loading them. They
   can be removed from common in a subsequent release.

**⚠️ Critical:** This must be done **before** Step 11 (JAX-RS to Spring MVC migration), as that step removes CXF REST dependencies.
The SOAP client functionality (CXF JAX-WS) is separate from CXF REST, but the XML namespace processing requires CXF core.

---

## 4. Common Config (`classpath:common-config.xml`)

### 4.1 Content

Located in `se.inera.intyg.common:common-support` (`common/support/src/main/resources/common-config.xml`).

**Entire content:**
```xml
<beans ...>
  <context:component-scan base-package="se.inera.intyg.common.support.modules.converter"/>
</beans>
```

A single component scan of the `se.inera.intyg.common.support.modules.converter` package. No beans, no imports, no property
placeholders.

### 4.2 Migration Recommendation

**Trivially replaceable.** Add `se.inera.intyg.common.support.modules.converter` to the `@ComponentScan` base packages, or use
`@ImportResource("classpath:common-config.xml")` as a temporary bridge.

Since `common-support` is already a dependency of all webcert modules, and Spring Boot's `@SpringBootApplication` scans the
application's own package tree, the key question is whether the `se.inera.intyg.common.support.modules.converter` package
contains `@Component`-annotated classes. If it does, they need to be in the scan path.

**Recommended action:** Include in the `@ComponentScan` alongside the certificate module packages.

---

## 5. Infra XML Configs (other `classpath:` imports)

### 5.1 Overview

Beyond the certificate module wildcards, `webcert-config.xml` and `services-cxf-servlet.xml` import 8 XML configs from
`se.inera.intyg.infra` modules.

### 5.2 Trivial — Component Scans Only

These can be **removed entirely** when Spring Boot's auto-scanning covers the packages, or replaced with `@ComponentScan`.

| XML File | Infra Module | Scanned Package | Migration Step |
|----------|-------------|-----------------|---------------|
| `hsa-integration-intyg-proxy-service-config.xml` | hsa-integration-intyg-proxy-service | `se.inera.intyg.infra.integration.intygproxyservice` | Step 14 (Spring Boot bootstrap) |
| `pu-integration-intyg-proxy-service-config.xml` | pu-integration-intyg-proxy-service | `se.inera.intyg.infra.pu.integration.intygproxyservice` | Step 14 (Spring Boot bootstrap) |

### 5.3 Simple — Profile-Based Bean Selection

These define beans conditionally based on Spring profiles. Easily converted to Java `@Configuration` with `@Profile`.

#### `basic-cache-config.xml` (from `common-redis-cache-core`)

```xml
<cache:annotation-driven cache-manager="cacheManager"/>

<beans profile="caching-enabled,prod">
  <bean class="se.inera.intyg.infra.rediscache.core.BasicCacheConfiguration"/>
</beans>
<beans profile="!caching-enabled">
  <beans profile="!prod">
    <bean id="cacheManager" class="org.springframework.cache.support.NoOpCacheManager"/>
  </beans>
</beans>
```

**Migration:** Replace with Spring Boot's `@EnableCaching` + `spring.cache.type` property. `BasicCacheConfiguration` either
becomes a `@Configuration` class with `@ConditionalOnProperty`, or is replaced entirely by Spring Boot Redis cache auto-config.
**Target step:** Step 18 (Redis auto-configuration).

#### `ia-services-config.xml` (from `ia-integration`)

```xml
<beans profile="ia-stub">
  <bean class="IABannerServiceStub"/>
</beans>
<beans profile="!ia-stub">
  <bean class="IABannerServiceImpl"/>
</beans>
<beans profile="qa,prod">
  <bean class="IaCacheConfiguration"/>
</beans>
```

**Migration:** Convert to `@Configuration` class with `@Profile`-annotated `@Bean` methods. **Target step:** Step 8 (infra inlining).

#### `xmldsig-config.xml` (from `xmldsig`)

```xml
<bean id="xmldSigService" class="XMLDSigServiceImpl"/>
<bean id="prepareSignatureService" class="PrepareSignatureServiceImpl"/>
<beans profile="!prod">
  <bean id="fakeSignatureService" class="FakeSignatureServiceImpl"/>
</beans>
<beans profile="prod">
  <bean id="fakeSignatureService" class="FakeSignatureServiceBlocked"/>
</beans>
```

**Migration:** Convert to `@Configuration` class with `@Profile`-annotated `@Bean` methods. **Target step:** Step 5 (inline xmldsig).

### 5.4 Complex — CXF SOAP/REST Endpoints

These use Apache CXF namespace elements (`<jaxws:client>`, `<jaxws:endpoint>`, `<jaxrs:server>`) requiring CXF-specific handling.

#### `srs-services-config.xml` (from `srs-integration`)

Defines **7 JAXWS SOAP client beans** + 1 service implementation:

| Bean ID | Type | Purpose |
|---------|------|---------|
| `srsClient` | `jaxws:client` | Get SRS information |
| `prediktionQuestionBean` | `jaxws:client` | Get prediction questions |
| `getConsentBean` | `jaxws:client` | Get patient consent |
| `setConsentBean` | `jaxws:client` | Set patient consent |
| `getDiagnosisCodesBean` | `jaxws:client` | Get diagnosis codes |
| `getSrsForDiagnosisBean` | `jaxws:client` | Get SRS for diagnosis |
| `setOwnOpinionBean` | `jaxws:client` | Set own opinion |
| `srsService` | `bean` | `SrsInfraServiceImpl` |

**Migration:** Convert `<jaxws:client>` elements to `JaxWsProxyFactoryBean` `@Bean` methods. Inline `SrsInfraServiceImpl` into
webcert or annotate with `@Service`. **Target step:** Step 8 (infra inlining) — must be done before Step 11 (CXF REST removal).

#### `ia-stub-context.xml` (from `ia-integration`)

Profile: `dev,ia-stub`. Defines a CXF REST server at `/api/ia-api` serving `IAStubRestApi`.

**Migration:** Convert `IAStubRestApi` to a Spring MVC `@RestController` with `@Profile("dev | ia-stub")`.
**Target step:** Step 11 (JAX-RS to Spring MVC).

#### `srs-stub-context.xml` (from `srs-integration`)

Profile: `dev,wc-all-stubs,wc-srs-stub`. Defines:
- 6 JAXWS stub endpoints (`/stubs/getsrs`, `/stubs/predictionquestions`, etc.)
- 1 CXF REST server at `/stubs/srs-statistics-stub`
- `consentRepository` and `statisticsImageStub` beans

**Migration:** Convert JAXWS endpoints to programmatic `Endpoint.publish()` in a `@Configuration` class. Convert REST server to
`@RestController`. **Target step:** Step 11 (JAX-RS to Spring MVC) for REST; SOAP stubs can use `Endpoint.publish()`.

---

## 6. Migration Recommendations Summary

### 6.1 Migration Actions by Step

| Migration Step | External Config Action |
|---------------|----------------------|
| **Step 5** (inline xmldsig) | Convert `xmldsig-config.xml` to Java `@Configuration` with `@Profile` beans |
| **Step 8** (inline infra) | Convert `ia-services-config.xml` and `srs-services-config.xml` to Java config; inline service implementations |
| **Step 11** (JAX-RS → Spring MVC) | Convert `ia-stub-context.xml` and `srs-stub-context.xml` CXF REST endpoints to `@RestController` |
| **Step 11** (pre-requisite) | Convert `wc-module-cxf-servlet.xml` JAXWS clients to `JaxWsProxyFactoryBean` beans **before** removing CXF REST |
| **Step 14** (Spring Boot bootstrap) | Replace `webcert-config.xml` with `@SpringBootApplication`; use `@ImportResource` bridge for `classpath*:module-config.xml` and `classpath:common-config.xml` |
| **Step 14** (Spring Boot bootstrap) | Remove `hsa-integration-*-config.xml` and `pu-integration-*-config.xml` (component scans handled by Spring Boot) |
| **Step 18** (Redis auto-config) | Replace `basic-cache-config.xml` with Spring Boot cache auto-configuration |

### 6.2 Bridge Strategy for External JARs

The certificate modules in `se.inera.intyg.common` are shared dependencies across multiple Inera applications (webcert, intygstjänst,
rehabstod, mina intyg). Changing their configuration mechanism requires a **coordinated release**.

**Recommended bridge approach:**
1. Use `@ImportResource("classpath*:module-config.xml")` in webcert's Spring Boot application class
2. Use `@ImportResource("classpath:common-config.xml")` alongside it
3. This preserves full backward compatibility — no changes needed in the common modules
4. Remove the `@ImportResource` annotations in a later step when the common modules provide Java `@Configuration` alternatives

### 6.3 Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| `classpath*:` wildcard import fails under Spring Boot | Low | High | Test early in Step 14; `@ImportResource` supports wildcards |
| CXF SOAP clients break when CXF REST is removed | High | High | Convert to `JaxWsProxyFactoryBean` before Step 11 |
| fk7263 explicit beans conflict with component scanning | Low | Medium | Keep fk7263-beans.xml via `@ImportResource` or convert beans to annotated classes |
| Profile-based bean activation changes behavior | Low | Medium | Verify active profiles in all environments match current config |
| Common module update breaks backward compatibility | Medium | High | Bridge strategy avoids this; coordinate releases when removing bridge |

### 6.4 Local Submodule Imports (Out of Scope)

The following `classpath:` imports in `webcert-config.xml` and `services-cxf-servlet.xml` point to **local webcert submodules**
(confirmed in `settings.gradle`), NOT external JARs. They are handled in **Step 12** (XML to Java conversion) and are not part
of this Step 0 investigation:

| Import | Source Submodule | Handled In |
|--------|-----------------|------------|
| `classpath:webcert-common-config.xml` | `:webcert-common` | Step 12 |
| `classpath:repository-context.xml` | `:webcert-persistence` | Step 12 |
| `classpath:fmb-services-config.xml` | `:fmb-integration` | Step 12 |
| `classpath:servicenow-services-config.xml` | `:servicenow-integration` | Step 12 |
| `classpath:integration-certificate-analytics-service-config.xml` | `:integration-certificate-analytics-service` | Step 12 |
| `classpath:integration-private-practitioner-service-config.xml` | `:integration-private-practitioner-service` | Step 12 |
| `classpath:mail-stub-context.xml` | `:mail-stub` | Step 12 |
| `classpath:notification-sender-config.xml` | `:notification-sender` | Step 13 |
| `classpath:fmb-stub-context.xml` | `:fmb-integration` | Step 12 |
| `classpath:/mail-stub-testability-api-context.xml` | `:mail-stub` | Step 12 |
| `classpath:/notification-stub-context.xml` | `:notification-stub` | Step 12 |
| `classpath:/servicenow-stub-context.xml` | `:servicenow-integration` | Step 12 |
| `classpath:/swagger-api-context.xml` | local file in web module | Step 14 (replaced by SpringDoc) |
| `classpath:/webcert-testability-api-context.xml` | local file in web module | Step 12 |
| `ws-config.xml` (relative) | local file in web resources | Step 12 |
| `mail-config.xml` (relative) | local file in web resources | Step 12 |

### 6.5 Scope Assessment

**No scope expansion needed.** The external JAR contents are simpler than anticipated:
- Most certificate module configs are trivial component scans
- The ws-stub migration is already complete in the common project
- Only 4 JAXWS client beans need special handling
- The `common-config.xml` is a single component scan
- Infra configs are well-understood profile-based bean selections

The incremental migration plan's 20-step structure adequately covers all discovered external dependencies without modification.
