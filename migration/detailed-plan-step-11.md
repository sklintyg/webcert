# Step 11 — Convert JAX-RS Controllers to Spring MVC

## Problem Statement

Webcert currently uses Apache CXF JAX-RS to serve REST endpoints across **7 CXF servlet contexts**
with **~52 controllers**. Each CXF servlet has its own XML configuration file, component scans, and
child Spring context. This creates tight coupling to Apache CXF for REST that blocks Spring Boot
migration, a complex multi-context architecture, and XML-heavy configuration.

**Goal:** Convert all JAX-RS REST controllers to Spring MVC `@RestController` pattern, remove CXF
REST servlets, and consolidate all REST handling under a single Spring DispatcherServlet. SOAP
endpoints at `/services/*` remain on CXF.

---

## Current State

### CXF REST Servlets (7 — all to be removed)

| Servlet | URL Pattern | XML Config | Controllers | Exception Handler |
|---------|------------|------------|-------------|-------------------|
| `api` | `/api/*` | `api-cxf-servlet.xml` | 22 | WebcertRestExceptionHandler |
| `moduleapi` | `/moduleapi/*` | `moduleapi-cxf-servlet.xml` | 3 | WebcertRestExceptionHandler |
| `internalapi` | `/internalapi/*` | `internalapi-cxf-servlet.xml` | 8 | WebcertRestExceptionHandler |
| `integrationapi` | `/visa/*`, `/v2/visa/*` | `integration-cxf-servlet.xml` | 2 | WebcertRedirectIntegrationExceptionHandler |
| `uthoppintegrationapi` | `/webcert/web/user/*` | `uthopp-integration-cxf-servlet.xml` | 4 | WebcertRedirectIntegrationExceptionHandler |
| `testability` | `/testability/*` | `testability-cxf-servlet.xml` | 12 | (none — via imported XML) |
| `authtestability` | `/authtestability/*` | `authtestability-cxf-servlet.xml` | 1 | WebcertRestExceptionHandler |

### CXF SOAP Servlet (1 — to be KEPT)

| Servlet | URL Pattern | XML Config | Purpose |
|---------|------------|------------|---------|
| `services` | `/services/*` | `services-cxf-servlet.xml` | SOAP web services + stubs |

### DispatcherServlet (current)

| Servlet | URL Pattern | Config | Controllers |
|---------|------------|--------|-------------|
| `web` | `/web/*` | `web-servlet.xml` | PageController (@Controller) |

### External Module Endpoints

`classpath*:wc-module-cxf-servlet.xml` from external modules (fk7263, ts-diabetes) contain
**JAXWS SOAP clients only** — not JAX-RS. **No action needed for Step 11.**

### JAX-RS Providers (registered in CXF servlet contexts)

| Provider | Type | Registered in | Replacement |
|----------|------|--------------|-------------|
| `jacksonJsonProvider` | JSON serialization (CustomObjectMapper) | All 7 CXF servlets | Spring MVC HttpMessageConverter |
| `webcertRestExceptionHandler` | ExceptionMapper for REST | api, internalapi, authtestability | @RestControllerAdvice |
| `webcertRedirectIntegrationExceptionHandler` | ExceptionMapper for redirects | integration, uthopp-integration | @ControllerAdvice |
| `localDateTimeHandler` | ParamConverterProvider | api (all 3 servers), internalapi, authtestability — **NOT** in moduleapi, integration, uthopp-integration, testability | Spring Converter<String, LocalDateTime> |

> **Note:** All 4 provider beans are defined in `webcert-config.xml` (the root application
> context) and shared across all CXF child contexts. When the CXF servlets are removed the
> bean definitions must be removed from `webcert-config.xml` in sub-step 11.15.

### Test Infrastructure

- **34 test files** — all Mockito unit tests with direct method invocation (no MockMvc, no CXF test containers)
- **8 controller test files** reference `jakarta.ws.rs.core.Response` — require update:
  - `StatModuleApiControllerTest`
  - `UserIntegrationControllerTest`
  - `IntygIntegrationControllerTest`
  - `SignatureApiControllerTest`
  - `PrivatePractitionerApiControllerTest`
  - `JsLogApiControllerTest`
  - `InvalidateSessionApiControllerTest`
  - `FmbApiControllerTest`
- **0 test files** use HTTP-level testing
- Testing pattern: `@ExtendWith(MockitoExtension.class)` + `@InjectMocks` + `@Mock`

---

## Migration Strategy

1. **Infrastructure first** — Set up Spring MVC equivalents (ObjectMapper, exception handling,
   parameter converters) before converting any controllers
2. **Incremental per-group** — Convert one CXF servlet group at a time, adding DispatcherServlet
   URL mappings progressively
3. **Verify after each group** — Compilation + full test suite after every group conversion
4. **Consolidate last** — Merge DispatcherServlet mappings and clean up JAX-RS dependencies

### DispatcherServlet Expansion Strategy

For each converted controller group, add a new `<servlet-mapping>` entry for the existing
`web` DispatcherServlet in `web.xml`. This way, converted controllers are immediately served
while unconverted groups remain on CXF. After all groups are converted, consolidate to a
single `/` mapping.

The existing `web-servlet.xml` component scan covers `se.inera.intyg.webcert.web.web.controller`
(parent package of all controller packages), so newly `@RestController`-annotated classes are
automatically discovered by the DispatcherServlet without configuration changes.

---

## Annotation Mapping Reference

| JAX-RS | Spring MVC |
|--------|------------|
| `@Path("/x")` on class | `@RequestMapping("/prefix/x")` on class ¹ |
| `@Path("/y")` on method | path in `@GetMapping("/y")` etc. |
| `@GET` | `@GetMapping` |
| `@POST` | `@PostMapping` |
| `@PUT` | `@PutMapping` |
| `@DELETE` | `@DeleteMapping` |
| `@Produces(APPLICATION_JSON)` | `produces = APPLICATION_JSON_VALUE` |
| `@Consumes(APPLICATION_JSON)` | `consumes = APPLICATION_JSON_VALUE` |
| `@PathParam("id")` | `@PathVariable("id")` |
| `@QueryParam("name")` | `@RequestParam(name = "name", required = false)` ² |
| `@FormParam("field")` | `@RequestParam("field")` |
| `@HeaderParam("X")` | `@RequestHeader("X")` |
| `@DefaultValue("10")` | `defaultValue = "10"` in `@RequestParam` |
| `@Context HttpServletRequest` | `HttpServletRequest` as method parameter |
| `@Context UriInfo` | `HttpServletRequest` + `UriComponentsBuilder` |
| `Response.ok(entity).build()` | `ResponseEntity.ok(entity)` |
| `Response.ok().build()` | `ResponseEntity.ok().build()` |
| `Response.status(400).build()` | `ResponseEntity.badRequest().build()` |
| `Response.seeOther(uri).build()` | `ResponseEntity.status(303).location(uri).build()` |
| `Response.serverError().build()` | `ResponseEntity.internalServerError().build()` |

