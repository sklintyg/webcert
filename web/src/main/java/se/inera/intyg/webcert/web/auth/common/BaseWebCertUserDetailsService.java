package se.inera.intyg.webcert.web.auth.common;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import se.inera.intyg.webcert.web.auth.authorities.AuthoritiesResolver;
import se.inera.intyg.webcert.web.service.feature.WebcertFeatureService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * Base UserDetailsService for both Siths and E-leg based authentication.
 *
 * Created by eriklupander on 2015-06-16.
 */
public abstract class BaseWebCertUserDetailsService {

    protected static final String COMMA = ", ";
    protected static final String SPACE = " ";

    private AuthoritiesResolver authoritiesResolver;

    private WebcertFeatureService webcertFeatureService;


    // - - - - - Public scope - - - - -

    public AuthoritiesResolver getAuthoritiesResolver() {
        return authoritiesResolver;
    }

    @Autowired
    public void setAuthoritiesResolver(AuthoritiesResolver authoritiesResolver) {
        this.authoritiesResolver = authoritiesResolver;
    }

    @Autowired
    public void setWebcertFeatureService(WebcertFeatureService webcertFeatureService) {
        this.webcertFeatureService = webcertFeatureService;
    }


    // - - - - - Protected scope - - - - -

    protected String compileName(String fornamn, String mellanOchEfterNamn) {

        StringBuilder sb = new StringBuilder();

        if (StringUtils.isNotBlank(fornamn)) {
            sb.append(fornamn);
        }

        if (StringUtils.isNotBlank(mellanOchEfterNamn)) {
            if (sb.length() > 0) {
                sb.append(SPACE);
            }
            sb.append(mellanOchEfterNamn);
        }

        return sb.toString();
    }

    protected void decorateWebCertUserWithAvailableFeatures(WebCertUser webcertUser) {
        Set<String> availableFeatures = webcertFeatureService.getActiveFeatures();
        webcertUser.setAktivaFunktioner(availableFeatures);
    }

    protected HttpServletRequest getCurrentRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

}
