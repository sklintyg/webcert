package se.inera.intyg.webcert.specifications.spec.api

import se.inera.intyg.webcert.specifications.spec.util.RestClientFixture

class HealthCheckService extends RestClientFixture {

    def healthCheck

    def endpoint

    void execute() {
        def restClient = createRestClient("${baseUrl}testability/")
        healthCheck = restClient.get(path: "/monitoring/health-check/${endpoint}").data
    }

    def status() {
        healthCheck
    }

}
