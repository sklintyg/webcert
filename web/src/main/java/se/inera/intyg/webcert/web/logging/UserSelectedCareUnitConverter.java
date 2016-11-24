/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import se.inera.intyg.infra.integration.hsa.model.SelectableVardenhet;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Logback converter that returns information about the current user.
 * User info is retrieved from the Spring Security context. If no context
 * is available a NO USER is returned.
 *
 * @author nikpet
 */
public class UserSelectedCareUnitConverter extends ClassicConverter {

    private static final String NO_UNIT_SELECTED = "NO UNIT";

    @Override
    public String convert(ILoggingEvent event) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            return NO_UNIT_SELECTED;
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof WebCertUser) {
            WebCertUser user = (WebCertUser) auth.getPrincipal();
            SelectableVardenhet valdVardenhet = user.getValdVardenhet();
            if (valdVardenhet != null) {
                return valdVardenhet.getId();
            }
        }

        return NO_UNIT_SELECTED;
    }

}
