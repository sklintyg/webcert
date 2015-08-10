package se.inera.webcert.spec.api

import groovy.json.JsonOutput
import se.inera.webcert.hsa.model.Mottagning
import se.inera.webcert.hsa.model.Vardenhet
import se.inera.webcert.spec.util.RestClientFixture

import static groovyx.net.http.ContentType.JSON

/**
 * @author andreaskaltenbach
 */
public class Vardgivare extends RestClientFixture {

    def vardgivarid
    def vardgivarnamn = "Vardgivare"

    def enhetsid
    def enhetsnamn
    def enhetsmail

    def mottagningsid
    def mottagningsnamn

    se.inera.webcert.hsa.model.Vardgivare currentVardgivare
    Vardenhet currentEnhet
    Mottagning currentMottagning

    def vardgivare = []

    public void execute() {
        buildOrganizationElements()
        attachVardgivare()
    }

    def attachVardgivare() {
        se.inera.webcert.hsa.model.Vardgivare existingVardgivare = vardgivare.find { it.id == vardgivarid }

        if (!existingVardgivare) {
            // if absent, add whole vardgivare to list
            vardgivare.add(currentVardgivare)
        } else {
            // if vargivare is existent, attach enhet
            attachEnhet(existingVardgivare)
        }
    }

    def attachEnhet(existingVardgivare) {

        if (currentEnhet) {
            def existingEnhet = existingVardgivare.vardenheter.find { it.id == currentEnhet.id }

            if (!existingEnhet) {
                //if absent, add whole enhet enhet list
                existingVardgivare.vardenheter.add(currentEnhet)
            } else {
                // if existent, attach mottagning
                attachMottagning(existingEnhet)
            }
        }
    }

    def attachMottagning(existingEnhet) {
        if (currentMottagning) {

            existingEnhet.mottagningar.add(currentMottagning)
        }
    }

    def buildOrganizationElements() {
        currentVardgivare = new se.inera.webcert.hsa.model.Vardgivare(
                id: vardgivarid,
                namn: vardgivarnamn)

        if (enhetsid) {
            currentEnhet = new Vardenhet(
                    id: enhetsid,
                    namn: enhetsnamn,
                    mail: enhetsmail)
            currentVardgivare.vardenheter.add(currentEnhet)
        } else {
            currentEnhet = null
        }

        if (mottagningsid) {
            currentMottagning = new Mottagning(
                    id: mottagningsid,
                    namn: mottagningsnamn
            )
            currentEnhet.mottagningar.add(currentMottagning)
        } else {
            currentMottagning = null
        }
    }

    private vardgivareJson() {
        JsonOutput.toJson(vardgivare)
    }

    def endTable() {
        def restClient = createRestClient("${baseUrl}services/")
        restClient.post(
                path: 'hsa-api/vardgivare',
                body: vardgivareJson(),
                requestContentType: JSON
        )
    }
}
