package se.inera.webcert.pages.fk7263

import se.inera.webcert.pages.VisaPage

class VisaFk7263Page extends VisaPage {

    static content = {

        // messages
        intygSaknas { $("#cert-load-error") }
        intygLaddat { $('#intyg-vy-laddad') }
        intygFel { $("#cert-inline-error") }
        certificateIsSentToITMessage(required: false) { $("#certificate-is-sent-to-it-message-text") }
        certificateIsSentToRecipientMessage(required: false) { $("#certificate-is-sent-to-recipient-message-text") }
        certificateIsRevokedMessage(required: false) { $("#certificate-is-revoked-message-text") }
        visaVadSomSaknasLista(required: false) { $("#visa-vad-som-saknas-lista") }

        // copy dialog
        annanEnhetText { $("#annanVardenhet")}

        // header
        webcertLogoLink(required: false) { $("#webcertLogoLink") }
        bytVardenhetLink(required: false) { $("#wc-care-unit-clinic-selector") }
        loggaUtLink(required: false) { $("#logoutLink") }
        omWebcertLink { $("#aboutLink") }
        omWebcertDialog(required: false) { $("#omWebcertDialog") }
        huvudmeny(required: false) { $("#huvudmeny")}

        // intyg top panel
        tillbakaButton(required: false) { $("#tillbakaButton")}
        copyButton { $("#copyBtn") }
        makuleraButton(required: false) { $("#makuleraBtn") }
        kopieraDialogKopieraKnapp { $("#button1copy-dialog") }
        kopieraDialogAvbrytKnapp { $("#button2copy-dialog") }
        kopieraDialogVisaInteIgen { $("#dontShowAgain") }
        makuleraDialogKopieraKnapp { $("#button1makulera-dialog") }
        makuleraConfirmationOkButton { $("#confirmationOkButton") }

        skickaDialogBody { $("span[key=\"fk7263.label.send.body\"]") }

        // kopiera dialog text webcert-1449
        copyDialog(required:false, toWait: true){ $("#copy-dialog") }
        kopieraDialogMsgInteFranJournalSystem(required:false){ $("#msgInteFranJournalSystem") }
        kopieraDialogMsgNyttPersonId(required:false){ $("#msgNyttPersonId") }

        // fraga svar
        vidarebefordraEjHanterad(required: false) { $("#vidarebefordraEjHanterad") }
        nyFragaSvarKnapp { $("#askQuestionBtn") }
        nyFragaFrageText { $("#newQuestionText") }
        nyFragaFrageAmne { $("#new-question-topic") }
        nyFragaSkickaFragaKnapp { $("#sendQuestionBtn") }
        nyFragaSkickadTextruta { $("#question-is-sent-to-fk-message-text") }

        // INTYG
        nyttPersonnummer { $("#nyttPersonnummer") }
        namnOchPersonnummer { $("#patientNamnPersonnummer") }

        // smittskydd
        smittskydd { $("#smittskydd") }

        // diagnos
        diagnosKod { $("#diagnosKod") }
        diagnosBeskrivning { $("#diagnosBeskrivning") }
        diagnosKod2 { $("#diagnosKod2") }
        diagnosBeskrivning2 { $("#diagnosBeskrivning2") }
        diagnosKod3 { $("#diagnosKod3") }
        diagnosBeskrivning3 { $("#diagnosBeskrivning3") }
        samsjuklighet { $("#samsjuklighet") }

        // 3
        sjukdomsforlopp { $("#sjukdomsforlopp") }

        // 4a
        funktionsnedsattning { $("#funktionsnedsattning") }

        // 4b
        baseratPaList { $("#baseratPaList") }
        undersokningAvPatienten { $("#undersokningAvPatienten") }
        telefonkontaktMedPatienten { $("#telefonkontaktMedPatienten") }
        journaluppgifter { $("#journaluppgifter") }
        annanReferens { $("#annanReferens") }
        annanReferensBeskrivning { $("#annanReferensBeskrivning") }

        // 5
        aktivitetsbegransning { $("#aktivitetsbegransning") }

        // 6
        rekommendationKontaktArbetsformedlingen { $("#rekommendationKontaktArbetsformedlingen") }
        rekommendationKontaktForetagshalsovarden { $("#rekommendationKontaktForetagshalsovarden") }
        rekommendationOvrigt { $("#rekommendationOvrigt") }
        atgardInomSjukvarden { $("#atgardInomSjukvarden") }
        annanAtgard { $("#annanAtgard") }

        // 7
        rehabiliteringAktuell { $("#rehabiliteringAktuell") }
        rehabiliteringEjAktuell { $("#rehabiliteringEjAktuell") }
        rehabiliteringGarInteAttBedoma { $("#rehabiliteringGarInteAttBedoma") }

        // 8
        nuvarandeArbetsuppgifter { $("#nuvarandeArbetsuppgifter") }
        arbetsloshet { $("#arbetsloshet") }
        foraldrarledighet { $("#foraldrarledighet") }
        nedsattMed25from { $("#nedsattMed25from") }
        nedsattMed25tom { $("#nedsattMed25tom") }
        nedsattMed25Beskrivning { $("#nedsattMed25Beskrivning") }
        nedsattMed50from { $("#nedsattMed50from") }
        nedsattMed50tom { $("#nedsattMed50tom") }
        nedsattMed50Beskrivning { $("#nedsattMed50Beskrivning") }
        nedsattMed75from { $("#nedsattMed75from") }
        nedsattMed75tom { $("#nedsattMed75tom") }
        nedsattMed75Beskrivning { $("#nedsattMed75Beskrivning") }
        nedsattMed100from { $("#nedsattMed100from") }
        nedsattMed100tom { $("#nedsattMed100tom") }

        // 9
        arbetsformagaPrognos { $("#arbetsformagaPrognos") }

        // 10
        arbetsformagaPrognosJa { $("#arbetsformataPrognosJa") }
        arbetsformagaPrognosJaDelvis { $("#arbetsformataPrognosJaDelvis") }
        arbetsformagaPrognosNej { $("#arbetsformataPrognosNej") }
        arbetsformagaPrognosGarInteAttBedoma { $("#arbetsformataPrognosGarInteAttBedoma") }
        arbetsformagaPrognosGarInteAttBedomaBeskrivning { $("#arbetsformagaPrognosGarInteAttBedomaBeskrivning") }

        // 11
        ressattTillArbeteAktuellt { $("#ressattTillArbeteAktuellt")}
        ressattTillArbeteEjAktuellt { $("#ressattTillArbeteEjAktuellt")}

        // 12
        kontaktMedFk { $("#kontaktMedFk") }

        // 13
        kommentar { $("#kommentar") }

        // 17
        forskrivarkodOchArbetsplatskod { $("#forskrivarkodOchArbetsplatskod") }

        // 14
        signeringsdatum { $("#signeringsdatum") }

        // 15,16
        vardperson_namn { $("#vardperson_namn") }
        vardperson_enhetsnamn { $("#vardperson_enhetsnamn") }
        vardperson_postadress { $("#vardperson_postadress") }
        vardperson_postnummer { $("#vardperson_postnummer") }
        vardperson_postort { $("#vardperson_postort") }
        vardperson_telefonnummer { $("#vardperson_telefonnummer") }
    }

