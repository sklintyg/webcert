package se.inera.intyg.webcert.web.csintegration.aggregate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.facade.SaveCertificateFacadeService;

@ExtendWith(MockitoExtension.class)
class SaveCertificateAggregatorTest {

    @Mock
    SaveCertificateFacadeService saveCertificateFacadeServiceInWC;
    @Mock
    SaveCertificateFacadeService saveCertificateFacadeServiceInCS;
    @Mock
    CertificateServiceProfile certificateServiceProfile;
    SaveCertificateAggregator aggregator;

    private static final Certificate CERTIFICATE = new Certificate();
    private static final boolean PDL_LOG = false;
    private static final long VERSION_FROM_WC = 1;
    private static final long VERSION_FROM_CS = 99;

    @BeforeEach
    void setup() {
        aggregator = new SaveCertificateAggregator(
            saveCertificateFacadeServiceInWC,
            saveCertificateFacadeServiceInCS,
            certificateServiceProfile
        );
    }

    @Test
    void shallSaveCertificateInWebcertIfCertificateServiceIsNotActive() {
        doReturn(false).when(certificateServiceProfile).active();
        doReturn(VERSION_FROM_WC).when(saveCertificateFacadeServiceInWC).saveCertificate(CERTIFICATE, PDL_LOG);
        assertEquals(VERSION_FROM_WC,
            aggregator.saveCertificate(CERTIFICATE, PDL_LOG)
        );
    }

    @Test
    void shallSaveCertificateInCSIfCertificateServiceIsActive() {
        doReturn(true).when(certificateServiceProfile).active();
        doReturn(VERSION_FROM_CS).when(saveCertificateFacadeServiceInCS).saveCertificate(CERTIFICATE, PDL_LOG);
        assertEquals(VERSION_FROM_CS,
            aggregator.saveCertificate(CERTIFICATE, PDL_LOG)
        );
    }

    @Test
    void shallSaveCertificateInWCIfCertificateServiceIsActiveButCSReturnsNegativeVersion() {
        doReturn(true).when(certificateServiceProfile).active();
        doReturn(-1L).when(saveCertificateFacadeServiceInCS).saveCertificate(CERTIFICATE, PDL_LOG);
        doReturn(VERSION_FROM_WC).when(saveCertificateFacadeServiceInWC).saveCertificate(CERTIFICATE, PDL_LOG);
        assertEquals(VERSION_FROM_WC,
            aggregator.saveCertificate(CERTIFICATE, PDL_LOG)
        );
    }
}