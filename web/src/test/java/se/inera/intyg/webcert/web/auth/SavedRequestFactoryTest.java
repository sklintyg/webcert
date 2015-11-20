package se.inera.intyg.webcert.web.auth;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.web.savedrequest.SavedRequest;

/**
 * Tests that the {@link SavedRequestFactoryImpl} produces correct SavedRequest instances.
 *
 * (The test actually tests the {@link org.springframework.security.web.savedrequest.DefaultSavedRequest} constructor
 * more than anything else)
 *
 * Created by eriklupander on 2015-10-14.
 */
@RunWith(MockitoJUnitRunner.class)
public class SavedRequestFactoryTest {

    public static final int PORT = 8888;
    public static final String SCHEME = "http";
    public static final String SERVER = "myserver.com";
    public static final String MY_PATH_SEGMENTS = "/my/path/segments";

    private SavedRequestFactoryImpl testee = new SavedRequestFactoryImpl();

    @Mock
    private HttpServletRequest req;

    @Test
    public void testCanProduceSavedRequest() {
        StringBuffer sb = new StringBuffer();
        sb.append(MY_PATH_SEGMENTS);

        // Just mock the bare minimum to produce a valid SavedRequest without having exceptions thrown...
        when(req.getHeaderNames()).thenReturn(new Vector().elements());
        when(req.getLocales()).thenReturn(new Vector().elements());
        when(req.getScheme()).thenReturn(SCHEME);
        when(req.getServerName()).thenReturn(SERVER);
        when(req.getServerPort()).thenReturn(PORT);

        when(req.getRequestURL()).thenReturn(sb);
        when(req.getRequestURI()).thenReturn(sb.toString());

        SavedRequest savedRequest = testee.buildSavedRequest(req);
        assertEquals(req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + req.getRequestURI(), savedRequest.getRedirectUrl());
    }

}
