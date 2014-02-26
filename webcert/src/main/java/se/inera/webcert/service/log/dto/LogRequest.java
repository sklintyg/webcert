package se.inera.webcert.service.log.dto;

public class LogRequest {
    
    private String intygId;
    
    private String patientId;
    
    private String patientName;
    
    public LogRequest() {
        super();
    }

    public String getIntygId() {
        return intygId;
    }

    public void setIntygId(String intygId) {
        this.intygId = intygId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
    
}
