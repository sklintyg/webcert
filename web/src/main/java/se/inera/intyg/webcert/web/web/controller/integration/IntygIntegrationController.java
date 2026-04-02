/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.web.web.controller.integration;

import com.google.common.base.Strings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.webcert.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.auth.CustomAuthenticationSuccessHandler;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.facade.util.ReactUriFactory;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;
import se.inera.intyg.webcert.web.web.controller.integration.dto.PrepareRedirectToIntyg;

/**
 * Controller to enable an external user to access certificates directly from a link in an external
 * patient care system.
 *
 * @author bensam
 */
@Controller
@RequestMapping({"/visa/intyg", "/v2/visa/intyg"})
// CHECKSTYLE:OFF ParameterNumber
public class IntygIntegrationController extends BaseIntegrationController {

  public static final String PARAM_CERT_ID = "certId";
  public static final String PARAM_ENHET_ID = "enhet";
  public static final String PARAM_COHERENT_JOURNALING = "sjf";
  public static final String PARAM_INACTIVE_UNIT = "inaktivEnhet";
  public static final String PARAM_FORNYA_OK = "kopieringOK";
  public static final String PARAM_PATIENT_ALTERNATE_SSN = "alternatePatientSSn";
  public static final String PARAM_PATIENT_DECEASED = "avliden";
  public static final String PARAM_PATIENT_EFTERNAMN = "efternamn";
  public static final String PARAM_PATIENT_FORNAMN = "fornamn";
  public static final String PARAM_PATIENT_MELLANNAMN = "mellannamn";
  public static final String PARAM_PATIENT_POSTADRESS = "postadress";
  public static final String PARAM_PATIENT_POSTNUMMER = "postnummer";
  public static final String PARAM_PATIENT_POSTORT = "postort";
  public static final String PARAM_REFERENCE = "ref";
  public static final String PARAM_RESPONSIBLE_HOSP_NAME = "responsibleHospName";
  public static final String PARAM_LAUNCH_ID = "launchId";

  private static final Logger LOG = LoggerFactory.getLogger(IntygIntegrationController.class);

  private static final UserOriginType GRANTED_ORIGIN = UserOriginType.DJUPINTEGRATION;

  private static final Set<String> FORM_ONLY_PARAMS =
      Set.of(
          PARAM_ENHET_ID,
          PARAM_PATIENT_ALTERNATE_SSN,
          PARAM_RESPONSIBLE_HOSP_NAME,
          PARAM_PATIENT_FORNAMN,
          PARAM_PATIENT_EFTERNAMN,
          PARAM_PATIENT_MELLANNAMN,
          PARAM_PATIENT_POSTADRESS,
          PARAM_PATIENT_POSTNUMMER,
          PARAM_PATIENT_POSTORT,
          PARAM_COHERENT_JOURNALING,
          PARAM_REFERENCE,
          PARAM_INACTIVE_UNIT,
          PARAM_PATIENT_DECEASED,
          PARAM_FORNYA_OK,
          PARAM_LAUNCH_ID);

  private static final String[] GRANTED_ROLES =
      new String[] {
        AuthoritiesConstants.ROLE_LAKARE,
        AuthoritiesConstants.ROLE_TANDLAKARE,
        AuthoritiesConstants.ROLE_ADMIN,
        AuthoritiesConstants.ROLE_SJUKSKOTERSKA,
        AuthoritiesConstants.ROLE_BARNMORSKA,
      };

  @Autowired private ReactUriFactory reactUriFactory;
  @Autowired private CommonAuthoritiesResolver commonAuthoritiesResolver;

  @Autowired
  @Qualifier("integrationCertificateAggregator") private IntegrationService integrationService;

  @Autowired private Cache redisCacheLaunchId;

  @Override
  protected String[] getGrantedRoles() {
    return GRANTED_ROLES;
  }

  @Override
  protected UserOriginType getGrantedRequestOrigin() {
    return GRANTED_ORIGIN;
  }

