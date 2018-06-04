/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.integration.registry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.luse.support.LuseEntryPoint;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.webcert.persistence.integreradenhet.model.IntegreradEnhet;
import se.inera.intyg.webcert.persistence.integreradenhet.repository.IntegreradEnhetRepository;
import se.inera.intyg.webcert.web.integration.registry.dto.IntegreradEnhetEntry;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IntegreradeEnheterRegistryImplTest {

    @Mock
    private IntegreradEnhetRepository integreradEnhetRepository;

    @InjectMocks
    private IntegreradeEnheterRegistryImpl registry;

    @Test
    public void testPutIntegreradEnhetSetsSchemaVersion() {
        final String enhetsId = "enhetsId";
        IntegreradEnhetEntry entry = new IntegreradEnhetEntry(enhetsId, "vardgivareId");

        when(integreradEnhetRepository.findOne(enhetsId)).thenReturn(null); // not found
        when(integreradEnhetRepository.save(any(IntegreradEnhet.class))).thenReturn(new IntegreradEnhet());

        registry.putIntegreradEnhet(entry, false, true);

        ArgumentCaptor<IntegreradEnhet> enhetCaptor = ArgumentCaptor.forClass(IntegreradEnhet.class);
        verify(integreradEnhetRepository).save(enhetCaptor.capture());

        assertTrue(enhetCaptor.getValue().isSchemaVersion3());
    }

    @Test
    public void testPutIntegreradEnhetAlreadyExists() {
        final String enhetsId = "enhetsId";
        IntegreradEnhetEntry entry = new IntegreradEnhetEntry(enhetsId, "vardgivareId");
        IntegreradEnhet integreradEnhet = new IntegreradEnhet();
        integreradEnhet.setSchemaVersion1(false);
        integreradEnhet.setSchemaVersion3(true);

        when(integreradEnhetRepository.findOne(enhetsId)).thenReturn(integreradEnhet);
        when(integreradEnhetRepository.save(any(IntegreradEnhet.class))).thenReturn(new IntegreradEnhet());

        registry.putIntegreradEnhet(entry, true, false);

        ArgumentCaptor<IntegreradEnhet> enhetCaptor = ArgumentCaptor.forClass(IntegreradEnhet.class);
        verify(integreradEnhetRepository, times(1)).save(enhetCaptor.capture());

        assertTrue(enhetCaptor.getValue().isSchemaVersion1());
        assertTrue(enhetCaptor.getValue().isSchemaVersion3());
    }

    @Test
    public void testIsEnhetIntegreradTrue() {
        final String enhetsId = "enhetsId";

        IntegreradEnhet enhet = new IntegreradEnhet();
        enhet.setSchemaVersion1(true);
        when(integreradEnhetRepository.findOne(eq(enhetsId))).thenReturn(enhet); // exists

        boolean result = registry.isEnhetIntegrerad(enhetsId, Fk7263EntryPoint.MODULE_ID);
        assertTrue(result);
    }

    @Test
    public void testIsEnhetIntegreradFalse() {
        final String enhetsId = "enhetsId";

        when(integreradEnhetRepository.findOne(eq(enhetsId))).thenReturn(null);

        boolean result = registry.isEnhetIntegrerad(enhetsId, Fk7263EntryPoint.MODULE_ID);
        assertFalse(result);
    }

    @Test
    public void testAddIfSameVardgivareButDifferentUnitsCopiesSchemaVersion() {
        final String enhetsId = "enhetsId";
        final String vardgivarId = "vardgivarId";
        IntegreradEnhetEntry entry = new IntegreradEnhetEntry(enhetsId, vardgivarId);
        IntegreradEnhet integreradEnhet = new IntegreradEnhet();
        integreradEnhet.setEnhetsId("another enhetsId");
        integreradEnhet.setSchemaVersion3(true);
        integreradEnhet.setVardgivarId(vardgivarId);

        // already exists
        when(integreradEnhetRepository.findOne(eq(enhetsId))).thenReturn(integreradEnhet);
        when(integreradEnhetRepository.save(any(IntegreradEnhet.class))).thenReturn(new IntegreradEnhet());

        registry.addIfSameVardgivareButDifferentUnits(enhetsId, entry, LuseEntryPoint.MODULE_ID);

        ArgumentCaptor<IntegreradEnhet> enhetCaptor = ArgumentCaptor.forClass(IntegreradEnhet.class);
        verify(integreradEnhetRepository, times(3)).save(enhetCaptor.capture());

        assertNotNull(enhetCaptor.getAllValues().get(0).getSenasteKontrollDatum());
        assertTrue(enhetCaptor.getAllValues().get(1).isSchemaVersion3());
    }

    @Test
    public void testAddIfSameVardgivareButDifferentUnitsAlreadyExists() {
        final String enhetsId = "enhetsId";
        final String vardgivarId = "vardgivarId";
        IntegreradEnhetEntry entry = new IntegreradEnhetEntry(enhetsId, vardgivarId);
        IntegreradEnhet integreradEnhet = new IntegreradEnhet();
        integreradEnhet.setEnhetsId(enhetsId);
        integreradEnhet.setSchemaVersion3(true);
        integreradEnhet.setVardgivarId(vardgivarId);

        when(integreradEnhetRepository.findOne(eq(enhetsId))).thenReturn(integreradEnhet);

        registry.addIfSameVardgivareButDifferentUnits(enhetsId, entry, LuseEntryPoint.MODULE_ID);

        ArgumentCaptor<IntegreradEnhet> enhetCaptor = ArgumentCaptor.forClass(IntegreradEnhet.class);
        verify(integreradEnhetRepository, times(2)).save(enhetCaptor.capture());

        assertNotNull(enhetCaptor.getValue().getSenasteKontrollDatum());
    }

    @Test
    public void testDeleteIntegreradEnhet() {
        final String enhetsId = "enhetsId";
        IntegreradEnhet integreradEnhet = new IntegreradEnhet();

        when(integreradEnhetRepository.findOne(eq(enhetsId))).thenReturn(integreradEnhet);
        registry.deleteIntegreradEnhet(enhetsId);

        verify(integreradEnhetRepository).delete(integreradEnhet);
    }

    @Test
    public void testDeleteIntegreradEnhetNotFound() {
        final String enhetsId = "enhetsId";

        when(integreradEnhetRepository.findOne(enhetsId)).thenReturn(null); // not found
        registry.deleteIntegreradEnhet(enhetsId);

        verify(integreradEnhetRepository, never()).delete(any(IntegreradEnhet.class));
    }

    @Test
    public void testGetSchemaVersionOldV1Found() {
        final String enhetsId = "enhetsid";

        IntegreradEnhet enhet = new IntegreradEnhet();
        enhet.setSchemaVersion1(true);
        when(integreradEnhetRepository.findOne(eq(enhetsId))).thenReturn(enhet);

        Optional<SchemaVersion> result = registry.getSchemaVersion(enhetsId, Fk7263EntryPoint.MODULE_ID);
        assertTrue(result.isPresent());
        assertEquals(SchemaVersion.VERSION_1, result.get());
    }

    @Test
    public void testGetSchemaVersionOldV3Found() {
        final String enhetsId = "enhetsid";

        IntegreradEnhet enhet = new IntegreradEnhet();
        enhet.setSchemaVersion1(true);
        enhet.setSchemaVersion3(true);
        when(integreradEnhetRepository.findOne(eq(enhetsId))).thenReturn(enhet);

        Optional<SchemaVersion> result = registry.getSchemaVersion(enhetsId, Fk7263EntryPoint.MODULE_ID);
        assertTrue(result.isPresent());
        assertEquals(SchemaVersion.VERSION_3, result.get());
    }

    @Test
    public void testGetSchemaVersionNotFound() {
        final String enhetsId = "enhetsid";

        IntegreradEnhet enhet = new IntegreradEnhet();
        enhet.setSchemaVersion1(false);
        enhet.setSchemaVersion3(false);
        when(integreradEnhetRepository.findOne(eq(enhetsId))).thenReturn(enhet);

        assertFalse(registry.getSchemaVersion(enhetsId, Fk7263EntryPoint.MODULE_ID).isPresent());
        assertFalse(registry.getSchemaVersion(enhetsId, LuseEntryPoint.MODULE_ID).isPresent());
    }

    @Test
    public void testGetSchemaVersionNewFound() {
        final String enhetsId = "enhetsid";

        IntegreradEnhet enhet = new IntegreradEnhet();
        enhet.setSchemaVersion1(false);
        enhet.setSchemaVersion3(true);
        when(integreradEnhetRepository.findOne(enhetsId)).thenReturn(enhet);

        Optional<SchemaVersion> result = registry.getSchemaVersion(enhetsId, LuseEntryPoint.MODULE_ID);
        assertTrue(result.isPresent());
        assertEquals(SchemaVersion.VERSION_3, result.get());
    }
}
