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

package se.inera.intyg.webcert.web.csintegration.testability;

import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.webcert.web.csintegration.certificate.CertificateModelIdDTO;

public class CertificateServiceCreateRequest {

    private final Patient patient;

    private final HoSPersonal hosPerson;

    private final CertificateModelIdDTO certificateModelId;

    public CertificateServiceCreateRequest(Patient patient, HoSPersonal hosPerson, CertificateModelIdDTO certificateModelId) {
        this.patient = patient;
        this.hosPerson = hosPerson;
        this.certificateModelId = certificateModelId;
    }

    public Patient getPatient() {
        return patient;
    }

    public HoSPersonal getHosPerson() {
        return hosPerson;
    }

    public CertificateModelIdDTO getCertificateModelId() {
        return certificateModelId;
    }
}