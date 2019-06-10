// Intyget är uppdelat på samma sätt som det är uppdelat när det fylls i genom WebCert

// Datumen är inte specifika för något testfall

// -------------------- 'Intyget avser' --------------------
export function sektionIntygetAvser(intygetAvser) {

    if (intygetAvser) {
        // TODO: Lägger väldigt lång timeout vid första elementet i intyget eftersom
        // sidan ibland inte har hunnit ladda innan den får timeout.
        // Initial analys är att Jenkins är överbelastad då det verkar fungera bra när
        // man kör lokalt

        var körkortsTyper = {
            "AM": '#intygAvser\\.behorigheter-IAV11',
            "A1": '#intygAvser\\.behorigheter-IAV12',
            "A2": '#intygAvser\\.behorigheter-IAV13',
            "A": '#intygAvser\\.behorigheter-IAV14',
            "B": '#intygAvser\\.behorigheter-IAV15',
            "BE": '#intygAvser\\.behorigheter-IAV16',
            "Traktor": '#intygAvser\\.behorigheter-IAV17',
            "C1": '#intygAvser\\.behorigheter-IAV1',
            "C1E": '#intygAvser\\.behorigheter-IAV2',
            "C": '#intygAvser\\.behorigheter-IAV3',
            "CE": '#intygAvser\\.behorigheter-IAV4',
            "D1": '#intygAvser\\.behorigheter-IAV5',
            "D1E": '#intygAvser\\.behorigheter-IAV6',
            "D": '#intygAvser\\.behorigheter-IAV7',
            "DE": '#intygAvser\\.behorigheter-IAV8',
            "Taxi": '#intygAvser\\.behorigheter-IAV9'
        };

        for (var key in intygetAvser) {
            if (intygetAvser[key]) {
                cy.get(körkortsTyper[key], {timeout: 60000}).check();
            }
        }
    }
}

// -------------------- 'Identitet' --------------------
export function sektionIdentitet(identitet) {
    if (identitet) {

        var indentifieringsTyper = {
            "IDkort": '#idKontroll\\.typ-ID_KORT',
            "FöretagskortTjänstekort": '#idKontroll\\.typ-FORETAG_ELLER_TJANSTEKORT',
            "Körkort": '#idKontroll\\.typ-KORKORT',
            "PersonligKännedom": '#idKontroll\\.typ-PERS_KANNEDOM',
            "Försäkran": '#idKontroll\\.typ-FORSAKRAN_KAP18',
            "Pass": '#idKontroll\\.typ-PASS'
        }

        for (var key in identitet) {
            if (identitet[key]) {
                cy.get(indentifieringsTyper[key]).check();
            }
        }
    }
}

// -------------------- 'Allmänt' --------------------
export function sektionAllmänt(Allmänt) {

    var diagnosRader = {
        "rad1": {
            "kod": "#diagnoseCode-0",
            "diagnos": "#diagnoseDescription-0",
            "årtal": "#diagnosKodad-0--diagnosArtal"
        },
        "rad2": {
            "kod": "#diagnoseCode-1",
            "diagnos": "#diagnoseDescription-1",
            "årtal": "#diagnosKodad-1--diagnosArtal"
        },
        "rad3": {
            "kod": "#diagnoseCode-2",
            "diagnos": "#diagnoseDescription-2",
            "årtal": "#diagnosKodad-2--diagnosArtal"
        },
        "rad4": {
            "kod": "#diagnoseCode-3",
            "diagnos": "#diagnoseDescription-3",
            "årtal": "#diagnosKodad-3--diagnosArtal"
        }
    }

    if (Allmänt.icd10) {
        cy.get('#diagnosRegistrering\\.typ-DIAGNOS_KODAD').check();
        var avsluta = 0
        for (var key in Allmänt.diagnos) {
            cy.get(diagnosRader[key].kod).type(Allmänt.diagnos[key].kod);
            //Todo: Misslyckas här ibland. Lade till en wait på 1 sekund innan enter skickas. Titta om det går att lösas utan wait.
            cy.wait(1000);
            cy.get(diagnosRader[key].kod).type('{enter}');

            cy.get(diagnosRader[key].diagnos).invoke('val').should('contain', Allmänt.diagnos[key].text);
            const iÅrMinus2År  = Cypress.moment().subtract(2,  'years').format('YYYY');
            cy.get(diagnosRader[key].årtal).type(iÅrMinus2År);
            avsluta++
            if (avsluta === Allmänt.antalDiagnoser || avsluta > 4) {
                break;
            }
        }
    } else {
        cy.get('#diagnosRegistrering\\.typ-DIAGNOS_FRITEXT').check();
        cy.get('#diagnosFritext-diagnosFritext').type(Allmänt.fritext);
        const iÅrMinus2År  = Cypress.moment().subtract(2,  'years').format('YYYY');
        cy.get('#diagnosFritext-diagnosArtal').type(iÅrMinus2År);
    }

}

