# Step 15 — Replace JPA Manual Config with Spring Boot Auto-Configuration

## Problem Statement

The persistence module uses a fully manual JPA stack (`JpaConfigBase.java` / `JpaConfig.java`) that manually
creates `HikariDataSource`, `LocalContainerEntityManagerFactoryBean`, and `JpaTransactionManager` beans,
reading from legacy `db.*` / `hibernate.*` properties. Step 15 replaces all of this with Spring Boot
`spring-boot-starter-data-jpa` auto-configuration and migrates property keys to `spring.datasource.*` /
`spring.jpa.*` conventions.

---

## Current State (Baseline)

### Files to Remove / Replace
| File | Location | Action |
|------|----------|--------|
| `JpaConfigBase.java` | `persistence/src/main/java/.../persistence/config/` | **Delete** |
| `JpaConfig.java` | `persistence/src/main/java/.../persistence/config/` | **Delete** |
| `repository-context.xml` | `persistence/src/test/resources/` | **Delete** |

### Key Manual Beans in `JpaConfigBase.java`
- `standaloneDataSource()` → returns `HikariDataSource` with hardcoded `autoCommit=false`, `minIdle=3`, `connectionTimeout=3000`, `idleTimeout=15000`
- `entityManagerFactory(DataSource)` → `LocalContainerEntityManagerFactoryBean` scanning `se.inera.intyg.webcert.persistence`
- `transactionManager(EntityManagerFactory)` → `JpaTransactionManager`
- `springLiquibase(DataSource)` → `SpringLiquibase` bean named **`dbUpdate`**

### `JpaConfig.java` (extends `JpaConfigBase`)
- `@Profile("!h2")` — skipped during tests
- `@ComponentScan("se.inera.intyg.webcert.persistence")` — component scan for persistence module
- `@EnableJpaRepositories(basePackages = "se.inera.intyg.webcert.persistence")` — JPA repo discovery

### `AppConfig.java` Coupling
- `@DependsOn("dbUpdate")` — depends on the bean name from the manual Liquibase bean
- `implements TransactionManagementConfigurer` — injects `JpaTransactionManager` by constructor
- `@EnableTransactionManagement` — declares transaction management

### Current Properties (old format)
```properties
# application.properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://${database.server}:${database.port}/${database.name}?...
db.username=${database.username}
db.password=${database.password}
db.pool.maxSize=20
hibernate.hbm2ddl.auto=none
hibernate.ejb.naming_strategy=org.hibernate.cfg.DefaultNamingStrategy
hibernate.show_sql=false
hibernate.format_sql=false
hibernate.id.new_generator_mappings=false
```
```properties
# devops/dev/config/application-dev.properties (db section)
database.server=localhost
database.port=3306
database.name=webcert
database.username=webcert
database.password=webcert
db.pool.maxSize=5
```
```properties
# persistence/src/test/resources/test.properties
db.driver=org.h2.Driver
db.url=jdbc:h2:mem:dataSource;MODE=MySQL;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER
db.username=sa
db.password=
db.pool.maxSize=3
hibernate.dialect=org.hibernate.dialect.MySQLDialect
hibernate.ejb.naming_strategy=org.hibernate.cfg.ImprovedNamingStrategy
hibernate.hbm2ddl.auto=
hibernate.show_sql=false
hibernate.format_sql=false
```

### Current Dependencies (`persistence/build.gradle`)
```gradle
implementation "com.zaxxer:HikariCP"              // explicitly declared
implementation "org.hibernate.orm:hibernate-core"  // explicitly declared
implementation "org.hibernate.orm:hibernate-hikaricp"
implementation "org.springframework.data:spring-data-jpa"  // NOT the boot starter
```

---

## Approach

Replace the manual JPA stack with Spring Boot auto-configuration in **5 ordered sub-steps**, each
independently verifiable. All existing JPA behavior must be preserved — especially `autoCommit=false`,
the entity scan package, and Liquibase execution order.

---

## Critical Complications (Must Address)

### 1. `autoCommit=false` — NOT Spring Boot's Default
HikariCP defaults to `autoCommit=true`. The current manual config explicitly sets `autoCommit=false`.
This must be preserved via `spring.datasource.hikari.auto-commit=false` or queries in `@Transactional`
code may change behavior.

### 2. Liquibase Bean Name: `dbUpdate` → `liquibase`
Spring Boot auto-configures Liquibase as a bean named `liquibase`, NOT `dbUpdate`.
`AppConfig.java` has `@DependsOn("dbUpdate")` — **this will throw a `NoSuchBeanDefinitionException`
at startup if the bean is renamed without updating the annotation**.
**Resolution:** Replace `@DependsOn("dbUpdate")` with `@DependsOn("liquibase")`.

