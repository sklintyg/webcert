# Step 18 — Replace Redis/Caching Manual Config + Spring Boot ECS Logging

## Problem Statement

Step 18 replaces three manually-managed infrastructure concerns with Spring Boot
auto-configuration:

1. **Redis** — `BasicCacheConfiguration` in `infra/` manually constructs a
   `JedisConnectionFactory` (standalone / Sentinel / Cluster) and a `RedisCacheManager`.
   `JobConfig` in `web/` also `@Autowired`-injects `JedisConnectionFactory` directly for
   ShedLock. These must be replaced by Spring Boot's Redis auto-configuration
   (`spring-boot-starter-data-redis`), with property keys mapped to `spring.data.redis.*`.

2. **Mail** — `MailConfig` in `web/` manually constructs a `JavaMailSenderImpl` from eight
   `@Value` fields. This must be replaced by `spring-boot-starter-mail` auto-configuration,
   with property keys mapped to `spring.mail.*`.

3. **Logging** — `logging.structured.format.console=ecs` is already present in
   `application.properties`. The remaining work is deleting dead-code
   (`LogbackConfiguratorContextListener`) and removing a now-redundant explicit
   `logback-classic` dependency from `web/build.gradle`.

---

## Pre-flight: What's Already Done

| Task | Status |
|------|--------|
| `logging.structured.format.console=ecs` in `application.properties` | ✅ Done |
| `logging.structured.format.console=` (empty/off in `application-dev.properties`) | ✅ Done |
| No `logback-ecs-encoder` or `logback-spring-base.xml` in the codebase | ✅ Done |
| `spring.session.redis.namespace=${app.name}` in `application.properties` | ✅ Done |
| `spring.session.servlet.filter-order=1` in `application.properties` | ✅ Done |
| `spring.data.redis.repositories.enabled=false` in `application.properties` | ✅ Done |
| `org.springframework.session:spring-session-data-redis` in `web/build.gradle` | ✅ Done |
| Add `spring-boot-starter-data-redis` | ❌ Not done |
| Remove manual `JedisConnectionFactory` from `BasicCacheConfiguration` | ❌ Not done |
| Fix `JobConfig` (uses `JedisConnectionFactory` directly) | ❌ Not done |
| Map `redis.*` properties to `spring.data.redis.*` | ❌ Not done |
| Add `spring-boot-starter-mail` | ❌ Not done |
| Remove manual `JavaMailSenderImpl` from `MailConfig` | ❌ Not done |
| Map `mail.*` properties to `spring.mail.*` | ❌ Not done |
| Delete `LogbackConfiguratorContextListener` (dead code since web.xml removed) | ❌ Not done |

---

## Current State (Baseline)

### Redis — Files to Change

| File | Location | Action |
|------|----------|--------|
| `web/build.gradle` | `web/` | Add `spring-boot-starter-data-redis` |
| `infra/build.gradle` | `infra/` | Remove `redis.clients:jedis`; remove `org.springframework.data:spring-data-redis` |
| `BasicCacheConfiguration.java` | `infra/src/main/java/.../rediscache/core/` | Remove manual Jedis factory; simplify to use auto-configured `RedisConnectionFactory` |
| `ConnectionStringUtil.java` | `infra/src/main/java/.../rediscache/core/util/` | **Delete** — only used by the Jedis factory parsing logic |
| `JobConfig.java` | `web/src/main/java/.../config/` | Change `JedisConnectionFactory` → `RedisConnectionFactory` |
| `application.properties` | `web/src/main/resources/` | Map `redis.*` → `spring.data.redis.*` |
| `application-dev.properties` | `devops/dev/config/` | Map `redis.password` → `spring.data.redis.password` |

### Redis — Current Jedis Setup in BasicCacheConfiguration

`BasicCacheConfiguration` currently:
- Reads 10 `@Value` fields (`redis.host`, `redis.port`, `redis.password`,
  `redis.cache.default_entry_expiry_time_in_seconds`, `redis.sentinel.master.name`,
  `redis.read.timeout`, `redis.cluster.*`)
- Switches on active Spring profiles (`redis-cluster`, `redis-sentinel`, standalone)
  to create a `JedisConnectionFactory`
