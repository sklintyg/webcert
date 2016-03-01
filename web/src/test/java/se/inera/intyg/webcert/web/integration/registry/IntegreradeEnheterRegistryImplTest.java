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

package se.inera.intyg.webcert.web.integration.registry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.webcert.persistence.integreradenhet.model.IntegreradEnhet;
import se.inera.intyg.webcert.persistence.integreradenhet.model.SchemaVersion;
import se.inera.intyg.webcert.persistence.integreradenhet.repository.IntegreradEnhetRepository;
import se.inera.intyg.webcert.web.integration.registry.dto.IntegreradEnhetEntry;

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
        SchemaVersion schemaVersion = SchemaVersion.V1;

        when(integreradEnhetRepository.findOne(enhetsId)).thenReturn(null); // not found
        when(integreradEnhetRepository.save(any(IntegreradEnhet.class))).thenReturn(new IntegreradEnhet());

        registry.putIntegreradEnhet(entry, schemaVersion);

        ArgumentCaptor<IntegreradEnhet> enhetCaptor = ArgumentCaptor.forClass(IntegreradEnhet.class);
        verify(integreradEnhetRepository).save(enhetCaptor.capture());

        assertEquals(schemaVersion, enhetCaptor.getValue().getSchemaVersion());
    }

    @Test
    public void testPutIntegreradEnhetAlredyExists() {
        final String enhetsId = "enhetsId";
        IntegreradEnhetEntry entry = new IntegreradEnhetEntry(enhetsId, "vardgivareId");
        SchemaVersion schemaVersion = SchemaVersion.V2;
        IntegreradEnhet integreradEnhet = new IntegreradEnhet();
        integreradEnhet.setSchemaVersion(SchemaVersion.V2);

        when(integreradEnhetRepository.findOne(enhetsId)).thenReturn(integreradEnhet); // already exists
        when(integreradEnhetRepository.save(any(IntegreradEnhet.class))).thenReturn(new IntegreradEnhet());

        registry.putIntegreradEnhet(entry, schemaVersion);

        // one update is made with control date
        ArgumentCaptor<IntegreradEnhet> enhetCaptor = ArgumentCaptor.forClass(IntegreradEnhet.class);
        verify(integreradEnhetRepository).save(enhetCaptor.capture());

        assertNotNull(enhetCaptor.getValue().getSenasteKontrollDatum());
    }

    @Test
    public void testPutIntegreradEnhetAlredyExistsGreaterSchemaVersion() {
        final String enhetsId = "enhetsId";
        IntegreradEnhetEntry entry = new IntegreradEnhetEntry(enhetsId, "vardgivareId");
        SchemaVersion newSchemaVersion = SchemaVersion.V2;
        IntegreradEnhet integreradEnhet = new IntegreradEnhet();
        integreradEnhet.setSchemaVersion(SchemaVersion.V1);

        when(integreradEnhetRepository.findOne(enhetsId)).thenReturn(integreradEnhet); // already exists
        when(integreradEnhetRepository.save(any(IntegreradEnhet.class))).thenReturn(new IntegreradEnhet());

        registry.putIntegreradEnhet(entry, newSchemaVersion);

        // two updates are made; one with control date and one with schema version
        ArgumentCaptor<IntegreradEnhet> enhetCaptor = ArgumentCaptor.forClass(IntegreradEnhet.class);
        verify(integreradEnhetRepository, times(2)).save(enhetCaptor.capture());

        assertNotNull(enhetCaptor.getAllValues().get(0).getSenasteKontrollDatum());
        assertEquals(newSchemaVersion, enhetCaptor.getAllValues().get(1).getSchemaVersion());
    }

    @Test
    public void testIsEnhetIntegreradTrue() {
        final String enhetsId = "enhetsId";

        when(integreradEnhetRepository.findOne(enhetsId)).thenReturn(new IntegreradEnhet()); // exists

        boolean result = registry.isEnhetIntegrerad(enhetsId);
        assertTrue(result);
    }

    @Test
    public void testIsEnhetIntegreradFalse() {
        final String enhetsId = "enhetsId";

        when(integreradEnhetRepository.findOne(enhetsId)).thenReturn(null); // not found

        boolean result = registry.isEnhetIntegrerad(enhetsId);
        assertFalse(result);
    }

    @Test
    public void testAddIfSameVardgivareButDifferentUnitsCopiesSchemaVersion() {
        final String enhetsId = "enhetsId";
        final String vardgivarId = "vardgivarId";
        IntegreradEnhetEntry entry = new IntegreradEnhetEntry(enhetsId, vardgivarId);
        SchemaVersion schemaVersion = SchemaVersion.V2;
        IntegreradEnhet integreradEnhet = new IntegreradEnhet();
        integreradEnhet.setEnhetsId("another enhetsId");
        integreradEnhet.setSchemaVersion(schemaVersion);
        integreradEnhet.setVardgivarId(vardgivarId);

        when(integreradEnhetRepository.findOne(enhetsId)).thenReturn(integreradEnhet); // already exists
        when(integreradEnhetRepository.save(any(IntegreradEnhet.class))).thenReturn(new IntegreradEnhet());

        registry.addIfSameVardgivareButDifferentUnits(enhetsId, entry);

        // two updates are made; one with control date and one with schema version
        ArgumentCaptor<IntegreradEnhet> enhetCaptor = ArgumentCaptor.forClass(IntegreradEnhet.class);
        verify(integreradEnhetRepository, times(2)).save(enhetCaptor.capture());

        assertNotNull(enhetCaptor.getAllValues().get(0).getSenasteKontrollDatum());
        assertEquals(schemaVersion, enhetCaptor.getAllValues().get(1).getSchemaVersion());
    }

    @Test
    public void testAddIfSameVardgivareButDifferentUnitsAlreadyExists() {
        final String enhetsId = "enhetsId";
        final String vardgivarId = "vardgivarId";
        IntegreradEnhetEntry entry = new IntegreradEnhetEntry(enhetsId, vardgivarId);
        SchemaVersion schemaVersion = SchemaVersion.V2;
        IntegreradEnhet integreradEnhet = new IntegreradEnhet();
        integreradEnhet.setEnhetsId(enhetsId);
        integreradEnhet.setSchemaVersion(schemaVersion);
        integreradEnhet.setVardgivarId(vardgivarId);

        when(integreradEnhetRepository.findOne(enhetsId)).thenReturn(integreradEnhet); // exists

        registry.addIfSameVardgivareButDifferentUnits(enhetsId, entry);

        // one update is made with control date
        ArgumentCaptor<IntegreradEnhet> enhetCaptor = ArgumentCaptor.forClass(IntegreradEnhet.class);
        verify(integreradEnhetRepository).save(enhetCaptor.capture());

        assertNotNull(enhetCaptor.getValue().getSenasteKontrollDatum());
    }

    @Test
    public void testDeleteIntegreradEnhet() {
        final String enhetsId = "enhetsId";
        IntegreradEnhet integreradEnhet = new IntegreradEnhet();

        when(integreradEnhetRepository.findOne(enhetsId)).thenReturn(integreradEnhet); // exists
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
    public void testGetSchemaVersion() {
        final String enhetsId = "enhetsid";
        final SchemaVersion schemaVersion = SchemaVersion.V1;

        IntegreradEnhet enhet = new IntegreradEnhet();
        enhet.setSchemaVersion(schemaVersion);

        when(integreradEnhetRepository.findOne(enhetsId)).thenReturn(enhet);

        Optional<SchemaVersion> result = registry.getSchemaVersion(enhetsId);
        assertTrue(result.isPresent());
        assertEquals(schemaVersion, result.get());
    }

    @Test
    public void testGetSchemaVersionNotFound() {
        final String enhetsId = "enhetsid";

        when(integreradEnhetRepository.findOne(enhetsId)).thenReturn(null); // not found

        Optional<SchemaVersion> result = registry.getSchemaVersion(enhetsId);
        assertFalse(result.isPresent());
    }
}
