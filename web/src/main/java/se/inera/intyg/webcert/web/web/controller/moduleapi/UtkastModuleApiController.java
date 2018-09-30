/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.moduleapi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.persistence.OptimisticLockException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;

import com.google.common.base.Strings;
import io.swagger.annotations.Api;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.relation.CertificateRelationService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.CopyUtkastService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateUtkastFromTemplateRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateUtkastFromTemplateResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidation;
import se.inera.intyg.webcert.web.service.utkast.dto.SaveDraftResponse;
import se.inera.intyg.webcert.web.service.utkast.util.CopyUtkastServiceHelper;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.DraftHolder;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.RevokeSignedIntygParameter;

/**
 * Controller for module interaction with drafts.
 *
 * @author npet
 */
@Path("/utkast")
@Api(value = "utkast", description = "REST API - moduleapi - utkast", produces = MediaType.APPLICATION_JSON)
public class UtkastModuleApiController extends AbstractApiController {

    public static final String LAST_SAVED_DRAFT = "lastSavedDraft";

    private static final Logger LOG = LoggerFactory.getLogger(UtkastModuleApiController.class);

    @Autowired
    private UtkastService utkastService;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Autowired
    private IntygTextsService intygTextsService;

    @Autowired
    private CertificateRelationService certificateRelationService;

    @Autowired
    private PatientDetailsResolver patientDetailsResolver;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private CopyUtkastService copyUtkastService;

    @Autowired
    private CopyUtkastServiceHelper copyUtkastServiceHelper;

