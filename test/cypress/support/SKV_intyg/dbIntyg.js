// Intyget är uppdelat på samma sätt som det är uppdelat när det fylls i genom WebCert

// Datumen är inte specifika för något testfall

// -------------------- 'Smittbärarpenning' --------------------
// Ej implementerad än

// -------------------- 'Kompletterande patientuppgifter' --------------------
export function sektionPatientuppgifter(identitet) {
    
    cy.get('#identitetStyrkt').type(identitet);
}

// -------------------- 'Dödsdatum och dödsplats' --------------------
export function sektionDödsdatumDödsplats(dödsuppgifter) {
    
    if (dödsuppgifter.dödsdatum.säkert) {
        cy.get('#dodsdatumSakertYes').check();
        const igår = Cypress.moment().subtract(1,  'days').format('YYYY-MM-DD');
        cy.get('#datepicker_dodsdatum').type(igår);
    } else {
        cy.get('#dodsdatumSakertNo').check();
        cy.get('#dodsdatum-year-selected-item-label').click();
        const dettaÅr = Cypress.moment().format('YYYY')
        cy.get('#dodsdatum-year-' + dettaÅr + ' > span').click();
        cy.get('#dodsdatum-month-selected-item-label').click();
        cy.get('#dodsdatum-month-00 > span').click();
        const igår = Cypress.moment().subtract(1,  'days').format('YYYY-MM-DD');
        cy.get('#datepicker_antraffatDodDatum').type(igår);
    }

    cy.get('#dodsplatsKommun').type(dödsuppgifter.dödsplats);

    if (dödsuppgifter.påträffades.sjukhus) {
        cy.get('#dodsplatsBoende-SJUKHUS').check();
    }
    if (dödsuppgifter.påträffades.ordinärt) {
        cy.get('#dodsplatsBoende-ORDINART_BOENDE').check();
    }
    if (dödsuppgifter.påträffades.särskilt) {
        cy.get('#dodsplatsBoende-SARSKILT_BOENDE').check();
    }
    if (dödsuppgifter.påträffades.okänd) {
        cy.get('#dodsplatsBoende-ANNAN').check();
    }
}

// -------------------- 'Barn som avlidigt senast 28 dygn efter födseln' --------------------
export function sektionBarnAvlidigt(avlidit28dagar) {
    
    cy.get('#barnYes').then((ele) => {
        cy.log('Är radioknapparna disable:ade för 28 dygn frågan: ' + ele[0].disabled);
        if (!ele[0].disabled) {
            if (avlidit28dagar.ja) {
                cy.get('#barnYes').check();
            } else {
                cy.get('#barnNo').check();
            }
        }
    });
}

// -------------------- 'Explosivt implantat' --------------------
export function sektionExplosivtImplantat(implantat) {
    
    if (implantat.ja) {
        cy.get('#explosivImplantatYes').check();
        if (implantat.avlägsnats) {
            cy.get('#explosivAvlagsnatYes').check();
        } else {
            cy.get('#explosivAvlagsnatNo').check();
        }
    } else {
        cy.get('#explosivImplantatNo').check();
    }
}

// -------------------- 'Yttre undersökning' --------------------
export function sektionYttreUndersökning(undersökning) {
    const idagMinus10Dagar  = Cypress.moment().subtract(10,  'days').format('YYYY-MM-DD');
    if (undersökning.ja) {
        cy.get('#undersokningYttre-SVAR_JA').check();
    }
    if (undersökning.skaGöras) {
        cy.get('#undersokningYttre-UNDERSOKNING_SKA_GORAS').check();
    }
    if (undersökning.kortFöreDöden) {
        cy.get('#undersokningYttre-UNDERSOKNING_GJORT_KORT_FORE_DODEN').check();
        cy.get('#datepicker_undersokningDatum').type(idagMinus10Dagar);
    }
}

// -------------------- 'Polisanmälan' --------------------
export function sektionPolisanmälan(polisanmälan) {
    
    cy.get('#polisanmalanYes').then((ele) => {
        cy.log('Är radioknapparna disable:ade för polisanmälan: ' + ele[0].disabled);
        if (!ele[0].disabled) {
            if (polisanmälan.ja) {
                cy.get('#polisanmalanYes').check();
            } else {
                cy.get('#polisanmalanNo').check();
            }
        }
    })
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
  /*  cy.get('#signera-utkast-button').parent().should('be.visible')

    cy.get('#signera-utkast-button')
    .pipe(click, {timeout: 60000}) // ToDo: Lång timeout (problem endast på Jenkins, överlastad slav?)
    .should($el => {
        expect($el.parent()).to.not.be.visible;
    })*/
    cy.get('#signera-utkast-button').click();
        
    cy.contains("Intyget är skickat till Skatteverket");
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
export function makuleraIntyg() {
    cy.get('#makuleraBtn').click();
    cy.get('#button1makulera-dialog').click();
}