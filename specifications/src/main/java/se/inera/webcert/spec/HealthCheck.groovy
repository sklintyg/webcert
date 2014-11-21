package se.inera.webcert.spec

import se.inera.webcert.spec.util.RestClientFixture

class HealthCheck extends RestClientFixture {

    def healthCheck

    def endpoint

    void execute() {
        def restClient = createRestClient(baseUrl)
        healthCheck = restClient.get(path: "/monitoring/health-check/${endpoint}").data
    }

    def status() {
        healthCheck
    }

}
