package se.inera.intyg.webcert.web.auth.eleg;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created by eriklupander on 2015-09-30.
 */
@RunWith(MockitoJUnitRunner.class)
public class ElegRedirectFilterTest {

    private ElegRedirectFilter testee = new ElegRedirectFilter();

    @Test
    public void testRedirectsIfPropertySet() throws ServletException, IOException {
        testee.setElegIdpUrl("test");
        HttpServletResponse resp = mock(HttpServletResponse.class);
        testee.doFilterInternal(mock(HttpServletRequest.class), resp, mock(FilterChain.class));
        verify(resp, times(1)).sendRedirect(anyString());
    }

    @Test(expected = IllegalStateException.class)
    public void testNoIdpUrlConfiguredThrowsException() throws ServletException, IOException {
        testee.doFilterInternal(mock(HttpServletRequest.class), mock(HttpServletResponse.class), mock(FilterChain.class));
    }

}
