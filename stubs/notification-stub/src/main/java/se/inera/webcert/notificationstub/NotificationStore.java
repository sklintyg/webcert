package se.inera.webcert.notificationstub;

import java.util.Collection;

import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;

public interface NotificationStore {

    public abstract void put(String utlatandeId, CertificateStatusUpdateForCareType request);

    public abstract Collection<CertificateStatusUpdateForCareType> getNotifications();

    public abstract void clear();

}
