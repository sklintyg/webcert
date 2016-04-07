package se.inera.intyg.webcert.web.service.utkast.dto;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.webcert.web.service.dto.HoSPerson;
import se.inera.intyg.webcert.web.service.dto.Vardenhet;

public class CreateRenewalCopyRequest extends CreateCopyRequest {
    private String meddelandeId;

    public CreateRenewalCopyRequest(String orginalIntygsId, String intygsTyp, String meddelandeId, Personnummer patientPersonnummer,
            HoSPerson hosPerson, Vardenhet vardenhet) {
        super(orginalIntygsId, intygsTyp, patientPersonnummer, hosPerson, vardenhet);
        this.setMeddelandeId(meddelandeId);
    }
    
    public CreateRenewalCopyRequest(){
        
    }

    public String getMeddelandeId() {
        return meddelandeId;
    }

    public void setMeddelandeId(String meddelandeId) {
        this.meddelandeId = meddelandeId;
    }
}