### 3. `hibernate.ejb.naming_strategy` is Hibernate 6 Incompatible
`org.hibernate.cfg.DefaultNamingStrategy` and `org.hibernate.cfg.ImprovedNamingStrategy` do not exist
in Hibernate 6 (Spring Boot 3.x). Spring Boot 3.x defaults to
`CamelCaseToUnderscoresNamingStrategy` which converts camelCase Java field names to snake_case column
names — a schema-breaking change if existing columns use camelCase naming.
**Resolution:**
- Inspect a representative entity (`Utkast.java`, `Arende.java`) to determine the actual DB column naming
  convention used.
- If the DB uses camelCase column names (matching Java field names): set
  `spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl`
- If the DB uses snake_case column names: Spring Boot's default (`CamelCaseToUnderscoresNamingStrategy`)
  applies and no override is needed.
- **Do not leave this implicit** — set the strategy explicitly regardless, to document the intent.

### 4. `AppConfig.java` `TransactionManagementConfigurer` — Remove After Auto-Config
`AppConfig.java` currently `implements TransactionManagementConfigurer` and injects `JpaTransactionManager`
by constructor (provided by `JpaConfigBase`). Once `JpaConfigBase` is deleted, this constructor injection
fails unless Spring Boot's auto-configured `JpaTransactionManager` bean is in scope. The entire
`TransactionManagementConfigurer` implementation should be removed from `AppConfig.java` since Spring Boot
auto-config handles transaction management.

### 5. H2 Test Profile — `@Profile("!h2")` on `JpaConfig`
Tests run with the `h2` Spring profile, which excluded `JpaConfig`. With `JpaConfig` deleted, the profile
is moot for production code. But `test.properties` still has old `db.*`/`hibernate.*` property names that
are no longer read by anything — tests will use Spring Boot auto-config and need the new property names
in a test-specific override (e.g., `application-h2.properties` or updated `test.properties`).

---

## Sub-Steps

### Sub-step 15.1 — Inspect Naming Strategy (prerequisite)

Before migrating, inspect entity classes in `se.inera.intyg.webcert.persistence` to determine
whether DB columns use camelCase (Java-style) or snake_case naming. Look for `@Column(name=...)` or
implicit naming. Run `SHOW CREATE TABLE utkast` against the dev DB or inspect Liquibase changelogs.

**Output:** Document the correct `physical-strategy` to use in properties.

---

### Sub-step 15.2 — Update `persistence/build.gradle`

Replace individual JPA/Hibernate dependencies with the Spring Boot starter:

```gradle
// REMOVE:
implementation "com.zaxxer:HikariCP"
implementation "jakarta.persistence:jakarta.persistence-api"
implementation "org.hibernate.orm:hibernate-core"
implementation "org.hibernate.orm:hibernate-hikaricp"
implementation "org.springframework.data:spring-data-jpa"

// ADD:
implementation "org.springframework.boot:spring-boot-starter-data-jpa"
```

Keep:
- `implementation "org.liquibase:liquibase-core"` — Liquibase is NOT included in `spring-boot-starter-data-jpa`; the Liquibase auto-config is triggered by `liquibase-core` on the classpath when Spring Boot finds it.
- `implementation "com.mysql:mysql-connector-j"` — still needed
- `testImplementation "com.h2database:h2"` — still needed for tests

Also remove `implementation "org.hibernate.orm:hibernate-core"` from `web/build.gradle` (no longer needed
as it comes transitively from the starter).

**Verify:** `./gradlew :webcert-persistence:compileJava` — should compile (JpaConfigBase still exists at
this point).

---

### Sub-step 15.3 — Migrate Properties

#### `web/src/main/resources/application.properties`

Remove the old `db.*` and `hibernate.*` properties and add the Spring Boot equivalents:

```properties
# REMOVE:
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://${database.server}:${database.port}/${database.name}?...
db.username=${database.username}
db.password=${database.password}
db.pool.maxSize=20
hibernate.hbm2ddl.auto=none
hibernate.ejb.naming_strategy=org.hibernate.cfg.DefaultNamingStrategy
hibernate.show_sql=false
hibernate.format_sql=false
hibernate.id.new_generator_mappings=false

# ADD:
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://${database.server}:${database.port}/${database.name}?useSSL=false&serverTimezone=Europe/Stockholm&allowPublicKeyRetrieval=true
spring.datasource.username=${database.username}
spring.datasource.password=${database.password}
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=3
spring.datasource.hikari.connection-timeout=3000
spring.datasource.hikari.idle-timeout=15000
spring.datasource.hikari.auto-commit=false

spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.properties.hibernate.id.new_generator_mappings=false
spring.jpa.properties.hibernate.enable_lazy_load_no_trans=false
# Set to PhysicalNamingStrategyStandardImpl if DB uses camelCase column names (see sub-step 15.1)
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl

spring.liquibase.change-log=classpath:changelog/changelog.xml
```

