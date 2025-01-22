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
package se.inera.intyg.webcert.web.service.access;

import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.schemas.contract.Personnummer;

public final class AccessEvaluationParameters {

    private final String certificateType;
    private final String certificateTypeVersion;
    private final Vardenhet unit;
    private final Personnummer patient;
    private final boolean isTestCertificate;

    private AccessEvaluationParameters(String certificateType, String certificateTypeVersion, Vardenhet unit, Personnummer patient,
        boolean isTestCertificate) {
        this.certificateType = certificateType;
        this.certificateTypeVersion = certificateTypeVersion;
        this.unit = unit;
        this.patient = patient;
        this.isTestCertificate = isTestCertificate;
    }

    public static AccessEvaluationParameters create(String intygsTyp, Personnummer patientPersonnummer) {
        return new AccessEvaluationParameters(intygsTyp, null, null, patientPersonnummer, false);
    }

    public static AccessEvaluationParameters create(String certificateType, String certificateTypeVersion, Vardenhet unit,
        Personnummer patient, boolean isTestCertificate) {
        return new AccessEvaluationParameters(certificateType, certificateTypeVersion, unit, patient, isTestCertificate);
    }

    public String getCertificateType() {
        return certificateType;
    }

    public Vardenhet getUnit() {
        return unit;
    }

    public Personnummer getPatient() {
        return patient;
    }

    public boolean isTestCertificate() {
        return isTestCertificate;
    }

    public String getCertificateTypeVersion() {
        return certificateTypeVersion;
    }
}
