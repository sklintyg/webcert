package se.inera.webcert.notificationstub;

import java.util.Collection;

import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class NotificationStore  {

    private Multimap<String, CertificateStatusUpdateForCareType> store = ArrayListMultimap.create();

    public void put(String utlatandeId, CertificateStatusUpdateForCareType request) {
        store.put(utlatandeId, request);
    }

    public Collection<CertificateStatusUpdateForCareType> getNotifications() {
        return store.values();
    }

    public void clear() {
        store.clear();
    }
}
