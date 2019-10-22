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

package se.inera.intyg.webcert.web.converter.util;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ModuleEntryPoint;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;


@RunWith(MockitoJUnitRunner.class)
public class IntygDraftDecoratorTest {

    @Mock
    private IntygModuleRegistry intygModuleRegistry;

    @InjectMocks
    private IntygDraftDecorator intygDraftDecorator;

    @Test
    public void testDecorateWithCertificateTypeName() throws Exception {
        final ModuleEntryPoint moduleEntryPointOne = mock(ModuleEntryPoint.class);
        doReturn("TypeOneName").when(moduleEntryPointOne).getModuleName();

        final ModuleEntryPoint moduleEntryPointTwo = mock(ModuleEntryPoint.class);
        doReturn("TypeTwoName").when(moduleEntryPointTwo).getModuleName();

        doReturn(moduleEntryPointOne).when(intygModuleRegistry).getModuleEntryPoint("TypeOne");
        doReturn(moduleEntryPointTwo).when(intygModuleRegistry).getModuleEntryPoint("TypeTwo");

        final List<ListIntygEntry> listIntygEntries = getIntygEntryList();

        intygDraftDecorator.decorateWithCertificateTypeName(listIntygEntries);

        assertEquals(3, listIntygEntries.size());
        assertEquals("TypeOneName", listIntygEntries.get(0).getIntygTypeName());
        assertEquals("TypeTwoName", listIntygEntries.get(1).getIntygTypeName());
        assertEquals("TypeOneName", listIntygEntries.get(2).getIntygTypeName());
    }

    @Test
    public void testDecorateWithCertificateTypeNameMissingModule() throws Exception {
        final ModuleEntryPoint moduleEntryPointOne = mock(ModuleEntryPoint.class);
        doReturn("TypeOneName").when(moduleEntryPointOne).getModuleName();

        final ModuleEntryPoint moduleEntryPointTwo = mock(ModuleEntryPoint.class);
        doReturn("TypeTwoName").when(moduleEntryPointTwo).getModuleName();

        doReturn(moduleEntryPointOne).when(intygModuleRegistry).getModuleEntryPoint("TypeOne");
        doReturn(moduleEntryPointTwo).when(intygModuleRegistry).getModuleEntryPoint("TypeTwo");

        doThrow(new ModuleNotFoundException()).when(intygModuleRegistry).getModuleEntryPoint("TypeThree");

        final ListIntygEntry listIntygEntryMissingModule = new ListIntygEntry();
        listIntygEntryMissingModule.setIntygType("TypeThree");

        final List<ListIntygEntry> listIntygEntries = getIntygEntryList();
        listIntygEntries.add(listIntygEntryMissingModule);

        intygDraftDecorator.decorateWithCertificateTypeName(listIntygEntries);

        assertEquals(4, listIntygEntries.size());
        assertEquals("TypeOneName", listIntygEntries.get(0).getIntygTypeName());
        assertEquals("TypeTwoName", listIntygEntries.get(1).getIntygTypeName());
        assertEquals("TypeOneName", listIntygEntries.get(2).getIntygTypeName());
        assertEquals("TypeThree", listIntygEntries.get(3).getIntygTypeName());
    }

    private List<ListIntygEntry> getIntygEntryList() {
        final ListIntygEntry listIntygEntryOne = new ListIntygEntry();
        listIntygEntryOne.setIntygType("TypeOne");

        final ListIntygEntry listIntygEntryTwo = new ListIntygEntry();
        listIntygEntryTwo.setIntygType("TypeTwo");

        final ListIntygEntry listIntygEntryThree = new ListIntygEntry();
        listIntygEntryThree.setIntygType("TypeOne");

        final List<ListIntygEntry> listIntygEntries = new ArrayList<>();
        listIntygEntries.add(listIntygEntryOne);
        listIntygEntries.add(listIntygEntryTwo);
        listIntygEntries.add(listIntygEntryThree);

        return listIntygEntries;
    }
}
