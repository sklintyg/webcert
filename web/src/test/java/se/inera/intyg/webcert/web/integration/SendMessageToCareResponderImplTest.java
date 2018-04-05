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
package se.inera.intyg.webcert.web.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType.SkickatAv;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Amneskod;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Part;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;

@RunWith(MockitoJUnitRunner.class)
public class SendMessageToCareResponderImplTest {

    private static final String DEFAULT_LOGICAL_ADDRESS = "webcert";
    private static final String DEFAULT_INTYG_ID = "intyg-1";
    private static final String DEFAULT_MEDDELANDE_ID = "1";
    private static final String DEFAULT_PATIENT_ID = "191212121212";

    @Mock
    private NotificationService mockNotificationService;

    @Mock
    private ArendeService arendeService;

    @InjectMocks
    private SendMessageToCareResponderImpl responder;

    @Test
    public void testSendRequestToService() throws WebCertServiceException {
        when(arendeService.processIncomingMessage(any())).thenReturn(new Arende());
        SendMessageToCareResponseType response = responder.sendMessageToCare(DEFAULT_LOGICAL_ADDRESS, createNewRequest());
        assertNotNull(response.getResult());
        assertEquals(response.getResult().getResultCode(), ResultCodeType.OK);
    }

    @Test
    public void testSendRequestToServiceFailed() throws WebCertServiceException {
        when(arendeService.processIncomingMessage(any()))
                .thenThrow(new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, ""));
        SendMessageToCareResponseType response = responder.sendMessageToCare(DEFAULT_LOGICAL_ADDRESS, createNewRequest());
        assertNotNull(response.getResult());
        assertEquals(ResultCodeType.ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdType.APPLICATION_ERROR, response.getResult().getErrorId());
    }

    @Test
    public void testSendRequestToServiceFailedNotSigned() throws WebCertServiceException {
        when(arendeService.processIncomingMessage(any()))
                .thenThrow(new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, ""));
        SendMessageToCareResponseType response = responder.sendMessageToCare(DEFAULT_LOGICAL_ADDRESS, createNewRequest());
        assertNotNull(response.getResult());
        assertEquals(ResultCodeType.ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdType.VALIDATION_ERROR, response.getResult().getErrorId());
    }

    @Test
    public void testSendRequestToServiceFailedNotFound() throws WebCertServiceException {
        when(arendeService.processIncomingMessage(any()))
                .thenThrow(new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, ""));
        SendMessageToCareResponseType response = responder.sendMessageToCare(DEFAULT_LOGICAL_ADDRESS, createNewRequest());
        assertNotNull(response.getResult());
        assertEquals(ResultCodeType.ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdType.VALIDATION_ERROR, response.getResult().getErrorId());
    }

    @Test
    public void testSendRequestToServiceFailedExternalServiceProblem() throws WebCertServiceException {
        when(arendeService.processIncomingMessage(any()))
                .thenThrow(new WebCertServiceException(WebCertServiceErrorCodeEnum.EXTERNAL_SYSTEM_PROBLEM, ""));
        SendMessageToCareResponseType response = responder.sendMessageToCare(DEFAULT_LOGICAL_ADDRESS, createNewRequest());
        assertNotNull(response.getResult());
        assertEquals(ResultCodeType.ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdType.VALIDATION_ERROR, response.getResult().getErrorId());
    }

    private SendMessageToCareType createNewRequest() {
        SendMessageToCareType res = new SendMessageToCareType();
        res.setAmne(new Amneskod());
        res.getAmne().setCode(ArendeAmne.KONTKT.toString());
        res.setIntygsId(createIntygsId(DEFAULT_INTYG_ID));
        res.setMeddelandeId(DEFAULT_MEDDELANDE_ID);
        res.setPatientPersonId(createPersonId(DEFAULT_PATIENT_ID));
        res.setSkickatAv(createSkickadAv());

        return res;
    }

    private SkickatAv createSkickadAv() {
        SkickatAv res = new SkickatAv();
        res.setPart(new Part());
        res.getPart().setCode("FKASSA");
        return res;
    }

    private PersonId createPersonId(String patientId) {
        PersonId res = new PersonId();
        res.setExtension(patientId);
        res.setRoot("");
        return res;
    }

    private IntygId createIntygsId(String intygId) {
        IntygId res = new IntygId();
        res.setExtension(intygId);
        res.setRoot("");
        return res;
    }
}
