package se.inera.intyg.webcert.web.service.log.dto;


public class LogUser {

    private String userId;
    private String userName;
    private String enhetsId;
    private String enhetsNamn;
    private String vardgivareId;
    private String vardgivareNamn;

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getEnhetsId() {
        return enhetsId;
    }
    public void setEnhetsId(String enhetsId) {
        this.enhetsId = enhetsId;
    }
    public String getEnhetsNamn() {
        return enhetsNamn;
    }
    public void setEnhetsNamn(String enhetsNamn) {
        this.enhetsNamn = enhetsNamn;
    }
    public String getVardgivareId() {
        return vardgivareId;
    }
    public void setVardgivareId(String vardgivareId) {
        this.vardgivareId = vardgivareId;
    }
    public String getVardgivareNamn() {
        return vardgivareNamn;
    }
    public void setVardgivareNamn(String vardgivareNamn) {
        this.vardgivareNamn = vardgivareNamn;
    }

}
