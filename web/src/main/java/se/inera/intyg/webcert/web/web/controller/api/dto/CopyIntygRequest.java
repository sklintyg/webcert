package se.inera.intyg.webcert.web.web.controller.api.dto;

import org.apache.commons.lang3.StringUtils;
import se.inera.certificate.modules.support.api.dto.Personnummer;

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
