/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.annotations.Api;
import se.inera.intyg.common.security.authorities.AuthoritiesException;
import se.inera.intyg.common.security.common.model.AuthoritiesConstants;
import se.inera.intyg.common.security.common.model.UserOriginType;
import se.inera.intyg.intygstyper.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.dto.PatientParameter;

/**
 * Controller to enable an external user to access certificates directly from a
 * link in an external patient care system.
 *
 * @author bensam
 */
@Path("/intyg")
@Api(value = "intyg (Djupintegration)", description = "REST API för Djupintegration", produces = MediaType.APPLICATION_JSON)
public class IntygIntegrationController extends BaseIntegrationController {

    private static final String PARAM_CERT_TYPE = "certType";
    private static final String PARAM_CERT_ID = "certId";
    public static final String PARAM_HOSP_NAME = "hospName";
    public static final String PARAM_PATIENT_SSN = "patientId";
    public static final String PARAM_PATIENT_FORNAMN = "fornamn";
    public static final String PARAM_PATIENT_EFTERNAMN = "efternamn";
    public static final String PARAM_PATIENT_MELLANNAMN = "mellannamn";
    public static final String PARAM_PATIENT_POSTADRESS = "postadress";
    public static final String PARAM_PATIENT_POSTNUMMER = "postnummer";
    public static final String PARAM_PATIENT_POSTORT = "postort";
    private static final String PARAM_COHERENT_JOURNALING = "sjf";
    private static final String PARAM_REFERENCE = "ref";
    private static final String PARAM_ENHET = "enhet";

    private static final Logger LOG = LoggerFactory.getLogger(IntygIntegrationController.class);

    private static final String[] GRANTED_ROLES = new String[] { AuthoritiesConstants.ROLE_LAKARE, AuthoritiesConstants.ROLE_TANDLAKARE,
            AuthoritiesConstants.ROLE_ADMIN };
    private static final UserOriginType GRANTED_ORIGIN = UserOriginType.DJUPINTEGRATION;

    private String urlIntygFragmentTemplate;
    private String urlUtkastFragmentTemplate;

    @Autowired
    private UtkastRepository utkastRepository;

    /**
     * Fetches an certificate from IT or Webcert and then performs a redirect to the view that displays
     * the certificate.
     *
     * @param intygId
     *            The id of the certificate to view.
     */
    @GET
    @Path("/{intygId}")
    // CHECKSTYLE:OFF ParameterNumber
    public Response redirectToIntyg(@Context UriInfo uriInfo, @PathParam("intygId") String intygId,
            @DefaultValue("") @QueryParam("alternatePatientSSn") String alternatePatientSSn,
            @DefaultValue("") @QueryParam("responsibleHospName") String responsibleHospName,
            @QueryParam(PARAM_PATIENT_FORNAMN) String fornamn,
            @QueryParam(PARAM_PATIENT_EFTERNAMN) String efternamn,
            @QueryParam(PARAM_PATIENT_MELLANNAMN) String mellannamn,
            @QueryParam(PARAM_PATIENT_POSTADRESS) String postadress,
            @QueryParam(PARAM_PATIENT_POSTNUMMER) String postnummer,
            @QueryParam(PARAM_PATIENT_POSTORT) String postort,
            @DefaultValue("false") @QueryParam(PARAM_COHERENT_JOURNALING) boolean coherentJournaling,
            @QueryParam(PARAM_REFERENCE) String reference,
            @QueryParam(PARAM_ENHET) String enhet) {
        return redirectToIntyg(uriInfo, intygId, null, alternatePatientSSn, responsibleHospName, fornamn, efternamn, mellannamn, postadress,
                postnummer, postort, coherentJournaling, reference, enhet);
    }
    // CHECKSTYLE:OFF ParameterNumber

    /**
     * Fetches a certificate from IT or webcert and then performs a redirect to the view that displays
     * the certificate. Can be used for all types of certificates.
     *
     * @param intygId
     *            The id of the certificate to view.
     * @param typ
     *            The type of certificate
     */
    @GET
    @Path("/{typ}/{intygId}")
    // CHECKSTYLE:OFF ParameterNumber
    public Response redirectToIntyg(@Context UriInfo uriInfo, @PathParam("intygId") String intygId, @PathParam("typ") String typ,
            @DefaultValue("") @QueryParam("alternatePatientSSn") String alternatePatientSSn,
            @DefaultValue("") @QueryParam("responsibleHospName") String responsibleHospName,
            @QueryParam(PARAM_PATIENT_FORNAMN) String fornamn,
            @QueryParam(PARAM_PATIENT_EFTERNAMN) String efternamn,
            @QueryParam(PARAM_PATIENT_MELLANNAMN) String mellannamn,
            @QueryParam(PARAM_PATIENT_POSTADRESS) String postadress,
            @QueryParam(PARAM_PATIENT_POSTNUMMER) String postnummer,
            @QueryParam(PARAM_PATIENT_POSTORT) String postort,
            @DefaultValue("false") @QueryParam(PARAM_COHERENT_JOURNALING) boolean coherentJournaling,
            @QueryParam(PARAM_REFERENCE) String reference,
            @QueryParam(PARAM_ENHET) String enhet) {

        super.validateRedirectToIntyg(intygId);

        if (StringUtils.isBlank(enhet)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MISSING_PARAMETER, "Missing required parameter 'enhet'");
        }
        WebCertUser user = getWebCertUserService().getUser();
        if (getWebCertUserService().isAuthorizedForUnit(enhet, false)) {
            user.changeValdVardenhet(enhet);
        } else {
            throw new AuthoritiesException("Authorization Validation failed because user is not authorized for enhet " + enhet);
        }

