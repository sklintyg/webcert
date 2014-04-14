package se.inera.webcert.service.dto;

public class Patient {

    private String forNamn;

    private String efterNamn;

    private String personNummer;

    private String postAdress;

    private String postNummer;

    private String postOrt;

    public Patient() {

    }

    public String getForNamn() {
        return forNamn;
    }

    public void setForNamn(String forNamn) {
        this.forNamn = forNamn;
    }

    public String getEfterNamn() {
        return efterNamn;
    }

    public void setEfterNamn(String efterNamn) {
        this.efterNamn = efterNamn;
    }

    public String getPersonNummer() {
        return personNummer;
    }

    public void setPersonNummer(String personNummer) {
        this.personNummer = personNummer;
    }

    public String getPostAdress() {
        return postAdress;
    }

    public void setPostAdress(String postAdress) {
        this.postAdress = postAdress;
    }

    public String getPostNummer() {
        return postNummer;
    }

    public void setPostNummer(String postNummer) {
        this.postNummer = postNummer;
    }

    public String getPostOrt() {
        return postOrt;
    }

    public void setPostOrt(String postOrt) {
        this.postOrt = postOrt;
    }

}
