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
package se.inera.intyg.webcert.web.service.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.modules.support.api.notification.ArendeCount;
import se.inera.intyg.common.support.modules.support.api.notification.FragorOchSvar;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.referens.ReferensService;

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

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private SendNotificationStrategy sendNotificationStrategy;

    @Mock
    private ReferensService referenceService;

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

        verifyNoInteractions(mockFragorOchSvarCreator);
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

        verifyNoInteractions(mockFragorOchSvarCreator);
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
        utkast.setPatientPersonnummer(Personnummer.createPersonnummer("19121212-1212").get());
        utkast.setPatientFornamn("Tolvan");
        utkast.setPatientEfternamn("Tolvansson");
        utkast.setStatus(UtkastStatus.DRAFT_INCOMPLETE);
        utkast.setModel("{model}");
        utkast.setSkapadAv(vardperson);
        utkast.setSenastSparadAv(vardperson);

        return utkast;
    }

    @Test
    public void shallDecideSchemaStrategyBasedOnUtlatandeWhenCreatingFromEvent() throws Exception {
        final var event = createEvent();
        final var json = "DRAFT_JSON";
        final var expectedSchemaVersion = SchemaVersion.VERSION_1;

        final var utlatande = mock(Utlatande.class);
        final var moduleApi = mock(ModuleApi.class);
        doReturn(moduleApi).when(moduleRegistry).getModuleApi(any(), any());
        doReturn(utlatande).when(moduleApi).getUtlatandeFromJson(json);
        doReturn(Optional.of(expectedSchemaVersion)).when(sendNotificationStrategy).decideNotificationForIntyg(utlatande);

        final var actualNotificationMessage = notificationMessageFactory.createNotificationMessage(event, json);

        assertEquals(expectedSchemaVersion, actualNotificationMessage.getVersion());
    }

    @Test
    public void shallUseSchemaV3StrategyAsDefaultWhenCreatingFromEvent() throws Exception {
        final var event = createEvent();
        final var json = "DRAFT_JSON";
        final var expectedSchemaVersion = SchemaVersion.VERSION_3;

        final var utlatande = mock(Utlatande.class);
        final var moduleApi = mock(ModuleApi.class);
        doReturn(moduleApi).when(moduleRegistry).getModuleApi(any(), any());
        doReturn(utlatande).when(moduleApi).getUtlatandeFromJson(json);
        doReturn(Optional.empty()).when(sendNotificationStrategy).decideNotificationForIntyg(utlatande);

        final var actualNotificationMessage = notificationMessageFactory.createNotificationMessage(event, json);

        assertEquals(expectedSchemaVersion, actualNotificationMessage.getVersion());
    }

    @Test
    public void shallIncludeReferenceWhenCreatingFromEvent() throws Exception {
        final var event = createEvent();
        final var json = "DRAFT_JSON";
        final var expectedReference = "REFERENCE";

        final var utlatande = mock(Utlatande.class);
        final var moduleApi = mock(ModuleApi.class);
        doReturn(moduleApi).when(moduleRegistry).getModuleApi(any(), any());
        doReturn(utlatande).when(moduleApi).getUtlatandeFromJson(json);
        doReturn(expectedReference).when(referenceService).getReferensForIntygsId(event.getIntygsId());

        final var actualNotificationMessage = notificationMessageFactory.createNotificationMessage(event, json);

        assertEquals(expectedReference, actualNotificationMessage.getReference());
    }

    @Test
    public void shallAddTopicFromEventWhenCreatingFromEvent() throws Exception {
        final var event = createEvent();
        final var json = "DRAFT_JSON";

        final var utlatande = mock(Utlatande.class);
        final var moduleApi = mock(ModuleApi.class);
        doReturn(moduleApi).when(moduleRegistry).getModuleApi(any(), any());
        doReturn(utlatande).when(moduleApi).getUtlatandeFromJson(json);

        final var actualNotificationMessage = notificationMessageFactory.createNotificationMessage(event, json);

        assertEquals(event.getAmne().name(), actualNotificationMessage.getAmne().getCode());
        assertEquals(event.getAmne().getDescription(), actualNotificationMessage.getAmne().getDisplayName());
    }

    @Test
    public void shallLeaveTopicAsNullIfMissingWhenCreatingFromEvent() throws Exception {
        final var event = createEvent();
        final var json = "DRAFT_JSON";
        event.setAmne(null);

        final var utlatande = mock(Utlatande.class);
        final var moduleApi = mock(ModuleApi.class);
        doReturn(moduleApi).when(moduleRegistry).getModuleApi(any(), any());
        doReturn(utlatande).when(moduleApi).getUtlatandeFromJson(json);

        final var actualNotificationMessage = notificationMessageFactory.createNotificationMessage(event, json);

        assertEquals(null, actualNotificationMessage.getAmne());
    }

    @Test
    public void shallResetEventTimestampBasedOnEventRecord() throws ModuleNotFoundException, IOException, ModuleException {
        final var event = createEvent();
        final var json = "DRAFT_JSON";
        final var eventTime = LocalDateTime.of(2021, Month.APRIL, 1, 12, 34, 56, 123456789);
        event.setTimestamp(eventTime);

        final var utlatande = mock(Utlatande.class);
        final var moduleApi = mock(ModuleApi.class);
        doReturn(moduleApi).when(moduleRegistry).getModuleApi(any(), any());
        doReturn(utlatande).when(moduleApi).getUtlatandeFromJson(json);

        final var actualNotificationMessage = notificationMessageFactory.createNotificationMessage(event, json);

        assertEquals(eventTime, actualNotificationMessage.getHandelseTid());
    }

    private Handelse createEvent() {
        final var event = new Handelse();
        event.setId(1000L);
        event.setDeliveryStatus(NotificationDeliveryStatusEnum.RESEND);
        event.setEnhetsId("UNIT_ID");
        event.setCode(HandelsekodEnum.SKAPAT);
        event.setIntygsId("CERTIFICATE_ID");
        event.setVardgivarId("CAREPROVIDER_ID");
        event.setTimestamp(LocalDateTime.now());
        event.setHanteratAv("HANDLED_BY");
        event.setPersonnummer("PERSON_NUMBER");
        event.setCertificateVersion("CERTIFICATE_VERSION");
        event.setCertificateIssuer("CERTIFICATE_ISSUER");
        event.setCertificateVersion("CERTIFICATE_VERSION");
        event.setAmne(ArendeAmne.AVSTMN);
        event.setSistaDatumForSvar(LocalDate.now().plusDays(10));
        return event;
    }
}