// -------------------- 'Läkemedelsbehandling' --------------------
export function sektionLäkemedelsBehandling(läkemedelsbehandling) {

    if (läkemedelsbehandling.harHaft) {
        cy.get('#lakemedelsbehandling-harHaftYes').check();
        if (läkemedelsbehandling.pågår) {
            cy.get('#lakemedelsbehandling-pagarYes').check();
            cy.get('#lakemedelsbehandling-aktuell').type(läkemedelsbehandling.aktuell);

            if (läkemedelsbehandling.treÅr) {
                cy.get('#lakemedelsbehandling-pagattYes').check();
            } else {
                cy.get('#lakemedelsbehandling-pagattNo').check();
            }

            if (läkemedelsbehandling.behandlingseffekt) {
                cy.get('#lakemedelsbehandling-effektYes').check();
            } else {
                cy.get('#lakemedelsbehandling-effektNo').check();
            }

            if (läkemedelsbehandling.följsamhet) {
                cy.get('#lakemedelsbehandling-foljsamhetYes').check();
            } else {
                cy.get('#lakemedelsbehandling-foljsamhetNo').check();
            }
        } else {
            cy.get('#lakemedelsbehandling-pagarNo').check();
            cy.get('#lakemedelsbehandling-avslutadTidpunkt').type(läkemedelsbehandling.tidpunktAvslut);
            cy.get('#lakemedelsbehandling-avslutadOrsak').type(läkemedelsbehandling.orsakAvslut);
        }
    } else {
        cy.get('#lakemedelsbehandling-harHaftNo').check();
    }
}

// -------------------- 'Symptom, funktionshinder och prognos' --------------------
export function sektionSymptomFunktionshinderPrognos(symptom) {

    cy.get('#bedomningAvSymptom').type(symptom.bedömning);

    if (symptom.prognos && !symptom.kanEjBedöma) {
        cy.get('#prognosTillstand\\.typ-JA').check();
    } else if (!symptom.prognos && !symptom.kanEjBedöma) {
        cy.get('#prognosTillstand\\.typ-NEJ').check();
    } else {
        cy.get('#prognosTillstand\\.typ-KANEJBEDOMA').check();
    }

}

// -------------------'Övrigt' -----------------------
export function sektionÖvrigt(övrigt) {
    if (övrigt.ja) {
        cy.get('#ovrigaKommentarer').type(övrigt.text);
    }
}

// -------------------'Bedömning' -----------------------
export function sektionBedömning(bedömning) {

    var bedömingAlternativ = {
        "AM": '#bedomning\\.uppfyllerBehorighetskrav-VAR12',
        "A1": '#bedomning\\.uppfyllerBehorighetskrav-VAR13',
        "A2": '#bedomning\\.uppfyllerBehorighetskrav-VAR14',
        "A": '#bedomning\\.uppfyllerBehorighetskrav-VAR15',
        "B": '#bedomning\\.uppfyllerBehorighetskrav-VAR16',
        "BE": '#bedomning\\.uppfyllerBehorighetskrav-VAR17',
        "Traktor": '#bedomning\\.uppfyllerBehorighetskrav-VAR18',
        "C1": '#bedomning\\.uppfyllerBehorighetskrav-VAR1',
        "C1E": '#bedomning\\.uppfyllerBehorighetskrav-VAR2',
        "C": '#bedomning\\.uppfyllerBehorighetskrav-VAR3',
        "CE": '#bedomning\\.uppfyllerBehorighetskrav-VAR4',
        "D1": '#bedomning\\.uppfyllerBehorighetskrav-VAR5',
        "D1E": '#bedomning\\.uppfyllerBehorighetskrav-VAR6',
        "D": '#bedomning\\.uppfyllerBehorighetskrav-VAR7',
        "DE": '#bedomning\\.uppfyllerBehorighetskrav-VAR8',
        "Taxi": '#bedomning\\.uppfyllerBehorighetskrav-VAR9'
    };

    if (bedömning.kanInteTaStällning) {
        cy.get('#bedomning\\.uppfyllerBehorighetskrav-VAR11').check();
    } else {
        for (var key in bedömning.kanTaStällning) {
            if (bedömning.kanTaStällning[key]) {
                cy.get(bedömingAlternativ[key]).check();
            }
        }
    }
}

