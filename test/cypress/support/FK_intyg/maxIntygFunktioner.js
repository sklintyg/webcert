import {implementeradeIntyg} from './../commands'

const implementeradeIntygArray = Object.values(implementeradeIntyg);

export function sektion_grund_för_medicinskt_underlag(intygsdata, intygstyp) {
    expect(intygsdata).to.exist;
    expect(implementeradeIntygArray).to.include.members([intygstyp]);

    /* Antagande: Det är inte viktigt med exakt samma datum relativt "idag"
    som används i mallarna. De flesta datum utgår från LISJP-mallen
    */
    const idagMinus5 =  Cypress.moment().subtract(5,  'days').format('YYYY-MM-DD');
    const idagMinus6 =  Cypress.moment().subtract(6,  'days').format('YYYY-MM-DD');
    const idagMinus14 = Cypress.moment().subtract(14, 'days').format('YYYY-MM-DD');
    const idagMinus15 = Cypress.moment().subtract(15, 'days').format('YYYY-MM-DD');
    const idagMinus2Mån = Cypress.moment().subtract(2, 'months').format('YYYY-MM-DD');


    // -------------------- 'Intyget/utlåtandet är baserat på' --------------------
    cy.get('#checkbox_undersokningAvPatienten').check();
    cy.get("#datepicker_undersokningAvPatienten").clear().type(idagMinus5);

    if (intygstyp === implementeradeIntyg.LISJP) {
        cy.get('#checkbox_telefonkontaktMedPatienten').check();
        cy.get("#datepicker_telefonkontaktMedPatienten").clear().type(idagMinus6);
    }

    cy.get('#checkbox_journaluppgifter').check();
    cy.get("#datepicker_journaluppgifter").clear().type(idagMinus15);

    if (intygstyp !== implementeradeIntyg.LISJP) {
        cy.get('#checkbox_anhorigsBeskrivningAvPatienten').check();
        cy.get("#datepicker_anhorigsBeskrivningAvPatienten").clear().type(idagMinus6);
    }

    cy.get('#checkbox_annatGrundForMU').check();
    cy.get("#datepicker_annatGrundForMU").clear().type(idagMinus14);

    // Denna textruta dyker upp efter att "Annat" har klickats i
    cy.get("#annatGrundForMUBeskrivning").type(intygsdata.annatTextareaText);

    if (intygstyp !== implementeradeIntyg.LISJP) {
        cy.get("#datepicker_kannedomOmPatient").clear().type(idagMinus14);

        // Klicka i att utlåtandet även baseras på andra medicinska
        // utredningar eller underlag. Detta gör att nya fält visualiseras
        cy.get("#underlagFinnsYes").click();

        /* Fyll i fält för utredning/underlag:
           - Klicka på raden för att fälla ut dropdown
           - Baserat på  id:t för dropdownen, verifiera att specifik text finns och välj den raden
           - Klicka på raden
           - Lokalisera datumfältet och fyll i datum
           - Lokalisera textfältet där vårdgivare ska anges, fyll i text
        */

        // Fyll i alla fält för utredning/underlag 1:
        cy.get("#underlag-0--typ-selected-item-label").click();
        cy.get("#wcdropdown-underlag-0--typ")
        .contains(intygsdata.underlag1Text)
        .then(option => {
            cy.wrap(option).contains(intygsdata.underlag1Text); // Säkerställ att rätt alternativ valts
            option[0].click(); // jquery "click()", inte Cypress "click()"
        });

        cy.get("#datepicker_underlag\\[0\\]\\.datum").clear().type(idagMinus2Mån);
        cy.get("#underlag-0--hamtasFran").type(intygsdata.underlag1HämtasFrånText);

        // Fyll i alla fält för utredning/underlag 2
        cy.get("#underlag-1--typ-selected-item-label").click();
        cy.get("#wcdropdown-underlag-1--typ")
        .contains(intygsdata.underlag2Text)
        .then(option => {
            cy.wrap(option).contains(intygsdata.underlag2Text); // Säkerställ att rätt alternativ valts
            option[0].click(); // jquery "click()", inte Cypress "click()"
        });

        cy.get("#datepicker_underlag\\[1\\]\\.datum").clear().type(idagMinus2Mån);
        cy.get("#underlag-1--hamtasFran").type(intygsdata.underlag2HämtasFrånText);

        // Fyll i alla fält för utredning/underlag 3
        cy.get("#underlag-2--typ-selected-item-label").click();
        cy.get("#wcdropdown-underlag-2--typ")
        .contains(intygsdata.underlag3Text)
        .then(option => {
            cy.wrap(option).contains(intygsdata.underlag3Text); // Säkerställ att rätt alternativ valts
            option[0].click(); // jquery "click()", inte Cypress "click()"
        });
        
        cy.get("#datepicker_underlag\\[2\\]\\.datum").clear().type(idagMinus2Mån);
        cy.get("#underlag-2--hamtasFran").type(intygsdata.underlag3HämtasFrånText);
    }
}

