# Webcert — Spring Boot 4 Lifecycle Migration Progress

**Jira:** K1J-2001

---

## Phase status

| Phase | Status | Commit |
|---|---|---|
| Phase 0 — Baseline documentation | ✅ Done | `K1J-2001: Add lifecycle migration plan and progress tracking documents` |
| Phase 1 — Gradle 9.6.0 wrapper | ✅ Done | `K1J-2001: Upgrade Gradle wrapper to 9.6.0` |
| Phase 2 — intyg-bom 1.0.0.16 + CI images | ✅ Done | `K1J-2001: Bump intyg-bom to 1.0.0.16 and update builder/runtime image tags` |
| Phase 3 — Spring Boot 4 starters + OpenSaml5 | ✅ Done | `K1J-2001: Adapt build and Spring Boot 4 starters for modular dependency layout` |
| Phase 4 — Jackson 3 migration | ✅ Done | `K1J-2001: Migrate application code and dependencies to Jackson 3` |
| Phase 5 — Autoconfig audit | ✅ Done | `K1J-2001: Align manual configuration with Spring Boot 4 autoconfiguration best practices` |
| Phase 6 — Integration tests + dependency audit | ⬜ Pending | |
| Phase 7 — WebClient auto-config | N/A — project uses RestClient | |
| Phase 8 — Final sign-off | ⬜ Pending | |
| Phase 9 — Friendliness audit | ⬜ Pending | |

---

## Phase log

### Phase 0 — Baseline documentation

**Status:** ✅ Done

**Baseline build:** Spring Boot 3.x / Java 21 / Gradle 8.14.4 / Jackson 2.x / intyg-bom 1.0.0.14

**Inventory findings:**
- 13 modules; only `web` is a Boot app
- ~40 main source files with non-annotation Jackson imports across web, infra, notification-sender, common, stubs, analytics
- `OpenSaml4` in use — needs upgrade to `OpenSaml5` (Spring Security 7)
- `MappingJackson2MessageConverter` in analytics config — needs `JacksonJsonMessageConverter`
- `spring-session-data-redis` raw dep — needs `spring-boot-starter-session-data-redis`
- `spring.session.redis.namespace` property — needs rename
- SBOM: `sbom.aggregation.module=webcert-web` (path `web`) — `cyclonedxBom` output override required
- `cyclonedxDirectBom` in root `build.gradle` uses `tasks.matching` — needs Gradle 9 fix
- No WebClient beans (project uses RestClient) — Phase 7 N/A
- No Testcontainers usage — no IT module changes needed
- Apache Camel in `notification-sender` — manual JMS config kept as accepted deviation

---

### Phase 1 — Gradle 9.6.0 wrapper

**Status:** ✅ Done

**Changes:**
- `gradle/wrapper/gradle-wrapper.properties`: `8.14.4` → `9.6.0`
- Root `build.gradle`: replaced deprecated `tasks.matching { cyclonedxDirectBom }` with `pluginManager.withPlugin('org.cyclonedx.bom')` pattern for Gradle 9 compat

---

### Phase 2 — intyg-bom 1.0.0.16 + CI images

**Status:** ✅ Done

**Changes:**
- `gradle.properties`: `intygBomVersion=1.0.0.14` → `1.0.0.16`
- `Jenkins.properties`: `builder.image.tag=21.0.6` → `25.0.3`, `runtime.image.tag=21.0.2` → `25.0.1`
- `Jenkins.properties`: `dependencies.common.version=4.3.0.+` → `4.4.0.2`, `dependencies.common.version.resolved=4.3.0.2` → `4.4.0.2`
- `web/build.gradle`: added `cyclonedxBom` output override to restore `bom.json` + `bom.xml` for CI pipeline

---

### Phase 3 — Spring Boot 4 starters + OpenSaml5

**Status:** ✅ Done

