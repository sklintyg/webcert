/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
import com.google.common.collect.ImmutableMap;
import com.itextpdf.xmp.impl.Base64;
import io.swagger.annotations.Api;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.referens.ReferensService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.facade.util.ReactPilotUtil;
import se.inera.intyg.webcert.web.web.controller.facade.util.ReactUriFactory;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;
import se.inera.intyg.webcert.web.web.controller.integration.dto.PrepareRedirectToIntyg;

/**
 * Controller to enable an external user to access certificates directly from a
 * link in an external patient care system.
 *
 * @author bensam
 */
@Path("/intyg")
@Api(value = "intyg (Djupintegration)", description = "REST API för Djupintegration", produces = MediaType.APPLICATION_JSON)
// CHECKSTYLE:OFF ParameterNumber
public class IntygIntegrationController extends BaseIntegrationController {

    public static final String PARAM_CERT_ID = "certId";
    public static final String PARAM_CERT_TYPE = "certType";
    public static final String PARAM_CERT_TYPE_VERSION = "certTypeVersion";
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
    public static final String INTYG_TYP = "intygTyp";
    public static final String PARAM_LAUNCH_ID = "launchId";

    private static final Logger LOG = LoggerFactory.getLogger(IntygIntegrationController.class);

    private static final UserOriginType GRANTED_ORIGIN = UserOriginType.DJUPINTEGRATION;

    private static final String[] GRANTED_ROLES = new String[]{
        AuthoritiesConstants.ROLE_LAKARE,
        AuthoritiesConstants.ROLE_TANDLAKARE,
        AuthoritiesConstants.ROLE_ADMIN
    };

    private String urlIntygFragmentTemplate;
    private String urlUtkastFragmentTemplate;

    @Autowired
    private ReactUriFactory reactUriFactory;

    @Autowired
    private ReactPilotUtil reactPilotUtil;

    @Autowired
    private CommonAuthoritiesResolver commonAuthoritiesResolver;

    @Autowired
    private ReferensService referensService;

    @Autowired
    @Qualifier("intygIntegrationServiceImpl")
    private IntegrationService integrationService;

    @Autowired
    private IntygModuleRegistry moduleRegistry;
    @Autowired
    private Cache redisCacheLaunchId;

    /**
     * Fetches a certificate from IT or webcert and then performs a redirect to the view that displays
     * the certificate. Can be used for all types of certificates.
     *
     * @param intygId The id of the certificate to view.
     * @param intygTyp The type of certificate
     */
    @GET
    @Path("/{certType}/{certId}")
    @PrometheusTimeMethod
    public Response getRedirectToIntyg(@Context UriInfo uriInfo,
        @PathParam(PARAM_CERT_TYPE) String intygTyp,
        @PathParam(PARAM_CERT_ID) String intygId,
        @DefaultValue("") @QueryParam(PARAM_ENHET_ID) String enhetId,
        @DefaultValue("") @QueryParam(PARAM_PATIENT_ALTERNATE_SSN) String alternatePatientSSn,
        @DefaultValue("") @QueryParam(PARAM_RESPONSIBLE_HOSP_NAME) String responsibleHospName,
        @QueryParam(PARAM_PATIENT_FORNAMN) String fornamn,
        @QueryParam(PARAM_PATIENT_EFTERNAMN) String efternamn,
        @QueryParam(PARAM_PATIENT_MELLANNAMN) String mellannamn,
        @QueryParam(PARAM_PATIENT_POSTADRESS) String postadress,
        @QueryParam(PARAM_PATIENT_POSTNUMMER) String postnummer,
        @QueryParam(PARAM_PATIENT_POSTORT) String postort,
        @QueryParam(PARAM_REFERENCE) String reference,
        @DefaultValue("false") @QueryParam(PARAM_COHERENT_JOURNALING) boolean coherentJournaling,
        @DefaultValue("false") @QueryParam(PARAM_INACTIVE_UNIT) boolean inactiveUnit,
        @DefaultValue("false") @QueryParam(PARAM_PATIENT_DECEASED) boolean deceased,
        @DefaultValue("true") @QueryParam(PARAM_FORNYA_OK) boolean fornyaOk) {

        String internIntygTyp = moduleRegistry.getModuleIdFromExternalId(intygTyp.toUpperCase());
        Map<String, Object> pathParameters = ImmutableMap.of(
            PARAM_CERT_TYPE, internIntygTyp,
            PARAM_CERT_ID, intygId);

        validateRequest(pathParameters);

        IntegrationParameters integrationParameters = IntegrationParameters.of(
            reference, responsibleHospName, alternatePatientSSn, fornamn, mellannamn, efternamn,
            postadress, postnummer, postort, coherentJournaling, deceased, inactiveUnit, fornyaOk);

        // Temp. logging in an attempt to track down hashCode failures (hashCode has to be changed for redis updates)
        WebCertUser user = getWebCertUser();
        LOG.info("WebCertUser.hashCode BEFORE parameters update: {}", user.hashCode());
        user.setParameters(integrationParameters);
        LOG.info("WebCertUser.hashCode AFTER parameters update: {}", user.hashCode());

        return handleRedirectToIntyg(uriInfo, internIntygTyp, intygId, enhetId, user);
    }

