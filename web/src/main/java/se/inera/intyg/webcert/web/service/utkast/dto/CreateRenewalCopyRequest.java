package se.inera.intyg.webcert.web.service.utkast.dto;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.webcert.web.service.dto.HoSPerson;
import se.inera.intyg.webcert.web.service.dto.Vardenhet;

public class CreateRenewalCopyRequest extends CreateCopyRequest {

    public CreateRenewalCopyRequest(String orginalIntygsId, String intygsTyp, Personnummer patientPersonnummer,
            HoSPerson hosPerson, Vardenhet vardenhet) {
        super(orginalIntygsId, intygsTyp, patientPersonnummer, hosPerson, vardenhet);
    }
    
    public CreateRenewalCopyRequest(){
        
    }

}
