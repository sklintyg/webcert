package se.inera.intyg.webcert.web.auth;

import org.springframework.security.web.PortResolverImpl;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * Produces DefaultSavedRequest instances.
 *
 * Created by eriklupander on 2015-10-14.
 */
@Service
public class SavedRequestFactoryImpl implements SavedRequestFactory {
    @Override
    public SavedRequest buildSavedRequest(HttpServletRequest req) {
        return new DefaultSavedRequest(req, new PortResolverImpl());
    }
}
