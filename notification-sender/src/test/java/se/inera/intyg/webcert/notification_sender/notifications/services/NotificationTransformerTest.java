/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NotificationTransformerTest {
//
//    private static final String INTYGS_ID = "intyg1";
//    private static final String LOGISK_ADRESS = "address1";
//    private static final String FK7263 = Fk7263EntryPoint.MODULE_ID;
//    private static final String LUSE = "luse";
//
//    private CamelContext camelContext = new DefaultCamelContext();
//
//    @Mock
//    private IntygModuleRegistry moduleRegistry;
//
//    @Mock
//    private NotificationPatientEnricher notificationPatientEnricher;
//
//    @Mock
//    private FeaturesHelper featuresHelper;
//
//    @Mock
//    private NotificationResultMessageCreator notificationResultMessageCreator;
//
//    @Mock
//    private NotificationResultMessageSender notificationResultMessageSender;
//
//    @InjectMocks
//    private NotificationTransformer transformer;
//
//    @Test(expected = IllegalArgumentException.class)
//    public void testSendVersion1ThrowsException() throws Exception {
//        // Given
//        NotificationMessage notificationMessage = new NotificationMessage(INTYGS_ID, FK7263, LocalDateTime.now(), HandelsekodEnum.SKAPAT,
//            LOGISK_ADRESS, "{ }", FragorOchSvar.getEmpty(), null, null, SchemaVersion.VERSION_1, "ref");
//        Message message = spy(new DefaultMessage(camelContext));
//        message.setBody(notificationMessage);
//
//        // When
//        try {
//            transformer.process(message);
//        } finally {
//            verifyNoInteractions(notificationPatientEnricher);
//        }
//    }
//
//    @Test
//    public void testSchemaVersion2Transformation() throws Exception {
//        NotificationMessage notificationMessage = new NotificationMessage(INTYGS_ID, LUSE, LocalDateTime.now(), HandelsekodEnum.SKAPAT,
//            LOGISK_ADRESS, "{ }", null, ArendeCount.getEmpty(), ArendeCount.getEmpty(), SchemaVersion.VERSION_3, "ref");
//        Message message = spy(new DefaultMessage(camelContext));
//        message.setBody(notificationMessage);
//
//        ModuleApi moduleApi = mock(ModuleApi.class);
//        when(moduleRegistry.getModuleApi(eq(LUSE), eq("1.0"))).thenReturn(moduleApi);
//        when(moduleRegistry.resolveVersionFromUtlatandeJson(anyString(), anyString())).thenReturn("1.0");
//        Intyg intyg = new Intyg();
//        IntygId intygsId = new IntygId();
//        intygsId.setExtension(INTYGS_ID);
//        intyg.setIntygsId(intygsId);
//        HosPersonal hosPersonal = new HosPersonal();
//        Enhet enhet = new Enhet();
//        enhet.setArbetsplatskod(new ArbetsplatsKod());
//        enhet.setVardgivare(new Vardgivare());
//        hosPersonal.setEnhet(enhet);
//        intyg.setSkapadAv(hosPersonal);
//
//        when(moduleApi.getIntygFromUtlatande(any())).thenReturn(intyg);
//
//        transformer.process(message);
//
//        assertEquals(INTYGS_ID,
//            ((se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType) message
//                .getBody()).getIntyg().getIntygsId().getExtension());
//        assertEquals(HandelsekodEnum.SKAPAT.value(), message.getHeader(NotificationRouteHeaders.HANDELSE));
//        assertEquals(INTYGS_ID, message.getHeader(NotificationRouteHeaders.INTYGS_ID));
//        assertEquals(LOGISK_ADRESS, message.getHeader(NotificationRouteHeaders.LOGISK_ADRESS));
//        assertEquals(SchemaVersion.VERSION_3.name(), message.getHeader(NotificationRouteHeaders.VERSION));
//
//        verify(message, times(1)).setHeader(eq(NotificationRouteHeaders.LOGISK_ADRESS), eq(LOGISK_ADRESS));
//        verify(message, times(1)).setHeader(eq(NotificationRouteHeaders.INTYGS_ID), eq(INTYGS_ID));
//        verify(message, times(1)).setHeader(eq(NotificationRouteHeaders.HANDELSE), eq(HandelsekodEnum.SKAPAT.value()));
//        verify(message, times(1)).setHeader(eq(NotificationRouteHeaders.VERSION), eq(SchemaVersion.VERSION_3.name()));
//        verify(moduleRegistry, times(1)).getModuleApi(eq(LUSE), eq("1.0"));
//        verify(moduleApi, times(1)).getUtlatandeFromJson(any());
//        verify(moduleApi, times(1)).getIntygFromUtlatande(any());
//        verify(notificationPatientEnricher, times(1)).enrichWithPatient(any());
//    }
//
//    @Test(expected = IllegalArgumentException.class)
//    public void testSituationanpassatCertificateOnSchemaVersion1() throws Exception {
//        NotificationMessage notificationMessage = new NotificationMessage(INTYGS_ID, LUSE, LocalDateTime.now(), HandelsekodEnum.SKAPAT,
//            LOGISK_ADRESS, "{ }", FragorOchSvar.getEmpty(), null, null, SchemaVersion.VERSION_1, "ref");
//        Message message = new DefaultMessage(camelContext);
//        message.setBody(notificationMessage);
//        transformer.process(message);
//        verifyNoInteractions(notificationPatientEnricher);
//    }
//
//    @Test
//    public void messageToPostProcessorWhenWebcertMessagingFeatureIsOn() throws Exception {
//        NotificationMessage notificationMessage = new NotificationMessage(INTYGS_ID, LUSE, LocalDateTime.now(), HandelsekodEnum.SKAPAT,
//            LOGISK_ADRESS, "{ }", FragorOchSvar.getEmpty(), null, null, SchemaVersion.VERSION_3, "ref");
//        final var message = new DefaultMessage(camelContext);
//        message.setBody(notificationMessage);
//
//        final var utlatande = getUtlatandeMock();
//        final var exception = new RuntimeException();
//        final var moduleApiMock = mock(ModuleApi.class);
//        doReturn("1.0").when(moduleRegistry).resolveVersionFromUtlatandeJson(anyString(), anyString());
//        doReturn(moduleApiMock).when(moduleRegistry).getModuleApi(LUSE, "1.0");
//        doReturn(utlatande).when(moduleApiMock).getUtlatandeFromJson(anyString());
//        doThrow(exception).when(moduleApiMock).getIntygFromUtlatande(any(Utlatande.class));
//        doReturn(true).when(featuresHelper).isFeatureActive(AuthoritiesConstants.FEATURE_USE_WEBCERT_MESSAGING);
//        doReturn(mock(NotificationResultMessage.class)).when(notificationResultMessageCreator)
//            .createFailureMessage(any(), any(), eq(notificationMessage), eq(utlatande), eq(exception));
//
//        try {
//            transformer.process(message);
//            assertTrue("No exception was thrown and this line should not be reached.", false);
//        } catch (Exception ex) {
//            verify(notificationResultMessageSender).sendResultMessage(any());
//        }
//    }
//
//    @Test
//    public void noMessageToPostProcessorWhenWebcertMessagingFeatureIsOff() throws Exception {
//        NotificationMessage notificationMessage = new NotificationMessage(INTYGS_ID, LUSE, LocalDateTime.now(), HandelsekodEnum.SKAPAT,
//            LOGISK_ADRESS, "{ }", FragorOchSvar.getEmpty(), null, null, SchemaVersion.VERSION_3, "ref");
//        final var message = new DefaultMessage(camelContext);
//        message.setBody(notificationMessage);
//
//        final var moduleApiMock = mock(ModuleApi.class);
//        doReturn("1.0").when(moduleRegistry).resolveVersionFromUtlatandeJson(anyString(), anyString());
//        doReturn(moduleApiMock).when(moduleRegistry).getModuleApi(LUSE, "1.0");
//        doThrow(new NullPointerException()).when(moduleApiMock).getUtlatandeFromJson(anyString());
//        doReturn(false).when(featuresHelper).isFeatureActive(AuthoritiesConstants.FEATURE_USE_WEBCERT_MESSAGING);
//
//        try {
//            transformer.process(message);
//            assertTrue("No exception was thrown and this line should not be reached.", false);
//        } catch (Exception ex) {
//            verifyNoInteractions(notificationResultMessageSender);
//        }
//    }
//
//    @Test
//    public void noMessageToPostProcessorWhenWebcertMessagingFeatureIsOnAndTemporaryException() throws Exception {
//        NotificationMessage notificationMessage = new NotificationMessage(INTYGS_ID, LUSE, LocalDateTime.now(), HandelsekodEnum.SKAPAT,
//            LOGISK_ADRESS, "{ }", FragorOchSvar.getEmpty(), null, null, SchemaVersion.VERSION_3, "ref");
//        final var message = new DefaultMessage(camelContext);
//        message.setBody(notificationMessage);
//
//        final var moduleApiMock = mock(ModuleApi.class);
//        final var utlatandeMock = getUtlatandeMock();
//        final var intygMock = mock(Intyg.class);
//        doReturn("1.0").when(moduleRegistry).resolveVersionFromUtlatandeJson(anyString(), anyString());
//        doReturn(moduleApiMock).when(moduleRegistry).getModuleApi(LUSE, "1.0");
//        doReturn(utlatandeMock).when(moduleApiMock).getUtlatandeFromJson(anyString());
//        doReturn(intygMock).when(moduleApiMock).getIntygFromUtlatande(utlatandeMock);
//        doThrow(new TemporaryException("")).when(notificationPatientEnricher).enrichWithPatient(any(Intyg.class));
//
//        try {
//            transformer.process(message);
//            assertTrue("No exception was thrown and this line should not be reached.", false);
//        } catch (Exception ex) {
//            verifyNoInteractions(notificationResultMessageSender);
//            verifyNoInteractions(featuresHelper);
//        }
//    }
//
//    private Utlatande getUtlatandeMock() {
//        return mock(Utlatande.class);
//    }
}
