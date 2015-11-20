package se.inera.intyg.webcert.web.web.controller.moduleapi;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygPdf;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygServiceResult;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
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
    private IntygService intygService;

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
    public Response getIntyg(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId) {

        LOG.debug("Fetching signed intyg with id '{}' from IT", intygsId);

        IntygContentHolder intygAsExternal = intygService.fetchIntygData(intygsId, intygsTyp);

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
    public Response sendSignedIntyg(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId, SendSignedIntygParameter param) {
        abortIfWebcertFeatureIsNotAvailableForModule(WebcertFeature.SKICKA_INTYG, intygsTyp);
        IntygServiceResult sendResult = intygService.sendIntyg(intygsId, intygsTyp, param.getRecipient(), param.isPatientConsent());
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
        abortIfWebcertFeatureIsNotAvailableForModule(WebcertFeature.MAKULERA_INTYG, intygsTyp);
        String revokeMessage = (param != null) ? param.getRevokeMessage() : null;
        IntygServiceResult revokeResult = intygService.revokeIntyg(intygsId, intygsTyp, revokeMessage);
        return Response.ok(revokeResult).build();
    }

}
