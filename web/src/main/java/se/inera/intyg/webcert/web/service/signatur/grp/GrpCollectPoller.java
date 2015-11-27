package se.inera.intyg.webcert.web.service.signatur.grp;

import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

/**
 * Created by eriklupander on 2015-08-25.
 */
public interface GrpCollectPoller extends Runnable {

    void setOrderRef(String orderRef);
    void setTransactionId(String transactionId);
    void setWebCertUser(WebCertUser webCertUser);
}
