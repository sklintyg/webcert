/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.facade.internalapi.service;

import java.util.List;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ApplicationOrigin;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.PdfResponse;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.web.converter.util.IntygConverterUtil;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.GetUtkastResponse;

@Service
public class GetCertificatePrintService {

    private final GetUtkastFacadeService getUtkastFacadeService;
    private final IntygModuleRegistry moduleRegistry;

    private static final String DONT_DISPLAY_DIAGNOSIS_ID = "!diagnoser";
    private static final List<String> OPTIONAL_FIELDS = List.of(DONT_DISPLAY_DIAGNOSIS_ID, "!onskarFormedlaDiagnoser");

    public GetCertificatePrintService(GetUtkastFacadeService getUtkastFacadeService, IntygModuleRegistry moduleRegistry) {
        this.getUtkastFacadeService = getUtkastFacadeService;
        this.moduleRegistry = moduleRegistry;
    }

    public PdfResponse get(String customizationId, Certificate certificate, boolean pdlLog,
        boolean validateAccess)
        throws ModuleNotFoundException, ModuleException {
        validateCertificate(certificate);
        final var moduleApi = moduleRegistry.getModuleApi(
            certificate.getMetadata().getTypeVersion(),
            certificate.getMetadata().getType()
        );
        final var utkastResponse = getUtkastFacadeService.get(certificate.getMetadata().getId(), pdlLog, validateAccess);
        final var jsonModel = getJsonFromUtkastResponse(utkastResponse);
        final var statuses = getStatuses(utkastResponse);
        return getPdfResponse(utkastResponse, customizationId, moduleApi, jsonModel, statuses);
    }

    private void validateCertificate(Certificate certificate) {
        if (certificate.getMetadata().getStatus().equals(CertificateStatus.REVOKED)) {
            throw new IllegalStateException("Revoked certificate can not be printed");
        }
    }

    private PdfResponse getPdfResponse(GetUtkastResponse utkastResponse, String customizationId, ModuleApi moduleApi, String jsonModel,
        List<Status> statuses) throws ModuleException {
        if (customizationId != null && customizationId.equals(DONT_DISPLAY_DIAGNOSIS_ID)) {
            return moduleApi.pdfEmployer(jsonModel, statuses, ApplicationOrigin.MINA_INTYG, OPTIONAL_FIELDS,
                getUtkastStatus(utkastResponse));
        }
        return moduleApi.pdf(jsonModel, statuses, ApplicationOrigin.MINA_INTYG, getUtkastStatus(utkastResponse));
    }

    private UtkastStatus getUtkastStatus(GetUtkastResponse utkastResponse) {
        return utkastResponse.getDraft() != null ? utkastResponse.getDraft().getStatus() : UtkastStatus.SIGNED;
    }

    private List<Status> getStatuses(GetUtkastResponse utkastResponse) {
        if (utkastResponse.getDraft() != null) {
            return IntygConverterUtil.buildStatusesFromUtkast(utkastResponse.getDraft());
        }
        return utkastResponse.getIntygContentHolder().getStatuses();
    }

    private String getJsonFromUtkastResponse(GetUtkastResponse utkastResponse) {
        return utkastResponse.getDraft() != null ? utkastResponse.getDraft().getModel()
            : utkastResponse.getIntygContentHolder().getContents();
    }
}
