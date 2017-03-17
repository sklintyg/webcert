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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Strings;

import io.swagger.annotations.Api;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.user.dto.IntegrationParameters;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.UpdatePatientOnDraftRequest;

/**
 * Controller to enable an external user to access certificates directly from a
 * link in an external patient care system.
 *
 * Please note that the vardenhet selection and auth validation is handled by
 * {@link se.inera.intyg.webcert.web.auth.IntegrationEnhetFilter}.
 *
 * @author bensam
 */
@Path("/intyg")
@Api(value = "intyg (Djupintegration)", description = "REST API för Djupintegration", produces = MediaType.APPLICATION_JSON)
// CHECKSTYLE:OFF ParameterNumber
public class IntygIntegrationController extends BaseIntegrationController {

    private static final String PARAM_CERT_ID = "certId";
    private static final String PARAM_CERT_TYPE = "certType";
    private static final String PARAM_COHERENT_JOURNALING = "sjf";
    private static final String PARAM_INACTIVE_UNIT = "inaktivEnhet";
    private static final String PARAM_PATIENT_ALTERNATE_SSN = "alternatePatientSSn";
    private static final String PARAM_PATIENT_DECEASED = "avliden";
    private static final String PARAM_PATIENT_EFTERNAMN = "efternamn";
    private static final String PARAM_PATIENT_FORNAMN = "fornamn";
    private static final String PARAM_PATIENT_MELLANNAMN = "mellannamn";
    private static final String PARAM_PATIENT_POSTADRESS = "postadress";
    private static final String PARAM_PATIENT_POSTNUMMER = "postnummer";
    private static final String PARAM_PATIENT_POSTORT = "postort";
    private static final String PARAM_REFERENCE = "ref";
    private static final String PARAM_RESPONSIBLE_HOSP_NAME = "responsibleHospName";

    private static final Logger LOG = LoggerFactory.getLogger(IntygIntegrationController.class);

    private static final String[] GRANTED_ROLES = new String[] { AuthoritiesConstants.ROLE_LAKARE, AuthoritiesConstants.ROLE_TANDLAKARE,
            AuthoritiesConstants.ROLE_ADMIN };
    private static final UserOriginType GRANTED_ORIGIN = UserOriginType.DJUPINTEGRATION;

    private String urlIntygFragmentTemplate;
    private String urlUtkastFragmentTemplate;

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private MonitoringLogService monitoringLog;

    @Autowired
    private UtkastService utkastService;

    /**
     * Fetches an certificate from IT or Webcert and then performs a redirect to the view that displays
     * the certificate.
     *
     * @param intygId
     *            The id of the certificate to view.
     */
    @GET
    @Path("/{intygId}")
    public Response redirectToIntyg(@Context UriInfo uriInfo, @PathParam("intygId") String intygId,
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
            @DefaultValue("false") @QueryParam(PARAM_PATIENT_DECEASED) boolean deceased) {
        return redirectToIntyg(uriInfo, intygId, null, alternatePatientSSn, responsibleHospName, fornamn, efternamn, mellannamn, postadress,
                postnummer, postort, coherentJournaling, reference, inactiveUnit, deceased);
    }

