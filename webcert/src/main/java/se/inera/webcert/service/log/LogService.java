package se.inera.webcert.service.log;


/**
 * @author andreaskaltenbach
 */
public interface LogService {

    void logReadOfIntyg(String intygId, String patientId);
    
    void logPrintOfIntyg(String intygId, String patientId);
}
