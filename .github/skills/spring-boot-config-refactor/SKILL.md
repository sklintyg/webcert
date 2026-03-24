---
name: spring-boot-config-refactor
description: Refactor a Spring Boot application's configuration from legacy application.properties and scattered @Value usage to structured YAML and immutable @ConfigurationProperties records. Use this when migrating messy configuration, standardizing property naming, introducing app-prefixed custom properties, or separating sensible defaults from local development overrides.
license: MIT
---

# Spring Boot configuration refactor skill

Use this skill when the task is to **analyze, restructure, and migrate application configuration** in a Spring Boot codebase.

This skill is specifically optimized for repositories where:
- `application.properties` has grown organically over time and is hard to understand.
- Local development uses a `dev` profile and an additional config location such as `devops/dev/config/`.
- Test and production deployments are configured externally through Kubernetes `ConfigMap`/`Secret`/sealed secrets rather than profile-specific packaged files.
- Many settings are injected via `@Value` annotations.
- The goal is to move to **type-safe**, **immutable**, **record-based** configuration using `@ConfigurationProperties`.

## Desired target state

Follow these rules unless the user explicitly instructs otherwise:

1. Use YAML instead of `.properties` for Spring configuration.
2. Put **application-specific properties** under the `app` prefix.
3. Keep **standard Spring Boot / framework / library properties** under their normal prefixes (for example `spring.*`, `management.*`, `server.*`, `logging.*`).
4. Keep the same conceptual split between:
   - `application.yml` for minimal defaults and shared base configuration.
   - `application-dev.yml` for local development overrides.
   - `application-test.yml` for Spring Boot test scenarios when relevant.
5. Migrate from scattered `@Value` usage to `@ConfigurationProperties` with immutable Java records.
6. Inject configuration records as Spring beans via constructor injection.
7. Add validation so invalid configuration fails fast during startup.
8. Preserve the existing deployment model where production/test environment overrides come from external configuration, Kubernetes config maps, and secrets.
9. Do **not** introduce environment-specific packaged config for production unless the user explicitly asks for it.

## Key implementation principles

### Configuration layout
- `src/main/resources/application.yml`
  - Keep it minimal.
  - Include sensible defaults only.
  - Do not hardcode environment-specific secrets or infrastructure endpoints.
  - Use Spring Boot's native property keys directly for framework properties (`spring.activemq.*`, `spring.data.redis.*`, etc.) rather than creating intermediate "bridge" property layers (e.g. `activemq.broker.url` → `spring.activemq.broker-url`). Set localhost/empty defaults inline and let K8s ConfigMaps override with real values.
- `src/main/resources/application-dev.yml`
  - Use for local dev defaults and local overrides **only if that matches the repo's current practice**.
  - If the project already loads external files from `devops/dev/config/` using `spring.config.additional-location`, preserve that approach unless the user asks to change it.
- `src/test/resources/application-test.yml`
  - Use when test-specific configuration is needed.

### Property modeling
- Group custom application properties into one or more records under a dedicated package such as:
  - `...config`
  - `...config.properties`
  - or the package already used by the project for application config
- Prefer a small number of well-structured top-level property records over many tiny fragmented ones.
- Use nested records to mirror the YAML hierarchy.
- Keep names domain-oriented and self-explanatory.
- **Multi-module projects**: each Gradle/Maven module that needs config should have its own `@ConfigurationProperties` record bound to its relevant subtree. For example, a main `app` module gets `AppProperties` (`prefix = "app"`), while an `integration-foo` module gets `FooProperties` (`prefix = "app.integration.foo"`). This keeps each module self-contained and avoids forcing a dependency on the root config record.

Example pattern:

```java
package com.example.app.config.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
    @Valid Feature feature,
    @Valid Integration integration
) {
    public record Feature(
        boolean enabled,
        @Min(1) int batchSize
    ) {}

    public record Integration(
        @NotBlank String baseUrl,
        @NotNull Timeout timeout
    ) {
        public record Timeout(
            @Min(1) int connectSeconds,
            @Min(1) int readSeconds
        ) {}
    }
}
```

