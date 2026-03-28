# Step 11 — Convert JAX-RS Controllers to Spring MVC

## Problem Statement

Webcert currently uses Apache CXF JAX-RS to serve REST endpoints across **7 CXF servlet contexts**
with **~47 controllers**. Each CXF servlet has its own XML configuration file, component scans, and
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
| `api` | `/api/*` | `api-cxf-servlet.xml` | 21 | WebcertRestExceptionHandler |
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

| Provider | Type | Replacement |
|----------|------|-------------|
| `jacksonJsonProvider` | JSON serialization (CustomObjectMapper) | Spring MVC HttpMessageConverter |
| `webcertRestExceptionHandler` | ExceptionMapper for REST | @RestControllerAdvice |
| `webcertRedirectIntegrationExceptionHandler` | ExceptionMapper for redirects | @ControllerAdvice |
| `localDateTimeHandler` | ParamConverterProvider | Spring Converter<String, LocalDateTime> |

### Test Infrastructure

- **34 test files** — all Mockito unit tests with direct method invocation (no MockMvc, no CXF test containers)
- **12 test files** reference `jakarta.ws.rs.core.Response` — require update
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
| 11.1 | Register CustomObjectMapper for Spring MVC | Low | ⬜ TODO |
| 11.2 | Create @RestControllerAdvice — REST exception handler | Medium | ⬜ TODO |
| 11.3 | Create @ControllerAdvice — redirect exception handler | Medium | ⬜ TODO |
| 11.4 | Create Spring MVC parameter converters | Low | ⬜ TODO |
| 11.5 | Refactor ReactUriFactory — remove UriInfo dependency | Medium | ⬜ TODO |
| **Phase B: Controller Group Conversion** | | | |
| 11.6 | Convert `/internalapi/*` controllers (8 controllers) | Medium | ⬜ TODO |
| 11.7 | Convert `/api/*` controllers (21 controllers) | ⚠️ High | ⬜ TODO |
| 11.8 | Convert `/moduleapi/*` controllers (3 controllers) | Medium | ⬜ TODO |
| 11.9 | Convert `/visa/*` & `/v2/visa/*` controllers (2 controllers) | Medium | ⬜ TODO |
| 11.10 | Convert `/webcert/web/user/*` controllers (4 controllers) | Medium | ⬜ TODO |
| 11.11 | Convert `/testability/*` controllers (12 controllers) | Low | ⬜ TODO |
| 11.12 | Convert `/authtestability/*` controllers (1 controller) | Low | ⬜ TODO |
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
       "se.inera.intyg.webcert.web.web.controller.testability"
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

**Verification:** `./gradlew compileJava`

---

### Sub-step 11.3 — Create @ControllerAdvice — redirect exception handler

**What:** Create a `@ControllerAdvice` class replacing `WebcertRedirectIntegrationExceptionHandler`.

**Why:** Integration controllers (`/visa/*`, `/webcert/web/user/*`) respond with HTTP 303 redirects
to error pages instead of JSON responses.

**Current behavior to preserve:**
- `MissingSubscriptionException` → redirect with `errorReason=auth-exception-subscription`
- `AuthoritiesException` → redirect with `errorReason=auth-exception` (or `-sekretessmarkering`,
  `-user-already-active` based on message content)
- `WebCertServiceException(PU_PROBLEM)` → redirect with `errorReason=pu-problem`
- Other `WebCertServiceException` → redirect with mapped `errorReason`
- Other `RuntimeException` → redirect with `errorReason=unknown`

**Dependency:** Sub-step 11.5 (ReactUriFactory refactoring) should be done first, since this
handler calls `ReactUriFactory.uriForErrorResponse()`.

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

### Sub-step 11.5 — Refactor ReactUriFactory — remove UriInfo dependency

**What:** Change `ReactUriFactory` to accept `HttpServletRequest` instead of JAX-RS `UriInfo`.

**Why:** `ReactUriFactory` is used by integration controllers and the redirect exception handler.
It currently uses `UriInfo.getBaseUriBuilder()` which is a JAX-RS API.

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
- All callers: `IntygIntegrationController`, `LaunchIntegrationController`,
  `FragaSvarUthoppController`, and any integration controllers that pass `UriInfo`

**Verification:** `./gradlew compileJava` + `./gradlew :web:test --tests "*ReactUriFactory*"`

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

**Note:** `internalapi-cxf-servlet.xml` has a `<context:property-placeholder>` for
`application.properties`. Verify the parent context already provides this — if not, add it.

**Verification:** `./gradlew compileJava` + `./gradlew test --parallel`

---

### Sub-step 11.7 — Convert `/api/*` controllers (21 controllers) ⚠️ High Risk

**Largest group — the main user-facing REST API. Contains 3 CXF jaxrs:server instances.**

#### Server 1: address=`/` — 19 controllers

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
- 21 controller Java files
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

**Gated by Spring profile.** Currently registered via `webcert-testability-api-context.xml`.

| Class | @Path | Full URL |
|-------|-------|----------|
| ArendeResource | `/arendetest` | `/testability/arendetest` |
| FragaSvarResource | `/fragasvartest` | `/testability/fragasvartest` |
| LogResource | `/logtest` | `/testability/logtest` |
| IntygResource | `/intygtest` | `/testability/intygtest` |
| UserAgreementResource | `/useragreementtest` | `/testability/useragreementtest` |
| FmbResource | `/fmbtest` | `/testability/fmbtest` |
| IntegreradEnhetResource | `/integreradenhettest` | `/testability/integreradenhettest` |
| CertificateTestabilityController | `/certificate` | `/testability/certificate` |
| FakeLoginTestabilityController | `/fake-login` | `/testability/fake-login` |
| ConfigurationResource | `/configuration` | `/testability/configuration` |
| EventResource | `/event` | `/testability/event` |
| ReferensResource | `/referenstest` | `/testability/referenstest` |

**Profile:** Original XML uses `profile="dev,testability-api"`. Add `@Profile({"dev", "testability-api"})`
to each controller class.

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

**Verification:** `./gradlew test --parallel` + manual startup check

---

### Sub-step 11.14 — Remove Swagger/ApiScanner JAX-RS endpoint

**What:** Remove the JAX-RS-based `ApiScanner` class and `swagger-api-context.xml`.

**Why:** The current Swagger implementation uses JAX-RS `@Path` annotations and `ReflectiveJaxrsScanner`
which won't work after JAX-RS removal. Swagger/OpenAPI replacement (SpringDoc) is planned for Step 14.

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
| Duplicate beans (parent + child context) | Startup warnings, extra memory | Accept during migration; clean up in Step 12 |
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
- ~47 controller Java files (annotation conversion)
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
