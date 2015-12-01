package se.inera.intyg.webcert.specifications.spec.api

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.springframework.core.io.ClassPathResource
import se.inera.intyg.webcert.specifications.spec.util.RestClientFixture

import static groovyx.net.http.ContentType.JSON

public class SvaraPaFraga extends RestClientFixture {

    String vardgivare
    String enhet
    String internReferens
    String svarsText

    def execute() {
        def restClient = createRestClient("${baseUrl}testability/")
        def response = restClient.put(
                path: "questions/svara/${vardgivare}/${enhet}/${internReferens}",
                body: svarsText,
                requestContentType: JSON
        )
    }
}
