package se.inera.webcert.service.signatur.grp;

import se.inera.webcert.hsa.model.WebCertUser;

/**
 * Created by eriklupander on 2015-08-25.
 */
public interface GrpCollectPoller extends Runnable {

    void setOrderRef(String orderRef);
    void setTransactionId(String transactionId);
    void setWebCertUser(WebCertUser webCertUser);
}
