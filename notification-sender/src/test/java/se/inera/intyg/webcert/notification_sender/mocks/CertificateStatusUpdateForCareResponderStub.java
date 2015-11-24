package se.inera.intyg.webcert.notification_sender.mocks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.intyg.common.schemas.clinicalprocess.healthcond.certificate.utils.ResultTypeUtil;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;


public class CertificateStatusUpdateForCareResponderStub implements CertificateStatusUpdateForCareResponderInterface {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateStatusUpdateForCareResponderStub.class);

    public static final String FALLERAT_MEDDELANDE = "fallerat-meddelande-";

    private ConcurrentHashMap<String, AtomicInteger> attemptsPerMessage = new ConcurrentHashMap<>();
    private List<CertificateStatusUpdateForCareType> store = new CopyOnWriteArrayList<>();

    private AtomicInteger counter = new AtomicInteger(0);

    @Override
    public CertificateStatusUpdateForCareResponseType certificateStatusUpdateForCare(String logicalAddress,
            CertificateStatusUpdateForCareType request) {

        counter.incrementAndGet();

        String utlatandeId = getUtlatandeId(request);

        LOG.debug("utlatandeId: " + utlatandeId);
        LOG.debug("numberOfReceivedMessages: " + getNumberOfReceivedMessages());

        if (utlatandeId.startsWith(FALLERAT_MEDDELANDE)) {
            int attempts = increaseAttempts(utlatandeId);
            int numberOfRequestedFailedAttempts = Integer.parseInt(utlatandeId.substring(utlatandeId.length() - 1));
            LOG.debug("attempts: " + attempts);
            LOG.debug("numberOfRequestedFailedAttempts: " + numberOfRequestedFailedAttempts);
            if (attempts < numberOfRequestedFailedAttempts + 1) {
                throw new RuntimeException("Something went wrong");
            }
        }

        store.add(request);

        CertificateStatusUpdateForCareResponseType response = new CertificateStatusUpdateForCareResponseType();
        response.setResult(ResultTypeUtil.okResult());
        LOG.debug("Request set to 'OK'");
        return response;
    }

    private int increaseAttempts(String key) {
        AtomicInteger value = attemptsPerMessage.get(key);
        if (value == null) {
            value = attemptsPerMessage.putIfAbsent(key, new AtomicInteger(1));
        }
        if (value != null) {
            value.incrementAndGet();
        }
        return attemptsPerMessage.get(key).intValue();
    }

    private String getUtlatandeId(CertificateStatusUpdateForCareType request) {
        return request.getUtlatande().getUtlatandeId().getExtension();
    }

    public int getNumberOfReceivedMessages() {
        return counter.get();
    }

    public int getNumberOfSentMessages() {
        return store.size();
    }

    public List<String> getIntygsIdsInOrder() {
        List<String> returnList = new ArrayList<>();
        for (CertificateStatusUpdateForCareType request : store) {
            returnList.add(getUtlatandeId(request));
        }
        return returnList;
    }

    public void reset() {
        counter = new AtomicInteger(0);
        attemptsPerMessage = new ConcurrentHashMap<>();
        store = new CopyOnWriteArrayList<>();
    }

}
