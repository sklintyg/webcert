package se.inera.intyg.webcert.web.security;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Created by Magnus Ekstrand on 03/12/15.
 */
public class WebCertUserOriginTest {

    @Test
    public void testDjupintegrationRegexp() throws Exception {
        assertTrue("/visa/intyg/99aaa4f1-d862-4750-a628-f7dcb9c8bac0".matches(WebCertUserOrigin.REGEXP_REQUESTURI_DJUPINTEGRATION));
        assertTrue("/visa/intyg/99aaa4f1-d862-4750-a628-f7dcb9c8bac0/".matches(WebCertUserOrigin.REGEXP_REQUESTURI_DJUPINTEGRATION));
    }

    @Test
    public void testUthoppRegexp() throws Exception {
        assertTrue("/webcert/web/user/certificate/99aaa4f1-d862-4750-a628-f7dcb9c8bac0/questions".matches(WebCertUserOrigin.REGEXP_REQUESTURI_UTHOPP));
    }

}


