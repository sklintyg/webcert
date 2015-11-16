package se.inera.webcert.spec.web

import se.inera.certificate.spec.Browser

class Fmb {

    def populeraData() {
        Browser.drive {
            go System.getProperty("webcert.baseUrl") + "api/testability/updatefmbdata"
            getDriver().navigate().back()
        }
    }

}