**Changes:**
- `web/build.gradle`: `spring-boot-starter-web` → `spring-boot-starter-webmvc`
- `web/build.gradle`: `spring-session-data-redis` → `spring-boot-starter-session-data-redis`
- `web/build.gradle`: added `runtimeOnly("org.springframework.boot:spring-boot-properties-migrator")`
- `web/src/main/resources/application.properties`: `spring.session.redis.namespace` → `spring.session.data.redis.namespace`
- `WebSecurityConfig.java`: `OpenSaml4AuthenticationProvider` → `OpenSaml5AuthenticationProvider`
- `WebSecurityConfig.java`: `OpenSaml4AuthenticationRequestResolver` → `OpenSaml5AuthenticationRequestResolver`
- `WebSecurityConfig.java`: `OpenSaml4LogoutRequestResolver` → `OpenSaml5LogoutRequestResolver`
- Updated method `getOpenSaml4AuthenticationProvider()` → `getOpenSaml5AuthenticationProvider()`

---

### Phase 4 — Jackson 3 migration

**Status:** ✅ Done

**Changes:**
- All `com.fasterxml.jackson.{core,databind,datatype}.*` imports migrated to `tools.jackson.*` across ~36 main source files and ~22 test files
- `CertificateAnalyticsServiceIntegrationConfig.java`: `MappingJackson2MessageConverter` → `JacksonJsonMessageConverter`; injected `JsonMapper mapper`
- `WebMvcConfiguration.java`: `MappingJackson2HttpMessageConverter` → `JacksonHttpMessageConverter` (Spring 7 rename); `JsonMapper` injection
- `JsonProcessingException` → `JacksonException` throughout (unchecked)
- `JavaTimeModule` kept in manually-constructed `JsonMapper.builder()` instances (`IntygUser.java`, `GetUnitNotificationConfig.java`) — Boot auto-config does not apply to manually constructed mappers
- Test files: `throws JsonProcessingException` → `throws JacksonException`, `catch (JsonProcessingException e)` → `catch (JacksonException e)`
- `NotificationCamelConfig.java`: `JacksonDataFormat(objectMapper, ...)` injection updated to `JsonMapper`

---

### Phase 5 — Autoconfig audit

**Status:** ✅ Done

**Redis cache:** `BasicCacheConfiguration` → **accepted deviation DEV-3** maintained. The `CacheFactory` (extends `RedisCacheManager`) + `RedisCacheOptionsSetter` pattern supports dynamic cache creation at runtime. Refactoring to `RedisCacheManagerBuilderCustomizer` would require significant API changes to `RedisCacheOptionsSetter`; kept as-is and documented.

**Properties migrator:** `runtimeOnly("org.springframework.boot:spring-boot-properties-migrator")` added to `web/build.gradle` in Phase 3. Migrator will report further renames at startup during Phase 6.

**Verified property renames applied:**
- `spring.session.redis.namespace` → `spring.session.data.redis.namespace` (done in Phase 3)

**Other properties checked:** `spring.session.store-type=redis`, `spring.session.servlet.filter-order=1` — no Boot 4 renames confirmed.

---

### Phase 6 — Integration tests + dependency audit

**Status:** ⬜ Pending

---

### Phase 7 — WebClient auto-config

**Status:** N/A — project uses `RestClient` throughout; no `WebClient` beans exist

---

### Phase 8 — Final sign-off

**Status:** ⬜ Pending

---

### Phase 9 — Friendliness audit

**Status:** ⬜ Pending

---

## Accepted deviations

| ID | Area | Deviation | Rationale |
|---|---|---|---|
| DEV-1 | `notification-sender` JMS/Camel | Manual `@Bean JmsTemplate` and `@Bean JmsListenerContainerFactory` kept | Custom queues, transacted sessions, Apache Camel routes; no `JmsTemplateCustomizer` in Boot 4.1 |
| DEV-2 | analytics integration `JmsTemplate` | Manual `@Bean` kept | Named bean, custom queue property, transacted session |
| DEV-3 | `BasicCacheConfiguration` | `@Bean RedisCacheManager` — to be evaluated in Phase 5 | Kept if refactor out of scope |
| DEV-4 | `aspectjweaver` in library modules | Raw dep kept in `infra`, `logging` | Not Boot app modules |

---

## Deferred items

| Item | Reason | Ticket |
|---|---|---|
| RestClient migration (sync HTTP) | Already in use — no further migration in scope | Separate ticket |
| RestTestClient IT migration | No IT module found | Separate ticket if ITs added |
| Large DTO primitive → wrapper conversion | Out of scope; `FAIL_ON_NULL_FOR_PRIMITIVES` impact assessed in Phase 4 | Separate ticket |

---

## Lessons learned

*(to be filled in as phases complete)*
