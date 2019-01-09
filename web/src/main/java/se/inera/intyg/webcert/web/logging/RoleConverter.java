/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.logging;

import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

public class RoleConverter extends ClassicConverter {

    private static final String NO_ROLE = "NO ROLE";

    @Override
    public String convert(ILoggingEvent event) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            return NO_ROLE;
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof WebCertUser) {
            WebCertUser user = (WebCertUser) auth.getPrincipal();
            Set<String> keys = user.getRoles().keySet();
            if (keys.size() == 1) {
                return keys.toArray(new String[0])[0];
            }
        }

        return NO_ROLE;
    }

}
