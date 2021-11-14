package se.inera.intyg.webcert.web.service.facade.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.ReadyForSignFacadeService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@Service
public class ReadyForSignFacadeServiceImpl implements ReadyForSignFacadeService {

    private static final Logger LOG = LoggerFactory.getLogger(ReadyForSignFacadeServiceImpl.class);

    private final UtkastService utkastService;
    private final GetCertificateFacadeService getCertificateFacadeService;

    public ReadyForSignFacadeServiceImpl(UtkastService utkastService,
        GetCertificateFacadeService getCertificateFacadeService) {
        this.utkastService = utkastService;
        this.getCertificateFacadeService = getCertificateFacadeService;
    }

    @Override
    public Certificate readyForSign(String certificateId) {
        LOG.debug("Get certificate type for certificate '{}'", certificateId);
        final var certificateType = utkastService.getCertificateType(certificateId);

        LOG.debug("Set certificate '{}' as 'ready to sign'", certificateId);
        utkastService.setKlarForSigneraAndSendStatusMessage(certificateId, certificateType);

        LOG.debug("Get the 'ready to sign' certificate '{}'", certificateId);
        return getCertificateFacadeService.getCertificate(certificateId, false);
    }
}
