// Intyget är uppdelat på samma sätt som det är uppdelat när det fylls i genom WebCert

// Datumen är inte specifika för något testfall

export {besökÖnskadUrl, loggaUtLoggaIn} from '../FK_intyg/fk_helpers';

// -------------------- 'Smittbärarpenning' --------------------
// Ej implementerad än

// -------------------- 'Grund för medicinskt underlag' --------------------
export function sektionGrundMedicinsktUnderlag(medicinsktUnderlag) {
    
    if (medicinsktUnderlag.undersökning) {
        cy.get('#checkbox_undersokningsDatum').check();
        const idagDatum  = Cypress.moment().format('YYYY-MM-DD');
        cy.get('#datepicker_undersokningsDatum').clear().type(idagDatum);
    }

    if (medicinsktUnderlag.annat) {
        cy.get('#checkbox_annatDatum').check();
        const idagMinusTvåMånDatum  = Cypress.moment().subtract(2, "months").format('YYYY-MM-DD');
        cy.log(idagMinusTvåMånDatum);
        cy.get('#datepicker_annatDatum').clear().type(idagMinusTvåMånDatum);
        cy.get('#annatBeskrivning').type(medicinsktUnderlag.annatText);
    }
}

// -------------------- 'Arbetsmarknadspolitiskt program' --------------------
export function sektionArbetsmarknadspolitisktProgram(arbetsmarknadspolitiskt) {
    
    cy.get('#arbetsmarknadspolitisktProgram-medicinskBedomning').type(arbetsmarknadspolitiskt.text);

    if (arbetsmarknadspolitiskt.heltid) {
        cy.get('#arbetsmarknadspolitisktProgram\\.omfattning-HELTID').check();
    }

    if (arbetsmarknadspolitiskt.deltid) {
        cy.get('#arbetsmarknadspolitisktProgram\\.omfattning-DELTID').check();
        cy.get('#arbetsmarknadspolitisktProgram-omfattningDeltid').type(arbetsmarknadspolitiskt.deltidTimmar);
    }

    if (arbetsmarknadspolitiskt.okänd) {
        cy.get('#arbetsmarknadspolitisktProgram\\.omfattning-OKAND').check();
    }
}

// -------------------- 'Sjukdomens konsekvenser för patienten' --------------------
export function sektionSjukdomensKonsekvenser(konsekvenser) {
    
    cy.get('#funktionsnedsattning').type(konsekvenser.funktionsnedsättning);
    cy.get('#aktivitetsbegransning').type(konsekvenser.aktivitetsbegränsning);
}

// -------------------- 'Bedömning' --------------------
export function sektionBedömning(bedömning) {
    
    var deltidBoxar = {
        1: "#sjukfranvaro-1-checked",
        2: "#sjukfranvaro-2-checked",
        3: "#sjukfranvaro-3-checked"
    }

    var deltidTOM = {
        1: "#sjukfranvaro-1-tom",
        2: "#sjukfranvaro-2-tom",
        3: "#sjukfranvaro-3-tom"
    }

    var deltidProc = {
        1: "#sjukfranvaro-1-niva",
        2: "#sjukfranvaro-2-niva",
        3: "#sjukfranvaro-3-niva"
    }

    var procent = {
        1: "75",
        2: "50",
        3: "25"
    }

    var dagar = {
        1: Cypress.moment().add(14,  'days').format('YYYY-MM-DD'),
        2: Cypress.moment().add(21,  'days').format('YYYY-MM-DD'),
        3: Cypress.moment().add(28,  'days').format('YYYY-MM-DD')
    }

    if (bedömning.förhindrad.ja) {
        cy.get('#harForhinderYes').check();
        if (bedömning.förhindrad.heltid) {
            cy.get('#sjukfranvaro-0-checked').check();
            const idagPlusSju  = Cypress.moment().add(7,  'days').format('YYYY-MM-DD');
            cy.get('#sjukfranvaro-0-tom').type(idagPlusSju);
        }
        assert.isTrue(bedömning.förhindrad.deltidAntal < 4, "Kontrollerar antal deltider. Ska vara mindra än 4, antal = " + bedömning.förhindrad.deltidAntal);
        if (bedömning.förhindrad.deltidAntal > 0) {
            for (var i = 1; i <= bedömning.förhindrad.deltidAntal; i++) {
                if (i > 1 && i < 4) {
                    cy.get('#sjukfranvaro-addRow').click();
                }
                if (i > 3) {
                    break;
                }
                cy.get(deltidBoxar[i]).check();
                cy.get(deltidProc[i]).type(procent[i]);
                cy.get(deltidTOM[i]).type(dagar[i]);
                
            }
        }

    } else {
        cy.get('#harForhinderNo').check();
    }

    if (bedömning.frånvaroPeriod.ja) {
        cy.get('#begransningSjukfranvaro-kanBegransasYes').check();
        cy.get('#begransningSjukfranvaro-beskrivning').type(bedömning.frånvaroPeriod.text);
    } else {
        cy.get('#begransningSjukfranvaro-kanBegransasNo').check();
    }

    if (bedömning.efterFrånvaro.utanAnpassning) {
        cy.get('#prognosAtergang\\.prognos-ATERGA_UTAN_ANPASSNING').check();
    }
    
    if (bedömning.efterFrånvaro.medAnpassning) {
        cy.get('#prognosAtergang\\.prognos-ATERGA_MED_ANPASSNING').check();
        cy.get('#prognosAtergang-anpassningar').type(bedömning.efterFrånvaro.medAnpassningText);
    }

    if (bedömning.efterFrånvaro.kanInte) {
        cy.get('#prognosAtergang\\.prognos-KAN_EJ_ATERGA').check();
    }

    if (bedömning.efterFrånvaro.ejMöjligt) {
        cy.get('#prognosAtergang\\.prognos-EJ_MOJLIGT_AVGORA').check();
    }
}


// -------------------- 'Vårdenhetens adress' --------------------
// Ej implementerad

// -------------------- 'Signera och skicka intyget' --------------------
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
export function skickaTillAf() {
    cy.get("#sendBtn", { timeout: 60000 }).click();

    // Modal som dyker upp och frågar om man verkligen vill skicka
    cy.get("#button1send-dialog").click();
    cy.contains("Intyget är skickat till Arbetsförmedlingen");
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