    /**
     * Fetches an certificate from IT or Webcert and then performs a redirect to the view that displays
     * the certificate.
     *
     * @param intygId The id of the certificate to view.
     */
    @GET
    @Path("{certId}")
    @PrometheusTimeMethod
    public Response getRedirectToIntyg(@Context UriInfo uriInfo,
        @PathParam(PARAM_CERT_ID) String intygId,
        @DefaultValue("") @QueryParam(PARAM_ENHET_ID) String enhetId,
        @DefaultValue("") @QueryParam(PARAM_PATIENT_ALTERNATE_SSN) String alternatePatientSSn,
        @DefaultValue("") @QueryParam(PARAM_RESPONSIBLE_HOSP_NAME) String responsibleHospName,
        @QueryParam(PARAM_PATIENT_FORNAMN) String fornamn,
        @QueryParam(PARAM_PATIENT_EFTERNAMN) String efternamn,
        @QueryParam(PARAM_PATIENT_MELLANNAMN) String mellannamn,
        @QueryParam(PARAM_PATIENT_POSTADRESS) String postadress,
        @QueryParam(PARAM_PATIENT_POSTNUMMER) String postnummer,
        @QueryParam(PARAM_PATIENT_POSTORT) String postort,
        @DefaultValue("false") @QueryParam(PARAM_COHERENT_JOURNALING) boolean coherentJournaling,
        @QueryParam(PARAM_REFERENCE) String reference,
        @DefaultValue("false") @QueryParam(PARAM_INACTIVE_UNIT) boolean inactiveUnit,
        @DefaultValue("false") @QueryParam(PARAM_PATIENT_DECEASED) boolean deceased,
        @DefaultValue("true") @QueryParam(PARAM_FORNYA_OK) boolean fornyaOk) {

        Map<String, Object> params = ImmutableMap.of(PARAM_CERT_ID, intygId);

        // validate the request
        validateRequest(params);

        IntegrationParameters integrationParameters = IntegrationParameters.of(
            reference, responsibleHospName, alternatePatientSSn, fornamn, mellannamn, efternamn,
            postadress, postnummer, postort, coherentJournaling, deceased, inactiveUnit, fornyaOk);

        WebCertUser user = getWebCertUser();
        user.setParameters(integrationParameters);

        return handleRedirectToIntyg(uriInfo, intygId, enhetId, user);
    }

    /**
     * Fetches an certificate from IT or Webcert and then performs a redirect to the view that displays
     * the certificate.
     *
     * This entry point is only used when redirecting a POST after authentication from the
     * {@link se.inera.intyg.webcert.web.auth.WebcertAuthenticationSuccessHandler}
     * where the custom handler has applied the deep-integration parameters on the session.
     *
     * This is a work-around for the issue where Springs default SavedRequestAuthenticationSuccessHandler only performs
     * URL-based redirect, e.g. our POST becomes a GET and all form-params are discarded.
     *
     * Note that this method requires the IntegrationParameters to be present or an exception will be thrown.
     *
     * @param intygId The id of the certificate to view.
     */
    @GET
    @Path("{certId}/saved")
    @PrometheusTimeMethod
    public Response getRedirectToIntyg(@Context UriInfo uriInfo,
        @PathParam(PARAM_CERT_ID) String intygId,
        @DefaultValue("") @QueryParam(PARAM_ENHET_ID) String enhetId) {

        Map<String, Object> params = ImmutableMap.of(PARAM_CERT_ID, intygId);

        // Get the user directly, do not run the "has already got parameters check" since that's exactly what we've got here.
        WebCertUser user = getWebCertUserService().getUser();

        // Integration params MUST be set
        if (user.getParameters() == null) {
            throw new IllegalStateException("Cannot process saved request, no deep-integration parameters has been set.");
        }

        // validate the request
        validateRequest(params);

        return handleRedirectToIntyg(uriInfo, intygId, enhetId, user);
    }

