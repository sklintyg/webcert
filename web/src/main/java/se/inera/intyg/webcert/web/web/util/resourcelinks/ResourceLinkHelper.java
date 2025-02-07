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
package se.inera.intyg.webcert.web.web.util.resourcelinks;

import java.util.List;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygModuleDTO;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.DraftHolder;

/**
 * Helper that decorates different resources with valid actions based on access rights. Uses access services for
 * Drafts, Locked drafts and Certificates to evaluate what actions are available.
 */
public interface ResourceLinkHelper {

    /**
     * Add available actions links for a list of IntygModuleDTO.
     *
     * @param intygModuleDTOList List of dtos to decorate.
     * @param patient Which patient access rights should be validated for.
     */
    void decorateIntygModuleWithValidActionLinks(List<IntygModuleDTO> intygModuleDTOList, Personnummer patient);

    /**
     * Add available action links for a IntygModuleDTO.
     *
     * @param intygModuleDTO DTO to decorate.
     * @param patient Which patient access rights should be validated for.
     */
    void decorateIntygModuleWithValidActionLinks(IntygModuleDTO intygModuleDTO, Personnummer patient);

    /**
     * Add available action links for DraftHolder.
     *
     * @param draftHolder DraftHolder to decorate.
     * @param certificateType Certificate type to consider.
     * @param certificateTypeVersion Certificate type version to consider.
     * @param careUnit Care Unit to consider.
     * @param patient Patient to consider.
     */
    void decorateUtkastWithValidActionLinks(DraftHolder draftHolder, String certificateType, String certificateTypeVersion,
        Vardenhet careUnit, Personnummer patient);

    /**
     * Add available action links for IntygContentHolder.
     *
     * @param intygContentHolder Holder to decorate
     */
    void decorateIntygWithValidActionLinks(IntygContentHolder intygContentHolder);

    /**
     * Add available action links to a list of ListIntygEntry.
     *
     * @param listIntygEntryList List of entries to decorate
     * @param patient Patient to consider.
     */
    void decorateIntygWithValidActionLinks(List<ListIntygEntry> listIntygEntryList, Personnummer patient);

    /**
     * Add available action links to a ListIntygEntry.
     *
     * @param listIntygEntry Entry to decorate.
     * @param patient Patient to consider.
     */
    void decorateIntygWithValidActionLinks(ListIntygEntry listIntygEntry, Personnummer patient);

    /**
     * Add available action links to a list of ArendeListItem.
     *
     * @param arendeListItems List of items to decorate.
     * @param careUnit Care Unit to consider.
     */
    void decorateArendeWithValidActionLinks(List<ArendeListItem> arendeListItems, Vardenhet careUnit);
}
