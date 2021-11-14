package se.inera.intyg.webcert.web.service.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.builder.CertificateBuilder;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@ExtendWith(MockitoExtension.class)
class ReadyForSignFacadeServiceImplTest {

    @Mock
    private UtkastService utkastService;

    @Mock
    private GetCertificateFacadeService getCertificateFacadeService;

    @InjectMocks
    private ReadyForSignFacadeServiceImpl readyForSignFacadeService;

    private final static String CERTIFICATE_ID = "XXXXX-YYYYY-ZZZZZ";
    private final static String CERTIFICATE_TYPE = "certificateType";
    private Certificate certificate;

    @BeforeEach
    void setup() {
        certificate = CertificateBuilder.create()
            .metadata(
                CertificateMetadata.builder()
                    .id(CERTIFICATE_ID)
                    .build()
            )
            .build();

        doReturn(CERTIFICATE_TYPE)
            .when(utkastService)
            .getCertificateType(CERTIFICATE_ID);

        doReturn(certificate)
            .when(getCertificateFacadeService)
            .getCertificate(CERTIFICATE_ID, false);
    }

    @Test
    void shallSetAsReadyForSign() {
        final var expectedReadyForSign = LocalDateTime.now();

        certificate.getMetadata().setReadyForSign(expectedReadyForSign);

        final var actualCertificate = readyForSignFacadeService.setReadyForSign(CERTIFICATE_ID);

        assertEquals(expectedReadyForSign, actualCertificate.getMetadata().getReadyForSign());
        verify(utkastService).setKlarForSigneraAndSendStatusMessage(CERTIFICATE_ID, CERTIFICATE_TYPE);
    }
}