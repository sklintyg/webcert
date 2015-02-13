package se.inera.webcert.service.feature;

public enum WebcertFeature {

    HANTERA_FRAGOR("hanteraFragor"),
    HANTERA_INTYGSUTKAST("hanteraIntygsutkast"),
    KOPIERA_INTYG("kopieraIntyg"),
    MAKULERA_INTYG("makuleraIntyg"),
    SKICKA_INTYG("skickaIntyg"),
    FRAN_JOURNALSYSTEM("franJournalsystem"),
    FRAN_JOURNALSYSTEM_QAONLY("franJournalsystemQAOnly");

    private String name;

    private WebcertFeature(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
