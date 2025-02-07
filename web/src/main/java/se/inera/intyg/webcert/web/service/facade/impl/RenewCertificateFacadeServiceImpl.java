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
import se.inera.intyg.webcert.web.service.facade.RenewCertificateFacadeService;
import se.inera.intyg.webcert.web.service.utkast.CopyUtkastService;
import se.inera.intyg.webcert.web.service.utkast.util.CopyUtkastServiceHelper;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygRequest;

@Service("renewCertificateFromWebcert")
public class RenewCertificateFacadeServiceImpl implements RenewCertificateFacadeService {

    private static final Logger LOG = LoggerFactory.getLogger(RenewCertificateFacadeServiceImpl.class);

    private final CopyUtkastServiceHelper copyUtkastServiceHelper;
    private final CopyUtkastService copyUtkastService;
    private final GetCertificateFacadeService getCertificateFacadeService;

    @Autowired
    public RenewCertificateFacadeServiceImpl(CopyUtkastServiceHelper copyUtkastServiceHelper,
        CopyUtkastService copyUtkastService,
        GetCertificateFacadeService getCertificateFacadeService) {
        this.copyUtkastServiceHelper = copyUtkastServiceHelper;
        this.copyUtkastService = copyUtkastService;
        this.getCertificateFacadeService = getCertificateFacadeService;
    }

    @Override
    public String renewCertificate(String certificateId) {
        LOG.debug("Get certificate '{}' that will be renewed", certificateId);
        final var certificate = getCertificateFacadeService.getCertificate(certificateId, false, true);
        final var certificateType = certificate.getMetadata().getType();
        final var copyRequest = new CopyIntygRequest();
        copyRequest.setPatientPersonnummer(
            getPersonId(certificate.getMetadata().getPatient())
        );

        LOG.debug("Preparing to create a renewal for '{}' with type '{}'", certificateId, certificateType);
        final var renewalRequest = copyUtkastServiceHelper.createRenewalCopyRequest(certificateId, certificateType, copyRequest);

        LOG.debug("Create a renewal for '{}' with type '{}'", renewalRequest.getOriginalIntygId(), renewalRequest.getTyp());
        final var renewalCopy = copyUtkastService.createRenewalCopy(renewalRequest);

        LOG.debug("Return renewal draft '{}' ", renewalCopy.getNewDraftIntygId());
        return renewalCopy.getNewDraftIntygId();
    }

    private Personnummer getPersonId(Patient patient) {
        if (patient.isReserveId()) {
            return Personnummer.createPersonnummer(patient.getPreviousPersonId().getId()).orElseThrow();
        }
        return Personnummer.createPersonnummer(patient.getPersonId().getId()).orElseThrow();
    }
}
