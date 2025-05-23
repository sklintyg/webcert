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
import se.inera.intyg.webcert.web.service.facade.DeleteCertificateFacadeService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@Service("deleteCertificateFromWebcert")
public class DeleteCertificateFacadeServiceImpl implements DeleteCertificateFacadeService {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteCertificateFacadeServiceImpl.class);

    private final UtkastService utkastService;

    @Autowired
    public DeleteCertificateFacadeServiceImpl(UtkastService utkastService) {
        this.utkastService = utkastService;
    }

    @Override
    public boolean deleteCertificate(String certificateId, long version) {
        LOG.debug("Deleting certificate '{}' with version '{}'", certificateId, version);
        utkastService.deleteUnsignedDraft(certificateId, version);
        LOG.debug("Certificate '{}' deleted!", certificateId);

        return true;
    }
}
