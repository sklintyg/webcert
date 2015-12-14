package se.inera.intyg.webcert.web.security;

import static se.inera.intyg.webcert.web.auth.common.AuthConstants.SPRING_SECURITY_SAVED_REQUEST_KEY;

import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Magnus Ekstrand on 25/11/15.
 */
public class WebCertUserOrigin {

    // ~ Static fields/initializers
    // =====================================================================================

    public static final String REGEXP_REQUESTURI_DJUPINTEGRATION = "/visa/intyg/.+";
    public static final String REGEXP_REQUESTURI_UTHOPP = "/webcert/web/user/certificate/.+/questions";


    public WebCertUserOrigin() {
    }


    // ~ API
    // =====================================================================================

    public String resolveOrigin(HttpServletRequest request) {
        Assert.notNull(request, "Request required");

        DefaultSavedRequest savedRequest = getSavedRequest(request);
        if (savedRequest == null) {
            return WebCertUserOriginType.NORMAL.name();
        }

        String uri = savedRequest.getRequestURI();

        if (uri.matches(REGEXP_REQUESTURI_DJUPINTEGRATION)) {
            return WebCertUserOriginType.DJUPINTEGRATION.name();
        } else if (uri.matches(REGEXP_REQUESTURI_UTHOPP)) {
            return WebCertUserOriginType.UTHOPP.name();
        }

        return WebCertUserOriginType.NORMAL.name();
    }


    // ~ Private
    // =====================================================================================

    private DefaultSavedRequest getSavedRequest(HttpServletRequest request) {
        DefaultSavedRequest savedRequest = (DefaultSavedRequest) request.getSession().getAttribute(SPRING_SECURITY_SAVED_REQUEST_KEY);
        return savedRequest;
    }
}
