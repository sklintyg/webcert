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

package se.inera.intyg.webcert.web.web.controller.moduleapi;

import java.util.Arrays;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.common.security.common.model.AuthoritiesConstants;
import se.inera.intyg.common.security.common.model.UserOriginType;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygPdf;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygServiceResult;
import se.inera.intyg.webcert.web.service.utkast.CopyUtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.*;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygResponse;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.RevokeReplaceSignedIntygRequest;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.RevokeSignedIntygParameter;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.SendSignedIntygParameter;
import io.swagger.annotations.Api;

/**
 * Controller exposing services to be used by modules.
 *
 * @author nikpet
 */
@Path("/intyg")
@Api(value = "/moduleapi/intyg", description = "REST API - moduleapi - intyg", produces = MediaType.APPLICATION_JSON)
public class IntygModuleApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(IntygModuleApiController.class);

    private static final String CONTENT_DISPOSITION = "Content-Disposition";

    @Autowired
    private IntygService intygService;

    @Autowired
    private CopyUtkastService copyUtkastService;

    /**
     * Retrieves a signed intyg from intygstjänst.
     *
     * @param intygsId
     *            intygid
     * @return Response
     */
    @GET
    @Path("/{intygsTyp}/{intygsId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response getIntyg(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
            @DefaultValue("false") @QueryParam("sjf") boolean coherentJournaling) {

        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                .privilege(AuthoritiesConstants.PRIVILEGE_VISA_INTYG)
                .orThrow();

        LOG.debug("Fetching signed intyg with id '{}' from IT, coherent journaling {}", intygsId, coherentJournaling);

        IntygContentHolder intygAsExternal = intygService.fetchIntygDataWithRelations(intygsId, intygsTyp, coherentJournaling);

        return Response.ok().entity(intygAsExternal).build();
    }

    /**
     * Return the signed certificate identified by the given id as PDF.
     *
     * @param intygsTyp
     *            the type of certificate
     * @param intygsId
     *            - the globally unique id of a certificate.
     * @return The certificate in PDF format
     */
    @GET
    @Path("/{intygsTyp}/{intygsId}/pdf")
    @Produces("application/pdf")
    public final Response getIntygAsPdf(@PathParam("intygsTyp") String intygsTyp, @PathParam(value = "intygsId") final String intygsId) {
        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                .privilege(AuthoritiesConstants.PRIVILEGE_VISA_INTYG)
                .features(Arrays.asList(WebcertFeature.UTSKRIFT))
                .orThrow();

        return getPdf(intygsTyp, intygsId, false);
    }

    /**
     * Return the signed certificate identified by the given id as PDF suited for the employer of the patient.
     *
     * @param intygsTyp
     *            the type of certificate
     * @param intygsId
     *            - the globally unique id of a certificate.
     * @return The certificate in PDF format
     */
    @GET
    @Path("/{intygsTyp}/{intygsId}/pdf/arbetsgivarutskrift")
    @Produces("application/pdf")
    public final Response getIntygAsPdfForEmployer(@PathParam("intygsTyp") String intygsTyp, @PathParam(value = "intygsId") final String intygsId) {
        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                .privilege(AuthoritiesConstants.PRIVILEGE_VISA_INTYG)
                .features(Arrays.asList(WebcertFeature.ARBETSGIVARUTSKRIFT))
                .orThrow();

        return getPdf(intygsTyp, intygsId, true);
    }

    private Response getPdf(String intygsTyp, final String intygsId, boolean isEmployerCopy) {
        if (!isEmployerCopy) {
            LOG.debug("Fetching signed intyg '{}' as PDF", intygsId);
        } else {
            LOG.debug("Fetching signed intyg '{}' as PDF for employer", intygsId);
        }

        IntygPdf intygPdfResponse = intygService.fetchIntygAsPdf(intygsId, intygsTyp, isEmployerCopy);

        return Response.ok(intygPdfResponse.getPdfData()).header(CONTENT_DISPOSITION, buildPdfHeader(intygPdfResponse.getFilename())).build();
    }

    private String buildPdfHeader(String pdfFileName) {
        return "attachment; filename=\"" + pdfFileName + "\"";
    }

    /**
     * Issues a request to Intygstjanst to send the signed intyg to a recipient.
     */
    @POST
    @Path("/{intygsTyp}/{intygsId}/skicka")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response sendSignedIntyg(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
            SendSignedIntygParameter param) {
        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp).features(WebcertFeature.SKICKA_INTYG).orThrow();
        IntygServiceResult sendResult = intygService.sendIntyg(intygsId, intygsTyp, param.getRecipient());
        return Response.ok(sendResult).build();
    }

    /**
     * Issues a request to Intygstjanst to revoke the signed intyg.
     *
     * @param intygsId
     *            The id of the intyg to revoke
     * @param param
     *            A JSON struct containing an optional message
     */
    @POST
    @Path("/{intygsTyp}/{intygsId}/aterkalla")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response revokeSignedIntyg(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
            RevokeSignedIntygParameter param) {
        validateRevokeAuthority(intygsTyp);
        IntygServiceResult result = revokeIntyg(intygsTyp, intygsId, param);
        return Response.ok(result).build();
    }

    /**
     * Issues a request to Intygstjanst to revoke the signed intyg and then copy it and return the id of the utkast.
     *
     * @param intygsId
     *            The id of the intyg to revoke
     * @param param
     *            A JSON struct containing an optional message
     */
    @POST
    @Path("/{intygsTyp}/{intygsId}/aterkallaersatt")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response revokeReplaceSignedIntyg(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
            RevokeReplaceSignedIntygRequest revokeReplaceRequest) {

        validateRevokeAuthority(intygsTyp);
        validateCopyAuthority(intygsTyp);

        if (!revokeReplaceRequest.getCopyIntygRequest().isValid()) {
            LOG.warn("Request to revoke and replace '{}' is not valid", intygsId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Missing vital arguments in payload");
        }

        revokeIntyg(intygsTyp, intygsId, revokeReplaceRequest.getRevokeSignedIntygParameter());
        CopyIntygResponse response = replaceIntyg(revokeReplaceRequest.getCopyIntygRequest(), intygsTyp, intygsId);

        return Response.ok().entity(response).build();
    }

    /**
     * Create a copy of a certificate.
     *
     * @param request
     * @param intygsTyp
     * @param orgIntygsId
     * @return
     */
    @POST
    @Path("/{intygsTyp}/{intygsId}/kopiera")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response createNewCopy(CopyIntygRequest request, @PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String orgIntygsId) {

        validateCopyAuthority(intygsTyp);

        LOG.debug("Attempting to create a draft copy of {} with id '{}', coherent journaling: {}", intygsTyp, orgIntygsId,
                request.isCoherentJournaling());

        if (!request.isValid()) {
            LOG.error("Request to create copy of '{}' is not valid", orgIntygsId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Missing vital arguments in payload");
        }

        CopyIntygResponse result = copyIntyg(request, intygsTyp, orgIntygsId);
        return Response.ok().entity(result).build();
    }

    /**
     * Create a copy that completes an existing certificate.
     *
     * @param request
     * @param intygsTyp
     * @param orgIntygsId
     * @return
     */
    @POST
    @Path("/{intygsTyp}/{intygsId}/{meddelandeId}/komplettera")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response createCompletion(CopyIntygRequest request, @PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String orgIntygsId,
            @PathParam("meddelandeId") String meddelandeId) {

        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                .features(WebcertFeature.KOPIERA_INTYG)
                .privilege(AuthoritiesConstants.PRIVILEGE_SVARA_MED_NYTT_INTYG)
                .orThrow();

        LOG.debug("Attempting to create a completion of {} with id '{}'", intygsTyp, orgIntygsId);

        if (!request.isValid()) {
            LOG.error("Request to create completion of '{}' is not valid", orgIntygsId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Missing vital arguments in payload");
        }

        CreateCompletionCopyRequest serviceRequest = createCompletionCopyRequest(orgIntygsId, intygsTyp, meddelandeId, request);
        CreateCompletionCopyResponse serviceResponse = copyUtkastService.createCompletion(serviceRequest);

        LOG.debug("Created a new draft with id: '{}' and type: {}, completing certificate with id '{}'.", serviceResponse.getNewDraftIntygId(),
                serviceResponse.getNewDraftIntygType(), orgIntygsId);

        CopyIntygResponse response = new CopyIntygResponse(serviceResponse.getNewDraftIntygId(), serviceResponse.getNewDraftIntygType());

        return Response.ok().entity(response).build();
    }

    /**
     * Create a copy that is a renewal of an existing certificate.
     *
     * @param request
     * @param intygsTyp
     * @param orgIntygsId
     * @return
     */
    @POST
    @Path("/{intygsTyp}/{intygsId}/fornya")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response createRenewal(CopyIntygRequest request, @PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String orgIntygsId) {
        validateCopyAuthority(intygsTyp);

        LOG.debug("Attempting to create a renewal of {} with id '{}'", intygsTyp, orgIntygsId);

        if (!request.isValid()) {
            LOG.error("Request to create renewal of '{}' is not valid", orgIntygsId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Missing vital arguments in payload");
        }

        CreateRenewalCopyRequest serviceRequest = createRenewalCopyRequest(orgIntygsId, intygsTyp, request);
        CreateRenewalCopyResponse serviceResponse = copyUtkastService.createRenewalCopy(serviceRequest);

        LOG.debug("Created a new draft with id: '{}' and type: {}, renewing certificate with id '{}'.",
                serviceResponse.getNewDraftIntygId(), serviceResponse.getNewDraftIntygType(), orgIntygsId);

        CopyIntygResponse response = new CopyIntygResponse(serviceResponse.getNewDraftIntygId(), serviceResponse.getNewDraftIntygType());

        return Response.ok().entity(response).build();
    }

    private CreateRenewalCopyRequest createRenewalCopyRequest(String orgIntygsId, String intygsTyp, CopyIntygRequest request) {
        HoSPersonal hosPerson = createHoSPersonFromUser();
        Patient patient = createPatientFromCopyIntygRequest(request);

        CreateRenewalCopyRequest req = new CreateRenewalCopyRequest(orgIntygsId, intygsTyp, patient, hosPerson);

        if (request.containsNewPersonnummer()) {
            LOG.debug("Adding new personnummer to request");
            req.setNyttPatientPersonnummer(request.getNyttPatientPersonnummer());
        }

        if (authoritiesValidator.given(getWebCertUserService().getUser()).origins(UserOriginType.DJUPINTEGRATION).isVerified()) {
            LOG.debug("Setting djupintegrerad flag on request to true");
            req.setDjupintegrerad(true);
        }
        return req;
    }

    private CreateCompletionCopyRequest createCompletionCopyRequest(String orgIntygsId, String intygsTyp, String meddelandeId,
            CopyIntygRequest copyRequest) {
        HoSPersonal hosPerson = createHoSPersonFromUser();
        Patient patient = createPatientFromCopyIntygRequest(copyRequest);

        CreateCompletionCopyRequest req = new CreateCompletionCopyRequest(orgIntygsId, intygsTyp, meddelandeId, patient, hosPerson);

        if (copyRequest.containsNewPersonnummer()) {
            LOG.debug("Adding new personnummer to request");
            req.setNyttPatientPersonnummer(copyRequest.getNyttPatientPersonnummer());
        }

        if (authoritiesValidator.given(getWebCertUserService().getUser()).origins(UserOriginType.DJUPINTEGRATION).isVerified()) {
            LOG.debug("Setting djupintegrerad flag on request to true");
            req.setDjupintegrerad(true);
        }

        return req;
    }

    private CreateNewDraftCopyRequest createNewDraftCopyRequest(String originalIntygId, String intygsTyp, CopyIntygRequest copyRequest) {
        HoSPersonal hosPerson = createHoSPersonFromUser();
        Patient patient = createPatientFromCopyIntygRequest(copyRequest);

        CreateNewDraftCopyRequest req = new CreateNewDraftCopyRequest(originalIntygId, intygsTyp, patient, hosPerson,
                copyRequest.isCoherentJournaling());

        if (copyRequest.containsNewPersonnummer()) {
            LOG.debug("Adding new personnummer to request");
            req.setNyttPatientPersonnummer(copyRequest.getNyttPatientPersonnummer());
        }

        if (authoritiesValidator.given(getWebCertUserService().getUser()).origins(UserOriginType.DJUPINTEGRATION).isVerified()) {
            LOG.debug("Setting djupintegrerad flag on request to true");
            req.setDjupintegrerad(true);
        }

        return req;
    }

    private Patient createPatientFromCopyIntygRequest(CopyIntygRequest copyRequest) {
        Patient patient = new Patient();

        patient.setPersonId(copyRequest.getPatientPersonnummer());

        if (!StringUtils.isBlank(copyRequest.getFornamn())
                && !StringUtils.isBlank(copyRequest.getEfternamn())
                && !StringUtils.isBlank(copyRequest.getPostadress())
                && !StringUtils.isBlank(copyRequest.getPostnummer())
                && !StringUtils.isBlank(copyRequest.getPostort())) {
            patient.setFornamn(copyRequest.getFornamn());
            patient.setEfternamn(copyRequest.getEfternamn());
            patient.setMellannamn(copyRequest.getMellannamn());
            patient.setPostadress(copyRequest.getPostadress());
            patient.setPostnummer(copyRequest.getPostnummer());
            patient.setPostort(copyRequest.getPostort());
        }
        return patient;
    }

    private IntygServiceResult revokeIntyg(String intygsTyp, String intygsId, RevokeSignedIntygParameter param) {
        String revokeMessage = (param != null) ? param.getRevokeMessage() : null;
        return intygService.revokeIntyg(intygsId, intygsTyp, revokeMessage);
    }

    private CopyIntygResponse copyIntyg(CopyIntygRequest request, String intygsTyp, String orgIntygsId) {
        CreateNewDraftCopyRequest serviceRequest = createNewDraftCopyRequest(orgIntygsId, intygsTyp, request);
        CreateNewDraftCopyResponse serviceResponse = copyUtkastService.createCopy(serviceRequest);

        LOG.debug("Created a new draft copy from '{}' with id '{}' and type {}", orgIntygsId, serviceResponse.getNewDraftIntygId(),
                serviceResponse.getNewDraftIntygType());

        return new CopyIntygResponse(serviceResponse.getNewDraftIntygId(), serviceResponse.getNewDraftIntygType());
    }

    private CopyIntygResponse replaceIntyg(CopyIntygRequest request, String intygsTyp, String orgIntygsId) {
        CreateNewDraftCopyRequest serviceRequest = createNewDraftCopyRequest(orgIntygsId, intygsTyp, request);
        CreateNewDraftCopyResponse serviceResponse = copyUtkastService.createReplacementCopy(serviceRequest);

        LOG.debug("Created a new replacement copy from '{}' with id '{}' and type {}", orgIntygsId, serviceResponse.getNewDraftIntygId(),
                serviceResponse.getNewDraftIntygType());

        return new CopyIntygResponse(serviceResponse.getNewDraftIntygId(), serviceResponse.getNewDraftIntygType());
    }

    private void validateRevokeAuthority(String intygsTyp) {
        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                .features(WebcertFeature.MAKULERA_INTYG)
                .privilege(AuthoritiesConstants.PRIVILEGE_MAKULERA_INTYG)
                .orThrow();
    }

    private void validateCopyAuthority(String intygsTyp) {
        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                .features(WebcertFeature.KOPIERA_INTYG)
                .privilege(AuthoritiesConstants.PRIVILEGE_KOPIERA_INTYG)
                .orThrow();
    }
}