    @POST
    @Path("/{certId}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @PrometheusTimeMethod
    public Response postRedirectToIntyg(@Context UriInfo uriInfo,
        @Context HttpServletRequest request,
        @PathParam(PARAM_CERT_ID) String intygId,
        @DefaultValue("") @FormParam(PARAM_ENHET_ID) String enhetId,
        @DefaultValue("") @FormParam(PARAM_PATIENT_ALTERNATE_SSN) String alternatePatientSSn,
        @DefaultValue("") @FormParam(PARAM_RESPONSIBLE_HOSP_NAME) String responsibleHospName,
        @FormParam(PARAM_PATIENT_FORNAMN) String fornamn,
        @FormParam(PARAM_PATIENT_EFTERNAMN) String efternamn,
        @FormParam(PARAM_PATIENT_MELLANNAMN) String mellannamn,
        @FormParam(PARAM_PATIENT_POSTADRESS) String postadress,
        @FormParam(PARAM_PATIENT_POSTNUMMER) String postnummer,
        @FormParam(PARAM_PATIENT_POSTORT) String postort,
        @DefaultValue("false") @FormParam(PARAM_COHERENT_JOURNALING) boolean coherentJournaling,
        @FormParam(PARAM_REFERENCE) String reference,
        @DefaultValue("false") @FormParam(PARAM_INACTIVE_UNIT) boolean inactiveUnit,
        @DefaultValue("false") @FormParam(PARAM_PATIENT_DECEASED) boolean deceased,
        @DefaultValue("true") @FormParam(PARAM_FORNYA_OK) boolean fornyaOk,
        @DefaultValue("") @FormParam(PARAM_LAUNCH_ID) String launchId) {

        final Map<String, Object> params = ImmutableMap.of(PARAM_CERT_ID, intygId);

        validateRequest(params);

        IntegrationParameters integrationParameters = IntegrationParameters.of(
            reference, responsibleHospName, alternatePatientSSn, fornamn, mellannamn, efternamn,
            postadress, postnummer, postort, coherentJournaling, deceased, inactiveUnit, fornyaOk,
            launchIdShouldBeAdded(launchId) ? launchId : null);

        WebCertUser user = getWebCertUser();
        user.setParameters(integrationParameters);

        if (user.getParameters().getLaunchId() != null) {
            redisCacheLaunchId.put(launchId, Base64.encode(request.getSession().getId()));
            LOG.info(String.format("launchId was successfully added to the session. launchId stored in session is: %s",
                user.getParameters().getLaunchId()));
        }

        return handleRedirectToIntyg(uriInfo, intygId, enhetId, user);
    }

    @GET
    @Path("/{certType}/{certTypeVersion}/{certId}/resume")
    @PrometheusTimeMethod
    public Response resumeRedirectToIntyg(
        @Context UriInfo uriInfo,
        @PathParam(PARAM_CERT_TYPE) String intygTyp,
        @PathParam(PARAM_CERT_TYPE_VERSION) String certTypeVersion,
        @PathParam(PARAM_CERT_ID) String intygId,
        @DefaultValue("") @QueryParam(PARAM_ENHET_ID) String enhetId) {

        Map<String, Object> params = ImmutableMap.of(
            INTYG_TYP, intygTyp,
            PARAM_CERT_ID, intygId,
            PARAM_ENHET_ID, enhetId);

        validateRequest(params);

        WebCertUser user = getWebCertUser();
        // Reset state parameter telling us that we have been redirected to 'enhetsvaljaren'
        user.getParameters().getState().setRedirectToEnhetsval(false);

        return handleRedirectToIntyg(uriInfo, intygTyp, intygId, enhetId, user);
    }

    public void setUrlIntygFragmentTemplate(String urlFragmentTemplate) {
        this.urlIntygFragmentTemplate = urlFragmentTemplate;
    }

    public void setUrlUtkastFragmentTemplate(String urlFragmentTemplate) {
        this.urlUtkastFragmentTemplate = urlFragmentTemplate;
    }

    @Override
    protected String[] getGrantedRoles() {
        return GRANTED_ROLES;
    }

    @Override
    protected UserOriginType getGrantedRequestOrigin() {
        return GRANTED_ORIGIN;
    }

    protected Response handleRedirectToIntyg(final UriInfo uriInfo, final String intygId, final String enhetId, final WebCertUser user) {
        return handleRedirectToIntyg(uriInfo, null, intygId, enhetId, user);
    }

    protected Response handleRedirectToIntyg(UriInfo uriInfo, String intygTyp, String intygId, String enhetId, WebCertUser user) {
        try {

            // Persist reference
            handleReference(intygId, user.getParameters().getReference());

            if (Strings.nullToEmpty(enhetId).trim().isEmpty()) {

                // If ENHET isn't set but the user only has one possible enhet that can be selected, we auto-select that one
                // explicitly and proceed down the filter chain. Typically, that unit should already have been selected by
                // the UserDetailsService that built the Principal, but better safe than sorry...

                // Call service
                PrepareRedirectToIntyg prepareRedirectInfo = integrationService.prepareRedirectToIntyg(intygTyp, intygId, user);

                if (userHasExactlyOneSelectableVardenhet(user)) {
                    user.changeValdVardenhet(user.getVardgivare().get(0).getVardenheter().get(0).getId());
                    updateUserWithActiveFeatures(user);

                    LOG.debug("Redirecting to view intyg {} of type {}", intygId, intygTyp);
                    return buildRedirectResponse(uriInfo, prepareRedirectInfo, user);
                }

                // Set state parameter telling us that we have been redirected to 'enhetsvaljaren'
                user.getParameters().getState().setRedirectToEnhetsval(true);

                LOG.warn("Deep integration request does not contain an 'enhet', redirecting to enhet selection page!");
                return buildChooseUnitResponse(uriInfo, prepareRedirectInfo);

            } else {
                if (user.changeValdVardenhet(enhetId)) {

                    final String beforeAlternateSsnParam = user.getParameters().getBeforeAlternateSsn();
                    PrepareRedirectToIntyg prepareRedirectInfo;

                    // We want to send beforeAlternateSsnParam to prepareRedirectToIntyg, to be able to
                    // see the difference between beforeAlternateSsn and alternateSsn in the gui when opening the utkast.
                    // This will trigger an info-message that the personnummer for the utkast has changed.
                    // This fix is not pretty but due to earlier implementations this is the easiest fix. See INTYG-8115 for more info.
                    if (!Strings.isNullOrEmpty(beforeAlternateSsnParam)) {
                        Personnummer beforeAlternateSsn = Personnummer.createPersonnummer(beforeAlternateSsnParam).orElse(null);
                        prepareRedirectInfo = integrationService.prepareRedirectToIntyg(intygTyp, intygId, user, beforeAlternateSsn);
                    } else {
                        prepareRedirectInfo = integrationService.prepareRedirectToIntyg(intygTyp, intygId, user);
                    }

                    updateUserWithActiveFeatures(user);
                    LOG.debug("Redirecting to view intyg {} of type {}", intygId, intygTyp);
                    return buildRedirectResponse(uriInfo, prepareRedirectInfo, user);
                }

                LOG.warn("Validation failed for deep-integration request because user {} is not authorized for enhet {}",
                    user.getHsaId(), enhetId);
                return buildAuthorizedErrorResponse(uriInfo);
            }
        } catch (WebCertServiceException e) {
            if (e.getErrorCode().equals(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND)) {
                LOG.info(e.getMessage());
                return buildNoContentErrorResponse(uriInfo);
            } else {
                throw e;
            }
        }
    }

    private void handleReference(String intygId, String referens) {
        if (referens != null) {
            if (!referensService.referensExists(intygId)) {
                referensService.saveReferens(intygId, referens);
            }
        }
    }

    private Response buildNoContentErrorResponse(UriInfo uriInfo) {
        return buildErrorResponse(uriInfo, "integration.nocontent");
    }

    private Response buildAuthorizedErrorResponse(UriInfo uriInfo) {
        return buildErrorResponse(uriInfo, "login.medarbetaruppdrag");
    }

    private Response buildErrorResponse(UriInfo uriInfo, String errorReason) {
        URI location = uriInfo.getBaseUriBuilder()
            .replacePath("/error.jsp")
            .queryParam("reason", errorReason)
            .build();

        return Response.temporaryRedirect(location).build();
    }

    private Response buildChooseUnitResponse(UriInfo uriInfo, PrepareRedirectToIntyg prepareRedirectToIntyg) {
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().replacePath(getUrlBaseTemplate());

        String destinationUrl = getDestinationUrl(uriInfo, prepareRedirectToIntyg);
        String urlFragment = "/integration-enhetsval";

        URI location = uriBuilder.queryParam("destination", destinationUrl).fragment(urlFragment).build();
        return Response.temporaryRedirect(location).build();
    }

    private Response buildRedirectResponse(UriInfo uriInfo, PrepareRedirectToIntyg prepareRedirectToIntyg, WebCertUser user) {
        final var location = getRedirectUri(uriInfo, prepareRedirectToIntyg, user);
        return Response.seeOther(location).build();
    }

    private URI getRedirectUri(UriInfo uriInfo, PrepareRedirectToIntyg prepareRedirectToIntyg, WebCertUser user) {
        if (reactPilotUtil.useReactClient(user, prepareRedirectToIntyg.getIntygTyp())) {
            return reactUriFactory.uriForCertificate(uriInfo, prepareRedirectToIntyg.getIntygId());
        }
        return getRedirectUriForAngularClient(uriInfo, prepareRedirectToIntyg);
    }

    private URI getRedirectUriForAngularClient(UriInfo uriInfo, PrepareRedirectToIntyg prepareRedirectToIntyg) {
        final var uriBuilder = uriInfo.getBaseUriBuilder().replacePath(getUrlBaseTemplate());
        final var urlFragmentTemplate = prepareRedirectToIntyg.isUtkast() ? urlUtkastFragmentTemplate : urlIntygFragmentTemplate;
        final var urlParams = Map.of(
            PARAM_CERT_TYPE, prepareRedirectToIntyg.getIntygTyp(),
            PARAM_CERT_TYPE_VERSION, prepareRedirectToIntyg.getIntygTypeVersion(),
            PARAM_CERT_ID, prepareRedirectToIntyg.getIntygId()
        );
        return uriBuilder.fragment(urlFragmentTemplate).buildFromMap(urlParams);
    }

    private String getDestinationUrl(UriInfo uriInfo, PrepareRedirectToIntyg prepareRedirectToIntyg) {
        String intygId = prepareRedirectToIntyg.getIntygId();
        String intygTyp = prepareRedirectToIntyg.getIntygTyp();
        String intygTypeVersion = prepareRedirectToIntyg.getIntygTypeVersion();

        String urlPath = String.format("/visa/intyg/%s/%s/%s/resume", intygTyp, intygTypeVersion, intygId);

        try {
            // get the builder without any existing query params
            UriBuilder uriBuilder = uriInfo.getRequestUriBuilder().replacePath(urlPath).replaceQuery(null);
            URI uri = uriBuilder.build();

            return URLEncoder.encode(uri.toString(), "UTF-8");

        } catch (UnsupportedEncodingException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM, e);
        }
    }