- Creates `RedisTemplate<Object,Object>` bean named `"rediscache"` (used by
  `RedisTicketTrackerImpl` with `@Qualifier("rediscache")`)
- Creates `CacheFactory` (a `RedisCacheManager`) bean named `"cacheManager"`
- Creates `RedisCacheOptionsSetter` bean used by four cache configs:
  `RedisLaunchIdCacheConfiguration`, `CertificatesForPatientCacheConfiguration`,
  `IntygProxyServiceHsaCacheConfiguration`, `IaCacheConfiguration`

### Redis — Other Consumers (no code changes needed)

- **`RedisTicketTrackerImpl`** — `@DependsOn("rediscache")`, uses
  `@Qualifier("rediscache") RedisTemplate`. Bean name `"rediscache"` must remain
  unchanged. ✅ Preserved by the rewritten config.
- **`CacheConfig`** — `StringRedisTemplate` already injects `RedisConnectionFactory`
  (the interface). ✅ No change needed. Note: if Spring Boot auto-config also tries to
  register `StringRedisTemplate`, a conflict will arise — see note in Task 18b.
- **`WebSecurityConfig`** — `@EnableRedisIndexedHttpSession`. Explicit annotation
  takes precedence over auto-config; session serialization stays JDK (no format
  change → **no session invalidation**).
- **`RedisLaunchIdCacheConfiguration`**, **`CertificatesForPatientCacheConfiguration`**,
  **`IntygProxyServiceHsaCacheConfiguration`**, **`IaCacheConfiguration`** — all use
  `RedisCacheOptionsSetter` (unchanged). ✅ No changes needed.

### Mail — Files to Change

| File | Location | Action |
|------|----------|--------|
| `web/build.gradle` | `web/` | Add `spring-boot-starter-mail`; remove `jakarta.mail:jakarta.mail-api` |
| `MailConfig.java` | `web/src/main/java/.../config/` | Remove `mailSender()` bean and its `@Value` fields; keep scheduler/executor |
| `application.properties` | `web/src/main/resources/` | Map `mail.protocol/defaultEncoding/smtps.*` → `spring.mail.*` |
| `application-dev.properties` | `devops/dev/config/` | Map `mail.host/username/password` → `spring.mail.host/username/password` |

### Mail — Current MailConfig

`MailConfig` currently defines three beans:
- `JavaMailSenderImpl mailSender()` — **remove** (Spring Boot auto-configures this)
- `ThreadPoolTaskScheduler mailTaskScheduler()` — **keep** (not auto-configured)
- `ThreadPoolTaskExecutor threadPoolTaskExecutor()` — **keep** (not auto-configured)

`MailNotificationServiceImpl` injects `JavaMailSender` (the interface), compatible with
auto-config without any changes.

`MailStubConfig` (dev profile) wraps whatever `JavaMailSender` bean is present via AOP
— also compatible without changes.

### Logging — Files to Change

| File | Location | Action |
|------|----------|--------|
| `LogbackConfiguratorContextListener.java` | `infra/src/main/java/.../monitoring/logging/` | **Delete** — dead code; was a `ServletContextListener` registered in the old `web.xml` (removed in Step 14) |
| `web/build.gradle` | `web/` | Remove `ch.qos.logback:logback-classic` (redundant; provided by `spring-boot-starter-web → spring-boot-starter-logging`) |

---

## Property Mapping Reference

### Redis (standalone mode)

| Old property | New property | Notes |
|---|---|---|
| `redis.host=127.0.0.1` | `spring.data.redis.host=127.0.0.1` | |
| `redis.port=6379` | `spring.data.redis.port=6379` | |
| `redis.password=` | `spring.data.redis.password=` | |
| `redis.read.timeout=PT1M` | `spring.data.redis.timeout=PT1M` | ISO-8601; applies to all modes |
| `redis.cache.default_entry_expiry_time_in_seconds=86400` | *(keep as-is)* | No Spring Boot equivalent; consumed directly by `BasicCacheConfiguration` |

### Redis (Sentinel profile: `redis-sentinel`)

