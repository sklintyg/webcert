package se.inera.webcert.modules.api.dto;


public class CreateNewIntygModuleRequest {

    private String certificateId;

    private HoSPersonal skapadAv;

    private Patient patientInfo;
    
    public CreateNewIntygModuleRequest() {
        // TODO Auto-generated constructor stub
    }

    public String getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(String certificateId) {
        this.certificateId = certificateId;
    }

    public HoSPersonal getSkapadAv() {
        return skapadAv;
    }

    public void setSkapadAv(HoSPersonal skapadAv) {
        this.skapadAv = skapadAv;
    }

    public Patient getPatientInfo() {
        return patientInfo;
    }

    public void setPatientInfo(Patient patientInfo) {
        this.patientInfo = patientInfo;
    }
        
}
