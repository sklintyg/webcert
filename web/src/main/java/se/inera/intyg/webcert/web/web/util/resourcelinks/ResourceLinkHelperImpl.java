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

package se.inera.intyg.webcert.web.web.util.resourcelinks;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.access.AccessService;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygModuleDTO;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLink;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLinkType;

@Component
public class ResourceLinkHelperImpl implements ResourceLinkHelper {

    @Autowired
    private AccessService accessService;

    @Override
    public void decorateWithValidActionLinks(List<IntygModuleDTO> intygModules, Personnummer personnummer) {
        for (IntygModuleDTO intygModule : intygModules) {
            decorateWithValidActionLinks(intygModule, personnummer);
        }
    }

    @Override
    public void decorateWithValidActionLinks(IntygModuleDTO intygModule, Personnummer personnummer) {
        if (accessService.allowedToCreateUtkast(intygModule.getId(), personnummer)) {
            final ActionLink actionLink = new ActionLink();
            actionLink.setType(ActionLinkType.SKAPA_UTKAST);
            actionLink.setUrl("testurl");
            intygModule.addLink(actionLink);
        }
    }
}
