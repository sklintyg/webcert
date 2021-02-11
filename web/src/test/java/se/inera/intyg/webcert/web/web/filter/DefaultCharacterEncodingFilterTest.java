package se.inera.intyg.webcert.web.web.filter;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.stream.Collectors;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * These tests check if {@link DefaultCharacterEncodingFilter} correctly sets encoding to requests.
 * When no encoding is set the filter should set request character encoding to UTF-8.
 */

@RunWith(MockitoJUnitRunner.class)
public class DefaultCharacterEncodingFilterTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private DefaultCharacterEncodingFilter testee = new DefaultCharacterEncodingFilter();

    @Test
    public void testNoEncodingSet() throws ServletException, IOException {
        testee.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

        verify(httpServletRequest, times(1)).setCharacterEncoding("UTF-8");
    }

    @Test
    public void testEncodingSet() throws IOException, ServletException {
        when(httpServletRequest.getCharacterEncoding()).thenReturn("UTF-8");

        testee.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

        verify(httpServletRequest, times(0)).setCharacterEncoding("UTF-8");
    }

    @Test
    public void testUtfStringDoesNotGetCorrupt() throws IOException, ServletException {
        String json = "{\"responsibleHospName\":åäö}";
        when(httpServletRequest.getReader()).thenReturn(
            new BufferedReader(new StringReader(json)));

        testee.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);
        String body = httpServletRequest.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

        Assert.assertEquals(json, body);
    }
}
