---
name: spring-boot-project-structure
description: Scaffold or restructure a Spring Boot application's @Configuration classes, module layout, profile strategy, and infrastructure wiring. Use this when setting up a new Spring Boot service, reorganizing an existing one, adding modules, or aligning project structure with production-grade patterns for Kubernetes deployment.
license: MIT
---

# Spring Boot project structure skill

Use this skill when the task is to **create, audit, or restructure the overall project layout and Spring `@Configuration` wiring** in a Spring Boot application.

This skill complements `spring-boot-config-refactor` (which handles property migration). This skill focuses on:

- Multi-module Gradle/Maven project organization.
- `@Configuration` class design and placement.
- Spring profile strategy.
- REST and SOAP client wiring.
- Servlet, filter, and embedded server customization.
- Cache configuration.
- Test configuration and testability.
- Kubernetes-ready deployment structure.

## Guiding principles

1. **Convention over configuration.** Follow Spring Boot defaults unless there is a concrete reason to deviate.
2. **One concern per `@Configuration` class.** Each class configures exactly one infrastructure concern (TLS, caching, servlets, a specific client, etc.).
3. **Constructor injection everywhere.** Use Lombok `@RequiredArgsConstructor` or explicit constructors — never field injection.
4. **Profiles gate behaviour, not structure.** Use `@Profile` to toggle runtime behaviour (stubs, TLS, testability endpoints) — not to reorganize the config class hierarchy.
5. **Modules own their wiring.** Each Gradle/Maven module that declares beans should contain its own `@Configuration` classes and, if needed, its own `@ConfigurationProperties` record.
6. **Fail fast.** Validation (`@Validated`, `@NotBlank`, `@Positive`) on configuration records ensures misconfiguration is caught at startup, not at the first user request.
7. **Externalize environment differences.** The repository ships sensible defaults; production values come from Kubernetes ConfigMap/Secret or environment variables.

## Target project layout

### Multi-module Gradle structure

```
my-service/
├── app/                                    # Main application module — owns startup and infrastructure config
│   ├── build.gradle
│   ├── src/main/java/<base-package>/
│   │   ├── Application.java               # @SpringBootApplication + @ConfigurationPropertiesScan
│   │   ├── infrastructure/config/          # All @Configuration classes for this module
│   │   │   ├── properties/                 # @ConfigurationProperties records
│   │   │   │   └── AppProperties.java
│   │   │   ├── TomcatConfig.java
│   │   │   ├── ServletConfig.java
│   │   │   ├── WebMvcConfig.java
│   │   │   ├── TlsConfig.java
│   │   │   └── ...
│   │   ├── domain/                         # Domain logic (services, models)
│   │   └── application/                    # Use cases / application services
│   ├── src/main/resources/
│   │   └── application.yml                 # Canonical config with sensible defaults
│   ├── src/test/java/
│   │   └── <base-package>/
│   │       └── config/
│   │           └── TestConfig.java         # Test-specific Spring config
│   └── src/test/resources/
│       └── test.properties                 # Minimal test overrides
│
├── integration-<name>/                     # Integration module (one per external system)
│   ├── build.gradle
│   └── src/main/java/<base-package>/integration/<name>/
│       ├── configuration/
│       │   ├── <Name>Properties.java       # Module-scoped @ConfigurationProperties
│       │   ├── <Name>RestClientConfig.java
│       │   └── <Name>CacheConfig.java
│       ├── service/                        # Integration service implementations
│       └── dto/                            # DTOs for the external API
│
├── persistence/                            # JPA entities, repositories
│   ├── build.gradle
│   └── src/main/java/<base-package>/persistence/
│
├── web/                                    # REST controllers, request/response DTOs
│   ├── build.gradle
│   └── src/main/java/<base-package>/web/
│
├── devops/
│   └── dev/config/
│       └── application-dev.yml             # Local development overrides
│
├── build.gradle                            # Root build file
├── settings.gradle
└── Dockerfile
```

### Key layout rules

