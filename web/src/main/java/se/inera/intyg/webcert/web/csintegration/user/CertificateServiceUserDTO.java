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

package se.inera.intyg.webcert.web.csintegration.user;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.webcert.web.csintegration.user.CertificateServiceUserDTO.CertificateServiceUserDTOBuilder;

@JsonDeserialize(builder = CertificateServiceUserDTOBuilder.class)
@Value
@Builder
public class CertificateServiceUserDTO {

    String id;
    String firstName;
    String lastName;
    String middleName;
    String fullName;
    CertificateServiceUserRole role;
    List<PaTitleDTO> paTitles;
    List<String> specialities;
    Boolean blocked;
    Boolean agreement;
    AccessScopeType accessScope;
    Boolean allowCopy;
    List<String> healthCareProfessionalLicence;
    String responsibleHospName;
    Boolean srsActive;

    @JsonPOJOBuilder(withPrefix = "")
    public static class CertificateServiceUserDTOBuilder {

    }
}