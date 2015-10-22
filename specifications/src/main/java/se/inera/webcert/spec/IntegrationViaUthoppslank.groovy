package se.inera.webcert.spec

import se.inera.certificate.spec.Browser
import se.inera.webcert.pages.VisaFragaSvarPage

class IntegrationViaUthoppslank {

    boolean exists(content) {
        content
    }

    def gaTillIntygsvyViaUthoppMedIntygsId(String id) {
        Browser.drive {
            go "/webcert/web/user/certificate/${id}/questions"
            waitFor {
                at VisaFragaSvarPage
            }
        }
    }

    boolean meddelandeIntygetInteSkickatVisas() {
        def result
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                result = page.certificateIsNotSentToFkMessage.isDisplayed()
            }
        }
        return result
    }

    boolean lamnaFragaSvarVarningVisas() {
        def result
        Browser.drive {
            waitFor {
                result = page.qaOnlyDialog.isDisplayed()
            }
        }
        return result
    }

    def gaTillEjSigneradeUtkast() {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            page.linkEjSigneradeUtkast.click()
        }
    }

    def gaTillSokSkrivaIntyg() {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
                page.linkSokSkrivIntyg.click()
        }
    }

    def avbrytLamnaFragaOchSvar() {
        Browser.drive {
            page.qaOnlyDialogCancel.click()
            waitFor {
                at VisaFragaSvarPage
            }
        }
    }

}
