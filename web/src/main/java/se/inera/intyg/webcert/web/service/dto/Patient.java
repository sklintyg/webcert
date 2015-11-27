package se.inera.intyg.webcert.web.service.dto;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;

public class Patient {

    private Personnummer personnummer;

    private String fornamn;

    private String mellannamn;

    private String efternamn;

    private String postadress;

    private String postnummer;
    private String postort;

    public Patient() {

    }

    public Personnummer getPersonnummer() {
        return personnummer;
    }

    public void setPersonnummer(Personnummer personnummer) {
        this.personnummer = personnummer;
    }

    public String getFornamn() {
        return fornamn;
    }

    public void setFornamn(String fornamn) {
        this.fornamn = fornamn;
    }

    public String getEfternamn() {
        return efternamn;
    }

    public String getMellannamn() {
        return mellannamn;
    }

    public void setMellannamn(String mellannamn) {
        this.mellannamn = mellannamn;
    }

    public void setEfternamn(String efternamn) {
        this.efternamn = efternamn;
    }

    public String getPostadress() {
        return postadress;
    }

    public void setPostadress(String postadress) {
        this.postadress = postadress;
    }

    public String getPostnummer() {
        return postnummer;
    }

    public void setPostnummer(String postnummer) {
        this.postnummer = postnummer;
    }

    public String getPostort() {
        return postort;
    }

    public void setPostort(String postort) {
        this.postort = postort;
    }
}
