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
package se.inera.intyg.webcert.notification_sender.certificatesender.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.clinicalprocess.healthcond.certificate.registerapprovedreceivers.v1.ReceiverApprovalStatus;
import se.inera.intyg.clinicalprocess.healthcond.certificate.registerapprovedreceivers.v1.RegisterApprovedReceiversResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.registerapprovedreceivers.v1.RegisterApprovedReceiversResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.registerapprovedreceivers.v1.RegisterApprovedReceiversType;
import se.inera.intyg.webcert.common.sender.exception.PermanentException;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.riv.clinicalprocess.healthcond.certificate.receiver.types.v1.ApprovalStatusType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;

import javax.xml.ws.WebServiceException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RegisterApprovedReceiversProcessorTest {

    private static final String INTYG_ID = "intyg-1";
    private static final String INTYG_TYP = "lijsp";

    private static final String LOGICAL_ADDRESS = "logisk-adress";

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RegisterApprovedReceiversResponderInterface registerApprovedReceiversClient;

    @InjectMocks
    private RegisterApprovedReceiversProcessor testee;

    @Test
    public void testRegisterOk() throws TemporaryException, PermanentException, JsonProcessingException {
        when(registerApprovedReceiversClient.registerApprovedReceivers(anyString(), any(RegisterApprovedReceiversType.class))).thenReturn(buildResponse(ResultCodeType.OK));

        testee.process(buildRequestBody("FKASSA", "FBA"), INTYG_ID, INTYG_TYP, LOGICAL_ADDRESS);
        verify(registerApprovedReceiversClient, times(1)).registerApprovedReceivers(anyString(), any(RegisterApprovedReceiversType.class));
    }

    @Test(expected = TemporaryException.class)
    public void testRegisterThrowsTemporaryExceptionOnWebServiceException() throws TemporaryException, PermanentException, JsonProcessingException {
        when(registerApprovedReceiversClient.registerApprovedReceivers(anyString(), any(RegisterApprovedReceiversType.class))).thenThrow(new WebServiceException(""));
        try {
            testee.process(buildRequestBody("FKASSA", "FBA"), INTYG_ID, INTYG_TYP, LOGICAL_ADDRESS);
        } finally {
            verify(registerApprovedReceiversClient, times(1)).registerApprovedReceivers(anyString(), any(RegisterApprovedReceiversType.class));
        }
    }

    @Test(expected = PermanentException.class)
    public void testRegisterThrowsPermanentExceptionOnError() throws TemporaryException, PermanentException, JsonProcessingException {
        when(registerApprovedReceiversClient.registerApprovedReceivers(anyString(), any(RegisterApprovedReceiversType.class))).thenReturn(buildResponse(ResultCodeType.ERROR));
        try {
            testee.process(buildRequestBody("FKASSA", "FBA"), INTYG_ID, INTYG_TYP, LOGICAL_ADDRESS);
        } finally {
            verify(registerApprovedReceiversClient, times(1)).registerApprovedReceivers(anyString(), any(RegisterApprovedReceiversType.class));
        }
    }

    @Test(expected = PermanentException.class)
    public void testUnparsableBodyThrowsPermanentException() throws TemporaryException, PermanentException, JsonProcessingException {
        try {
            testee.process("this-is-not-json", INTYG_ID, INTYG_TYP, LOGICAL_ADDRESS);
        } finally {
            verifyZeroInteractions(registerApprovedReceiversClient);
        }
    }


    @Test(expected = PermanentException.class)
    public void testRegisterThrowsPermanentExceptionOnMissingIntygsId() throws TemporaryException, PermanentException, JsonProcessingException {
        try {
            testee.process(buildRequestBody("FKASSA", "FBA"), null, INTYG_TYP, LOGICAL_ADDRESS);
        } finally {
            verifyZeroInteractions(registerApprovedReceiversClient);
        }
    }

    @Test(expected = PermanentException.class)
    public void testRegisterThrowsPermanentExceptionOnMissingIntygsTyp() throws TemporaryException, PermanentException, JsonProcessingException {
        try {
            testee.process(buildRequestBody("FKASSA", "FBA"), INTYG_ID, null, LOGICAL_ADDRESS);
        } finally {
            verifyZeroInteractions(registerApprovedReceiversClient);
        }
    }

    @Test(expected = PermanentException.class)
    public void testRegisterThrowsPermanentExceptionOnBlankIntygsTyp() throws TemporaryException, PermanentException, JsonProcessingException {
        try {
            testee.process(buildRequestBody("FKASSA", "FBA"), INTYG_ID, "", LOGICAL_ADDRESS);
        } finally {
            verifyZeroInteractions(registerApprovedReceiversClient);
        }
    }

    private RegisterApprovedReceiversResponseType buildResponse(ResultCodeType resultCodeType) {
        RegisterApprovedReceiversResponseType resp = new RegisterApprovedReceiversResponseType();

        ResultType resultType = new ResultType();
        resultType.setResultCode(resultCodeType);
        resp.setResult(resultType);
        return resp;
    }

    private String buildRequestBody(String... mottagare) throws JsonProcessingException {
        List<ReceiverApprovalStatus> approved = Stream.of(mottagare).map(receiverId -> {
            ReceiverApprovalStatus ras = new ReceiverApprovalStatus();
            ras.setReceiverId(receiverId);
            ras.setApprovalStatus(ApprovalStatusType.YES);
            return ras;
        }).collect(Collectors.toList());
        return objectMapper.writeValueAsString(approved);
    }
}
