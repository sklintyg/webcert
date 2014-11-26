package se.inera.webcert.notifications.service;

import org.apache.camel.Header;

import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;

public interface CertificateStatusUpdateService {

    public abstract void sendStatusUpdate(@Header("intygsId") String intygsId, CertificateStatusUpdateForCareType request) throws Exception;

}
