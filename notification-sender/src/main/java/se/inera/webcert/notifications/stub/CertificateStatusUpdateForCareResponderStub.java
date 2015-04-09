package se.inera.webcert.notifications.stub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;
import se.inera.intyg.common.schemas.clinicalprocess.healthcond.certificate.utils.ResultTypeUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CertificateStatusUpdateForCareResponderStub implements CertificateStatusUpdateForCareResponderInterface {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateStatusUpdateForCareResponderStub.class);

    private ConcurrentHashMap<String, CertificateStatusUpdateForCareType> store = new ConcurrentHashMap<>();
    private AtomicInteger counter = new AtomicInteger(0);

    @Override
    public CertificateStatusUpdateForCareResponseType certificateStatusUpdateForCare(String logicalAddress,
            CertificateStatusUpdateForCareType request) {
        String utlatandeId = request.getUtlatande().getUtlatandeId().getExtension();

        counter.incrementAndGet();
        store.put(utlatandeId, request);

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

    public Map<String, CertificateStatusUpdateForCareType> getExchange() {
        return store;
    }

    public void reset() {
        this.store.clear();
        this.counter.set(0);
    }

}
