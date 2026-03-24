/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.clinicalprocess.healthcond.certificate.receiver.types.v1.CertificateReceiverRegistrationType;
import se.inera.clinicalprocess.healthcond.certificate.receiver.types.v1.CertificateReceiverType;
import se.inera.clinicalprocess.healthcond.certificate.receiver.types.v1.CertificateReceiverTypeType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listapprovedreceivers.v1.ListApprovedReceiversResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listapprovedreceivers.v1.ListApprovedReceiversResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listapprovedreceivers.v1.ListApprovedReceiversType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listpossiblereceivers.v1.ListPossibleReceiversResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listpossiblereceivers.v1.ListPossibleReceiversResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listpossiblereceivers.v1.ListPossibleReceiversType;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.certificatesender.CertificateSenderService;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygReceiver;

@ExtendWith(MockitoExtension.class)
public class CertificateReceiverServiceImplTest {

  private static final String RECEIVER_ID = "FKASSA";
  private static final String RECEIVER_NAME = "Försäkringskassan";

  @Mock private ListApprovedReceiversResponderInterface listApprovedReceiversClient;

  @Mock private ListPossibleReceiversResponderInterface listPossibleReceiversClient;

  @Mock private CertificateSenderService certificateSenderService;

  @InjectMocks private CertificateReceiverServiceImpl testee;

  @BeforeEach
  public void init() {
    ReflectionTestUtils.setField(testee, "logicalAddress", "123");
  }

  @Test
  public void testListAllowedAndApprovedReceivers() {
    when(listPossibleReceiversClient.listPossibleReceivers(
            anyString(), any(ListPossibleReceiversType.class)))
        .thenReturn(buildPossibleReceivers());
    when(listApprovedReceiversClient.listApprovedReceivers(
            anyString(), any(ListApprovedReceiversType.class)))
        .thenReturn(buildApprovedReceivers());
    List<IntygReceiver> resp = testee.listPossibleReceiversWithApprovedInfo("LISJP", "intyg-123");
    assertEquals(1, resp.size());

    verify(listPossibleReceiversClient, times(1))
        .listPossibleReceivers(anyString(), any(ListPossibleReceiversType.class));
  }

  private ListApprovedReceiversResponseType buildApprovedReceivers() {
    ListApprovedReceiversResponseType resp = new ListApprovedReceiversResponseType();

    CertificateReceiverRegistrationType certReceiverType =
        new CertificateReceiverRegistrationType();
    certReceiverType.setReceiverId(RECEIVER_ID);
    certReceiverType.setReceiverName(RECEIVER_NAME);
    certReceiverType.setReceiverType(CertificateReceiverTypeType.HUVUDMOTTAGARE);
    certReceiverType.setTrusted(true);
    resp.getReceiverList().add(certReceiverType);
    return resp;
  }

  @Test
  public void testListAllowedWithApprovedNullIntygsTyp() {
    assertThrows(WebCertServiceException.class, () -> {
    testee.listPossibleReceiversWithApprovedInfo(null, "intyg-123");
      });
  }

  @Test
  public void testListAllowedWithApprovedReceiversBlankIntygsTyp() {
    assertThrows(WebCertServiceException.class, () -> {
    testee.listPossibleReceiversWithApprovedInfo("", "intyg-123");
      });
  }

  @Test
  public void testRegisterApproved() {
    testee.registerApprovedReceivers("intyg-123", "lijsp", Arrays.asList("FKASSA", "TRANSP"));
    verify(certificateSenderService, times(1))
        .sendRegisterApprovedReceivers(anyString(), anyString(), anyString());
  }

  @Test
  public void testRegisterApprovedNullIntygsId() {
    assertThrows(WebCertServiceException.class, () -> {
    testee.registerApprovedReceivers(null, "lijsp", Arrays.asList("FKASSA"));
      });
  }

  @Test
  public void testListAllowedReceiversBlankIntygsId() {
    assertThrows(WebCertServiceException.class, () -> {
    testee.registerApprovedReceivers("", "lijsp", Arrays.asList("FKASSA"));
      });
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
