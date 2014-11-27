package se.inera.webcert.web.controller.integration;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.certificate.modules.fk7263.model.Constants;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;
import se.inera.webcert.persistence.intyg.repository.IntygRepository;
import se.inera.webcert.service.feature.WebcertFeature;
import se.inera.webcert.service.intyg.IntygService;
import se.inera.webcert.web.service.WebCertUserService;

/**
 * Controller to enable an external user to access certificates directly from a
 * link in an external patient care system.
 *
 * @author nikpet
 */
@Path("/")
public class IntygIntegrationController {

    private static final String PARAM_CERT_TYPE = "certType";
    private static final String PARAM_CERT_ID = "certId";

    private static final Logger LOG = LoggerFactory.getLogger(IntygIntegrationController.class);

    private String urlBaseTemplate;

    private String urlIntygFragmentTemplate;
    private String urlUtkastFragmentTemplate;

    @Autowired
    private IntygService intygService;

    @Autowired
    private IntygRepository intygRepository;

    @Autowired
    private WebCertUserService webCertUserService;

    /**
     * Fetches an FK certificate from IT or webcert and then performs a redirect to the view that displays
     * the certificate.
     *
     * @param uriInfo
     * @param intygId
     *            The id of the certificate to view.
     * @return
     */
    @GET
    @Path("/{intygId}")
    public Response redirectToIntyg(@Context UriInfo uriInfo, @PathParam("intygId") String intygId) {
        return redirectToIntyg(uriInfo, intygId, Constants.FK7263);
    }

    /**
     * Fetches a certificate from IT or webcert and then performs a redirect to the view that displays
     * the certificate. Can be used for all types of certificates.
     *
     * @param uriInfo
     * @param intygId
     *            The id of the certificate to view.
     * @param typ The type of certificate
     * @return
     */
    @GET
    @Path("/{typ}/{intygId}")
    public Response redirectToIntyg(@Context UriInfo uriInfo, @PathParam("intygId") String intygId, @PathParam("typ") String typ) {

        Boolean draft = false;

        if(StringUtils.isBlank(intygId)) {
            return Response.serverError().build();
        }

        Intyg draftData = intygRepository.findOne(intygId);
        if (draftData != null && !draftData.getStatus().equals(IntygsStatus.SIGNED)) {
            draft = true;
        }
        
        LOG.debug("Redirecting to view intyg {} of type {}", intygId, typ);

        webCertUserService.enableFeaturesOnUser(WebcertFeature.FRAN_JOURNALSYSTEM);

        return buildRedirectResponse(uriInfo, typ, intygId, draft);
    }

    private Response buildRedirectResponse(UriInfo uriInfo, String certificateType, String certificateId, Boolean draft) {

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();

        Map<String, Object> urlParams = new HashMap<String, Object>();
        urlParams.put(PARAM_CERT_TYPE, certificateType);
        urlParams.put(PARAM_CERT_ID, certificateId);

        String urlFragmentTemplate = this.urlIntygFragmentTemplate;
        if(draft) {
            urlFragmentTemplate = this.urlUtkastFragmentTemplate;
        }
        URI location = uriBuilder.replacePath(urlBaseTemplate).fragment(urlFragmentTemplate).buildFromMap(urlParams);

        return Response.status(Status.TEMPORARY_REDIRECT).location(location).build();
    }

    public void setUrlBaseTemplate(String urlBaseTemplate) {
        this.urlBaseTemplate = urlBaseTemplate;
    }

    public void setUrlIntygFragmentTemplate(String urlFragmentTemplate) {
        this.urlIntygFragmentTemplate = urlFragmentTemplate;
    }

    public void setUrlUtkastFragmentTemplate(String urlFragmentTemplate) {
        this.urlUtkastFragmentTemplate = urlFragmentTemplate;
    }
}
