package se.inera.intyg.webcert.web.service.diagnos.model;

public class Diagnos implements Comparable<Diagnos> {

    private String kod;

    private String beskrivning;

    public String getKod() {
        return kod;
    }

    public void setKod(String kod) {
        this.kod = kod;
    }

    public String getBeskrivning() {
        return beskrivning;
    }

    public void setBeskrivning(String beskrivning) {
        this.beskrivning = beskrivning;
    }

    @Override
    public int compareTo(Diagnos d) {
        return getKod().compareTo(d.getKod());
    }
}
