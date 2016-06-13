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

package se.inera.intyg.webcert.notification_sender.notifications.services;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import org.apache.camel.Message;
import org.apache.camel.impl.DefaultMessage;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.modules.support.api.notification.*;
import se.inera.intyg.common.support.modules.support.api.notification.FragorOchSvar;
import se.inera.intyg.intygstyper.fk7263.model.converter.Fk7263InternalToNotification;
import se.inera.intyg.intygstyper.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.webcert.notification_sender.notifications.routes.RouteHeaders;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.UtlatandeType;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.UtlatandeId;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.ArbetsplatsKod;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.v2.*;

@RunWith(MockitoJUnitRunner.class)
public class NotificationTransformerTest {

    private static final String INTYGS_ID = "intyg1";
    private static final String LOGISK_ADRESS = "address1";
    private static final String FK7263 = Fk7263EntryPoint.MODULE_ID;
    private static final String LUSE = "luse";

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private Fk7263InternalToNotification internalToNotification;

    @InjectMocks
    private NotificationTransformer transformer;

    @Test
    public void testSend() throws Exception {
        // Given
        NotificationMessage notificationMessage = new NotificationMessage(INTYGS_ID, FK7263, new LocalDateTime(),
                HandelseType.INTYGSUTKAST_SKAPAT, LOGISK_ADRESS, "{ }", FragorOchSvar.getEmpty(), SchemaVersion.VERSION_1);
        Message message = spy(new DefaultMessage());
        message.setBody(notificationMessage);

        setupInternalToNotification();

        // When
        transformer.process(message);

        // Then
        assertEquals(INTYGS_ID, ((CertificateStatusUpdateForCareType) message.getBody()).getUtlatande().getUtlatandeId().getExtension());
        assertEquals(HandelseType.INTYGSUTKAST_SKAPAT.value(), message.getHeader(RouteHeaders.HANDELSE));
        assertEquals(INTYGS_ID, message.getHeader(RouteHeaders.INTYGS_ID));
        assertEquals(LOGISK_ADRESS, message.getHeader(RouteHeaders.LOGISK_ADRESS));
        assertEquals(SchemaVersion.VERSION_1.name(), message.getHeader(RouteHeaders.VERSION));

        verify(message, times(1)).setHeader(eq(RouteHeaders.LOGISK_ADRESS), eq(LOGISK_ADRESS));
        verify(message, times(1)).setHeader(eq(RouteHeaders.INTYGS_ID), eq(INTYGS_ID));
        verify(message, times(1)).setHeader(eq(RouteHeaders.HANDELSE), eq(HandelseType.INTYGSUTKAST_SKAPAT.value()));
        verify(message, times(1)).setHeader(eq(RouteHeaders.VERSION), eq(SchemaVersion.VERSION_1.name()));
        verify(internalToNotification, times(1)).createCertificateStatusUpdateForCareType(any());
    }

    @Test
    public void testSendBackwardsCompatibility() throws Exception {
        // Given
        NotificationMessage notificationMessage = new NotificationMessage(INTYGS_ID, FK7263, new LocalDateTime(),
                HandelseType.INTYGSUTKAST_SKAPAT, LOGISK_ADRESS, "{ }", FragorOchSvar.getEmpty(), null);
        Message message = spy(new DefaultMessage());
        message.setBody(notificationMessage);

        setupInternalToNotification();

        // When
        transformer.process(message);

        // Then
        assertEquals(INTYGS_ID, ((CertificateStatusUpdateForCareType) message.getBody()).getUtlatande().getUtlatandeId().getExtension());
        assertEquals(HandelseType.INTYGSUTKAST_SKAPAT.value(), message.getHeader(RouteHeaders.HANDELSE));
        assertEquals(INTYGS_ID, message.getHeader(RouteHeaders.INTYGS_ID));
        assertEquals(LOGISK_ADRESS, message.getHeader(RouteHeaders.LOGISK_ADRESS));
        assertEquals(SchemaVersion.VERSION_1.name(), message.getHeader(RouteHeaders.VERSION));

        verify(message, times(1)).setHeader(eq(RouteHeaders.LOGISK_ADRESS), eq(LOGISK_ADRESS));
        verify(message, times(1)).setHeader(eq(RouteHeaders.INTYGS_ID), eq(INTYGS_ID));
        verify(message, times(1)).setHeader(eq(RouteHeaders.HANDELSE), eq(HandelseType.INTYGSUTKAST_SKAPAT.value()));
        verify(message, times(1)).setHeader(eq(RouteHeaders.VERSION), eq(SchemaVersion.VERSION_1.name()));
        verify(internalToNotification, times(1)).createCertificateStatusUpdateForCareType(any());
    }

