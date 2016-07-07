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

package se.inera.intyg.webcert.notification_sender.notifications.route;

import static org.apache.camel.component.mock.MockEndpoint.assertIsSatisfied;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Message;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultMessage;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.apache.camel.test.spring.CamelTestContextBootstrapper;
import org.apache.camel.test.spring.MockEndpointsAndSkip;
import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalDate;
import org.joda.time.Partial;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;

import se.inera.intyg.common.support.modules.converter.InternalConverterUtil;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.modules.support.api.notification.HandelseType;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.intygstyper.fk7263.model.converter.Fk7263InternalToNotification;
import se.inera.intyg.webcert.common.sender.exception.PermanentException;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.ArbetsplatsKod;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.PartialDateTypeFormatEnum;
import se.riv.clinicalprocess.healthcond.certificate.v2.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.v2.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.v2.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.v2.Vardgivare;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration("/notifications/unit-test-notification-sender-config.xml")
@BootstrapWith(CamelTestContextBootstrapper.class)
@MockEndpointsAndSkip("bean:notificationAggregator||bean:notificationWSClient|bean:notificationWSClientV2|direct:permanentErrorHandlerEndpoint|direct:temporaryErrorHandlerEndpoint")
public class RouteTest {

    private static final Logger LOG = LoggerFactory.getLogger(RouteTest.class);

    @Autowired
    CamelContext camelContext;

    @Mock
    private ModuleApi moduleApi; // this is a mock from unit-test-notification-sender-config.xml

    @Autowired
    private IntygModuleRegistry moduleRegistry; // this is a mock from unit-test-notification-sender-config.xml

    @Autowired
    private Fk7263InternalToNotification mockInternalToNotification; // this is a mock from unit-test-notification-sender-config.xml

    @Produce(uri = "direct:receiveNotificationForAggregationRequestEndpoint")
    private ProducerTemplate producerTemplate;

    @EndpointInject(uri = "mock:bean:notificationAggregator")
    private MockEndpoint notificationAggregator;

    @EndpointInject(uri = "mock:bean:notificationWSClient")
    private MockEndpoint notificationWSClient;

    @EndpointInject(uri = "mock:bean:notificationWSClientV2")
    private MockEndpoint notificationWSClientV2;

    @EndpointInject(uri = "mock:direct:permanentErrorHandlerEndpoint")
    private MockEndpoint permanentErrorHandlerEndpoint;

    @EndpointInject(uri = "mock:direct:temporaryErrorHandlerEndpoint")
    private MockEndpoint temporaryErrorHandlerEndpoint;

