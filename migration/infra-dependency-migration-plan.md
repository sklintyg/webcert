# Infra Dependency Migration Plan

## Problem Statement

The `infra` library (`se.inera.intyg.infra`) is used across four modules in webcert. As part of the incremental migration to Spring Boot (see `migration/incremental-migration-plan.md`), Steps 2–10 progressively inline or replace these dependencies so they can all be removed in one clean Step 10 sweep.

This plan details exactly **which `infra` dependency is removed in which step and from which `build.gradle` file**.

---

## Infra Dependencies by Module (Current State)

### `web/build.gradle` — 25 infra entries

| Scope | Dependency |
|-------|------------|
| implementation | `se.inera.intyg.infra:certificate` |
| implementation | `se.inera.intyg.infra:common-redis-cache-core` |
| implementation | `se.inera.intyg.infra:driftbanner-dto` |
| implementation | `se.inera.intyg.infra:dynamiclink` |
| implementation | `se.inera.intyg.infra:hsa-integration-api` |
| implementation | `se.inera.intyg.infra:hsa-integration-intyg-proxy-service` |
| implementation | `se.inera.intyg.infra:ia-integration` |
| implementation | `se.inera.intyg.infra:integreradeenheter` |
| implementation | `se.inera.intyg.infra:intyginfo` |
| implementation | `se.inera.intyg.infra:log-messages` |
| implementation | `se.inera.intyg.infra:message` |
| implementation | `se.inera.intyg.infra:monitoring` |
| implementation | `se.inera.intyg.infra:postnummerservice-integration` |
| implementation | `se.inera.intyg.infra:privatepractitioner` |
| implementation | `se.inera.intyg.infra:pu-integration-api` |
| implementation | `se.inera.intyg.infra:security-authorities` |
| implementation | `se.inera.intyg.infra:security-common` |
| implementation | `se.inera.intyg.infra:security-filter` |
| implementation | `se.inera.intyg.infra:security-siths` |
| implementation | `se.inera.intyg.infra:sjukfall-engine` |
| implementation | `se.inera.intyg.infra:srs-integration` |
| implementation | `se.inera.intyg.infra:testcertificate` |
| implementation | `se.inera.intyg.infra:xmldsig` |
| runtimeOnly | `se.inera.intyg.infra:pu-integration-intyg-proxy-service` |

### `notification-sender/build.gradle` — 5 infra entries

| Scope | Dependency |
|-------|------------|
| implementation | `se.inera.intyg.infra:hsa-integration-api` |
| implementation | `se.inera.intyg.infra:monitoring` |
| implementation | `se.inera.intyg.infra:pu-integration-api` |
| implementation | `se.inera.intyg.infra:security-authorities` |
| runtimeOnly | `se.inera.intyg.infra:pu-integration-intyg-proxy-service` |

### `common/build.gradle` — 1 infra entry

| Scope | Dependency |
|-------|------------|
| implementation | `se.inera.intyg.infra:log-messages` |

### `stubs/notification-stub/build.gradle` — 1 infra entry

| Scope | Dependency |
|-------|------------|
| implementation | `se.inera.intyg.infra:common-redis-cache-core` |

---

## Migration Steps and Their Infra Dependencies

### Step 2 — Inline simple DTO-only modules (web/build.gradle)

Copy classes locally and switch imports. Infra deps stay on classpath until Step 10.

| Infra Module | Key Classes to Copy | Used In (~files) |
|---|---|---|
| `testcertificate` | `TestCertificateEraseRequest`, `TestCertificateEraseResult` | ~2 |
| `message` | `MessageFromIT` | ~4 |
| `integreradeenheter` | `IntegratedUnitDTO` | ~2 |
| `driftbanner-dto` | `Application`, `Banner` | ~5 |
| `dynamiclink` | `DynamicLink`, `DynamicLinkService` | ~3 |
| `certificate` | `CertificateListEntry`, `CertificateListRequest`, `CertificateListResponse` etc. | ~12 |
| `intyginfo` | `WcIntygInfo`, `ItIntygInfo`, `IntygInfoEvent`, `IntygInfoEventType` | ~22 |

### Step 3 — Inline `log-messages` (web/build.gradle + common/build.gradle)

Copy `PdlLogMessage`, `ActivityType`, `ActivityPurpose`, `Patient`, `Enhet`, `PdlResource`, `ResourceType` into `common` module. Update imports in both `web` and `common`.

| Infra Module | File |
|---|---|
| `log-messages` | `web/build.gradle` |
| `log-messages` | `common/build.gradle` |

### Step 4 — Inline `monitoring` (web/build.gradle + notification-sender/build.gradle)

Copy `@PrometheusTimeMethod`, its AOP aspect, `@EnablePrometheusTiming`, `UserAgentParser`, `UserAgentInfo`, `LogMDCHelper`. Update `LoggingConfig.java`.

| Infra Module | File |
|---|---|
| `monitoring` | `web/build.gradle` |
| `monitoring` | `notification-sender/build.gradle` |

### Step 5 — Inline `xmldsig` (web/build.gradle)

Copy `IntygXMLDSignature`, `IntygSignature`, `ValidationResponse`, `PrepareSignatureService`, `XMLDSigService`, `FakeSignatureService`, `PartialSignatureFactory`. Replace `classpath:xmldsig-config.xml`.

| Infra Module | File |
|---|---|
| `xmldsig` | `web/build.gradle` |

