package se.inera.intyg.webcert.web.csintegration.certificate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.lisjp.v1.rest.LisjpModuleApiV1;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateDataElement;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.service.facade.impl.GetCertificateFacadeServiceImpl;

@ExtendWith(MockitoExtension.class)
class DecorateCertificateDataServiceTest {

    private static final String TYPE = "type";
    private static final String TYPE_VERSION = "typeVersion";
    private static final String PARENT_ID = "parentId";
    private static final Map<String, CertificateDataElement> DATA = Collections.emptyMap();
    @Mock
    CSIntegrationService csIntegrationService;
    @Mock
    GetCertificateFacadeServiceImpl getCertificateFacadeService;
    @Mock
    IntygModuleRegistry intygModuleRegistry;
    @InjectMocks
    DecorateCertificateDataService decorateCertificateDataService;

    @Test
    void shouldNotDecorateIfCertificateIsFoundInCS() {
        final var certificate = createCertificate();

        when(csIntegrationService.certificateExists(PARENT_ID)).thenReturn(true);

        decorateCertificateDataService.decorateFromParent(certificate);

        verifyNoInteractions(getCertificateFacadeService);
    }

    @Test
    void shouldDecorateCertificateIfCertificateNotFoundInCS() throws ModuleNotFoundException {
        final var certificate = createCertificate();
        final var parentCertificate = createCertificate();
        final var moduleApiV1 = mock(LisjpModuleApiV1.class);

        when(csIntegrationService.certificateExists(PARENT_ID)).thenReturn(false);
        when(getCertificateFacadeService.getCertificate(PARENT_ID, false, false)).thenReturn(parentCertificate);
        when(intygModuleRegistry.getModuleApi(TYPE, TYPE_VERSION)).thenReturn(moduleApiV1);

        decorateCertificateDataService.decorateFromParent(certificate);

        verify(moduleApiV1).decorate(certificate, DATA);
    }

    private Certificate createCertificate() {
        final var certificate = new Certificate();
        certificate.setMetadata(
            CertificateMetadata.builder()
                .type(TYPE)
                .typeVersion(TYPE_VERSION)
                .relations(
                    CertificateRelations.builder()
                        .parent(
                            CertificateRelation.builder()
                                .certificateId(PARENT_ID)
                                .build()
                        )
                        .build()
                )
                .build()
        );

        certificate.setData(DATA);

        return certificate;
    }
}