---
name: spring-boot-4-lifecycle-migration
description: >-
  Migrates Inera intyg Java apps from Spring Boot 3.5.x / Gradle 8.14.x / Jackson 2
  to Spring Boot 4.1.0 / Gradle 9.6.0 / Jackson 3 using phased, verifiable steps.
  Use when lifecycle migration, Java 25 upgrade, intyg-bom bump, Jackson 3 migration,
  Gradle 9 upgrade, or Spring Boot 4 migration is requested for intyg projects.
---

# Spring Boot 4 lifecycle migration (Inera intyg)

Generalized workflow from **minaintyg (K1J-1999)**. Adapt per project; do not assume every Jira item applies.

**Primary references**

- [Spring Boot 4.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)
- intyg-bom (target: `1.0.0.16` or later — Java 25, Gradle 9.6.0, Spring Boot 4.1.0)
- Prior migration docs in repo: `LIFECYCLE-MIGRATION-PLAN.md`, `LIFECYCLE-MIGRATION-PROGRESS.md`, `SPRING-BOOT-4-AUDIT.md`

**Detailed starter/API mappings:** [reference.md](reference.md)

---

## Jira acceptance criteria (standard)

The application must build, pass unit and integration tests, start, and behave as before while running on:

| Target | Default version |
| ------ | --------------- |
| Java | 25 |
| Spring Boot (+ BOM) | 4.1.0 (latest stable 4.x at migration time) |
| Gradle | 9.6.0 |
| Jackson | 3 (`tools.jackson.*`) |
| Builder image tag | 25.0.3 |
| Runtime image tag | 25.0.1 |

Also required:

- Updated dependencies and plugins (via intyg-bom + project-specific bumps)
- Use Spring Boot **starters and auto-configuration** wherever practical
- No Jackson 2 compatibility layer — full Jackson 3 adoption

**Jira description goals (interpret per repo):**

- Java 21 → 25, Spring Boot 3 → 4, Gradle 8 → 9
- Replace remaining manual Spring config with Boot starters/autoconfig (ActiveMQ, Camel, etc.) **where the project actually uses them**
- Update builder/runtime images
- Follow the Spring Boot 4 Migration Guide

**Stop and ask the user before assuming:**

- Jira ticket number and commit prefix (`K1J-XXXX:`)
- External schema artifact version bumps
- `dependencies.common.version` applicability (only if project depends on intyg-common)
- Analytics or API `schemaVersion` changes
- Whether Gradle wrapper upgrade is manual (team-owned step)

---

## Migration principles

1. **Phased and verifiable** — green build (or documented expected partial failure) before next phase
2. **Stop between phases** — propose commit message; do not commit unless asked
3. **Minimal behaviour change** — lifecycle migration, not opportunistic refactors
4. **Document deviations** — accepted manual beans, deferred RestClient/RestTestClient, etc.
5. **Project inventory first** — not every Jira bullet applies (wsdl2java, Camel, intyg-common)

---

## Default version targets

| Property | From (typical) | To |
| -------- | -------------- | -- |
| `intygBomVersion` | 1.0.0.14 | 1.0.0.16+ |
| Gradle wrapper | 8.14.4 | 9.6.0 |
| Spring Boot | 3.5.x (via BOM) | 4.1.0 |
| Jackson | 2.x | 3.x |
| Java toolchain | 21 | 25 |

Verify resolved versions after BOM bump:

```bash
./gradlew --version
./gradlew properties | grep -E 'javaVersion|intygBom'
./gradlew :app:dependencies --configuration runtimeClasspath | grep spring-boot
```

---

## Verification commands (after every phase)

```bash
./gradlew clean build spotlessCheck test
# If project has integration-test module:
./gradlew :integration-test:integrationTest
```

Optional: `testAggregateTestReport`, `:app:bootRun` with dev profile, SonarQube CI args.

**Definition of done:** All Jira acceptance criteria met; progress doc complete; properties migrator removed; audit doc (Phase 9) reviewed.

---

## Phase workflow

Copy this checklist and track in `LIFECYCLE-MIGRATION-PROGRESS.md`:

