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
package se.inera.intyg.webcert.web.service.facade.util;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.utkast.UtkastCandidateServiceImpl;
import se.inera.intyg.webcert.web.service.utkast.dto.UtkastCandidateMetaData;

@Component
public class CandidateDataHelperImpl implements CandidateDataHelper {

    private final UtkastCandidateServiceImpl utkastCandidateService;
    private final IntygModuleRegistry moduleRegistry;
    private final PatientDetailsResolver patientDetailsResolver;

    @Autowired
    public CandidateDataHelperImpl(UtkastCandidateServiceImpl utkastCandidateService,
        IntygModuleRegistry moduleRegistry, PatientDetailsResolver patientDetailsResolver) {
        this.utkastCandidateService = utkastCandidateService;
        this.moduleRegistry = moduleRegistry;
        this.patientDetailsResolver = patientDetailsResolver;
    }

    @Override
    public Optional<UtkastCandidateMetaData> getCandidateMetadata(String certificateType, String certificateTypeVersion,
        Personnummer patientPersonId) {
        try {
            final ModuleApi moduleApi = moduleRegistry
                .getModuleApi(certificateType, certificateTypeVersion);

            Optional<UtkastCandidateMetaData> metaData = utkastCandidateService
                .getCandidateMetaData(moduleApi, certificateType, certificateTypeVersion,
                    getPatientDataFromPU(certificateType, certificateTypeVersion, patientPersonId), false);
            return metaData;
        } catch (ModuleNotFoundException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e.getMessage());
        }
    }


    private Patient getPatientDataFromPU(String certificateType, String certificateTypeVersion, Personnummer patientPersonId) {
        Patient resolvedPatientData = patientDetailsResolver.resolvePatient(patientPersonId, certificateType, certificateTypeVersion);
        if (resolvedPatientData == null) {
            throw new WebCertServiceException(
                WebCertServiceErrorCodeEnum.PU_PROBLEM, "Could not resolve Patient in PU-service when opening draft.");
        }
        return resolvedPatientData;
    }
}
