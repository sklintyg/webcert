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

package se.inera.intyg.webcert.web.csintegration.certificate;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.w3._2000._09.xmldsig_.ReferenceType;
import org.w3._2000._09.xmldsig_.SignatureType;
import org.w3._2000._09.xmldsig_.SignedInfoType;
import se.inera.intyg.infra.xmldsig.model.IntygXMLDSignature;
import se.inera.intyg.infra.xmldsig.model.TransformAndDigestResponse;
import se.inera.intyg.infra.xmldsig.service.PrepareSignatureService;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SignCertificateRequestDTO;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;

@ExtendWith(MockitoExtension.class)
class SignCertificateServiceTest {

    private static final SignCertificateRequestDTO SIGN_CERTIFICATE_REQUEST = SignCertificateRequestDTO.builder().build();
    private static final String CERTIFICATE_ID = "certificateId";
    private static final String CERTIFICATE_XML = "<certificate></certificate>";
    private static final String SIGNATURE_XML = "<signature></signature>";
    private static final long VERSION = 1L;
    private static final String DIGEST_VALUE = "digestValue";
    private static final String MATCHING_DIGEST_VALUE_IN_BYTES = "ZGlnZXN0VmFsdWU=";
    @Mock
    private PrepareSignatureService prepareSignatureService;

    @Mock
    private CSIntegrationService csIntegrationService;

    @Mock
    private CSIntegrationRequestFactory csIntegrationRequestFactory;

    @InjectMocks
    private SignCertificateService signCertificateService;

    @Test
    void shouldSignCertificate() {
        final var ticket = mock(SignaturBiljett.class);
        final var xmldSignature = mock(IntygXMLDSignature.class);
        final var signatureType = mock(SignatureType.class);
        final var signedInfoType = mock(SignedInfoType.class);
        final var referenceType = mock(ReferenceType.class);

        doReturn(xmldSignature).when(ticket).getIntygSignature();
        doReturn(signatureType).when(xmldSignature).getSignatureType();
        doReturn(signedInfoType).when(signatureType).getSignedInfo();
        doReturn(List.of(referenceType)).when(signedInfoType).getReference();
        doReturn(DIGEST_VALUE.getBytes()).when(referenceType).getDigestValue();
        doReturn(new TransformAndDigestResponse(null, MATCHING_DIGEST_VALUE_IN_BYTES.getBytes())).when(prepareSignatureService)
            .transformAndGenerateDigest(CERTIFICATE_XML, CERTIFICATE_ID);
        doReturn(CERTIFICATE_ID).when(ticket).getIntygsId();
        doReturn(VERSION).when(ticket).getVersion();
        doReturn(SIGN_CERTIFICATE_REQUEST).when(csIntegrationRequestFactory)
            .signCertificateRequest(CERTIFICATE_XML, SIGNATURE_XML, VERSION);

        signCertificateService.sign(ticket, CERTIFICATE_XML, SIGNATURE_XML);
        verify(ticket).setStatus(SignaturStatus.SIGNERAD);
    }
}