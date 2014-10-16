package se.inera.webcert.service.feature;

public enum Features {

    HANTERA_FRAGOR("hanteraFragor"),
    HANTERA_INTYGSUTKAST("hanteraIntygsutkast"),
    KOPIERA_INTYG("kopieraIntyg"),
    MAKULERA_INTYG("makuleraIntyg"),
    SKICKA_INTYG("skickaIntyg");

    private String name;
    
    private Features(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
}
