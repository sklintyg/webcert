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
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.ReadyForSignFacadeService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@Service("readyForSignForWC")
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
        return getCertificateFacadeService.getCertificate(certificateId, false, true);
    }
}