| Rule | Rationale |
|---|---|
| `app/` module is the only module with `application.yml` | Single source of truth for configuration defaults |
| Integration modules never ship their own `application.yml` | They bind to a subtree of the parent's config via their own `@ConfigurationProperties` record |
| `infrastructure/config/` holds all `@Configuration` classes | Easy to audit, review, and navigate |
| `infrastructure/config/properties/` holds all `@ConfigurationProperties` records | Keeps property binding separate from bean wiring |
| `devops/dev/config/` holds local dev overrides | Keeps dev config out of the packaged artifact |
| Test config lives under `src/test/java/.../config/` | Never leaks into production classpath |

## @Configuration class patterns

### Pattern 1: Infrastructure config with injected properties

Use when a `@Configuration` class needs application-specific settings.

```java
@Configuration
@RequiredArgsConstructor
public class TomcatConfig {

  private final AppProperties appProperties;

  @Bean
  public WebServerFactoryCustomizer<TomcatServletWebServerFactory> multiConnectorCustomizer() {
    return factory -> {
      final var connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
      connector.setPort(appProperties.server().internalPort());
      factory.addAdditionalTomcatConnectors(connector);
    };
  }
}
```

### Pattern 2: Profile-gated config

Use `@Profile` to enable or disable entire `@Configuration` classes based on environment.

```java
@Configuration
@Profile("!dev")
@RequiredArgsConstructor
public class TlsConfig {

  private final AppProperties appProperties;

  @Bean
  public SslBundles sslBundles() {
    // Configure mTLS for production — disabled in dev
  }
}
```

**Common profile patterns:**

| Profile expression | Meaning |
|---|---|
| `@Profile("dev")` | Active only during local development |
| `@Profile("!dev")` | Active in all deployed environments (test, staging, prod) |
| `@Profile("testability-api")` | Enables testability/debug endpoints |
| `@Profile("it-fk-stub")` | Enables stub implementations for integration tests |

### Pattern 3: Module-scoped REST client config

Each integration module wires its own REST client using its own `@ConfigurationProperties` record.

```java
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(IntegrationFooProperties.class)
public class FooRestClientConfig {

  private final IntegrationFooProperties properties;

  @Bean(name = "fooRestClient")
  public RestClient fooRestClient() {
    return RestClient.builder()
        .baseUrl(properties.baseUrl())
        .build();
  }
}
```

**Why `@EnableConfigurationProperties` here?** The module cannot rely on the main application's `@ConfigurationPropertiesScan` reaching its package. Explicit enablement makes the module self-contained.

### Pattern 4: Cache config with TTL from properties

```java
@Configuration
@RequiredArgsConstructor
@EnableCaching
public class FooCacheConfig {

  private final IntegrationFooProperties properties;

  @Bean
  public RedisCacheConfiguration fooCacheConfiguration() {
    return RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofSeconds(properties.cache().ttlSeconds()));
  }
}
```

### Pattern 5: Servlet and filter registration

```java
@Configuration
public class ServletConfig {

  @Bean
  public ServletRegistrationBean<DispatcherServlet> dispatcherServletRegistration(
      DispatcherServlet dispatcherServlet) {
    final var registration = new ServletRegistrationBean<>(dispatcherServlet);
    registration.addUrlMappings("/*");
    registration.setLoadOnStartup(1);
    return registration;
  }

  @Bean
  public FilterRegistrationBean<CharacterEncodingFilter> encodingFilter() {
    final var filter = new CharacterEncodingFilter("UTF-8", true);
    final var registration = new FilterRegistrationBean<>(filter);
    registration.addUrlPatterns("/*");
    return registration;
  }
}
```

### Pattern 6: Test configuration

```java
@Configuration
@EnableConfigurationProperties(AppProperties.class)
@PropertySource("classpath:test.properties")
@EnableJpaRepositories(basePackages = "...")
@EntityScan(basePackages = "...")
public class TestConfig {

  @Bean
  public DataSource dataSource() {
    return new EmbeddedDatabaseBuilder()
        .setType(EmbeddedDatabaseType.H2)
        .build();
  }
}
```

**Important:** `@PropertySource` does not support YAML. Test property files must use `.properties` format and include all required `app.*` keys.

## @ConfigurationProperties record design

### Root record (app module)