¹ The class-level `@RequestMapping` must include the servlet prefix (e.g., `/api`) since
the controller is no longer in a CXF servlet mapped to that prefix.

² **Critical:** JAX-RS `@QueryParam` without `@DefaultValue` results in `null`. Spring MVC
`@RequestParam` defaults to `required=true` and returns 400 if missing. **Always set
`required = false`** when converting `@QueryParam` to preserve JAX-RS behavior.

---

## Progress Tracker

| Sub-step | Title | Risk | Status |
|----------|-------|------|--------|
| **Phase A: Spring MVC Infrastructure** | | | |
| 11.1 | Register CustomObjectMapper for Spring MVC | Low | ✅ DONE |
| 11.2 | Create @RestControllerAdvice — REST exception handler | Medium | ✅ DONE |
| 11.3 | Refactor ReactUriFactory — remove UriInfo dependency | Medium | ✅ DONE |
| 11.4 | Create Spring MVC parameter converters | Low | ✅ DONE |
| 11.5 | Create @ControllerAdvice — redirect exception handler *(depends on 11.3)* | Medium | ✅ DONE |
| **Phase B: Controller Group Conversion** | | | |
| 11.6 | Convert `/internalapi/*` controllers (8 controllers) | Medium | ✅ DONE |
| 11.7 | Convert `/api/*` controllers (22 controllers) | ⚠️ High | ✅ DONE |
| 11.8 | Convert `/moduleapi/*` controllers (3 controllers) | Medium | ✅ DONE |
| 11.9 | Convert `/visa/*` & `/v2/visa/*` controllers (2 controllers) | Medium | ✅ DONE |
| 11.10 | Convert `/webcert/web/user/*` controllers (4 controllers) | Medium | ✅ DONE |
| 11.11 | Convert `/testability/*` controllers (12 controllers) | Low | ✅ DONE |
| 11.12 | Convert `/authtestability/*` controllers (1 controller) | Low | ✅ DONE |
| **Phase C: Consolidation & Cleanup** | | | |
| 11.13 | Consolidate DispatcherServlet — change mapping to `/` | ⚠️ Critical | ⬜ TODO |
| 11.14 | Remove Swagger/ApiScanner JAX-RS endpoint | Low | ⬜ TODO |
| 11.15 | Remove old ExceptionMappers and JAX-RS providers | Medium | ⬜ TODO |
| 11.16 | Remove JAX-RS and CXF REST dependencies | ⚠️ Critical | ⬜ TODO |
| 11.17 | Final verification — build, test, startup | ⚠️ Critical | ⬜ TODO |

---

## Phase A: Spring MVC Infrastructure

### Sub-step 11.1 — Register CustomObjectMapper for Spring MVC

**What:** Configure Spring MVC to use `CustomObjectMapper` for JSON serialization/deserialization.

**Why:** Currently, `CustomObjectMapper` is only registered as a CXF `JacksonJsonProvider`. Spring
MVC uses its own `MappingJackson2HttpMessageConverter` which must be configured with the same
`ObjectMapper` to produce identical JSON output (null handling, date formats, custom serializers).

**Current CXF registration** (`webcert-config.xml` lines 135-143):
```xml
<bean id="objectMapper" class="se.inera.intyg.common.util.integration.json.CustomObjectMapper"/>

<bean id="jacksonJsonProvider" class="com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider">
  <property name="mapper">
    <bean class="se.inera.intyg.common.util.integration.json.CustomObjectMapper"/>
  </property>
</bean>
```

**Changes:**
1. Create `web/src/main/java/se/inera/intyg/webcert/web/config/WebMvcConfiguration.java`:
   ```java
   @Configuration
   public class WebMvcConfiguration implements WebMvcConfigurer {

       @Override
       public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
           MappingJackson2HttpMessageConverter converter =
               new MappingJackson2HttpMessageConverter();
           converter.setObjectMapper(new CustomObjectMapper());
           converters.add(0, converter);
       }
   }
   ```
2. This class will also be used in sub-steps 11.4 (parameter converters).

**Note:** The existing `objectMapper` bean in `webcert-config.xml` remains — it's used elsewhere.
The `WebMvcConfiguration` ensures Spring MVC's HTTP message conversion uses the same settings.

**Verification:** `./gradlew compileJava`

---

### Sub-step 11.2 — Create @RestControllerAdvice — REST exception handler

**What:** Create a `@RestControllerAdvice` class that replaces the JAX-RS
`WebcertRestExceptionHandler` (`ExceptionMapper<RuntimeException>`).

**Why:** Spring MVC uses `@ExceptionHandler` methods in `@ControllerAdvice` classes instead of
JAX-RS `ExceptionMapper`.

**Current behavior to preserve exactly:**
- `WebCertServiceException` → HTTP 500 + JSON `{"errorCode": "<code>", "message": "<msg>"}`
- `AuthoritiesException` → HTTP 500 + JSON `{"errorCode": "AUTHORIZATION_PROBLEM", "message": "<msg>"}`
- Other `RuntimeException` → HTTP 500 + JSON `{"errorCode": "UNKNOWN_INTERNAL_PROBLEM", "message": "<msg>"}`

**Response DTO:** The existing handler uses `WebcertRestExceptionResponse` — reuse this class.

