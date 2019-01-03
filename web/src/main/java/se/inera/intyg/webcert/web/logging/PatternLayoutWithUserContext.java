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

import ch.qos.logback.classic.PatternLayout;

/**
 * Logback {@link PatternLayout} PatternLayout implementation that exposes
 * user and session information.
 *
 * @author nikpet
 */
public class PatternLayoutWithUserContext extends PatternLayout {
    static {
        PatternLayout.defaultConverterMap.put("user", UserConverter.class.getName());
        PatternLayout.defaultConverterMap.put("session", SessionConverter.class.getName());
        PatternLayout.defaultConverterMap.put("selectedCareUnit", UserSelectedCareUnitConverter.class.getName());
        PatternLayout.defaultConverterMap.put("origin", OriginConverter.class.getName());
        PatternLayout.defaultConverterMap.put("role", RoleConverter.class.getName());
    }
}
