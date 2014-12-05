package se.inera.webcert.notifications.stub;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.HandelseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.utils.ResultTypeUtil;

public class CertificateStatusUpdateForCareResponderStub implements CertificateStatusUpdateForCareResponderInterface {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateStatusUpdateForCareResponderStub.class);

    private ConcurrentHashMap<String, HandelseType> store = new ConcurrentHashMap<String, HandelseType>();
    private AtomicInteger counter = new AtomicInteger(0);

    @Override
    public CertificateStatusUpdateForCareResponseType certificateStatusUpdateForCare(String logicalAddress,
            CertificateStatusUpdateForCareType request) {

        LOG.info("Request to address '{}' recieved", logicalAddress);
        counter.incrementAndGet();
        store.put(request.getUtlatande().getUtlatandeId().getExtension(), request.getUtlatande().getHandelse());

        CertificateStatusUpdateForCareResponseType response = new CertificateStatusUpdateForCareResponseType();
        response.setResult(ResultTypeUtil.okResult());
        LOG.debug("Request set to 'OK'");
        return response;
    }

    public int getNumberOfReceivedMessages() {
        return counter.get();
    }

    public int countReceivedMessages() {
        return store.keySet().size();
    }

    public Map<String, HandelseType> getExchange() {
        return (Map<String, HandelseType>) store;
    }
    
    public void reset() {
        this.store.clear();
        this.counter.set(0);
    }

}
