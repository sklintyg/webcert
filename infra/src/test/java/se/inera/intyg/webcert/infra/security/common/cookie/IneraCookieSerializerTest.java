/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.infra.security.common.cookie;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.session.web.http.CookieSerializer;

@ExtendWith(MockitoExtension.class)
class IneraCookieSerializerTest {

  private final HttpServletRequest req = mock(HttpServletRequest.class);
  private final HttpServletResponse resp = mock(HttpServletResponse.class);

  static Stream<Arguments> userAgents() {
    return Stream.of(
        Arguments.of(
            "Mozilla/5.0 doogiePIM/1.0.4.2 AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.84 Safari/537.36",
            false),
        Arguments.of(
            "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.82 Safari/537.36",
            false),
        Arguments.of(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2883.87 Safari/537.36",
            false),
        Arguments.of("Mozilla/5.0 Chrome/54.0.2840.99 Safari/537.36", false),
        Arguments.of(
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_2) AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Chrome/55.0.2883.95 Safari/537.36",
            false),
        Arguments.of(
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.76 Safari/537.36",
            false),
        Arguments.of(
            "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36",
            false),
        Arguments.of(
            "Mozilla/5.0 (Linux; Android 8.0.0) AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Version/4.0 Klar/1.0 Chrome/58.0.3029.121 Mobile Safari/537.36",
            false),
        Arguments.of(
            "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36",
            false),
        Arguments.of(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.78 Safari/537.36",
            false),
        Arguments.of(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.79 Safari/537.36",
            false),
        Arguments.of(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3165.0 Safari/537.36",
            false),
        Arguments.of(
            "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3213.3 Safari/537.36",
            false),
        Arguments.of(
            "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.119 Safari/537.36",
            false),
        Arguments.of(
            "Mozilla/5.0 (Win) AppleWebKit/1000.0 (KHTML, like Gecko) Chrome/65.663 Safari/1000.01",
            false),
        Arguments.of(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3334.0 Safari/537.36",
            false),
        Arguments.of(
            "Mozilla/5.0 (Linux; Android 4.4.4; One Build/KTU84L.H4) AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Version/4.0 Chrome/66.0.0.0 Mobile Safari/537.36 [FB_IAB/FB4A;FBAV/28.0.0.20.16;]",
            false),
        Arguments.of(
            "UCWEB/2.0 (MIDP-2.0; U; Adr 4.0.4; en-US; ZTE_U795) U2/1.0.0 UCBrowser/10.7.6.805 U2/1.0.0 Mobile",
            false),
        Arguments.of(
            "Mozilla/5.0 (Linux; U; Android 7.1.1; en-US; Lenovo K8 Note Build/NMB26.54-74) AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Version/4.0 Chrome/57.0.2987.108 UCBrowser/12.0.0.1088 Mobile Safari/537.36",
            false),
        Arguments.of(
            "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X; zh-CN) AppleWebKit/537.51.1 (KHTML, like Gecko) "
                + "Mobile/15A5304i UCBrowser/11.5.7.986 Mobile AliApp(TUnionSDK/0.1.15)",
            false),
        Arguments.of(
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_1) AppleWebKit/605.1.15 (KHTML, like Gecko) "
                + "Version/12.0 Safari/605.1.15",
            false),
        Arguments.of(
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_2) AppleWebKit/537.36 (KHTML, like Gecko)",
            false),
        Arguments.of(
            "Mozilla/5.0 (iPhone; CPU iPhone OS 12_0 like Mac OS X) AppleWebKit/ 604.1.21 (KHTML, like Gecko) "
                + "Version/ 12.0 Mobile/17A6278a Safari/602.1.26",
            false),
        Arguments.of(
            "Mozilla/5.0 (iPhone; CPU iPhone OS 12_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) "
                + "CriOS/70.0.3538.75 Mobile/15E148 Safari/605.1",
            false),
        Arguments.of(
            "Mozilla/5.0 (iPad; CPU OS 12_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) "
                + "FxiOS/13.2b11866 Mobile/16A366 Safari/605.1.15",
            false),
        Arguments.of(
            "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.2661.102 Safari/537.36",
            true),
        Arguments.of(
            "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.2526.73 Safari/537.36",
            true),
        Arguments.of(
            "Mozilla/5.0 (iPad; CPU OS 11_0 like Mac OS X) AppleWebKit/603.1.30 (KHTML, like Gecko) "
                + "CriOS/60.0.3112.72 Mobile/15A5327g Safari/602.1",
            true),
        Arguments.of(
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.29 Safari/537.36",
            true),
        Arguments.of(
            "Mozilla/5.0 (Linux; U; Android 8.0.0; en-US; Pixel XL Build/OPR3.170623.007) AppleWebKit/534.30 (KHTML, like Gecko) "
                + "Version/4.0 UCBrowser/12.13.2.1005 U3/0.8.0 Mobile Safari/534.30",
            true),
        Arguments.of(
            "Mozilla/5.0 (Linux; U; Android 8.0.0; en-US; Pixel XL Build/OPR3.170623.007) AppleWebKit/534.30 (KHTML, like Gecko) "
                + "Version/4.0 UCBrowser/12.13.4.1005 U3/0.8.0 Mobile Safari/534.30",
            true),
        Arguments.of(
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_1) AppleWebKit/605.1.15 (KHTML, like Gecko) "
                + "Version/13.0.3 Safari/605.1.15",
            true),
        Arguments.of(
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15) AppleWebKit/601.1.39 (KHTML, like Gecko) Version/10.1.2 Safari/601.1.39",
            true),
        Arguments.of(
            "Mozilla/5.0 (iPhone; CPU iPhone OS 13_0 like Mac OS X) AppleWebKit/602.1.38 (KHTML, like Gecko) "
                + "Version/66.6 Mobile/14A5297c Safari/602.1",
            true));
  }

  @ParameterizedTest
  @MethodSource("userAgents")
  void writeCookieValue_samesite_exclusion(String userAgent, boolean isSameSiteNone) {

    IneraCookieSerializer testee = new IneraCookieSerializer(true);

    CookieSerializer.CookieValue cookieValue = new CookieSerializer.CookieValue(req, resp, "");
    when(req.isSecure()).thenReturn(true);
    when(req.getHeader(HttpHeaders.USER_AGENT)).thenReturn(userAgent);

    testee.writeCookieValue(cookieValue);

    ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
    verify(resp).addHeader(anyString(), stringCaptor.capture());

    assertEquals(
        isSameSiteNone,
        stringCaptor.getValue().contains("SameSite=none"),
        "Erroneous samesite attribut for: " + userAgent);
  }

  @ParameterizedTest
  @MethodSource("userAgents")
  void writeCookieValue_samesite_no_exclusion(String userAgent, boolean isSameSiteNone) {

    IneraCookieSerializer testee = new IneraCookieSerializer();

    CookieSerializer.CookieValue cookieValue = new CookieSerializer.CookieValue(req, resp, "");
    when(req.isSecure()).thenReturn(true);
    when(req.getHeader(HttpHeaders.USER_AGENT)).thenReturn(userAgent);

    testee.writeCookieValue(cookieValue);

    ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
    verify(resp).addHeader(anyString(), stringCaptor.capture());

    assertEquals(
        true,
        stringCaptor.getValue().contains("SameSite=none"),
        "Erroneous samesite attribut for: " + userAgent);
  }

  @ParameterizedTest
  @MethodSource("userAgents")
  void writeCookieValue_samesite_no_isSecure(String userAgent, boolean isSameSiteNone) {

    IneraCookieSerializer testee = new IneraCookieSerializer();

    CookieSerializer.CookieValue cookieValue = new CookieSerializer.CookieValue(req, resp, "");
    when(req.isSecure()).thenReturn(false);
    when(req.getHeader(HttpHeaders.USER_AGENT)).thenReturn(userAgent);

    testee.writeCookieValue(cookieValue);

    ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
    verify(resp).addHeader(anyString(), stringCaptor.capture());

    assertEquals(
        false,
        stringCaptor.getValue().contains("SameSite=none"),
        "Erroneous samesite attribut for: " + userAgent);
  }
}
