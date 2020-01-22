// Intyget är uppdelat på samma sätt som det är uppdelat när det fylls i genom WebCert

// Datumen är inte specifika för något testfall
export {besökÖnskadUrl, loggaUtLoggaIn} from '../FK_intyg/fk_helpers';

// -------------------- 'Identitet' --------------------
export function sektionIdentitet(identitet) {
    if (identitet) {

        var indentifieringsTyper = {
            "IDkort": '#identitetStyrktGenom\\.typ-ID_KORT',
            "FöretagskortTjänstekort": '#identitetStyrktGenom\\.typ-FORETAG_ELLER_TJANSTEKORT',
            "Körkort": '#identitetStyrktGenom\\.typ-KORKORT',
            "PersonligKännedom": '#identitetStyrktGenom\\.typ-PERS_KANNEDOM',
            "Försäkran": '#identitetStyrktGenom\\.typ-FORSAKRAN_KAP18',
            "Pass": '#identitetStyrktGenom\\.typ-PASS'
        }

        for (var key in identitet) {
            if (identitet[key]) {
                cy.get(indentifieringsTyper[key]).check();
            }
        }
    }
}

// -------------------- 'Anmälan' --------------------
export function sektionAnmälan(anmälan) {
    if (anmälan.olämplighet) {
        cy.get('#anmalanAvser\\.typ-OLAMPLIGHET').check();
    } else {
        cy.get('#anmalanAvser\\.typ-SANNOLIK_OLAMPLIGHET').check();
    }
}

// -------------------- 'Medicinska förhållanden' --------------------
export function sektionMedicinskaFörhållanden(medicinskaFörhållanden) {
    cy.get('#medicinskaForhallanden').type(medicinskaFörhållanden);
    const idagMinus2Mån  = Cypress.moment().subtract(2,  'months').format('YYYY-MM-DD');
    cy.get('#datepicker_senasteUndersokningsdatum').type(idagMinus2Mån);
}

// -------------------'Bedömning' -----------------------
export function sektionBedömning(bedömning) {

    var bedömingAlternativ = {
        "ABochTraktorTyper": '#intygetAvserBehorigheter\\.typer-A_B_TRAKTOR',
        "Ctyper": '#intygetAvserBehorigheter\\.typer-C_E',
        "Dtyper": '#intygetAvserBehorigheter\\.typer-D',
        "taxi": '#intygetAvserBehorigheter\\.typer-TAXI'
    };

    if (bedömning.kanInteTaStällning) {
        cy.get('#intygetAvserBehorigheter\\.typer-KANINTETASTALLNING').check();
    } else if (bedömning.allaBehörigheter) {
        cy.get('#intygetAvserBehorigheter\\.typer-ALLA').check();
    } else {
        for (var key in bedömning.typer) {
            if (bedömning.typer[key]) {
                cy.get(bedömingAlternativ[key]).check();
            }
        }
    }
}

// -------------------- 'Information om beslut' --------------------
export function sektionInfoOmBeslut(info) {
    if (info) {
        cy.get('#informationOmTsBeslutOnskas').check();
    }
}

// -------------------- 'Vårdenhetens adress' --------------------
// Ej implementerad

// -------------------- 'Signera intyget' --------------------
export function signeraOchSkicka() {
    // TODO: Utan wait så tappas ofta slutet på texten bort i sista textboxen.
    // Antagligen hinner WebCert inte auto-spara innan man trycker på "signera".
    // Wait är dock ett anti-pattern så finns något annat sätt så är det att föredra.
    cy.wait(8000);

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
    });
    cy.contains("Intyget är skickat till Transportstyrelsen");
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