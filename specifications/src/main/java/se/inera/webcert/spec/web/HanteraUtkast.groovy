package se.inera.webcert.spec.web

import se.inera.certificate.spec.Browser
import se.inera.webcert.pages.*
import se.inera.webcert.pages.fk7263.EditeraFk7263Page
import se.inera.webcert.pages.fk7263.VisaFk7263Page
import se.inera.webcert.pages.ts_bas.VisaTsBasPage
import se.inera.webcert.pages.ts_diabetes.VisaTsDiabetesPage
import se.inera.webcert.spec.util.WebcertRestUtils

class HanteraUtkast {

    void gaTillEditeraIntygMedTypOchIntygid(String typ, String intygid) {
        Browser.drive {
            go "/web/dashboard#/$typ/edit/$intygid"
            waitFor {
                if (typ == "fk7263") {
                    at se.inera.webcert.pages.fk7263.EditeraFk7263Page
                } else if (typ == "ts-bas") {
                    at se.inera.webcert.pages.ts_bas.EditeraTsBasPage
                } else if (typ == "ts-diabetes") {
                    at se.inera.webcert.pages.ts_diabetes.EditeraTsDiabetesPage
                }
            }
        }
    }

    void gaTillEditIntygMedIntygsid(String id) {
        Browser.drive {
            go "/web/dashboard#/fk7263/edit/${id}"
            waitFor {
                at se.inera.webcert.pages.fk7263.EditeraFk7263Page
            }
        }
    }

    void provaGaTillEditIntygMedIntygsid(String id) {
        Browser.drive {
            go "/web/dashboard#/fk7263/edit/${id}"
        }
    }

    // ------- behaviour

    void raderaUtkast() {
        Browser.drive {
            page.radera.click()
            waitFor {
                page.doneLoading()
            }
            page.konfirmeraRadera.click()
            waitFor {
                page.doneLoading()
            }
        }
    }

    void signeraUtkast() {
        Browser.drive {
            page.signeraBtn.click()
        }
    }

    void sparaUtkast() {
        Browser.drive {
            page.spara()
        }
    }

    boolean harSparat() {
        boolean result
        Browser.drive {
            result = page.harSparat()
        }
        result
    }

    boolean intygEjKomplettVisas() {
        boolean result
        Browser.drive {
            result = page.intygetSparatOchEjKomplettMeddelande.isDisplayed()
        }
        result
    }

    void klickaPaTillbakaKnappen() {
        Browser.drive {
            page.tillbakaButton.click();
        }
    }

    void valjIntygFranEjSigneradeIntyg(intygsid) {
        Browser.drive {
            $("#showBtn-$intygsid").click()
            waitFor {
                at AbstractEditCertPage
            }
        }
    }

    // ------- state

    // ---------- pages

    boolean visaSidanVisas() {
        boolean result
        Browser.drive {
            result = isAt VisaFragaSvarPage
        }
        result
    }

    boolean sokSkrivIntygSidanVisas() {
        boolean result
        Browser.drive {
            result = isAt SokSkrivaIntygPage
        }
        result
    }

    boolean editeraSidanVisas() {
        boolean result
        Browser.drive {
            result = isAt AbstractEditCertPage
        }
        result
    }

    boolean editeraFk7263SidanVisas() {
        boolean result
        Browser.drive {
            result = isAt EditeraFk7263Page
        }
        result
    }

    boolean visaIntygSidanVisas() {
        boolean result
        Browser.drive {
            result = page.intygLaddat.isDisplayed()
        }
        result
    }

    boolean visaIntygSidanVisasMedTyp(intygsTyp) {
        boolean result
        Browser.drive {
            if (intygsTyp == "fk7263")
                result = isAt VisaFk7263Page
            else if (intygsTyp == "ts-bas")
                result = isAt VisaTsBasPage
            else if (intygsTyp == "ts-diabetes")
                result = isAt VisaTsDiabetesPage
            result = result && page.intygLaddat.isDisplayed()
        }
        result
    }

