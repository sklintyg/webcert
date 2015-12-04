package se.inera.intyg.webcert.web.web.controller.legacyintegration;

import static se.inera.intyg.webcert.web.security.RequestOrigin.REQUEST_ORIGIN_TYPE_NORMAL;

import io.swagger.annotations.Api;

import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 * Controller to enable a landsting user to access certificates directly from a link.
 *
 * This controller inherits from the LegacyIntygIntegrationController which manages
 * request for UTHOPP links. In this scenario we change the request origin to be
 * NORMAL instead of UTHOPP. This inheritance is somewhat confusing but it make
 * sense if we look at it from a functional perspective.
 */
@Path("/basic-certificate")
@Api(value = "/webcert/web/user/basic-certificate", description = "REST API för fråga/svar via normal link, landstingspersonal", produces = MediaType.APPLICATION_JSON)
public class CertificateIntegrationController extends LegacyIntygIntegrationController {

    protected String getGrantedRequestOrigin() {
        return REQUEST_ORIGIN_TYPE_NORMAL;
    }

}