**Changes:**
1. Create `web/src/main/java/se/inera/intyg/webcert/web/web/handlers/WebcertRestExceptionHandlerAdvice.java`:
   ```java
   @RestControllerAdvice(basePackages = {
       "se.inera.intyg.webcert.web.web.controller.api",
       "se.inera.intyg.webcert.web.web.controller.facade",
       "se.inera.intyg.webcert.web.web.controller.moduleapi",
       "se.inera.intyg.webcert.web.web.controller.internalapi",
       "se.inera.intyg.webcert.web.web.controller.authtestability",
       "se.inera.intyg.webcert.web.web.controller.testability",
       "se.inera.intyg.webcert.web.web.controller.testability.facade"
   })
   public class WebcertRestExceptionHandlerAdvice {

       @ExceptionHandler(WebCertServiceException.class)
       public ResponseEntity<WebcertRestExceptionResponse>
           handleWebCertServiceException(WebCertServiceException ex) { ... }

       @ExceptionHandler(AuthoritiesException.class)
       public ResponseEntity<WebcertRestExceptionResponse>
           handleAuthoritiesException(AuthoritiesException ex) { ... }

       @ExceptionHandler(RuntimeException.class)
       public ResponseEntity<WebcertRestExceptionResponse>
           handleRuntimeException(RuntimeException ex) { ... }
   }
   ```

**⚠️ Important:** Must NOT apply to integration/legacyintegration controllers — those use
redirect-based error handling (sub-step 11.3).

**⚠️ Behavioral change for testability:** The testability CXF context (`webcert-testability-api-context.xml`)
currently registers **no exception handler at all** — only `jacksonJsonProvider`. Adding testability
to this advice is an **intentional improvement**: testability endpoints will now return proper JSON
error responses instead of CXF's default unhandled exception format. This is safe and desirable.
Note also that `CertificateTestabilityController` and `FakeLoginTestabilityController` reside in
the `testability.facade` sub-package and are covered by the `testability.facade` entry above.

**Verification:** `./gradlew compileJava`

---

### Sub-step 11.3 — Refactor ReactUriFactory — remove UriInfo dependency

**What:** Change `ReactUriFactory` to accept `HttpServletRequest` instead of JAX-RS `UriInfo`.

**Why:** `ReactUriFactory` is used by integration controllers and the redirect exception handler.
It currently uses `UriInfo.getBaseUriBuilder()` which is a JAX-RS API.

**⚠️ This must be done before sub-step 11.5** (the redirect exception handler calls the new
`HttpServletRequest`-based API).

**Pre-work — identify all callers:**
```bash
grep -r "reactUriFactory\." --include="*.java" web/src/main/java/
```
Expected callers: `WebcertRedirectIntegrationExceptionHandler` and potentially
`IntygIntegrationController`, `LaunchIntegrationController`, `FragaSvarUthoppController`.
Confirm before changing signatures.

**Current API:**
```java
public URI uriForCertificate(UriInfo uriInfo, String certificateId)
public URI uriForCertificateWithSignError(UriInfo uriInfo, String certId, SignaturStatus status)
public URI uriForErrorResponse(UriInfo uriInfo, String errorReason)
public URI uriForCertificateQuestions(UriInfo uriInfo, String certificateId)
public URI uriForUnitSelection(UriInfo uriInfo, String certificateId)
```

**New API:**
```java
public URI uriForCertificate(HttpServletRequest request, String certificateId)
public URI uriForCertificateWithSignError(HttpServletRequest request, String certId, SignaturStatus status)
public URI uriForErrorResponse(HttpServletRequest request, String errorReason)
public URI uriForCertificateQuestions(HttpServletRequest request, String certificateId)
public URI uriForUnitSelection(HttpServletRequest request, String certificateId)
```

**URI building replacement:**
- Old: `uriInfo.getBaseUriBuilder().replacePath("/")`
- New: `ServletUriComponentsBuilder.fromRequest(request).replacePath("/")` (from Spring Web)

**Files to modify:**
- `ReactUriFactory.java` — change method signatures and URI building
- `ReactUriFactoryTest.java` — replace `UriInfo` mocks with `MockHttpServletRequest`
- All callers confirmed by the pre-work grep above

**Verification:** `./gradlew compileJava` + `./gradlew :web:test --tests "*ReactUriFactory*"`

---

### Sub-step 11.4 — Create Spring MVC parameter converters

**What:** Replace JAX-RS `LocalDateTimeHandler` (`ParamConverterProvider`) with Spring MVC
`Converter` implementations.

**Why:** Spring MVC uses `Converter<String, T>` registered via `WebMvcConfigurer.addFormatters()`
instead of JAX-RS `ParamConverterProvider`.

**Current behavior (LocalDateTimeHandler):**
- If string contains "T" → parse as `LocalDateTime` via `ISO_DATE_TIME`
- Otherwise → parse as `LocalDate` via `ISO_DATE` → convert to `LocalDateTime.atStartOfDay()`
- toString format: `yyyy-MM-dd'T'HH:mm:ss.SSS`

**Changes:** Add to `WebMvcConfiguration.java` (created in 11.1):
```java
@Override
public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(String.class, LocalDateTime.class, source -> {
        if (source.contains("T")) {
            return LocalDateTime.parse(source, DateTimeFormatter.ISO_DATE_TIME);
        }
        return LocalDate.parse(source, DateTimeFormatter.ISO_DATE).atStartOfDay();
    });
    registry.addConverter(String.class, LocalDate.class, source ->
        LocalDate.parse(source, DateTimeFormatter.ISO_DATE));
}
```

**Verification:** `./gradlew compileJava`

---

### Sub-step 11.5 — Create @ControllerAdvice — redirect exception handler

**What:** Create a `@ControllerAdvice` class replacing `WebcertRedirectIntegrationExceptionHandler`.

**Why:** Integration controllers (`/visa/*`, `/webcert/web/user/*`) respond with HTTP 303 redirects
to error pages instead of JSON responses.

**Dependency:** Sub-step 11.3 (ReactUriFactory refactoring) must be completed first — this
handler calls `ReactUriFactory.uriForErrorResponse(request, ...)` which uses the new
`HttpServletRequest`-based signature introduced in 11.3.

**Current behavior to preserve:**
- `MissingSubscriptionException` → redirect with `errorReason=auth-exception-subscription`
- `AuthoritiesException` → redirect with `errorReason=auth-exception` (or `-sekretessmarkering`,
  `-user-already-active` based on message content)
- `WebCertServiceException(PU_PROBLEM)` → redirect with `errorReason=pu-problem`
- Other `WebCertServiceException` → redirect with mapped `errorReason`
- Other `RuntimeException` → redirect with `errorReason=unknown`

