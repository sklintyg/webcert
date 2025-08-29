package se.inera.intyg.webcert.web.csintegration.certificate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.service.facade.impl.GetCertificateFacadeServiceImpl;

@Service
@RequiredArgsConstructor
public class DecorateCertificateDataService {

    private final CSIntegrationService csIntegrationService;
    private final GetCertificateFacadeServiceImpl getCertificateFacadeService;
    private final IntygModuleRegistry intygModuleRegistry;

    public void decorateFromParent(Certificate certificate) {
        final var parentCertificateId = certificate.getMetadata()
            .getRelations()
            .getParent()
            .getCertificateId();

        if (Boolean.TRUE.equals(csIntegrationService.certificateExists(parentCertificateId))) {
            return;
        }

        final var parentCertificate = getCertificateFacadeService.getCertificate(
            parentCertificateId,
            false,
            false
        );

        final var metadata = parentCertificate.getMetadata();
        final var data = parentCertificate.getData();
        
        final var moduleApi = getModuleApi(metadata.getType(), metadata.getTypeVersion());
        moduleApi.decorate(certificate, data);
    }

    private ModuleApi getModuleApi(String certificateType, String certificateVersion) {
        try {
            return intygModuleRegistry.getModuleApi(certificateType, certificateVersion);
        } catch (ModuleNotFoundException ex) {
            throw new WebCertServiceException(
                WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM,
                String.format("Could not find ModuleAPI for certificate type '%s' and version %s", certificateType, certificateVersion),
                ex
            );
        }
    }

}