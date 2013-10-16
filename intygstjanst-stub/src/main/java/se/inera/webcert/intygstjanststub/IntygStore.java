package se.inera.webcert.intygstjanststub;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.inera.ifv.clinicalprocess.healtcond.certificate.getcertificateforcareresponder.v1.GetCertificateForCareResponseType;


/**
 * @author marced
 */
@Component
public class IntygStore {
    private static final Logger LOG = LoggerFactory.getLogger(IntygStore.class);

    private ConcurrentHashMap<String, GetCertificateForCareResponseType> intyg = new ConcurrentHashMap<String, GetCertificateForCareResponseType>();

    public void addIntyg(GetCertificateForCareResponseType request) {
        LOG.debug("IntygStore: added intyg " + request.getMeta().getCertificateId() + " to store.");
        intyg.put(request.getMeta().getCertificateId(), request);
    }

    public ConcurrentHashMap<String, GetCertificateForCareResponseType> getAllIntyg() {
        return intyg;
    }

}