**⚠️ Field injection change:** The existing `WebcertRedirectIntegrationExceptionHandler` has a
field-level `@Context UriInfo uriInfo` injection (JAX-RS context injection). The new class must
**remove this field entirely** — there is no equivalent field injection in Spring MVC.
Instead, inject `HttpServletRequest` as a **method parameter** in each `@ExceptionHandler` method.

**Changes:**
1. Create `web/src/main/java/se/inera/intyg/webcert/web/web/handlers/WebcertRedirectExceptionHandlerAdvice.java`:
   ```java
   @ControllerAdvice(basePackages = {
       "se.inera.intyg.webcert.web.web.controller.integration",
       "se.inera.intyg.webcert.web.web.controller.legacyintegration"
   })
   public class WebcertRedirectExceptionHandlerAdvice {

       @Autowired
       private ReactUriFactory reactUriFactory;

       @ExceptionHandler(RuntimeException.class)
       public ResponseEntity<Void> handleException(
               RuntimeException ex, HttpServletRequest request) {
           // Determine errorReason from exception type/message
           // Build redirect URI via reactUriFactory.uriForErrorResponse(request, reason)
           // Return ResponseEntity.status(303).location(uri).build()
       }
   }
   ```

**Verification:** `./gradlew compileJava`

---

## Phase B: Controller Group Conversion

### General Approach per Group

For each controller group, perform these steps in order:

1. **Convert annotations** on all controller classes:
   - Add `@RestController` (or `@Controller` for redirect-only controllers)
   - Add `@RequestMapping("/prefix")` at class level — combines servlet prefix + CXF server
     address + original `@Path` value
   - Replace method annotations: `@GET`→`@GetMapping`, `@POST`→`@PostMapping`, etc.
   - Replace parameter annotations: `@PathParam`→`@PathVariable`,
     `@QueryParam`→`@RequestParam(required=false)`, `@FormParam`→`@RequestParam`, etc.
   - Replace `@Context HttpServletRequest` with plain `HttpServletRequest` method parameter
   - Replace `@Context UriInfo` with `HttpServletRequest` (already handled after 11.5)
   - Convert `Response` return types to `ResponseEntity<T>` or direct return type
   - Remove `@Produces`/`@Consumes` from methods if they're just `APPLICATION_JSON` (Spring MVC
     defaults to JSON with Jackson on classpath) — or move to `produces`/`consumes` attributes

2. **Update test files** that reference `jakarta.ws.rs.core.Response`:
   - Replace `Response` with `ResponseEntity<T>`
   - Replace `response.getStatus()` with `response.getStatusCode().value()`
   - Replace `(T) response.getEntity()` with `response.getBody()`

3. **Update `web.xml`:**
   - Add: `<servlet-mapping><servlet-name>web</servlet-name><url-pattern>/prefix/*</url-pattern></servlet-mapping>`
   - Remove: The CXF `<servlet>` definition block
   - Remove: The CXF `<servlet-mapping>` block

4. **Delete CXF servlet XML file** (after grep confirms no remaining references)

5. **Verify:** `./gradlew compileJava` + `./gradlew test --parallel`

**Note on root-context duplicate beans:** `webcert-config.xml` contains
`<context:component-scan base-package="se.inera.intyg.webcert.web"/>` — this is the ROOT
application context and it scans the entire `se.inera.intyg.webcert.web` package, including all
controller sub-packages. This means that once a controller is annotated with `@RestController`,
the bean exists in **both** the root context AND the DispatcherServlet child context
(`web-servlet.xml` scans `se.inera.intyg.webcert.web.web.controller`). This is a pre-existing
condition (the current `PageController` already has this dual-context presence), and Spring MVC
handler resolution correctly prefers the child context. Accept this during migration; the root
context's broad scan is cleaned up in Step 12.

---

### Sub-step 11.6 — Convert `/internalapi/*` controllers (8 controllers)

**Start here — least user-facing, moderate complexity.**

**Controllers:**

| Class | @Path | Full URL After Conversion |
|-------|-------|---------------------------|
| IntegratedUnitsApiController | `/integratedUnits` | `/internalapi/integratedUnits` |
| IntygInfoApiController | `/intygInfo` | `/internalapi/intygInfo` |
| TestCertificateController | `/testCertificate` | `/internalapi/testCertificate` |
| TermsApiController | `/terms` | `/internalapi/terms` |
| EraseApiController | `/v1/certificates` | `/internalapi/v1/certificates` |
| UnansweredCommunicationController | `/unanswered-communication` | `/internalapi/unanswered-communication` |
| CertificateInternalApiController | `/certificate` | `/internalapi/certificate` |
| NotificationController | `/notification` | `/internalapi/notification` |

