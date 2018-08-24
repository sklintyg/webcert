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
package se.inera.intyg.webcert.web.service.receiver;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listpossiblereceivers.v1.ListPossibleReceiversResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listpossiblereceivers.v1.ListPossibleReceiversResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listpossiblereceivers.v1.ListPossibleReceiversType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.registerapprovedreceivers.v1.RegisterApprovedReceiversResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.registerapprovedreceivers.v1.RegisterApprovedReceiversType;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygReceiver;
import se.riv.clinicalprocess.healthcond.certificate.receiver.types.v1.CertificateReceiverType;
import se.riv.clinicalprocess.healthcond.certificate.receiver.types.v1.CertificateReceiverTypeType;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CertificateReceiverServiceImplTest {

    private static final String RECEIVER_ID = "FKASSA";
    private static final String RECEIVER_NAME = "Försäkringskassan";
    @Mock
    private ListPossibleReceiversResponderInterface listPossibleReceiversClient;

    @Mock
    private RegisterApprovedReceiversResponderInterface registerApprovedReceiversClient;

    @InjectMocks
    private CertificateReceiverServiceImpl testee;

    @Before
    public void init() {
        ReflectionTestUtils.setField(testee, "logicalAddress", "123");
    }

    @Test
    public void testListAllowedReceivers() {
        when(listPossibleReceiversClient.listPossibleReceivers(anyString(), any(ListPossibleReceiversType.class)))
                .thenReturn(buildPossibleReceivers());
        List<IntygReceiver> resp = testee.listPossibleReceivers("LISJP");
        assertEquals(1, resp.size());

        verify(listPossibleReceiversClient, times(1)).listPossibleReceivers(anyString(), any(ListPossibleReceiversType.class));
    }

    @Test(expected = WebCertServiceException.class)
    public void testListAllowedReceiversNullIntygsTyp() {
        testee.listPossibleReceivers(null);
    }

    @Test(expected = WebCertServiceException.class)
    public void testListAllowedReceiversBlankIntygsTyp() {
        testee.listPossibleReceivers("");
    }

    @Test
    public void testRegisterApproved() {
        testee.registerApprovedReceivers("intyg-123", Arrays.asList("FKASSA", "TRANSP"));
        verify(registerApprovedReceiversClient, times(1)).registerApprovedReceivers(anyString(), any(RegisterApprovedReceiversType.class));
    }

    @Test(expected = WebCertServiceException.class)
    public void testRegisterApprovedNullIntygsId() {
        testee.registerApprovedReceivers(null, Arrays.asList("FKASSA"));
    }

    @Test(expected = WebCertServiceException.class)
    public void testListAllowedReceiversBlankIntygsId() {
        testee.registerApprovedReceivers("", Arrays.asList("FKASSA"));
    }

    private ListPossibleReceiversResponseType buildPossibleReceivers() {
        ListPossibleReceiversResponseType resp = new ListPossibleReceiversResponseType();
        CertificateReceiverType certReceiverType = new CertificateReceiverType();
        certReceiverType.setReceiverId(RECEIVER_ID);
        certReceiverType.setReceiverName(RECEIVER_NAME);
        certReceiverType.setReceiverType(CertificateReceiverTypeType.HUVUDMOTTAGARE);
        certReceiverType.setTrusted(true);
        resp.getReceiverList().add(certReceiverType);
        return resp;
    }

}
