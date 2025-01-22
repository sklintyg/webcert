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
package se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions;

import se.inera.intyg.common.luae_na.support.LuaenaEntryPoint;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;


public class ResourceLinkFactory {

    private ResourceLinkFactory() {
        throw new IllegalStateException("Utility class");
    }

    public static ResourceLinkDTO read() {
        return ResourceLinkDTO.create(
            ResourceLinkTypeDTO.READ_CERTIFICATE,
            "Öppna",
            "",
            true
        );
    }

    public static ResourceLinkDTO create(boolean enabled) {
        return ResourceLinkDTO.create(
            ResourceLinkTypeDTO.CREATE_CERTIFICATE,
            "Skapa intyg",
            enabled ? "Skapa ett intygsutkast." : "",
            enabled
        );
    }

    public static ResourceLinkDTO confirmLuaena(boolean enabled) {
        return ResourceLinkDTO.create(
            ResourceLinkTypeDTO.CREATE_LUAENA_CONFIRMATION,
            "Visa bekräftelsemodal för " + LuaenaEntryPoint.MODULE_NAME,
            "Visa modal med ett bekräftelsemeddelande.",
            enabled
        );
    }
}
