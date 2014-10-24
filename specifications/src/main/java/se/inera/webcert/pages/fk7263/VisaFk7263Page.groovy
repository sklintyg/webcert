package se.inera.webcert.pages.fk7263

import geb.Page

class VisaFk7263Page extends Page {

    static at = { $("#viewCertAndQA").isDisplayed() }

    static content = {

        intygSaknas { $("#cert-load-error") }
        intygLaddat { $('#intyg-vy-laddad') }

        copyButton { $("#copyBtn") }
        makuleraButton { $("#makuleraBtn") }
        kopieraDialogKopieraKnapp { $("#button1copy-dialog") }
        makuleraDialogKopieraKnapp { $("#button1makulera-dialog") }
        makuleraConfirmationOkButton { $("#confirmationOkButton") }
        skickaDialogCheck { $("#patientSamtycke") }
        skickaDialogSkickaKnapp { $("#button1send-dialog") }

        certificateIsSentToITMessage(required: false) { $("#certificate-is-sent-to-it-message-text") }
        certificateIsSentToRecipientMessage(required: false) { $("#certificate-is-sent-to-recipient-message-text") }
        certificateIsRevokedMessage(required: false) { $("#certificate-is-revoked-message-text") }

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

        // 4
        funktionsnedsattning { $("#funktionsnedsattning") }
        undersokningAvPatienten { $("#undersokningAvPatienten") }
        telefonkontaktMedPatienten { $("#telefonkontaktMedPatienten") }
        journaluppgifter { $("#journaluppgifter") }
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
        sleep(300)
        kopieraDialogKopieraKnapp.click()
    }

    def makulera() {
        $("#makuleraBtn").click()
        sleep(300)
        makuleraDialogKopieraKnapp.click()
    }

    def send() {
        $("#sendBtn").click()
        sleep(1000)
        skickaDialogCheck.click()
        sleep(100)
        skickaDialogSkickaKnapp.click()
    }

}
