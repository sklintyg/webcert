package se.inera.intyg.webcert.web.auth;

import org.springframework.security.web.savedrequest.SavedRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * Defines contract for producing SavedRequest instances.
 *
 * Created by eriklupander on 2015-10-14.
 */
public interface SavedRequestFactory {
    SavedRequest buildSavedRequest(HttpServletRequest req);
}
