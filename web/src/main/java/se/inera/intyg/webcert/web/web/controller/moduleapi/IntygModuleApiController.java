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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.annotations.Api;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygPdf;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygServiceResult;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.CopyUtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateCompletionCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateCompletionCopyResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateRenewalCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateRenewalCopyResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateReplacementCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateReplacementCopyResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateUtkastFromTemplateRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateUtkastFromTemplateResponse;
import se.inera.intyg.webcert.web.service.utkast.util.CopyUtkastServiceHelper;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygResponse;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.RevokeSignedIntygParameter;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.SendSignedIntygParameter;

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
    private ArendeService arendeService;

    @Autowired
    private IntygService intygService;

    @Autowired
    private CopyUtkastService copyUtkastService;

    @Autowired
    private CopyUtkastServiceHelper copyUtkastServiceHelper;

    /**
     * Retrieves a signed intyg from intygstjänst.
     *
     * @param intygsId intygid
     * @return Response
     */
    @GET
    @Path("/{intygsTyp}/{intygsId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response getIntyg(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId) {

        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                .privilege(AuthoritiesConstants.PRIVILEGE_VISA_INTYG)
                .orThrow();

        WebCertUser user = getWebCertUserService().getUser();
        boolean coherentJournaling = user.getParameters() != null && user.getParameters().isSjf();

        LOG.debug("Fetching signed intyg with id '{}' from IT, coherent journaling {}", intygsId, coherentJournaling);

        IntygContentHolder intygAsExternal = intygService.fetchIntygDataWithRelations(intygsId, intygsTyp, coherentJournaling);
        return Response.ok().entity(intygAsExternal).build();
    }

    /**
     * Return the signed certificate identified by the given id as PDF.
     *
     * @param intygsTyp the type of certificate
     * @param intygsId  - the globally unique id of a certificate.
     * @return The certificate in PDF format
     */
    @GET
    @Path("/{intygsTyp}/{intygsId}/pdf")
    @Produces("application/pdf")
    public final Response getIntygAsPdf(@PathParam("intygsTyp") String intygsTyp, @PathParam(value = "intygsId") final String intygsId) {
        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                .privilege(AuthoritiesConstants.PRIVILEGE_VISA_INTYG)
                .features(AuthoritiesConstants.FEATURE_UTSKRIFT)
                .orThrow();

        return getPdf(intygsTyp, intygsId, false);
    }

    /**
     * Return the signed certificate identified by the given id as PDF suited for the employer of the patient.
     *
     * @param intygsTyp the type of certificate
     * @param intygsId  - the globally unique id of a certificate.
     * @return The certificate in PDF format
     */
    @GET
    @Path("/{intygsTyp}/{intygsId}/pdf/arbetsgivarutskrift")
    @Produces("application/pdf")
    public final Response getIntygAsPdfForEmployer(@PathParam("intygsTyp") String intygsTyp,
            @PathParam(value = "intygsId") final String intygsId) {
        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                .privilege(AuthoritiesConstants.PRIVILEGE_VISA_INTYG)
                .features(AuthoritiesConstants.FEATURE_ARBETSGIVARUTSKRIFT)
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

        return Response.ok(intygPdfResponse.getPdfData()).header(CONTENT_DISPOSITION, buildPdfHeader(intygPdfResponse.getFilename()))
                .build();
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
        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp).features(AuthoritiesConstants.FEATURE_SKICKA_INTYG)
                .orThrow();

        IntygServiceResult sendResult = intygService.sendIntyg(intygsId, intygsTyp, param.getRecipient(), false);
        return Response.ok(sendResult).build();
    }

    /**
     * Issues a request to Intygstjanst to revoke the signed intyg.
     *
     * @param intygsId The id of the intyg to revoke
     * @param param    A JSON struct containing an optional message
     */
    @POST
    @Path("/{intygsTyp}/{intygsId}/aterkalla")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response revokeSignedIntyg(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
            RevokeSignedIntygParameter param) {
        validateRevokeAuthority(intygsTyp);

        if (authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                .features(AuthoritiesConstants.FEATURE_MAKULERA_INTYG_KRAVER_ANLEDNING).isVerified() && !param.isValid()) {
            LOG.warn("Request to revoke '{}' is not valid", intygsId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Missing vital arguments in payload");
        }

        IntygServiceResult result = revokeIntyg(intygsTyp, intygsId, param);
        return Response.ok(result).build();
    }

    /**
     * Create a copy that completes an existing certificate.
     */
    @POST
    @Path("/{intygsTyp}/{intygsId}/komplettera")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response createCompletion(CopyIntygRequest request, @PathParam("intygsTyp") String intygsTyp,
            @PathParam("intygsId") String orgIntygsId) {

        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                .features(AuthoritiesConstants.FEATURE_FORNYA_INTYG)
                .privilege(AuthoritiesConstants.PRIVILEGE_SVARA_MED_NYTT_INTYG)
                .orThrow();

        LOG.debug("Attempting to create a completion of {} with id '{}'", intygsTyp, orgIntygsId);

        if (!request.isValid()) {
            LOG.error("Request to create completion of '{}' is not valid", orgIntygsId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Missing vital arguments in payload");
        }

        String meddelandeId = arendeService.getLatestMeddelandeIdForCurrentCareUnit(orgIntygsId);

        CreateCompletionCopyRequest serviceRequest = copyUtkastServiceHelper.createCompletionCopyRequest(orgIntygsId, intygsTyp,
                meddelandeId, request);
        CreateCompletionCopyResponse serviceResponse = copyUtkastService.createCompletion(serviceRequest);

        LOG.debug("Created a new draft with id: '{}' and type: {}, completing certificate with id '{}'.",
                serviceResponse.getNewDraftIntygId(),
                serviceResponse.getNewDraftIntygType(), orgIntygsId);

        CopyIntygResponse response = new CopyIntygResponse(serviceResponse.getNewDraftIntygId(), serviceResponse.getNewDraftIntygType(),
                serviceResponse.getNewDraftIntygTypeVersion());

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
    public Response createRenewal(CopyIntygRequest request, @PathParam("intygsTyp") String intygsTyp,
            @PathParam("intygsId") String orgIntygsId) {

        validateCopyAuthority(intygsTyp);

        LOG.debug("Attempting to create a renewal of {} with id '{}'", intygsTyp, orgIntygsId);

        WebCertUser user = getWebCertUserService().getUser();

        boolean copyOkParam = user.getParameters() == null || user.getParameters().isCopyOk();
        if (!copyOkParam) {
            LOG.info("User is not allowed to request a copy for id '{}' due to false kopieraOK-parameter", orgIntygsId);
            final String message = "Authorization failed due to false kopieraOK-parameter";
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM, message);
        }

        if (!request.isValid()) {
            LOG.error("Request to create renewal of '{}' is not valid", orgIntygsId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Missing vital arguments in payload");
        }
        CreateRenewalCopyRequest serviceRequest = copyUtkastServiceHelper.createRenewalCopyRequest(orgIntygsId, intygsTyp, request);
        CreateRenewalCopyResponse serviceResponse = copyUtkastService.createRenewalCopy(serviceRequest);

        LOG.debug("Created a new draft with id: '{}' and type: {}, renewing certificate with id '{}'.",
                serviceResponse.getNewDraftIntygId(), serviceResponse.getNewDraftIntygType(), orgIntygsId);

        CopyIntygResponse response = new CopyIntygResponse(serviceResponse.getNewDraftIntygId(), serviceResponse.getNewDraftIntygType(),
                serviceResponse.getNewDraftIntygTypeVersion());

        return Response.ok().entity(response).build();
    }

    /**
     * Create a new utkast from a template.
     * <p>
     * Usually (but not necessarily) the template is of a different intygType than the new utkast.
     */
    @POST
    @Path("/{intygsTyp}/{intygsId}/{newIntygsTyp}/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response createUtkastFromTemplate(CopyIntygRequest request, @PathParam("intygsTyp") String orgIntygsTyp,
            @PathParam("intygsId") String orgIntygsId, @PathParam("newIntygsTyp") String newIntygsTyp) {

        validateCreateUtkastFromTemplateAuthority(newIntygsTyp);

        LOG.debug("Attempting to create a new certificate with type {} from certificate with type {} and id '{}'", newIntygsTyp,
                orgIntygsTyp, orgIntygsId);

        WebCertUser user = getWebCertUserService().getUser();

        boolean copyOkParam = user.getParameters() == null || user.getParameters().isCopyOk();
        if (!copyOkParam) {
            LOG.info("User is not allowed to request a copy for id '{}' due to false kopieraOK-parameter", orgIntygsId);
            final String message = "Authorization failed due to false kopieraOK-parameter";
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM, message);
        }

        if (!request.isValid()) {
            LOG.error("Request to create utkast from certificate '{}' as template is not valid", orgIntygsId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Missing vital arguments in payload");
        }
        CreateUtkastFromTemplateRequest serviceRequest = copyUtkastServiceHelper.createUtkastFromDifferentIntygTypeRequest(orgIntygsId,
                newIntygsTyp, orgIntygsTyp, request);
        CreateUtkastFromTemplateResponse serviceResponse = copyUtkastService.createUtkastFromTemplate(serviceRequest);

        LOG.debug("Created a new draft with id: '{}' and type: {} from certificate with type: {} and id '{}'.",
                serviceResponse.getNewDraftIntygId(), serviceResponse.getNewDraftIntygType(), orgIntygsTyp, orgIntygsId);

        CopyIntygResponse response = new CopyIntygResponse(serviceResponse.getNewDraftIntygId(), serviceResponse.getNewDraftIntygType(),
                serviceResponse.getNewDraftIntygTypeVersion());

        return Response.ok().entity(response).build();
    }

    /**
     * Create a copy that is a replacement (ersättning) of an existing certificate.
     */
    @POST
    @Path("/{intygsTyp}/{intygsId}/ersatt")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response createReplacement(CopyIntygRequest request, @PathParam("intygsTyp") String intygsTyp,
            @PathParam("intygsId") String orgIntygsId) {
        validateReplaceAuthority(intygsTyp);

        LOG.debug("Attempting to create a replacement of {} with id '{}'", intygsTyp, orgIntygsId);

        if (!request.isValid()) {
            LOG.error("Request to create replacement of '{}' is not valid", orgIntygsId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Missing vital arguments in payload");
        }

        CreateReplacementCopyRequest serviceRequest = copyUtkastServiceHelper.createReplacementCopyRequest(orgIntygsId, intygsTyp, request);
        CreateReplacementCopyResponse serviceResponse = copyUtkastService.createReplacementCopy(serviceRequest);

        LOG.debug("Created a new replacement draft with id: '{}' and type: {}, replacing certificate with id '{}'.",
                serviceResponse.getNewDraftIntygId(), serviceResponse.getNewDraftIntygType(), orgIntygsId);

        CopyIntygResponse response = new CopyIntygResponse(serviceResponse.getNewDraftIntygId(), serviceResponse.getNewDraftIntygType(),
                serviceResponse.getNewDraftIntygTypeVersion());

        return Response.ok().entity(response).build();
    }

    private IntygServiceResult revokeIntyg(String intygsTyp, String intygsId, RevokeSignedIntygParameter param) {
        return intygService.revokeIntyg(intygsId, intygsTyp, param.getMessage(), param.getReason());
    }

    private void validateRevokeAuthority(String intygsTyp) {
        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                .features(AuthoritiesConstants.FEATURE_MAKULERA_INTYG)
                .privilege(AuthoritiesConstants.PRIVILEGE_MAKULERA_INTYG)
                .orThrow();
    }

    private void validateReplaceAuthority(String intygsTyp) {
        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                .privilege(AuthoritiesConstants.PRIVILEGE_ERSATTA_INTYG)
                .orThrow();
    }

    private void validateCopyAuthority(String intygsTyp) {
        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                .features(AuthoritiesConstants.FEATURE_FORNYA_INTYG)
                .privilege(AuthoritiesConstants.PRIVILEGE_FORNYA_INTYG)
                .orThrow();
    }

    private void validateCreateUtkastFromTemplateAuthority(String newIntygTyp) {
        authoritiesValidator.given(getWebCertUserService().getUser(), newIntygTyp)
                .features(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST)
                .privilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG)
                .orThrow();
    }
}
