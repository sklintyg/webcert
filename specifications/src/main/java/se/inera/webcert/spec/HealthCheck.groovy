package se.inera.webcert.spec

import se.inera.webcert.spec.util.RestClientFixture

class HealthCheck extends RestClientFixture {

    def healthCheck

    def endpoint

    void execute() {
        def restClient = createRestClient(baseUrl)
        healthCheck = restClient.get(path: "/health-check/${endpoint}.jsp").data
    }

    def status() {
        healthCheck
    }

}
