package se.inera.webcert.web.controller.integration;

import static se.inera.certificate.common.enumerations.CertificateTypes.FK7263;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.webcert.common.security.authority.UserRole;
import se.inera.webcert.persistence.roles.repository.RoleRepository;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.webcert.security.AuthoritiesException;
import se.inera.webcert.service.intyg.IntygService;
import se.inera.webcert.service.user.WebCertUserService;
import se.inera.webcert.service.user.dto.WebCertUser;

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

    private static final String[] GRANTED_ROLES = new String[] { UserRole.ROLE_LAKARE_DJUPINTEGRERAD.name(), UserRole.ROLE_VARDADMINISTRATOR_DJUPINTEGRERAD.name() };

    private String urlBaseTemplate;

    private String urlIntygFragmentTemplate;
    private String urlUtkastFragmentTemplate;


    @Autowired
    private IntygService intygService;

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private WebCertUserService webCertUserService;

    @Autowired
    private RoleRepository roleRepository;

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
    public Response redirectToIntyg(@Context UriInfo uriInfo, @PathParam("intygId") String intygId, @DefaultValue("") @QueryParam("alternatePatientSSn") String alternatePatientSSn, @DefaultValue("") @QueryParam("responsibleHospName") String responsibleHospName) {
        return redirectToIntyg(uriInfo, intygId, FK7263.toString(), alternatePatientSSn, responsibleHospName);
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
    public Response redirectToIntyg(@Context UriInfo uriInfo, @PathParam("intygId") String intygId, @PathParam("typ") String typ, @DefaultValue("") @QueryParam("alternatePatientSSn") String alternatePatientSSn, @DefaultValue("") @QueryParam("responsibleHospName") String responsibleHospName) {
        if (StringUtils.isBlank(intygId)) {
            LOG.error("Path parameter 'intygId' was either whitespace, empty (\"\") or null");
            return Response.serverError().build();
        }

        WebCertUser user = webCertUserService.getUser();

        try {
            // Ensure user has valid role
            assertUserRole(user);
        } catch (AuthoritiesException e) {
            LOG.error(e.getMessage());
            return Response.serverError().build();
        }

        // Enable user features
        webCertUserService.enableFeaturesOnUser();

        Boolean isUtkast = false;
        Utkast utkast = utkastRepository.findOne(intygId);

        if (utkast != null && !utkast.getStatus().equals(UtkastStatus.SIGNED)) {
            isUtkast = true;
        }
        
        LOG.debug("Redirecting to view intyg {} of type {}", intygId, typ);
        return buildRedirectResponse(uriInfo, typ, intygId, alternatePatientSSn, responsibleHospName, isUtkast);
    }

    /*
     * Gör inget om användare redan har rollen:
     *  - ROLE_LAKARE_DJUPINTEGRERAD eller
     *  - ROLE_VARDADMINISTRATOR_DJUPINTEGRERAD
     *
     * Om användare har rollen:
     * - ROLE_LAKARE eller
     * - ROLE_VARDADMINISTRATOR
     *
     * så ändra/nedgradera rollen till
     *  - ROLE_LAKARE_DJUPINTEGRERAD eller
     *  - ROLE_VARDADMINISTRATOR_DJUPINTEGRERAD
     *
     * För alla andra roller, eller ingen roll,
     * släng ett exception.
     */
    void assertUserRole(WebCertUser user) {

        Map<String, UserRole> userRoles = user.getRoles();

        List<String> gr = Arrays.asList(new String[] { UserRole.ROLE_LAKARE.name(), UserRole.ROLE_VARDADMINISTRATOR.name() });
        for (String role : userRoles.keySet()) {
            if (gr.contains(role)) {
                updateUserRoles(user);
                return;
            }
        }

        // Assert user has a valid role for this request
        webCertUserService.assertUserRoles(GRANTED_ROLES);
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


    // - - - - - Private scope - - - - -

    private Response buildRedirectResponse(UriInfo uriInfo, String certificateType, String certificateId, String alternatePatientSSn,
            String responsibleHospName, Boolean isUtkast) {

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();

        Map<String, Object> urlParams = new HashMap<String, Object>();
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

        URI location = uriBuilder.replacePath(urlBaseTemplate).fragment(urlFragmentTemplate).buildFromMap(urlParams);

        return Response.status(Status.TEMPORARY_REDIRECT).location(location).build();
    }

    private void updateUserRoles(WebCertUser user) {
        boolean isDoctor = user.isLakare();
        String userRole = UserRole.ROLE_VARDADMINISTRATOR_DJUPINTEGRERAD.name();

        if (isDoctor) {
            userRole = UserRole.ROLE_LAKARE_DJUPINTEGRERAD.name();
        }

        LOG.debug("Updating user role to be {}", userRole);
        webCertUserService.updateUserRoles(new String[] { userRole });
    }

}
