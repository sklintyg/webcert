package se.inera.webcert.spec.ts_diabetes

import se.inera.webcert.spec.Browser

class ValideringTsDiabetes {

    boolean intygSparatVisas() {
        Browser.drive {
            waitFor {
                page.intygetSparatMeddelande.isDisplayed()
            }
        }
        true
    }

    boolean intygEjKomplettVisas() {
        Browser.drive {
            waitFor(6) {
                page.intygetEjKomplettMeddelande.isDisplayed()
            }
        }
        true
    }

    def ingaValideringsfelVisas() {
        def result = false
        Browser.drive {
            result = !page.valideringPatient.isDisplayed() &&
                    !page.valideringIntygAvser.isDisplayed() &&
                    !page.valideringIdentitet.isDisplayed() &&
                    !page.valideringDiabetes.isDisplayed() &&
                    !page.valideringHypoglykemier.isDisplayed() &&
                    !page.valideringSyn.isDisplayed() &&
                    !page.valideringBedomning.isDisplayed() &&
                    !page.valideringVardEnhet.isDisplayed()
        }
        result
    }

    def valideringPatientVisas() {
        def result = false
        Browser.drive{
            result = page.valideringPatient.isDisplayed()
        }
        result
    }

    def valideringIntygAvserVisas() {
        def result = false
        Browser.drive{
            result = page.valideringIntygAvser.isDisplayed()
        }
        result
    }

    def valideringIdentitetVisas() {
        def result = false
        Browser.drive{
            result = page.valideringIdentitet.isDisplayed()
        }
        result
    }

    def valideringDiabetesVisas() {
        def result = false
        Browser.drive{
            result = page.valideringDiabetes.isDisplayed()
        }
        result
    }

    def valideringHypoglykemierVisas() {
        def result = false
        Browser.drive{
            result = page.valideringHypoglykemier.isDisplayed()
        }
        result
    }

    def valideringSynVisas() {
        def result = false
        Browser.drive{
            result = page.valideringSyn.isDisplayed()
        }
        result
    }

    def valideringBedomningVisas() {
        def result = false
        Browser.drive{
            result = page.valideringBedomning.isDisplayed()
        }
        result
    }

    def valideringVardEnhetVisas() {
        def result = false
        Browser.drive{
            result = page.valideringVardEnhet.isDisplayed()
        }
        result
    }

}