    @Test
    public void testSchemaVersion2Transformation() throws Exception {
        NotificationMessage notificationMessage = new NotificationMessage(INTYGS_ID, LUSE, new LocalDateTime(),
                HandelseType.INTYGSUTKAST_SKAPAT, LOGISK_ADRESS, "{ }", FragorOchSvar.getEmpty(), SchemaVersion.VERSION_2);
        Message message = spy(new DefaultMessage());
        message.setBody(notificationMessage);

        ModuleApi moduleApi = mock(ModuleApi.class);
        when(moduleRegistry.getModuleApi(eq(LUSE))).thenReturn(moduleApi);
        Intyg intyg = new Intyg();
        IntygId intygsId = new IntygId();
        intygsId.setExtension(INTYGS_ID);
        intyg.setIntygsId(intygsId);
        HosPersonal hosPersonal = new HosPersonal();
        Enhet enhet = new Enhet();
        enhet.setArbetsplatskod(new ArbetsplatsKod());
        enhet.setVardgivare(new Vardgivare());
        hosPersonal.setEnhet(enhet);
        intyg.setSkapadAv(hosPersonal);

        when(moduleApi.getIntygFromUtlatande(any())).thenReturn(intyg);

        transformer.process(message);

        assertEquals(INTYGS_ID,
                ((se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v2.CertificateStatusUpdateForCareType) message
                        .getBody()).getIntyg().getIntygsId().getExtension());
        assertEquals(HandelseType.INTYGSUTKAST_SKAPAT.value(), message.getHeader(RouteHeaders.HANDELSE));
        assertEquals(INTYGS_ID, message.getHeader(RouteHeaders.INTYGS_ID));
        assertEquals(LOGISK_ADRESS, message.getHeader(RouteHeaders.LOGISK_ADRESS));
        assertEquals(SchemaVersion.VERSION_2.name(), message.getHeader(RouteHeaders.VERSION));

        verify(message, times(1)).setHeader(eq(RouteHeaders.LOGISK_ADRESS), eq(LOGISK_ADRESS));
        verify(message, times(1)).setHeader(eq(RouteHeaders.INTYGS_ID), eq(INTYGS_ID));
        verify(message, times(1)).setHeader(eq(RouteHeaders.HANDELSE), eq(HandelseType.INTYGSUTKAST_SKAPAT.value()));
        verify(message, times(1)).setHeader(eq(RouteHeaders.VERSION), eq(SchemaVersion.VERSION_2.name()));
        verify(moduleRegistry, times(1)).getModuleApi(eq(LUSE));
        verify(moduleApi, times(1)).getUtlatandeFromJson(any());
        verify(moduleApi, times(1)).getIntygFromUtlatande(any());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSituationanpassatCertificateOnSchemaVersion1() throws Exception {
        NotificationMessage notificationMessage = new NotificationMessage(INTYGS_ID, LUSE, new LocalDateTime(),
                HandelseType.INTYGSUTKAST_SKAPAT, LOGISK_ADRESS, "{ }", FragorOchSvar.getEmpty(), SchemaVersion.VERSION_1);
        Message message = new DefaultMessage();
        message.setBody(notificationMessage);
        transformer.process(message);
    }

    private void setupInternalToNotification() throws ModuleException {
        when(internalToNotification.createCertificateStatusUpdateForCareType(any())).thenAnswer(invocation -> {
            NotificationMessage msg = (NotificationMessage) invocation.getArguments()[0];
            if (msg == null) {
                return null;
            }
            CertificateStatusUpdateForCareType request = new CertificateStatusUpdateForCareType();
            UtlatandeType utlatande = new UtlatandeType();
            UtlatandeId utlatandeId = new UtlatandeId();
            utlatandeId.setExtension(msg.getIntygsId());
            utlatande.setUtlatandeId(utlatandeId);
            request.setUtlatande(utlatande);
            return request;
        });
    }
}
