/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.csintegration.integration.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SaveCertificateRequestDTO.SaveCertificateRequestDTOBuilder;
import se.inera.intyg.webcert.web.csintegration.patient.CertificateServicePatientDTO;
import se.inera.intyg.webcert.web.csintegration.unit.CertificateServiceUnitDTO;
import se.inera.intyg.webcert.web.csintegration.user.CertificateServiceUserDTO;

@JsonDeserialize(builder = SaveCertificateRequestDTOBuilder.class)
@Value
@Builder
public class SaveCertificateRequestDTO {

    CertificateServiceUserDTO user;
    CertificateServicePatientDTO patient;
    CertificateServiceUnitDTO careUnit;
    CertificateServiceUnitDTO unit;
    CertificateServiceUnitDTO careProvider;
    Certificate certificate;

    @JsonPOJOBuilder(withPrefix = "")
    public static class SaveCertificateRequestDTOBuilder {

    }
}
