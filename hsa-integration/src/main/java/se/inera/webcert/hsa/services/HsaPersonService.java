package se.inera.webcert.hsa.services;

import java.util.List;

import se.inera.ifv.hsawsresponder.v3.GetHsaPersonHsaUserType;

public interface HsaPersonService {

    public abstract List<String> getSpecialitiesForHsaPerson(String personHsaId);

    public abstract List<GetHsaPersonHsaUserType> getHsaPersonInfo(String personHsaId);

}