```java
@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
    @Valid Server server,
    @Valid Integration integration,
    @Valid Security security) {

  public record Server(@Positive int internalPort) {}

  public record Integration(
      @Valid CertificateService certificateService,
      @Valid ExternalApi externalApi) {

    public record CertificateService(@NotBlank String baseUrl) {}

    public record ExternalApi(
        @NotBlank String baseUrl,
        @Valid Cache cache) {

      public record Cache(@Positive long ttlSeconds) {}
    }
  }

  public record Security(@NotBlank String hashSalt) {}
}
```

### Module-scoped record (integration module)

```java
@Validated
@ConfigurationProperties(prefix = "app.integration.external-api")
public record ExternalApiProperties(
    @NotBlank String baseUrl,
    @Valid Endpoints endpoints,
    @Valid Cache cache) {

  public record Endpoints(
      @NotBlank String usersEndpoint,
      @NotBlank String ordersEndpoint) {}

  public record Cache(@Positive long ttlSeconds) {}
}
```

**Key rules:**
- The module record binds to a **subtree** of the root `app.*` hierarchy.
- It may duplicate structure from the root `AppProperties` — this is intentional for module isolation.
- Each module can be tested independently with only its own property subset.

## Spring profile strategy

### Profile purpose map

| Profile | Purpose | Where activated |
|---|---|---|
| `dev` | Local development with stubs, no TLS, embedded dependencies | `application.dir` JVM arg or `SPRING_PROFILES_ACTIVE=dev` |
| `testability-api` | Exposes debug/test endpoints (certificate injection, cache clearing) | DevOps environment variable |
| `it-fk-stub` | Stub implementations for integration testing | CI/CD pipeline |
| `bootstrap` | One-time data loading on first startup | Manual activation |
| *(no profile)* | Production baseline — K8s ConfigMap/Secret provides values | Default in deployed environments |

### Profile rules

1. **Never create a `prod` profile.** Production is the default. Non-production environments opt *in* to alternative behaviour via profiles.
2. **Use negative profiles sparingly.** `@Profile("!dev")` is acceptable for disabling expensive infrastructure (TLS, external connections) in local dev. Avoid complex profile expressions.
3. **Profiles go on `@Configuration` classes and `@Component`/`@RestController` classes** — not on individual `@Bean` methods unless there is a strong reason.
4. **Dev profile config lives outside the JAR.** Use `spring.config.additional-location` pointing to `devops/dev/config/` so dev overrides are not packaged.

## Application startup class

```java
@SpringBootApplication(scanBasePackages = {"<base-package>"})
@ConfigurationPropertiesScan(basePackages = {"<base-package>"})
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
```

**Notes:**
- `@ConfigurationPropertiesScan` auto-discovers all `@ConfigurationProperties` records in scanned packages.
- Explicit `scanBasePackages` is needed when the main class is not at the root of all modules' package hierarchy.
- If a multi-module project has modules with different base packages, list all relevant packages.

## Configuration layering for Kubernetes

```
┌──────────────────────────────────────────┐
│  K8s ConfigMap / Secret / Env vars       │  ← Highest priority (production values)
├──────────────────────────────────────────┤
│  application-dev.yml (devops/dev/config) │  ← Local dev only (not packaged)
├──────────────────────────────────────────┤
│  application.yml (src/main/resources)    │  ← Sensible defaults (packaged in JAR)
└──────────────────────────────────────────┘
```

### Rules for each layer

**application.yml (defaults):**
- Provide values that work for *most* environments or are safe fallbacks.
- Use env var placeholders for secrets: `app.security.hash-salt: "${HASH_SALT:}"`.
- Never hardcode production URLs, credentials, or hostnames.
- Set localhost defaults for infrastructure: `spring.activemq.broker-url: tcp://localhost:61616`.

**application-dev.yml (local dev):**
- Override base URLs to point to local stubs or Docker containers.
- Provide dummy credentials for local services.
- Set file paths relative to the project directory using `${application.dir}`.

**K8s ConfigMap/Secret (production):**
- Override `app.*` and `spring.*` properties with real values.
- Mount as external config file or inject via environment variables.
- Use Spring Boot relaxed binding for env vars: `APP_NTJP_BASE_URL` → `app.ntjp.base-url`.

## Required workflow

### Setting up a new module

