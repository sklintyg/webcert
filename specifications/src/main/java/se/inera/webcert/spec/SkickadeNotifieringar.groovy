package se.inera.webcert.spec
import se.inera.webcert.spec.util.RestClientFixture
import se.inera.webcert.spec.util.WebcertRestUtils

class SkickadeNotifieringar extends RestClientFixture {

    def notifieringar

    String id 
    String kod
    int antal

    public void execute() {
        notifieringar = WebcertRestUtils.getNotifications(antal)
    }

    public boolean checkHandelseKod() {
        def matching = []
        notifieringar.each { 
            if (it?.utlatande?.utlatandeId?.extension.equalsIgnoreCase(id) &&
                it?.utlatande?.handelse?.handelsekod?.code.equalsIgnoreCase(kod)) {
                matching += it
            }   
        }
        return matching.isEmpty() == true ? false : true
    }  

}