    private static int currentId = 1000;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        MockEndpoint.resetMocks(camelContext);
        when(moduleRegistry.getModuleApi(anyString())).thenReturn(moduleApi);
    }

    @After
    public void cleanup() {
        Mockito.reset(moduleRegistry, moduleApi, mockInternalToNotification);
    }

    @Test
    public void testRoutesDirectlyToNotificationQueueForFK7263Andrat() throws ModuleException, InterruptedException {
        // Given
        when(moduleApi.getIntygFromUtlatande(any())).thenReturn(createIntyg());
        notificationWSClient.expectedMessageCount(1);
        notificationWSClientV2.expectedMessageCount(0);
        notificationAggregator.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        Map<String, Object> headers = new HashMap<>();
        headers.put(NotificationRouteHeaders.INTYGS_TYP ,"fk7263");
        headers.put(NotificationRouteHeaders.HANDELSE , HandelseType.INTYGSUTKAST_ANDRAT.value());
        producerTemplate.sendBodyAndHeaders(createNotificationMessage(null), headers);

        // Then
        assertIsSatisfied(notificationWSClient);
        assertIsSatisfied(notificationWSClientV2);
        assertIsSatisfied(notificationAggregator);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
    }

    @Test
    public void testRoutesToAggregatorForLuaeFsAndrat() throws InterruptedException, ModuleException {
        // This is just a hack in order to not fail subsequent tests due to camel unit tests leaking context between
        // tests. This one just makes sure the notficationAggregator doesn't cause the subsequent "split(body())"
        // to throw some exception caught by later tests.

        notificationAggregator.whenAnyExchangeReceived(exchange -> {
            Message msg = new DefaultMessage();
            exchange.setOut(msg);
        });

        // Given
        notificationWSClient.expectedMessageCount(0);
        notificationAggregator.expectedMessageCount(1);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        Map<String, Object> headers = new HashMap<>();
        headers.put(NotificationRouteHeaders.INTYGS_TYP ,"luae_fs");
        headers.put(NotificationRouteHeaders.HANDELSE , HandelseType.INTYGSUTKAST_ANDRAT.value());
        producerTemplate.sendBodyAndHeaders(createNotificationMessage(SchemaVersion.VERSION_2, "luae_fs"), headers);

        // Then
        assertIsSatisfied(notificationWSClient);
        assertIsSatisfied(notificationAggregator);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
    }

    @Test
    public void testRoutesDirectlyToNotificationQueueForLuaeFsSkapad() throws InterruptedException, ModuleException {
        // Given
        when(moduleApi.getIntygFromUtlatande(any())).thenReturn(createIntyg());
        notificationWSClientV2.expectedMessageCount(1);
        notificationAggregator.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        Map<String, Object> headers = new HashMap<>();
        headers.put(NotificationRouteHeaders.INTYGS_TYP ,"luae_fs");
        headers.put(NotificationRouteHeaders.HANDELSE , HandelseType.INTYGSUTKAST_SKAPAT.value());
        producerTemplate.sendBodyAndHeaders(createNotificationMessage(SchemaVersion.VERSION_2, "luae_fs"), headers);

        // Then
        assertIsSatisfied(notificationWSClientV2);
        assertIsSatisfied(notificationAggregator);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
    }

    @Test
    public void testRoutesDirectlyToNotificationQueueForLuaeFsSkickad() throws InterruptedException {
        // Given
        notificationWSClient.expectedMessageCount(1);
        notificationAggregator.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        Map<String, Object> headers = new HashMap<>();
        headers.put(NotificationRouteHeaders.INTYGS_TYP ,"luae_fs");
        headers.put(NotificationRouteHeaders.HANDELSE , HandelseType.INTYG_SKICKAT_FK.value());
        producerTemplate.sendBodyAndHeaders(createNotificationMessage(null), headers);

        // Then
        assertIsSatisfied(notificationWSClient);
        assertIsSatisfied(notificationAggregator);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
    }

    @Test
    public void testNormalRoute() throws InterruptedException {
        // Given
        notificationWSClient.expectedMessageCount(1);
        notificationWSClientV2.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        Map<String, Object> headers = new HashMap<>();
        headers.put(NotificationRouteHeaders.INTYGS_TYP ,"fk7263");
        headers.put(NotificationRouteHeaders.HANDELSE , HandelseType.INTYGSUTKAST_ANDRAT.value());
        producerTemplate.sendBodyAndHeaders(createNotificationMessage(null), headers);

        // Then
        assertIsSatisfied(notificationWSClient);
        assertIsSatisfied(notificationWSClientV2);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
    }

    @Test
    public void testNormalRouteExplicitNotificationVersion1() throws InterruptedException {
        // Given
        notificationWSClient.expectedMessageCount(1);
        notificationWSClientV2.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        producerTemplate.sendBody(createNotificationMessage(SchemaVersion.VERSION_1, "fk7263"));

        // Then
        assertIsSatisfied(notificationWSClient);
        assertIsSatisfied(notificationWSClientV2);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
    }

    @Test
    public void testNormalRouteNotificationVersion2() throws Exception {
        when(moduleApi.getIntygFromUtlatande(any())).thenReturn(createIntyg());
        // Given
        notificationWSClient.expectedMessageCount(0);
        notificationWSClientV2.expectedMessageCount(1);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        producerTemplate.sendBody(createNotificationMessage(SchemaVersion.VERSION_2, "fk7263"));

        // Then
        assertIsSatisfied(notificationWSClient);
        assertIsSatisfied(notificationWSClientV2);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
    }

    @Test
    public void testTransformationException() throws Exception {
        // Given
        when(mockInternalToNotification.createCertificateStatusUpdateForCareType(any())).thenThrow(new ModuleException("Testing runtime exception"));

        notificationWSClient.expectedMessageCount(0);
        notificationWSClientV2.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(1);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        producerTemplate.sendBody(createNotificationMessage(null));

        // Then
        assertIsSatisfied(notificationWSClient);
        assertIsSatisfied(notificationWSClientV2);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
    }

    @Test
    public void testTransformationExceptionNotificationVersion2() throws Exception {
        // Given
        when(moduleRegistry.getModuleApi(anyString())).thenThrow(new ModuleNotFoundException("Testing checked exception"));

        notificationWSClient.expectedMessageCount(0);
        notificationWSClientV2.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(1);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        producerTemplate.sendBody(createNotificationMessage(SchemaVersion.VERSION_2, "fk7263"));

        // Then
        assertIsSatisfied(notificationWSClient);
        assertIsSatisfied(notificationWSClientV2);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
    }



    @Test
    public void testRuntimeExceptionNotificationVersion2() throws Exception {
        // Given
        when(moduleRegistry.getModuleApi(anyString())).thenThrow(new RuntimeException("Testing runtime exception"));

        notificationWSClient.expectedMessageCount(0);
        notificationWSClientV2.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(1);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        producerTemplate.sendBody(createNotificationMessage(SchemaVersion.VERSION_2, "fk7263"));

        // Then
        assertIsSatisfied(notificationWSClient);
        assertIsSatisfied(notificationWSClientV2);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
    }

    @Test
    public void testTemporaryException() throws InterruptedException, ModuleException {
        // Given
        notificationWSClient.whenAnyExchangeReceived(exchange -> {
            throw new TemporaryException("Testing application error, with exhausted retries");
        });

        notificationWSClient.expectedMessageCount(1);
        notificationWSClientV2.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(1);

        // When
        producerTemplate.sendBody(createNotificationMessage(SchemaVersion.VERSION_1, "fk7263"));

        // Then
        assertIsSatisfied(notificationWSClient);
        assertIsSatisfied(notificationWSClientV2);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
    }

    @Test
    public void testTemporaryExceptionNotificationVersion2() throws Exception {
        // Given
        notificationWSClientV2.whenAnyExchangeReceived(exchange -> {
            throw new TemporaryException("Testing application error, with exhausted retries");
        });
        when(moduleApi.getIntygFromUtlatande(any())).thenReturn(createIntyg());

        notificationWSClient.expectedMessageCount(0);
        notificationWSClientV2.expectedMessageCount(1);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(1);

        // When
        producerTemplate.sendBody(createNotificationMessage(SchemaVersion.VERSION_2, "fk7263"));

        // Then
        assertIsSatisfied(notificationWSClient);
        assertIsSatisfied(notificationWSClientV2);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
    }

    @Test
    public void testPermanentException() throws InterruptedException {
        // Given
        notificationWSClient.whenAnyExchangeReceived(exchange -> {
            throw new PermanentException("Testing technical error");
        });

        notificationWSClient.expectedMessageCount(1);
        notificationWSClientV2.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(1);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        producerTemplate.sendBody(createNotificationMessage(null));

        // Then
        assertIsSatisfied(notificationWSClient);
        assertIsSatisfied(notificationWSClientV2);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
    }

    @Test
    public void testPermanentExceptionNotificationVersion2() throws Exception {
        // Given
        notificationWSClientV2.whenAnyExchangeReceived(exchange -> {
            throw new PermanentException("Testing technical error");
        });
        when(moduleApi.getIntygFromUtlatande(any())).thenReturn(createIntyg());

        notificationWSClient.expectedMessageCount(0);
        notificationWSClientV2.expectedMessageCount(1);
        permanentErrorHandlerEndpoint.expectedMessageCount(1);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        producerTemplate.sendBody(createNotificationMessage(SchemaVersion.VERSION_2, "fk7263"));

        // Then
        assertIsSatisfied(notificationWSClient);
        assertIsSatisfied(notificationWSClientV2);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
    }

    @Test
    public void testZRuntimeException() throws Exception {
        // Given
        when(mockInternalToNotification.createCertificateStatusUpdateForCareType(any())).thenThrow(new RuntimeException("Testing runtime exception"));

        notificationWSClient.expectedMessageCount(0);
        notificationWSClientV2.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(1);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        producerTemplate.sendBody(createNotificationMessage(null));

        // Then
        assertIsSatisfied(notificationWSClient);
        assertIsSatisfied(notificationWSClientV2);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
    }

    private String createNotificationMessage(SchemaVersion version) {
        return createNotificationMessage(version, "fk7263");
    }

    private String createNotificationMessage(SchemaVersion version, String intygsTyp) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"intygsId\":\"" + currentId++ + "\",\"intygsTyp\":\"" + intygsTyp + "\",\"logiskAdress\":\"SE12345678-1234\",\"handelseTid\":\"2001-12-31T12:34:56.789\",\"handelse\":\"INTYGSUTKAST_ANDRAT\",");
        if (version != null) {
            sb.append("\"version\":\"");
            sb.append(version.name());
            sb.append("\",");
        }
        sb.append("\"utkast\":{\"id\":\"" + currentId + "\",\"typ\":\"" + intygsTyp + "\" },\"fragaSvar\":{\"antalFragor\":0,\"antalSvar\":0,\"antalHanteradeFragor\":0,\"antalHanteradeSvar\":0}}");
        return sb.toString();
    }

    private Intyg createIntyg() {
        Intyg intyg = new Intyg();
        HosPersonal hosPersonal = new HosPersonal();
        Enhet enhet = new Enhet();
        enhet.setVardgivare(new Vardgivare());
        enhet.setArbetsplatskod(new ArbetsplatsKod());
        hosPersonal.setEnhet(enhet);
        intyg.setSkapadAv(hosPersonal);
        // DatePeriodType and PartialDateType must be allowed
        intyg.getSvar().add(InternalConverterUtil.aSvar("")
                .withDelsvar("", InternalConverterUtil.aDatePeriod(LocalDate.now(), LocalDate.now().plusDays(1)))
                .withDelsvar("", InternalConverterUtil.aPartialDate(PartialDateTypeFormatEnum.YYYY, new Partial(DateTimeFieldType.year(), 1999))).build());
        return intyg;
    }

}