1. Create the module directory under the project root.
2. Add `build.gradle` with dependencies on the modules it needs.
3. Register the module in `settings.gradle`.
4. Create a `@ConfigurationProperties` record bound to its `app.*` subtree.
5. Create `@Configuration` classes for the module's beans (REST clients, caches, etc.).
6. Use `@EnableConfigurationProperties` in each `@Configuration` class.
7. Ensure the main application's `scanBasePackages` includes the new module's package.
8. Add required properties to `application.yml` and `application-dev.yml`.
9. Run the full test suite to verify configuration binding.

### Auditing an existing project

1. **Map all `@Configuration` classes.** List each class, its purpose, and which properties or profiles it uses.
2. **Check for profile consistency.** Verify profiles are applied at class level, not scattered across bean methods.
3. **Check for module isolation.** Verify integration modules own their own `@Configuration` and `@ConfigurationProperties`, not depending on the root module's records.
4. **Check for constructor injection.** Flag any field injection (`@Autowired` on fields).
5. **Check for single concern.** Flag `@Configuration` classes that mix unrelated concerns (e.g., TLS + caching in one class).
6. **Check test configuration.** Verify test configs use `@EnableConfigurationProperties` and provide all required keys.
7. **Produce a report** with findings and recommended changes.

### Restructuring a project

1. **Audit** the current state (see above).
2. **Plan** the target structure following the layout in this skill.
3. **Move `@Configuration` classes** into the correct packages, one commit per logical group.
4. **Extract module-scoped configs** into their respective modules.
5. **Introduce missing `@ConfigurationProperties` records** for modules that inject properties via `@Value` or untyped means.
6. **Run tests after each step** to catch wiring issues early.
7. **Update `scanBasePackages`** if package reorganization requires it.

## Anti-patterns to avoid

| Anti-pattern | Do instead |
|---|---|
| Mega `@Configuration` class with 20+ beans | Split by concern: one class per infrastructure topic |
| `@Bean` methods gated by `@Profile` inside a shared config class | Create separate `@Configuration` class per profile |
| Integration module importing root `AppProperties` | Create module-scoped `@ConfigurationProperties` record |
| `@Autowired` field injection | Constructor injection via `@RequiredArgsConstructor` |
| `@Configuration` class in `src/main/java` root package | Place under `infrastructure/config/` or `configuration/` |
| Hardcoded URLs in `@Configuration` classes | Inject via `@ConfigurationProperties` record |
| Test config that duplicates production config | Minimal test config with `@EnableConfigurationProperties` and test property file |
| Profile per environment (dev, staging, prod) | Default is production; only dev/test/stub profiles exist |
| `@ComponentScan` with broad patterns | Explicit `scanBasePackages` listing relevant packages |
| Mixing SOAP and REST client config in one class | Separate `@Configuration` per protocol and external system |

## Strong preferences

- Prefer **Lombok `@RequiredArgsConstructor`** for constructor injection to reduce boilerplate.
- Prefer **`RestClient`** (Spring 6.1+) over `RestTemplate` for new REST client beans.
- Prefer **immutable Java records** for all `@ConfigurationProperties` classes.
- Prefer **`@ConfigurationPropertiesScan`** on the main class over scattered `@EnableConfigurationProperties` — except in test configs and self-contained modules.
- Prefer **one `@Configuration` class per external system** (e.g., `HsaRestClientConfig`, `PuRestClientConfig`) over a monolithic `RestClientConfig`.
- Prefer **package-by-concern** within modules (`configuration/`, `service/`, `dto/`) over flat structures.
- Prefer **negative profiles (`@Profile("!dev")`)** for production-only infrastructure over requiring a `prod` profile.
- Prefer **kebab-case** in YAML keys and **camelCase** in Java records, relying on Spring Boot's relaxed binding.

## Example prompts

- `Use /spring-boot-project-structure to audit this repo's @Configuration class organization and recommend improvements.`
- `Use /spring-boot-project-structure to set up a new integration module for the billing API.`
- `Use /spring-boot-project-structure to restructure our config classes — they're all in one mega class right now.`
- `Use /spring-boot-project-structure to review our Spring profile strategy and suggest a cleaner approach.`
- `Use /spring-boot-project-structure to scaffold a new Spring Boot service following production-grade patterns.`
