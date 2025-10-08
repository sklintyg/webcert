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
import se.inera.intyg.common.ag7804.support.Ag7804EntryPoint;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.facade.CreateCertificateFromTemplateFacadeService;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.utkast.CopyUtkastService;
import se.inera.intyg.webcert.web.service.utkast.util.CopyUtkastServiceHelper;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygRequest;

@Service("createCertificateFromTemplateFromWC")
public class CreateCertificateFromTemplateFacadeServiceImpl implements CreateCertificateFromTemplateFacadeService {

    private static final Logger LOG = LoggerFactory.getLogger(CreateCertificateFromTemplateFacadeServiceImpl.class);

    private final CopyUtkastServiceHelper copyUtkastServiceHelper;
    private final CopyUtkastService copyUtkastService;
    private final IntygTextsService intygTextsService;
    private final GetCertificateFacadeService getCertificateFacadeService;

    @Autowired
    public CreateCertificateFromTemplateFacadeServiceImpl(CopyUtkastServiceHelper copyUtkastServiceHelper,
        CopyUtkastService copyUtkastService, IntygTextsService intygTextsService, GetCertificateFacadeService getCertificateFacadeService) {
        this.copyUtkastServiceHelper = copyUtkastServiceHelper;
        this.copyUtkastService = copyUtkastService;
        this.intygTextsService = intygTextsService;
        this.getCertificateFacadeService = getCertificateFacadeService;
    }

    @Override
    public String createCertificateFromTemplate(String certificateId) {
        LOG.debug("Get certificate '{}' that will be used as template", certificateId);
        final var certificate = getCertificateFacadeService.getCertificate(certificateId, false, true);
        final var certificateType = certificate.getMetadata().getType();
        final var newCertificateType = getNewCertificateType(certificateType);
        final var copyRequest = new CopyIntygRequest();
        copyRequest.setPatientPersonnummer(
            getPersonId(certificate.getMetadata().getPatient())
        );

        LOG.debug("Preparing to create a renewal from template for '{}' with new type '{}' from old type '{}'", certificateId,
            newCertificateType, certificateType);
        final var request = copyUtkastServiceHelper
            .createUtkastFromDifferentIntygTypeRequest(certificateId, newCertificateType, certificateType, copyRequest);

        request.setTypVersion(intygTextsService.getLatestVersion(newCertificateType));

        LOG.debug("Create renewal from template for '{}' with new type '{}' from old type '{}'", certificateId, newCertificateType,
            certificateType);
        final var templateCopy = copyUtkastService.createUtkastFromSignedTemplate(request);

        LOG.debug("Return renewal from template draft '{}' ", templateCopy.getNewDraftIntygId());
        return templateCopy.getNewDraftIntygId();
    }

    private String getNewCertificateType(String templateType) {
      return switch (templateType) {
        case LisjpEntryPoint.MODULE_ID -> Ag7804EntryPoint.MODULE_ID;
        case DbModuleEntryPoint.MODULE_ID -> DoiModuleEntryPoint.MODULE_ID;
        default -> throw new IllegalArgumentException(
            String.format(
                "Cannot create draft from template because certificate type '%s' is not supported",
                templateType)
        );
      };
    }

    private Personnummer getPersonId(Patient patient) {
        if (patient.isReserveId()) {
            return Personnummer.createPersonnummer(patient.getPreviousPersonId().getId()).orElseThrow();
        }
        return Personnummer.createPersonnummer(patient.getPersonId().getId()).orElseThrow();
    }
}
