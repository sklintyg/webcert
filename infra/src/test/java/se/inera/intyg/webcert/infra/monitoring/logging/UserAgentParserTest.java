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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.inera.intyg.webcert.infra.monitoring.logging.UserAgentParser.NO_USER_AGENT_STRING;
import static se.inera.intyg.webcert.infra.monitoring.logging.UserAgentParser.UNKNOWN_OS;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = UserAgentParser.class)
class UserAgentParserTest {

  @Autowired private UserAgentParser parser = new UserAgentParser();

  @Test
  void testParserNullInput() {
    final UserAgentInfo userAgentInfo = parser.parse(null);
    assertEquals(NO_USER_AGENT_STRING, userAgentInfo.getBrowserName());
    assertEquals("0.0", userAgentInfo.getBrowserVersion());
    assertEquals(UNKNOWN_OS, userAgentInfo.getOsFamily());
  }

  @Test
  void testParserEmptyInput() {
    final UserAgentInfo userAgentInfo = parser.parse("");
    assertEquals("Other", userAgentInfo.getBrowserName());
    assertEquals("0.0", userAgentInfo.getBrowserVersion());
    assertEquals("Other", userAgentInfo.getOsFamily());
  }

  @Test
  void testParserUnknownInput() {
    final UserAgentInfo userAgentInfo = parser.parse("this is not a known browser");
    assertEquals("Other", userAgentInfo.getBrowserName());
    assertEquals("0.0", userAgentInfo.getBrowserVersion());
    assertEquals("Other", userAgentInfo.getOsFamily());
  }

  @Test
  void testParserWin7IE11() {
    // User agent string taken from Virtualbox running IE11 on win7 image
    final UserAgentInfo userAgentInfo =
        parser.parse("Mozilla/5.0 (Windows NT 6.1; Trident/7.0; rv:11.0) like Gecko");
    assertEquals("IE", userAgentInfo.getBrowserName());
    assertEquals("11.0", userAgentInfo.getBrowserVersion());
    assertEquals("Windows", userAgentInfo.getOsFamily());
    assertEquals("7.0", userAgentInfo.getOsVersion());
  }

  @Test
  void testParserWin10Edge() {
    // User agent string taken from Virtualbox running Edge on win10 image
    final UserAgentInfo userAgentInfo =
        parser.parse(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36 Edge/16.16299");
    assertEquals("Edge", userAgentInfo.getBrowserName());
    assertEquals("16.16299", userAgentInfo.getBrowserVersion());
    assertEquals("Windows", userAgentInfo.getOsFamily());
    assertEquals("10.0", userAgentInfo.getOsVersion());
  }

  @Test
  void testParserChrome() {
    // User agent string taken from my local MBP laptop with latest chrome installed
    final UserAgentInfo userAgentInfo =
        parser.parse(
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36");
    assertEquals("Chrome", userAgentInfo.getBrowserName());
    assertEquals("76.0", userAgentInfo.getBrowserVersion());
    assertEquals("Mac OS X", userAgentInfo.getOsFamily());
  }
}
