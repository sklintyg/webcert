package se.inera.webcert.notifications.service;

import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;

public interface CertificateStatusUpdateService {

    void sendStatusUpdate(String intygsId, CertificateStatusUpdateForCareType request) throws Exception;

}