**Special note:** `UnansweredCommunicationController` already has `@RestController` annotation
(it's a mixed JAX-RS/Spring MVC class). Remove all JAX-RS annotations, keep `@RestController`.

**Base class:** `EraseApiController` extends `AbstractApiController`. The base class uses
`@Autowired` (Spring) — no JAX-RS annotations to change there.

**Files to modify:**
- 8 controller Java files
- Test files: `CertificateInternalApiControllerTest`, `TermsApiControllerTest`,
  `UnansweredCommunicationControllerTest`, `NotificationControllerTest`

**Files to delete:**
- `web/src/main/webapp/WEB-INF/internalapi-cxf-servlet.xml`

**Web.xml changes:**
- Add `<url-pattern>/internalapi/*</url-pattern>` to `web` DispatcherServlet mapping
- Remove `internalapi` CXF servlet definition and mapping

**Note:** `internalapi-cxf-servlet.xml` has two `<context:property-placeholder>` entries:
`classpath:application.properties` and `${dev.config.file}` (a dev-environment override).
Verify that the root application context (loaded from `webcert-config.xml`) already resolves
both. The `${dev.config.file}` variable is typically only set in dev-profile contexts — confirm
it is provided at the root level or is not referenced by any bean that gets moved to the root
context. If not, add it to the dev profile configuration.

**Verification:** `./gradlew compileJava` + `./gradlew test --parallel`

---

### Sub-step 11.7 — Convert `/api/*` controllers (22 controllers) ⚠️ High Risk

**Largest group — the main user-facing REST API. Contains 3 CXF jaxrs:server instances.**

#### Server 1: address=`/` — 20 controllers

| Class | Package | @Path | Full URL |
|-------|---------|-------|----------|
| JsLogApiController | api | `/jslog` | `/api/jslog` |
| UserApiController | api | `/anvandare` | `/api/anvandare` |
| FmbApiController | api | `/fmb` | `/api/fmb` |
| SrsApiController | api | `/srs` | `/api/srs` |
| ConfigApiController | api | `/config` | `/api/config` |
| SignatureApiController | api | `/signature` | `/api/signature` |
| SessionStatusController | api | `/session-auth-check` | `/api/session-auth-check` |
| SubscriptionController | api | `/subscription` | `/api/subscription` |
| InvalidateSessionApiController | api | `/v1/session` | `/api/v1/session` |
| CertificateController | facade | `/certificate` | `/api/certificate` |
| QuestionController | facade | `/question` | `/api/question` |
| UserController | facade | `/user` | `/api/user` |
| IcfController | facade | `/icf` | `/api/icf` |
| FMBController | facade | `/fmb` | `/api/fmb` |
| LogController | facade | `/log` | `/api/log` |
| ConfigController | facade | `/configuration` | `/api/configuration` |
| PatientController | facade | `/patient` | `/api/patient` |
| ListController | facade | `/list` | `/api/list` |
| ListConfigController | facade | `/list/config` | `/api/list/config` |
| CertificateTypeController | facade | `/certificate/type` | `/api/certificate/type` |

**⚠️ Path collision note:** Both `FmbApiController` (@Path `/fmb`) and `FMBController`
(@Path `/fmb`) map to `/api/fmb`. In CXF, multiple resource classes with the same base @Path
are merged by method-level path + HTTP method. In Spring MVC, two `@RestController` classes with
`@RequestMapping("/api/fmb")` work as long as their method-level mappings don't collide.
**Verify method-level paths are distinct before converting.**

**CXF `default.wae.mapper.least.specific` property:** All 3 `jaxrs:server` instances in
`api-cxf-servlet.xml` set `default.wae.mapper.least.specific=true`. This was a CXF-specific
workaround (WEBCERT-1978) to prevent CXF's built-in `WebApplicationExceptionMapper` from being
too specific and intercepting exceptions before `webcertRestExceptionHandler` could handle them.
Spring MVC's `@ExceptionHandler` resolution is type-specificity-based by design — no equivalent
workaround is needed. This property simply disappears when the CXF servlet is removed.

#### Server 2: address=`/fake` (profile `!prod`) — 1 controller

| Class | @Path | Full URL |
|-------|-------|----------|
| FakeSignatureApiController | `/signature` | `/api/fake/signature` |

Add `@Profile("!prod")` to this controller — it must only be active in non-production.

#### Server 3: address=`/private-practitioner` — 1 controller

| Class | @Path | Full URL |
|-------|-------|----------|
| PrivatePractitionerApiController | (sub-paths) | `/api/private-practitioner/...` |

**Special annotations in this group:**
- `@Context HttpServletRequest` — 15+ instances across controllers (→ method param)
- `@Context UriInfo` — in `SignatureApiController` (→ `HttpServletRequest`)
- `@FormParam` — in `SignatureApiController` (2 instances)
- `@QueryParam` with `@DefaultValue` — in `SrsApiController` (4+6 instances)
- `@QueryParam` — in `FmbApiController` (5 instances)

**Files to modify:**
- 22 controller Java files
- Test files: `ConfigApiControllerTest`, `FmbApiControllerTest`,
  `InvalidateSessionApiControllerTest`, `JsLogApiControllerTest`,
  `PrivatePractitionerApiControllerTest`, `SignatureApiControllerTest`,
  `UserApiControllerTest`, `CertificateControllerTest`, `QuestionControllerTest`,
  and all other facade controller tests

**Files to delete:**
- `web/src/main/webapp/WEB-INF/api-cxf-servlet.xml`

**Web.xml changes:**
- Add `<url-pattern>/api/*</url-pattern>` to `web` DispatcherServlet mapping
- Remove `api` CXF servlet definition and mapping

**Verification:** `./gradlew compileJava` + `./gradlew test --parallel`

---

### Sub-step 11.8 — Convert `/moduleapi/*` controllers (3 controllers)

| Class | @Path | Full URL |
|-------|-------|----------|
| IntygModuleApiController | `/intyg` | `/moduleapi/intyg` |
| StatModuleApiController | `/stat` | `/moduleapi/stat` |
| DiagnosModuleApiController | `/diagnos` | `/moduleapi/diagnos` |

**Special annotations:**
- `IntygModuleApiController`: `@Context HttpServletRequest` (1 instance)

**Note on `localDateTimeHandler`:** `moduleapi-cxf-servlet.xml` does **not** register
`localDateTimeHandler` as a provider — unlike `api` and `internalapi`. This means moduleapi
controllers never had CXF-based `LocalDateTime`/`LocalDate` parameter conversion. The Spring MVC
`Converter` registered in sub-step 11.4 is still correct to add; it provides consistent
parameter conversion across all controllers and is a safe improvement for this group.

**Base class:** All extend `AbstractApiController`.

**Files to modify:** 3 controller files + 1 test (`StatModuleApiControllerTest`)

**Files to delete:**
- `web/src/main/webapp/WEB-INF/moduleapi-cxf-servlet.xml`

**Web.xml changes:**
- Add `<url-pattern>/moduleapi/*</url-pattern>` to `web` DispatcherServlet
- Remove `moduleapi` CXF servlet definition and mapping

**Verification:** `./gradlew compileJava` + `./gradlew test --parallel`

---

### Sub-step 11.9 — Convert `/visa/*` & `/v2/visa/*` controllers (2 controllers)

**These use redirect-based error handling** (WebcertRedirectIntegrationExceptionHandler).

| Class | Servlet URL | Full URL |
|-------|------------|----------|
| IntygIntegrationController | `/visa/*` | `/visa/...` |
| UserIntegrationController | `/v2/visa/*` | `/v2/visa/...` |

**Special annotations:**
- `IntygIntegrationController`: Heavy use of `@QueryParam` (16), `@FormParam` (15),
  `@DefaultValue` (17), `@Context UriInfo`, `@Context HttpServletRequest`
- `UserIntegrationController`: `@Context HttpServletRequest`

**Note on `localDateTimeHandler`:** `integration-cxf-servlet.xml` does **not** register
`localDateTimeHandler` — only `jacksonJsonProvider` and `webcertRedirectIntegrationExceptionHandler`.

**Note on explicit bean registrations:** `integration-cxf-servlet.xml` has **no component scan**.
Controllers are registered as named beans with explicit IDs (`intygIntegrationController`,
`userIntegrationController`). After conversion, they are discovered automatically by the
DispatcherServlet's parent-package scan. **Pre-conversion check:** grep for
`@Qualifier("intygIntegrationController")`, `@Qualifier("userIntegrationController")`, and
`@Resource(name="...")` with either ID — if found, update those references.

**Note on `<context:property-placeholder>`:** `integration-cxf-servlet.xml` declares:
```xml
<context:property-placeholder location="classpath:application.properties" order="2"/>
```
This placeholder is scoped to the CXF child context. Verify that the root application context
(loaded from `webcert-config.xml`) already resolves all `${...}` properties used in
`IntygIntegrationController` and `UserIntegrationController` — it should, since the root
context also loads `application.properties`. No separate action is needed, but confirm after
this servlet XML is deleted that no `NoSuchBeanDefinitionException` or unresolved placeholder
errors appear at startup.

**Base class:** Both extend `BaseIntegrationController` which provides authority validation.

**⚠️ Note on @FormParam + @GET:** `IntygIntegrationController` has methods annotated with both
`@GET` and `@POST` for the same path, using `@FormParam` for POST and `@QueryParam` for GET.
In Spring MVC, create separate handler methods for GET and POST, or use
`@RequestMapping(method = {GET, POST})` with `@RequestParam` (works for both form and query).

**Files to modify:** 2 controller files + 2 tests (`IntygIntegrationControllerTest`,
`UserIntegrationControllerTest`)

**Files to delete:**
- `web/src/main/webapp/WEB-INF/integration-cxf-servlet.xml`

**Web.xml changes:**
- Add `<url-pattern>/visa/*</url-pattern>` and `<url-pattern>/v2/visa/*</url-pattern>` to
  `web` DispatcherServlet
- Remove `integrationapi` CXF servlet definition and mappings

**Verification:** `./gradlew compileJava` + `./gradlew test --parallel`

---

### Sub-step 11.10 — Convert `/webcert/web/user/*` controllers (4 controllers)

**Legacy "uthopp" integration controllers.** Also use redirect-based error handling.

| Class | @Path | Full URL |
|-------|-------|----------|
| FragaSvarUthoppController | (various) | `/webcert/web/user/...` |
| PrivatePractitionerFragaSvarUthoppController | (inherits) | `/webcert/web/user/...` |
| CertificateIntegrationController | `/basic-certificate` | `/webcert/web/user/basic-certificate/...` |
| LaunchIntegrationController | (various) | `/webcert/web/user/...` |

**Special annotations:**
- `FragaSvarUthoppController`: `@Context UriInfo` (2 instances), `@QueryParam` (2),
  `@FormParam` (possibly)
- `LaunchIntegrationController`: `@Context UriInfo` (2 instances)

**Note on `localDateTimeHandler`:** `uthopp-integration-cxf-servlet.xml` does **not** register
`localDateTimeHandler` — only `jacksonJsonProvider` and `webcertRedirectIntegrationExceptionHandler`.

**Note on explicit bean registrations:** `uthopp-integration-cxf-servlet.xml` has **no component
scan**. All 4 controllers are registered as named beans with explicit IDs:
`fragaSvarUthoppController`, `privatePractitionerFragaSvarUthoppController`,
`certificateIntegrationController`, `launchIntegrationController`. **Pre-conversion check:** grep
for `@Qualifier` or `@Resource(name="...")` with any of these IDs and update if found.

Note that `LaunchIntegrationController` is in `se.inera.intyg.webcert.web.web.controller.integration`
while the other three are in `...controller.legacyintegration`. Both packages are covered by the
DispatcherServlet's parent-package scan and by the `@ControllerAdvice` in sub-step 11.3.

**Note on `<context:property-placeholder>`:** `uthopp-integration-cxf-servlet.xml` declares:
```xml
<context:property-placeholder location="classpath:application.properties" order="2"/>
```
As with `integration-cxf-servlet.xml` (sub-step 11.9), verify that the root application context
resolves all `${...}` properties used by these controllers after the servlet XML is deleted.

**Inheritance:** `PrivatePractitionerFragaSvarUthoppController` and
`CertificateIntegrationController` both extend `FragaSvarUthoppController`.

**Files to modify:** 4 controller files + 2 tests (`CertificateIntegrationControllerTest`,
`FragaSvarUthoppControllerTest`)

**Files to delete:**
- `web/src/main/webapp/WEB-INF/uthopp-integration-cxf-servlet.xml`

**Web.xml changes:**
- Add `<url-pattern>/webcert/web/user/*</url-pattern>` to `web` DispatcherServlet
- Remove `uthoppintegrationapi` CXF servlet definition and mapping

**Verification:** `./gradlew compileJava` + `./gradlew test --parallel`

---

### Sub-step 11.11 — Convert `/testability/*` controllers (12 controllers)

**Gated by Spring profile.** Currently registered via `webcert-testability-api-context.xml`
(which is imported by `testability-cxf-servlet.xml` — the servlet file itself contains only
the import; all beans are in the context file).

| Class | Package | @Path | Full URL |
|-------|---------|-------|----------|
| ArendeResource | `testability` | `/arendetest` | `/testability/arendetest` |
| FragaSvarResource | `testability` | `/fragasvartest` | `/testability/fragasvartest` |
| LogResource | `testability` | `/logtest` | `/testability/logtest` |
| IntygResource | `testability` | `/intygtest` | `/testability/intygtest` |
| UserAgreementResource | `testability` | `/useragreementtest` | `/testability/useragreementtest` |
| FmbResource | `testability` | `/fmbtest` | `/testability/fmbtest` |
| IntegreradEnhetResource | `testability` | `/integreradenhettest` | `/testability/integreradenhettest` |
| CertificateTestabilityController | `testability.facade` | `/certificate` | `/testability/certificate` |
| FakeLoginTestabilityController | `testability.facade` | `/fake` | `/testability/fake` |
| ConfigurationResource | `testability` | `/configuration` | `/testability/configuration` |
| EventResource | `testability` | `/event` | `/testability/event` |
| ReferensResource | `testability` | `/referenstest` | `/testability/referenstest` |

**Profile:** The CXF context file has `profile="dev,testability-api"`. Add
`@Profile({"dev", "testability-api"})` to **all 12 controller classes**, including
`CertificateTestabilityController` and `FakeLoginTestabilityController` which are in the
`testability.facade` sub-package.

**Note on exception handling:** `webcert-testability-api-context.xml` registers only
`jacksonJsonProvider` — **no exception handler**. After migration, these controllers gain
proper JSON error responses via the `@RestControllerAdvice` in sub-step 11.2 (which covers
both `testability` and `testability.facade` packages). This is an intentional improvement.

**Files to modify:** 12 controller Java files (no test files exist for testability controllers)

**Files to delete:**
- `web/src/main/webapp/WEB-INF/testability-cxf-servlet.xml`
- `web/src/main/resources/webcert-testability-api-context.xml`

**Web.xml changes:**
- Add `<url-pattern>/testability/*</url-pattern>` to `web` DispatcherServlet
- Remove `testability` CXF servlet definition and mapping

**Verification:** `./gradlew compileJava` + `./gradlew test --parallel`

---

### Sub-step 11.12 — Convert `/authtestability/*` controllers (1 controller)

| Class | @Path | Full URL |
|-------|-------|----------|
| UserResource | `/user` | `/authtestability/user` |

**Profile:** Original XML uses `profile="!prod"`. Add `@Profile("!prod")` to the controller.

**Files to modify:** 1 controller Java file (no test files)

**Files to delete:**
- `web/src/main/webapp/WEB-INF/authtestability-cxf-servlet.xml`

**Web.xml changes:**
- Add `<url-pattern>/authtestability/*</url-pattern>` to `web` DispatcherServlet
- Remove `authtestability` CXF servlet definition and mapping

**Verification:** `./gradlew compileJava` + `./gradlew test --parallel`

---

## Phase C: Consolidation & Cleanup

### Sub-step 11.13 — Consolidate DispatcherServlet mapping ⚠️ Critical

**What:** Replace all individual DispatcherServlet URL patterns with a single `/` mapping.

**Why:** After all CXF REST servlets are removed, the DispatcherServlet should serve all
non-SOAP requests.

**Changes in `web.xml`:**
1. Replace all `web` servlet-mapping entries (added during Phase B) with:
   ```xml
   <servlet-mapping>
       <servlet-name>web</servlet-name>
       <url-pattern>/</url-pattern>
   </servlet-mapping>
   ```
2. Keep `services` CXF SOAP servlet at `/services/*` — prefix mappings take precedence over `/`
3. Keep `metrics` servlet at `/metrics`

**Consider merging `web-servlet.xml`:**
- The `<mvc:annotation-driven/>` and component scan could move to `WebMvcConfiguration.java` or
  the main application context
- This may be deferred to Step 12 (XML → Java config conversion)

**Filter chain verification:** All existing filter URL patterns (`/api/*`, `/moduleapi/*`,
`/internalapi/*`, etc.) remain valid because the URL paths haven't changed — only the serving
servlet has changed from CXF to DispatcherServlet.

**⚠️ Forward reference — `services-cxf-servlet.xml` stub component scans (Step 14 prep):**
The SOAP servlet (`services-cxf-servlet.xml`) is kept unchanged in Step 11 but contains two
component scans that run in the CXF child context:
- `se.inera.intyg.webcert.infra.srs.stub.config`
- `se.inera.intyg.webcert.infra.ia.stub.config`

When Step 14 migrates the CXF SOAP servlet to a Spring Boot `ServletRegistrationBean`, these
stub package scans will no longer have a Spring child context. They must be moved to the main
application context or a profile-gated `@Configuration` class. Flag this for Step 14 planning.

**Verification:** `./gradlew test --parallel` + manual startup check

---

### Sub-step 11.14 — Remove Swagger/ApiScanner JAX-RS endpoint

**What:** Remove the JAX-RS-based `ApiScanner` class and `swagger-api-context.xml`.

**Why:** The current Swagger implementation uses JAX-RS `@Path` annotations and `ReflectiveJaxrsScanner`
which won't work after JAX-RS removal. Swagger/OpenAPI replacement (SpringDoc) is planned for Step 14.

**Current URL:** `swagger-api-context.xml` is imported by `services-cxf-servlet.xml` (the SOAP
CXF servlet mapped to `/services/*`). The JAX-RS server inside has `address="/swagger"`, making the
full URL **`/services/swagger`** (active on profile `!prod` only). After this step, that endpoint
is gone until SpringDoc is added in Step 14.

**Changes:**
1. Delete `web/src/main/java/se/inera/intyg/webcert/web/web/controller/swagger/ApiScanner.java`
2. Delete `web/src/main/resources/swagger-api-context.xml`
3. Edit `services-cxf-servlet.xml`: remove `<import resource="classpath:/swagger-api-context.xml"/>`
4. Remove `swagger-jaxrs` dependency from `web/build.gradle` (if explicitly declared)

**Pre-deletion grep:** Search for `ApiScanner` and `swagger-api-context` references.

**Verification:** `./gradlew compileJava`

---

### Sub-step 11.15 — Remove old ExceptionMappers and JAX-RS providers

**What:** Delete the original JAX-RS `ExceptionMapper` implementations and the `ParamConverterProvider`
that have been replaced by Spring MVC equivalents.

**Files to delete:**
- `web/src/main/java/.../handlers/WebcertRestExceptionHandler.java`
  (replaced by `WebcertRestExceptionHandlerAdvice.java` in 11.2)
- `web/src/main/java/.../handlers/WebcertRedirectIntegrationExceptionHandler.java`
  (replaced by `WebcertRedirectExceptionHandlerAdvice.java` in 11.3)
- `web/src/main/java/.../handlers/LocalDateTimeHandler.java`
  (replaced by converters in `WebMvcConfiguration.java` in 11.4)

**Pre-deletion checks:**
1. Grep for each class name across all Java, XML, and test files
2. Verify all CXF servlet XML files that referenced these as `<ref bean="..."/>` have been deleted
3. Remove any remaining bean definitions in `webcert-config.xml` (the `jacksonJsonProvider` and
   handler beans defined there)

**Also clean up `webcert-config.xml`:**
- Remove `<bean id="jacksonJsonProvider" ...>` definition (no longer needed by Spring MVC)
- Remove handler bean definitions if present
- Keep `<bean id="objectMapper" ...>` if still used by non-CXF code

**Verification:** `./gradlew compileJava` + `./gradlew test --parallel`

---

### Sub-step 11.16 — Remove JAX-RS and CXF REST dependencies ⚠️ Critical

**What:** Remove JAX-RS API and CXF JAX-RS runtime dependencies that are no longer needed.

**Dependencies to evaluate in `web/build.gradle`:**

| Dependency | Action | Reason |
|------------|--------|--------|
| `jakarta.ws.rs:jakarta.ws.rs-api` | **Remove** | No JAX-RS annotations remain |
| `com.fasterxml.jackson.jakarta.rs:jackson-jakarta-rs-json-provider` | **Remove** | Was CXF JSON provider |
| `org.apache.cxf:cxf-rt-frontend-jaxrs` | **Remove** | CXF JAX-RS runtime |
| `org.apache.cxf:cxf-rt-frontend-jaxws` | **Keep** | Needed for SOAP endpoints |
| `org.apache.cxf:cxf-core` | **Keep** | Needed for SOAP |
| `org.apache.cxf:cxf-rt-transports-http` | **Keep** | Needed for SOAP |

**Also check `infra/build.gradle`:**
- `jackson-jakarta-rs-json-provider` — may still be needed by SRS/IA stub configurations
  (which use `JacksonJsonProvider` in `JAXRSServerFactoryBean`). If SRS/IA stubs are served via
  the CXF SOAP servlet context, this dependency stays in `infra/build.gradle`.

**⚠️ Caution:** Run `./gradlew dependencies` to check for transitive JAX-RS dependencies.
Some libraries may pull in `jakarta.ws.rs-api` transitively.

**Verification:** `./gradlew clean build` (full build including all tests)

---

### Sub-step 11.17 — Final verification ⚠️ Critical

1. **Full clean build:**
   ```bash
   ./gradlew clean build
   ```
   Expected: zero compilation errors, all tests pass.

2. **Startup check:** Start the application and verify:
   - No `UnsatisfiedDependencyException`, `NoSuchBeanDefinitionException`, or
     `BeanDefinitionOverrideException`
   - Application context loads successfully
   - No HTTP 404 or 500 on previously-working endpoints

3. **Spot-check endpoints:**
   - `GET /api/config` → configuration JSON
   - `GET /api/session-auth-check/ping` → session status
   - `GET /internalapi/terms` → terms data
   - `POST /moduleapi/stat` → statistics
   - `GET /visa/intyg/...` → redirect (with appropriate parameters)
   - `GET /authtestability/user` → user data (non-prod profile)
   - SOAP: `POST /services/create-draft-certificate/v3.0` → still responds

4. **Verify JSON format preservation:**
   - Date serialization format matches before/after
   - Null handling matches (NON_NULL)
   - Custom types (Temporal, InternalDate) serialize correctly

5. **Verify no stale JAX-RS imports:**
   ```bash
   grep -r "jakarta.ws.rs" --include="*.java" web/src/main/java/
   ```
   Should return zero matches (except possibly SOAP-related usage if any).

6. **Verify filter chain works:**
   - `unitSelectedAssuranceFilter` still applies to `/api/*` and `/moduleapi/*`
   - `internalApiFilter` still applies to `/internalapi/*`
   - `springSecurityFilterChain` covers all paths

---

## Risk Assessment

| Risk | Impact | Mitigation |
|------|--------|------------|
| URL path mismatch after conversion | Endpoints unreachable (404) | Carefully combine servlet prefix + server address + @Path into @RequestMapping |
| JSON format differences | API contract broken | Register CustomObjectMapper first (11.1); compare JSON output |
| Missing @QueryParam → required=true | 400 errors for optional params | Always set `required = false` on converted @RequestParam |
| Duplicate beans (parent + child context) | Startup warnings, extra memory | Pre-existing: `webcert-config.xml` scans all of `se.inera.intyg.webcert.web` in the root context; `web-servlet.xml` scans the controller sub-package in the child context. Controllers exist in both — same as the current `PageController`. Spring handler resolution prefers the child context; clean up the root scan in Step 12 |
| Filter chain breaks | Security bypass or 403 errors | URL patterns unchanged; verify filter mappings match |
| Profile-gated controllers missing | Testability endpoints unavailable | Match @Profile annotations to original XML profiles exactly |
| FmbApiController / FMBController path collision | Ambiguous mapping exception | Verify method-level paths are distinct |
| CXF extension mappings (`.json` suffix) lost | Clients using suffix fail | Verify no clients use suffix-based content negotiation |
| Property placeholder removal (internalapi XML) | Missing config values | Verify parent context provides all needed properties |

---

## Files Summary

### Files to create (~3-4)
- `web/src/main/java/.../config/WebMvcConfiguration.java`
- `web/src/main/java/.../handlers/WebcertRestExceptionHandlerAdvice.java`
- `web/src/main/java/.../handlers/WebcertRedirectExceptionHandlerAdvice.java`

### Files to modify (~55-60)
- ~52 controller Java files (annotation conversion)
- ~12 test files (Response type changes)
- `ReactUriFactory.java` + `ReactUriFactoryTest.java`
- `web.xml` (servlet mappings — multiple edits)
- `web-servlet.xml` (possible merge)
- `services-cxf-servlet.xml` (remove swagger import)
- `webcert-config.xml` (remove CXF provider beans)
- Build files (dependency removal)

### Files to delete (~13)
- 7 CXF servlet XML files (`api-`, `moduleapi-`, `internalapi-`, `integration-`,
  `uthopp-integration-`, `testability-`, `authtestability-cxf-servlet.xml`)
- `webcert-testability-api-context.xml`
- `swagger-api-context.xml`
- `ApiScanner.java`
- `WebcertRestExceptionHandler.java`
- `WebcertRedirectIntegrationExceptionHandler.java`
- `LocalDateTimeHandler.java`
