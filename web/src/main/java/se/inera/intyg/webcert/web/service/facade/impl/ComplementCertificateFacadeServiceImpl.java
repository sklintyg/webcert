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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.facade.ComplementCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.utkast.CopyUtkastService;
import se.inera.intyg.webcert.web.service.utkast.util.CopyUtkastServiceHelper;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygRequest;

@Service("complementCertificateFromWebcert")
public class ComplementCertificateFacadeServiceImpl implements ComplementCertificateFacadeService {

    private final GetCertificateFacadeService getCertificateFacadeService;
    private final ArendeService arendeService;
    private final CopyUtkastServiceHelper copyUtkastServiceHelper;
    private final CopyUtkastService copyUtkastService;

    @Autowired
    public ComplementCertificateFacadeServiceImpl(
        GetCertificateFacadeService getCertificateFacadeService, ArendeService arendeService,
        CopyUtkastServiceHelper copyUtkastServiceHelper, CopyUtkastService copyUtkastService) {
        this.getCertificateFacadeService = getCertificateFacadeService;
        this.arendeService = arendeService;
        this.copyUtkastServiceHelper = copyUtkastServiceHelper;
        this.copyUtkastService = copyUtkastService;
    }

    @Override
    public Certificate complement(String certificateId, String message) {
        final var certificate = getCertificateFacadeService.getCertificate(certificateId, false, true);

        final var newCertificateId = complement(certificate, message);
        return getCertificateFacadeService.getCertificate(newCertificateId, true, true);
    }

    @Override
    public Certificate answerComplement(String certificateId, String message) {
        arendeService.answerKomplettering(certificateId, message);
        return getCertificateFacadeService.getCertificate(certificateId, false, true);
    }

    private String complement(Certificate certificate, String message) {
        final var latestComplementQuestionId = arendeService.getLatestMeddelandeIdForCurrentCareUnit(certificate.getMetadata().getId());

        final var copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setKommentar(message);
        copyIntygRequest.setPatientPersonnummer(
            getPersonId(certificate.getMetadata().getPatient())
        );

        final var serviceRequest = copyUtkastServiceHelper.createCompletionCopyRequest(
            certificate.getMetadata().getId(),
            certificate.getMetadata().getType(),
            latestComplementQuestionId,
            copyIntygRequest
        );

        return copyUtkastService.createCompletion(serviceRequest).getNewDraftIntygId();
    }

    private Personnummer getPersonId(Patient patient) {
        if (patient.isReserveId()) {
            return Personnummer.createPersonnummer(patient.getPreviousPersonId().getId()).orElseThrow();
        }
        return Personnummer.createPersonnummer(patient.getPersonId().getId()).orElseThrow();
    }
}
