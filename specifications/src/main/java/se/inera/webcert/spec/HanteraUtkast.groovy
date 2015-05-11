package se.inera.webcert.spec

import se.inera.certificate.page.AbstractPage
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

    def gaTillEjSigneradeIntyg() {
        Browser.drive {
            go "/web/dashboard#/unsigned"
            waitFor {
                at UnsignedIntygPage
            }
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

    boolean ejSigneradeIntygSidanVisas() {
        Browser.drive {
            waitFor {
                at UnsignedIntygPage
            }
        }
    }

    boolean ingaEjSigneradeIntygVisas() {
        return WebcertRestUtils.getNumberOfUnsignedCertificates() == 0
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
            }

            waitFor {
                page.intygLaddat.isDisplayed()
            }
        }
    }

    boolean visaIntygSidanVisasMedTyp(intygsTyp) {
        Browser.drive {
            waitFor {
                if(intygsTyp == "fk7263")
                    at VisaFk7263Page
                else if(intygsTyp == "ts-bas")
                    at VisaTsBasPage
                else if(intygsTyp == "ts-diabetes")
                    at VisaTsDiabetesPage
            }

            waitFor {
                page.intygLaddat.isDisplayed()
            }
        }
    }

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

    boolean signeraKnappAktiverad(boolean expected = true) {
        Browser.drive {
            at EditeraIntygPage
            waitFor {
                expected == page.signeraBtn.isEnabled()
            }
        }
        true
    }

    boolean signeraKnappEjAktiverad(boolean expected = true) {
        Browser.drive {
            at EditeraIntygPage
            waitFor {
                expected != page.signeraBtn.isEnabled()
            }
        }
        true
    }

    boolean signeraKnappVisas(boolean expected = true) {
        Browser.drive {
            at EditeraIntygPage
            waitFor {
                expected == page.signeraBtn.isDisplayed()
            }
        }
        true
    }

    boolean signeraUtkast() {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            page.signeraBtn.click()
        }
    }

    boolean signeringKraverLakareVisas(boolean expected = true) {
        Browser.drive {
            asType EditeraIntygPage
            waitFor {
                expected == page.signRequiresDoctorMessage.isDisplayed()
            }
        }
        true
    }

    boolean intygetSigneratMeddelandeVisas(boolean expected = true) {
        Browser.drive {
            asType EditeraIntygPage
            waitFor {
                expected == page.certificateIsSentToITMessage.isDisplayed()
            }
        }
        true
    }

    boolean intygetEjKomplettMeddelandeVisas(boolean expected = true) {
        Browser.drive {
            asType EditeraIntygPage
            waitFor {
                expected == page.intygetEjKomplettMeddelande.isDisplayed()
            }
        }
        true
    }

    boolean intygetKomplettMeddelandeVisas(boolean expected = true) {
        Browser.drive {
            asType EditeraIntygPage
            waitFor {
                expected == page.intygetSparatMeddelande.isDisplayed()
            }
        }
        true
    }
    
    boolean felmeddelandeVisas(boolean expected = true) {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
                expected == page.errorPanel.isDisplayed()
            }
        }
        true
    }

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

    String postadress() {
        def result
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            result = page.enhetsPostadress.value()
        }
        sleep(300)
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
        sleep(300)
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
        sleep(300)
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
        sleep(300)
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
        sleep(300)
        result
    }

    boolean andraPostadress(String value) {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            page.enhetsPostadress = value
        }
        sleep(300)
        true
    }

    boolean andraPostnummer(String value) {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            page.enhetsPostnummer = value
        }
        sleep(300)
        true
    }

    boolean andraPostort(String value) {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            page.enhetsPostort = value
        }
        sleep(300)
        true
    }

    boolean andraTelefonnummer(String value) {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            page.enhetsTelefonnummer = value
        }
        sleep(300)
        true
    }

    boolean andraEpost(String value) {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            page.enhetsEpost = value
        }
        sleep(300)
        true
    }

    boolean sparaUtkast() {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            println("skrivUtBtn disabled : " + AbstractPage.isButtonDisabled(page.skrivUtBtn));
            if(!AbstractPage.isButtonDisabled(page.sparaBtn)){
                page.sparaBtn.click()
            } else {
                // utkast är redan sparat genom autospar
            }
        }
    }

    boolean wait4it() {
        Thread.sleep(5000)
        true
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

    boolean kommentarInnehallarText( String text){
        def result = false;
        Browser.drive {
            result = page.kommentar.text().contains(text);
        }
        return result;
    }

    boolean feltMedNamnInnehallarText( String felt, String text ){
        def result = false;
        Browser.drive {
            waitFor {
                result = page."$felt".text().contains(text)

                //result = page.vardenhet.postadress.text().contains(text);
            }
        }
        return result;
    }

    def klickaPaTillbakaKnappen() {
        Browser.drive {
            page.tillbakaButton.click();
        }
    }

    boolean skrivUtKnappVisas(boolean expected = true) {
        Browser.drive {
            at EditeraIntygPage
            waitFor {
                expected == page.skrivUtBtn.isDisplayed()
            }
        }
        true
    }
}
