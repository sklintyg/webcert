package se.inera.webcert.hsa.services;

import java.util.List;

import se.inera.ifv.hsawsresponder.v3.GetHsaPersonHsaUserType;
import se.inera.ifv.hsawsresponder.v3.MiuInformationType;

public interface HsaPersonService {

    List<GetHsaPersonHsaUserType> getHsaPersonInfo(String personHsaId);
    
    List<MiuInformationType> checkIfPersonHasMIUsOnUnit(String hosPersonHsaId, final String unitHsaId);

}
