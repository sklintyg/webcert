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
package se.inera.intyg.webcert.infra.monitoring.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import java.util.Objects;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import se.inera.intyg.infra.security.common.model.IntygUser;

public class UserConverter extends ClassicConverter {

  @Override
  public String convert(ILoggingEvent iLoggingEvent) {
    return userInfo();
  }

  String userInfo() {
    final IntygUser u = intygUser();
    if (Objects.isNull(u)) {
      return "noUser";
    }

    return u.getHsaId()
        + ","
        + (Objects.nonNull(u.getValdVardenhet()) ? u.getValdVardenhet().getId() : "noUnit")
        + ","
        + (Objects.nonNull(u.getOrigin()) ? u.getOrigin() : "noOrigin")
        + ","
        + ((u.getRoles().size() == 1) ? u.getRoles().keySet().iterator().next() : "noRole")
        + ","
        + (Objects.nonNull(u.getValdVardgivare()) ? u.getValdVardgivare().getId() : "noVg");
  }

  //
  IntygUser intygUser() {
    final Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (Objects.isNull(auth)) {
      return null;
    }

    final Object principal = auth.getPrincipal();

    return (principal instanceof IntygUser) ? (IntygUser) principal : null;
  }
}
