/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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

import jakarta.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.HttpHeaders;
import org.springframework.session.web.http.DefaultCookieSerializer;

public class IneraCookieSerializer extends DefaultCookieSerializer {

  private final boolean useSameSiteNoneExclusion;
  private final Pattern ucBrowserPattern = Pattern.compile("UCBrowser/(\\d+)\\.(\\d+)\\.(\\d+)\\.");

  public IneraCookieSerializer() {
    this(false);
  }

  public IneraCookieSerializer(boolean useSameSiteNoneExclusion) {
    super();
    this.useSameSiteNoneExclusion = useSameSiteNoneExclusion;
  }

  @Override
  public void writeCookieValue(CookieValue cookieValue) {

    HttpServletRequest request = cookieValue.getRequest();

    if (useSameSiteNoneExclusion
        && shouldntGetSameSiteNone(request.getHeader(HttpHeaders.USER_AGENT))) {
      setSameSite(null);
    } else if (request.isSecure()) {
      setSameSite("none");
    }

    super.writeCookieValue(cookieValue);
  }

  /**
   * Som older browser/OS doesn't handle samesite=none. These must be excluded when this attribute
   * is set for session cookies.
   *
   * <ul>
   *   <li>Based on https://catchjs.com/Blog/SameSiteCookies
   *   <li>Information on incompatible browsers:
   *       https://www.chromium.org/updates/same-site/incompatible-clients
   * </ul>
   *
   * @param userAgent User Agent
   * @return true if samesite=none should not be set.
   */
  private boolean shouldntGetSameSiteNone(String userAgent) {
    return userAgent.contains("iPhone OS 12_")
        || userAgent.contains("iPad; CPU OS 12_") // iOS 12
        || (userAgent.contains("UCBrowser/")
            ? isOlderUcBrowser(userAgent) // UC Browser < 12.13.2
            : (userAgent.contains("Chrome/5") || userAgent.contains("Chrome/6"))) // Chrome
        || userAgent.contains("Chromium/5")
        || userAgent.contains("Chromium/6") // Chromium
        || (userAgent.contains(" OS X 10_14_")
            && ((userAgent.contains("Version/")
                    && userAgent.contains("Safari")) // Safari on MacOS 10.14
                || userAgent.endsWith("(KHTML, like Gecko)"))); // Embedded browser on MacOS 10.14
  }

  private boolean isOlderUcBrowser(String userAgent) {
    // CHECKSTYLE:OFF MagicNumber
    Matcher uaMatcher = ucBrowserPattern.matcher(userAgent);

    if (!uaMatcher.find()) {
      return false;
    }

    int major = Integer.parseInt(uaMatcher.group(1));
    int minor = Integer.parseInt(uaMatcher.group(2));
    int build = Integer.parseInt(uaMatcher.group(3));

    if (major != 12) {
      return major < 12;
    }

    if (minor != 13) {
      return minor < 13;
    }
    return build < 2;
    // CHECKSTYLE:ON MagicNumber
  }
}
