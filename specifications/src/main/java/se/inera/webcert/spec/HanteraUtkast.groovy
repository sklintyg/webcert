package se.inera.webcert.spec

import se.inera.certificate.spec.Browser
import se.inera.webcert.pages.*
import se.inera.webcert.pages.fk7263.EditCertPage
import se.inera.webcert.pages.fk7263.VisaFk7263Page
import se.inera.webcert.pages.ts_bas.VisaTsBasPage
import se.inera.webcert.pages.ts_diabetes.VisaTsDiabetesPage
import se.inera.webcert.spec.util.WebcertRestUtils

class HanteraUtkast {

    def loggaInSom(String id) {
        Browser.drive {
            go "/welcome.jsp"

            waitFor {
                at WelcomePage
            }
            page.loginAs(id)
        }
    }

    // ------- navigation

    def gaTillEjSigneradeIntyg() {
        Browser.drive {
            go "/web/dashboard#/unsigned"
            waitFor {
                at UnsignedIntygPage
            }
        }
    }

    def gaTillEditeraIntygMedTypOchIntygid(String typ, String intygid) {
        Browser.drive {
            go "/web/dashboard#/$typ/edit/$intygid"
            waitFor {
                if (typ == "fk7263") {
                    at se.inera.webcert.pages.fk7263.EditCertPage
                } else if (typ == "ts-bas") {
                    at se.inera.webcert.pages.ts_bas.EditCertPage
                } else if (typ == "ts-diabetes") {
                    at se.inera.webcert.pages.ts_diabetes.EditCertPage
                }
            }
        }
    }

    def gaTillEditIntygMedIntygsid(String id) {
        Browser.drive {
            go "/web/dashboard#/fk7263/edit/${id}"
            waitFor {
                at EditCertPage
            }
        }
    }

    def provaGaTillEditIntygMedIntygsid(String id) {
        Browser.drive {
            go "/web/dashboard#/fk7263/edit/${id}"
        }
    }

    // ------- behaviour

    boolean raderaUtkast() {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            waitFor {
                page.radera.click()
            }
            Thread.sleep(300);
            waitFor {
                page.konfirmeraRadera.click()
            }
            Thread.sleep(300);
        }
    }

    boolean signeraUtkast() {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            page.signeraBtn.click()
        }
    }

    boolean sparaUtkast() {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            page.spara()
        }
    }

    boolean harSparat() {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            page.harSparat()
        }
    }

    boolean intygEjKomplettVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            waitFor {
                result = page.intygetSparatOchEjKomplettMeddelande.isDisplayed()
            }
        }
        result
    }

    def klickaPaTillbakaKnappen() {
        Browser.drive {
            page.tillbakaButton.click();
        }
    }

    def valjIntygFranEjSigneradeIntyg(intygsid) {
        Browser.drive {
            $("#showBtn-$intygsid").click()
            waitFor {
                at se.inera.webcert.pages.EditeraIntygPage
            }
        }
    }

    // ------- state

    // ---------- pages

    boolean visaSidanVisas() {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
        }
    }

    boolean sokSkrivIntygSidanVisas() {
        Browser.drive {
            waitFor {
                at SokSkrivaIntygPage
            }
        }
    }

    boolean editeraSidanVisas() {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
        }
    }

    boolean editeraFk7263SidanVisas() {
        Browser.drive {
            waitFor {
                at se.inera.webcert.pages.fk7263.EditCertPage
            }
        }
    }

    boolean visaIntygSidanVisas() {
        Browser.drive {
            waitFor {
                at VisaFk7263Page
                page.intygLaddat.isDisplayed()
            }
        }
    }

    boolean visaIntygSidanVisasMedTyp(intygsTyp) {
        Browser.drive {
            waitFor {
                if (intygsTyp == "fk7263")
                    at VisaFk7263Page
                else if (intygsTyp == "ts-bas")
                    at VisaTsBasPage
                else if (intygsTyp == "ts-diabetes")
                    at VisaTsDiabetesPage
            }
            page.intygLaddat.isDisplayed()
        }
    }

    boolean ejSigneradeIntygSidanVisas() {
        Browser.drive {
            waitFor {
                at UnsignedIntygPage
            }
        }
    }

    // ---------- elements
    boolean ingaEjSigneradeIntygVisas() {
        return WebcertRestUtils.getNumberOfUnsignedCertificates() == 0
    }

    boolean signeraKnappAktiverad() {
        boolean result
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            result = page.signeraBtn.isEnabled()
        }
        result
    }

    boolean signeraKnappEjAktiverad() {
        boolean result
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            result = !page.signeraBtn.isEnabled()
        }
        result
    }

    boolean signeraKnappVisas() {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            return page.signeraBtn.isDisplayed()
        }
    }

    boolean signeraKnappEjVisas() {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            return !page.signeraBtnNoWait.isDisplayed()
        }
    }

    boolean signeringKraverLakareVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            waitFor {
                result = page.signRequiresDoctorMessage.isDisplayed()
            }
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
            waitFor {
                at EditeraIntygPage
            }
            result = page.intygetSparatOchEjKomplettMeddelande.isDisplayed()
        }
        result
    }

    boolean intygetKomplettMeddelandeVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            result = page.intygetSparatOchKomplettMeddelande.isDisplayed();
        }
        result
    }

    boolean felmeddelandeVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            result = page.errorPanel.isDisplayed()
        }
        result
    }

    boolean skrivUtKnappVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            result = page.skrivUtBtn.isDisplayed()
        }
        result
    }

    String kanInteTaStallningTsBas() {
        def result
        Browser.drive {
            waitFor {
                at se.inera.webcert.pages.ts_bas.EditCertPage
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
                at se.inera.webcert.pages.ts_diabetes.EditCertPage
            }
            result = page.bedomning.behorighetGroup
        }
        result
    }

    // ------ form elements
    // -------- form edit
    boolean andraPostadress(String value) {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            page.enhetsPostadress = value
        }
        true
    }

    boolean andraPostnummer(String value) {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            page.enhetsPostnummer = value
        }
        true
    }

    boolean andraPostort(String value) {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            page.enhetsPostort = value
        }
        true
    }

    boolean andraTelefonnummer(String value) {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            page.enhetsTelefonnummer = value
        }
        true
    }

    boolean andraEpost(String value) {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            page.enhetsEpost = value
        }
        true
    }

    // -------- form view

    String postadress() {
        def result
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            result = page.enhetsPostadress.value()
        }
        result
    }

    String postnummer() {
        def result
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            result = page.enhetsPostnummer.value()
        }
        result
    }

    String postort() {
        def result
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            result = page.enhetsPostort.value()
        }
        result
    }

    String telefonnummer() {
        def result
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            result = page.enhetsTelefonnummer.value()
        }
        result
    }

    String epost() {
        def result
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            result = page.enhetsEpost.value()
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
        def result = false;
        Browser.drive {
            waitFor {
                result = page."$felt".text().contains(text)
                //result = page.vardenhet.postadress.text().contains(text);
            }
        }
        return result;
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
