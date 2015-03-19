package se.inera.webcert.spec

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import org.springframework.core.io.ClassPathResource

import se.inera.webcert.spec.util.RestClientFixture
import static groovyx.net.http.ContentType.JSON

public class StallFraga extends RestClientFixture {

    String vardgivare
    String enhet
    String intygsId
    String ämne
    String frågeText
    String typ = "fk7263"
    String internReferens
    String internReferens() {
        internReferens
    }

    def execute() {
        def restClient = createRestClient("${baseUrl}services/")
        def response = restClient.post(
                path: "questions/skickafraga/${vardgivare}/${enhet}/${intygsId}/${typ}",
                body: "{\"amne\":\"${ämne}\",\"frageText\":\"${frågeText}\"}",
                requestContentType: JSON
        )
        internReferens = response.data.internReferens
    }
}
