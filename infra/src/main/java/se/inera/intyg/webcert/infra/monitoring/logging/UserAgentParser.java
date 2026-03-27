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
package se.inera.intyg.webcert.infra.monitoring.logging;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ua_parser.Client;
import ua_parser.OS;
import ua_parser.Parser;
import ua_parser.UserAgent;

@Component
public class UserAgentParser {

  protected static final String NO_USER_AGENT_STRING = "NO-USER-AGENT-STRING";
  protected static final String UNKNOWN_OS = "UNKNOWN-OS";
  // Constructing the parser is relatively expensive.
  // Since 1.4.2 it's threadsafe, so we initialize it just once.
  private Parser uaParser;

  @PostConstruct
  public void init() throws IOException {
    uaParser = new Parser();
  }

  public UserAgentInfo parse(String userAgentString) {

    Client c = uaParser.parse(userAgentString);
    UserAgent userAgent = c.userAgent;
    OS os = c.os;

    // Make sure we have values for userAgent and os
    if (userAgent == null) {
      userAgent = new UserAgent(NO_USER_AGENT_STRING, null, null, null);
    }
    if (os == null) {
      os = new OS(UNKNOWN_OS, null, null, null, null);
    }

    return new UserAgentInfo(
        userAgent.family,
        String.join(".", getVersionOrZero(userAgent.major), getVersionOrZero(userAgent.minor)),
        getOSFamily(os),
        String.join(".", getVersionOrZero(os.major), getVersionOrZero(os.minor)));
  }

  private String getOSFamily(OS os) {
    if (os != null && !StringUtils.isEmpty(os.family)) {
      return os.family;
    }
    return UNKNOWN_OS;
  }

  private String getVersionOrZero(String version) {
    return StringUtils.isEmpty(version) ? "0" : version;
  }
}
