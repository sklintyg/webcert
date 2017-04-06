/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.common.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import javax.xml.ws.WebServiceException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.fasterxml.jackson.databind.ObjectMapper;

import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v2.SendCertificateToRecipientResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v2.SendCertificateToRecipientResponseType;
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v2.SendCertificateToRecipientType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;

/**
 * Created by eriklupander on 2015-06-04.
 */
@RunWith(MockitoJUnitRunner.class)
public class SendCertificateServiceClientTest {

    private static final String INTYGS_ID = "intyg-1";
    private static final String PERSON_ID = "person-1";
    private static final String SKICKAT_AV_JSON = createSkickatAvJson();
    private static final String RECIPIENT = "FKASSA";
    private static final String LOGICAL_ADDRESS = "logical-address-1";

    @Mock
    SendCertificateToRecipientResponderInterface sendService;

    @Mock
    SendCertificateToRecipientResponseType response;

    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    SendCertificateServiceClientImpl testee = new SendCertificateServiceClientImpl();

    private static String createSkickatAvJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"personId\":\"skapad av pid\",\"fullstandigtNamn\":\"fullständigt namn\",\"forskrivarKod\":");
        sb.append("\"forskrivarKod\",\"befattningar\":[],\"specialiteter\":[],\"vardenhet\":{\"enhetsid\":\"enhetsid\",");
        sb.append("\"enhetsnamn\":\"enhetsnamn\",\"postadress\":\"postadress\",\"postnummer\":\"postNummer\",\"postort\":");
        sb.append("\"postOrt\",\"telefonnummer\":\"telefonNummer\",\"epost\":\"epost\",\"vardgivare\":{\"vardgivarid\":");
        sb.append("\"vardgivarid\",\"vardgivarnamn\":\"vardgivarNamn\"},\"arbetsplatsKod\":\"arbetsplatsKod\"}}");
        return sb.toString();
    }

    @Before
    public void setup() throws Exception {
        when(objectMapper.readValue(anyString(), eq(HoSPersonal.class))).then(new Answer<HoSPersonal>() {

            @Override
            public HoSPersonal answer(InvocationOnMock invocation) throws Throwable {
                return new ObjectMapper().readValue((String) invocation.getArguments()[0], HoSPersonal.class);
            }
        });
    }

    @Test
    public void testSendCertificateOk() {

        when(response.getResult()).thenReturn(buildResultOfCall(ResultCodeType.OK));

        when(sendService.sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class)))
                .thenReturn(response);
        SendCertificateToRecipientResponseType resp = testee.sendCertificate(INTYGS_ID, PERSON_ID, SKICKAT_AV_JSON, RECIPIENT,
                LOGICAL_ADDRESS);

        assertEquals(ResultCodeType.OK, resp.getResult().getResultCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSendCertificateNoIntygsId() {

        try {
            testee.sendCertificate(null, PERSON_ID, SKICKAT_AV_JSON, RECIPIENT, LOGICAL_ADDRESS);
        } catch (Exception e) {
            verifyZeroInteractions(sendService);
            throw e;
        }
        fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSendCertificateNoPersonId() {

        try {
            testee.sendCertificate(INTYGS_ID, null, SKICKAT_AV_JSON, RECIPIENT, LOGICAL_ADDRESS);
        } catch (Exception e) {
            verifyZeroInteractions(sendService);
            throw e;
        }
        fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSendCertificateNoRecipient() {

        try {
            testee.sendCertificate(INTYGS_ID, PERSON_ID, SKICKAT_AV_JSON, null, LOGICAL_ADDRESS);
        } catch (Exception e) {
            verifyZeroInteractions(sendService);
            throw e;
        }
        fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSendCertificateNoLogicalAddress() {

        try {
            testee.sendCertificate(INTYGS_ID, SKICKAT_AV_JSON, PERSON_ID, RECIPIENT, null);
        } catch (Exception e) {
            verifyZeroInteractions(sendService);
            throw e;
        }
        fail();
    }

    /**
     * It's important that the testee is not catching exceptions emitted by the WebService client code. It's up to the
     * caller of the testee to handle exceptions.
     */
    @Test(expected = WebServiceException.class)
    public void testExceptionsAreForwardedAsIs() {
        when(sendService.sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class)))
                .thenThrow(new WebServiceException("FOO BAR"));
        testee.sendCertificate(INTYGS_ID, PERSON_ID, SKICKAT_AV_JSON, RECIPIENT, LOGICAL_ADDRESS);

        verify(sendService, times(1)).sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class));
    }

    private ResultType buildResultOfCall(ResultCodeType resultCodeType) {
        ResultType roc = new ResultType();
        roc.setResultCode(resultCodeType);
        return roc;
    }
}
