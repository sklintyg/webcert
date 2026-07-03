# Spring Boot 4 lifecycle migration — reference

Companion to [SKILL.md](SKILL.md). Read when applying starter swaps, Jackson API changes, or audit items.

---

## Starter migration table (Boot 3 → Boot 4)


| Boot 3 / raw dependency                     | Boot 4 replacement                       | Notes                                        |
| ------------------------------------------- | ---------------------------------------- | -------------------------------------------- |
| `spring-boot-starter-web`                   | `spring-boot-starter-webmvc`             | Servlet MVC app                              |
| `spring-boot-starter-webflux` (client only) | `spring-boot-starter-webclient`          | No reactive server needed                    |
| `spring-boot-starter-webflux` (server)      | `spring-boot-starter-webflux`            | Unchanged name                               |
| `aspectjweaver`                             | `spring-boot-starter-aspectj`            | AOP / `@Aspect`                              |
| `spring-context` + JSON needs               | `spring-boot-starter-jackson`            | JSON auto-config                             |
| `spring-context` + JMS                      | `spring-boot-starter-jms`                | JMS infrastructure                           |
| `spring-session-data-redis` (raw)           | `spring-boot-starter-session-data-redis` | Required for filter auto-config              |
| `spring-webmvc` in library module           | `spring-web`                             | Minimal — e.g. `SpringBeanAutowiringSupport` |
| Blocking sync HTTP (new)                    | `spring-boot-starter-restclient`         | Optional future migration from WebClient     |


### Test starters


| Purpose                  | Dependency                                                            |
| ------------------------ | --------------------------------------------------------------------- |
| MVC slice tests          | `spring-boot-starter-webmvc-test`                                     |
| Jackson / JSON tests     | `spring-boot-starter-jackson-test`                                    |
| JDBC slice               | `spring-boot-starter-jdbc-test`                                       |
| REST client tests        | `spring-boot-starter-restclient` + `spring-boot-resttestclient`       |
| Plain unit tests         | `junit-jupiter` + `mockito-junit-jupiter` (+ `spring-test` if needed) |
| Transitional full bundle | `spring-boot-starter-test` or `spring-boot-starter-test-classic`      |


**Does not exist:** `spring-boot-starter-junit-test`

---

## Jackson 2 → 3 API mapping


| Jackson 2                             | Jackson 3                                                |
| ------------------------------------- | -------------------------------------------------------- |
| `com.fasterxml.jackson.databind.`*    | `tools.jackson.databind.*`                               |
| `com.fasterxml.jackson.core.*`        | `tools.jackson.core.*`                                   |
| `com.fasterxml.jackson.datatype.*`    | `tools.jackson.datatype.*`                               |
| `com.fasterxml.jackson.annotation.*`  | **unchanged**                                            |
| `ObjectMapper` (manual)               | `JsonMapper.builder().build()`                           |
| `JsonProcessingException`             | `JacksonException` (unchecked)                           |
| `MappingJackson2MessageConverter`     | `JacksonJsonMessageConverter`                            |
| `GenericJackson2JsonRedisSerializer`  | `GenericJacksonJsonRedisSerializer`                      |
| `FAIL_ON_NULL_FOR_PRIMITIVES` default | `true` — use wrapper types or document global customizer |


Inject `JsonMapper` type (not `ObjectMapper`) where Boot provides the bean.

---

## Configuration property renames (common)


| Boot 3                             | Boot 4                                      |
| ---------------------------------- | ------------------------------------------- |
| `spring.session.redis.`*           | `spring.session.data.redis.*`               |
| `management.health.probes.enabled` | `management.endpoint.health.probes.enabled` |


Use `spring-boot-properties-migrator` temporarily to discover project-specific renames.

---

## Auto-configuration patterns

### Redis cache (preferred)

```java
@Configuration
@EnableCaching
public class RedisConfig {
    @Bean
    RedisCacheManagerBuilderCustomizer bannersCacheCustomizer(JsonMapper jsonMapper) {
        return builder -> builder
            .withCacheConfiguration("BANNERS_CACHE", RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofDays(1))
                .serializeValuesWith(/* GenericJacksonJsonRedisSerializer */));
    }
}
```

Do **not** declare `@Bean RedisCacheManager` unless full override is required.

### WebClient (preferred)

