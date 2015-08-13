package se.inera.intyg.webcert.integration.pp.services;

import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;

/**
 * Created by Magnus Ekstrand on 18/06/15.
 */
public interface PPService {

    HoSPersonType getPrivatePractitioner(String logicalAddress, String hsaIdentity, String personalIdentity);

    boolean validatePrivatePractitioner(String logicalAddress, String hsaIdentity, String personalIdentity);

}
