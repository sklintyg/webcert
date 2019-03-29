// Intyget är uppdelat på samma sätt som det är uppdelat när det fylls i genom WebCert

// Datumen är inte specifika för något testfall

// -------------------- 'Grund för medicinskt underlag' --------------------
export function sektionGrundFörMedicinsktUnderlag(medUnderlag) {
    const idagMinus5    = Cypress.moment().subtract(5,   'days').format('YYYY-MM-DD');
    const idagMinus6    = Cypress.moment().subtract(6,   'days').format('YYYY-MM-DD');
    const idagMinus14   = Cypress.moment().subtract(14,  'days').format('YYYY-MM-DD');
    const idagMinus15   = Cypress.moment().subtract(15,  'days').format('YYYY-MM-DD');
    const idagMinus2Mån = Cypress.moment().subtract(2, 'months').format('YYYY-MM-DD');

    const utlåtandeBaseratPå = medUnderlag.utlåtandetÄrBaseratPå;
    if (utlåtandeBaseratPå.minUndersökning) {
        // TODO: Lägger väldigt lång timeout vid första elementet i intyget eftersom
        // sidan ibland inte har hunnit ladda innan den får timeout.
        // Initial analys är att Jenkins är överbelastad då det verkar fungera bra när
        // man kör lokalt.
        cy.get('#checkbox_undersokningAvPatienten', {timeout: 60000}).check();
        cy.get("#datepicker_undersokningAvPatienten").clear().type(idagMinus5);
    }

    if (utlåtandeBaseratPå.journaluppgifter) {
        cy.get('#checkbox_journaluppgifter').check();
        cy.get("#datepicker_journaluppgifter").clear().type(idagMinus15);
    }

    if (utlåtandeBaseratPå.anhörigsBeskrivning) {
        cy.get('#checkbox_anhorigsBeskrivningAvPatienten').check();
        cy.get("#datepicker_anhorigsBeskrivningAvPatienten").clear().type(idagMinus6);
    }

    if (utlåtandeBaseratPå.annat) {
        cy.get('#checkbox_annatGrundForMU').check();
        cy.get("#datepicker_annatGrundForMU").clear().type(idagMinus14);

        // cy.type() tar bara in text eller nummer (så vi behöver inte verifiera värdet)
        cy.get("#annatGrundForMUBeskrivning").type(utlåtandeBaseratPå.annatText);
    }

    cy.get("#datepicker_kannedomOmPatient").clear().type(idagMinus14);

    const andraUtrEllerUnderlag = medUnderlag.andraUtredningarEllerUnderlag;
    if (andraUtrEllerUnderlag.ja) {
        cy.get("#underlagFinnsYes").click();

        // TODO: Ska nedanstående brytas ut till en funktion och anropas tre gånger istället?
        if (andraUtrEllerUnderlag.rad1) {
            const rad = andraUtrEllerUnderlag.rad1;
            cy.get("#underlag-0--typ").click();
            cy.get("#wcdropdown-underlag-0--typ")
            .contains(rad.underlagstyp)
            .then(option => {
                cy.wrap(option).contains(rad.underlagstyp); // Säkerställ att rätt alternativ valts
                option[0].click(); // jquery "click()", inte Cypress "click()"
            });

            cy.get("#datepicker_underlag\\[0\\]\\.datum").clear().type(idagMinus2Mån);
            cy.get("#underlag-0--hamtasFran").type(rad.underlagHämtasFrån);
        }

        if (andraUtrEllerUnderlag.rad2) {
            expect(andraUtrEllerUnderlag.rad1).to.exist;
            const rad = andraUtrEllerUnderlag.rad2;
            cy.get("#underlag-1--typ").click();
            cy.get("#wcdropdown-underlag-1--typ")
            .contains(rad.underlagstyp)
            .then(option => {
                cy.wrap(option).contains(rad.underlagstyp); // Säkerställ att rätt alternativ valts
                option[0].click(); // jquery "click()", inte Cypress "click()"
            });

            cy.get("#datepicker_underlag\\[1\\]\\.datum").clear().type(idagMinus2Mån);
            cy.get("#underlag-1--hamtasFran").type(rad.underlagHämtasFrån);
        }

        if (andraUtrEllerUnderlag.rad3) {
            expect(andraUtrEllerUnderlag.rad2).to.exist;
            const rad = andraUtrEllerUnderlag.rad3;
            cy.get("#underlag-2--typ").click();
            cy.get("#wcdropdown-underlag-2--typ")
            .contains(rad.underlagstyp)
            .then(option => {
                cy.wrap(option).contains(rad.underlagstyp); // Säkerställ att rätt alternativ valts
                option[0].click(); // jquery "click()", inte Cypress "click()"
            });

            cy.get("#datepicker_underlag\\[2\\]\\.datum").clear().type(idagMinus2Mån);
            cy.get("#underlag-2--hamtasFran").type(rad.underlagHämtasFrån);
        }
    }
}

// -------------------- 'Diagnos' --------------------
export function sektionDiagnos(diagnos) {
    if (diagnos.rad1) {
        cy.get('#diagnoseCode-0').parent().within(($diagnoskodrad) => {
            cy.get('#diagnoseCode-0').type(diagnos.rad1.kod);
            cy.wrap($diagnoskodrad).contains(diagnos.rad1.text).click();
        });
        cy.get('#diagnoseDescription-0').invoke('val').should('contain', diagnos.rad1.text);
    }

    if (diagnos.rad2) {
        expect(diagnos.rad1).to.exist;
        cy.get('#diagnoseCode-1').parent().within(($diagnoskodrad) => {
            cy.get('#diagnoseCode-1').type(diagnos.rad2.kod);
            cy.wrap($diagnoskodrad).contains(diagnos.rad2.text).click();
        });
        cy.get('#diagnoseDescription-1').invoke('val').should('contain', diagnos.rad2.text);
    }

    if (diagnos.rad3) {
        expect(diagnos.rad2).to.exist;
        cy.get('#diagnoseCode-2').parent().within(($diagnoskodrad) => {
            cy.get('#diagnoseCode-2').type(diagnos.rad3.kod);
            cy.wrap($diagnoskodrad).contains(diagnos.rad3.text).click();
        });
        cy.get('#diagnoseDescription-2').invoke('val').should('contain', diagnos.rad3.text);
    }
}

// -------------------- 'Funktionsnedsättningar' --------------------
export function sektionFunktionsnedsättning(funkNedsättning) {
    if (funkNedsättning.debut) {
        cy.get("#funktionsnedsattningDebut").type(funkNedsättning.debut);
    }

    if (funkNedsättning.påverkan) {
        cy.get("#funktionsnedsattningPaverkan").type(funkNedsättning.påverkan);
    }
}

// -------------------- 'Övrigt' --------------------
export function sektionÖvrigt(övrigt) {
    cy.get("#ovrigt").type(övrigt.text);
}

// -------------------- 'Kontakt' --------------------
export function sektionKontakt(kontakt) {
    if (kontakt.ja) {
        cy.get("#kontaktMedFk").check();

        if (kontakt.text) {
            cy.get("#anledningTillKontakt").type(kontakt.text);
        }
    }
}