```java
@Bean
WebClient webcertWebClient(WebClient.Builder builder) {
    return builder
        .filter(mdcFilter)
        .codecs(/* module-specific only */)
        .build();
}
```

Requires `spring-boot-starter-webclient` on classpath.

### JMS (when manual config is justified)

Boot 4.1 has **no** `JmsTemplateCustomizer`. Auto-config creates one `JmsTemplate` when no `JmsOperations` bean exists, picking up a unique `MessageConverter` and `spring.jms.template.`* properties.

Keep manual `@Bean JmsTemplate` when you need:

- Custom queue property (`@Value`, not `spring.jms.template.default-destination`)
- Transacted session without shared properties
- Named bean for injection (`jmsTemplateForCertificateAnalyticsMessages`)
- Profile-gated config (`@Profile("feature-active")`)

Optional IDE fix: `@Qualifier("jmsConnectionFactory")` on `ConnectionFactory` parameter.

---

## Spring Security 7 / SAML

- `OpenSaml4AuthenticationProvider` → `OpenSaml5AuthenticationProvider`
- Lambda-style `SecurityFilterChain` configuration

---

## Testcontainers 2.x

Artifact prefix: `testcontainers-`

Examples: `testcontainers-activemq`, `testcontainers-mockserver`, `testcontainers-postgresql`

---

## Actuator / health (Boot 4 defaults)


| Area            | Boot 3       | Boot 4                                                                           |
| --------------- | ------------ | -------------------------------------------------------------------------------- |
| K8s probes      | Opt-in       | **Enabled by default**                                                           |
| Endpoints       | Manual setup | `/actuator/health/liveness`, `/actuator/health/readiness`                        |
| Readiness scope | N/A          | Only `readinessState` by default — Redis/ActiveMQ not included unless configured |


Smoke-test probe URLs in OpenShift after deploy.

---

## CycloneDX SBOM (Gradle plugin 3.x + Spring Boot 4)

**Cause of `application.cdx.json`:** Spring Boot Gradle plugin (since 3.3) reconfigures `cyclonedxBom` when CycloneDX is on the classpath — not caused by intyg-bom CycloneDX patch bumps (e.g. 3.1.0 → 3.2.4).

**Tasks:**


| Task                 | Scope                    | Default output                                                 |
| -------------------- | ------------------------ | -------------------------------------------------------------- |
| `cyclonedxDirectBom` | Single module            | `build/reports/cyclonedx-direct/bom.json`, `bom.xml`           |
| `cyclonedxBom`       | Aggregate (multi-module) | `build/reports/cyclonedx/application.cdx.json` (Boot override) |


**CI fix** (when pipeline expects `bom.json`/`bom.xml` from `cyclonedxBom` on the app module):

```gradle
tasks.named('cyclonedxBom') {
    jsonOutput.set(file("build/reports/cyclonedx/bom.json"))
    xmlOutput.set(file("build/reports/cyclonedx/bom.xml"))
}
```

Check `Jenkins.properties` → `sbom.aggregation.module` / `sbom.aggregation.path` for which module needs this.

---

## Audit finding template (Phase 9)

Use IDs and priority in `SPRING-BOOT-4-AUDIT.md`:


| ID  | Priority | Category | Summary                         | Disposition        |
| --- | -------- | -------- | ------------------------------- | ------------------ |
| M*  | Medium   | —        | Blocking or policy decision     | Keep / Fix / Defer |
| L*  | Low      | —        | Polish / optional modernisation | Done / Defer / N/A |


Track **accepted deviations** separately with rationale.

---

## intygstjanst fill-in prompts

When starting a new project migration, ask or determine:

1. Jira ticket number (`K1J-XXXX`)
2. Current `intygBomVersion` and target BOM version
3. Module list and roles
4. wsdl2java / Camel / ActiveMQ / intyg-common usage (yes/no)
5. Schema artifact bumps needed (yes/no — stop and ask for versions)
6. `dependencies.common.version` applicable (yes/no)
7. Gradle wrapper: manual or agent-driven
8. Jenkins SBOM paths (`sbom.aggregation.module`; `bom.json` vs `application.cdx.json`)
9. Known deferred items from prior migrations to carry forward

Copy minaintyg doc **structure**, replace project name, inventory, and phase log entries.