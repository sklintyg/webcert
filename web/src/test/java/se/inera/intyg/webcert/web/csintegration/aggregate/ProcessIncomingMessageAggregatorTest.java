/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.csintegration.aggregate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.message.ProcessIncomingMessageService;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType.SkickatAv;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Amneskod;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Part;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;

@ExtendWith(MockitoExtension.class)
class ProcessIncomingMessageAggregatorTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String MESSAGE_ID = "messageId";
    private static final String PATIENT_ID = "191212121212";
    private static final String FKASSA = "FKASSA";
    private SendMessageToCareType sendMessageToCareType;
    @Mock
    ArendeService arendeService;
    @Mock
    CSIntegrationService csIntegrationService;
    @Mock
    ProcessIncomingMessageService processIncomingMessageService;
    @InjectMocks
    ProcessIncomingMessageAggregator processIncomingMessageAggregator;

    @BeforeEach
    void setUp() {
        sendMessageToCareType = getSendMessageToCareTypeRequest();
    }
    @Test
    void shallProcessIncomingMessageInArandeServiceIfCertificateNotFoundInCertificateService() {        doReturn(false).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
        processIncomingMessageAggregator.process(sendMessageToCareType);
        verify(arendeService, times(1)).processIncomingMessage(any());
    }

    @Test
    void shallReturnSendMessageToCareResponseTypeFromProcessIncomingMessageService() {
        final var expectedResult = new SendMessageToCareResponseType();        doReturn(true).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
        doReturn(expectedResult).when(processIncomingMessageService).process(sendMessageToCareType);

        final var actualResult = processIncomingMessageAggregator.process(sendMessageToCareType);

        verify(arendeService, times(0)).processIncomingMessage(any());
        assertEquals(expectedResult, actualResult);
    }

    private SendMessageToCareType getSendMessageToCareTypeRequest() {
        final var res = new SendMessageToCareType();
        res.setAmne(new Amneskod());
        res.getAmne().setCode(ArendeAmne.KONTKT.toString());
        res.setIntygsId(createIntygsId());
        res.setMeddelandeId(MESSAGE_ID);
        res.setPatientPersonId(createPersonId());
        res.setSkickatAv(createSkickadAv());
        return res;
    }

    private SkickatAv createSkickadAv() {
        final var res = new SkickatAv();
        res.setPart(new Part());
        res.getPart().setCode(FKASSA);
        return res;
    }

    private PersonId createPersonId() {
        final var res = new PersonId();
        res.setExtension(ProcessIncomingMessageAggregatorTest.PATIENT_ID);
        res.setRoot("");
        return res;
    }

    private IntygId createIntygsId() {
        final var res = new IntygId();
        res.setExtension(ProcessIncomingMessageAggregatorTest.CERTIFICATE_ID);
        res.setRoot("");
        return res;
    }
}
