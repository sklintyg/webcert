package se.inera.webcert.spec.util

class SkickadeNotifieringar extends RestClientFixture {

    def notifieringar

    public void execute() {
        def restClient = createRestClient(baseUrl)
        notifieringar = restClient.get(path: "notification-stub/notifieringar").data
    }

    public String intygsId() {
        notifieringar.toString();
    }


}
