package se.inera.webcert.notificationstub;

import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;

import java.util.Collection;


public interface NotificationStore {

    void put(String utlatandeId, CertificateStatusUpdateForCareType request);

    Collection<CertificateStatusUpdateForCareType> getNotifications();

    void clear();

}
