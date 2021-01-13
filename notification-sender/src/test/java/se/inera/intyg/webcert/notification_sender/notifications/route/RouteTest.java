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
package se.inera.intyg.webcert.notification_sender.notifications.route;

import static org.apache.camel.component.mock.MockEndpoint.assertIsSatisfied;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.INTYG_TYPE_VERSION;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Message;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultMessage;
import org.apache.camel.test.spring.CamelSpringRunner;
import org.apache.camel.test.spring.CamelTestContextBootstrapper;
import org.apache.camel.test.spring.MockEndpointsAndSkip;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;

import se.inera.intyg.common.fk7263.model.internal.Fk7263Utlatande;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.infra.integration.pu.services.PUService;
import se.inera.intyg.webcert.common.sender.exception.PermanentException;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.inera.intyg.webcert.notification_sender.notifications.helper.NotificationTestHelper;
import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders;

@Ignore("This test is unstable, INTYGFV-12301")
@RunWith(CamelSpringRunner.class)
@ContextConfiguration("/notifications/unit-test-notification-sender-config.xml")
@BootstrapWith(CamelTestContextBootstrapper.class)
@MockEndpointsAndSkip("bean:notificationAggregator|direct:signatWireTap|bean:notificationWSClientV3|direct:permanentErrorHandlerEndpoint|direct:temporaryErrorHandlerEndpoint")
public class RouteTest {

    private static int currentId = 1000;

    @Autowired
    CamelContext camelContext;

    @Mock
    private ModuleApi moduleApi; // this is a mock from unit-test-notification-sender-config.xml

    @Autowired
    private PUService mockedPuService;

    @Autowired
    private StringRedisTemplate mockedStringRedisTemplate;

    @Autowired
    private IntygModuleRegistry moduleRegistry; // this is a mock from unit-test-notification-sender-config.xml

