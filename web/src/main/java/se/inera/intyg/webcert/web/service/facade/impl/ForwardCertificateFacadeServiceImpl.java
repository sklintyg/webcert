/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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
import se.inera.intyg.webcert.web.service.facade.ForwardCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@Service
public class ForwardCertificateFacadeServiceImpl implements ForwardCertificateFacadeService {

    private static final Logger LOG = LoggerFactory.getLogger(ForwardCertificateFacadeServiceImpl.class);

    private final UtkastService utkastService;

    private final GetCertificateFacadeService getCertificateFacadeService;

    @Autowired
    public ForwardCertificateFacadeServiceImpl(UtkastService utkastService,
        GetCertificateFacadeService getCertificateFacadeService) {
        this.utkastService = utkastService;
        this.getCertificateFacadeService = getCertificateFacadeService;
    }

    @Override
    public Certificate forwardCertificate(String certificateId, long version, boolean forwarded) {
        LOG.debug("Set certificate '{}' with version '{}' as forwarded '{}'", certificateId, version, forwarded);
        final var certificate = utkastService.setNotifiedOnDraft(certificateId, version, forwarded);
        LOG.debug("Get the forwarded certificate '{}'", certificateId);
        return getCertificateFacadeService.getCertificate(certificate.getIntygsId(), false);
    }
}