  /**
   * Fetches an certificate from IT or Webcert and then performs a redirect to the view that
   * displays the certificate.
   *
   * @param intygId The id of the certificate to view.
   * @deprecated This method is will be removed when the last deep-integrated region has moved to
   *     the POST-version of this endpoint.
   */
  @GetMapping("/{certId}")
  @PrometheusTimeMethod
  @Deprecated(since = "2019")
  @PerformanceLogging(
      eventAction = "intyg-integration-get-redirect-to-certificate",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<Void> getRedirectToIntyg(
      HttpServletRequest request,
      @PathVariable(PARAM_CERT_ID) String intygId,
      @RequestParam(value = PARAM_ENHET_ID, required = false, defaultValue = "") String enhetId,
      @RequestParam(value = PARAM_PATIENT_ALTERNATE_SSN, required = false, defaultValue = "")
          String alternatePatientSSn,
      @RequestParam(value = PARAM_RESPONSIBLE_HOSP_NAME, required = false, defaultValue = "")
          String responsibleHospName,
      @RequestParam(value = PARAM_PATIENT_FORNAMN, required = false) String fornamn,
      @RequestParam(value = PARAM_PATIENT_EFTERNAMN, required = false) String efternamn,
      @RequestParam(value = PARAM_PATIENT_MELLANNAMN, required = false) String mellannamn,
      @RequestParam(value = PARAM_PATIENT_POSTADRESS, required = false) String postadress,
      @RequestParam(value = PARAM_PATIENT_POSTNUMMER, required = false) String postnummer,
      @RequestParam(value = PARAM_PATIENT_POSTORT, required = false) String postort,
      @RequestParam(value = PARAM_COHERENT_JOURNALING, required = false, defaultValue = "false")
          boolean coherentJournaling,
      @RequestParam(value = PARAM_REFERENCE, required = false) String reference,
      @RequestParam(value = PARAM_INACTIVE_UNIT, required = false, defaultValue = "false")
          boolean inactiveUnit,
      @RequestParam(value = PARAM_PATIENT_DECEASED, required = false, defaultValue = "false")
          boolean deceased,
      @RequestParam(value = PARAM_FORNYA_OK, required = false, defaultValue = "true")
          boolean fornyaOk) {

    final var params = Map.of(PARAM_CERT_ID, intygId);
    validateRequest(params);

    final var integrationParameters =
        IntegrationParameters.of(
            reference,
            responsibleHospName,
            alternatePatientSSn,
            fornamn,
            mellannamn,
            efternamn,
            postadress,
            postnummer,
            postort,
            coherentJournaling,
            deceased,
            inactiveUnit,
            fornyaOk);

    final var user = getWebCertUser(request.getSession());
    user.setParameters(integrationParameters);

    return handleRedirectToIntyg(request, intygId, enhetId, user);
  }

  @PostMapping("/{certId}")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "intyg-integration-post-redirect-to-certificate",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<Void> postRedirectToIntyg(
      HttpServletRequest request,
      @PathVariable(PARAM_CERT_ID) String intygId,
      @RequestParam(value = PARAM_ENHET_ID, required = false, defaultValue = "") String enhetId,
      @RequestParam(value = PARAM_PATIENT_ALTERNATE_SSN, required = false, defaultValue = "")
          String alternatePatientSSn,
      @RequestParam(value = PARAM_RESPONSIBLE_HOSP_NAME, required = false, defaultValue = "")
          String responsibleHospName,
      @RequestParam(value = PARAM_PATIENT_FORNAMN, required = false) String fornamn,
      @RequestParam(value = PARAM_PATIENT_EFTERNAMN, required = false) String efternamn,
      @RequestParam(value = PARAM_PATIENT_MELLANNAMN, required = false) String mellannamn,
      @RequestParam(value = PARAM_PATIENT_POSTADRESS, required = false) String postadress,
      @RequestParam(value = PARAM_PATIENT_POSTNUMMER, required = false) String postnummer,
      @RequestParam(value = PARAM_PATIENT_POSTORT, required = false) String postort,
      @RequestParam(value = PARAM_COHERENT_JOURNALING, required = false, defaultValue = "false")
          boolean coherentJournaling,
      @RequestParam(value = PARAM_REFERENCE, required = false) String reference,
      @RequestParam(value = PARAM_INACTIVE_UNIT, required = false, defaultValue = "false")
          boolean inactiveUnit,
      @RequestParam(value = PARAM_PATIENT_DECEASED, required = false, defaultValue = "false")
          boolean deceased,
      @RequestParam(value = PARAM_FORNYA_OK, required = false, defaultValue = "true")
          boolean fornyaOk,
      @RequestParam(value = PARAM_LAUNCH_ID, required = false, defaultValue = "") String launchId) {

    rejectFormParamsInQueryString(request);

    final var params = Map.of(PARAM_CERT_ID, intygId);
    validateRequest(params);

    final var integrationParameters =
        IntegrationParameters.of(
            reference,
            responsibleHospName,
            alternatePatientSSn,
            fornamn,
            mellannamn,
            efternamn,
            postadress,
            postnummer,
            postort,
            coherentJournaling,
            deceased,
            inactiveUnit,
            fornyaOk,
            launchIdShouldBeAdded(launchId) ? launchId : null);

    final var user = getWebCertUser(request.getSession());
    user.setParameters(integrationParameters);

    cacheExistingLaunchIdForSession(
        user.getParameters().getLaunchId(), request.getSession().getId());

    return handleRedirectToIntyg(request, intygId, enhetId, user);
  }

  /**
   * Fetches an certificate from IT or Webcert and then performs a redirect to the view that
   * displays the certificate.
   *
   * <p>This entry point is only used when redirecting a POST after authentication from the {@link
   * CustomAuthenticationSuccessHandler} where the custom handler has applied the deep-integration
   * parameters on the session.
   *
   * <p>This is a work-around for the issue where Springs default
   * SavedRequestAuthenticationSuccessHandler only performs URL-based redirect, e.g. our POST
   * becomes a GET and all form-params are discarded.
   *
   * <p>Note that this method requires the IntegrationParameters to be present or an exception will
   * be thrown.
   *
   * @param intygId The id of the certificate to view.
   */
  @GetMapping("/{certId}/saved")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "intyg-integration-get-redirect-to-certificate-saved",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<Void> getRedirectToIntyg(
      HttpServletRequest request,
      @PathVariable(PARAM_CERT_ID) String intygId,
      @RequestParam(value = PARAM_ENHET_ID, required = false, defaultValue = "") String enhetId) {

    final var user = getWebCertUserService().getUser();
    if (user.getParameters() == null) {
      throw new IllegalStateException(
          "Cannot process saved request, no deep-integration parameters has been set.");
    }

    final var params = Map.of(PARAM_CERT_ID, intygId);
    validateRequest(params);

    cacheExistingLaunchIdForSession(
        user.getParameters().getLaunchId(), request.getSession().getId());

    return handleRedirectToIntyg(request, intygId, enhetId, user);
  }

  /**
   * Resumes launching the application and redirecting to provided certificate after user has
   * selected which unit (SelectedVardenhet) it chooses to be logged in to.
   */
  @GetMapping("/{certId}/resume")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "intyg-integration-resume-redirect-to-certificate",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<Void> resumeRedirectToIntyg(
      HttpServletRequest request,
      @PathVariable(PARAM_CERT_ID) String intygId,
      @RequestParam(value = PARAM_ENHET_ID, required = false, defaultValue = "") String enhetId) {

    final var params =
        Map.of(
            PARAM_CERT_ID, intygId,
            PARAM_ENHET_ID, enhetId);
    validateRequest(params);

    final var user = getWebCertUser(request.getSession());
    user.getParameters().getState().setRedirectToEnhetsval(false);

    return handleRedirectToIntyg(request, intygId, enhetId, user);
  }

  private ResponseEntity<Void> handleRedirectToIntyg(
      HttpServletRequest request, String intygId, String enhetId, WebCertUser user) {
    try {
      LOG.atDebug()
          .addKeyValue("event.certificate.id", intygId)
          .log(
              "Launch for certificate with id '{}' with ref '{}'",
              intygId,
              user.getParameters().getReference());

      if (userHasNotSelectedVardenhet(enhetId)) {
        if (userHasExactlyOneSelectableVardenhet(user)) {
          changeValdVardenhet(
              user.getVardgivare().getFirst().getVardenheter().getFirst().getId(), user);
          final var prepareRedirectInfo = prepareRedirectToIntyg(intygId, user);
          LOG.debug(
              "Redirecting to view intyg {} of type {}",
              intygId,
              prepareRedirectInfo.getIntygTyp());
          return buildViewCertificateResponse(request, prepareRedirectInfo);
        }

        LOG.info(
            "Deep integration request does not contain an 'enhet', redirecting to enhet selection page!");
        user.getParameters().getState().setRedirectToEnhetsval(true);
        return buildSelectUnitResponse(request, intygId);
      }

      if (changeValdVardenhet(enhetId, user)) {
        final var prepareRedirectInfo = prepareRedirectToIntyg(intygId, user);
        LOG.debug(
            "Redirecting to view intyg {} of type {}", intygId, prepareRedirectInfo.getIntygTyp());
        return buildViewCertificateResponse(request, prepareRedirectInfo);
      }

      LOG.warn(
          "Validation failed for deep-integration request because user {} is not authorized for enhet {}",
          user.getHsaId(),
          enhetId);
      return buildAuthorizedErrorResponse(request);
    } catch (WebCertServiceException e) {
      if (e.getErrorCode().equals(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND)) {
        LOG.info(e.getMessage());
        return buildNoContentErrorResponse(request);
      } else {
        throw e;
      }
    }
  }

  /**
   * Legacy code-comment relating to implementation choices of this logic: // We want to send
   * beforeAlternateSsnParam to prepareRedirectToIntyg, to be able to // see the difference between
   * beforeAlternateSsn and alternateSsn in the gui when opening the utkast. // This will trigger an
   * info-message that the personnummer for the utkast has changed. // This fix is not pretty but
   * due to earlier implementations this is the easiest fix. See INTYG-8115 for more info.
   */
  private PrepareRedirectToIntyg prepareRedirectToIntyg(String intygId, WebCertUser user) {
    final String beforeAlternateSsnParam = user.getParameters().getBeforeAlternateSsn();

    if (!Strings.isNullOrEmpty(beforeAlternateSsnParam)) {
      Personnummer beforeAlternateSsn =
          Personnummer.createPersonnummer(beforeAlternateSsnParam).orElse(null);
      return integrationService.prepareRedirectToIntyg(intygId, user, beforeAlternateSsn);
    }
    return integrationService.prepareRedirectToIntyg(intygId, user);
  }

  private ResponseEntity<Void> buildNoContentErrorResponse(HttpServletRequest request) {
    return buildErrorResponse(request, "integration.nocontent");
  }

  private ResponseEntity<Void> buildAuthorizedErrorResponse(HttpServletRequest request) {
    return buildErrorResponse(request, "login.medarbetaruppdrag");
  }

  private ResponseEntity<Void> buildErrorResponse(HttpServletRequest request, String errorReason) {
    final var location = reactUriFactory.uriForErrorResponse(request, errorReason);
    return ResponseEntity.status(HttpStatus.SEE_OTHER).location(location).build();
  }

  private ResponseEntity<Void> buildSelectUnitResponse(
      HttpServletRequest request, String certificateId) {
    final var location = reactUriFactory.uriForUnitSelection(request, certificateId);
    return ResponseEntity.status(HttpStatus.SEE_OTHER).location(location).build();
  }

  private ResponseEntity<Void> buildViewCertificateResponse(
      HttpServletRequest request, PrepareRedirectToIntyg prepareRedirectToIntyg) {
    final var location =
        reactUriFactory.uriForCertificate(request, prepareRedirectToIntyg.getIntygId());
    return ResponseEntity.status(HttpStatus.SEE_OTHER).location(location).build();
  }

  private WebCertUser getWebCertUser(HttpSession session) {
    final var user = getWebCertUserService().getUser();
    if (user.getParameters() != null
        && !user.getParameters().getState().hasUserBeenRedirectedToEnhetsval()) {
      getWebCertUserService().removeSessionNow(session);
      throw new WebCertServiceException(
          WebCertServiceErrorCodeEnum.AUTHORIZATION_USER_SESSION_ALREADY_ACTIVE,
          "This user session is already active and using Webcert. Please use a new user session for each deep integration link.");
    }

    return user;
  }

  private boolean changeValdVardenhet(String enhetId, WebCertUser user) {
    final var successful = user.changeValdVardenhet(enhetId);
    if (successful) {
      updateUserWithActiveFeatures(user);
    }
    return successful;
  }

  private void updateUserWithActiveFeatures(WebCertUser webCertUser) {
    webCertUser.setFeatures(
        commonAuthoritiesResolver.getFeatures(
            Arrays.asList(
                webCertUser.getValdVardenhet().getId(), webCertUser.getValdVardgivare().getId())));
  }

  private boolean userHasExactlyOneSelectableVardenhet(WebCertUser webCertUser) {
    return webCertUser.getVardgivare().stream()
            .distinct()
            .flatMap(vg -> vg.getVardenheter().stream().distinct())
            .count()
        == 1L;
  }

  private static boolean userHasNotSelectedVardenhet(String enhetId) {
    return Strings.nullToEmpty(enhetId).trim().isEmpty();
  }

  private void validateRequest(Map<String, String> pathParameters) {
    super.validateParameters(pathParameters);
    super.validateAuthorities();
  }

  private static void rejectFormParamsInQueryString(HttpServletRequest request) {
    final var queryString = request.getQueryString();
    if (queryString == null || queryString.isEmpty()) {
      return;
    }

    final var queryParamNames =
        Arrays.stream(queryString.split("&"))
            .map(
                pair -> {
                  final var eq = pair.indexOf('=');
                  return URLDecoder.decode(
                      eq >= 0 ? pair.substring(0, eq) : pair, StandardCharsets.UTF_8);
                })
            .collect(Collectors.toSet());

    queryParamNames.retainAll(FORM_ONLY_PARAMS);

    if (!queryParamNames.isEmpty()) {
      LOG.warn("Rejected POST with integration parameters in query string: {}", queryParamNames);
      throw new WebCertServiceException(
          WebCertServiceErrorCodeEnum.MISSING_PARAMETER,
          "Integration parameters must be provided in the request body, not in the query string: "
              + queryParamNames);
    }
  }

  private boolean launchIdShouldBeAdded(String launchId) {
    if (launchId == null || launchId.isEmpty()) {
      return false;
    }
    try {
      UUID.fromString(launchId);
    } catch (IllegalArgumentException exception) {
      LOG.info(
          String.format(
              "Provided launchId was not correct format: %s. LaunchId should be of type GUID",
              launchId));
      throw new IllegalArgumentException(
          String.format(
              "Provided launchId was not correct format: %s. LaunchId should be of type GUID",
              launchId));
    }
    return true;
  }

  private void cacheExistingLaunchIdForSession(String launchId, String sessionId) {
    if (launchId != null) {
      redisCacheLaunchId.put(
          launchId, Base64.getEncoder().encodeToString(sessionId.getBytes(StandardCharsets.UTF_8)));
      LOG.info(
          "launchId was successfully added to the session. launchId stored in session is: {}",
          launchId);
    }
  }
}

// CHECKSTYLE:ON ParameterNumber
