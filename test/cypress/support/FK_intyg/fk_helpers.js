/**
 * Denna fil innehåller FK-gemensamma funktioner för att reducera mängden duplicerad kod
 */

export function besökÖnskadUrl(önskadUrl, vårdpersonal, vårdenhet, utkastId) {
    cy.visit(önskadUrl);

    // Om vi dirigeras till sidan som säger att 'Intygsutkastet är raderat'
    // så försöker vi igen eftersom det antagligen gick för snabbt.
    cy.get('body').then(($body) => {
        if ($body.text().includes('Intygsutkastet är raderat och kan därför inte längre visas.')) {
            cy.log("Kom till 'Intygetsutkastet är raderat', antagligen gick det för snabbt. Provar igen.");
            cy.loggaInVårdpersonalIntegrerat(vårdpersonal, vårdenhet); // Vi behöver logga in igen
            cy.visit(önskadUrl);
        }
    });
    cy.url().should('include', utkastId);
}

export function loggaUtLoggaIn(vårdpersonal, vårdenhet) {
    // Lite specialvariant av logga ut/logga in för att sedan öppna intyget på nytt med en ny session
    cy.clearCookies();
    cy.visit('/logout');
    cy.loggaInVårdpersonalIntegrerat(vårdpersonal, vårdenhet);
}

export function sektionÖvrigt(övrigt) {
    cy.get("#ovrigt").type(övrigt.text);
}

export function sektionKontakt(kontakt) {
    if (kontakt.ja) {
        cy.get("#kontaktMedFk").check();

        if (kontakt.text) {
            cy.get("#anledningTillKontakt").type(kontakt.text);
        }
    }
}

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

export function skickaTillFk() {
    cy.get("#sendBtn", { timeout: 60000 }).click();

    // Modal som dyker upp och frågar om man verkligen vill skicka
    cy.get("#button1send-dialog").click();
    cy.contains("Intyget är skickat till Försäkringskassan");
}

// Generell utskriftsfunktion. Gäller inte för t.ex. LISJP
export function skrivUt(typAvUtskrift, intygsId, intygsTyp){
    switch(typAvUtskrift) {
        case "utkast":
        case "fullständigt":
            cy.request({
                method: 'GET',
                url: 'moduleapi/intyg/' + intygsTyp + '/' + intygsId + "/pdf",
            });
            cy.log('Skriver ut ett ' + typAvUtskrift + ' intyg (via cy.request, ej grafiskt)');
            break;
        default:
            cy.log('Ingen korrekt typ av utskrift vald');
    }
}

export function fornya() {
    cy.get('#fornyaBtn').click();
    if (cy.get('#button1fornya-dialog')) {
        cy.get('#button1fornya-dialog').click();
    }
}

export function raderaUtkast() {
    cy.get('#ta-bort-utkast').click();
    cy.get('#confirm-draft-delete-button').click();   
}

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