package se.inera.intyg.webcert.web.security;

import static se.inera.intyg.webcert.web.auth.common.AuthConstants.SPRING_SECURITY_SAVED_REQUEST_KEY;

import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Magnus Ekstrand on 25/11/15.
 */
public class RequestOrigin {

    // ~ Static fields/initializers
    // =====================================================================================

    public static final String REGEXP_REQUESTURI_DJUPINTEGRATION = "/visa/intyg/.+";
    public static final String REGEXP_REQUESTURI_UTHOPP = "/webcert/web/user/certificate/.+/questions";

    // These static fields map the authorities configuation's known request origins
    public static final String REQUEST_ORIGIN_TYPE_NORMAL = "NORMAL";
    public static final String REQUEST_ORIGIN_TYPE_DJUPINTEGRATION = "DJUPINTEGRATION";
    public static final String REQUEST_ORIGIN_TYPE_UTHOPP = "UTHOPP";

    private HttpServletRequest httpServletRequest;

    public RequestOrigin(HttpServletRequest request) {
        Assert.notNull(request, "Request required");
        this.httpServletRequest = request;
    }


    // ~ API
    // =====================================================================================

    public String resolveOrigin() {

        DefaultSavedRequest savedRequest = getSavedRequest(httpServletRequest);
        if (savedRequest == null) {
            return REQUEST_ORIGIN_TYPE_NORMAL;
        }

        String uri = savedRequest.getRequestURI();

        if (uri.matches(REGEXP_REQUESTURI_DJUPINTEGRATION)) {
            return REQUEST_ORIGIN_TYPE_DJUPINTEGRATION;
        } else if (uri.matches(REGEXP_REQUESTURI_UTHOPP)) {
            return REQUEST_ORIGIN_TYPE_UTHOPP;
        }

        return REQUEST_ORIGIN_TYPE_NORMAL;
    }


    // ~ Private
    // =====================================================================================

    private DefaultSavedRequest getSavedRequest(HttpServletRequest request) {
        DefaultSavedRequest savedRequest = (DefaultSavedRequest) request.getSession().getAttribute(SPRING_SECURITY_SAVED_REQUEST_KEY);
        return savedRequest;
    }
}
