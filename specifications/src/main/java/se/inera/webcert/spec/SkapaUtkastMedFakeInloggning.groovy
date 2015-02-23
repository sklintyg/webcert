package se.inera.webcert.spec

import se.inera.webcert.spec.util.WebcertRestUtils;
import se.inera.webcert.spec.util.RestClientFixture
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.ContentType.URLENC
import static se.inera.webcert.spec.util.WebcertRestUtils.*

import org.apache.commons.io.IOUtils

class SkapaUtkastMedFakeInloggning extends RestClientFixture {

    String intygTyp = "fk7263"

    String patientPersonnummer
    String patientFornamn = "Test"
    String patientEfternamn = "Testsson"
    
    String hsaUser = "user1"

    def response

    public void execute() {
        WebcertRestUtils.login("$hsaUser")
        response = WebcertRestUtils.createNewUtkast(intygTyp, json())
    }

    boolean utkastCreated() {
        return response.success
    }

    private json() {
        """{ "intygType" : "fk7263", "patientPersonnummer" : "$patientPersonnummer", "patientFornamn" : "$patientFornamn", "patientEfternamn" : "$patientEfternamn", "patientPostadress" : "adres", "patientPostnummer" : "12345", "patientPostort" : "ort" }"""
    }

    // Return a string representation of the response payload, more specifically: utkastId
    String utkastId() {
        IOUtils.toString(response.data)
    }
}
