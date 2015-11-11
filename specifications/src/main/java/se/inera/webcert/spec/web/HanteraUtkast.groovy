package se.inera.webcert.spec.web
import se.inera.certificate.spec.Browser
import se.inera.webcert.pages.AbstractEditCertPage
import se.inera.webcert.pages.SokSkrivaIntygPage
import se.inera.webcert.pages.UnsignedIntygPage
import se.inera.webcert.pages.VisaFragaSvarPage
import se.inera.webcert.pages.fk7263.EditeraFk7263Page
import se.inera.webcert.pages.fk7263.VisaFk7263Page
import se.inera.webcert.pages.ts_bas.EditeraTsBasPage
import se.inera.webcert.pages.ts_bas.VisaTsBasPage
import se.inera.webcert.pages.ts_diabetes.EditeraTsDiabetesPage
import se.inera.webcert.pages.ts_diabetes.VisaTsDiabetesPage
import se.inera.webcert.spec.util.WebcertRestUtils

class HanteraUtkast {

    void gaTillEditeraIntygMedTypOchIntygid(String typ, String intygid) {
        Browser.drive {
            go "/web/dashboard#/$typ/edit/$intygid"
            if (typ == "fk7263") {
                waitFor {
                    at EditeraFk7263Page
                }
            } else if (typ == "ts-bas") {
                waitFor {
                    at EditeraTsBasPage
                }
            } else if (typ == "ts-diabetes") {
                waitFor {
                    at EditeraTsDiabetesPage
                }
            }
        }
    }

    // ------- navigation

    void gaTillEditIntygMedIntygsid(String id) {
        Browser.drive {
            go "/web/dashboard#/fk7263/edit/${id}"
            waitFor {
                at EditeraFk7263Page
            }
        }
    }

    void gaTillEjSigneradeIntyg() {
        Browser.drive {
            to UnsignedIntygPage
            waitFor {
                at UnsignedIntygPage
            }
        }
    }

    def provaGaTillEditIntygMedIntygsid(String id) {
        Browser.drive {
            go "/web/dashboard#/fk7263/edit/${id}"
        }
    }

    // ------- behaviour