| Old property | New property | Notes |
|---|---|---|
| `redis.host=host1,host2,host3` | `spring.data.redis.sentinel.nodes=host1:26379,host2:26379,...` | Combine host:port pairs |
| `redis.port=26379,...` | *(merged into nodes above)* | |
| `redis.sentinel.master.name=master` | `spring.data.redis.sentinel.master=master` | |

### Redis (Cluster profile: `redis-cluster`)

| Old property | New property | Notes |
|---|---|---|
| `redis.cluster.nodes=host1:6379,...` | `spring.data.redis.cluster.nodes=host1:6379,...` | Direct rename |
| `redis.cluster.max.redirects=3` | `spring.data.redis.cluster.max-redirects=3` | |
| `redis.cluster.password=` | `spring.data.redis.password=` | Unified password key |
| `redis.cluster.read.timeout=PT1M` | `spring.data.redis.timeout=PT1M` | Lettuce uses global timeout |

### Mail

| Old property | New property | Notes |
|---|---|---|
| `mail.host=` | `spring.mail.host=` | Dev/deployment config only |
| `mail.username=` | `spring.mail.username=` | Dev/deployment config only |
| `mail.password=` | `spring.mail.password=` | Dev/deployment config only |
| `mail.protocol=smtps` | `spring.mail.protocol=smtps` | |
| `mail.defaultEncoding=UTF-8` | `spring.mail.default-encoding=UTF-8` | |
| `mail.smtps.auth=true` | `spring.mail.properties.mail.smtps.auth=true` | |
| `mail.smtps.starttls.enable=true` | `spring.mail.properties.mail.smtps.starttls.enable=true` | |
| `mail.smtps.debug=false` | `spring.mail.properties.mail.smtps.debug=false` | |
| `mail.from=...` | *(keep as `mail.from`)* | App-specific, no Spring Boot equivalent |
| `mail.admin=...` | *(keep as `mail.admin`)* | App-specific |
| `mail.webcert.host.url=...` | *(keep as `mail.webcert.host.url`)* | App-specific |

---

## Detailed Task Instructions

### Task 18a — Add `spring-boot-starter-data-redis`; drop raw deps

**`web/build.gradle`** — add:
```gradle
implementation "org.springframework.boot:spring-boot-starter-data-redis"
```

**`infra/build.gradle`** — remove:
```gradle
// REMOVE:
implementation "redis.clients:jedis"
implementation "org.springframework.data:spring-data-redis"
// KEEP: spring-session-core (still used by infra session classes)
```

> **Why switch from Jedis to Lettuce?** Lettuce is the Spring Boot 3.x default,
> supports reactive operations, and performs better under load. The existing
> Sentinel / Cluster profiles map cleanly to Spring Boot Lettuce properties. The
> session serialization format (JDK) does not change because `@EnableRedisIndexedHttpSession`
> controls it independently of the connection factory client library.

---

### Task 18b — Rewrite `BasicCacheConfiguration`

Replace the body of `BasicCacheConfiguration.java`. **Keep:** three beans below.
**Remove:** all Jedis factory methods, all `@Value` fields except `defaultEntryExpiry`,
`@Resource Environment`, `PropertySourcesPlaceholderConfigurer`.

```java
@Configuration
@EnableCaching
public class BasicCacheConfiguration {

  @Value("${redis.cache.default_entry_expiry_time_in_seconds}")
  long defaultEntryExpiry;

  @Bean
  @DependsOn("cacheManager")
  public RedisCacheOptionsSetter redisCacheOptionsSetter() {
    return new RedisCacheOptionsSetter();
  }

  @Bean(name = "rediscache")
  RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory);
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    return redisTemplate;
  }

  @Bean
  public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
    return new CacheFactory(
        RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory),
        RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofSeconds(defaultEntryExpiry)));
  }
}
```

**Why keep `CacheFactory extends RedisCacheManager`?**
`RedisCacheOptionsSetter` calls `redisCacheFactory.createRedisCache(...)`, which is a
protected method on `RedisCacheManager`. `CacheFactory` exposes it as package-accessible
methods to four per-cache-TTL configurations. Because we define our own
`@Bean cacheManager()`, Spring Boot's `RedisCacheManagerAutoConfiguration` backs off
(`@ConditionalOnMissingBean(CacheManager.class)`).

