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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.service.facade.RenewCertificateFacadeService;
import se.inera.intyg.webcert.web.service.utkast.CopyUtkastService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.util.CopyUtkastServiceHelper;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygRequest;

@Service
public class RenewCertificateFacadeServiceImpl implements RenewCertificateFacadeService {

    private final CopyUtkastServiceHelper copyUtkastServiceHelper;
    private final CopyUtkastService copyUtkastService;
    private final UtkastService utkastService;

    @Autowired
    public RenewCertificateFacadeServiceImpl(CopyUtkastServiceHelper copyUtkastServiceHelper,
        CopyUtkastService copyUtkastService, UtkastService utkastService) {
        this.copyUtkastServiceHelper = copyUtkastServiceHelper;
        this.copyUtkastService = copyUtkastService;
        this.utkastService = utkastService;
    }

    @Override
    public String renewCertificate(String certificateId) {
        final var certificate = utkastService.getDraft(certificateId, false);

        final var certificateType = certificate.getIntygsTyp();

        final var copyRequest = new CopyIntygRequest();
        copyRequest.setPatientPersonnummer(certificate.getPatientPersonnummer());

        final var renewalRequest = copyUtkastServiceHelper.createRenewalCopyRequest(certificateId, certificateType, copyRequest);
        final var renewalCopy = copyUtkastService.createRenewalCopy(renewalRequest);
        return renewalCopy.getNewDraftIntygId();
    }
}
