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
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.ReplaceCertificateFacadeService;
import se.inera.intyg.webcert.web.service.utkast.CopyUtkastService;
import se.inera.intyg.webcert.web.service.utkast.util.CopyUtkastServiceHelper;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygRequest;

@Service("replaceCertificateFromWebcert")
public class ReplaceCertificateFacadeServiceImpl implements ReplaceCertificateFacadeService {

    private static final Logger LOG = LoggerFactory.getLogger(ReplaceCertificateFacadeServiceImpl.class);

    private final CopyUtkastServiceHelper copyUtkastServiceHelper;

    private final CopyUtkastService copyUtkastService;

    private final GetCertificateFacadeService getCertificateFacadeService;

    @Autowired
    public ReplaceCertificateFacadeServiceImpl(
        CopyUtkastServiceHelper copyUtkastServiceHelper, CopyUtkastService copyUtkastService,
        GetCertificateFacadeService getCertificateFacadeService) {
        this.copyUtkastServiceHelper = copyUtkastServiceHelper;
        this.copyUtkastService = copyUtkastService;
        this.getCertificateFacadeService = getCertificateFacadeService;
    }

    @Override
    public String replaceCertificate(String certificateId) {
        LOG.debug("Get certificate '{}' that will be replaced", certificateId);
        final var certificate = getCertificateFacadeService.getCertificate(certificateId, false, true);
        final var certificateType = certificate.getMetadata().getType();
        final var copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(
            getPersonId(certificate.getMetadata().getPatient())
        );

        LOG.debug("Preparing to create a replacement for '{}' with type '{}'", certificateId, certificateType);
        final var serviceRequest = copyUtkastServiceHelper.createReplacementCopyRequest(certificateId, certificateType, copyIntygRequest);
        final var serviceResponse = copyUtkastService.createReplacementCopy(serviceRequest);

        LOG.debug("Created replacement '{}'", serviceResponse.getNewDraftIntygId());
        return serviceResponse.getNewDraftIntygId();
    }

    private Personnummer getPersonId(Patient patient) {
        if (patient.isReserveId()) {
            return Personnummer.createPersonnummer(patient.getPreviousPersonId().getId()).orElseThrow();
        }
        return Personnummer.createPersonnummer(patient.getPersonId().getId()).orElseThrow();
    }
}
