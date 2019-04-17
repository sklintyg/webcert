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

package se.inera.intyg.webcert.web.web.util.resourcelink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import se.inera.intyg.common.support.modules.registry.IntygModule;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.access.DraftAccessService;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygModuleDTO;
import se.inera.intyg.webcert.web.web.util.resourcelinks.ResourceLinkHelperImpl;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLink;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLinkType;

@RunWith(MockitoJUnitRunner.class)
public class ResourceLinkHelperImplTest {

    @Mock
    private DraftAccessService draftAccessService;

    @InjectMocks
    private ResourceLinkHelperImpl resourceLinkHelper;

    @Test
    public void validActionsForIntygModuleWithAccessAllowed() {
        final String intygsTyp = "intygstyp";
        final Personnummer personnummer = Personnummer.createPersonnummer("191212121212").get();

        doReturn(true).when(draftAccessService).allowToCreateDraft(intygsTyp, personnummer);

        final ActionLink expectedActionLink = new ActionLink();
        expectedActionLink.setType(ActionLinkType.SKAPA_UTKAST);
        expectedActionLink.setUrl("testurl");

        final IntygModuleDTO intygModuleDTO = createIntygModuleDTO(intygsTyp);
        resourceLinkHelper.decorateWithValidActionLinks(intygModuleDTO, personnummer);

        final List<ActionLink> actualLinks = intygModuleDTO.getLinks();

        assertNotNull(actualLinks);
        assertEquals("Should be one link", 1, actualLinks.size());
        assertEquals("ActionLink type should be same", expectedActionLink.getType(), actualLinks.get(0).getType());
        assertEquals("ActionLink url should be same", expectedActionLink.getUrl(), actualLinks.get(0).getUrl());
    }

    private IntygModuleDTO createIntygModuleDTO(String intygsTyp) {
        return new IntygModuleDTO(new IntygModule(intygsTyp, "", "", "", "", "", "", "", "", false, false));
    }
}