### Spring Boot wiring
- Prefer `@ConfigurationPropertiesScan` on the main application class if it is not already in use.
- Alternatively use `@EnableConfigurationProperties(...)` only when there is a strong repository-specific reason (for example, test `@Configuration` classes that don't trigger `@SpringBootTest` auto-scanning — see the test pitfalls section).
- Use constructor injection exclusively.
- Access values using record accessors, for example `appProperties.integration().baseUrl()`.
- **Dependency**: `@Validated` on `@ConfigurationProperties` records requires `spring-boot-starter-validation`. Check if it is already present before adding it.

### Validation
- Use `@Validated` on the configuration record.
- Use Jakarta Bean Validation annotations such as:
  - `@NotNull`
  - `@NotBlank`
  - `@Min`
  - `@Max`
  - `@Positive`
  - `@Valid` for nested records
- Ensure configuration errors fail fast at application startup.
- **`@Valid` on nested records does NOT enforce non-null**: `@Valid Security security` means validation runs on the nested record's contents *only if the record is non-null*. If the property subtree is absent, the component is `null` and `@Valid null` passes silently. At runtime, accessing `security().hashSalt()` then throws `NullPointerException`. Use `@NotNull @Valid` if the component must always be present, or accept that the component may be null and guard access accordingly.

### Handling existing `@Value` usage

#### Auditing `@Value` annotations
- Search the codebase for all `@Value` annotations — but **distinguish Lombok `@Value` from Spring `@Value`**. Lombok `@Value` is a class-level annotation (generates getters, equals, hashCode). Spring `@Value` is `org.springframework.beans.factory.annotation.Value` and is used on fields/parameters for property injection. Filter for the Spring import, not just `@Value` text matches.
- **Also search for `@Scheduled`, `@JmsListener`, `@KafkaListener`, and other annotations that use `${...}` placeholders** in their attributes (e.g. `@Scheduled(cron = "${my.cron}")`, `@JmsListener(destination = "${my.queue}")`). These are property references that will break when legacy keys are removed, but they do not show up in a `@Value` search.

#### Classifying properties
Classify each occurrence:
1. custom application property → migrate under `app.*`
2. framework/library property → usually leave as framework config and inject a richer Spring abstraction where possible
3. literal default with occasional override need → model as a typed property with a sensible default in YAML, or keep optional via constructor defaulting only if clearly justified

#### Migrating classes
- Replace field injection and `@Value` injection with constructor-injected configuration beans.
- When using Lombok `@RequiredArgsConstructor` for constructor injection, be aware that **`@RequiredArgsConstructor` does not propagate `@Qualifier` annotations**. If a bean needs both a `@ConfigurationProperties` record and a `@Qualifier`-annotated bean (e.g. a named `RestClient`), keep `@Autowired @Qualifier` on the qualified field (non-final) and make only the config properties field `final` for constructor injection.
- Remove dead properties after migration.

### Bridge properties and infrastructure config
Legacy configurations often have "bridge" properties — custom intermediate property keys that are referenced by Spring Boot's native property placeholders. For example:

```yaml
# Bridge pattern (avoid in target state):
redis:
  host: 127.0.0.1
  port: 6379
spring:
  data:
    redis:
      host: "${redis.host}"
      port: "${redis.port}"
```

During migration:
- **Identify all bridge properties** during the audit phase. Common examples: `db.*` → `spring.datasource.*`, `activemq.broker.*` → `spring.activemq.*`, `redis.*` → `spring.data.redis.*`.
- **Replace bridges with direct values** in `application.yml`. Set localhost/empty defaults for `spring.activemq.*`, `spring.data.redis.*`, etc. directly, and let K8s ConfigMaps override using the standard Spring property names (or their env var equivalents via Spring Boot's relaxed binding, e.g. `SPRING_ACTIVEMQ_BROKER_URL`).
- If a bridge cannot be removed immediately (e.g. `db.*` properties are used to compose `spring.datasource.url` via `${db.server}:${db.port}/${db.name}`), keep it temporarily and document it as a follow-up cleanup.

### Property naming rules
- Application-specific keys must move under `app`.
- Avoid flat names when the domain has hierarchy.
- Prefer this:
  - `app.integration.certificate-service.base-url`
  - `app.jobs.cleanup.batch-size`
- Avoid this:
  - `certificateServiceUrl`
  - `cleanupBatch`

### Secrets and external config
- Never hardcode secrets in repository defaults.
- Assume sensitive values should come from environment variables, sealed secrets, or external config in the devops repository.
- Prefer env var placeholders for secrets where possible, for example `app.security.hash-salt: "${HASH_SALT:}"`.
- Preserve compatibility with `spring.config.additional-location` if the project already relies on it.

## Required workflow — phased migration

When using this skill, follow this phased workflow. Each phase should result in a **passing test suite** and a **separate commit**. The user can review, test, and roll back each phase independently.

### Phase 1: Audit the current state
Inspect at least:
- `application.properties` and any existing `application-*.properties` or `.yml` files
- local dev config under `devops/dev/config/` if present
- the custom Gradle run task (for example `appRunDebug` or `bootRun` configuration)
- main application class
- all usages of Spring `@Value` (filter by `org.springframework.beans.factory.annotation.Value`)
- all `@Scheduled`, `@JmsListener`, `@KafkaListener`, and similar annotations with `${...}` placeholders
- any existing `@ConfigurationProperties` classes
- test configuration files under `src/test/resources`
- bridge properties (custom keys referenced by Spring Boot's `spring.*` placeholders)
- properties shared with other applications or common libraries (these may need to be kept as-is or coordinated)

Produce a migration assessment that identifies:
- what should remain standard framework configuration
- what should move under `app.*`
- which properties appear environment-specific
- which values look like secrets
- bridge properties and whether they can be eliminated
- properties used by `@Scheduled`, `@JmsListener`, etc. that must be renamed carefully
- where defaults are currently duplicated or contradictory
- any properties used by shared libraries that cannot be renamed unilaterally

### Phase 2: Convert `.properties` → `.yml` (1:1, no renames)
- Convert `application.properties` to `application.yml` with **identical keys** (no renames yet).
- Convert any `application-dev.properties` to `application-dev.yml` likewise.
- Delete the `.properties` files.
- **Run tests.** This phase validates the YAML conversion without changing any property names.
- Commit.

### Phase 3: Introduce `app.*` canonical hierarchy with dual aliases
- Add the full `app:` YAML block with the target property structure.
- **Keep all old flat property names** as aliases that reference the new canonical names, e.g.:
  ```yaml
  # Canonical value:
  app:
    server:
      internal-port: 8081
  # Legacy alias — points to the canonical value:
  internal:
    api:
      port: "${app.server.internal-port}"
  ```
- This dual-alias pattern ensures all existing `@Value("${internal.api.port}")` references still resolve. No `@Value` annotation needs to change yet.
- Update `application-dev.yml` to use `app.*` keys for overrides (since those files are maintained in the repo).
- **Run tests.** This phase validates that the alias chain resolves correctly.
- Commit.

### Phase 4: Create `@ConfigurationProperties` record(s)
- Create the `AppProperties` record (and per-module records for multi-module projects).
- Add `@ConfigurationPropertiesScan` to the main application class.
- Add `spring-boot-starter-validation` dependency if not already present.
- **Run tests.** The record exists but is not injected anywhere yet. This validates that the record binds correctly from YAML.
- Commit.

### Phase 5: Migrate `@Value` → `@ConfigurationProperties` injection
- Module by module, replace all `@Value` field injections with constructor-injected config records.
- Update `@Scheduled`, `@JmsListener`, etc. placeholders to use `app.*` keys.
- Update test files as needed (see test pitfalls section below).
- **Run tests after each module** to catch issues early.
- Commit (one per module, or combined if small).

### Phase 6: Remove legacy aliases
- Remove all legacy alias sections from `application.yml`.
- Remove any bridge properties that have been replaced with direct values.
- Verify no unresolved `${...}` placeholders remain (search for `@Value`, `@Scheduled`, `@JmsListener` references to removed keys).
- **Run tests.**
- Commit.

### Phase 7: Final cleanup and verification
- Remove unused Spring `@Value` imports from all source files.
- Verify the full test suite passes.
- Verify `application-dev.yml` still has all needed overrides.
- Commit.

### Phase 8: Generate K8s deployment migration guide (if applicable)
If the application is deployed via Kubernetes:
- Generate a migration document (e.g. `migration/kubernetes-config-migration.md`) that maps:
  - Old property name → new property name (for ConfigMap keys)
  - Old secret key → new secret key or env var (for SealedSecrets)
  - Which properties have defaults and don't need K8s injection
  - Before/after ConfigMap and SealedSecret YAML examples
  - Step-by-step migration checklist for platform engineers
  - Rollback procedure
- Commit.

## Test pitfalls and solutions

These are common issues encountered when migrating `@Value` to `@ConfigurationProperties` in test code. Be aware of all of them before starting Phase 5.

### `@PropertySource` does not support YAML
If a test `@Configuration` class uses `@PropertySource("classpath:test.properties")`, that file **cannot be converted to YAML**. Spring's default `PropertySourceFactory` only handles `.properties` and `.xml`. Leave it as `.properties` and add the required `app.*` keys there (e.g. `app.security.hash-salt=salt`). Converting to YAML requires implementing a custom `YamlPropertySourceFactory` which is usually not worth the effort.

### Non-`@SpringBootTest` tests don't auto-load `application.yml`
Tests using `@ContextConfiguration(classes = TestConfig.class)` (without `@SpringBootTest`) do not trigger Spring Boot's auto-configuration or load `application.yml`. In this case:
- Add `@EnableConfigurationProperties(AppProperties.class)` to the `TestConfig` class.
- Ensure the test property source (e.g. `test.properties`) contains all required `app.*` keys, otherwise the bound record component will be `null` and runtime access will throw `NullPointerException`.

### `@Spy` with `ReflectionTestUtils.setField` breaks
When a class previously had a field like `@Value("${hash.salt}") private String salt;`, tests often used `@Spy` with `ReflectionTestUtils.setField(hashUtility, "salt", "test-salt")`. After migration, the field is gone (replaced by constructor-injected `AppProperties`). Mockito's `@Spy` also needs a no-arg constructor or explicit initialization. Fix by initializing the spy explicitly:
```java
@Spy
private HashUtility hashUtility = new HashUtility(
    new AppProperties(null, null, null, null,
        new AppProperties.Security("test-salt"), null));
```
Only populate the record components the spy actually needs; use `null` for the rest.

### Mockito strict stubbing causes `UnnecessaryStubbingException`
When adding stubs in `@BeforeEach` for the new config record (e.g. `when(appProperties.jms()).thenReturn(...)`) that are not used by all test methods, Mockito's strict stubbing mode flags them as unnecessary. Fix with `Mockito.lenient().when(...)` for stubs that are only exercised by a subset of tests.

### Mirror/copy test files
Some projects keep exact copies of source files under `src/test/java` (sometimes for test classpath isolation or legacy build reasons). When modifying a main source class, check whether an identical copy exists in the test source tree and update it to match.

## Handling shared library properties
Some properties may be consumed by shared libraries (e.g. a common library used across multiple microservices that reads `texts.file.directory`). These cannot be renamed to `app.*` unilaterally without coordinating with the library. Options:
1. **Keep the old property name** — document it as a known exception.
2. **Add a dual alias** — keep the old name pointing to the new `app.*` canonical value until the library is updated.
3. **Update the library** — if you control the library, update it to read the new `app.*` key.

Identify these during the audit phase and flag them for the user.

## Output expectations

When working on a refactor, structure the response like this:

1. **Assessment**
   - brief summary of current state
   - key problems found
   - bridge properties identified
   - shared library properties identified
   - properties used by `@Scheduled`/`@JmsListener`/etc.
2. **Target structure**
   - proposed YAML layout
   - proposed Java configuration records (per module)
   - property rename table (old → new)
3. **Phased plan**
   - phases with rollback points
   - risk assessment per phase
   - K8s impact summary
4. **Changes made** (during implementation)
   - files created/updated
   - `@Value` usages migrated
   - test files updated
5. **Notes / follow-up**
   - remaining risks
   - recommended cleanup not yet done
   - K8s deployment changes needed

## Strong preferences
- Prefer **clarity over cleverness**.
- Prefer **few well-structured config records** over many fragmented ones.
- Prefer **minimal defaults** in `application.yml`.
- Prefer **externalized environment-specific config** for deployed environments.
- Prefer **keeping deployment semantics stable** over introducing a new profile strategy for production.
- Prefer **direct Spring Boot property keys** over bridge property layers for infrastructure config.
- Prefer **one commit per phase** with passing tests at each step.
- Prefer **env var placeholders** for secrets (e.g. `${HASH_SALT:}`) over leaving them empty.

## Avoid
- Do not convert standard Spring Boot properties to `app.*`.
- Do not keep spreading configuration logic across `@Value` annotations.
- Do not place secrets in `application.yml` or `application-dev.yml` unless the user explicitly wants local-only placeholders and understands the tradeoff.
- Do not introduce mutable configuration POJOs when records are sufficient.
- Do not silently change how the application is started in local dev.
- Do not assume that profile-specific files should drive Kubernetes deployment.
- Do not rename properties consumed by shared libraries without coordinating with the library.
- Do not create bridge property layers (custom keys → `spring.*` via `${...}` placeholders) for new configuration. If they exist, plan to remove them.
- Do not use `@Valid` on a nested record component without considering whether `null` is acceptable. If the component must always be present, use `@NotNull @Valid`.

## Repository-specific guidance for this style of project
This skill is especially suitable for projects that follow these conventions:
- Spring Boot application
- Gradle or Maven build
- local development starts with a custom task such as `appRunDebug` or `bootRun` with extra JVM args
- local overrides may come from `devops/dev/config/`
- deployed environments use external config and secrets rather than packaged profile files
- multi-module project structure with shared config across modules

If the repository matches this pattern, align the refactor to that model rather than forcing a generic Spring Boot setup.

## Example request prompts
Use prompts like:
- `Use /spring-boot-config-refactor to migrate this repo from @Value and application.properties to YAML + ConfigurationProperties records.`
- `Use /spring-boot-config-refactor to audit our current config structure and propose a target app.* hierarchy.`
- `Use /spring-boot-config-refactor to refactor the local dev config flow without changing our Kubernetes deployment strategy.`
- `Use /spring-boot-config-refactor to analyze this repository and create a phased migration plan. Do not implement yet.`