        Boolean isUtkast = false;
        Utkast utkast = utkastRepository.findOne(intygId);

        if (utkast != null && !utkast.getStatus().equals(UtkastStatus.SIGNED)) {
            isUtkast = true;
        }

        if (typ == null) {
            typ = utkast != null ? utkast.getIntygsTyp() : Fk7263EntryPoint.MODULE_ID;
        }

        if (!typ.equals(Fk7263EntryPoint.MODULE_ID)) {
            if (StringUtils.isBlank(fornamn)) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MISSING_PARAMETER, "Missing required parameter 'fornamn'");
            }
            if (StringUtils.isBlank(efternamn)) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MISSING_PARAMETER, "Missing required parameter 'efternamn'");
            }
            if (StringUtils.isBlank(postadress)) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MISSING_PARAMETER, "Missing required parameter 'postadress'");
            }
            if (StringUtils.isBlank(postnummer)) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MISSING_PARAMETER, "Missing required parameter 'postnummer'");
            }
            if (StringUtils.isBlank(postort)) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MISSING_PARAMETER, "Missing required parameter 'postort'");
            }
        }

        if (!StringUtils.isBlank(reference)) {
            user.setReference(reference);
        }

        PatientParameter patientDetails = new PatientParameter(fornamn, efternamn, mellannamn, postadress, postnummer, postort);

        LOG.debug("Redirecting to view intyg {} of type {} coherent journaling: {}", intygId, typ, coherentJournaling);
        return buildRedirectResponse(uriInfo, typ, intygId, alternatePatientSSn, responsibleHospName,
                patientDetails, isUtkast, coherentJournaling);
    }
    // CHECKSTYLE:OFF ParameterNumber

    public void setUrlIntygFragmentTemplate(String urlFragmentTemplate) {
        this.urlIntygFragmentTemplate = urlFragmentTemplate;
    }

    public void setUrlUtkastFragmentTemplate(String urlFragmentTemplate) {
        this.urlUtkastFragmentTemplate = urlFragmentTemplate;
    }

    // - - - - - Protected scope - - - - -

    @Override
    protected String[] getGrantedRoles() {
        return GRANTED_ROLES;
    }

    @Override
    protected UserOriginType getGrantedRequestOrigin() {
        return GRANTED_ORIGIN;
    }

    // - - - - - Private scope - - - - -

    private Response buildRedirectResponse(UriInfo uriInfo, String certificateType, String certificateId, String alternatePatientSSn,
            String responsibleHospName, PatientParameter patientDetails, Boolean isUtkast, boolean coherentJournaling) {

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();

        Map<String, Object> urlParams = new HashMap<>();
        urlParams.put(PARAM_CERT_TYPE, certificateType);
        urlParams.put(PARAM_CERT_ID, certificateId);
        urlParams.put(PARAM_PATIENT_SSN, alternatePatientSSn);

        String urlFragmentTemplate;
        if (isUtkast) {
            urlParams.put(PARAM_HOSP_NAME, responsibleHospName);
            urlFragmentTemplate = this.urlUtkastFragmentTemplate;
        } else {
            urlFragmentTemplate = this.urlIntygFragmentTemplate;
        }

        if (patientDetails.getFornamn() != null) {
            urlParams.put(PARAM_PATIENT_FORNAMN, patientDetails.getFornamn());
            urlFragmentTemplate += "&" + PARAM_PATIENT_FORNAMN + "={" + PARAM_PATIENT_FORNAMN + "}";
        }
        if (patientDetails.getMellannamn() != null) {
            urlParams.put(PARAM_PATIENT_MELLANNAMN, patientDetails.getMellannamn());
            urlFragmentTemplate += "&" + PARAM_PATIENT_MELLANNAMN + "={" + PARAM_PATIENT_MELLANNAMN + "}";
        }
        if (patientDetails.getEfternamn() != null) {
            urlParams.put(PARAM_PATIENT_EFTERNAMN, patientDetails.getEfternamn());
            urlFragmentTemplate += "&" + PARAM_PATIENT_EFTERNAMN + "={" + PARAM_PATIENT_EFTERNAMN + "}";
        }
        if (patientDetails.getPostadress() != null) {
            urlParams.put(PARAM_PATIENT_POSTADRESS, patientDetails.getPostadress());
            urlFragmentTemplate += "&" + PARAM_PATIENT_POSTADRESS + "={" + PARAM_PATIENT_POSTADRESS + "}";
        }
        if (patientDetails.getPostnummer() != null) {
            urlParams.put(PARAM_PATIENT_POSTNUMMER, patientDetails.getPostnummer());
            urlFragmentTemplate += "&" + PARAM_PATIENT_POSTNUMMER + "={" + PARAM_PATIENT_POSTNUMMER + "}";
        }
        if (patientDetails.getPostort() != null) {
            urlParams.put(PARAM_PATIENT_POSTORT, patientDetails.getPostort());
            urlFragmentTemplate += "&" + PARAM_PATIENT_POSTORT + "={" + PARAM_PATIENT_POSTORT + "}";
        }

        // Add query param for coherent journaling
        if (coherentJournaling) {
            urlParams.put(PARAM_COHERENT_JOURNALING, coherentJournaling);
            urlFragmentTemplate += "&" + PARAM_COHERENT_JOURNALING + "={" + PARAM_COHERENT_JOURNALING + "}";
        }

        URI location = uriBuilder.replacePath(getUrlBaseTemplate()).fragment(urlFragmentTemplate).buildFromMap(urlParams);

        return Response.status(Status.TEMPORARY_REDIRECT).location(location).build();
    }

}
