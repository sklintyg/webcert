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
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.facade.ReplaceCertificateFacadeService;
import se.inera.intyg.webcert.web.service.utkast.CopyUtkastService;
import se.inera.intyg.webcert.web.service.utkast.util.CopyUtkastServiceHelper;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygRequest;

@Service
public class ReplaceCertificateFacadeServiceImpl implements ReplaceCertificateFacadeService {

    private static final Logger LOG = LoggerFactory.getLogger(ReplaceCertificateFacadeServiceImpl.class);

    private final CopyUtkastServiceHelper copyUtkastServiceHelper;

    private final CopyUtkastService copyUtkastService;

    @Autowired
    public ReplaceCertificateFacadeServiceImpl(
        CopyUtkastServiceHelper copyUtkastServiceHelper, CopyUtkastService copyUtkastService) {
        this.copyUtkastServiceHelper = copyUtkastServiceHelper;
        this.copyUtkastService = copyUtkastService;
    }

    @Override
    public String replaceCertificate(String certificateId, String certificateType, String patientId) {
        final var copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(
            Personnummer.createPersonnummer(patientId).orElseThrow()
        );

        LOG.debug("Preparing to create a replacement for '{}' with type '{}'", certificateId, certificateType);
        final var serviceRequest = copyUtkastServiceHelper.createReplacementCopyRequest(certificateId, certificateType, copyIntygRequest);
        final var serviceResponse = copyUtkastService.createReplacementCopy(serviceRequest);

        LOG.debug("Created draft copy '{}'", serviceResponse.getNewDraftIntygId());
        return serviceResponse.getNewDraftIntygId();
    }
}
