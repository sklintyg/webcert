package se.inera.webcert.pages.fk7263

import geb.Page

class VisaFk7263Page extends Page {

    static at = { $("#viewCertAndQA").isDisplayed() }

    static content = {

        intygSaknas { $("#cert-load-error") }
        intygLaddat { $('#intyg-vy-laddad') }

        // smittskydd
        field1yes { $("#field1yes") }
        field1no { $("#field1no") }

        // diagnos
        field2 { $("#field2") }
        diagnosKod { $("#diagnosKod") }
        diagnosBeskrivning { $("#diagnosBeskrivning") }
        diagnosKod2 { $("#diagnosKod2") }
        diagnosBeskrivning2 { $("#diagnosBeskrivning2") }
        diagnosKod3 { $("#diagnosKod3") }
        diagnosBeskrivning3 { $("#diagnosBeskrivning3") }
        samsjuklighet { $("#samsjuklighet") }

        //field3 { $("#field3") }

        field4 { $("#field4") }
        field4b { $("#field4b") }
        field5 { $("#field5") }
        field6a { $("#field6a") }
        field6b { $("#field6b") }
        field7 { $("#field7") }
        field8a { $("#field8") }
        field8b { $("#field8b") }
        field9 { $("#field9") }
        field10 { $("#field10") }
        field11 { $("#field11") }
        field12 { $("#field12") }
        field13 { $("#field13") }
        field17 { $("#field17") }
        field_vardperson_namn { $("#vardperson_namn") }
        field_vardperson_enhetsnamn { $("#vardperson_enhetsnamn") }
    }
}
