/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.webcert.persistence.utkast.model.*;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;

@RunWith(MockitoJUnitRunner.class)
public class SendNotificationStrategyTest {

    private static final String INTYG_ID_1 = "intyg-1";
    private static final String INTYG_ID_2 = "intyg-2";
    private static final String INTYG_ID_3 = "intyg-3";
    private static final String INTYG_ID_4 = "intyg-4";

    private static final String INTYG_FK = "fk7263";
    private static final String INTYG_LUSE = "luse";
    private static final String INTYG_TS = "ts-bas";

    private static final String ENHET_1 = "SE12345678-1000";
    private static final String ENHET_2 = "SE12345678-2000";
    private static final String ENHET_3 = "SE12345678-3000";
    private static final String ENHET_4 = "SE12345678-4000";

    @Mock
    private IntegreradeEnheterRegistry mockIntegreradeEnheterRegistry;

    @Mock
    private UtkastRepository mockUtkastRepository;

    @InjectMocks
    private SendNotificationStrategy sendStrategy = new DefaultSendNotificationStrategyImpl();

    @Before
    public void setupIntegreradeEnheter() {
        when(mockIntegreradeEnheterRegistry.getSchemaVersion(ENHET_1, INTYG_FK)).thenReturn(Optional.of(SchemaVersion.VERSION_1));
        when(mockIntegreradeEnheterRegistry.getSchemaVersion(ENHET_2, INTYG_FK)).thenReturn(Optional.empty());
        when(mockIntegreradeEnheterRegistry.getSchemaVersion(ENHET_3, INTYG_FK)).thenReturn(Optional.of(SchemaVersion.VERSION_1));
        when(mockIntegreradeEnheterRegistry.getSchemaVersion(ENHET_4, INTYG_LUSE)).thenReturn(Optional.of(SchemaVersion.VERSION_2));
    }

    @Before
    public void setupUtkastRepository() {
        Utkast utkast1 = createUtkast(INTYG_ID_1, INTYG_FK, ENHET_1);
        Utkast utkast2 = createUtkast(INTYG_ID_2, INTYG_FK, ENHET_2);
        Utkast utkast3 = createUtkast(INTYG_ID_3, INTYG_TS, ENHET_3);
        Utkast utkast4 = createUtkast(INTYG_ID_4, INTYG_LUSE, ENHET_4);
        when(mockUtkastRepository.findOne(INTYG_ID_1)).thenReturn(utkast1);
        when(mockUtkastRepository.findOne(INTYG_ID_2)).thenReturn(utkast2);
        when(mockUtkastRepository.findOne(INTYG_ID_3)).thenReturn(utkast3);
        when(mockUtkastRepository.findOne(INTYG_ID_4)).thenReturn(utkast4);
    }

    @Test
    public void testUtkastOk() {

        Optional<SchemaVersion> res = sendStrategy.decideNotificationForIntyg(createUtkast(INTYG_ID_1, INTYG_FK, ENHET_1));
        assertTrue(res.isPresent());
        assertEquals(SchemaVersion.VERSION_1, res.get());

        verify(mockIntegreradeEnheterRegistry).getSchemaVersion(ENHET_1, INTYG_FK);
    }

    @Test
    public void testUtkastUnitNotIntegrated() {
        Optional<SchemaVersion> res = sendStrategy.decideNotificationForIntyg(createUtkast(INTYG_ID_1, INTYG_FK, ENHET_2));
        assertFalse(res.isPresent());

        verify(mockIntegreradeEnheterRegistry).getSchemaVersion(ENHET_2, INTYG_FK);
    }

    @Test
    public void testUtkastWrongType() {
        when(mockIntegreradeEnheterRegistry.getSchemaVersion(ENHET_1, INTYG_TS)).thenReturn(Optional.empty());
        Optional<SchemaVersion> res = sendStrategy.decideNotificationForIntyg(createUtkast(INTYG_ID_1, INTYG_TS, ENHET_1));
        assertFalse(res.isPresent());
        verify(mockIntegreradeEnheterRegistry).getSchemaVersion(ENHET_1, INTYG_TS);
    }

    @Test
    public void testUtkastWrongSchemaVersionLuse() {
        when(mockIntegreradeEnheterRegistry.getSchemaVersion(ENHET_4, INTYG_LUSE)).thenReturn(Optional.empty());
        Optional<SchemaVersion> res = sendStrategy.decideNotificationForIntyg(createUtkast(INTYG_ID_4, INTYG_LUSE, ENHET_4));
        assertFalse(res.isPresent());

        verify(mockIntegreradeEnheterRegistry).getSchemaVersion(ENHET_4, INTYG_LUSE);
    }

    @Test
    public void testUtkastVersion2() {
        Optional<SchemaVersion> res = sendStrategy.decideNotificationForIntyg(createUtkast(INTYG_ID_4, INTYG_LUSE, ENHET_4));
        assertTrue(res.isPresent());
        assertEquals(SchemaVersion.VERSION_2, res.get());
        verify(mockIntegreradeEnheterRegistry).getSchemaVersion(ENHET_4, INTYG_LUSE);
    }

    private Utkast createUtkast(String intygId, String intygsTyp, String enhetsId) {

        VardpersonReferens vardperson = new VardpersonReferens();
        vardperson.setHsaId("SE12345678-0000");
        vardperson.setNamn("Dr Börje Dengroth");

        Utkast utkast = new Utkast();
        utkast.setIntygsId(intygId);
        utkast.setIntygsTyp(intygsTyp);
        utkast.setEnhetsId(enhetsId);
        utkast.setEnhetsNamn("Vårdenheten");
        utkast.setPatientPersonnummer(new Personnummer("19121212-1212"));
        utkast.setPatientFornamn("Tolvan");
        utkast.setPatientEfternamn("Tolvansson");
        utkast.setStatus(UtkastStatus.DRAFT_INCOMPLETE);
        utkast.setModel("{model}");
        utkast.setSkapadAv(vardperson);
        utkast.setSenastSparadAv(vardperson);

        return utkast;
    }
}
