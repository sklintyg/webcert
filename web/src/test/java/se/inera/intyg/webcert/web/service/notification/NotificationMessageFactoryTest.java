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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.modules.support.api.notification.ArendeCount;
import se.inera.intyg.common.support.modules.support.api.notification.FragorOchSvar;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.common.model.UtkastStatus;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;

/**
 * Created by Magnus Ekstrand on 03/12/14.
 */
@RunWith(MockitoJUnitRunner.class)
public class NotificationMessageFactoryTest {

    private static final String INTYGS_ID = "1234";
    private static final String INTYGS_TYP = "fk7263";

    @Mock
    private FragorOchSvarCreator mockFragorOchSvarCreator;

    @Mock
    private UtkastRepository mockUtkastRepository;

    @InjectMocks
    private NotificationMessageFactory notificationMessageFactory = new NotificationMessageFactoryImpl();

    @Test
    public void testCreateNotificationMessageForUtkast() {

        Utkast utkast = createUtkast(INTYGS_ID);
        String reference = "ref";

        NotificationMessage msg = notificationMessageFactory.createNotificationMessage(utkast, HandelsekodEnum.SIGNAT,
                SchemaVersion.VERSION_1, reference, null, null);

        assertNotNull(msg);
        assertNotNull(msg.getHandelse());
        assertEquals(HandelsekodEnum.SIGNAT, msg.getHandelse());
        assertNotNull(msg.getHandelseTid());
        assertEquals(INTYGS_ID, msg.getIntygsId());
        assertEquals(INTYGS_TYP, msg.getIntygsTyp());
        assertEquals("SE12345678-1000", msg.getLogiskAdress());
        assertEquals("{model}", msg.getUtkast());
        assertNotNull(msg.getFragaSvar());
        assertEquals(SchemaVersion.VERSION_1, msg.getVersion());
        assertEquals(reference, msg.getReference());
        assertNotNull(msg.getFragaSvar());
        assertNull(msg.getSkickadeFragor());
        assertNull(msg.getMottagnaFragor());
        assertNull(msg.getAmne());
        assertNull(msg.getSistaSvarsDatum());

        verifyZeroInteractions(mockFragorOchSvarCreator);
    }

    @Test
    public void testCreateNotificationMessageForUtkastSchemaVersion3() {

        Utkast utkast = createUtkast(INTYGS_ID);
        String reference = "ref";

        NotificationMessage msg = notificationMessageFactory.createNotificationMessage(utkast, HandelsekodEnum.SIGNAT,
                SchemaVersion.VERSION_3, reference, null, null);

        assertNotNull(msg);
        assertNotNull(msg.getHandelse());
        assertEquals(HandelsekodEnum.SIGNAT, msg.getHandelse());
        assertNotNull(msg.getHandelseTid());
        assertEquals(INTYGS_ID, msg.getIntygsId());
        assertEquals(INTYGS_TYP, msg.getIntygsTyp());
        assertEquals("SE12345678-1000", msg.getLogiskAdress());
        assertEquals("{model}", msg.getUtkast());
        assertEquals(SchemaVersion.VERSION_3, msg.getVersion());
        assertEquals(reference, msg.getReference());
        assertNull(msg.getFragaSvar());
        assertNotNull(msg.getSkickadeFragor());
        assertNotNull(msg.getMottagnaFragor());
        assertNull(msg.getAmne());
        assertNull(msg.getSistaSvarsDatum());

        verifyZeroInteractions(mockFragorOchSvarCreator);
    }