#### `devops/dev/config/application-dev.properties`

```properties
# REMOVE:
db.pool.maxSize=5

# ADD:
spring.datasource.hikari.maximum-pool-size=5
```

Note: `database.server`, `database.port`, `database.name`, `database.username`, `database.password` are
NOT changed — they are indirect config values consumed by `${database.server}` placeholder in
`spring.datasource.url` above. They remain as-is.

#### `persistence/src/test/resources/test.properties`

This file is loaded via `@PropertySource("classpath:test.properties")` in `TestConfig.java`. After
removing `JpaConfigBase`, no code reads `db.*` or `hibernate.*` from it. However, Spring Boot needs
`spring.datasource.*` to configure H2 for tests.

```properties
# REMOVE:
db.driver=org.h2.Driver
db.url=jdbc:h2:mem:dataSource;MODE=MySQL;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER
db.username=sa
db.password=
db.pool.maxSize=3
hibernate.dialect=org.hibernate.dialect.MySQLDialect
hibernate.ejb.naming_strategy=org.hibernate.cfg.ImprovedNamingStrategy
hibernate.hbm2ddl.auto=
hibernate.show_sql=false
hibernate.format_sql=false

# ADD:
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.url=jdbc:h2:mem:dataSource;MODE=MySQL;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.hikari.maximum-pool-size=3
spring.datasource.hikari.auto-commit=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=none
spring.jpa.properties.hibernate.id.new_generator_mappings=false
```

Note: `hibernate.ejb.naming_strategy` is not migrated — it will be superseded by the
`spring.jpa.hibernate.naming.physical-strategy` set in `application.properties`.

---

### Sub-step 15.4 — Refactor `AppConfig.java`

`AppConfig.java` must be updated before deleting `JpaConfigBase` (it references `JpaTransactionManager`
via constructor injection):

1. **Remove** `implements TransactionManagementConfigurer`
2. **Remove** the `private final JpaTransactionManager transactionManager` field and constructor injection
3. **Remove** the `annotationDrivenTransactionManager()` override method
4. **Change** `@DependsOn("dbUpdate")` → `@DependsOn("liquibase")`
5. Keep `@EnableTransactionManagement` as-is (harmless; Spring Boot respects it)

Full diff summary for `AppConfig.java`:
```java
// REMOVE these lines:
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

@DependsOn("dbUpdate")          // → change to @DependsOn("liquibase")
@RequiredArgsConstructor        // may be removable if JpaTransactionManager was the only constructor-injected field
public class AppConfig implements TransactionManagementConfigurer { ... }
//                          ↑ remove "implements TransactionManagementConfigurer"

// REMOVE method:
@Override
public PlatformTransactionManager annotationDrivenTransactionManager() {
  return transactionManager;
}
```

If `JpaTransactionManager` was the only constructor-injected field, also remove `@RequiredArgsConstructor`.

---

### Sub-step 15.5 — Delete Manual JPA Config Classes

Once properties are migrated and `AppConfig.java` is updated:

1. **Delete** `JpaConfigBase.java`
2. **Delete** `JpaConfig.java`
3. **Delete** `repository-context.xml` (test resource, was used for XML-based component scan in tests —
   now superseded by `@SpringBootTest` auto-config)

**Note on component scan:** `JpaConfig` had `@ComponentScan("se.inera.intyg.webcert.persistence")`.
Since `WebcertApplication` (in `se.inera.intyg.webcert`) has `@SpringBootApplication`, it already
component-scans all of `se.inera.intyg.webcert.*` including the `persistence` sub-package. This scan
covers classes in the `webcert-persistence` JAR as well. No explicit `@ComponentScan` override needed.

**Note on `@EnableJpaRepositories`:** Spring Boot's `JpaRepositoriesAutoConfiguration` will auto-enable
JPA repositories for all packages under `se.inera.intyg.webcert`. Since all repositories are in
`se.inera.intyg.webcert.persistence.*`, they will be discovered.

---

### Sub-step 15.6 — Verify

```
./gradlew :webcert-persistence:test    # H2-based persistence tests pass
./gradlew test                          # full test suite passes
./gradlew bootRun                       # app starts, connects to MySQL, Liquibase runs
```

Specific checks:
- Liquibase changelog applied on startup (look for `Successfully acquired change log lock` in logs)
- No `NoSuchBeanDefinitionException` for `dbUpdate` or `liquibase`
- No `PropertyNotFoundException` for `db.*` or `hibernate.*`
- `spring.jpa.open-in-view=false` still in `application.properties` (already present — keep)
- `./gradlew bootRun` — DB connects, entities are found, no schema mismatch errors
- Run a sample query via an integration test or actuator health endpoint

---

## Property Migration Reference Table

