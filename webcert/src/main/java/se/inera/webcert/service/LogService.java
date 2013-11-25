package se.inera.webcert.service;

import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;

/**
 * @author andreaskaltenbach
 */
public interface LogService {

    void logReadOfIntyg(GetCertificateForCareResponseType intyg );
}
