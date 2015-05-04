package se.inera.webcert.spec.ts_bas

import se.inera.certificate.spec.Browser


class ValideringTsBas {

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
                !page.valideringSyn.isDisplayed() &&
                !page.valideringHorselBalans.isDisplayed() &&
                !page.valideringFunktionsNedsattning.isDisplayed() &&
                !page.valideringHjartkarl.isDisplayed() &&
                !page.valideringNeurologi.isDisplayed() &&
                !page.valideringMedvetandestorning.isDisplayed() &&
                !page.valideringNjurar.isDisplayed() &&
                !page.valideringKognitivt.isDisplayed() &&
                !page.valideringSomnVakenhet.isDisplayed() &&
                !page.valideringNarkotikaLakemedel.isDisplayed() &&
                !page.valideringPsykiskt.isDisplayed() &&
                !page.valideringUtvecklingsStorning.isDisplayed() &&
                !page.valideringSjukhusVard.isDisplayed() &&
                !page.valideringMedicinering.isDisplayed() &&
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

    boolean valideringSynVisas() {
        boolean result
        Browser.drive{
            result = page.valideringSyn.isDisplayed()
        }
        result
    }

    boolean valideringHorselBalansVisas() {
        boolean result
        Browser.drive{
            result = page.valideringHorselBalans.isDisplayed()
        }
        result
    }

    boolean valideringFunktionsNedsattningVisas() {
        boolean result
        Browser.drive{
            result = page.valideringFunktionsNedsattning.isDisplayed()
        }
        result
    }

    boolean valideringHjartkarlVisas() {
        boolean result
        Browser.drive{
            result = page.valideringHjartkarl.isDisplayed()
        }
        result
    }

    boolean valideringNeurologiVisas() {
        boolean result
        Browser.drive{
            result = page.valideringNeurologi.isDisplayed()
        }
        result
    }

    boolean valideringMedvetandestorningVisas() {
        boolean result
        Browser.drive{
            result = page.valideringMedvetandestorning.isDisplayed()
        }
        result
    }

    boolean valideringNjurarVisas() {
        boolean result
        Browser.drive{
            result = page.valideringNjurar.isDisplayed()
        }
        result
    }

    boolean valideringKognitivtVisas() {
        boolean result
        Browser.drive{
            result = page.valideringKognitivt.isDisplayed()
        }
        result
    }

    boolean valideringSomnVakenhetVisas() {
        boolean result
        Browser.drive{
            result = page.valideringSomnVakenhet.isDisplayed()
        }
        result
    }

    boolean valideringNarkotikaLakemedelVisas() {
        boolean result
        Browser.drive{
            result = page.valideringNarkotikaLakemedel.isDisplayed()
        }
        result
    }

    boolean valideringPsykisktVisas() {
        boolean result
        Browser.drive{
            result = page.valideringPsykiskt.isDisplayed()
        }
        result
    }

    boolean valideringUtvecklingsStorningVisas() {
        boolean result
        Browser.drive{
            result = page.valideringUtvecklingsStorning.isDisplayed()
        }
        result
    }

    boolean valideringSjukhusVardVisas() {
        boolean result
        Browser.drive{
            result = page.valideringSjukhusVard.isDisplayed()
        }
        result
    }

    boolean valideringMedicineringVisas() {
        boolean result
        Browser.drive{
            result = page.valideringMedicinering.isDisplayed()
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
