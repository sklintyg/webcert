package se.inera.webcert.service.feature;

public enum WebcertFeature {

    HANTERA_FRAGOR("hanteraFragor"),
    HANTERA_INTYGSUTKAST("hanteraIntygsutkast"),
    KOPIERA_INTYG("kopieraIntyg"),
    MAKULERA_INTYG("makuleraIntyg"),
    SKICKA_INTYG("skickaIntyg"),
    ARBETSGIVARUTSKRIFT("arbetsgivarUtskrift"),
    JS_LOGGNING("jsLoggning"),
    JS_MINIFIED("jsMinified", "webcert.useMinifiedJavaScript");

    private String name;
    private String envName;

    private WebcertFeature(String name) {
        this.name = name;
    }

    private WebcertFeature(String name, String envName) {
        this(name);
        this.envName = envName;
    }

    public String getName() {
        return name;
    }

    public String getEnvName() {
        return envName;
    }
}
