/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.facade.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.RevokeCertificateFacadeService;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@Service("revokeCertificateFromWC")
public class RevokeCertificateFacadeServiceImpl implements RevokeCertificateFacadeService {

    private static final Logger LOG = LoggerFactory.getLogger(RevokeCertificateFacadeServiceImpl.class);

    private final UtkastService utkastService;

    private final IntygService intygService;

    private final GetCertificateFacadeService getCertificateFacadeService;

    @Autowired
    public RevokeCertificateFacadeServiceImpl(UtkastService utkastService,
        IntygService intygService, GetCertificateFacadeService getCertificateFacadeService) {
        this.utkastService = utkastService;
        this.intygService = intygService;
        this.getCertificateFacadeService = getCertificateFacadeService;
    }

    @Override
    public Certificate revokeCertificate(String certificateId, String reason, String message) {
        final var certificate = getCertificateFacadeService.getCertificate(certificateId, false, true);

        if (isCertificateLocked(certificate)) {
            LOG.debug("Revoke locked draft {} with reason {}", certificateId, reason);
            utkastService.revokeLockedDraft(certificateId, certificate.getMetadata().getType(), message, reason);
        } else {
            LOG.debug("Revoke certificate {} with reason {}", certificateId, reason);
            intygService.revokeIntyg(certificateId, certificate.getMetadata().getType(), message, reason);
        }

        return getCertificateFacadeService.getCertificate(certificateId, false, true);
    }

    private boolean isCertificateLocked(Certificate certificate) {
        return certificate.getMetadata().getStatus() == CertificateStatus.LOCKED;
    }
}