**StringRedisTemplate conflict watch:** `BasicCacheConfiguration` defines a
`RedisTemplate<Object,Object>` named `"rediscache"`, which triggers Spring Boot
`RedisAutoConfiguration` to back off on its `RedisTemplate<Object,Object>` bean.
However, Spring Boot still registers its own `StringRedisTemplate` via auto-config.
`CacheConfig` also registers a `StringRedisTemplate`. This creates a duplicate-bean
situation. **Resolution:** remove the `StringRedisTemplate` bean from `CacheConfig`
(the one auto-configured by Spring Boot is identical) and verify the application starts
cleanly.

---

### Task 18c — Fix `JobConfig`

```java
// BEFORE
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
...
@Autowired private JedisConnectionFactory jedisConnectionFactory;

@Bean
public LockProvider lockProvider() {
  return new RedisLockProvider(jedisConnectionFactory, "webcert");
}

// AFTER
import org.springframework.data.redis.connection.RedisConnectionFactory;
...
@Autowired private RedisConnectionFactory redisConnectionFactory;

@Bean
public LockProvider lockProvider() {
  return new RedisLockProvider(redisConnectionFactory, "webcert");
}
```

---

### Task 18d — Map Redis properties in property files

**`web/src/main/resources/application.properties`** — replace the Redis block:

```properties
########################################
#
# Redis connection
# (standalone default; override via spring.data.redis.sentinel.*
#  or spring.data.redis.cluster.* in deployment config)
#
########################################
spring.data.redis.host=127.0.0.1
spring.data.redis.port=6379
spring.data.redis.password=
spring.data.redis.timeout=PT1M

# Sentinel example (profile redis-sentinel, deployment config):
#   spring.data.redis.sentinel.master=master
#   spring.data.redis.sentinel.nodes=host1:26379,host2:26379

# Cluster example (profile redis-cluster, deployment config):
#   spring.data.redis.cluster.nodes=host1:6379,host2:6379
#   spring.data.redis.cluster.max-redirects=3

# Custom — no Spring Boot equivalent; consumed by BasicCacheConfiguration
redis.cache.default_entry_expiry_time_in_seconds=86400

spring.session.store-type=redis
spring.session.redis.namespace=${app.name}
```

Remove: `redis.host`, `redis.port`, `redis.password`, `redis.read.timeout`,
`redis.sentinel.master.name`, `redis.cluster.nodes`, `redis.cluster.password`,
`redis.cluster.max.redirects`, `redis.cluster.read.timeout`.

**`devops/dev/config/application-dev.properties`** — rename:
```properties
# BEFORE
redis.cache.default_entry_expiry_time_in_seconds=60
redis.password=redis

# AFTER
redis.cache.default_entry_expiry_time_in_seconds=60
spring.data.redis.password=redis
```

---

### Task 18e — Delete `ConnectionStringUtil.java`

Delete:
```
infra/src/main/java/se/inera/intyg/webcert/infra/rediscache/core/util/ConnectionStringUtil.java
```
Verify no other callers before deleting:
```
grep -r "ConnectionStringUtil" --include="*.java"
```
must return empty.

---

### Task 18f — Add `spring-boot-starter-mail`; remove `jakarta.mail-api`

**`web/build.gradle`**:
```gradle
// ADD:
implementation "org.springframework.boot:spring-boot-starter-mail"

// REMOVE:
implementation "jakarta.mail:jakarta.mail-api"
```

---

### Task 18g — Remove `mailSender()` bean from `MailConfig`

From `MailConfig.java`, delete:
- `@Value("${mail.host}") private String mailHost`
- `@Value("${mail.protocol}") private String mailProtocol`
- `@Value("${mail.username}") private String mailUsername`
- `@Value("${mail.password}") private String mailPassword`
- `@Value("${mail.defaultEncoding}") private String mailDefaultEncoding`
- `@Value("${mail.smtps.auth}") private String smtpsAuth`
- `@Value("${mail.smtps.starttls.enable}") private String startTls`
- `@Value("${mail.smtps.debug}") private String smtpsDebug`
- The entire `mailSender()` `@Bean` method
- Imports: `JavaMailSenderImpl`, `Properties`

