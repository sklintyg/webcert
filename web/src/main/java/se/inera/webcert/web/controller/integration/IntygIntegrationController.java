package se.inera.webcert.web.controller.integration;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;
import se.inera.webcert.persistence.intyg.repository.IntygRepository;
import se.inera.webcert.service.draft.IntygDraftService;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.feature.WebcertFeature;
import se.inera.webcert.service.intyg.IntygService;
import se.inera.webcert.service.intyg.dto.IntygContentHolder;
import se.inera.webcert.web.service.WebCertUserService;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller to enable an external user to access certificates directly from a
 * link in an external patient care system.
 *
 * @author bensam
 */
@Path("/intyg")
public class IntygIntegrationController {

    private static final String PARAM_CERT_TYPE = "certType";
    private static final String PARAM_CERT_ID = "certId";
    public static final String PARAM_HOSP_NAME = "hospName";
    public static final String PARAM_PATIENT_SSN = "patientId";

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
     * Fetches a certificate from IT or webcert and then performs a redirect to the view that displays
     * the certificate. Can be used for all types of certificates.
     *
     * @param uriInfo
     * @param intygId
     *            The id of the certificate to view.
     * @return
     */
    @GET
    @Path("/{intygId}")
    public Response redirectToIntyg(@Context UriInfo uriInfo, @PathParam("intygId") String intygId, @DefaultValue("") @QueryParam("alternatePatientSSN") String alternatePatientSSN, @DefaultValue("") @QueryParam("responsibleHospName") String responsibleHospName) {

        Boolean draft = true;
        String intygType;

        if(StringUtils.isBlank(intygId)) {
            return Response.serverError().build();
        }

        Intyg draftData = intygRepository.findOne(intygId);
        if (draftData != null && !draftData.getStatus().equals(IntygsStatus.SIGNED)) {
            intygType = draftData.getIntygsTyp();
        } else {
            draft = false;
            IntygContentHolder intygData = intygService.fetchExternalIntygData(intygId); // Throws exceptions in case of errors
            intygType = intygData.getMetaData().getType();
        }

        LOG.debug("Redirecting to view intyg {} of type {}", intygId, intygType);

        webCertUserService.enableFeaturesOnUser(WebcertFeature.FRAN_JOURNALSYSTEM);

        return buildRedirectResponse(uriInfo, intygType, intygId, alternatePatientSSN, responsibleHospName, draft);
    }

    private Response buildRedirectResponse(UriInfo uriInfo, String certificateType, String certificateId, String alternatePatientSSN, String responsibleHospName, Boolean draft) {

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();

        Map<String, Object> urlParams = new HashMap<String, Object>();
        urlParams.put(PARAM_CERT_TYPE, certificateType);
        urlParams.put(PARAM_CERT_ID, certificateId);
        urlParams.put(PARAM_PATIENT_SSN, alternatePatientSSN);

        String urlFragmentTemplate;
        if(draft) {
            urlParams.put(PARAM_HOSP_NAME, responsibleHospName);
            urlFragmentTemplate = this.urlUtkastFragmentTemplate;
        } else {
            urlFragmentTemplate = this.urlIntygFragmentTemplate;
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
