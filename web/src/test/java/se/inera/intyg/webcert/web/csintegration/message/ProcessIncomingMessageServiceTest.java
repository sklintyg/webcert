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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.web.csintegration.certificate.PublishCertificateStatusUpdateService;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.message.dto.IncomingMessageRequestDTO;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Amneskod;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.v3.MeddelandeReferens;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;


@ExtendWith(MockitoExtension.class)
class ProcessIncomingMessageServiceTest {

    private static final IncomingMessageRequestDTO INCOMING_MESSAGE_REQUEST_DTO = IncomingMessageRequestDTO.builder()
        .build();
    private static final String CERTIFICATE_ID = "certificateId";
    private static final String CERTIFICATE_TYPE = "certificateType";
    private static final String UNIT_ID = "unitId";
    private static final GetCertificateRequestDTO GET_CERTIFICATE_REQUEST_DTO = GetCertificateRequestDTO.builder().build();

    private SendMessageToCareType sendMessageToCareType;
    private Certificate certificate;
    @Mock
    SendMailNotificationForReceivedMessageService sendMailNotificationForReceivedMessageService;
    @Mock
    PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;
    @Mock
    CSIntegrationService csIntegrationService;
    @Mock
    CSIntegrationRequestFactory csIntegrationRequestFactory;
    @Mock
    MonitoringLogService monitoringLogService;
    @InjectMocks
    ProcessIncomingMessageService processIncomingMessageService;

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
                        .build()
                )
                .build()
        );

        doReturn(INCOMING_MESSAGE_REQUEST_DTO).when(csIntegrationRequestFactory)
            .getIncomingMessageRequest(any(SendMessageToCareType.class));
        doReturn(GET_CERTIFICATE_REQUEST_DTO).when(csIntegrationRequestFactory)
            .getCertificateRequest();
        doReturn(certificate).when(csIntegrationService).getCertificate(
            CERTIFICATE_ID, GET_CERTIFICATE_REQUEST_DTO
        );

        sendMessageToCareType = new SendMessageToCareType();
        sendMessageToCareType.setAmne(new Amneskod());
        sendMessageToCareType.getAmne().setCode("KOMPLT");
        final var intygId = new IntygId();
        intygId.setExtension(CERTIFICATE_ID);
        sendMessageToCareType.setIntygsId(intygId);
    }

    @Test
    void shallPostMessageToCertificateService() {
        processIncomingMessageService.process(sendMessageToCareType);
        verify(csIntegrationService, times(1)).postMessage(INCOMING_MESSAGE_REQUEST_DTO);
    }

    @Test
    void shallMonitorLogArendeRecieved() {
        processIncomingMessageService.process(sendMessageToCareType);
        verify(monitoringLogService).logArendeReceived(
            CERTIFICATE_ID,
            CERTIFICATE_TYPE,
            UNIT_ID,
            ArendeAmne.KOMPLT,
            Collections.emptyList(),
            false
        );
    }

    @Test
    void shallMonitorLogArendeRecievedAsAnswer() {
        sendMessageToCareType.setSvarPa(new MeddelandeReferens());
        processIncomingMessageService.process(sendMessageToCareType);
        verify(monitoringLogService).logArendeReceived(
            CERTIFICATE_ID,
            CERTIFICATE_TYPE,
            UNIT_ID,
            ArendeAmne.KOMPLT,
            Collections.emptyList(),
            true
        );
    }

    @Test
    void shallPublishEventForQuestion() {
        processIncomingMessageService.process(sendMessageToCareType);
        verify(publishCertificateStatusUpdateService).publish(certificate, HandelsekodEnum.NYFRFM);
    }

    @Test
    void shallPublishEventForAnswer() {
        sendMessageToCareType.setSvarPa(new MeddelandeReferens());
        processIncomingMessageService.process(sendMessageToCareType);
        verify(publishCertificateStatusUpdateService).publish(certificate, HandelsekodEnum.NYSVFM);
    }

    @Test
    void shallSendStatusUpdateForQuestion() {
        processIncomingMessageService.process(sendMessageToCareType);
        verify(sendMailNotificationForReceivedMessageService).send(sendMessageToCareType, certificate);
    }

    @Test
    void shallReturnSendMessageToCareResponseTypeWithResultCodeOk() {
        final var expectedResult = new SendMessageToCareResponseType();
        final var result = new ResultType();
        result.setResultCode(ResultCodeType.OK);
        expectedResult.setResult(result);

        final var actualResult = processIncomingMessageService.process(sendMessageToCareType);
        assertEquals(ResultCodeType.OK, actualResult.getResult().getResultCode());
    }
}
