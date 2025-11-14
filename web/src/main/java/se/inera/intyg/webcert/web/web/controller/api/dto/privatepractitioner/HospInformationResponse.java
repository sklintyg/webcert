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

package se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner;

import java.util.List;
import lombok.Builder;
import se.inera.intyg.webcert.integration.privatepractitioner.model.HospInformation;

@Builder
public record HospInformationResponse(String personalPrescriptionCode, List<CodeDTO> licensedHealthcareProfessions,
                                      List<CodeDTO> specialities) {

    public static HospInformationResponse convert(HospInformation hospInformation) {
        return HospInformationResponse.builder()
            .personalPrescriptionCode(hospInformation.personalPrescriptionCode())
            .licensedHealthcareProfessions(
                hospInformation.licensedHealthcareProfessions() == null ? List.of() :
                    hospInformation.licensedHealthcareProfessions().stream().map(l -> new CodeDTO(l.code(), l.description())).toList())
            .specialities(
                hospInformation.specialities() == null ? List.of() :
                    hospInformation.specialities().stream().map(s -> new CodeDTO(s.code(), s.description())).toList())
            .build();
    }
}
