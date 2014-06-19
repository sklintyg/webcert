package se.inera.webcert.pu.model;

public class Person {
    private final String fornamn;
    private final String efternamn;
    private final String postadress;
    private final String postnummer;
    private final String postort;

    public Person(String fornamn, String efternamn, String postadress, String postnummer, String postort) {
        this.fornamn = fornamn;
        this.efternamn = efternamn;
        this.postadress = postadress;
        this.postnummer = postnummer;
        this.postort = postort;
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
