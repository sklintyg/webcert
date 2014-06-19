package se.inera.webcert.web.controller.api.dto;

public class Personuppgifter {

    private final String personnummer;

    private final String fornamn;

    private final String efternamn;

    private final String postadress;

    private final String postnummer;

    private final String postort;

    public Personuppgifter(String personnummer, String fornamn, String efternamn, String postadress, String postnummer, String postort) {
        this.personnummer = personnummer;
        this.fornamn = fornamn;
        this.efternamn = efternamn;
        this.postadress = postadress;
        this.postnummer = postnummer;
        this.postort = postort;
    }

    public String getPersonnummer() {
        return personnummer;
    }

    public String getFornamn() {
        return fornamn;
    }

    public String getEfternamn() {
        return efternamn;
    }

    public String getPostadress() {
        return postadress;
    }

    public String getPostnummer() {
        return postnummer;
    }

    public String getPostort() {
        return postort;
    }
}