export function sektion_diagnoser_för_sjukdom(intygsdata, intygstyp) {
    // Antag att ICD-10-SE är förvalt
    cy.get('#diagnoseCode-0').type(intygsdata.diagnosKod1).wait(1000).type('{enter}');
    cy.get('#diagnoseCode-1').type(intygsdata.diagnosKod2).wait(1000).type('{enter}');
    cy.get('#diagnoseCode-2').type(intygsdata.diagnosKod3).wait(1000).type('{enter}');

    if (intygstyp === implementeradeIntyg.LUSE ||
        intygstyp === implementeradeIntyg.LUAE_NA) {
        cy.get("#diagnosgrund").type(intygsdata.diagnosgrundTextSkriv);

        // Finns skäl att revidera tidigare diagnos?
        cy.get("#nyBedomningDiagnosgrundYes").click();
        cy.get("#diagnosForNyBedomning").type(intygsdata.revideraTidigareDiagnosSkriv);
    }
}

export function sektion_aktivitetsbegränsningar(intygsdata) {
    cy.get("#aktivitetsbegransning").type(intygsdata.aktivitetsbegränsningSkriv);
}

export function sektion_medicinsk_behandling(intygsdata, intygstyp) {
    if (intygstyp === implementeradeIntyg.LUSE ||
        intygstyp === implementeradeIntyg.LUAE_NA) {
        cy.get("#avslutadBehandling").type(intygsdata.avslutadBehandlingSkriv);
    }

    cy.get("#pagaendeBehandling").type(intygsdata.pågåendeBehandlingarSkriv);
    cy.get("#planeradBehandling").type(intygsdata.planeradeBehandlingarSkriv);

    if (intygstyp === implementeradeIntyg.LUSE ||
        intygstyp === implementeradeIntyg.LUAE_NA) {
        cy.get("#substansintag").type(intygsdata.substansintagSkriv);
    }
}

export function sektion_medicinska_förutsättningar_för_arbete(intygsdata, intygstyp) {
    cy.get("#medicinskaForutsattningarForArbete").type(intygsdata.förutsättningarFörArbeteSkriv);
    cy.get("#formagaTrotsBegransning").type(intygsdata.förmågaTrotsBegränsningSkriv);
    if (intygstyp === implementeradeIntyg.LUAE_NA) {
        cy.get("#forslagTillAtgard").type(intygsdata.förslagTillÅtgärdSkriv);
    }
}

export function sektion_övriga_upplysningar(intygsdata, intygstyp) {
    var textAttSkriva = intygsdata.övrigaUpplysningarSkriv;
    if (intygstyp === implementeradeIntyg.LISJP) {
        const idagPlus41 = Cypress.moment().add(41, 'days').format('YYYY-MM-DD');
        textAttSkriva += idagPlus41;
    }
    cy.get("#ovrigt").type(textAttSkriva);
}

export function sektion_kontakta_mig(intygsdata) {
    cy.get("#kontaktMedFk").check();
    cy.get("#anledningTillKontakt").type(intygsdata.kontaktMedFKSkriv);
}

export function sektion_signera_intyg(intygsdata) {
    // cy.click() fungerar inte alltid. Det finns issues rapporterade (stängd pga inaktivitet):
    // https://github.com/cypress-io/cypress/issues/2551
    // Nedanstående steg innan klicket på signera-knappen är en workaround för detta.
    cy.contains("Klart att signera");
    cy.contains("Obligatoriska uppgifter saknas").should('not.exist');
    cy.contains("Utkastet sparas").should('not.exist');

    // En utökad timeout p.g.a. att det tar en stund innan denna text försvinner.
    cy.contains("Utkastet är sparat", { timeout: 15000 }).should('not.exist');
    cy.get("#signera-utkast-button").invoke('width').should('be.greaterThan', 0); // TODO: Ta bort?
    cy.get("#signera-utkast-button").should('not.be.disabled'); // TODO: Ta bort?
    cy.get("#signera-utkast-button").click();
}

export function skicka_till_FK(intygsdata) {
    cy.get("#sendBtn", { timeout: 60000 }).click();
    cy.get("#button1send-dialog").click(); // Modal som dyker upp och frågar om man verkligen vill skicka
    cy.contains("Intyget är skickat till Försäkringskassan");
}

export function sektion_funktionsnedsättning(intygsdata) {
    cy.get('#toggle-funktionsnedsattningIntellektuell').click();
    cy.get('#funktionsnedsattningIntellektuell').type(intygsdata.funknedsättningIntellektuellSkriv);

    cy.get('#toggle-funktionsnedsattningKommunikation').click();
    cy.get('#funktionsnedsattningKommunikation').type(intygsdata.funknedsättningKommunikationSkriv);

    cy.get('#toggle-funktionsnedsattningKoncentration').click();
    cy.get('#funktionsnedsattningKoncentration').type(intygsdata.funknedsättningUppmärksamhetSkriv);

    cy.get('#toggle-funktionsnedsattningPsykisk').click();
    cy.get('#funktionsnedsattningPsykisk').type(intygsdata.funknedsättningPsykiskSkriv);

    cy.get('#toggle-funktionsnedsattningSynHorselTal').click();
    cy.get('#funktionsnedsattningSynHorselTal').type(intygsdata.funknedsättningSinneSkriv);

    cy.get('#toggle-funktionsnedsattningBalansKoordination').click();
    cy.get('#funktionsnedsattningBalansKoordination').type(intygsdata.funknedsättningBalansSkriv);

    cy.get('#toggle-funktionsnedsattningAnnan').click();
    cy.get('#funktionsnedsattningAnnan').type(intygsdata.funknedsättningAnnanSkriv);
}