    def copy() {
        $("#copyBtn").click()
        waitFor {
            doneLoading()
        }
        kopieraDialogKopieraKnapp.click()
    }

    def openCopyDialog() {
        $("#copyBtn").click()
        waitFor {
            doneLoading()
        }
    }

    def closeCopyDialog() {
        $("#button2copy-dialog").click()
        waitFor {
            doneLoading()
        }
    }

    def makulera() {
        $("#makuleraBtn").click()
        waitFor {
            doneLoading()
        }
        makuleraDialogKopieraKnapp.click()
    }

    boolean exists(content) {
        content
    }

    def kanInteMakulera() {
        waitFor {
            !exists(makuleraButton)
        }
    }

    def sendWithValidation() {
        skickaKnapp.click()
        waitFor {
            doneLoading()
            skickaDialogBody.text().contains("Försäkringskassan.")
        }
        skickaDialogCheck.click()
        waitFor {
            doneLoading()
        }
        skickaDialogSkickaKnapp.click()
    }

    def stallNyFragaTillForsakringskassan() {
        nyFragaSvarKnapp.click()
        waitFor {
            doneLoading()
        }
    }

    boolean nyFragaTillForsakringskassanFormularVisas(boolean expected = true) {
        waitFor {
            expected == nyFragaFrageText.isDisplayed()
        }
        true
    }

    def fillNyFragaFormular() {
        waitFor {
            nyFragaTillForsakringskassanFormularVisas(true)
        }
        nyFragaFrageText.value("Kan vi boka in ett möte med alla inblandade 15/5 15:00 på FK kontor?")
        nyFragaFrageAmne.value("2")
        nyFragaSkickaFragaKnapp.click()

        waitFor {
            nyFragaSkickadTextruta.isDisplayed()
        }
    }

    boolean nyFragaSkickadTextVisas(boolean expected = true) {
        waitFor {
            expected == nyFragaSkickadTextruta.isDisplayed()
        }
        true
    }
}
