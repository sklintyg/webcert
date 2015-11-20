package se.inera.intyg.webcert.web.web.controller.integration;

import static se.inera.certificate.common.enumerations.CertificateTypes.FK7263;

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.webcert.common.security.authority.UserRole;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.webcert.persistence.utkast.repository.UtkastRepository;

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
@Api(value = "intyg (Djupintegration)", description = "REST API för Djupintegration", produces = MediaType.APPLICATION_JSON)
public class IntygIntegrationController extends BaseIntegrationController {

    private static final String PARAM_CERT_TYPE = "certType";
    private static final String PARAM_CERT_ID = "certId";
    public static final String PARAM_HOSP_NAME = "hospName";
    public static final String PARAM_PATIENT_SSN = "patientId";

    private static final Logger LOG = LoggerFactory.getLogger(IntygIntegrationController.class);

    private static final String[] GRANTED_ROLES = new String[] { UserRole.ROLE_LAKARE_DJUPINTEGRERAD.name(), UserRole.ROLE_TANDLAKARE_DJUPINTEGRERAD.name(), UserRole.ROLE_VARDADMINISTRATOR_DJUPINTEGRERAD.name() };

    private String urlIntygFragmentTemplate;
    private String urlUtkastFragmentTemplate;

    @Autowired
    private UtkastRepository utkastRepository;

    @Override
    protected String[] getGrantedRoles() {
        return GRANTED_ROLES;
    }

    /**
     * Fetches an FK certificate from IT or webcert and then performs a redirect to the view that displays
     * the certificate.
     *
     * @param intygId
     *            The id of the certificate to view.
     */
    @GET
    @Path("/{intygId}")
    public Response redirectToIntyg(@Context UriInfo uriInfo, @PathParam("intygId") String intygId, @DefaultValue("") @QueryParam("alternatePatientSSn") String alternatePatientSSn, @DefaultValue("") @QueryParam("responsibleHospName") String responsibleHospName) {
        return redirectToIntyg(uriInfo, intygId, FK7263.toString(), alternatePatientSSn, responsibleHospName);
    }

    /**
     * Fetches a certificate from IT or webcert and then performs a redirect to the view that displays
     * the certificate. Can be used for all types of certificates.
     *
     * @param intygId
     *            The id of the certificate to view.
     * @param typ The type of certificate
     */
    @GET
    @Path("/{typ}/{intygId}")
    public Response redirectToIntyg(@Context UriInfo uriInfo, @PathParam("intygId") String intygId, @PathParam("typ") String typ, @DefaultValue("") @QueryParam("alternatePatientSSn") String alternatePatientSSn, @DefaultValue("") @QueryParam("responsibleHospName") String responsibleHospName) {

        boolean ok = super.validateRedirectToIntyg(intygId);
        if (!ok) {
            return Response.serverError().build();
        }

        getWebCertUserService().enableFeaturesOnUser();

        Boolean isUtkast = false;
        Utkast utkast = utkastRepository.findOne(intygId);

        if (utkast != null && !utkast.getStatus().equals(UtkastStatus.SIGNED)) {
            isUtkast = true;
        }

        LOG.debug("Redirecting to view intyg {} of type {}", intygId, typ);
        return buildRedirectResponse(uriInfo, typ, intygId, alternatePatientSSn, responsibleHospName, isUtkast);
    }

    public void setUrlIntygFragmentTemplate(String urlFragmentTemplate) {
        this.urlIntygFragmentTemplate = urlFragmentTemplate;
    }

    public void setUrlUtkastFragmentTemplate(String urlFragmentTemplate) {
        this.urlUtkastFragmentTemplate = urlFragmentTemplate;
    }

    // - - - - - Private scope - - - - -

    private Response buildRedirectResponse(UriInfo uriInfo, String certificateType, String certificateId, String alternatePatientSSn,
            String responsibleHospName, Boolean isUtkast) {

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

        URI location = uriBuilder.replacePath(getUrlBaseTemplate()).fragment(urlFragmentTemplate).buildFromMap(urlParams);

        return Response.status(Status.TEMPORARY_REDIRECT).location(location).build();
    }

}
