package se.inera.webcert.pages.ts_diabetes

import se.inera.webcert.pages.VisaPage

class VisaTsDiabetesPage extends VisaPage {

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

        patientpostadress { $("#patientpostadress") }
        patientpostnummer { $("#patientpostnummer") }
        patientpostort { $("#patientpostort") }
        intygAvser { $("#intygAvser") }
        identitet { $("#identitet ") }
        observationsperiod { $("#observationsperiod") }
        diabetestyp { $("#diabetestyp") }
        endastKost { $("#endastKost") }
        tabletter { $("#tabletter") }
        insulin { $("#insulin") }
        insulinBehandlingsperiod { $("#insulinBehandlingsperiod") }
        annanBehandlingBeskrivning { $("#annanBehandlingBeskrivning") }
        teckenNedsattHjarnfunktion { $("#teckenNedsattHjarnfunktion") }
        saknarFormagaKannaVarningstecken { $("#saknarFormagaKannaVarningstecken") }
        allvarligForekomst { $("#allvarligForekomst") }
        allvarligForekomstBeskrivning { $("#allvarligForekomstBeskrivning") }
        allvarligForekomstTrafiken { $("#allvarligForekomstTrafiken") }
        allvarligForekomstTrafikBeskrivning { $("#allvarligForekomstTrafikBeskrivning") }
        egenkontrollBlodsocker { $("#egenkontrollBlodsocker") }
        allvarligForekomstVakenTid { $("#allvarligForekomstVakenTid") }
        allvarligForekomstVakenTidObservationstid { $("#allvarligForekomstVakenTidObservationstid") }
        separatOgonlakarintyg { $("#separatOgonlakarintyg") }
        synfaltsprovningUtanAnmarkning { $("#synfaltsprovningUtanAnmarkning") }
        hogerutanKorrektion { $("#hogerutanKorrektion") }
        hogermedKorrektion { $("#hogermedKorrektion") }
        vansterutanKorrektion { $("#vansterutanKorrektion") }
        vanstermedKorrektion { $("#vanstermedKorrektion") }
        binokulartutanKorrektion { $("#binokulartutanKorrektion") }
        binokulartmedKorrektion { $("#binokulartmedKorrektion") }
        diplopi { $("#diplopi") }
        lamplighetInnehaBehorighet { $("#lamplighetInnehaBehorighet") }
        bedomningKanInteTaStallning { $("#bedomningKanInteTaStallning") }
        kommentar { $("#kommentar") }
        kommentarEjAngivet { $("#kommentarEjAngivet") }
        bedomning { $("#bedomning") }
        lakareSpecialKompetens { $("#lakareSpecialKompetens") }
        signeringsdatum { $("#signeringsdatum") }
        vardperson_namn { $("#vardperson_namn") }
        vardperson_enhetsnamn { $("#vardperson_enhetsnamn") }
        vardenhet_postadress { $("#vardenhet_postadress") }
        vardenhet_postnummer { $("#vardenhet_postnummer") }
        vardenhet_postort { $("#vardenhet_postort") }
        vardenhet_telefonnummer { $("#vardenhet_telefonnummer") }
    }

    def copy() {
        $("#copyBtn").click()
        waitFor {
            doneLoading()
        }
        kopieraDialogKopieraKnapp.click()
    }

    def makulera() {
        $("#makuleraBtn").click()
        waitFor {
            doneLoading()
        }
        makuleraDialogKopieraKnapp.click()
    }

    def send() {
        $("#sendBtn").click()
        waitFor {
            doneLoading()
        }
        skickaDialogCheck.click()
        waitFor {
            doneLoading()
        }
        skickaDialogSkickaKnapp.click()
    }
}
