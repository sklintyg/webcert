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

package se.inera.intyg.webcert.web.service.mail;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import se.inera.intyg.infra.integration.hsa.client.OrganizationUnitService;
import se.inera.intyg.infra.integration.hsa.exception.HsaServiceCallException;
import se.inera.intyg.intygstyper.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.riv.infrastructure.directory.organization.getunitresponder.v1.UnitType;

@RunWith(MockitoJUnitRunner.class)
public class MailNotificationServiceMockedTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private OrganizationUnitService organizationUnitService;

    @Mock
    private MonitoringLogService monitoringService;

    @Mock
    private UtkastRepository utkastRepository;

    @InjectMocks
    private MailNotificationServiceImpl mailNotificationService;

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(mailNotificationService, "fromAddress", "no-reply@webcert.intygstjanster.se");
    }

    @Test
    public void sendMailForIncomingQuestionWithTimeoutThrowsNoException() throws Exception {
        doThrow(new MailSendException("Timeout")).when(mailSender).send(any(MimeMessage.class));
        mockOrganizationUnitServiceGetUnit();
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        mailNotificationService.sendMailForIncomingQuestion(mailNotification("enhetsid"));
    }

    @Test
    public void sendMailForIncomingAnswerWithTimeoutThrowsNoException() throws Exception {
        doThrow(new MailSendException("Timeout")).when(mailSender).send(any(MimeMessage.class));
        mockOrganizationUnitServiceGetUnit();
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        mailNotificationService.sendMailForIncomingAnswer(mailNotification("enhetsid"));
    }

    private void mockOrganizationUnitServiceGetUnit() throws HsaServiceCallException {
        UnitType unit = new UnitType();
        unit.setMail("test@test.invalid");
        unit.setUnitHsaId("enhetsid");
        when(organizationUnitService.getUnit(anyString())).thenReturn(unit);
    }

    @Test
    public void testNoHSAResponse() throws HsaServiceCallException {
        try {
            SOAPFault soapFault = SOAPFactory.newInstance().createFault();
            soapFault.setFaultString("Connection reset");
            when(organizationUnitService.getUnit(anyString())).thenThrow(new SOAPFaultException(soapFault));
        } catch (SOAPException e) {
            e.printStackTrace();
        }
        mailNotificationService.sendMailForIncomingAnswer(mailNotification("enhetsid"));
    }

    @Test
    public void setAdminMailAddress() throws Exception {
    }

    private MailNotification mailNotification(String enhetsId) {
        return new MailNotification(null, "1L", Fk7263EntryPoint.MODULE_ID, enhetsId, null, null);
    }

}
