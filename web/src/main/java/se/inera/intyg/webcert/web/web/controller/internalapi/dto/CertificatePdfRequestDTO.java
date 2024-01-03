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

package se.inera.intyg.webcert.web.web.controller.internalapi.dto;

import java.util.Objects;

public class CertificatePdfRequestDTO {

    private String customizationId;

    public CertificatePdfRequestDTO() {
    }

    public CertificatePdfRequestDTO(String customizationId) {
        this.customizationId = customizationId;
    }

    public String getCustomizationId() {
        return customizationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CertificatePdfRequestDTO that = (CertificatePdfRequestDTO) o;
        return Objects.equals(customizationId, that.customizationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customizationId);
    }

    @Override
    public String toString() {
        return "PrintCertificateRequestDTO{"
            + "customizationId='" + customizationId + '\''
            + '}';
    }
}