    void raderaUtkast() {
        Browser.drive {
            page.tabortUtkast()
            page.konfirmeraTabortUtkast()
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

    void klickaPaTillbakaKnappen() {
        Browser.drive {
            page.tillbakaBtn.click();
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

    void visaAvanceratFilter() {
        Browser.drive {
            waitFor {
                at UnsignedIntygPage
            }
            page.showAdvancedFilter()
            waitFor {
                page.advancedFilterForm.isDisplayed()
            }
        }
    }

    // ------- state

    // ---------- pages

    boolean visaSidanVisas() {
        boolean result
        Browser.drive {
            waitFor {
                result = isAt VisaFragaSvarPage
            }
        }
        result
    }

    boolean sokSkrivIntygSidanVisas() {
        boolean result
        Browser.drive {
            waitFor {
                result = isAt SokSkrivaIntygPage
            }
        }
        result
    }

    boolean editeraSidanVisas() {
        boolean result
        Browser.drive {
            result = isAt AbstractEditCertPage
        }
    }

    boolean editeraFk7263SidanVisas() {
        boolean result
        Browser.drive {
            waitFor {
                result = isAt EditeraFk7263Page
            }
        }
        result
    }

    boolean visaIntygSidanVisas() {
        boolean result
        Browser.drive {
            waitFor {
                isAt VisaFk7263Page
                result = page.intygLaddat.isDisplayed()
            }

        }
        return result;
    }

    boolean visaIntygSidanVisasMedTyp(intygsTyp) {
        boolean result
        Browser.drive {
            waitFor {
                if (intygsTyp == "fk7263")
                    result = isAt VisaFk7263Page
                else if (intygsTyp == "ts-bas")
                    result = isAt VisaTsBasPage
                else if (intygsTyp == "ts-diabetes")
                    result = isAt VisaTsDiabetesPage
                result = result && page.intygLaddat.isDisplayed()
            }
        }
        result
    }

    boolean ejSigneradeIntygSidanVisas() {
        boolean result
        Browser.drive {
            waitFor {
                result = isAt UnsignedIntygPage
            }
        }
        result
    }

    // ---------- elements

    boolean ingaEjSigneradeIntygVisas() {
        return WebcertRestUtils.getNumberOfUnsignedCertificates() == 0
    }

    boolean ejSigneradeIntygVisas() {
        return WebcertRestUtils.getNumberOfUnsignedCertificates() > 0
    }

    boolean vidareBefordraKnappVisas() {
        def result
        Browser.drive {
            waitFor {
                at UnsignedIntygPage
            }
            result = $("#unsignedCertTable button.vidarebefordra-btn")?.isDisplayed()

        }
        result
    }

    boolean vidarebefordradCheckboxVisas() {
        def result
        Browser.drive {
            waitFor {
                at UnsignedIntygPage
            }
            result = $("#unsignedCertTable input.vidarebefordrad-checkbox")?.isDisplayed();
        }
        result
    }

    boolean filterVidarebefordradVisas() {
        boolean result = false
        Browser.drive {
            isAt UnsignedIntygPage
            result = page.filterVidarebefordrad?.isDisplayed()
        }
        result
    }

    boolean filterSparatAvVisas() {
        boolean result = false
        Browser.drive {
            isAt UnsignedIntygPage
            result = page.filterSparatAv?.isDisplayed()
        }
        result
    }

    boolean filterSigneratAvVisas() {
        boolean result = false
        Browser.drive {
            isAt UnsignedIntygPage
            result = page.filterSigneratAv?.isDisplayed()
        }
        result
    }

    boolean signeraKnappAktiverad() {
        boolean result
        Browser.drive {
            result = page.signeraBtn?.isEnabled()
        }
        result
    }

    boolean signeraKnappVisas() {
        boolean result
        Browser.drive {
            result = page.signeraBtn?.isDisplayed()
        }
        result
    }

    boolean signeringKraverLakareVisas() {
        boolean result
        Browser.drive {
            result = page.signRequiresDoctorMessage?.isDisplayed()
        }
        result
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
            result = page.intygetSparatOchEjKomplettMeddelande?.isDisplayed()
        }
        result
    }

    boolean meddelandeIntygetSigneratVisas() {
        boolean result
        Browser.drive {
            result = page.certificateIsSentToITMessage?.isDisplayed()
        }
        result
    }

    boolean felmeddelandeVisas() {
        boolean result
        Browser.drive {
            result = page.errorPanel?.isDisplayed()
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

    boolean utkastAvTypVisasInteIListanAvTidigareUtkast(String intygsTyp) {
        //
        boolean result = false
        Browser.drive {
            waitFor {
                at UnsignedIntygPage
            }
            waitFor {
                // TODO use better page abstraction
                result = $("#unsignedCertTable table tr td span[key=\"certificatetypes\\.$intygsTyp\\.typename\"]").size() == 0
            }
        }
        return result
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
            page.vardenhet.postadress = value
        }
    }

    void andraPostnummer(String value) {
        Browser.drive {
            page.vardenhet.postnummer = value
        }
    }

    void andraPostort(String value) {
        Browser.drive {
            page.vardenhet.postort = value
        }
    }

    void andraTelefonnummer(String value) {
        Browser.drive {
            page.vardenhet.telefonnummer = value
        }
    }

    void andraEpost(String value) {
        Browser.drive {
            page.vardenhet.epost = value
        }
    }

    // -------- form view

    String postadress() {
        def result
        Browser.drive {
            result = page.vardenhet.postadress.value()
        }
        result
    }

    String postnummer() {
        def result
        Browser.drive {
            result = page.vardenhet.postnummer.value()
        }
        result
    }

    String postort() {
        def result
        Browser.drive {
            result = page.vardenhet.postort.value()
        }
        result
    }

    String telefonnummer() {
        def result
        Browser.drive {
            result = page.vardenhet.telefonnummer.value()
        }
        result
    }

    String epost() {
        def result
        Browser.drive {
            result = page.vardenhet.epost.value()
        }
        result
    }

    boolean kommentarInnehallerText(String text) {
        def result = false;
        Browser.drive {
            result = page.kommentar.text().contains(text);
        }
        return result;
    }

    boolean faltMedNamnInnehallerText(String falt, String text) {
        boolean result
        Browser.drive {
            waitFor {
                result = page."$falt".text().contains(text)
                //result = page.vardenhet.postadress.text().contains(text);
            }
        }
        return result
    }

    boolean sekretessmarkeringVisas() {
        def result
        Browser.drive {
            at EditeraFk7263Page
            result = page.sekretessmarkering.isDisplayed()
        }
        result
    }
}
