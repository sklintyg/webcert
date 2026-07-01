# Webcert — Spring Boot 4 Lifecycle Migration Plan

**Jira:** K1J-2001
**Branch:** lifecycle/spring-boot-4

---

## Goals (Jira acceptance criteria)

The application must build, pass unit and integration tests, start, and behave as before
while running on:

| Target | Version |
|---|---|
| Java | 25 |
| Spring Boot | 4.1.0 |
| Gradle | 9.6.0 |
| Jackson | 3 (`tools.jackson.*`) |
| intyg-bom | 1.0.0.16 |
| Builder image tag | 25.0.3 |
| Runtime image tag | 25.0.1 |

Also required:

- Updated dependencies and plugins via intyg-bom + project-specific bumps
- Use Spring Boot starters and auto-configuration wherever practical
- No Jackson 2 compatibility layer — full Jackson 3 adoption
- `dependencies.common.version` bumped to 4.4.0.2

---

## Module layout

| Module | Role |
|---|---|
| `web` (webcert-web) | Boot app — main deployable |
| `common` (webcert-common) | Library |
| `infra` | Library — security, caching, integrations |
| `logging` (webcert-logging) | Library — AOP logging |
| `persistence` (webcert-persistence) | Library — JPA |
| `notification-sender` | Library — Apache Camel + JMS |
| `integration/fmb-integration` | Library |
| `integration/integration-api` | Library — API DTOs |
| `integration/servicenow-integration` | Library |
| `integration-certificate-analytics-service` | Library — JMS |
| `integration-private-practitioner-service` | Library |
| `stubs/mail-stub` | Stub |
| `stubs/notification-stub` | Stub |

---

## Version baseline

| Property | Before | After |
|---|---|---|
| `intygBomVersion` | 1.0.0.14 | 1.0.0.16 |
| Gradle wrapper | 8.14.4 | 9.6.0 |
| Java toolchain | 21 | 25 |
| Spring Boot | 3.x | 4.1.0 |
| Jackson | 2.x | 3.x |
| builder.image.tag | 21.0.6 | 25.0.3 |
| runtime.image.tag | 21.0.2 | 25.0.1 |
| dependencies.common.version | 4.3.0.+ | 4.4.0.2 |

---

## Key migration touchpoints

### Build / Gradle
- `gradle/wrapper/gradle-wrapper.properties`: Gradle 8.14.4 → 9.6.0
- Root `build.gradle`: fix `cyclonedxDirectBom` Gradle 9 compat (use `pluginManager.withPlugin`)
- `web/build.gradle`: add `cyclonedxBom` output override for CI (`bom.json` + `bom.xml`)
  - `sbom.aggregation.module=webcert-web` (path `web`)

### Dependency changes (web module)
- `spring-boot-starter-web` → `spring-boot-starter-webmvc`
- `spring-session-data-redis` (raw) → `spring-boot-starter-session-data-redis`
- Add `runtimeOnly("org.springframework.boot:spring-boot-properties-migrator")` temporarily (Phase 3; remove Phase 8)

### Properties renames
- `spring.session.redis.namespace` → `spring.session.data.redis.namespace`

### Spring Security / SAML
- `OpenSaml4AuthenticationProvider` → `OpenSaml5AuthenticationProvider`
- `OpenSaml4AuthenticationRequestResolver` → `OpenSaml5AuthenticationRequestResolver`
- `OpenSaml4LogoutRequestResolver` → `OpenSaml5LogoutRequestResolver`

### Jackson 3 (~40 main source files across web, infra, notification-sender, common, stubs, analytics)
- `com.fasterxml.jackson.databind.*` → `tools.jackson.databind.*`
- `com.fasterxml.jackson.core.*` → `tools.jackson.core.*`
- `com.fasterxml.jackson.datatype.*` → `tools.jackson.datatype.*`
- `com.fasterxml.jackson.annotation.*` — **unchanged**
- `JsonProcessingException` → `JacksonException` (unchecked)
- `MappingJackson2MessageConverter` → `JacksonJsonMessageConverter`
- Remove redundant `JavaTimeModule` registrations (Boot auto-detects)

### Autoconfig audit
- `BasicCacheConfiguration`: manual `@Bean RedisCacheManager` → evaluate `RedisCacheManagerBuilderCustomizer`

---

## N/A items

| Item | Reason |
|---|---|
| Phase 7 (WebClient auto-config) | Project already uses `RestClient` throughout — no `WebClient` beans |
| Testcontainers migration | No Testcontainers usage found |
| wsdl2java plugin | Not used (uses ant `xjc` for XSD generation) |
| RestClient migration | Already in use — no further migration in scope |
| RestTestClient ITs | No integration-test module / Testcontainers ITs found |

---

## Accepted deviations (pre-declared)

| Area | Deviation | Rationale |
|---|---|---|
| `notification-sender` JMS/Camel | Manual `@Bean JmsTemplate` and `@Bean JmsListenerContainerFactory` kept | Custom queues, transacted sessions, Apache Camel routes require isolation; no `JmsTemplateCustomizer` in Boot 4.1 |
| analytics `JmsTemplate` | Manual `@Bean` kept | Named bean, custom queue, transacted session |
| `BasicCacheConfiguration` | `@Bean RedisCacheManager` — refactor evaluated in Phase 5 | Kept as accepted deviation if refactor out of scope |
| `aspectjweaver` in library modules | Raw dep kept in `infra`, `logging` | Not Boot app modules; starter not applicable |

---

## Phase plan

| Phase | Description | Commit |
|---|---|---|
| 0 | Baseline documentation | `K1J-2001: Add lifecycle migration plan and progress tracking documents` |
| 1 | Gradle 9.6.0 wrapper | `K1J-2001: Upgrade Gradle wrapper to 9.6.0` |
| 2 | intyg-bom 1.0.0.16 + CI images | `K1J-2001: Bump intyg-bom to 1.0.0.16 and update builder/runtime image tags` |
| 3 | Spring Boot 4 starters + OpenSaml5 | `K1J-2001: Adapt build and Spring Boot 4 starters for modular dependency layout` |
| 4 | Jackson 3 migration | `K1J-2001: Migrate application code and dependencies to Jackson 3` |
| 5 | Autoconfig audit | `K1J-2001: Align manual configuration with Spring Boot 4 autoconfiguration best practices` |
| 6 | Integration tests + dependency audit | `K1J-2001: Fix integration tests and complete lifecycle dependency audit` |
| 7 | WebClient (N/A) | — |
| 8 | Final sign-off | `K1J-2001: Complete Java 25 and Spring Boot 4.1 lifecycle migration` |
| 9 | Friendliness audit | `K1J-2001: Add Spring Boot 4 friendliness audit and Phase 9 documentation` |

---

## Verification commands

```bash
./gradlew clean build spotlessCheck test
./gradlew testAggregateTestReport
./gradlew :notification-sender:camelTest
```

After boot bump, verify resolved versions:

```bash
./gradlew --version
./gradlew properties | grep -E 'javaVersion|intygBom'
./gradlew :web:dependencies --configuration runtimeClasspath | grep spring-boot
```

---

## References

- [Spring Boot 4.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)
- intyg-bom: `se.inera.intyg.bom:platform:1.0.0.16`
