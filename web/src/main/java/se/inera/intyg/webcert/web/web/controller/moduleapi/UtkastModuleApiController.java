/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

import io.swagger.annotations.Api;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Optional;
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
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.access.AccessResult;
import se.inera.intyg.webcert.web.service.access.DraftAccessService;
import se.inera.intyg.webcert.web.service.access.LockedDraftAccessService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.relation.CertificateRelationService;
import se.inera.intyg.webcert.web.service.utkast.CopyUtkastService;
import se.inera.intyg.webcert.web.service.utkast.UtkastCandidateServiceImpl;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateUtkastFromTemplateRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateUtkastFromTemplateResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidation;
import se.inera.intyg.webcert.web.service.utkast.dto.SaveDraftResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.UtkastCandidateMetaData;
import se.inera.intyg.webcert.web.service.utkast.util.CopyUtkastServiceHelper;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.DraftHolder;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.RevokeSignedIntygParameter;
import se.inera.intyg.webcert.web.web.util.access.AccessResultExceptionHelper;
import se.inera.intyg.webcert.web.web.util.resourcelinks.ResourceLinkHelper;

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

    @Autowired
    private DraftAccessService draftAccessService;

    @Autowired
    private LockedDraftAccessService lockedDraftAccessService;

    @Autowired
    private ResourceLinkHelper resourceLinkHelper;

    @Autowired
    private AccessResultExceptionHelper accessResultExceptionHelper;

    @Autowired
    private UtkastCandidateServiceImpl utkastCandidateService;

    /*
    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private IntygModuleFacade moduleFacade;

    @Autowired
    private LogService logService;

    @Autowired
    private LogRequestFactory logRequestFactory;
    */

    /**
     * Returns the draft certificate as JSON identified by the intygId.
     *
     * @param intygsId The id of the certificate
     * @return a JSON object
     */
    @GET
    @Path("/{intygsTyp}/{intygsId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response getDraft(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
        @Context HttpServletRequest request) {

        LOG.debug("Retrieving Intyg with id {} and type {}", intygsId, intygsTyp);

        Utkast utkast = utkastService.getDraft(intygsId, intygsTyp);

        // Do authorization check
        validateAllowToReadUtkast(utkast, utkast.getPatientPersonnummer());

        request.getSession(true).removeAttribute(LAST_SAVED_DRAFT);

        // Business logic below should not be here inside a controller. Should preferably be moved in the future.
        try {
            final ModuleApi moduleApi = moduleRegistry.getModuleApi(intygsTyp, utkast.getIntygTypeVersion());
            Utlatande utlatande = moduleApi.getUtlatandeFromJson(utkast.getModel());

            Patient savedPatientData = utlatande.getGrundData().getPatient();
            Patient resolvedPatientData = getPatientDataFromPU(intygsTyp, utkast);

            // Do not change the execution order below
            DraftHolder newDraft = createDraftHolder(utkast, savedPatientData, resolvedPatientData);

            if (!resolvedPatientData.isCompleteAddressProvided()) {
                copyOldAddressToNewPatientData(savedPatientData, resolvedPatientData);
            }

            // Update the internal model with the resolved patient. This means that
            // the draft may be updated with new patient info on the next auto-save!
            String updatedModel = moduleApi.updateBeforeSave(utkast.getModel(), resolvedPatientData);
            //utkast.setModel(updatedModel);
            newDraft.setContent(updatedModel);

            // If utkast actually is a newly created draft, we shall provide
            // candidate information to copy data from
            if (utkast.getStatus().equals(UtkastStatus.DRAFT_INCOMPLETE) && utkast.getVersion() == 0) {
                Optional<UtkastCandidateMetaData> metaData =
                    utkastCandidateService.getCandidateMetaData(moduleApi, resolvedPatientData, false);
                // Update draft with meta data
                newDraft.setCandidateMetaData(metaData.orElse(null));
            }

            resourceLinkHelper.decorateUtkastWithValidActionLinks(
                newDraft,
                intygsTyp,
                utlatande.getGrundData().getSkapadAv().getVardenhet(),
                resolvedPatientData.getPersonId());

            return Response.ok(newDraft).build();

        } catch (ModuleException | ModuleNotFoundException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e.getMessage());
        } catch (IOException e) {
            LOG.error("Error while using the module api to convert json to Utlatande for intygsId {}", intygsId);
            throw new RuntimeException("Error while using the module api to convert json to Utlatande", e);
        }
    }

    private Patient getPatientDataFromPU(String intygsTyp, Utkast utkast) {
        Patient resolvedPatientData = patientDetailsResolver.resolvePatient(
            utkast.getPatientPersonnummer(), intygsTyp, utkast.getIntygTypeVersion());
        if (resolvedPatientData == null) {
            throw new WebCertServiceException(
                WebCertServiceErrorCodeEnum.PU_PROBLEM, "Could not resolve Patient in PU-service when opening draft.");
        }
        return resolvedPatientData;
    }

    private DraftHolder createDraftHolder(Utkast utkast, Patient savedPatientData, Patient resolvedPatientData) {
        DraftHolder draftHolder = new DraftHolder();
        draftHolder.setVersion(utkast.getVersion());
        draftHolder.setVidarebefordrad(utkast.getVidarebefordrad());
        draftHolder.setStatus(utkast.getStatus());
        draftHolder.setEnhetsNamn(utkast.getEnhetsNamn());
        draftHolder.setVardgivareNamn(utkast.getVardgivarNamn());
        draftHolder.setKlartForSigneringDatum(utkast.getKlartForSigneringDatum());
        draftHolder.setAterkalladDatum(utkast.getAterkalladDatum());
        draftHolder.setCreated(utkast.getSkapad());
        draftHolder.setRevokedAt(utkast.getAterkalladDatum());

        // Upgrade to latest minor version available for major version of the intygtype
        draftHolder.setLatestTextVersion(
            intygTextsService.getLatestVersionForSameMajorVersion(utkast.getIntygsTyp(), utkast.getIntygTypeVersion()));

        // Handle relations
        Relations relations1 = certificateRelationService.getRelations(utkast.getIntygsId());
        draftHolder.setRelations(relations1);

        // Update patient information
        draftHolder.setPatientResolved(true); // Is the patientResolved property unnecessary?
        draftHolder.setSekretessmarkering(resolvedPatientData.isSekretessmarkering());
        draftHolder.setAvliden(resolvedPatientData.isAvliden());

        draftHolder.setPatientNameChangedInPU(patientDetailsResolver.isPatientNamedChanged(savedPatientData, resolvedPatientData));

        if (resolvedPatientData.isCompleteAddressProvided()) {
            draftHolder.setValidPatientAddressAquiredFromPU(true);
            draftHolder.setPatientAddressChangedInPU(patientDetailsResolver.isPatientAddressChanged(savedPatientData, resolvedPatientData));
        } else {
            // Overwrite retrieved address data with saved one.
            draftHolder.setValidPatientAddressAquiredFromPU(false);
            draftHolder.setPatientAddressChangedInPU(false);
        }

        return draftHolder;
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

    /**
     * Persists the supplied draft certificate using the intygId as key.
     *
     * @param intygsId The id of the certificate.
     * @param payload Object holding the certificate and its current status.
     */
    @PUT
    @Path("/{intygsTyp}/{intygsId}/{version}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response saveDraft(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
        @PathParam("version") long version,
        @DefaultValue("false") @QueryParam("autoSave") boolean autoSave, byte[] payload, @Context HttpServletRequest request) {

        Utkast utkast = utkastService.getDraft(intygsId, intygsTyp, false);

        validateAllowToEditUtkast(utkast);

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
     * @param intygsId The id of the certificate.
     * @param payload Object holding the certificate and its current status.
     */
    @POST
    @Path("/{intygsTyp}/{intygsId}/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response validateDraft(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
        byte[] payload) {

        Utkast utkast = utkastService.getDraft(intygsId, intygsTyp, false);

        validateAllowToReadUtkast(utkast, utkast.getPatientPersonnummer());

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
    @PrometheusTimeMethod
    public Response copyUtkast(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String orgIntygsId) {

        LOG.debug("Attempting to create a new certificate from certificate with type {} and id '{}'",
            intygsTyp, orgIntygsId);

        Utkast utkast = utkastService.getDraft(orgIntygsId, intygsTyp);

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
     * @param intygsId The id of the certificate
     */
    @DELETE
    @Path("/{intygsTyp}/{intygsId}/{version}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response discardDraft(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
        @PathParam("version") long version,
        @Context HttpServletRequest request) {

        Utkast utkast = utkastService.getDraft(intygsId, intygsTyp, false);

        validateAllowToDeleteUtkast(utkast);

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
     * @param param A JSON struct containing an optional message
     */
    @POST
    @Path("/{intygsTyp}/{intygsId}/aterkalla")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response revokeLockedDraft(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
        RevokeSignedIntygParameter param) {

        Utkast utkast = utkastService.getDraft(intygsId, intygsTyp, false);

        validateAllowToInvalidateLockedUtkast(utkast);

        if (authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
            .features(AuthoritiesConstants.FEATURE_MAKULERA_INTYG_KRAVER_ANLEDNING).isVerified() && !param.isValid()) {
            LOG.warn("Request to revoke '{}' is not valid", intygsId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Missing vital arguments in payload");
        }

        utkastService.revokeLockedDraft(intygsId, intygsTyp, param.getMessage(), param.getReason());

        return Response.ok().build();
    }

    private void validateAllowToReadUtkast(Utkast utkast, Personnummer personnummer) {
        final AccessResult accessResult = draftAccessService.allowToReadDraft(
            utkast.getIntygsTyp(),
            getVardenhet(utkast),
            personnummer);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private void validateAllowToEditUtkast(Utkast utkast) {
        final AccessResult accessResult = draftAccessService.allowToEditDraft(
            utkast.getIntygsTyp(),
            getVardenhet(utkast),
            utkast.getPatientPersonnummer());

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private void validateAllowToDeleteUtkast(Utkast utkast) {
        final AccessResult accessResult = draftAccessService.allowToDeleteDraft(
            utkast.getIntygsTyp(),
            getVardenhet(utkast),
            utkast.getPatientPersonnummer());

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private void validateAllowToInvalidateLockedUtkast(Utkast utkast) {
        final AccessResult accessResult = lockedDraftAccessService.allowedToInvalidateLockedUtkast(
            utkast.getIntygsTyp(),
            getVardenhet(utkast),
            utkast.getPatientPersonnummer());

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private Vardenhet getVardenhet(Utkast utkast) {
        final Vardgivare vardgivare = new Vardgivare();
        vardgivare.setVardgivarid(utkast.getVardgivarId());

        final Vardenhet vardenhet = new Vardenhet();
        vardenhet.setEnhetsid(utkast.getEnhetsId());
        vardenhet.setVardgivare(vardgivare);

        return vardenhet;
    }
}
