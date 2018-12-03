package se.inera.intyg.webcert.web.service.fmb.sjukfall;

import se.inera.intyg.schemas.contract.Personnummer;

public interface FmbSjukfallService {
    void beraknaSjukfallForPatient(Personnummer personnummer);
}
