package se.inera.intyg.webcert.web.service.signatur.grp;

import se.inera.intyg.webcert.web.service.signatur.dto.SignaturTicket;

/**
 * Created by eriklupander on 2015-08-21.
 */
public interface GrpSignaturService {

    SignaturTicket startGrpAuthentication(String intygId, long version);
}
