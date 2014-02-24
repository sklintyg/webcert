package se.inera.webcert.service;


/**
 * @author andreaskaltenbach
 */
public interface LogService {

    void logReadOfIntyg(String intygId, String patientId);
    
    void logPrintOfIntyg(String intygId, String patientId);
}
