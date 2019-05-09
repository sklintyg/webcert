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

import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygModuleDTO;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.DraftHolder;

public interface ResourceLinkHelper {

    void decorateWithValidActionLinks(List<IntygModuleDTO> intygModules, Personnummer personnummer);

    void decorateWithValidActionLinks(IntygModuleDTO intygModule, Personnummer personnummer);

    void decorateWithValidActionLinks(DraftHolder utkast, String intygsTyp, Vardenhet vardenhet, Personnummer personnummer);

    void decorateWithValidActionLinks(IntygContentHolder intygAsExternal);

    void decorateIntygWithValidActionLinks(List<ListIntygEntry> allIntyg, Personnummer personNummer);

    void decorateWithValidActionLinks(ListIntygEntry intygEntry, Personnummer personNummer);

    void decorateWithValidActionLinks(List<ArendeListItem> results, Vardenhet vardenhet);
}