    /**
     * Returns the draft certificate as JSON identified by the intygId.
     *
     * @param intygsId
     *            The id of the certificate
     * @return a JSON object
     */
    @GET
    @Path("/{intygsTyp}/{intygsId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response getDraft(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
            @Context HttpServletRequest request) {

        LOG.debug("Retrieving Intyg with id {} and type {}", intygsId, intygsTyp);

        Utkast utkast = utkastService.getDraft(intygsId, intygsTyp);

        Patient resolvedPatient = patientDetailsResolver.resolvePatient(utkast.getPatientPersonnummer(), intygsTyp);
        if (resolvedPatient == null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.PU_PROBLEM,
                    "Could not resolve Patient in PU-service when opening draft.");
        }

        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                .features(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST)
                .privilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG)
                .orThrow();

        verifySekretessmarkering(intygsTyp, utkast.getEnhetsId(), resolvedPatient);

        request.getSession(true).removeAttribute(LAST_SAVED_DRAFT);

        DraftHolder draftHolder = new DraftHolder();
        draftHolder.setVersion(utkast.getVersion());
        draftHolder.setVidarebefordrad(utkast.getVidarebefordrad());
        draftHolder.setStatus(utkast.getStatus());
        draftHolder.setEnhetsNamn(utkast.getEnhetsNamn());
        draftHolder.setVardgivareNamn(utkast.getVardgivarNamn());
        // Upgrade to latest minor version available for major version of the intygtype
        draftHolder.setLatestTextVersion(
                intygTextsService.getLatestVersionForSameMajorVersion(utkast.getIntygsTyp(), utkast.getIntygTypeVersion()));

        Relations relations1 = certificateRelationService.getRelations(utkast.getIntygsId());
        draftHolder.setRelations(relations1);
        draftHolder.setKlartForSigneringDatum(utkast.getKlartForSigneringDatum());
        draftHolder.setCreated(utkast.getSkapad());
        draftHolder.setRevokedAt(utkast.getAterkalladDatum());
        // The patientResolved is unnecessary?
        draftHolder.setPatientResolved(true);
        draftHolder.setSekretessmarkering(resolvedPatient.isSekretessmarkering());
        draftHolder.setAvliden(resolvedPatient.isAvliden());

        // Businesss logic below should not be here inside a controller.. Should preferably be moved in the future.
        try {
            final ModuleApi moduleApi = moduleRegistry.getModuleApi(intygsTyp, utkast.getIntygTypeVersion());
            Utlatande utlatande = moduleApi.getUtlatandeFromJson(utkast.getModel());

            draftHolder.setPatientNameChangedInPU(patientDetailsResolver.isPatientNamedChanged(
                    utlatande.getGrundData().getPatient(), resolvedPatient));

            if (completeAddressProvided(resolvedPatient)) {
                draftHolder.setValidPatientAddressAquiredFromPU(true);
                draftHolder.setPatientAddressChangedInPU(patientDetailsResolver.isPatientAddressChanged(
                        utlatande.getGrundData().getPatient(), resolvedPatient));
            } else {
                // Overwrite retrieved address data with saved one.
                draftHolder.setValidPatientAddressAquiredFromPU(false);
                draftHolder.setPatientAddressChangedInPU(false);
                Patient oldPatientData = utlatande.getGrundData().getPatient();
                copyOldAddressToNewPatientData(oldPatientData, resolvedPatient);
            }
            // Update the internal model with the resolved patient. This means the draft may be updated
            // with new patient info on the next auto-save!
            String updatedModel = moduleApi.updateBeforeSave(utkast.getModel(), resolvedPatient);
            utkast.setModel(updatedModel);
            draftHolder.setContent(utkast.getModel());

            return Response.ok(draftHolder).build();
        } catch (ModuleException | ModuleNotFoundException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e.getMessage());
        } catch (IOException e) {
            LOG.error("Error while using the module api to convert json to Utlatande for intygsId {}", intygsId);
            throw new RuntimeException("Error while using the module api to convert json to Utlatande", e);
        }
    }

    // Copied from IntygServiceImpl, INTYG-5380
    private static boolean completeAddressProvided(Patient patient) {
        return !Strings.isNullOrEmpty(patient.getPostadress())
                && !Strings.isNullOrEmpty(patient.getPostort())
                && !Strings.isNullOrEmpty(patient.getPostnummer());
    }

    // Copied from IntygServiceImpl, INTYG-5380
    private static void copyOldAddressToNewPatientData(Patient oldPatientData, Patient newPatientData) {
        if (oldPatientData == null) {
            newPatientData.setPostadress(null);
            newPatientData.setPostnummer(null);
            newPatientData.setPostort(null);
        } else {
            newPatientData.setPostadress(oldPatientData.getPostadress());
            newPatientData.setPostnummer(oldPatientData.getPostnummer());
            newPatientData.setPostort(oldPatientData.getPostort());

        }
    }

    private void verifySekretessmarkering(String intygsTyp, String enhetsId, Patient resolvedPatient) {
        WebCertUser user = getWebCertUserService().getUser();

        authoritiesValidator.given(user, intygsTyp)
                .privilegeIf(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT,
                        resolvedPatient != null && resolvedPatient.isSekretessmarkering())
                .orThrow(new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM_SEKRETESSMARKERING,
                        "User missing required privilege or cannot handle sekretessmarkerad patient"));

        // INTYG-4231: Om patienten är sekretessmarkerad så måste användaren vara inloggad på exakt samma vårdenhet
        // som utkastet tillhör
        if (resolvedPatient != null && resolvedPatient.isSekretessmarkering()
                && !getWebCertUserService().userIsLoggedInOnEnhetOrUnderenhet(enhetsId)) {
            LOG.debug("User not logged in on same unit as draft unit for sekretessmarkerad patient.");
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM_SEKRETESSMARKERING_ENHET,
                    "User not logged in on same unit as draft unit for sekretessmarkerad patient.");
        }
    }

    /**
     * Persists the supplied draft certificate using the intygId as key.
     *
     * @param intygsId
     *            The id of the certificate.
     * @param payload
     *            Object holding the certificate and its current status.
     */
    @PUT
    @Path("/{intygsTyp}/{intygsId}/{version}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response saveDraft(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
            @PathParam("version") long version,
            @DefaultValue("false") @QueryParam("autoSave") boolean autoSave, byte[] payload, @Context HttpServletRequest request) {

        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                .features(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST)
                .privilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG)
                .orThrow();

        LOG.debug("Saving utkast with id '{}', autosave is {}", intygsId, autoSave);

        String draftAsJson = fromBytesToString(payload);

        LOG.debug("---- intyg : " + draftAsJson);

        boolean firstSave = false;
        HttpSession session = request.getSession(true);
        String lastSavedDraft = (String) session.getAttribute(LAST_SAVED_DRAFT);
        if (!intygsId.equals(lastSavedDraft)) {
            firstSave = true;
        }
        session.setAttribute(LAST_SAVED_DRAFT, intygsId);

        try {
            SaveDraftResponse saveResponse = utkastService.saveDraft(intygsId, version, draftAsJson, firstSave);

            return Response.ok().entity(saveResponse).build();
        } catch (OptimisticLockException | OptimisticLockingFailureException e) {
            monitoringLogService.logUtkastConcurrentlyEdited(intygsId, intygsTyp);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.CONCURRENT_MODIFICATION, e.getMessage());
        }
    }

    /**
     * Validate the supplied draft certificate.
     *
     * @param intygsId
     *            The id of the certificate.
     * @param payload
     *            Object holding the certificate and its current status.
     */
    @POST
    @Path("/{intygsTyp}/{intygsId}/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response validateDraft(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
            byte[] payload) {

        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                .features(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST)
                .privilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG)
                .orThrow();

        LOG.debug("Validating utkast with id '{}'", intygsId);

        String draftAsJson = fromBytesToString(payload);

        LOG.debug("---- intyg : " + draftAsJson);

        DraftValidation validateResponse = utkastService.validateDraft(intygsId, intygsTyp, draftAsJson);

        LOG.debug("Utkast validation on '{}' is {}", intygsId, validateResponse.isDraftValid() ? "valid" : "invalid");
        return Response.ok().entity(validateResponse).build();
    }

    /**
     * Create a new utkast from locked utkast.
     */
    @POST
    @Path("/{intygsTyp}/{intygsId}/copy")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response copyUtkast(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String orgIntygsId) {

        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                .features(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST)
                .privilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG)
                .orThrow();

        LOG.debug("Attempting to create a new certificate from certificate with type {} and id '{}'",
                intygsTyp, orgIntygsId);

        WebCertUser user = getWebCertUserService().getUser();

        boolean copyOkParam = user.getParameters() == null || user.getParameters().isCopyOk();
        if (!copyOkParam) {
            LOG.info("User is not allowed to request a copy for id '{}' due to false kopieraOK-parameter", orgIntygsId);
            final String message = "Authorization failed due to false kopieraOK-parameter";
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM, message);
        }

        if (user.getParameters() != null && user.getParameters().isInactiveUnit()) {
            LOG.info("User is not allowed to request a copy for id '{}' due to true inaktivEnhet-parameter", orgIntygsId);
            final String message = "Authorization failed due to true inaktivEnhet-parameter";
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM, message);
        }

        Utkast utkast = utkastService.getDraft(orgIntygsId, intygsTyp);

        // Check avliden
        if (patientDetailsResolver.isAvliden(utkast.getPatientPersonnummer())) {
            authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                    .features(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST_AVLIDEN)
                    .orThrow();
        }

        CopyIntygRequest request = new CopyIntygRequest();
        request.setPatientPersonnummer(utkast.getPatientPersonnummer());

        CreateUtkastFromTemplateRequest serviceRequest = copyUtkastServiceHelper.createUtkastFromUtkast(orgIntygsId, intygsTyp, request);
        CreateUtkastFromTemplateResponse serviceResponse = copyUtkastService.createUtkastCopy(serviceRequest);

        LOG.debug("Created a new draft with id: '{}' and type: {} from certificate with type: {} and id '{}'.",
                serviceResponse.getNewDraftIntygId(), serviceResponse.getNewDraftIntygType(), intygsTyp, orgIntygsId);

        CopyIntygResponse response = new CopyIntygResponse(serviceResponse.getNewDraftIntygId(), serviceResponse.getNewDraftIntygType(),
                serviceResponse.getNewDraftIntygTypeVersion());

        return Response.ok().entity(response).build();
    }

    private String fromBytesToString(byte[] bytes) {
        try {
            return new String(bytes, UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Could not convert the payload from draftCertificate to String!", e);
        }
    }

    /**
     * Deletes a draft certificate identified by the certificateId.
     *
     * @param intygsId
     *            The id of the certificate
     */
    @DELETE
    @Path("/{intygsTyp}/{intygsId}/{version}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response discardDraft(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
            @PathParam("version") long version,
            @Context HttpServletRequest request) {

        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                .features(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST)
                .privilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG)
                .orThrow();

        LOG.debug("Deleting draft with id {}", intygsId);

        try {
            utkastService.deleteUnsignedDraft(intygsId, version);
        } catch (OptimisticLockException | OptimisticLockingFailureException e) {
            monitoringLogService.logUtkastConcurrentlyEdited(intygsId, intygsTyp);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.CONCURRENT_MODIFICATION, e.getMessage());
        }

        request.getSession(true).removeAttribute(LAST_SAVED_DRAFT);

        return Response.ok().build();
    }

    /**
     * Revoke a locked draft.
     *
     * @param intygsId The id of the intyg to revoke
     * @param param    A JSON struct containing an optional message
     */
    @POST
    @Path("/{intygsTyp}/{intygsId}/aterkalla")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response revokeLockedDraft(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
                                      RevokeSignedIntygParameter param) {
        validateRevokeAuthority(intygsTyp);

        if (authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                .features(AuthoritiesConstants.FEATURE_MAKULERA_INTYG_KRAVER_ANLEDNING).isVerified() && !param.isValid()) {
            LOG.warn("Request to revoke '{}' is not valid", intygsId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Missing vital arguments in payload");
        }

        utkastService.revokeLockedDraft(intygsId, intygsTyp, param.getMessage(), param.getReason());

        return Response.ok().build();
    }

    private void validateRevokeAuthority(String intygsTyp) {
        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                .features(AuthoritiesConstants.FEATURE_MAKULERA_INTYG)
                .privilege(AuthoritiesConstants.PRIVILEGE_MAKULERA_INTYG)
                .orThrow();
    }
}
