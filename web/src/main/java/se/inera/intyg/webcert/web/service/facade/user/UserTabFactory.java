package se.inera.intyg.webcert.web.service.facade.user;

public class UserTabFactory {
    public static UserTab listDrafts(long number) {
        return new UserTab("Ej signerade utkast", "/list/draft", number);
    }

    public static UserTab searchCreate() {
        return new UserTab("Sök/Skriv", "/create");
    }

    public static UserTab signedCertificates() {
        return new UserTab("Signerade intyg", "/list/certificate");
    }
}
