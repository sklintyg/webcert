package se.inera.intyg.webcert.integration.hsa.services;

import java.util.List;

import se.riv.infrastructure.directory.v1.CommissionType;
import se.riv.infrastructure.directory.v1.PersonInformationType;

public interface HsaPersonService {

    List<PersonInformationType> getHsaPersonInfo(String personHsaId);

    List<CommissionType> checkIfPersonHasMIUsOnUnit(String hosPersonHsaId, final String unitHsaId);
}
