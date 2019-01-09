/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.monitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import se.inera.intyg.webcert.web.service.monitoring.HealthCheckService;
import se.inera.intyg.webcert.web.service.monitoring.dto.HealthStatus;
import se.riv.itintegration.monitoring.v1.PingForConfigurationResponseType;
import se.riv.itintegration.monitoring.v1.PingForConfigurationType;

@RunWith(MockitoJUnitRunner.class)
public class PingForConfigurationResponderImplTest {

    private static final String PROJECT_VERSION = "project version";
    private static final String BUILD_NUMBER = "build number";
    private static final String BUILD_TIME = "build time";

    @Mock
    private HealthCheckService healthCheck;

    @InjectMocks
    private PingForConfigurationResponderImpl responder;

    @Before
    public void setup() {
        responder.init();
        ReflectionTestUtils.setField(responder, "projectVersion", PROJECT_VERSION);
        ReflectionTestUtils.setField(responder, "buildNumberString", BUILD_NUMBER);
        ReflectionTestUtils.setField(responder, "buildTimeString", BUILD_TIME);
    }

    @Test
    public void testPingForConfiguration() {
        long signatureQueueMeasure = 3;
        long nbrUsersMeasure = 7;
        when(healthCheck.checkDB()).thenReturn(new HealthStatus(1, true));
        when(healthCheck.checkJMS()).thenReturn(new HealthStatus(2, true));
        when(healthCheck.checkSignatureQueue()).thenReturn(new HealthStatus(signatureQueueMeasure, true));
        when(healthCheck.checkIntygstjanst()).thenReturn(new HealthStatus(4, true));
        when(healthCheck.checkPrivatlakarportal()).thenReturn(new HealthStatus(5, true));
        when(healthCheck.checkUptime()).thenReturn(new HealthStatus(6, true));
        when(healthCheck.checkNbrOfUsers()).thenReturn(new HealthStatus(nbrUsersMeasure, true));

        PingForConfigurationResponseType res = responder.pingForConfiguration("logicalAddress", new PingForConfigurationType());

        assertNotNull(res);
        assertNotNull(res.getPingDateTime());
        assertEquals(PROJECT_VERSION, res.getVersion());
        assertEquals(BUILD_NUMBER, res.getConfiguration().stream().filter(c -> "buildNumber".equals(c.getName())).findAny().get().getValue());
        assertEquals(BUILD_TIME, res.getConfiguration().stream().filter(c -> "buildTime".equals(c.getName())).findAny().get().getValue());
        assertNotNull(res.getConfiguration().stream().filter(c -> "systemUptime".equals(c.getName())).findAny().get().getValue());
        assertEquals("ok", res.getConfiguration().stream().filter(c -> "dbStatus".equals(c.getName())).findAny().get().getValue());
        assertEquals("ok", res.getConfiguration().stream().filter(c -> "jmsStatus".equals(c.getName())).findAny().get().getValue());
        assertEquals("ok", res.getConfiguration().stream().filter(c -> "intygstjanst".equals(c.getName())).findAny().get().getValue());
        assertEquals("ok", res.getConfiguration().stream().filter(c -> "privatlakarportal".equals(c.getName())).findAny().get().getValue());
        assertEquals("" + signatureQueueMeasure, res.getConfiguration().stream().filter(c -> "signatureQueueSize".equals(c.getName())).findAny().get().getValue());
        assertEquals("" + nbrUsersMeasure, res.getConfiguration().stream().filter(c -> "nbrOfUsers".equals(c.getName())).findAny().get().getValue());

        verify(healthCheck).checkDB();
        verify(healthCheck).checkJMS();
        verify(healthCheck).checkSignatureQueue();
        verify(healthCheck).checkIntygstjanst();
        verify(healthCheck).checkPrivatlakarportal();
        verify(healthCheck).checkUptime();
        verify(healthCheck).checkNbrOfUsers();
    }

    @Test
    public void testPingForConfigurationErrors() {
        long signatureQueueMeasure = -1;
        long nbrUsersMeasure = -1;
        when(healthCheck.checkDB()).thenReturn(new HealthStatus(-1, false));
        when(healthCheck.checkJMS()).thenReturn(new HealthStatus(-1, false));
        when(healthCheck.checkSignatureQueue()).thenReturn(new HealthStatus(signatureQueueMeasure, false));
        when(healthCheck.checkIntygstjanst()).thenReturn(new HealthStatus(-1, false));
        when(healthCheck.checkPrivatlakarportal()).thenReturn(new HealthStatus(-1, false));
        when(healthCheck.checkUptime()).thenReturn(new HealthStatus(-1, false));
        when(healthCheck.checkNbrOfUsers()).thenReturn(new HealthStatus(nbrUsersMeasure, false));

        PingForConfigurationResponseType res = responder.pingForConfiguration("logicalAddress", new PingForConfigurationType());

        assertNotNull(res);
        assertNotNull(res.getPingDateTime());
        assertEquals(PROJECT_VERSION, res.getVersion());
        assertEquals(BUILD_NUMBER, res.getConfiguration().stream().filter(c -> "buildNumber".equals(c.getName())).findAny().get().getValue());
        assertEquals(BUILD_TIME, res.getConfiguration().stream().filter(c -> "buildTime".equals(c.getName())).findAny().get().getValue());
        assertNotNull(res.getConfiguration().stream().filter(c -> "systemUptime".equals(c.getName())).findAny().get().getValue());
        assertEquals("error", res.getConfiguration().stream().filter(c -> "dbStatus".equals(c.getName())).findAny().get().getValue());
        assertEquals("error", res.getConfiguration().stream().filter(c -> "jmsStatus".equals(c.getName())).findAny().get().getValue());
        assertEquals("no connection", res.getConfiguration().stream().filter(c -> "intygstjanst".equals(c.getName())).findAny().get().getValue());
        assertEquals("no connection", res.getConfiguration().stream().filter(c -> "privatlakarportal".equals(c.getName())).findAny().get().getValue());
        assertEquals("" + signatureQueueMeasure, res.getConfiguration().stream().filter(c -> "signatureQueueSize".equals(c.getName())).findAny().get().getValue());
        assertEquals("" + nbrUsersMeasure, res.getConfiguration().stream().filter(c -> "nbrOfUsers".equals(c.getName())).findAny().get().getValue());

        verify(healthCheck).checkDB();
        verify(healthCheck).checkJMS();
        verify(healthCheck).checkSignatureQueue();
        verify(healthCheck).checkIntygstjanst();
        verify(healthCheck).checkPrivatlakarportal();
        verify(healthCheck).checkUptime();
        verify(healthCheck).checkNbrOfUsers();
    }
}
