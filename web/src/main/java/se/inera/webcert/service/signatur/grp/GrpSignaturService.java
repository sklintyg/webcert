package se.inera.webcert.service.signatur.grp;

import se.inera.webcert.service.signatur.dto.SignaturTicket;

/**
 * Created by eriklupander on 2015-08-21.
 */
public interface GrpSignaturService {

    SignaturTicket sendAuthenticateRequest(String intygId, long version);
}