    private WebCertUser getWebCertUser() {
        WebCertUser user = getWebCertUserService().getUser();

        // Throw an exception if user already has the integration parameters set
        if (user.getParameters() != null && !user.getParameters().getState().hasUserBeenRedirectedToEnhetsval()) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_USER_SESSION_ALREADY_ACTIVE,
                "This user session is already active and using Webcert. Please use a new user session for each deep integration link.");
        }

        return user;
    }

    private void updateUserWithActiveFeatures(WebCertUser webCertUser) {
        webCertUser.setFeatures(commonAuthoritiesResolver
            .getFeatures(Arrays.asList(webCertUser.getValdVardenhet().getId(), webCertUser.getValdVardgivare().getId())));
    }

    private boolean userHasExactlyOneSelectableVardenhet(WebCertUser webCertUser) {
        return webCertUser.getVardgivare().stream()
            .distinct()
            .flatMap(vg -> vg.getVardenheter().stream().distinct())
            .count() == 1L;
    }

    private void validateRequest(Map<String, Object> pathParameters) {
        super.validateParameters(pathParameters);
        super.validateAuthorities();
    }

    private boolean launchIdShouldBeAdded(String launchId) {
        if (launchId == null || launchId.isEmpty()) {
            return false;
        }
        try {
            UUID.fromString(launchId);
        } catch (IllegalArgumentException exception) {
            LOG.info(String.format("Provided launchId was not correct format: %s. LaunchId should be of type GUID", launchId));
            throw new IllegalArgumentException(
                String.format("Provided launchId was not correct format: %s. LaunchId should be of type GUID", launchId));
        }
        return true;
    }
}

// CHECKSTYLE:ON ParameterNumber
