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
package se.inera.intyg.webcert.notification_sender.notifications.integration;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.notification_sender.mocks.v1.CertificateStatusUpdateForCareResponderStub.FALLERAT_MEDDELANDE;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.jms.Queue;
import javax.jms.TextMessage;

import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;

import se.inera.intyg.common.fk7263.model.converter.Fk7263InternalToNotification;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.modules.converter.InternalConverterUtil;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.modules.support.api.notification.ArendeCount;
import se.inera.intyg.common.support.modules.support.api.notification.FragorOchSvar;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.notification_sender.mocks.NotificationStubEntry;
import se.inera.intyg.webcert.notification_sender.mocks.v1.CertificateStatusUpdateForCareResponderStub;
import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.UtlatandeType;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.UtlatandeId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.ArbetsplatsKod;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PartialDateTypeFormatEnum;
import se.riv.clinicalprocess.healthcond.certificate.v3.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.v3.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.Vardgivare;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration("/notifications/integration-test-notification-sender-config.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RouteIT {

    private static final int SECONDS_TO_WAIT = 20;
    private static final String INTYG_JSON = "{\"id\":\"1234\",\"typ\":\"fk7263\"}";

    @Autowired
    private IntygModuleRegistry mockIntygModuleRegistry;

    @Autowired
    private Fk7263InternalToNotification mockFk7263Transform;

    @Autowired
    private ModuleApi fk7263ModuleApi;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    @Qualifier("notificationQueueForAggregation")
    private Queue sendQueue;

    @Autowired
    private CertificateStatusUpdateForCareResponderStub certificateStatusUpdateForCareResponderStub;

    @Autowired
    private se.inera.intyg.webcert.notification_sender.mocks.v3.CertificateStatusUpdateForCareResponderStub certificateStatusUpdateForCareResponderV3;

    private ObjectMapper objectMapper = new CustomObjectMapper();

    @Before
    public void init() throws Exception {
        when(fk7263ModuleApi.getIntygFromUtlatande(any())).thenReturn(createIntyg());
        when(mockIntygModuleRegistry.getModuleApi(anyString())).thenReturn(fk7263ModuleApi);

        certificateStatusUpdateForCareResponderStub.reset();
        certificateStatusUpdateForCareResponderV3.reset();
        setupConverter();
    }

    @Test
    public void ensureAggregatorFiltersOutOldestAndratMessages() throws Exception {
        LocalDateTime first = LocalDateTime.now().minusSeconds(15);
        LocalDateTime second = LocalDateTime.now().minusSeconds(10);
        LocalDateTime third = LocalDateTime.now().minusSeconds(5);

        NotificationMessage notificationMessage1 = createNotificationMessage("intyg1", LocalDateTime.now(), HandelsekodEnum.SKAPAT,
                "luae_fs", SchemaVersion.VERSION_3);
        NotificationMessage notificationMessage2 = createNotificationMessage("intyg1", first, HandelsekodEnum.ANDRAT, "luae_fs",
                SchemaVersion.VERSION_3);
        NotificationMessage notificationMessage3 = createNotificationMessage("intyg1", second, HandelsekodEnum.ANDRAT, "luae_fs",
                SchemaVersion.VERSION_3);
        NotificationMessage notificationMessage4 = createNotificationMessage("intyg1", third, HandelsekodEnum.ANDRAT, "luae_fs",
                SchemaVersion.VERSION_3);

        sendMessage(notificationMessage1);
        sendMessage(notificationMessage2);
        sendMessage(notificationMessage3);
        sendMessage(notificationMessage4);

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(() -> {
            int numberOfReceivedMessages = certificateStatusUpdateForCareResponderV3.getNumberOfReceivedMessages();
            if (numberOfReceivedMessages == 2) {
                List<NotificationStubEntry> notificationMessages = certificateStatusUpdateForCareResponderV3.getNotificationMessages();
                for (NotificationStubEntry nse : notificationMessages) {
                    if (nse.handelseTyp.equals(HandelsekodEnum.ANDRAT.value())) {
                        assertEquals(third, nse.handelseTid);
                    }
                }
            }
            return (numberOfReceivedMessages == 2);
        });
    }

    @Test
    public void ensureWiretapWorks() throws Exception {
        LocalDateTime first = LocalDateTime.now().minusSeconds(15);
        LocalDateTime second = LocalDateTime.now().minusSeconds(10);

        NotificationMessage notificationMessage2 = createNotificationMessage("intyg1", first, HandelsekodEnum.ANDRAT, "luae_fs",
                SchemaVersion.VERSION_3);
        NotificationMessage notificationMessage3 = createNotificationMessage("intyg1", second, HandelsekodEnum.SIGNAT, "luae_fs",
                SchemaVersion.VERSION_3);

        sendMessage(notificationMessage2);
        sendMessage(notificationMessage3);

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(() -> {
            int numberOfReceivedMessages = certificateStatusUpdateForCareResponderV3.getNumberOfReceivedMessages();
            if (numberOfReceivedMessages == 1) {
                List<NotificationStubEntry> notificationMessages = certificateStatusUpdateForCareResponderV3.getNotificationMessages();
                for (NotificationStubEntry nse : notificationMessages) {
                    if (nse.handelseTyp.equals(HandelsekodEnum.SIGNAT.value())) {
                        assertEquals(second, nse.handelseTid);
                    }
                }
            }
            return (numberOfReceivedMessages == 1);
        });
    }

    @Test
    public void ensureAggregatorFiltersOutAndratMessagesWhenSigned() throws Exception {

        NotificationMessage notificationMessage1 = createNotificationMessage("intyg1", LocalDateTime.now(), HandelsekodEnum.SKAPAT,
                "luae_fs", SchemaVersion.VERSION_3);
        NotificationMessage notificationMessage2 = createNotificationMessage("intyg1", LocalDateTime.now(), HandelsekodEnum.ANDRAT,
                "luae_fs", SchemaVersion.VERSION_3);
        NotificationMessage notificationMessage3 = createNotificationMessage("intyg1", LocalDateTime.now(), HandelsekodEnum.ANDRAT,
                "luae_fs", SchemaVersion.VERSION_3);
        NotificationMessage notificationMessage4 = createNotificationMessage("intyg1", LocalDateTime.now(), HandelsekodEnum.SIGNAT,
                "luae_fs", SchemaVersion.VERSION_3);

        sendMessage(notificationMessage1);
        sendMessage(notificationMessage2);
        sendMessage(notificationMessage3);
        sendMessage(notificationMessage4);

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(() -> {
            int numberOfReceivedMessages = certificateStatusUpdateForCareResponderV3.getNumberOfReceivedMessages();
            if (numberOfReceivedMessages == 2) {
                List<NotificationStubEntry> notificationMessages = certificateStatusUpdateForCareResponderV3.getNotificationMessages();
                for (NotificationStubEntry nse : notificationMessages) {
                    if (nse.handelseTyp.equals(HandelsekodEnum.ANDRAT.value())) {
                        fail("No ANDRAT messages are allowed when intyg has been signed");
                    }
                }
            }
            return (numberOfReceivedMessages == 2);
        });
    }

    @Test
    public void ensureRouting() throws Exception {
        // 2 messages
        NotificationMessage luaefs1 = createNotificationMessage("intyg1", LocalDateTime.now(), HandelsekodEnum.SKAPAT, "luae_fs",
                SchemaVersion.VERSION_3);
        NotificationMessage luaefs2 = createNotificationMessage("intyg1", LocalDateTime.now(), HandelsekodEnum.ANDRAT, "luae_fs",
                SchemaVersion.VERSION_3);
        NotificationMessage luaefs3 = createNotificationMessage("intyg1", LocalDateTime.now(), HandelsekodEnum.ANDRAT, "luae_fs",
                SchemaVersion.VERSION_3);

        // 3 messages
        NotificationMessage fk1 = createNotificationMessage("intyg2", HandelsekodEnum.SKAPAT);
        NotificationMessage fk2 = createNotificationMessage("intyg2", HandelsekodEnum.ANDRAT);
        NotificationMessage fk3 = createNotificationMessage("intyg2", HandelsekodEnum.SIGNAT);

        // 2 messages
        NotificationMessage luaefs4 = createNotificationMessage("intyg3", LocalDateTime.now(), HandelsekodEnum.MAKULE, "luae_fs",
                SchemaVersion.VERSION_3);
        NotificationMessage luaefs5 = createNotificationMessage("intyg4", LocalDateTime.now(), HandelsekodEnum.SKICKA, "luae_fs",
                SchemaVersion.VERSION_3);

        sendMessage(luaefs1);
        sendMessage(fk1);
        sendMessage(luaefs2);
        sendMessage(luaefs3);
        sendMessage(fk2);
        sendMessage(luaefs4);
        sendMessage(fk3);
        sendMessage(luaefs5);

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(() -> {
            int numberOfReceivedMessagesV1 = certificateStatusUpdateForCareResponderStub.getNumberOfReceivedMessages();
            int numberOfReceivedMessagesV3 = certificateStatusUpdateForCareResponderV3.getNumberOfReceivedMessages();
            return (numberOfReceivedMessagesV1 == 3 && numberOfReceivedMessagesV3 == 4);
        });
    }

    @Test
    public void ensureStubReceivedAllMessages() throws Exception {
        NotificationMessage notificationMessage1 = createNotificationMessage("intyg1", HandelsekodEnum.SKAPAT);
        NotificationMessage notificationMessage2 = createNotificationMessage("intyg2", HandelsekodEnum.ANDRAT);
        NotificationMessage notificationMessage3 = createNotificationMessage("intyg3", HandelsekodEnum.SIGNAT);

        sendMessage(notificationMessage1);
        sendMessage(notificationMessage2);
        sendMessage(notificationMessage3);

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(() -> {
            int numberOfReceivedMessages = certificateStatusUpdateForCareResponderStub.getNumberOfReceivedMessages();
            return (numberOfReceivedMessages == 3);
        });
    }

    @Test
    public void ensureMessagesAreResentAndDoNotBlockEachOther() throws Exception {
        final String intygsId1 = FALLERAT_MEDDELANDE + "2";
        final String intygsId2 = "korrekt-meddelande-1";
        NotificationMessage notificationMessage1 = createNotificationMessage(intygsId1, HandelsekodEnum.SKAPAT);
        NotificationMessage notificationMessage2 = createNotificationMessage(intygsId2, HandelsekodEnum.ANDRAT);

        sendMessage(notificationMessage1);
        sendMessage(notificationMessage2);

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(() -> {
            int numberOfSuccessfulMessages = certificateStatusUpdateForCareResponderStub.getNumberOfSentMessages();
            if (numberOfSuccessfulMessages == 2) {
                List<String> utlatandeIds = certificateStatusUpdateForCareResponderStub.getIntygsIdsInOrder();
                System.err.println(utlatandeIds);
                return (utlatandeIds.size() == 2
                        && utlatandeIds.get(0).equals(intygsId2)
                        && utlatandeIds.get(1).equals(intygsId1));
            }
            return false;
        });
    }

    private NotificationMessage createNotificationMessage(String intygsId1, HandelsekodEnum handelseType) {
        return createNotificationMessage(intygsId1, LocalDateTime.now(), handelseType, "fk7263", SchemaVersion.VERSION_1);
    }

    private NotificationMessage createNotificationMessage(String intygsId, LocalDateTime handelseTid, HandelsekodEnum handelseType,
            String intygsTyp,
            SchemaVersion schemaVersion) {
        if (SchemaVersion.VERSION_3 == schemaVersion) {
            return new NotificationMessage(intygsId, intygsTyp, handelseTid, handelseType, "address2", INTYG_JSON, null,
                    ArendeCount.getEmpty(), ArendeCount.getEmpty(),
                    schemaVersion, "ref");
        } else {
            return new NotificationMessage(intygsId, intygsTyp, handelseTid, handelseType, "address2", INTYG_JSON, FragorOchSvar.getEmpty(),
                    null, null, schemaVersion, "ref");
        }
    }

    private String notificationMessageToJson(NotificationMessage notificationMessage) throws Exception {
        return objectMapper.writeValueAsString(notificationMessage);
    }

    private void setupConverter() throws ModuleException {
        when(mockFk7263Transform.createCertificateStatusUpdateForCareType(any(NotificationMessage.class))).thenAnswer(invocation -> {
            NotificationMessage msg = (NotificationMessage) invocation.getArguments()[0];
            if (msg == null) {
                return null;
            }
            CertificateStatusUpdateForCareType request = new CertificateStatusUpdateForCareType();
            UtlatandeType utlatande = new UtlatandeType();
            UtlatandeId id = new UtlatandeId();
            id.setExtension(msg.getIntygsId());
            utlatande.setUtlatandeId(id);
            request.setUtlatande(utlatande);
            return request;
        });

    }

    private void sendMessage(final NotificationMessage message) throws Exception {
        jmsTemplate.send(sendQueue, session -> {
            try {
                TextMessage textMessage = session.createTextMessage(notificationMessageToJson(message));
                textMessage.setStringProperty(NotificationRouteHeaders.INTYGS_TYP, message.getIntygsTyp());
                textMessage.setStringProperty(NotificationRouteHeaders.HANDELSE, message.getHandelse().value());
                return textMessage;
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        });
    }

    private Intyg createIntyg() {
        Intyg intyg = new Intyg();
        IntygId intygId = new IntygId();
        intygId.setExtension("intyg1");
        intyg.setIntygsId(intygId);
        HosPersonal hosPersonal = new HosPersonal();
        Enhet enhet = new Enhet();
        enhet.setVardgivare(new Vardgivare());
        enhet.setArbetsplatskod(new ArbetsplatsKod());
        hosPersonal.setEnhet(enhet);
        intyg.setSkapadAv(hosPersonal);
        // DatePeriodType and PartialDateType must be allowed
        intyg.getSvar().add(InternalConverterUtil.aSvar("")
                .withDelsvar("", InternalConverterUtil.aDatePeriod(LocalDate.now(), LocalDate.now().plusDays(1)))
                .withDelsvar("", InternalConverterUtil.aPartialDate(PartialDateTypeFormatEnum.YYYY, Year.of(1999)))
                .build());
        return intyg;
    }
}
