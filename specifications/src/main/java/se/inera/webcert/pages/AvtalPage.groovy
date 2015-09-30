package se.inera.webcert.pages

import geb.Browser
import geb.Page
import se.inera.certificate.page.AbstractPage

class AvtalPage extends AbstractPage {
    static at = { waitForModal() }

    static def waitForModal(){
        Browser.drive{
            waitFor {
                $(".modal-dialog").isDisplayed()
            }
        }
    }
    static content = {
        termsBody(required:true,wait:true) {$(".modal-dialog")}
    }
}
