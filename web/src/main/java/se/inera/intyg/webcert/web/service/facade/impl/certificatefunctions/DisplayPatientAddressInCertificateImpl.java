/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@Component
public class DisplayPatientAddressInCertificateImpl implements DisplayPatientAddressInCertificate {

    private static final String DISPLAY_PATIENT_NAME = "Patientuppgifter";
    private static final String DISPLAY_PATIENT_DESCRIPTION = "Presenterar patientens adressuppgifter";

    private final List<String> certificatesTypeToDisplay = List.of(
        DbModuleEntryPoint.MODULE_ID,
        DoiModuleEntryPoint.MODULE_ID
    );

    @Override
    public Optional<ResourceLinkDTO> get(Certificate certificate) {
        if (certificatesTypeToDisplay.contains(certificate.getMetadata().getType())) {
            return Optional.of(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.DISPLAY_PATIENT_ADDRESS_IN_CERTIFICATE,
                    DISPLAY_PATIENT_NAME,
                    DISPLAY_PATIENT_DESCRIPTION,
                    !certificate.getMetadata().getPatient().isAddressFromPU())
            );
        }
        return Optional.empty();
    }
}
