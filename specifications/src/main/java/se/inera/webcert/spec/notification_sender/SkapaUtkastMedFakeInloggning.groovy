package se.inera.webcert.spec.notification_sender

import se.inera.webcert.spec.util.WebcertRestUtils

class SkapaUtkastMedFakeInloggning {

    String intygTyp = "fk7263"

    String patientPersonnummer
    String patientFornamn = "Test"
    String patientEfternamn = "Testsson"
    
    def response

    public void execute() {
        WebcertRestUtils.login()
        response = WebcertRestUtils.createNewUtkast(intygTyp, json())
    }

    public boolean utkastCreated() {
        return response.success
    }

    public String utkastId() {
        response.data.intygsId
    }

    public long version() {
        response.data.version
    }

    private json() {
        """{ "intygType" : "fk7263", "patientPersonnummer" : "$patientPersonnummer", "patientFornamn" : "$patientFornamn", "patientEfternamn" : "$patientEfternamn", "patientPostadress" : "adres", "patientPostnummer" : "12345", "patientPostort" : "ort" }"""
    }


}
