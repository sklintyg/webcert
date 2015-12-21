/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.web.controller.api.dto;

import org.apache.commons.lang3.StringUtils;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;

public class CopyIntygRequest {

    private Personnummer patientPersonnummer;

    private Personnummer nyttPatientPersonnummer;

    public CopyIntygRequest() {

    }

    public Personnummer getPatientPersonnummer() {
        return patientPersonnummer;
    }

    public void setPatientPersonnummer(Personnummer patientPersonnummer) {
        this.patientPersonnummer = patientPersonnummer;
    }

    public Personnummer getNyttPatientPersonnummer() {
        return nyttPatientPersonnummer;
    }

    public void setNyttPatientPersonnummer(Personnummer nyttPatientPersonnummer) {
        this.nyttPatientPersonnummer = nyttPatientPersonnummer;
    }

    public boolean containsNewPersonnummer() {
        return nyttPatientPersonnummer != null && StringUtils.isNotBlank(nyttPatientPersonnummer.getPersonnummer());
    }

    public boolean isValid() {
        return patientPersonnummer != null && StringUtils.isNotBlank(patientPersonnummer.getPersonnummer());
    }
}
