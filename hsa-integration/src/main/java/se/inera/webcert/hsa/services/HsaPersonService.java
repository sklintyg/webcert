package se.inera.webcert.hsa.services;

import java.util.List;

import se.inera.ifv.hsawsresponder.v3.GetHsaPersonHsaUserType;
import se.inera.webcert.hsa.model.Specialisering;

public interface HsaPersonService {

    public abstract List<Specialisering> getSpecialitiesForHsaPerson(String personHsaId);

    public abstract List<GetHsaPersonHsaUserType> getHsaPersonInfo(String personHsaId);

}
