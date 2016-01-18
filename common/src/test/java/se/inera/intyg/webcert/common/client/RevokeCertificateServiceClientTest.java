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
import static org.mockito.Mockito.*;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.rivtabp20.v1.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultOfCall;
import se.inera.intyg.webcert.common.client.converter.RevokeRequestConverter;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;

/**
 * Created by eriklupander on 2015-06-04.
 */
@RunWith(MockitoJUnitRunner.class)
public class RevokeCertificateServiceClientTest {

    private static final String LOGICAL_ADDRESS = "address-1";
    private static final String XML = "<xml/>";
    @Mock
    RevokeMedicalCertificateResponderInterface revokeMedicalCertificateResponderInterface;

    @Mock
    RevokeRequestConverter revokeRequestConverter;

    @InjectMocks
    RevokeCertificateServiceClientImpl testee = new RevokeCertificateServiceClientImpl();

    @Mock
    RevokeMedicalCertificateResponseType response;

    @Test
     public void testRevokeCertificateOk() {
        when(response.getResult()).thenReturn(buildResultOfCall(ResultCodeEnum.OK));
        when(revokeMedicalCertificateResponderInterface.revokeMedicalCertificate(any(AttributedURIType.class), any(RevokeMedicalCertificateRequestType.class)))
                .thenReturn(response);
        RevokeMedicalCertificateResponseType resp = testee.revokeCertificate(XML, LOGICAL_ADDRESS);
        assertEquals(ResultCodeEnum.OK, resp.getResult().getResultCode());


        verify(revokeMedicalCertificateResponderInterface).revokeMedicalCertificate(any(AttributedURIType.class), any(RevokeMedicalCertificateRequestType.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRevokeCertificateThrowsExceptionOnNullBody() {
        when(response.getResult()).thenReturn(buildResultOfCall(ResultCodeEnum.OK));
        when(revokeMedicalCertificateResponderInterface.revokeMedicalCertificate(any(AttributedURIType.class), any(RevokeMedicalCertificateRequestType.class)))
                .thenReturn(response);

        try {
            testee.revokeCertificate(null, LOGICAL_ADDRESS);
        } catch (Exception e) {
            verifyZeroInteractions(revokeMedicalCertificateResponderInterface);
            throw e;
        }
        fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRevokeCertificateThrowsExceptionOnNullLogicalAddress() {
        when(response.getResult()).thenReturn(buildResultOfCall(ResultCodeEnum.OK));
        when(revokeMedicalCertificateResponderInterface.revokeMedicalCertificate(any(AttributedURIType.class), any(RevokeMedicalCertificateRequestType.class)))
                .thenReturn(response);

        try {
            testee.revokeCertificate(XML, null);
        } catch (Exception e) {
            verifyZeroInteractions(revokeMedicalCertificateResponderInterface);
            throw e;
        }
        fail();
    }

    @Test(expected = WebCertServiceException.class)
    public void testRevokeCertificateThrowsExceptionOnJaxBException() throws JAXBException {
        when(revokeRequestConverter.fromXml(XML)).thenThrow(new JAXBException("Message"));
        when(response.getResult()).thenReturn(buildResultOfCall(ResultCodeEnum.OK));
        when(revokeMedicalCertificateResponderInterface.revokeMedicalCertificate(any(AttributedURIType.class), any(RevokeMedicalCertificateRequestType.class)))
                .thenReturn(response);

        try {
            testee.revokeCertificate(XML, LOGICAL_ADDRESS);
        } catch (Exception e) {
            verifyZeroInteractions(revokeMedicalCertificateResponderInterface);
            throw e;
        }
        fail();
    }

    private ResultOfCall buildResultOfCall(ResultCodeEnum resultCodeEnum) {
        ResultOfCall roc = new ResultOfCall();
        roc.setResultCode(resultCodeEnum);
        return roc;
    }


}
