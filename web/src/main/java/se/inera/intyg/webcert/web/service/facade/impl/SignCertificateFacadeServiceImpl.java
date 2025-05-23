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

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.SignCertificateFacadeService;
import se.inera.intyg.webcert.web.service.underskrift.UnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignMethod;

@Service
public class SignCertificateFacadeServiceImpl implements SignCertificateFacadeService {

    private static final Logger LOG = LoggerFactory.getLogger(SignCertificateFacadeServiceImpl.class);

    private final UnderskriftService underskriftService;
    private final GetCertificateFacadeService getCertificateFacadeService;

    public SignCertificateFacadeServiceImpl(@Qualifier("signAggregator") UnderskriftService signatureAggregator,
        @Qualifier("getCertificateAggregator") GetCertificateFacadeService getCertificateFacadeService) {
        this.underskriftService = signatureAggregator;
        this.getCertificateFacadeService = getCertificateFacadeService;
    }

    @Override
    public Certificate signCertificate(Certificate certificate) {
        final var certificateId = certificate.getMetadata().getId();
        final var certificateType = certificate.getMetadata().getType();
        final var version = certificate.getMetadata().getVersion();
        final var signMethod = SignMethod.FAKE;
        final var ticketId = UUID.randomUUID().toString();
        final var userIpAddress = "127.0.0.1";

        LOG.debug("Start fake signing process for certificate '{}'", certificateId);
        underskriftService.startSigningProcess(certificateId, certificateType, version, signMethod, ticketId, userIpAddress);

        LOG.debug("Make fake signature for certificate '{}'", certificateId);
        underskriftService.fakeSignature(certificateId, certificateType, version, ticketId);

        LOG.debug("Get signed certificate '{}' and return", certificateId);
        return getCertificateFacadeService.getCertificate(certificateId, false, true);
    }
}
