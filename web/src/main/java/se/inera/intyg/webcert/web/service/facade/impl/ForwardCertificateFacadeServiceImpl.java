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
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.facade.ForwardCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.util.UtkastToCertificateConverter;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@Service("forwardCertificateFromWC")
public class ForwardCertificateFacadeServiceImpl implements ForwardCertificateFacadeService {

    private static final Logger LOG = LoggerFactory.getLogger(ForwardCertificateFacadeServiceImpl.class);

    private final UtkastService utkastService;

    private final GetCertificateFacadeService getCertificateFacadeService;

    private final UtkastToCertificateConverter utkastToCertificateConverter;

    private final ArendeService arendeService;

    @Autowired
    public ForwardCertificateFacadeServiceImpl(UtkastService utkastService,
        GetCertificateFacadeService getCertificateFacadeService,
        UtkastToCertificateConverter utkastToCertificateConverter,
        ArendeService arendeService) {
        this.utkastService = utkastService;
        this.getCertificateFacadeService = getCertificateFacadeService;
        this.utkastToCertificateConverter = utkastToCertificateConverter;
        this.arendeService = arendeService;
    }

    @Override
    public Certificate forwardCertificate(String certificateId, boolean forwarded) {
        final var certificate = getCertificateFacadeService.getCertificate(certificateId, false, true);

        if (certificate.getMetadata().getStatus() != CertificateStatus.SIGNED) {
            return forwardDraft(certificateId, certificate, forwarded);
        } else {
            arendeService.setForwarded(certificateId);
            return certificate;
        }
    }

    private Certificate forwardDraft(String certificateId, Certificate certificate, boolean forwarded) {
        LOG.debug("Set certificate '{}' with version '{}' as forwarded '{}'", certificateId,
            certificate.getMetadata().getVersion(), forwarded);
        final var draft = utkastService.setNotifiedOnDraft(certificateId,
            certificate.getMetadata().getVersion(), forwarded);

        LOG.debug("Get the forwarded certificate '{}'", certificateId);
        return utkastToCertificateConverter.convert(draft);
    }
}
