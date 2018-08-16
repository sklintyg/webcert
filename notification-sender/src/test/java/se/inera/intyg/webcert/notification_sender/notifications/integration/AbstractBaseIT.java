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
package se.inera.intyg.webcert.notification_sender.notifications.integration;

import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.annotation.DirtiesContext;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.TextMessage;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import se.inera.intyg.common.fk7263.model.internal.Fk7263Utlatande;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.notification.ArendeCount;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.notification_sender.mocks.v3.CertificateStatusUpdateForCareResponderStub;
import se.inera.intyg.webcert.notification_sender.notifications.helper.NotificationTestHelper;
import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class AbstractBaseIT {

    protected static final int SECONDS_TO_WAIT = 20;
    protected static final String INTYG_JSON = "{\"id\":\"1234\",\"typ\":\"fk7263\"}";

    @Autowired
    protected ConfigurableApplicationContext applicationContext;

    @Autowired
    protected IntygModuleRegistry mockIntygModuleRegistry;

    @Autowired
    protected ModuleApi fk7263ModuleApi;

    @Autowired
    protected JmsTemplate jmsTemplate;

    @Autowired
    @Qualifier("notificationQueueForAggregation")
    protected Queue sendQueue;

    @Autowired
    protected ActiveMQConnectionFactory activeMQConnectionFactory;

    @Autowired
    protected CertificateStatusUpdateForCareResponderStub certificateStatusUpdateForCareResponderV3;

    protected ObjectMapper objectMapper = new CustomObjectMapper();

    @Before
    public void init() throws Exception {
        when(fk7263ModuleApi.getIntygFromUtlatande(any())).thenReturn(NotificationTestHelper.createIntyg("fk7263"));
        when(fk7263ModuleApi.getUtlatandeFromJson(anyString())).thenReturn(new Fk7263Utlatande());
        when(mockIntygModuleRegistry.getModuleApi(anyString())).thenReturn(fk7263ModuleApi);

        certificateStatusUpdateForCareResponderV3.reset();
    }

    NotificationMessage createNotificationMessage(String intygsId1, String intygsTyp, HandelsekodEnum handelseType) {
        return createNotificationMessage(intygsId1, LocalDateTime.now(), handelseType, intygsTyp, SchemaVersion.VERSION_3);
    }

    NotificationMessage createNotificationMessage(String intygsId, LocalDateTime handelseTid, HandelsekodEnum handelseType,
                                                  String intygsTyp,
                                                  SchemaVersion schemaVersion) {
        if (SchemaVersion.VERSION_3 == schemaVersion) {
            return new NotificationMessage(intygsId, intygsTyp, handelseTid, handelseType, "address2", INTYG_JSON, null,
                    ArendeCount.getEmpty(), ArendeCount.getEmpty(),
                    schemaVersion, "ref");
        } else {
            throw new IllegalArgumentException("SchemaVersion 1 not supported anymore.");
            // return new NotificationMessage(intygsId, intygsTyp, handelseTid, handelseType, "address2", INTYG_JSON,
            //                                FragorOchSvar.getEmpty(), null, null, schemaVersion, "ref");
        }
    }

    private String notificationMessageToJson(NotificationMessage notificationMessage) throws Exception {
        return objectMapper.writeValueAsString(notificationMessage);
    }

    void sendMessage(final NotificationMessage message) throws Exception {
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

    private List<String> getQueueNames(Set<ActiveMQQueue> queues) {
        return queues.stream().map(queue -> {
            String name;
            try {
                name = queue.getQueueName();
            } catch (JMSException e) {
                e.printStackTrace();
                name = null;
            }
            return name;
        }).collect(toList());
    }

    List<Pair<String, Integer>> getAmqStatus(Set<ActiveMQQueue> queues) {
        return getQueueNames(queues).stream()
                .map(queue -> jmsTemplate.browse(queue, (session, browser) -> {
                    AtomicInteger nbrMessages = new AtomicInteger(0);
                    Enumeration<?> messages = browser.getEnumeration();
                    while (messages.hasMoreElements()) {
                        messages.nextElement();
                        nbrMessages.incrementAndGet();
                    }
                    return Pair.of(queue, nbrMessages.get());
                })).collect(toList());
    }
}
