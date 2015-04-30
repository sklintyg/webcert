package se.inera.webcert.spec.ts_diabetes

import se.inera.certificate.spec.Browser

class ValideringTsDiabetes {

    void sparaUtkast() {
        Browser.drive {
            page.spara()
        }
    }

    boolean intygSparatVisas() {
        boolean result
        Browser.drive {
            result = page.intygetSparatMeddelande.isDisplayed()
        }
        result
    }

    boolean intygEjKomplettVisas() {
        boolean result
        Browser.drive {
            result = page.intygetEjKomplettMeddelande.isDisplayed()
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