```
- [ ] Phase 0 — Baseline and documentation
- [ ] Phase 1 — Gradle 9 wrapper
- [ ] Phase 2 — intyg-bom + CI images
- [ ] Phase 3 — Spring Boot 4 compile + modular starters
- [ ] Phase 4 — Jackson 3 migration
- [ ] Phase 5 — Autoconfig audit (Redis, JMS, Jackson)
- [ ] Phase 6 — Integration tests + dependency audit
- [ ] Phase 7 — WebClient.Builder + webclient starter
- [ ] Phase 8 — Final sign-off (remove migrator)
- [ ] Phase 9 — Friendliness audit (documentation)
```

### Phase 0 — Baseline

- Add `LIFECYCLE-MIGRATION-PLAN.md` and `LIFECYCLE-MIGRATION-PROGRESS.md`
- Record baseline build result, modules, Jackson/Spring touchpoints
- Resolve open questions (Jira ticket, schema bumps, common dep)
- Commit: `K1J-XXXX: Add lifecycle migration plan and progress tracking documents`

### Phase 1 — Gradle 9 wrapper

- Update `gradle/wrapper/gradle-wrapper.properties` → Gradle 9.6.0
- Fix Gradle 9 breakages (plugins, deprecated APIs)
- **Note:** Team may do wrapper upgrade manually — agent verifies only
- Commit: `K1J-XXXX: Upgrade Gradle wrapper to 9.6.0`

**Known fix:** CycloneDX 3.2.4 + Gradle 9 — replace `tasks.matching { … }.configureEach` with:

```gradle
pluginManager.withPlugin('org.cyclonedx.bom') {
    tasks.named('cyclonedxDirectBom').configure { t ->
        t.includeConfigs = ['^runtimeClasspath$']
    }
}
```

**CycloneDX SBOM output (CI pipeline):** After Boot 4 + CycloneDX 3.x, `cyclonedxBom` no longer writes `bom.json`/`bom.xml` by default — Spring Boot Gradle plugin reconfigures it to output `application.cdx.json` (JSON only) for Actuator/JAR embedding. intyg-bom delivers CycloneDX **3.1.0+** (e.g. 3.2.4); the filename change is **not** from 3.1→3.2.4 but from **Spring Boot 3.3+** reacting to the CycloneDX plugin.

| Task | Default output (Boot 4 + CycloneDX 3.x) |
| ---- | --------------------------------------- |
| `cyclonedxBom` | `build/reports/cyclonedx/application.cdx.json` |
| `cyclonedxDirectBom` | `build/reports/cyclonedx-direct/bom.json` + `bom.xml` |

If Jenkins/CI expects `bom.json` and `bom.xml` from **`cyclonedxBom`** (check `Jenkins.properties` → `sbom.aggregation.module`), restore legacy paths on the **app module** (where SBOM is generated):

```gradle
tasks.named('cyclonedxBom') {
    jsonOutput.set(file("build/reports/cyclonedx/bom.json"))
    xmlOutput.set(file("build/reports/cyclonedx/bom.xml"))
}
```

Apply only on the module referenced by `sbom.aggregation.module` (typically `app`), not on every subproject. Verify `:app:cyclonedxBom` produces both files after the override.

### Phase 2 — intyg-bom + CI images

- `gradle.properties`: `intygBomVersion=1.0.0.16` (or current target)
- `Jenkins.properties`: `builder.image.tag=25.0.3`, `runtime.image.tag=25.0.1`
- Fix **build-level** breakages only; defer Java source fixes
- Compile may fail on Jackson 2 imports — expected until Phase 4
- Commit: `K1J-XXXX: Bump intyg-bom to 1.0.0.16 and update builder/runtime image tags`

### Phase 3 — Spring Boot 4 compile + starters

Replace raw Spring deps with Boot 4 modular starters (see [reference.md](reference.md)):

- `spring-boot-starter-web` → `spring-boot-starter-webmvc`
- `aspectjweaver` → `spring-boot-starter-aspectj`
- `spring-context` + `spring-jms` → `spring-boot-starter-jms` (where applicable)
- Shared library modules: prefer minimal `spring-web` over full webmvc starter

**Properties (common renames):**

- `spring.session.redis.*` → `spring.session.data.redis.*`

**Temporary:** add `runtimeOnly("	")` to app module; remove in Phase 8.

Commit: `K1J-XXXX: Adapt build and Spring Boot 4 starters for modular dependency layout`

### Phase 4 — Jackson 3 migration

**Imports:**