### Step 6 — Inline sjukfall + ia-integration + postnummerservice (web/build.gradle)

| Infra Module | Key Classes | File |
|---|---|---|
| `sjukfall-engine` | `IntygData`, `SjukfallEnhet`, `Formaga`, `DiagnosKod`, `SjukfallEngineService` | `web/build.gradle` |
| `ia-integration` | `IABannerService`, `BannerJob` | `web/build.gradle` |
| `postnummerservice-integration` | postal code service classes | `web/build.gradle` |

### Step 7 — Inline `srs-integration` (web/build.gradle)

Copy `SrsCertificate`, `SrsResponse`, `SrsQuestion`, `SrsPrediction`, `SrsInfraService` (~28 files). Replace `classpath:srs-services-config.xml` and `classpath:srs-stub-context.xml`.

| Infra Module | File |
|---|---|
| `srs-integration` | `web/build.gradle` |

### Step 8 — Inline security modules (web/build.gradle + notification-sender/build.gradle)

This is the largest and highest-risk step (~267+ files, plus ~1,158 csintegration files).

| Infra Module | Key Classes | Files affected |
|---|---|---|
| `security-common` | `HoSPerson`, `CareUnit`, `CareProvider`, `User`, `UserOrigin`, `AuthenticationMethod`, `IntygUser`, `Vardenhet`, `Mottagning`, `Vardgivare`, `SelectableVardenhet`, `IneraCookieSerializer` | `web/build.gradle`, `notification-sender/build.gradle`; ~1,158 csintegration files |
| `security-authorities` | `SecurityConfigurationLoader`, `CommonAuthoritiesResolver`, `AuthoritiesHelper`, role/privilege enums, `AuthoritiesConstants`, `Feature`, `Role` | `web/build.gradle`, `notification-sender/build.gradle` |
| `security-siths` | `BaseUserDetailsService` (or refactor `WebcertUserDetailsService`) | `web/build.gradle` |
| `security-filter` | `SessionTimeoutFilter`, `InternalApiFilter`, `RequestContextHolderUpdateFilter`, `PrincipalUpdatedFilter`, `SecurityHeadersFilter` | `web/build.gradle` |

### Step 9 — Replace HSA/PU integrations with REST clients (web + notification-sender)

Instead of inlining, replace with local REST clients using Spring `RestClient`/`RestTemplate`.

| Infra Module | Replacement | File |
|---|---|---|
| `hsa-integration-api` | Local HSA REST client + local DTOs | `web/build.gradle`, `notification-sender/build.gradle` |
| `hsa-integration-intyg-proxy-service` | (bundled in REST client above) | `web/build.gradle` |
| `pu-integration-api` | Local PU REST client + local DTOs | `web/build.gradle`, `notification-sender/build.gradle` |
| `pu-integration-intyg-proxy-service` | (bundled in REST client above) | `web/build.gradle` (runtimeOnly), `notification-sender/build.gradle` (runtimeOnly) |

Remove XML imports: `classpath:/hsa-integration-intyg-proxy-service-config.xml`, `classpath:/pu-integration-intyg-proxy-service-config.xml`.

### Step 10 — Inline remaining modules + remove ALL infra deps

Handle the remaining two modules and then remove all `se.inera.intyg.infra` lines from all build.gradle files.

| Infra Module | Action | File |
|---|---|---|
| `privatepractitioner` | Inline into `integration-private-practitioner-service` module | `web/build.gradle` |
| `common-redis-cache-core` | Replace `classpath:basic-cache-config.xml` with direct Redis config, keep `CacheConfig`, `RedisLaunchIdCacheConfiguration`, `CertificatesForPatientCacheConfiguration` beans | `web/build.gradle`, `stubs/notification-stub/build.gradle` |

**Final sweep — delete these lines:**
- `web/build.gradle`: all ~25 `se.inera.intyg.infra:*` lines
- `notification-sender/build.gradle`: all ~5 `se.inera.intyg.infra:*` lines
- `common/build.gradle`: 1 `se.inera.intyg.infra:log-messages` line
- `stubs/notification-stub/build.gradle`: 1 `se.inera.intyg.infra:common-redis-cache-core` line

Remove `infraVersion` property from root `build.gradle`.

**Verify:** `grep -r "se.inera.intyg.infra" --include="build.gradle"` returns nothing. `./gradlew build` passes.

---

## Dependency Removal Schedule Summary

| Step | Infra modules being inlined/replaced | Modules/files where deps live |
|------|-------------------------------------|-------------------------------|
| **2** | testcertificate, message, integreradeenheter, driftbanner-dto, dynamiclink, certificate, intyginfo | web |
| **3** | log-messages | web, common |
| **4** | monitoring | web, notification-sender |
| **5** | xmldsig | web |
| **6** | sjukfall-engine, ia-integration, postnummerservice-integration | web |
| **7** | srs-integration | web |
| **8** | security-common, security-authorities, security-siths, security-filter | web, notification-sender |
| **9** | hsa-integration-api, hsa-integration-intyg-proxy-service, pu-integration-api, pu-integration-intyg-proxy-service | web, notification-sender |
| **10** | privatepractitioner, common-redis-cache-core | web, notification-sender, common, stubs/notification-stub |

**Note:** Steps 2–9 inline/replace classes but keep the infra dependencies on the classpath (safe fallback). All dependency lines are physically deleted in Step 10 after everything is verified.
