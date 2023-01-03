/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.infra.integration.hsatk.exception.HsaServiceCallException;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.services.legacy.HsaOrganizationsService;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MailNotificationServiceMockedTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private HsaOrganizationsService hsaOrganizationsService;

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
        Vardenhet enhet = new Vardenhet("enhetsid", null, null, null);
        enhet.setEpost("test@test.invalid");
        when(hsaOrganizationsService.getVardenhet(anyString())).thenReturn(enhet);
    }

    @Test
    public void testNoHSAResponse() throws HsaServiceCallException {
        try {
            SOAPFault soapFault = SOAPFactory.newInstance().createFault();
            soapFault.setFaultString("Connection reset");
            when(hsaOrganizationsService.getVardenhet(anyString())).thenThrow(new SOAPFaultException(soapFault));
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
