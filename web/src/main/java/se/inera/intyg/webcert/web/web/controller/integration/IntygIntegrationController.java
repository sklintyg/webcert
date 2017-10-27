/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.infra.security.common.service.CommonFeatureService;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
    public static final String PARAM_ENHET_ID = "enhet";
    public static final String PARAM_COHERENT_JOURNALING = "sjf";
    public static final String PARAM_INACTIVE_UNIT = "inaktivEnhet";
    public static final String PARAM_COPY_OK = "kopieringOK";
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

    private static final Logger LOG = LoggerFactory.getLogger(IntygIntegrationController.class);

    private static final UserOriginType GRANTED_ORIGIN = UserOriginType.DJUPINTEGRATION;

    private static final String[] GRANTED_ROLES = new String[] {
            AuthoritiesConstants.ROLE_LAKARE, AuthoritiesConstants.ROLE_TANDLAKARE, AuthoritiesConstants.ROLE_ADMIN
    };

    private Optional<CommonFeatureService> commonFeatureService;

    private IntegrationService integrationService;

    private String urlIntygFragmentTemplate;
    private String urlUtkastFragmentTemplate;

    /**
     * Fetches a certificate from IT or webcert and then performs a redirect to the view that displays
     * the certificate. Can be used for all types of certificates.
     *
     * @param intygId
     *            The id of the certificate to view.
     * @param intygTyp
     *            The type of certificate
     */
    @GET
    @Path("/{intygTyp}/{intygId}")
    public Response getRedirectToIntyg(@Context UriInfo uriInfo,
                                    @PathParam("intygTyp") String intygTyp,
                                    @PathParam("intygId") String intygId,
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
                                    @DefaultValue("true") @QueryParam(PARAM_COPY_OK) boolean copyOk) {

        Map<String, Object> pathParameters = new HashMap<>();
        pathParameters.put("intygTyp", intygTyp);
        pathParameters.put("intygId", intygId);

        // validate the request
        validateRequest(pathParameters);

        IntegrationParameters integrationParameters =
                getIntegrationParameters(reference, responsibleHospName, alternatePatientSSn, fornamn, efternamn, mellannamn,
                        postadress, postnummer, postort, coherentJournaling, inactiveUnit, deceased, copyOk);

        WebCertUser user = getWebCertUser();
        user.setParameters(integrationParameters);

        return handleRedirectToIntyg(uriInfo, intygTyp, intygId, enhetId, user);
    }

    /**
     * Fetches an certificate from IT or Webcert and then performs a redirect to the view that displays
     * the certificate.
     *
     * @param intygId
     *            The id of the certificate to view.
     */
    @GET
    @Path("/{intygId}")
    public Response getRedirectToIntyg(@Context UriInfo uriInfo,
                                    @PathParam("intygId") String intygId,
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
                                    @DefaultValue("true") @QueryParam(PARAM_COPY_OK) boolean copyOk) {

        Map<String, Object> params = new HashMap<>();
        params.put("intygId", intygId);

        // validate the request
        validateRequest(params);

        IntegrationParameters integrationParameters =
                getIntegrationParameters(reference, responsibleHospName, alternatePatientSSn, fornamn, efternamn, mellannamn,
                        postadress, postnummer, postort, coherentJournaling, inactiveUnit, deceased, copyOk);

        WebCertUser user = getWebCertUser();
        user.setParameters(integrationParameters);

        return handleRedirectToIntyg(uriInfo, null, intygId, enhetId, user);
    }

    @POST
    @Path("/{intygTyp}/{intygId}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response postRedirectToIntyg(@Context UriInfo uriInfo,
                                    @PathParam("intygTyp") String intygTyp,
                                    @PathParam("intygId") String intygId,
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
                                    @DefaultValue("true") @FormParam(PARAM_COPY_OK) boolean copyOk) {

        Map<String, Object> params = new HashMap<>();
        params.put("intygTyp", intygTyp);
        params.put("intygId", intygId);

        // validate the request
        validateRequest(params);

        IntegrationParameters integrationParameters =
                getIntegrationParameters(reference, responsibleHospName, alternatePatientSSn, fornamn, efternamn, mellannamn,
                        postadress, postnummer, postort, coherentJournaling, inactiveUnit, deceased, copyOk);

        WebCertUser user = getWebCertUser();
        user.setParameters(integrationParameters);

        return handleRedirectToIntyg(uriInfo, intygTyp, intygId, enhetId, user);
    }

    @GET
    @Path("/{intygTyp}/{intygId}/resume")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response resumeRedirectToIntyg(
            @Context UriInfo uriInfo,
            @PathParam("intygTyp") String intygTyp,
            @PathParam("intygId") String intygId,
            @DefaultValue("") @QueryParam(PARAM_ENHET_ID) String enhetId) {

        Map<String, Object> params = new HashMap<>();
        params.put("intygTyp", intygTyp);
        params.put("intygId", intygId);
        params.put(PARAM_ENHET_ID, enhetId);

        // validate the request
        validateRequest(params);

        WebCertUser user = getWebCertUser();
        // Reset state parameter telling us that we have been redirected to 'enhetsvaljaren'
        user.getParameters().getState().setRedirectToEnhetsval(false);

        return handleRedirectToIntyg(uriInfo, intygTyp, intygId, enhetId, user);
    }

    @Autowired(required = false)
    public void setCommonFeatureService(Optional<CommonFeatureService> commonFeatureService) {
        this.commonFeatureService = commonFeatureService;
    }

    @Autowired
    @Qualifier("intygIntegrationServiceImpl")
    public void setIntegrationService(IntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    public void setUrlIntygFragmentTemplate(String urlFragmentTemplate) {
        this.urlIntygFragmentTemplate = urlFragmentTemplate;
    }

    public void setUrlUtkastFragmentTemplate(String urlFragmentTemplate) {
        this.urlUtkastFragmentTemplate = urlFragmentTemplate;
    }


    // protected scope

    @Override
    protected String[] getGrantedRoles() {
        return GRANTED_ROLES;
    }

    @Override
    protected UserOriginType getGrantedRequestOrigin() {
        return GRANTED_ORIGIN;
    }


    // default scope

    Response handleRedirectToIntyg(UriInfo uriInfo, String intygTyp, String intygId, String enhetId, WebCertUser user) {
        // Call service
        PrepareRedirectToIntyg prepareRedirect =
                integrationService.prepareRedirectToIntyg(intygTyp, intygId, user);

        // If the type doesn't equals to FK7263 then verify the required query-parameters
        if (!prepareRedirect.getIntygTyp().equals(Fk7263EntryPoint.MODULE_ID)) {
            verifyIntegrationParameters(user.getParameters());
        }

        if (Strings.nullToEmpty(enhetId).trim().isEmpty()) {

            // If ENHET isn't set but the user only has one possible enhet that can be selected, we auto-select that one
            // explicitly and proceed down the filter chain. Typically, that unit should already have been selected by
            // the UserDetailsService that built the Principal, but better safe than sorry...

            if (userHasExactlyOneSelectableVardenhet(user)) {
                user.changeValdVardenhet(user.getVardgivare().get(0).getVardenheter().get(0).getId());
                updateUserWithActiveFeatures(user);

                LOG.debug("Redirecting to view intyg {} of type {}", intygId, intygTyp);
                return buildRedirectResponse(uriInfo, prepareRedirect);
            }

            // Set state parameter telling us that we have been redirected to 'enhetsvaljaren'
            user.getParameters().getState().setRedirectToEnhetsval(true);

            LOG.warn("Deep integration request does not contain an 'enhet', redirecting to enhet selection page!");
            return buildChooseUnitResponse(uriInfo, prepareRedirect);

        } else {
            if (user.changeValdVardenhet(enhetId)) {
                updateUserWithActiveFeatures(user);

                LOG.debug("Redirecting to view intyg {} of type {}", intygId, intygTyp);
                return buildRedirectResponse(uriInfo, prepareRedirect);
            }

            LOG.warn("Validation failed for deep-integration request because user {} is not authorized for enhet {}",
                    user.getHsaId(), enhetId);
            return buildErroResponse(uriInfo);
        }
    }


    // private stuff

    private Response buildChooseUnitResponse(UriInfo uriInfo, PrepareRedirectToIntyg prepareRedirectToIntyg) {
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().replacePath(getUrlBaseTemplate());

        String destinationUrl = getDestinationUrl(uriInfo, prepareRedirectToIntyg);
        String urlFragment = "/integration-enhetsval";

        URI location = uriBuilder.queryParam("destination", destinationUrl).fragment(urlFragment).build();
        return Response.status(Response.Status.TEMPORARY_REDIRECT).location(location).build();
    }

    private Response buildErroResponse(UriInfo uriInfo) {
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().replacePath(getUrlBaseTemplate());

        Map<String, Object> urlParams = new HashMap<>();
        urlParams.put("reason", "login.medarbetaruppdrag");

        URI location = uriBuilder.path("error.jsp").buildFromMap(urlParams);
        return Response.status(Response.Status.UNAUTHORIZED).location(location).build();
    }

    private Response buildRedirectResponse(UriInfo uriInfo, PrepareRedirectToIntyg prepareRedirectToIntyg) {
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().replacePath(getUrlBaseTemplate());

        String intygId = prepareRedirectToIntyg.getIntygId();
        String intygTyp = prepareRedirectToIntyg.getIntygTyp();
        boolean isUtkast = prepareRedirectToIntyg.isUtkast();
        String urlFragmentTemplate = isUtkast ? urlUtkastFragmentTemplate : urlIntygFragmentTemplate;

        Map<String, Object> urlParams = new HashMap<>();
        urlParams.put(PARAM_CERT_TYPE, intygTyp);
        urlParams.put(PARAM_CERT_ID, intygId);

        URI location = uriBuilder.fragment(urlFragmentTemplate).buildFromMap(urlParams);
        return Response.status(Response.Status.TEMPORARY_REDIRECT).location(location).build();
    }


    private String getDestinationUrl(UriInfo uriInfo, PrepareRedirectToIntyg prepareRedirectToIntyg) {
        String intygId = prepareRedirectToIntyg.getIntygId();
        String intygTyp = prepareRedirectToIntyg.getIntygTyp();

        String urlPath = String.format("/visa/intyg/%s/%s/resume", intygTyp, intygId);

        try {
            // get the builder without any existing query params
            UriBuilder uriBuilder = uriInfo.getRequestUriBuilder().replacePath(urlPath).replaceQuery(null);
            URI uri = uriBuilder.build();

            return URLEncoder.encode(uri.toString(), "UTF-8");

        } catch (UnsupportedEncodingException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM, e);
        }
    }

    private IntegrationParameters getIntegrationParameters(String reference, String responsibleHospName, String alternatePatientSSn, String fornamn, String efternamn, String mellannamn, String postadress, String postnummer, String postort, boolean coherentJournaling, boolean inactiveUnit, boolean deceased, boolean copyOk) {
        return new IntegrationParameters(StringUtils.trimToNull(reference),
                responsibleHospName, alternatePatientSSn, fornamn, mellannamn, efternamn, postadress, postnummer, postort,
                coherentJournaling, deceased, inactiveUnit, copyOk);
    }

    private WebCertUser getWebCertUser() {
        WebCertUser user = getWebCertUserService().getUser();

        // Throw an exception if user already has the integration parameters set
        if (user.getParameters() != null && !user.getParameters().getState().hasUserBeenRedirectedToEnhetsval()) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
                    "This user session is already active and using Webcert. Please use a new user session for each deep integration link.");
        }

        return user;
    }

    private void updateUserWithActiveFeatures(WebCertUser webCertUser) {
        if (commonFeatureService.isPresent()) {
            webCertUser.setFeatures(commonFeatureService.get().getActiveFeatures(webCertUser.getValdVardenhet().getId(),
                    webCertUser.getValdVardgivare().getId()));
        }
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

    private void verifyIntegrationParameters(IntegrationParameters parameters) {
        if (parameters == null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MISSING_PARAMETER,
                    "Missing required integration parameters");
        }

        verifyParameter(PARAM_PATIENT_FORNAMN, parameters.getFornamn());
        verifyParameter(PARAM_PATIENT_EFTERNAMN, parameters.getEfternamn());
        verifyParameter(PARAM_PATIENT_POSTADRESS, parameters.getPostadress());
        verifyParameter(PARAM_PATIENT_POSTNUMMER, parameters.getPostnummer());
        verifyParameter(PARAM_PATIENT_POSTORT, parameters.getPostort());
    }

    private void verifyParameter(String paramName, String paramValue) {
        if (Strings.nullToEmpty(paramValue).trim().isEmpty()) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MISSING_PARAMETER,
                    "Missing required parameter '" + paramName + "'");
        }
    }

}
// CHECKSTYLE:ON ParameterNumber
