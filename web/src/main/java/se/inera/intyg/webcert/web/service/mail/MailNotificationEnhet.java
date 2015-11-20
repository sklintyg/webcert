package se.inera.intyg.webcert.web.service.mail;

public class MailNotificationEnhet {

    private String hsaId;
    private String name;
    private String email;

    public MailNotificationEnhet(String hsaId, String name, String email) {
        this.hsaId = hsaId;
        this.name = name;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public String getHsaId() {
        return hsaId;
    }

    public String getName() {
        return name;
    }

}