- `com.fasterxml.jackson.databind.*` → `tools.jackson.databind.*`
- `com.fasterxml.jackson.core.*` → `tools.jackson.core.*`
- `com.fasterxml.jackson.datatype.*` → `tools.jackson.datatype.*`
- **Keep** `com.fasterxml.jackson.annotation.*`

**API changes:**

- `MappingJackson2MessageConverter` → `JacksonJsonMessageConverter`
- `GenericJackson2JsonRedisSerializer` → `GenericJacksonJsonRedisSerializer`
- `JsonProcessingException` → `JacksonException` (unchecked)
- Manual construction: `JsonMapper.builder().build()`
- Prefer injected Boot `JsonMapper` bean over manual instances
- Remove redundant `JavaTimeModule` registration where Boot auto-detects

**Primitive deserialization:** Jackson 3 defaults `FAIL_ON_NULL_FOR_PRIMITIVES=true`. Prefer wrapper types (`Boolean`) on DTOs with optional JSON fields. Global `JsonMapperBuilderCustomizer` is an **accepted deviation** only when DTO sweep is out of scope — document it.

**Also check:** Spring Security 7 (`OpenSaml5AuthenticationProvider`), Testcontainers 2.x (`testcontainers-*` module prefix).

Commit: `K1J-XXXX: Migrate application code and dependencies to Jackson 3`

### Phase 5 — Autoconfig audit

| Area | Anti-pattern | Preferred Boot 4 pattern |
| ---- | ------------ | ------------------------ |
| Redis cache | `@Bean RedisCacheManager` | Boot cache auto-config + `RedisCacheManagerBuilderCustomizer` + `@EnableCaching` |
| Jackson | Project-wide customizers | Targeted DTO fixes; `JsonMapperBuilderCustomizer` only when justified |
| JMS | Manual everything | Keep manual `@Bean` when queue name + transacted session + custom converter need isolation; **no `JmsTemplateCustomizer` in Boot 4.1** |
| WebClient | `WebClient.builder()` | Deferred to Phase 7 |

Run properties migrator once; apply reported renames only.

Commit: `K1J-XXXX: Align manual configuration with Spring Boot 4 autoconfiguration best practices`

### Phase 6 — Integration tests

- Fix Testcontainers module names (`testcontainers-activemq`, etc.)
- Session: use `spring-boot-starter-session-data-redis` (raw `spring-session-data-redis` insufficient in Boot 4)
- IT HTTP: `@AutoConfigureTestRestTemplate` + `spring-boot-starter-restclient` + `spring-boot-resttestclient`
- Cookie parsing: exact `SESSION=` match in `Set-Cookie`, not `contains("SESSION")`
- `RestTestClient` migration is **optional/deferred** if session/cookie issues

Commit: `K1J-XXXX: Fix integration tests and complete lifecycle dependency audit`

### Phase 7 — WebClient auto-config

**Critical:** `WebClient.Builder` is **not** auto-configured by `spring-boot-starter-webmvc` alone.

- Add `spring-boot-starter-webclient` to app and modules defining WebClient beans
- Replace `spring-boot-starter-webflux` with `webclient` when only HTTP client is needed (no reactive server)
- Refactor `@Bean WebClient` to inject `WebClient.Builder`, apply only module filters/customisation, then `.build()`

Commit: `K1J-XXXX: Align integration WebClients with Spring Boot 4 auto-configuration`

### Phase 8 — Final sign-off

- Full build + tests green
- Remove `spring-boot-properties-migrator`
- Confirm Java 25, Boot 4.1.0, Gradle 9.6.0, Jackson 3 resolved
- Commit: `K1J-XXXX: Complete Java 25 and Spring Boot 4.1 lifecycle migration`

### Phase 9 — Friendliness audit (documentation)

Create `SPRING-BOOT-4-AUDIT.md`:

- Map findings to Jira acceptance criteria
- Classify: aligned / accepted deviation / deferred follow-up
- No requirement to implement all follow-ups to close migration

Typical deferred items: RestClient for sync HTTP, RestTestClient ITs, large DTO primitive sweeps.

Commit: `K1J-XXXX: Add Spring Boot 4 friendliness audit and Phase 9 documentation`

---

## Test dependency strategy (Boot 4)

There is **no** `spring-boot-starter-junit-test` artifact.