// -------------------- 'Vårdenhetens adress' --------------------
// Ej implementerad

// -------------------- 'Signera intyget' --------------------
export function signera() {
    // TODO: Utan wait så tappas ofta slutet på texten bort i sista textboxen.
    // Antagligen hinner WebCert inte auto-spara innan man trycker på "signera".
    // Wait är dock ett anti-pattern så finns något annat sätt så är det att föredra.
    cy.wait(1000);

    cy.contains("Klart att signera");
    cy.contains("Obligatoriska uppgifter saknas").should('not.exist');
    cy.contains("Utkastet sparas").should('not.exist');

    // cy.click() fungerar inte alltid. Det finns ärenden rapporterade
    // (stängd pga inaktivitet):
    // https://github.com/cypress-io/cypress/issues/2551
    // https://www.cypress.io/blog/2019/01/22/when-can-the-test-click/ :
    // "If a tree falls in the forest and no one has attached a “fall” event listener, did it really fall?"

    const click = $el => { return $el.click() }

    // Parent() p.g.a. att ett element täcker knappen
    cy.get('#signera-utkast-button').parent().should('be.visible')

    cy.get('#signera-utkast-button')
    .pipe(click, {timeout: 60000}) // ToDo: Lång timeout (problem endast på Jenkins, överlastad slav?)
    .should($el => {
        expect($el.parent()).to.not.be.visible;
    })
}

// -------------------- 'Skicka intyget' --------------------
export function skickaTillTs() {
    cy.get("#sendBtn", { timeout: 60000 }).click();

    // Modal som dyker upp och frågar om man verkligen vill skicka
    cy.get("#button1send-dialog").click();
    cy.contains("Intyget är skickat till Transportstyrelsen");
}

// -------------------- 'Skriv ut intyget' --------------------
export function skrivUt(typAvUtskrift, intygsId, typAvIntyg){
    switch(typAvUtskrift) {
        case "utkast":
        case "fullständigt":
            cy.request({
                method: 'GET',
                url: 'moduleapi/intyg/' + typAvIntyg +'/' + intygsId + "/pdf",
            });
            cy.log('Skriver ut ett ' + typAvUtskrift+ ' intyg (via cy.request, ej grafiskt)');
            break;
        default:
            cy.log('Ingen korrekt typ av utskrift vald');
    }
}
// ------------------'Ersätta intyg'---------------------------
export function ersatta() {
    cy.get('#ersattBtn').click();
    cy.get('#button1ersatt-dialog').click();
}

// ------------------'Radera utkast'--------------------------
export function raderaUtkast() {
    cy.get('#ta-bort-utkast').click();
    cy.get('#confirm-draft-delete-button').click();   
}

// ------------------'Makulera intyg'-------------------------
export function makuleraIntyg(arg) {
    cy.get('#makuleraBtn').click();
    if (arg === "Annat allvarligt fel") {
        cy.get('#reason-ANNAT_ALLVARLIGT_FEL').check();
        cy.get('#clarification-ANNAT_ALLVARLIGT_FEL').type('Testanledning');
        cy.get('#button1makulera-dialog').click();
    } else {
        cy.get('#reason-FEL_PATIENT').check();
        cy.get('#button1makulera-dialog').click();
    }
}