    /**
     * Fetches a certificate from IT or webcert and then performs a redirect to the view that displays
     * the certificate. Can be used for all types of certificates.
     *
     * @param intygId
     *            The id of the certificate to view.
     * @param typParam
     *            The type of certificate
     */
    @GET
    @Path("/{typ}/{intygId}")
    public Response redirectToIntyg(@Context UriInfo uriInfo, @PathParam("intygId") String intygId, @PathParam("typ") String typParam,
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
            @DefaultValue("false") @QueryParam(PARAM_PATIENT_DECEASED) boolean deceased) {

        super.validateRedirectToIntyg(intygId);

        WebCertUser user = getWebCertUserService().getUser();

        Boolean isUtkast = false;
        Utkast utkast = utkastRepository.findOne(intygId);

        if (utkast != null && !utkast.getStatus().equals(UtkastStatus.SIGNED)) {
            isUtkast = true;
        }

        // If intygstyp can't be established, default to FK7263 to be backwards compatible
        String intygsTyp = typParam;
        if (typParam == null) {
            intygsTyp = utkast != null ? utkast.getIntygsTyp() : Fk7263EntryPoint.MODULE_ID;
        }

        // Monitoring log the usage of coherentJournaling
        if (coherentJournaling) {
            if (!utkast.getVardgivarId().equals(user.getValdVardgivare().getId())) {
                monitoringLog.logIntegratedOtherCaregiver(intygId, intygsTyp, utkast.getVardgivarId(), utkast.getEnhetsId());
            } else if (!user.getValdVardenhet().getHsaIds().contains(utkast.getEnhetsId())) {
                monitoringLog.logIntegratedOtherUnit(intygId, intygsTyp, utkast.getEnhetsId());
            }
        }

        // If the type doesn't equals to FK7263 then verify the required query-parameters
        if (!intygsTyp.equals(Fk7263EntryPoint.MODULE_ID)) {
            verifyQueryStrings(fornamn, efternamn, postadress, postnummer, postort);
        }

        if (isUtkast) {
            // INTYG-3212: Draft patient info should always be up-to-date with the patient info supplied by the
            // integrating journaling system
            ensureDraftPatientInfoUpdated(intygsTyp, intygId, utkast.getVersion(), alternatePatientSSn, fornamn, mellannamn, efternamn,
                    postadress, postnummer,
                    postort);
        }

        user.setParameters(new IntegrationParameters(StringUtils.trimToNull(reference), responsibleHospName, alternatePatientSSn, fornamn,
                mellannamn, efternamn, postadress, postnummer, postort, coherentJournaling, deceased, inactiveUnit));

        LOG.debug("Redirecting to view intyg {} of type {} coherent journaling: {}", intygId, intygsTyp, coherentJournaling);
        return buildRedirectResponse(uriInfo, intygsTyp, intygId, isUtkast);
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

    /**
     * Updates Patient section of a draft with updated patient details for selected types.
     *
     * @param intygsType
     * @param draftId
     * @param draftVersion
     * @param alternatePatientSSn
     * @param fornamn
     * @param mellannamn
     * @param efternamn
     * @param postadress
     * @param postnummer
     * @param postort
     */
    private void ensureDraftPatientInfoUpdated(String intygsType, String draftId, long draftVersion, String alternatePatientSSn,
            String fornamn, String mellannamn, String efternamn, String postadress, String postnummer, String postort) {

        // To be allowed to update utkast, we need to have the same authority as when saving a draft..
        authoritiesValidator.given(getWebCertUserService().getUser(), intygsType)
                .features(WebcertFeature.HANTERA_INTYGSUTKAST)
                .privilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG)
                .orThrow();

        // 1. Create patient info based on what the journal system supplied
        Patient patient = new Patient();
        patient.setPersonId(new Personnummer(alternatePatientSSn));

        // INTYG-3329: All but Legacy Fk7263 drafts should update all patient properties
        if (!Fk7263EntryPoint.MODULE_ID.equals(intygsType)) {
            patient.setFornamn(fornamn);
            patient.setMellannamn(mellannamn);
            patient.setEfternamn(efternamn);

            if (Strings.nullToEmpty(patient.getMellannamn()).trim().isEmpty()) {
                patient.setFullstandigtNamn(patient.getFornamn() + " " + patient.getEfternamn());
            } else {
                patient.setFullstandigtNamn(patient.getFornamn() + " " + patient.getMellannamn() + " " + patient.getEfternamn());
            }

            patient.setPostadress(postadress);
            patient.setPostnummer(postnummer);
            patient.setPostort(postort);
        }
        UpdatePatientOnDraftRequest request = new UpdatePatientOnDraftRequest(patient, draftId, draftVersion);

        utkastService.updatePatientOnDraft(request);

    }

    private Response buildRedirectResponse(UriInfo uriInfo, String certificateType, String certificateId, boolean isUtkast) {

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();

        Map<String, Object> urlParams = new HashMap<>();
        urlParams.put(PARAM_CERT_TYPE, certificateType);
        urlParams.put(PARAM_CERT_ID, certificateId);

        String urlFragmentTemplate = isUtkast ? urlUtkastFragmentTemplate : urlIntygFragmentTemplate;

        URI location = uriBuilder.replacePath(getUrlBaseTemplate()).fragment(urlFragmentTemplate).buildFromMap(urlParams);

        return Response.status(Status.TEMPORARY_REDIRECT).location(location).build();
    }

    private void verifyQueryStrings(String fornamn, String efternamn, String postadress, String postnummer, String postort) {
        verifyQueryString(PARAM_PATIENT_FORNAMN, fornamn);
        verifyQueryString(PARAM_PATIENT_EFTERNAMN, efternamn);
        verifyQueryString(PARAM_PATIENT_POSTADRESS, postadress);
        verifyQueryString(PARAM_PATIENT_POSTNUMMER, postnummer);
        verifyQueryString(PARAM_PATIENT_POSTORT, postort);
    }

    private void verifyQueryString(String queryStringName, String queryStringValue) {
        if (Strings.nullToEmpty(queryStringValue).trim().isEmpty()) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MISSING_PARAMETER,
                    "Missing required parameter '" + queryStringName + "'");
        }
    }

}
// CHECKSTYLE:ON ParameterNumber
