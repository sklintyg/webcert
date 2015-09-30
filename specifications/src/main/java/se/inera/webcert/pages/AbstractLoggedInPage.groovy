package se.inera.webcert.pages

import se.inera.certificate.page.AbstractPage
import se.inera.certificate.spec.Browser

class AbstractLoggedInPage extends AbstractPage {

    static content = {

        // header
        webcertLogoLink(required: false, to: [SokSkrivaIntygPage, UnhandledQAPage], toWait: true) { $("#webcertLogoLink") }
        bytVardenhetLink(required: false) { $("#wc-care-unit-clinic-selector") }
        loggaUtLink(required: false) { $("#logoutLink") }
        omWebcertLink(required: false) { $("#aboutLink") }
        omWebcertDialog(required: false) { $("#omWebcertDialog") }
        huvudmeny(required: false) { $("#huvudmeny")}
        skrivIntyg(required: false, to: SokSkrivaIntygPage, toWait: true){$("#menu-skrivintyg")}
        unhandledQa(required: false, to: UnhandledQAPage, toWait: true){$("#menu-unhandled-qa")}
        unsigned(required: false, to: UnsignedIntygPage, toWait: true){$("#menu-unsigned")}
        omWebcert(required: false, to: OmWebcertPage, toWait: true){$("#menu-about")}
        
        // care unit dialog
        careUnitSelector(required: false) { $("div#wc-care-unit-clinic-selector")}
        activeUnit(required: false) {careUnit -> $("#select-active-unit-${careUnit}")}
        careUnitModal(required: false) { $("a#wc-care-unit-clinic-selector-link") }
        careUnitModalBody(required: false) { $(".modal-body") }
        modalBackdrop(required:false) {$('.modal-backdrop')}
    }

    def waitForModalBackdropToHide(){
        waitFor() {
            return !modalBackdrop.isDisplayed();
        }
    }

    def selectCareUnit(String careUnit) {
        activeUnit(careUnit).click()
        waitFor {
            doneLoading()
        }
    }

    boolean isCareUnitVisible(String careUnit) {
        activeUnit(careUnit).isDisplayed()
    }

    def clickCareUnitModal() {
        careUnitModal.click();
    }

    def expandEnhetModal(String id) {
        Browser.drive {
            waitFor {
                $("#expand-enhet-${id}").click()
            }
        }
    }

    def modalIsDisplayed() {
        careUnitModalBody.isDisplayed();
    }

    def isCareUnitModalVisible(String careUnit, boolean expected) {
        Browser.drive {
            def ref = "#select-active-unit-${careUnit}-modal"
            if (expected) {
                waitFor {
                    $(ref).isDisplayed()
                }
            } else {
                !$(ref).isDisplayed()
            }
        }
    }

    def selectCareUnitModal(String careUnit) {
        Browser.drive {
            waitFor {
                $("#select-active-unit-${careUnit}-modal").click()
            }
        }
    }

    boolean isNumberPresent(String careUnit, String expected) {
        Browser.drive {
            waitFor {
                expected == $("#select-active-unit-${careUnit}").find(".qa-circle").text()
            }
        }
    }

    boolean isNumberPresentInModal(String careUnit, String expected) {
        Browser.drive {
            waitFor {
                expected == $("#fraga-svar-stat-${careUnit}").text()
            }
        }
    }

    def gaTillSkrivIntyg() {
        skrivIntyg().click()
    }

    def gaTillFragaSvar() {
        unhandledQa().click()
    }

    def gaTillEjSigneradeIntyg() {
        unsigned().click()
    }

    def logout() {
        loggaUtLink.click()
    }
}
