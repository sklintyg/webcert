package se.inera.webcert.spec

import se.inera.webcert.spec.util.WebcertRestUtils;
import se.inera.webcert.spec.util.RestClientFixture
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.ContentType.URLENC
import static se.inera.webcert.spec.util.WebcertRestUtils.*

import org.apache.commons.io.IOUtils

class SkapaUtkastViaDjupintegration extends RestClientFixture {

    String intygId
    String intygTyp

    String patientPersonnummer
    String patientFornamn = "Test"
    String patientEfternamn = "Testsson"

    // hosperson
    String hsaId
    String namn

    // Enhet
    String enhetId = "SE4815162344-1A02"
    String enhetnamn = "WebCert-Integration Enhet 1"
    String telefonnummer = "123456789"
    String postadress = "Storgatan 12"
    String postnummer = "12345"
    String postort = "Ankeborg"
    String arbetsplatskod = "arbetsplatskod"
    String epost = "enhet1@webcert.se.invalid"

    // vardgivare
    String vardgivarId = "SE4815162344-1A01"
    String vardgivarnamn = "WebCert-Integration Vårdgivare 1"

    String komplett
    String kodverk = "ICD_10_SE"

    public setIntygId(String value) {
        intygId = value;
    }
    public setIntygTyp(String value) {
        intygTyp = value;
    }
    public setPatientPersonnummer(String value) {
        patientPersonnummer = value;
    }
    public setPatientFornamn(String value) {
        patientFornamn = value;
    }
    public setPatientEfternamn(String value) {
        patientEfternamn = value;
    }
    public setHhsaId(String value) {
        hsaId = value;
    }
    public setNamn(String value) {
        namn = value;
    }
    public setEnhetId(String value) {
        enhetId = value;
    }
    public setKomplett(String value) {
        if (value.equalsIgnoreCase('ja')){
            komplett = 'ja'
        }
        else if (value?.equalsIgnoreCase('nej'))
        {
            komplett = 'nej'
        }
    }

    def response

    public void execute() {
        WebcertRestUtils.login("user1")
        response = WebcertRestUtils.createNewUtkast(json())
    }

    private json() {
        '{ "intygType" : "fk7263", "patientPersonnummer" : "19121212-1212", "patientFornamn" : "Test", "patientEfternamn" : "Test", "patientPostadress" : "adres", "patientPostnummer" : "12345", "patientPostort" : "ort" }'
    }

    boolean utkastCreated() {
        if (response.success) {
            true
        } else {
            false
        }
    }

    String utkastId() {
        IOUtils.toString(response.data)
    }
}
