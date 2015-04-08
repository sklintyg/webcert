package se.inera.webcert.spec.ts_bas

import se.inera.webcert.spec.Browser

class ValideringTsBas {

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

    def valideringSynVisas() {
        def result = false
        Browser.drive{
            result = page.valideringSyn.isDisplayed()
        }
        result
    }

    def valideringHorselBalansVisas() {
        def result = false
        Browser.drive{
            result = page.valideringHorselBalans.isDisplayed()
        }
        result
    }

    def valideringFunktionsNedsattningVisas() {
        def result = false
        Browser.drive{
            result = page.valideringFunktionsNedsattning.isDisplayed()
        }
        result
    }

    def valideringHjartkarlVisas() {
        def result = false
        Browser.drive{
            result = page.valideringHjartkarl.isDisplayed()
        }
        result
    }

    def valideringNeurologiVisas() {
        def result = false
        Browser.drive{
            result = page.valideringNeurologi.isDisplayed()
        }
        result
    }

    def valideringMedvetandestorningVisas() {
        def result = false
        Browser.drive{
            result = page.valideringMedvetandestorning.isDisplayed()
        }
        result
    }

    def valideringNjurarVisas() {
        def result = false
        Browser.drive{
            result = page.valideringNjurar.isDisplayed()
        }
        result
    }

    def valideringKognitivtVisas() {
        def result = false
        Browser.drive{
            result = page.valideringKognitivt.isDisplayed()
        }
        result
    }

    def valideringSomnVakenhetVisas() {
        def result = false
        Browser.drive{
            result = page.valideringSomnVakenhet.isDisplayed()
        }
        result
    }

    def valideringNarkotikaLakemedelVisas() {
        def result = false
        Browser.drive{
            result = page.valideringNarkotikaLakemedel.isDisplayed()
        }
        result
    }

    def valideringPsykisktVisas() {
        def result = false
        Browser.drive{
            result = page.valideringPsykiskt.isDisplayed()
        }
        result
    }

    def valideringUtvecklingsStorningVisas() {
        def result = false
        Browser.drive{
            result = page.valideringUtvecklingsStorning.isDisplayed()
        }
        result
    }

    def valideringSjukhusVardVisas() {
        def result = false
        Browser.drive{
            result = page.valideringSjukhusVard.isDisplayed()
        }
        result
    }

    def valideringMedicineringVisas() {
        def result = false
        Browser.drive{
            result = page.valideringMedicinering.isDisplayed()
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
