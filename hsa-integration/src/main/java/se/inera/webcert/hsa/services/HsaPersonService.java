package se.inera.webcert.hsa.services;

import java.util.List;

import se.inera.ifv.hsawsresponder.v3.GetHsaPersonHsaUserType;

public interface HsaPersonService {

    List<GetHsaPersonHsaUserType> getHsaPersonInfo(String personHsaId);

}
