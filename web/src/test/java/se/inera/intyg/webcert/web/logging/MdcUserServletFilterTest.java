package se.inera.intyg.webcert.web.logging;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.integration.hsatk.model.legacy.SelectableVardenhet;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@ExtendWith(MockitoExtension.class)
class MdcUserServletFilterTest {


  @InjectMocks
  private MdcUserServletFilter filter;

  @Mock
  private WebCertUserService webCertUserService;

  @Mock
  private ServletRequest request;

  @Mock
  private ServletResponse response;

  @Mock
  private FilterChain filterChain;

  public static final SelectableVardenhet selectedVardgivare = mock(SelectableVardenhet.class);
  public static final SelectableVardenhet selectedVardenhet = mock(SelectableVardenhet.class);

  @Test
  void shouldGetUserDataIfAuthContextExists() throws IOException, ServletException {
    when(webCertUserService.hasAuthenticationContext()).thenReturn(Boolean.TRUE);
    when(webCertUserService.getUser()).thenReturn(new WebCertUser());

    filter.doFilter(request, response, filterChain);

    verify(webCertUserService).getUser();
    verify(webCertUserService).hasAuthenticationContext();
  }

  @Test
  void shouldNotGetUserDataIfAuthContextNull() throws IOException, ServletException {
    when(webCertUserService.hasAuthenticationContext()).thenReturn(Boolean.FALSE);

    filter.doFilter(request, response, filterChain);

    verify(webCertUserService).hasAuthenticationContext();
    verify(webCertUserService, never()).getUser();
  }

  @Test
  void shouldNotThrowWhenUserDataIsMissing() {
    when(webCertUserService.hasAuthenticationContext()).thenReturn(Boolean.TRUE);
    when(webCertUserService.getUser()).thenReturn(new WebCertUser());

    assertDoesNotThrow(() -> filter.doFilter(request, response, filterChain));
  }

  @Test
  void shouldNotThrowWhenIdsAreMissing() {
    var user = new WebCertUser();
    user.setHsaId(null);
    user.setValdVardenhet(selectedVardenhet);
    user.setValdVardgivare(selectedVardgivare);
    when(webCertUserService.hasAuthenticationContext()).thenReturn(Boolean.TRUE);
    when(webCertUserService.getUser()).thenReturn(user);

    assertDoesNotThrow(() -> filter.doFilter(request, response, filterChain));
  }

}