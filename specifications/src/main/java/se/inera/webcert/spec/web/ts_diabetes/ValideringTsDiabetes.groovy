package se.inera.webcert.spec.web.ts_diabetes

import se.inera.intyg.common.specifications.spec.Browser
import se.inera.webcert.spec.util.screenshot.ExceptionHandlingFixture

class ValideringTsDiabetes extends ExceptionHandlingFixture {

    boolean sparaUtkast() {
        Browser.drive {
            page.spara()
        }
    }

    boolean meddelandeIntygetKomplettVisas() {
        boolean result
        Browser.drive {
            waitFor {
                page.intygetSparatOchKomplettMeddelande.isDisplayed()
            }
            result = page.intygetSparatOchKomplettMeddelande?.isDisplayed()
        }
        result
    }

    boolean meddelandeIntygetEjKomplettVisas() {
        boolean result
        Browser.drive {
            waitFor {
                page.intygetSparatOchEjKomplettMeddelande.isDisplayed()
            }
            result = page.intygetSparatOchEjKomplettMeddelande.isDisplayed()
        }
        result
    }

    boolean ingaValideringsfelVisas() {
        boolean result
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

    boolean valideringPatientVisas() {
        boolean result
        Browser.drive{
            result = page.valideringPatient.isDisplayed()
        }
        result
    }

    boolean valideringIntygAvserVisas() {
        boolean result
        Browser.drive{
            result = page.valideringIntygAvser.isDisplayed()
        }
        result
    }

    boolean valideringIdentitetVisas() {
        boolean result
        Browser.drive{
            result = page.valideringIdentitet.isDisplayed()
        }
        result
    }

    boolean valideringDiabetesVisas() {
        boolean result
        Browser.drive{
            result = page.valideringDiabetes.isDisplayed()
        }
        result
    }

    boolean valideringHypoglykemierVisas() {
        boolean result
        Browser.drive{
            result = page.valideringHypoglykemier.isDisplayed()
        }
        result
    }

    boolean valideringSynVisas() {
        boolean result
        Browser.drive{
            result = page.valideringSyn.isDisplayed()
        }
        result
    }

    boolean valideringBedomningVisas() {
        boolean result
        Browser.drive{
            result = page.valideringBedomning.isDisplayed()
        }
        result
    }

    boolean valideringVardEnhetVisas() {
        boolean result
        Browser.drive{
            result = page.valideringVardEnhet.isDisplayed()
        }
        result
    }

}
