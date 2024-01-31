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

import se.inera.intyg.webcert.web.csintegration.patient.CertificateServicePatientDTO;
import se.inera.intyg.webcert.web.csintegration.unit.CertificateServiceUnitDTO;
import se.inera.intyg.webcert.web.csintegration.user.CertificateServiceUserDTO;

public class GetCertificateRequestDTO {

    private CertificateServiceUserDTO user;

    private CertificateServicePatientDTO patient;

    private CertificateServiceUnitDTO unit;
    private CertificateServiceUnitDTO careUnit;
    private CertificateServiceUnitDTO careProvider;

    public CertificateServiceUserDTO getUser() {
        return user;
    }

    public void setUser(CertificateServiceUserDTO user) {
        this.user = user;
    }

    public CertificateServicePatientDTO getPatient() {
        return patient;
    }

    public void setPatient(CertificateServicePatientDTO patient) {
        this.patient = patient;
    }

    public CertificateServiceUnitDTO getUnit() {
        return unit;
    }

    public void setUnit(CertificateServiceUnitDTO unit) {
        this.unit = unit;
    }

    public CertificateServiceUnitDTO getCareUnit() {
        return careUnit;
    }

    public void setCareUnit(CertificateServiceUnitDTO careUnit) {
        this.careUnit = careUnit;
    }

    public CertificateServiceUnitDTO getCareProvider() {
        return careProvider;
    }

    public void setCareProvider(CertificateServiceUnitDTO careProvider) {
        this.careProvider = careProvider;
    }
}
