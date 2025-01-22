/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.csintegration.aggregate.PrintCertificateAggregator;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygServiceResult;
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
import se.inera.intyg.webcert.web.web.util.resourcelinks.ResourceLinkHelper;

/**
 * Controller exposing services to be used by modules.
 *
 * @author nikpet
 */
@Path("/intyg")
@Api(value = "/moduleapi/intyg", produces = MediaType.APPLICATION_JSON)
public class IntygModuleApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(IntygModuleApiController.class);

    private static final String CONTENT_DISPOSITION = "Content-Disposition";
    private static final String MISSING_VITAL_ARGUMENTS_IN_PAYLOAD = "Missing vital arguments in payload";

    @Autowired
    private ArendeService arendeService;

    @Autowired
    private IntygService intygService;

    @Autowired
    private CopyUtkastService copyUtkastService;

    @Autowired
    private CopyUtkastServiceHelper copyUtkastServiceHelper;

    @Autowired
    private IntygTextsService intygTextsService;

    @Autowired
    private ResourceLinkHelper resourceLinkHelper;

    @Autowired
    private PrintCertificateAggregator printCertificateAggregator;

    /**
     * Retrieves a signed intyg from intygstjänst.
     *
     * @param intygsId intygid
     * @return Response
     */
    @GET
    @Path("/{intygsTyp}/{intygsId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PerformanceLogging(eventAction = "intyg-module-get-certificate", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response getIntyg(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId) {
        LOG.debug("Fetching signed intyg with id '{}' from IT", intygsId);

        IntygContentHolder intygAsExternal = intygService.fetchIntygDataWithRelations(intygsId, intygsTyp);

        resourceLinkHelper.decorateIntygWithValidActionLinks(intygAsExternal);

        return Response.ok().entity(intygAsExternal).build();
    }

    /**
     * Return the signed certificate identified by the given id as PDF.
     *
     * @param intygsTyp the type of certificate
     * @param intygsId - the globally unique id of a certificate.
     * @return The certificate in PDF format
     */
    @GET
    @Path("/{intygsTyp}/{intygsId}/pdf")
    @Produces("application/pdf")
    @PerformanceLogging(eventAction = "intyg-module-get-certificate-as-pdf", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public final Response getIntygAsPdf(@PathParam("intygsTyp") String intygsTyp, @PathParam(value = "intygsId") final String intygsId,
        @Context HttpServletRequest request) {
        return getPdf(intygsTyp, intygsId, false, request);
    }

    /**
     * Return the signed certificate identified by the given id as PDF suited for the employer of the patient.
     *
     * @param intygsTyp the type of certificate
     * @param intygsId - the globally unique id of a certificate.
     * @return The certificate in PDF format
     */
    @GET
    @Path("/{intygsTyp}/{intygsId}/pdf/arbetsgivarutskrift")
    @Produces("application/pdf")
    @PerformanceLogging(eventAction = "intyg-module-get-certificate-as-pdf-for-employer", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public final Response getIntygAsPdfForEmployer(@PathParam("intygsTyp") String intygsTyp,
        @PathParam(value = "intygsId") final String intygsId, @Context HttpServletRequest request) {
        return getPdf(intygsTyp, intygsId, true, request);
    }

    private Response getPdf(String intygsTyp, final String intygsId, boolean isEmployerCopy, HttpServletRequest request) {
        if (!isEmployerCopy) {
            LOG.debug("Fetching signed intyg '{}' as PDF", intygsId);
        } else {
            LOG.debug("Fetching signed intyg '{}' as PDF for employer", intygsId);
        }

        final var response = printCertificateAggregator.get(intygsId, intygsTyp, isEmployerCopy);

        final var userAgent = request.getHeader("User-Agent");
        final var contentDisposition = userAgent.matches(".*Trident/\\d+.*|.*MSIE \\d+.*")
            ? buildPdfHeader(response.getFilename()) : "inline";

        return Response.ok(response.getPdfData()).header(CONTENT_DISPOSITION, contentDisposition).build();
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
    @PerformanceLogging(eventAction = "intyg-module-send-signed-certificate", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public Response sendSignedIntyg(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
        SendSignedIntygParameter param) {

        IntygServiceResult sendResult = intygService.sendIntyg(intygsId, intygsTyp, param.getRecipient(), false);
        return Response.ok(sendResult).build();
    }

    /**
     * Issues a request to Intygstjanst to revoke the signed intyg.
     *
     * @param intygsId The id of the intyg to revoke
     * @param param A JSON struct containing an optional message
     */
    @POST
    @Path("/{intygsTyp}/{intygsId}/aterkalla")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PerformanceLogging(eventAction = "intyg-module-revoke-signed-certificate", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public Response revokeSignedIntyg(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
        RevokeSignedIntygParameter param) {

        if (authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
            .features(AuthoritiesConstants.FEATURE_MAKULERA_INTYG_KRAVER_ANLEDNING).isVerified() && !param.isValid()) {
            LOG.warn("Request to revoke '{}' is not valid", intygsId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, MISSING_VITAL_ARGUMENTS_IN_PAYLOAD);
        }

        IntygServiceResult result = intygService.revokeIntyg(intygsId, intygsTyp, param.getMessage(), param.getReason());
        return Response.ok(result).build();
    }

    /**
     * Create a copy that completes an existing certificate.
     */
    @POST
    @Path("/{intygsTyp}/{intygsId}/komplettera")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PerformanceLogging(eventAction = "intyg-module-create-complement", eventType = MdcLogConstants.EVENT_TYPE_CREATION)
    public Response createCompletion(CopyIntygRequest request, @PathParam("intygsTyp") String intygsTyp,
        @PathParam("intygsId") String orgIntygsId) {

        LOG.debug("Attempting to create a completion of {} with id '{}'", intygsTyp, orgIntygsId);

        if (!request.isValid()) {
            LOG.error("Request to create completion of '{}' is not valid", orgIntygsId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, MISSING_VITAL_ARGUMENTS_IN_PAYLOAD);
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
     */
    @POST
    @Path("/{intygsTyp}/{intygsId}/fornya")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PerformanceLogging(eventAction = "intyg-module-create-renewal", eventType = MdcLogConstants.EVENT_TYPE_CREATION)
    public Response createRenewal(CopyIntygRequest request, @PathParam("intygsTyp") String intygsTyp,
        @PathParam("intygsId") String orgIntygsId) {

        LOG.debug("Attempting to create a renewal of {} with id '{}'", intygsTyp, orgIntygsId);

        if (!request.isValid()) {
            LOG.error("Request to create renewal of '{}' is not valid", orgIntygsId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, MISSING_VITAL_ARGUMENTS_IN_PAYLOAD);
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
     * Create a new utkast from a signed template.
     * <p>
     * Usually (but not necessarily) the template is of a different intygType than the new utkast.
     */
    @POST
    @Path("/{intygsTyp}/{intygsId}/{newIntygsTyp}/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PerformanceLogging(eventAction = "intyg-module-create-draft-from-signed-template", eventType = MdcLogConstants.EVENT_TYPE_CREATION)
    public Response createUtkastFromSignedTemplate(CopyIntygRequest request, @PathParam("intygsTyp") String orgIntygsTyp,
        @PathParam("intygsId") String orgIntygsId, @PathParam("newIntygsTyp") String newIntygsTyp) {

        LOG.debug("Attempting to create a new certificate with type {} from certificate with type {} and id '{}'", newIntygsTyp,
            orgIntygsTyp, orgIntygsId);

        if (!request.isValid()) {
            LOG.error("Request to create utkast from certificate '{}' as template is not valid", orgIntygsId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, MISSING_VITAL_ARGUMENTS_IN_PAYLOAD);
        }
        CreateUtkastFromTemplateRequest serviceRequest = copyUtkastServiceHelper.createUtkastFromDifferentIntygTypeRequest(orgIntygsId,
            newIntygsTyp, orgIntygsTyp, request);

        serviceRequest.setTypVersion(intygTextsService.getLatestVersion(newIntygsTyp));

        CreateUtkastFromTemplateResponse serviceResponse = copyUtkastService.createUtkastFromSignedTemplate(serviceRequest);

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
    @PerformanceLogging(eventAction = "intyg-module-create-replacement", eventType = MdcLogConstants.EVENT_TYPE_CREATION)
    public Response createReplacement(CopyIntygRequest request, @PathParam("intygsTyp") String intygsTyp,
        @PathParam("intygsId") String orgIntygsId) {

        LOG.debug("Attempting to create a replacement of {} with id '{}'", intygsTyp, orgIntygsId);

        if (!request.isValid()) {
            LOG.error("Request to create replacement of '{}' is not valid", orgIntygsId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, MISSING_VITAL_ARGUMENTS_IN_PAYLOAD);
        }

        CreateReplacementCopyRequest serviceRequest = copyUtkastServiceHelper.createReplacementCopyRequest(orgIntygsId, intygsTyp, request);
        CreateReplacementCopyResponse serviceResponse = copyUtkastService.createReplacementCopy(serviceRequest);

        LOG.debug("Created a new replacement draft with id: '{}' and type: {}, replacing certificate with id '{}'.",
            serviceResponse.getNewDraftIntygId(), serviceResponse.getNewDraftIntygType(), orgIntygsId);

        CopyIntygResponse response = new CopyIntygResponse(serviceResponse.getNewDraftIntygId(), serviceResponse.getNewDraftIntygType(),
            serviceResponse.getNewDraftIntygTypeVersion());

        return Response.ok().entity(response).build();
    }
}
