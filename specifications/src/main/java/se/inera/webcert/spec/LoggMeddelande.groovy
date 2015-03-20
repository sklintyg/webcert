package se.inera.webcert.spec

import se.inera.log.messages.AbstractLogMessage;
import se.inera.webcert.spec.util.RestClientFixture

/**
 * @author andreaskaltenbach
 */
class LoggMeddelande extends RestClientFixture {

    def restClient = createRestClient(baseUrl)

    private AbstractLogMessage logMessage
    private int count
    
    def rensaLoggMeddelanden() {
        restClient.delete(path: "logMessages/")
    }
    
    def hamtaLoggMeddelande() {
        logMessage = restClient.get(path: "logMessages/")
    }
    
    String aktivitet() {
        logMessage.activityType.name
    }

    int antalLoggMeddelanden() {
        restClient.get(path: "logMessages/count").data
    }
    

}