| Old Property | New Property | Notes |
|---|---|---|
| `db.driver` | `spring.datasource.driver-class-name` | |
| `db.url` | `spring.datasource.url` | Keep `${database.*}` placeholders |
| `db.username` | `spring.datasource.username` | |
| `db.password` | `spring.datasource.password` | |
| `db.pool.maxSize` | `spring.datasource.hikari.maximum-pool-size` | |
| *(hardcoded 3)* | `spring.datasource.hikari.minimum-idle` | **NEW** — was hardcoded |
| *(hardcoded 3000)* | `spring.datasource.hikari.connection-timeout` | **NEW** — was hardcoded |
| *(hardcoded 15000)* | `spring.datasource.hikari.idle-timeout` | **NEW** — was hardcoded |
| *(hardcoded false)* | `spring.datasource.hikari.auto-commit` | **NEW** — was hardcoded, critical |
| `hibernate.hbm2ddl.auto` | `spring.jpa.hibernate.ddl-auto` | |
| `hibernate.show_sql` | `spring.jpa.show-sql` | |
| `hibernate.format_sql` | `spring.jpa.properties.hibernate.format_sql` | |
| `hibernate.id.new_generator_mappings` | `spring.jpa.properties.hibernate.id.new_generator_mappings` | |
| `hibernate.enable_lazy_load_no_trans` *(hardcoded)* | `spring.jpa.properties.hibernate.enable_lazy_load_no_trans` | **NEW** |
| `hibernate.ejb.naming_strategy` | `spring.jpa.hibernate.naming.physical-strategy` | Class name changed (see 15.1) |
| *(manual SpringLiquibase bean)* | `spring.liquibase.change-log` | Boot auto-detects changelog |

---

## Risks

| Risk | Mitigation |
|------|-----------|
| Wrong naming strategy → schema mismatch | Inspect entity annotations before migrating (sub-step 15.1) |
| `autoCommit=true` default breaks transactions | Explicitly set `spring.datasource.hikari.auto-commit=false` |
| `@DependsOn("dbUpdate")` → missing bean | Change to `@DependsOn("liquibase")` in sub-step 15.4 |
| Test H2 config not picked up | Update `test.properties` with `spring.datasource.*` names |
| `AppConfig` fails to compile after JpaConfigBase removal | Update `AppConfig` before deleting JpaConfigBase |
| Spring Boot scans different packages for repositories | Verify `WebcertApplication` package covers `se.inera.intyg.webcert.persistence` |

---

## Post-Implementation Fix: Missing `transactionManager` Bean

### Root Cause

At runtime the application fails with:
> A component required a bean named 'transactionManager' that could not be found.

**Why:** `JmsConfig.jmsTransactionManager()` registers a `JmsTransactionManager` (a
`PlatformTransactionManager`). Spring Boot's `JpaTransactionManagerAutoConfiguration` has
`@ConditionalOnMissingBean(PlatformTransactionManager.class)` — it sees `jmsTransactionManager`
exists and **backs off** without creating a JPA transaction manager. Spring Data JPA's
`@EnableJpaRepositories` uses `transactionManagerRef = "transactionManager"` as its default — that
named bean no longer exists, so all JPA repositories fail at startup.

Before Step 15, `JpaConfigBase.transactionManager()` was an explicit `@Bean` that always
registered regardless of what else existed. Removing it exposed this conflict.

### Fix

Re-add a `JpaTransactionManager` bean named `transactionManager`, marked `@Primary`, in
`AppConfig.java`. This coexists with `jmsTransactionManager`. The `@Primary` qualifier ensures
`@Transactional` methods without an explicit qualifier use the JPA transaction manager.

**File changed:** `AppConfig.java` only — add:

```java
import jakarta.persistence.EntityManagerFactory;
import org.springframework.orm.jpa.JpaTransactionManager;

// inside AppConfig class:
@Bean
@Primary
public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
    return new JpaTransactionManager(entityManagerFactory);
}
```

---

## File Change Summary

| File | Change |
|------|--------|
| `persistence/build.gradle` | Replace individual JPA deps with `spring-boot-starter-data-jpa` |
| `web/build.gradle` | Remove explicit `hibernate-core` |
| `web/src/main/resources/application.properties` | Migrate `db.*`/`hibernate.*` → Spring Boot keys |
| `devops/dev/config/application-dev.properties` | Migrate `db.pool.maxSize` → `spring.datasource.hikari.maximum-pool-size` |
| `persistence/src/test/resources/test.properties` | Migrate H2 config to Spring Boot keys |
| `web/src/main/java/.../web/config/AppConfig.java` | Remove `TransactionManagementConfigurer`, fix `@DependsOn` |
| `persistence/src/main/java/.../persistence/config/JpaConfigBase.java` | **DELETE** |
| `persistence/src/main/java/.../persistence/config/JpaConfig.java` | **DELETE** |
| `persistence/src/test/resources/repository-context.xml` | **DELETE** |