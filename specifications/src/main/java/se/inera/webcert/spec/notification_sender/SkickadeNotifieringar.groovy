package se.inera.webcert.spec.notification_sender
import se.inera.webcert.spec.util.RestClientFixture
import se.inera.webcert.spec.util.WebcertRestUtils

class SkickadeNotifieringar extends RestClientFixture {

    def notifieringar

    String id 
    String kod
    int antal = 0
    def matching = new HashMap<String, Collection>()

    public void execute() {
        notifieringar = WebcertRestUtils.getNotifications(antal)
        notifieringar.each {
            if (it?.utlatande?.utlatandeId?.extension.equalsIgnoreCase(id) &&
                it?.utlatande?.handelse?.handelsekod?.code.equalsIgnoreCase(kod)) {
                matching.put(it?.utlatande?.handelse?.handelsekod?.code, it)
            }
        }
    }

    public boolean checkHandelseKod() {
        return matching.isEmpty() == true ? false : true
    }

    public def antalFragor() {
        matching.get(kod)?.utlatande?.fragorOchSvar?.antalFragor
    }

    public def antalHanteradeFragor() {
        matching.get(kod)?.utlatande?.fragorOchSvar?.antalHanteradeFragor
    }

    public def antalSvar() {
        matching.get(kod)?.utlatande?.fragorOchSvar?.antalSvar
    }

    public def antalHanteradeSvar() {
        matching.get(kod)?.utlatande?.fragorOchSvar?.antalHanteradeSvar
    }

}
