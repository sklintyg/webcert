package se.inera.webcert.notificationstub;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.utils.ResultTypeUtil;

public class NotificationStore  {

    private ConcurrentHashMap<String, CertificateStatusUpdateForCareType> store = new ConcurrentHashMap<String, CertificateStatusUpdateForCareType>();

    public void put(String utlatandeId, CertificateStatusUpdateForCareType request) {
        store.put(utlatandeId, request);
    }

    public Collection<CertificateStatusUpdateForCareType> getNotifications() {
        return store.values();
    }
}