| Test type | Dependencies |
| --------- | ------------ |
| Plain unit tests (Mockito only) | `junit-jupiter` + `mockito-junit-jupiter` |
| Needs `ReflectionTestUtils` | add `spring-test` |
| MVC slice / `@WebMvcTest` | `spring-boot-starter-webmvc-test` |
| Jackson slice | `spring-boot-starter-jackson-test` |
| Full stack transitional | `spring-boot-starter-test` or `spring-boot-starter-test-classic` |

Remove unused starters (e.g. `spring-boot-starter-security-test` if no `@WithMockUser` usage).

---

## Project discovery checklist (run at Phase 0)

Before planning, inventory the target repo:

```
- [ ] Module layout (app, integration-*, logging, integration-test)
- [ ] Current intygBomVersion, Gradle wrapper, Jenkins image tags
- [ ] Jackson imports count (`com.fasterxml.jackson` excluding annotations)
- [ ] Raw Spring deps (`spring-context`, `spring-webmvc`, `aspectjweaver`, etc.)
- [ ] Manual @Configuration classes (Redis, JMS, WebClient, Security, Session)
- [ ] wsdl2java plugin usage (Gradle 9 compatibility)
- [ ] Apache Camel manual config
- [ ] intyg-common / dependencies.common.version dependency
- [ ] External schema artifacts needing version bumps
- [ ] Testcontainers usage and module names
- [ ] SAML / OpenSaml provider version
- [ ] Jenkins `sbom.aggregation.module` and expected SBOM file paths (`bom.json` vs `application.cdx.json`)
```

Mark Jira items **N/A** when not used in the project.

---

## High-impact pitfalls (from minaintyg)

| Pitfall | Symptom | Fix |
| ------- | ------- | --- |
| Missing `webclient` starter | `No qualifying bean of type WebClient.Builder` | Add `spring-boot-starter-webclient` |
| Raw session-data-redis | IT uses `JSESSIONID` instead of `SESSION` | `spring-boot-starter-session-data-redis` |
| `@Bean RedisCacheManager` | Boot cache auto-config disabled | Use `RedisCacheManagerBuilderCustomizer` |
| Jackson 3 primitives | Deserialization fails on missing fields | Wrapper types or documented global customizer |
| IntelliJ duplicate `ConnectionFactory` | Red underline on JMS config | False positive — Boot conditionals; optional `@Qualifier("jmsConnectionFactory")` |
| Boot 4 health probes | Unexpected probe endpoints | Probes enabled by default; property `management.endpoint.health.probes.enabled` |
| CycloneDX `cyclonedxBom` output | CI expects `bom.json` but gets `application.cdx.json` | Spring Boot SBOM integration — override `jsonOutput`/`xmlOutput` on app module (see Phase 2) |
| Property typo in prod config | Renaming breaks deploy | Keep typo if externally referenced; document |

---

## Commit message format

```
K1J-XXXX: [Short imperative summary]
```

One phase ≈ one commit. Do not commit unless the user asks.

---

## Document templates

Each migrated project should have:

1. **LIFECYCLE-MIGRATION-PLAN.md** — phases, inventory, risks, verification
2. **LIFECYCLE-MIGRATION-PROGRESS.md** — status table, phase log, lessons learned, deferred items
3. **SPRING-BOOT-4-AUDIT.md** — post-migration friendliness audit (Phase 9)

When migrating **intygstjanst** or another app: copy structure from minaintyg docs, fill in project-specific inventory and decisions. Use minaintyg as reference implementation, not a verbatim copy.

---

## Scope boundaries (do not expand without approval)

- RestClient migration (sync HTTP) — separate ticket
- RestTestClient IT migration — separate ticket
- Bulk WebCert/large DTO primitive → wrapper conversion — separate ticket
- wsdl2java / Camel upgrades — only if project uses them
- intyg-bom version authoring — external; only bump consumption version
- Behaviour-changing refactors disguised as migration

---

## Agent behaviour

1. Read this skill + Spring Boot 4 Migration Guide for unknown APIs
2. Create/update plan and progress docs in Phase 0
3. Execute one phase at a time; verify before proceeding
4. Stop after each phase with commit message suggestion
5. Record lessons learned and accepted deviations in progress doc
6. Ask before schema bumps, common-dep version strategy, or large behavioural changes
