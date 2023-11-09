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
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ApplicationOrigin;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.PdfResponse;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CertificatePdfResponseDTO;

@Service
public class GetCertificatePdfService {

    private final GetRequiredFieldsForCertificatePdfService getRequiredFieldsForCertificatePdfService;
    private final IntygModuleRegistry moduleRegistry;

    private static final String DONT_DISPLAY_DIAGNOSIS_ID = "!diagnoser";
    private static final List<String> OPTIONAL_FIELDS = List.of(DONT_DISPLAY_DIAGNOSIS_ID, "!onskarFormedlaDiagnoser");

    public GetCertificatePdfService(GetRequiredFieldsForCertificatePdfService getRequiredFieldsForCertificatePdfService,
        IntygModuleRegistry moduleRegistry) {
        this.getRequiredFieldsForCertificatePdfService = getRequiredFieldsForCertificatePdfService;
        this.moduleRegistry = moduleRegistry;
    }

    public CertificatePdfResponseDTO get(String customizationId, String certificateId) {
        final var requiredFieldsForCertificatePdf = getRequiredFieldsForCertificatePdfService.get(certificateId);

        final var moduleApi = getModuleApi(
            requiredFieldsForCertificatePdf.getCertificateTypeVersion(),
            requiredFieldsForCertificatePdf.getCertificateType()
        );

        final var pdfResponse = getPdfResponse(
            customizationId,
            moduleApi,
            requiredFieldsForCertificatePdf.getInternalJsonModel(),
            requiredFieldsForCertificatePdf.getStatuses(),
            requiredFieldsForCertificatePdf.getStatus()
        );

        return CertificatePdfResponseDTO.create(
            pdfResponse.getFilename(),
            pdfResponse.getPdfData()
        );
    }

    private ModuleApi getModuleApi(String certificateTypeVersion, String certificateType) {
        try {
            return moduleRegistry.getModuleApi(
                certificateTypeVersion,
                certificateType
            );
        } catch (ModuleNotFoundException exception) {
            throw new IllegalStateException(
                String.format("Module api not found with typeVersion '%s' and type '%s'",
                    certificateTypeVersion,
                    certificateType
                )
            );
        }
    }

    private PdfResponse getPdfResponse(String customizationId, ModuleApi moduleApi, String jsonModel, List<Status> statuses,
        UtkastStatus status) {
        try {
            if (DONT_DISPLAY_DIAGNOSIS_ID.equals(customizationId)) {
                return moduleApi.pdfEmployer(jsonModel, statuses, ApplicationOrigin.MINA_INTYG, OPTIONAL_FIELDS, status);
            }
            return moduleApi.pdf(jsonModel, statuses, ApplicationOrigin.MINA_INTYG, status);
        } catch (ModuleException exception) {
            throw new IllegalStateException("Unable to get pdf from module api implementation");
        }
    }
}
