// Intyget är uppdelat på samma sätt som det är uppdelat när det fylls i genom WebCert

// Datumen är inte specifika för något testfall

export {besökÖnskadUrl, loggaUtLoggaIn} from '../FK_intyg/fk_helpers';

// -------------------- 'Smittbärarpenning' --------------------
// Ej implementerad än

// -------------------- 'Funktionsnedsättning' --------------------
export function sektionFunktionsnedsättning(funktionsnedsättning) {
    
    if (funktionsnedsättning.ja) {
        cy.get('#harFunktionsnedsattningYes').check();
        cy.get('#funktionsnedsattning').type(funktionsnedsättning.text);
    } else {
        cy.get('#harFunktionsnedsattningNo').check();
    }
}

// -------------------- 'Aktivitetsbegränsning' --------------------
export function sektionAktivitetsbegränsning(funktionsnedsättning, aktivitetsbegränsning) {
    
    if (funktionsnedsättning.ja) {
        if (aktivitetsbegränsning.ja) {
            cy.get('#harAktivitetsbegransningYes').check();
            cy.get('#aktivitetsbegransning').type(aktivitetsbegränsning.text);
        } else {
            cy.get('#harAktivitetsbegransningNo').check();
        }
    }
}

// -------------------- 'Utredning och behandling' --------------------
export function sektionUtredningBehandling(utredning) {
    
    if (utredning.ja) {
        cy.get('#harUtredningBehandlingYes').check();
        cy.get('#utredningBehandling').type(utredning.text);
    } else {
        cy.get('#harUtredningBehandlingNo').check();
    }
}

// -------------------- 'Arbetets påverkan på sjukdom/skada' --------------------
export function sektionArbetetsPåverkanSjukdomSkada(påverkan) {
    
    if (påverkan.ja) {
        cy.get('#harArbetetsPaverkanYes').check();
        cy.get('#arbetetsPaverkan').type(påverkan.text);
    } else {
        cy.get('#harArbetetsPaverkanNo').check();
    }
}

// -------------------- 'Övrigt' --------------------
export function sektionÖvrigt(övrigt) {
    
    if (övrigt.ja) {
        cy.get('#ovrigt').type(övrigt.text);
    }
}

// -------------------- 'Vårdenhetens adress' --------------------
// Ej implementerad

// -------------------- 'Signera och skicka intyget' --------------------
export function signeraOchSkicka() {
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

    cy.contains("Intyget är skickat till Arbetsförmedlingen");
}

// -------------------- 'Skicka intyget' --------------------
export function skickaTillFk() {
    cy.get("#sendBtn", { timeout: 60000 }).click();

    // Modal som dyker upp och frågar om man verkligen vill skicka
    cy.get("#button1send-dialog").click();
    cy.contains("Intyget är skickat till Försäkringskassan");
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