package se.inera.webcert.hsa.services;

import se.inera.ifv.hsawsresponder.v3.GetHsaPersonHsaUserType;
import se.inera.ifv.hsawsresponder.v3.MiuInformationType;

import java.util.List;

public interface HsaPersonService {

    List<GetHsaPersonHsaUserType> getHsaPersonInfo(String personHsaId);

    List<MiuInformationType> checkIfPersonHasMIUsOnUnit(String hosPersonHsaId, final String unitHsaId);
}
