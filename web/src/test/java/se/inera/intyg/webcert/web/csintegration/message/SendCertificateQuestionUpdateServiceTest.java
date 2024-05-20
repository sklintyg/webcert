/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.csintegration.message;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.Staff;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.webcert.persistence.integreradenhet.model.IntegreradEnhet;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import se.inera.intyg.webcert.web.service.mail.MailNotification;
import se.inera.intyg.webcert.web.service.mail.MailNotificationService;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Amneskod;
import se.riv.clinicalprocess.healthcond.certificate.v3.MeddelandeReferens;

@ExtendWith(MockitoExtension.class)
class SendCertificateQuestionUpdateServiceTest {

    private static final String CERTIFICATE_TYPE = "certificateType";
    private static final String UNIT_ID = "unitId";
    private static final String UNIT_NAME = "unitName";
    private static final String ISSUED_BY_PERSON_ID = "issuedByPersonId";
    private Certificate certificate;
    private static final String CERTIFICATE_ID = "certificateId";
    private SendMessageToCareType sendMessageToCareType;
    @Mock
    IntegreradeEnheterRegistry integreradeEnheterRegistry;
    @Mock
    MailNotificationService mailNotificationService;
    @InjectMocks
    SendCertificateQuestionUpdateService sendCertificateQuestionUpdateService;

    @BeforeEach
    void setUp() {
        certificate = new Certificate();
        certificate.setMetadata(
            CertificateMetadata.builder()
                .id(CERTIFICATE_ID)
                .type(CERTIFICATE_TYPE)
                .unit(
                    Unit.builder()
                        .unitId(UNIT_ID)
                        .unitName(UNIT_NAME)
                        .build()
                )
                .issuedBy(
                    Staff.builder()
                        .personId(ISSUED_BY_PERSON_ID)
                        .build()
                )
                .build()
        );

        sendMessageToCareType = new SendMessageToCareType();

    }

    @Test
    void shallNotCallMailNotificationServiceIfUnitIsIntegrated() {
        doReturn(new IntegreradEnhet()).when(integreradeEnheterRegistry).getIntegreradEnhet(UNIT_ID);
        sendCertificateQuestionUpdateService.send(new SendMessageToCareType(), certificate);
        verifyNoInteractions(mailNotificationService);
    }

    @Test
    void shallSendMailForIncomingQuestionIfQuestionTypeIsReminder() {
        sendMessageToCareType.setAmne(new Amneskod());
        sendMessageToCareType.getAmne().setCode("PAMINN");

        doReturn(null).when(integreradeEnheterRegistry).getIntegreradEnhet(UNIT_ID);

        sendCertificateQuestionUpdateService.send(sendMessageToCareType, certificate);
        verify(mailNotificationService).sendMailForIncomingQuestion(any(MailNotification.class));
    }

    @Test
    void shallSendMailForIncomingQuestionIfQuestionIsNotAnswer() {
        sendMessageToCareType.setAmne(new Amneskod());
        sendMessageToCareType.getAmne().setCode("KOMPLT");

        doReturn(null).when(integreradeEnheterRegistry).getIntegreradEnhet(UNIT_ID);

        sendCertificateQuestionUpdateService.send(sendMessageToCareType, certificate);
        verify(mailNotificationService).sendMailForIncomingQuestion(any(MailNotification.class));
    }

    @Test
    void shallSendMailForIncomingAnswerIfQuestionIsAnswer() {
        sendMessageToCareType.setAmne(new Amneskod());
        sendMessageToCareType.getAmne().setCode("KOMPLT");
        sendMessageToCareType.setSvarPa(new MeddelandeReferens());

        doReturn(null).when(integreradeEnheterRegistry).getIntegreradEnhet(UNIT_ID);

        sendCertificateQuestionUpdateService.send(sendMessageToCareType, certificate);
        verify(mailNotificationService).sendMailForIncomingAnswer(any(MailNotification.class));
    }
}