    @Test
    public void testCreateNotificationMessageForUsesFragorOchSvarSchemaVersion1() {
        when(mockFragorOchSvarCreator.createFragorOchSvar(INTYGS_ID)).thenReturn(new FragorOchSvar(1, 1, 1, 1));

        Utkast utkast = createUtkast(INTYGS_ID);
        final String reference = "ref";
        NotificationMessage msg = notificationMessageFactory.createNotificationMessage(utkast, HandelsekodEnum.NYFRFM,
                SchemaVersion.VERSION_1, reference, null, null);

        assertNotNull(msg);
        assertNotNull(msg.getHandelse());
        assertEquals(HandelsekodEnum.NYFRFM, msg.getHandelse());
        assertNotNull(msg.getHandelseTid());
        assertEquals(INTYGS_ID, msg.getIntygsId());
        assertEquals(INTYGS_TYP, msg.getIntygsTyp());
        assertEquals("SE12345678-1000", msg.getLogiskAdress());
        assertEquals("{model}", msg.getUtkast());
        assertEquals(SchemaVersion.VERSION_1, msg.getVersion());
        assertEquals(reference, msg.getReference());
        assertNotNull(msg.getFragaSvar());
        assertEquals(1, msg.getFragaSvar().getAntalFragor());
        assertEquals(1, msg.getFragaSvar().getAntalHanteradeFragor());
        assertEquals(1, msg.getFragaSvar().getAntalHanteradeSvar());
        assertEquals(1, msg.getFragaSvar().getAntalSvar());
        assertNull(msg.getSkickadeFragor());
        assertNull(msg.getMottagnaFragor());
        assertNull(msg.getAmne());
        assertNull(msg.getSistaSvarsDatum());

        verify(mockFragorOchSvarCreator).createFragorOchSvar(INTYGS_ID);
        verifyNoMoreInteractions(mockFragorOchSvarCreator);
    }

    @Test
    public void testCreateNotificationMessageForUsesFragorOchSvarSchemaVersion3() {
        when(mockFragorOchSvarCreator.createArenden(INTYGS_ID, INTYGS_TYP)).thenReturn(
                Pair.of(new ArendeCount(1, 1, 1, 1), new ArendeCount(2, 2, 2, 2)));

        Utkast utkast = createUtkast(INTYGS_ID);
        final String reference = "ref";

        NotificationMessage msg = notificationMessageFactory.createNotificationMessage(utkast, HandelsekodEnum.NYFRFV,
                SchemaVersion.VERSION_3, reference, null, null);

        assertNotNull(msg);
        assertNotNull(msg.getHandelse());
        assertEquals(HandelsekodEnum.NYFRFV, msg.getHandelse());
        assertNotNull(msg.getHandelseTid());
        assertEquals(INTYGS_ID, msg.getIntygsId());
        assertEquals(INTYGS_TYP, msg.getIntygsTyp());
        assertEquals("SE12345678-1000", msg.getLogiskAdress());
        assertEquals("{model}", msg.getUtkast());
        assertEquals(SchemaVersion.VERSION_3, msg.getVersion());
        assertEquals(reference, msg.getReference());
        assertNull(msg.getFragaSvar());
        assertNotNull(msg.getSkickadeFragor());
        assertEquals(1, msg.getSkickadeFragor().getTotalt());
        assertEquals(1, msg.getSkickadeFragor().getBesvarade());
        assertEquals(1, msg.getSkickadeFragor().getEjBesvarade());
        assertEquals(1, msg.getSkickadeFragor().getHanterade());
        assertNotNull(msg.getMottagnaFragor());
        assertEquals(2, msg.getMottagnaFragor().getTotalt());
        assertEquals(2, msg.getMottagnaFragor().getBesvarade());
        assertEquals(2, msg.getMottagnaFragor().getEjBesvarade());
        assertEquals(2, msg.getMottagnaFragor().getHanterade());

        verify(mockFragorOchSvarCreator).createArenden(INTYGS_ID, INTYGS_TYP);
        verifyNoMoreInteractions(mockFragorOchSvarCreator);
    }

    private Utkast createUtkast(String intygId) {

        VardpersonReferens vardperson = new VardpersonReferens();
        vardperson.setHsaId("SE12345678-0000");
        vardperson.setNamn("Dr Börje Dengroth");

        Utkast utkast = new Utkast();
        utkast.setIntygsId(intygId);
        utkast.setIntygsTyp(INTYGS_TYP);
        utkast.setEnhetsId("SE12345678-1000");
        utkast.setEnhetsNamn("Vårdenhet 1");
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
