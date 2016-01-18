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

package se.inera.intyg.webcert.common.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultType;

import javax.xml.ws.WebServiceException;

/**
 * Created by eriklupander on 2015-06-04.
 */
@RunWith(MockitoJUnitRunner.class)
public class SendCertificateServiceClientTest {

    private static final String INTYGS_ID = "intyg-1";
    private static final String PERSON_ID = "person-1";
    private static final String RECIPIENT = "recipient-1";
    private static final String LOGICAL_ADDRESS = "logical-address-1";

    @Mock
    SendCertificateToRecipientResponderInterface sendService;

    @Mock
    SendCertificateToRecipientResponseType response;

    @InjectMocks
    SendCertificateServiceClientImpl testee = new SendCertificateServiceClientImpl();


    @Test
    public void testSendCertificateOk() {

        when(response.getResult()).thenReturn(buildResultOfCall(ResultCodeType.OK));

        when(sendService.sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class)))
                .thenReturn(response);
        SendCertificateToRecipientResponseType resp = testee.sendCertificate(INTYGS_ID, PERSON_ID, RECIPIENT, LOGICAL_ADDRESS);

        assertEquals(ResultCodeType.OK, resp.getResult().getResultCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSendCertificateNoIntygsId() {

        try {
            testee.sendCertificate(null, PERSON_ID, RECIPIENT, LOGICAL_ADDRESS);
        } catch (Exception e) {
            verifyZeroInteractions(sendService);
            throw e;
        }
        fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSendCertificateNoPersonId() {

        try {
            testee.sendCertificate(INTYGS_ID, null, RECIPIENT, LOGICAL_ADDRESS);
        } catch (Exception e) {
            verifyZeroInteractions(sendService);
            throw e;
        }
        fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSendCertificateNoRecipient() {

        try {
            testee.sendCertificate(INTYGS_ID, PERSON_ID, null, LOGICAL_ADDRESS);
        } catch (Exception e) {
            verifyZeroInteractions(sendService);
            throw e;
        }
        fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSendCertificateNoLogicalAddress() {

        try {
            testee.sendCertificate(INTYGS_ID, PERSON_ID, RECIPIENT, null);
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
        testee.sendCertificate(INTYGS_ID, PERSON_ID, RECIPIENT, LOGICAL_ADDRESS);

        verify(sendService, times(1)).sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class));
    }

    private ResultType buildResultOfCall(ResultCodeType resultCodeType) {
        ResultType roc = new ResultType();
        roc.setResultCode(resultCodeType);
        return roc;
    }
}
