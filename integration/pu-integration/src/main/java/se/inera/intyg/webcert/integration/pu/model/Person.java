package se.inera.intyg.webcert.integration.pu.model;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;

public class Person {
    private final Personnummer personnummer;
    private final boolean sekretessmarkering;
    private final String fornamn;
    private final String mellannamn;
    private final String efternamn;
    private final String postadress;
    private final String postnummer;
    private final String postort;

    // CHECKSTYLE:OFF ParameterNumber
    public Person(Personnummer personnummer, boolean sekretessmarkering, String fornamn, String mellannamn, String efternamn, String postadress, String postnummer, String postort) {
        this.personnummer = personnummer;
        this.sekretessmarkering = sekretessmarkering;
        this.fornamn = fornamn;
        this.mellannamn = mellannamn;
        this.efternamn = efternamn;
        this.postadress = postadress;
        this.postnummer = postnummer;
        this.postort = postort;
    }
    // CHECKSTYLE:ON ParameterNumber

    public Personnummer getPersonnummer() {
        return personnummer;
    }

    public boolean isSekretessmarkering() {
        return sekretessmarkering;
    }

    public String getFornamn() {
        return fornamn;
    }

    public String getMellannamn() {
        return mellannamn;
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
