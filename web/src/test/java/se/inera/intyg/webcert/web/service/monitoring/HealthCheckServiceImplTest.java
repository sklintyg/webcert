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

package se.inera.intyg.webcert.web.service.monitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Time;
import java.util.Arrays;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.xml.ws.WebServiceException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jms.core.BrowserCallback;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.test.util.ReflectionTestUtils;

import se.inera.intyg.webcert.web.service.monitoring.dto.HealthStatus;
import se.riv.itintegration.monitoring.rivtabp21.v1.PingForConfigurationResponderInterface;
import se.riv.itintegration.monitoring.v1.PingForConfigurationResponseType;
import se.riv.itintegration.monitoring.v1.PingForConfigurationType;

@RunWith(MockitoJUnitRunner.class)
public class HealthCheckServiceImplTest {

    private static final String IT_LOGICAL_ADDRESS = "it-logical-address";
    private static final String PP_LOGICAL_ADDRESS = "pp-logical-address";

    @Mock
    private EntityManager entityManager;

    @Mock
    private JmsTemplate jmsCertificateSenderTemplate;

    @Mock
    private ConnectionFactory connectionFactory;

    @Mock
    private PingForConfigurationResponderInterface intygstjanstPingForConfiguration;

    @Mock
    private PingForConfigurationResponderInterface privatlakarportalPingForConfiguration;

    @Mock
    private SessionRegistry sessionRegistry;

    @InjectMocks
    private HealthCheckServiceImpl service;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(service, "itLogicalAddress", IT_LOGICAL_ADDRESS);
        ReflectionTestUtils.setField(service, "ppLogicalAddress", PP_LOGICAL_ADDRESS);
    }

    @Test
    public void testCheckDB() {
        Query query = mock(Query.class);
        when(query.getSingleResult()).thenReturn(mock(Time.class));
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        HealthStatus res = service.checkDB();

        assertNotNull(res);
        assertTrue(res.isOk());
        assertNotNull(res.getMeasurement());
    }

    @Test
    public void testCheckDBTimestampNull() {
        Query query = mock(Query.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        HealthStatus res = service.checkDB();

        assertNotNull(res);
        assertFalse(res.isOk());
        assertNotNull(res.getMeasurement());
    }

    @Test
    public void testCheckDBQueryException() {
        HealthStatus res = service.checkDB();

        assertNotNull(res);
        assertFalse(res.isOk());
        assertNotNull(res.getMeasurement());
    }

    @Test
    public void testCheckJMS() throws JMSException {
        when(connectionFactory.createConnection()).thenReturn(mock(Connection.class));
        HealthStatus res = service.checkJMS();

        assertNotNull(res);
        assertTrue(res.isOk());
        assertNotNull(res.getMeasurement());
    }

    @Test
    public void testCheckJMSException() throws JMSException {
        when(connectionFactory.createConnection()).thenThrow(new JMSException(""));
        HealthStatus res = service.checkJMS();

        assertNotNull(res);
        assertFalse(res.isOk());
        assertNotNull(res.getMeasurement());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCheckSignatureQueue() {
        final int queueDepth = 5;
        when(jmsCertificateSenderTemplate.browse(any(BrowserCallback.class))).thenReturn(queueDepth);
        HealthStatus res = service.checkSignatureQueue();

        assertNotNull(res);
        assertTrue(res.isOk());
        assertEquals(queueDepth, res.getMeasurement());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCheckSignatureQueueException() {
        when(jmsCertificateSenderTemplate.browse(any(BrowserCallback.class))).thenThrow(new RuntimeException(""));
        HealthStatus res = service.checkSignatureQueue();

        assertNotNull(res);
        assertFalse(res.isOk());
        assertEquals(-1, res.getMeasurement());
    }

    @Test
    public void testCheckIntygstjanst() {
        when(intygstjanstPingForConfiguration.pingForConfiguration(eq(IT_LOGICAL_ADDRESS), any(PingForConfigurationType.class)))
                .thenReturn(new PingForConfigurationResponseType());
        HealthStatus res = service.checkIntygstjanst();

        assertNotNull(res);
        assertTrue(res.isOk());
        assertNotNull(res.getMeasurement());
    }

    @Test
    public void testCheckIntygstjanstNullResponse() {
        when(intygstjanstPingForConfiguration.pingForConfiguration(eq(IT_LOGICAL_ADDRESS), any(PingForConfigurationType.class)))
        .thenReturn(null);
        HealthStatus res = service.checkIntygstjanst();

        assertNotNull(res);
        assertFalse(res.isOk());
        assertNotNull(res.getMeasurement());

        verify(intygstjanstPingForConfiguration).pingForConfiguration(eq(IT_LOGICAL_ADDRESS), any(PingForConfigurationType.class));
    }

    @Test
    public void testCheckIntygstjanstException() {
        when(intygstjanstPingForConfiguration.pingForConfiguration(eq(IT_LOGICAL_ADDRESS), any(PingForConfigurationType.class)))
                .thenThrow(new WebServiceException());
        HealthStatus res = service.checkIntygstjanst();

        assertNotNull(res);
        assertFalse(res.isOk());
        assertNotNull(res.getMeasurement());
    }

    @Test
    public void testCheckNbrOfUsers() {
        when(sessionRegistry.getAllPrincipals()).thenReturn(Arrays.asList("principal1", "principal2"));
        HealthStatus res = service.checkNbrOfUsers();

        assertNotNull(res);
        assertTrue(res.isOk());
        assertEquals(2, res.getMeasurement());
    }

    @Test
    public void testCheckNbrOfUsersException() {
        when(sessionRegistry.getAllPrincipals()).thenThrow(new RuntimeException());
        HealthStatus res = service.checkNbrOfUsers();

        assertNotNull(res);
        assertFalse(res.isOk());
        assertEquals(-1, res.getMeasurement());
    }

    @Test
    public void testCheckUptime() {
        HealthStatus res = service.checkUptime();

        assertNotNull(res);
        assertTrue(res.isOk());
        assertNotNull(res.getMeasurement());
    }

    @Test
    public void testCheckUptimeAsString() {
        String res = service.checkUptimeAsString();

        assertNotNull(res);
    }

    @Test
    public void testCheckPrivatlakarportal() {
        when(privatlakarportalPingForConfiguration.pingForConfiguration(eq(PP_LOGICAL_ADDRESS), any(PingForConfigurationType.class)))
                .thenReturn(new PingForConfigurationResponseType());
        HealthStatus res = service.checkPrivatlakarportal();

        assertNotNull(res);
        assertTrue(res.isOk());
        assertNotNull(res.getMeasurement());

        verify(privatlakarportalPingForConfiguration).pingForConfiguration(eq(PP_LOGICAL_ADDRESS), any(PingForConfigurationType.class));
    }

    @Test
    public void testCheckPrivatlakarportalNullResponse() {
        when(privatlakarportalPingForConfiguration.pingForConfiguration(eq(PP_LOGICAL_ADDRESS), any(PingForConfigurationType.class)))
        .thenReturn(null);
        HealthStatus res = service.checkPrivatlakarportal();

        assertNotNull(res);
        assertFalse(res.isOk());
        assertNotNull(res.getMeasurement());
    }

    @Test
    public void testCheckPrivatlakarportalException() {
        when(privatlakarportalPingForConfiguration.pingForConfiguration(eq(PP_LOGICAL_ADDRESS), any(PingForConfigurationType.class)))
                .thenThrow(new WebServiceException());
        HealthStatus res = service.checkPrivatlakarportal();

        assertNotNull(res);
        assertFalse(res.isOk());
        assertNotNull(res.getMeasurement());
    }
}