    @Produce(uri = "direct:receiveNotificationForAggregationRequestEndpoint")
    protected ProducerTemplate producerTemplate;
    @EndpointInject(uri = "mock:bean:notificationAggregator")
    protected MockEndpoint notificationAggregator;
    @EndpointInject(uri = "mock:direct:signatWireTap")
    protected MockEndpoint signatWireTap;
    @EndpointInject(uri = "mock:bean:notificationWSClientV3")
    protected MockEndpoint notificationWSClientV3;
    @EndpointInject(uri = "mock:direct:permanentErrorHandlerEndpoint")
    protected MockEndpoint permanentErrorHandlerEndpoint;
    @EndpointInject(uri = "mock:direct:temporaryErrorHandlerEndpoint")
    protected MockEndpoint temporaryErrorHandlerEndpoint;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        MockEndpoint.resetMocks(camelContext);
        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
        when(moduleRegistry.resolveVersionFromUtlatandeJson(anyString(), anyString())).thenReturn("1.0");
        when(moduleApi.getUtlatandeFromJson(anyString())).thenReturn(new Fk7263Utlatande());
        when(moduleApi.getIntygFromUtlatande(any())).thenReturn(NotificationTestHelper.createIntyg("fk7263"));
        when(mockedPuService.getPerson(any())).thenReturn(PersonSvar.found(NotificationTestHelper.buildPerson(false)));
    }

    @After
    public void cleanup() {
        Mockito.reset(moduleRegistry, moduleApi, mockedPuService);
    }

    @Test
    public void testWiretappingOfSignedMessages() throws ModuleException, InterruptedException {
        notificationAggregator.whenAnyExchangeReceived(exchange -> {
            Message msg = new DefaultMessage(camelContext);
            exchange.setMessage(msg);
        });

        // Given
        when(moduleApi.getIntygFromUtlatande(any())).thenReturn(NotificationTestHelper.createIntyg("luae_fs"));

        notificationWSClientV3.expectedMessageCount(0);
        notificationAggregator.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);
        signatWireTap.expectedMessageCount(1);

        // When
        Map<String, Object> headers = new HashMap<>();
        headers.put(NotificationRouteHeaders.INTYGS_TYP, "luae_fs");
        headers.put(INTYG_TYPE_VERSION, "1.0");
        headers.put(NotificationRouteHeaders.HANDELSE, HandelsekodEnum.SIGNAT.value());
        producerTemplate.sendBodyAndHeaders(createNotificationMessage(SchemaVersion.VERSION_3, "luae_fs"), headers);

        // Then
        assertIsSatisfied(notificationWSClientV3);
        assertIsSatisfied(notificationAggregator);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
        assertIsSatisfied(signatWireTap);
    }

    @Test
    public void testRoutesDirectlyToNotificationQueueForFK7263Andrat() throws ModuleException, InterruptedException {
        // Given
        when(moduleApi.getIntygFromUtlatande(any())).thenReturn(NotificationTestHelper.createIntyg("fk7263"));
        notificationWSClientV3.expectedMessageCount(1);
        notificationAggregator.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);
        signatWireTap.expectedMessageCount(0);

        // When
        Map<String, Object> headers = new HashMap<>();
        headers.put(NotificationRouteHeaders.INTYGS_TYP, "fk7263");
        headers.put(INTYG_TYPE_VERSION, "1.0");
        headers.put(NotificationRouteHeaders.HANDELSE, HandelsekodEnum.ANDRAT.value());
        producerTemplate.sendBodyAndHeaders(createNotificationMessage(SchemaVersion.VERSION_3), headers);

        // Then
        assertIsSatisfied(notificationWSClientV3);
        assertIsSatisfied(notificationAggregator);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
        assertIsSatisfied(signatWireTap);
    }

    @Test
    public void testRoutesToAggregatorForLuaeFsAndrat() throws InterruptedException {
        // This is just a hack in order to not fail subsequent tests due to camel unit tests leaking context between
        // tests. This one just makes sure the notficationAggregator doesn't cause the subsequent "split(body())"
        // to throw some exception caught by later tests.

        notificationAggregator.whenAnyExchangeReceived(exchange -> {
            Message msg = new DefaultMessage(camelContext);
            exchange.setMessage(msg);
        });

        // Given
        notificationWSClientV3.expectedMessageCount(0);
        notificationAggregator.expectedMessageCount(1);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);
        signatWireTap.expectedMessageCount(1);

        // When
        Map<String, Object> headers = new HashMap<>();
        headers.put(NotificationRouteHeaders.INTYGS_TYP, "luae_fs");
        headers.put(INTYG_TYPE_VERSION, "1.0");
        headers.put(NotificationRouteHeaders.HANDELSE, HandelsekodEnum.ANDRAT.value());
        producerTemplate.sendBodyAndHeaders(createNotificationMessage(SchemaVersion.VERSION_3, "luae_fs"), headers);

        // Then
        assertIsSatisfied(notificationWSClientV3);
        assertIsSatisfied(notificationAggregator);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
        assertIsSatisfied(signatWireTap);
    }

    @Test
    public void testRoutesDirectlyToNotificationQueueForLuaeFsSkapad() throws InterruptedException, ModuleException {
        // Given
        when(moduleApi.getIntygFromUtlatande(any())).thenReturn(NotificationTestHelper.createIntyg("luae_fs"));
        notificationWSClientV3.expectedMessageCount(1);
        notificationAggregator.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);
        signatWireTap.expectedMessageCount(0);

        // When
        Map<String, Object> headers = new HashMap<>();
        headers.put(NotificationRouteHeaders.INTYGS_TYP, "luae_fs");
        headers.put(INTYG_TYPE_VERSION, "1.0");
        headers.put(NotificationRouteHeaders.HANDELSE, HandelsekodEnum.SKAPAT.value());
        producerTemplate.sendBodyAndHeaders(createNotificationMessage(SchemaVersion.VERSION_3, "luae_fs"), headers);

        // Then
        assertIsSatisfied(notificationWSClientV3);
        assertIsSatisfied(notificationAggregator);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
        assertIsSatisfied(signatWireTap);
    }

    @Test
    public void testRoutesDirectlyToNotificationQueueForLuaeFsSkickad() throws InterruptedException {
        // Given
        notificationWSClientV3.expectedMessageCount(1);
        notificationAggregator.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);
        signatWireTap.expectedMessageCount(0);

        // When
        Map<String, Object> headers = new HashMap<>();
        headers.put(NotificationRouteHeaders.INTYGS_TYP, "luae_fs");
        headers.put(INTYG_TYPE_VERSION, "1.0");
        headers.put(NotificationRouteHeaders.HANDELSE, HandelsekodEnum.SKICKA.value());
        producerTemplate.sendBodyAndHeaders(createNotificationMessage(SchemaVersion.VERSION_3), headers);

        // Then
        assertIsSatisfied(notificationWSClientV3);
        assertIsSatisfied(notificationAggregator);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
        assertIsSatisfied(signatWireTap);
    }

    @Test
    public void testNormalRoute() throws InterruptedException {
        // Given
        notificationWSClientV3.expectedMessageCount(1);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);
        signatWireTap.expectedMessageCount(0);

        // When
        Map<String, Object> headers = new HashMap<>();
        headers.put(NotificationRouteHeaders.INTYGS_TYP, "fk7263");
        headers.put(INTYG_TYPE_VERSION, "1.0");
        headers.put(NotificationRouteHeaders.HANDELSE, HandelsekodEnum.ANDRAT.value());
        producerTemplate.sendBodyAndHeaders(createNotificationMessage(SchemaVersion.VERSION_3), headers);

        // Then
        assertIsSatisfied(notificationWSClientV3);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
        assertIsSatisfied(signatWireTap);
    }

    @Test
    public void testNormalRouteExplicitNotificationVersion1ResultsInPermanentException() throws InterruptedException {
        // Given
        notificationWSClientV3.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(1);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        producerTemplate.sendBody(createNotificationMessage(SchemaVersion.VERSION_1, "fk7263"));

        // Then
        assertIsSatisfied(notificationWSClientV3);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
    }

    @Test
    public void testNormalRouteNotificationVersion2() throws Exception {
        when(moduleApi.getIntygFromUtlatande(any())).thenReturn(NotificationTestHelper.createIntyg("lisjp"));

        // Given
        notificationWSClientV3.expectedMessageCount(1);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        producerTemplate.sendBodyAndHeader(createNotificationMessage(SchemaVersion.VERSION_3, "lisjp"),
            INTYG_TYPE_VERSION, "1.0");

        // Then
        assertIsSatisfied(notificationWSClientV3);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
    }

    @Test
    public void testTransformationException() throws Exception {
        // Given
        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenThrow(new ModuleNotFoundException("Testing runtime exception"));

        notificationWSClientV3.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(1);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        producerTemplate.sendBody(createNotificationMessage(null));

        // Then
        assertIsSatisfied(notificationWSClientV3);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
    }

    @Test
    public void testTransformationExceptionNotificationVersion3() throws Exception {
        // Given
        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenThrow(new ModuleNotFoundException("Testing checked exception"));

        notificationWSClientV3.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(1);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        producerTemplate.sendBody(createNotificationMessage(SchemaVersion.VERSION_3, "fk7263"));

        // Then
        assertIsSatisfied(notificationWSClientV3);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
    }

    @Test
    public void testRuntimeExceptionNotificationVersion3() throws Exception {
        // Given
        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenThrow(new RuntimeException("Testing runtime exception"));

        notificationWSClientV3.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(1);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        producerTemplate.sendBody(createNotificationMessage(SchemaVersion.VERSION_3, "fk7263"));

        // Then
        assertIsSatisfied(notificationWSClientV3);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
    }

    @Test
    public void testTemporaryException() throws InterruptedException {
        // Given
        notificationWSClientV3.whenAnyExchangeReceived(exchange -> {
            throw new TemporaryException("Testing application error, with exhausted retries");
        });

        notificationWSClientV3.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(1);

        // When
        producerTemplate.sendBody(createNotificationMessage(SchemaVersion.VERSION_3, "fk7263"));

        // Then
        assertIsSatisfied(notificationWSClientV3);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
    }

    @Test
    public void testTemporaryExceptionNotificationVersion3() throws Exception {
        // Given
        notificationWSClientV3.whenAnyExchangeReceived(exchange -> {
            throw new TemporaryException("Testing application error, with exhausted retries");
        });
        when(moduleApi.getIntygFromUtlatande(any())).thenReturn(NotificationTestHelper.createIntyg("fk7263"));

        notificationWSClientV3.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(1);

        // When
        producerTemplate.sendBody(createNotificationMessage(SchemaVersion.VERSION_3, "fk7263"));

        // Then
        assertIsSatisfied(notificationWSClientV3);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
    }

    @Test
    public void testPermanentException() throws InterruptedException {
        // Given
        notificationWSClientV3.whenAnyExchangeReceived(exchange -> {
            throw new PermanentException("Testing technical error");
        });

        notificationWSClientV3.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(1);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        producerTemplate.sendBody(createNotificationMessage(null));

        // Then
        assertIsSatisfied(notificationWSClientV3);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
    }

    @Test
    public void testPermanentExceptionNotificationVersion3() throws Exception {
        // Given
        notificationWSClientV3.whenAnyExchangeReceived(exchange -> {
            throw new PermanentException("Testing technical error");
        });
        when(moduleApi.getIntygFromUtlatande(any())).thenReturn(NotificationTestHelper.createIntyg("fk7263"));

        notificationWSClientV3.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(1);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        producerTemplate.sendBody(createNotificationMessage(SchemaVersion.VERSION_3, "fk7263"));

        // Then
        assertIsSatisfied(notificationWSClientV3);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
    }

    private String createNotificationMessage(SchemaVersion version) {
        return createNotificationMessage(version, "fk7263");
    }

    private String createNotificationMessage(SchemaVersion version, String intygsTyp) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"intygsId\":\"").append(currentId++).append("\",\"intygsTyp\":\"").append(intygsTyp)
            .append("\",\"logiskAdress\":\"SE12345678-1234\",\"handelseTid\":\"2001-12-31T12:34:56.789\",\"handelse\":\"ANDRAT\",");
        if (version != null) {
            sb.append("\"version\":\"");
            sb.append(version.name());
            sb.append("\",");
        }
        sb.append("\"utkast\":{\"id\":\"").append(currentId).append("\",\"typ\":\"").append(intygsTyp).append("\" },");
        if (SchemaVersion.VERSION_3 == version) {
            sb.append("\"skickadeFragor\":{\"totalt\":0,\"besvarade\":0,\"ejBesvarade\":0,\"hanterade\":0},");
            sb.append("\"mottagnaFragor\":{\"totalt\":0,\"besvarade\":0,\"ejBesvarade\":0,\"hanterade\":0}}");
        } else {
            sb.append("\"fragaSvar\":{\"antalFragor\":0,\"antalSvar\":0,\"antalHanteradeFragor\":0,\"antalHanteradeSvar\":0}}");
        }
        return sb.toString();
    }
}