    boolean ejSigneradeIntygSidanVisas() {
        boolean result
        Browser.drive {
            result = isAt UnsignedIntygPage
        }
        result
    }

    // ---------- elements
    boolean ingaEjSigneradeIntygVisas() {
        return WebcertRestUtils.getNumberOfUnsignedCertificates() == 0
    }

    boolean signeraKnappAktiverad() {
        boolean result
        Browser.drive {
            result = page.signeraBtn.isEnabled()
        }
        result
    }

    boolean signeraKnappVisas() {
        boolean result
        Browser.drive {
            result = page.signeraBtn.isDisplayed()
        }
        result
    }

    boolean signeringKraverLakareVisas() {
        boolean result
        Browser.drive {
            result = page.signRequiresDoctorMessage.isDisplayed()
        }
        result
    }

    boolean intygetSigneratMeddelandeVisas() {
        boolean result
        Browser.drive {
            // this is kind of a special case as the page changes based on intyg's type
            result = page.certificateIsSentToITMessage.isDisplayed()
        }
        result
    }

    boolean intygetEjKomplettMeddelandeVisas() {
        boolean result
        Browser.drive {
            result = page.intygetSparatOchEjKomplettMeddelande.isDisplayed()
        }
        result
    }

    boolean intygetKomplettMeddelandeVisas() {
        boolean result
        Browser.drive {
            result = page.intygetSparatOchKomplettMeddelande.isDisplayed()
        }
        result
    }

    boolean felmeddelandeVisas() {
        boolean result
        Browser.drive {
            result = page.errorPanel.isDisplayed()
        }
        result
    }

    boolean skrivUtKnappVisas() {
        boolean result
        Browser.drive {
            result = page.skrivUtBtn.isDisplayed()
        }
        result
    }

    String kanInteTaStallningTsBas() {
        def result
        Browser.drive {
            waitFor {
                at se.inera.webcert.pages.ts_bas.EditeraTsBasPage
            }
            result = page.bedomning.behorighetGroup
        }
        result
    }

    // --
    String kanInteTaStallningTsDiabetes() {
        def result
        Browser.drive {
            waitFor {
                at se.inera.webcert.pages.ts_diabetes.EditeraTsDiabetesPage
            }
            result = page.bedomning.behorighetGroup
        }
        result
    }

    // ------ form elements
    // -------- form edit
    void andraPostadress(String value) {
        Browser.drive {
            page.postadress = value
        }
    }

    void andraPostnummer(String value) {
        Browser.drive {
            page.postnummer = value
        }
    }

    void andraPostort(String value) {
        Browser.drive {
            page.postort = value
        }
    }

    void andraTelefonnummer(String value) {
        Browser.drive {
            page.telefonnummer = value
        }
    }

    void andraEpost(String value) {
        Browser.drive {
            page.epost = value
        }
    }

    // -------- form view

    String postadress() {
        def result
        Browser.drive {
            result = page.postadress.value()
        }
        result
    }

    String postnummer() {
        def result
        Browser.drive {
            result = page.postnummer.value()
        }
        result
    }

    String postort() {
        def result
        Browser.drive {
            result = page.postort.value()
        }
        result
    }

    String telefonnummer() {
        def result
        Browser.drive {
            result = page.telefonnummer.value()
        }
        result
    }

    String epost() {
        def result
        Browser.drive {
            result = page.epost.value()
        }
        result
    }

    boolean kommentarInnehallarText(String text) {
        def result = false;
        Browser.drive {
            result = page.kommentar.text().contains(text);
        }
        return result;
    }

    boolean feltMedNamnInnehallarText(String felt, String text) {
        boolean result
        Browser.drive {
            waitFor {
                result = page."$felt".text().contains(text)
                //result = page.vardenhet.postadress.text().contains(text);
            }
        }
        return result
    }

    // ------- utils
    boolean wait4it() {
        Thread.sleep(5000)
        true
    }

    boolean sekretessmarkeringVisas() {
        def result
        Browser.drive {
            at EditeraIntygPage
            result = page.sekretessmarkering.isDisplayed()
        }
        result
    }

}