Keep (unchanged):
- `mailTaskScheduler()` — Spring Boot does not auto-configure schedulers
- `threadPoolTaskExecutor()` — Spring Boot does not auto-configure this executor

Spring Boot's `JavaMailSenderAutoConfiguration` creates a bean named `"mailSender"`
typed as `JavaMailSenderImpl` implementing `JavaMailSender`. `MailNotificationServiceImpl`
injects `JavaMailSender` — compatible without changes.

---

### Task 18h — Map mail properties

**`web/src/main/resources/application.properties`** — update the mail section:

```properties
########################################
#
# Mail
# (host/username/password set via deployment config or application-dev.properties)
#
########################################
mail.webcert.host.url=${webcert.host.url}
mail.admin=admin@webcert.se
mail.from=no-reply@${webcert.host.url}
forward.draft.or.question.url=${webcert.host.url}/webcert/web/user/launch/certificate/

spring.mail.protocol=smtps
spring.mail.default-encoding=UTF-8
spring.mail.properties.mail.smtps.auth=true
spring.mail.properties.mail.smtps.starttls.enable=true
spring.mail.properties.mail.smtps.debug=false
```

Remove: `mail.protocol`, `mail.defaultEncoding`, `mail.smtps.auth`,
`mail.smtps.starttls.enable`, `mail.smtps.debug`.

**`devops/dev/config/application-dev.properties`**:
```properties
# BEFORE
mail.host=
mail.username=
mail.password=

# AFTER
spring.mail.host=
spring.mail.username=
spring.mail.password=
```

---

### Task 18i — Logging cleanup

**Delete:**
```
infra/src/main/java/se/inera/intyg/webcert/infra/monitoring/logging/LogbackConfiguratorContextListener.java
```
This `ServletContextListener` was previously registered in `web.xml` (removed in Step 14).
Spring Boot initialises Logback during `SpringApplication.run()` before any `ServletContext`
exists. The class is unreachable dead code.

Verify no references remain:
```
grep -r "LogbackConfiguratorContextListener" --include="*.java" --include="*.xml"
```

**`web/build.gradle`** — remove:
```gradle
// REMOVE (transitively provided by spring-boot-starter-web → spring-boot-starter-logging):
implementation "ch.qos.logback:logback-classic"
```
Keep `ch.qos.logback:logback-classic` in `infra/build.gradle` and
`logging/build.gradle` — those are non-Boot modules.

---

## Session Serialization Risk Analysis

`@EnableRedisIndexedHttpSession` in `WebSecurityConfig` explicitly configures session
storage. The serializer for session data is JDK serialization by default — determined
by Spring Session, not by the `RedisConnectionFactory` implementation. Switching from
Jedis to Lettuce does **not** change the session serialization format. Active sessions
survive a rolling deployment without invalidation.

If a future step changes the serializer (e.g. to JSON), that requires an explicit
`RedisSerializer` bean and a session migration plan. Not in scope for Step 18.

---

## Verify

After all tasks are complete:

1. `./gradlew test` — all tests pass.
2. `./gradlew :web:bootRun` (profiles: `dev,testability-api,caching-enabled,...`)
3. No `NoSuchBeanDefinitionException` for `RedisConnectionFactory`, `JedisConnectionFactory`,
   `JavaMailSender`, or `CacheManager` at startup.
4. Redis caches populated: `redisCacheLaunchId`, `certificatesForPatientCache`, HSA caches,
   `iaCache`.
5. ShedLock acquires and releases locks in Redis.
6. `RedisTicketTrackerImpl` stores and retrieves signature tickets
   (`webcert.signature.ticket*` keys in Redis).
7. Mail sending works (mail stub in dev intercepts without errors).
8. Stdout shows ECS JSON in non-dev; plain text in dev.
9. `/actuator/health` → HTTP 200.
10. Sanity checks:
    ```
    grep -r "JedisConnectionFactory" --include="*.java"   # must be empty
    grep -r "ConnectionStringUtil" --include="*.java"     # must be empty
    grep -r "LogbackConfiguratorContextListener" --include="*.java" --include="*.xml"  # must be empty
    ```
