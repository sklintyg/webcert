Cypress.Commands.add("fillOutMaxLisjp", aliasesFromCaller => {

    // Beräkna datum både framåt och bakåt från idag
    const idagPlus1 = Cypress.moment().add(1,  'days').format('YYYY-MM-DD'); // 25%  sjukskrivning start
    const idagPlus11 = Cypress.moment().add(11, 'days').format('YYYY-MM-DD'); // 25%  sjukskrivning slut
    const idagPlus12 = Cypress.moment().add(12, 'days').format('YYYY-MM-DD'); // 50%  sjukskrivning start
    const idagPlus19 = Cypress.moment().add(19, 'days').format('YYYY-MM-DD'); // 50%  sjukskrivning slut
    const idagPlus20 = Cypress.moment().add(20, 'days').format('YYYY-MM-DD'); // 75%  sjukskrivning start
    const idagPlus28 = Cypress.moment().add(28, 'days').format('YYYY-MM-DD'); // 75%  sjukskrivning slut
    const idagPlus29 = Cypress.moment().add(29, 'days').format('YYYY-MM-DD'); // 100% sjukskrivning start
    const idagPlus41 = Cypress.moment().add(41, 'days').format('YYYY-MM-DD'); // 100% sjukskrivning slut
    const idagMinus5 = Cypress.moment().subtract(5,  'days').format('YYYY-MM-DD');  // Patient examination date
    const idagMinus6 = Cypress.moment().subtract(6,  'days').format('YYYY-MM-DD');  // Date of phone contact with patient
    const idagMinus14 = Cypress.moment().subtract(14, 'days').format('YYYY-MM-DD'); // Midwife's assessment date
    const idagMinus15 = Cypress.moment().subtract(15, 'days').format('YYYY-MM-DD'); // Journal entry date

    // -------------------- 'Intyget är baserat på' --------------------
    cy.contains(aliasesFromCaller.lisjpData.minUndersökning).parentsUntil('.ue-del-fraga').within(($form) => {
        cy.get('[type="checkbox"]').check();
        cy.get(aliasesFromCaller.lisjpData.datumUndersökning).clear().type(idagMinus5);
    });

    cy.contains(aliasesFromCaller.lisjpData.telefonKontakt).parentsUntil('.ue-del-fraga').within(($form) => {
        cy.get('[type="checkbox"]').check();
        cy.get(aliasesFromCaller.lisjpData.datumTelefonkontakt).clear().type(idagMinus6);
    });

    cy.contains(aliasesFromCaller.lisjpData.journalUppgifterFrån).parentsUntil('.ue-del-fraga').within(($form) => {
        cy.wrap($form).get('[type="checkbox"]').check();
        cy.wrap($form).get(aliasesFromCaller.lisjpData.datumJournalUppgifterFrån).clear().type(idagMinus15);
    });

    cy.contains(aliasesFromCaller.lisjpData.annat).parentsUntil('.ue-del-fraga').within(($form) => {
        cy.get('[type="checkbox"]').check();
        cy.get(aliasesFromCaller.lisjpData.datumAnnat).clear().type(idagMinus14);
    });

    // Denna textruta dyker upp efter att "Annat" har klickats i
    cy.get(aliasesFromCaller.lisjpData.annatTextarea).type(aliasesFromCaller.lisjpData.annatTextareaText);


    // ----- 'I relation till vilken sysselsättning bedömer du arbetsförmågan?' -----
    cy.contains(aliasesFromCaller.lisjpData.nuvarandeArbete).parent().within(($form) => {
        cy.get('[type="checkbox"]').check();
    });

    cy.contains(aliasesFromCaller.lisjpData.yrkeOchUppgifter).parent().parent().parent().within(($form) => {
        cy.wrap($form).find('textarea').type(aliasesFromCaller.lisjpData.yrkeOchUppgifterText);
    });

    cy.contains(aliasesFromCaller.lisjpData.checkboxTextNormaltFörekommandeJobb).parent().within(($form) => {
        cy.get('[type="checkbox"]').check();
    });

    cy.contains(aliasesFromCaller.lisjpData.checkboxTextVAB).parent().within(($form) => {
        cy.get('[type="checkbox"]').check();
    });

    cy.contains(aliasesFromCaller.lisjpData.checkboxStudier).parent().within(($form) => {
        cy.get('[type="checkbox"]').check();
    });

    // ----- 'Diagnos' ----- //
    cy.contains(aliasesFromCaller.lisjpData.diagnoserNedsattArbetsförmåga).parent().parent().parent().within(($form) => {
        // Antag att ICD-10-SE är förvalt
        cy.get('[placeholder=' + aliasesFromCaller.lisjpData.kodTextareaPlaceholder + ']').then(($codeFields) => {
            cy.wrap($codeFields.eq(0)).type(aliasesFromCaller.lisjpData.diagnosKod1).wait(1000).type('{enter}');
            cy.wrap($codeFields.eq(1)).type(aliasesFromCaller.lisjpData.diagnosKod2).wait(1000).type('{enter}');
            cy.wrap($codeFields.eq(2)).type(aliasesFromCaller.lisjpData.diagnosKod3).wait(1000).type('{enter}');
        });
    });

    // ----- 'Sjukdomens konsekvenser för patienten' ----- //
    cy.contains(aliasesFromCaller.lisjpData.beskrivObservationer).parent().parent().parent().within(($form) => {
        cy.wrap($form).find('textarea').type(aliasesFromCaller.lisjpData.besvärsBeskrivning);
    });

    cy.contains(aliasesFromCaller.lisjpData.beskrivPatientBegränsning).parent().parent().parent().within(($form) => {
        cy.wrap($form).find('textarea')
            .type(aliasesFromCaller.lisjpData.begränsningsBeskrivning);
    });

    // ----- 'Medicinsk behandling' ----- //
    cy.contains(aliasesFromCaller.lisjpData.medicinskaBehandlingar).parent().parent().parent().within(($form) => {
        cy.wrap($form).find('textarea')
            .type(aliasesFromCaller.lisjpData.medicinskaBehandlingarBeskrivning);
    });

    cy.contains(aliasesFromCaller.lisjpData.planeradeBehandlingar).parent().parent().parent().within(($form) => {
        cy.wrap($form).find('textarea')
            .type(aliasesFromCaller.lisjpData.planeradeBehandlingarBeskrivning);
    });

    // ----- 'Bedömning' -----//
    cy.contains(aliasesFromCaller.lisjpData.arbetsförmågaBedömning).parent().parent().parent().within(($form) => {
        cy.get('[type="checkbox"]').within(($checkboxes) => {
            cy.wrap($checkboxes).check(); // Klickar i alla checkboxar i sektionen. "Från"-datum blir dagens datum

            cy.wrap($checkboxes.eq(0)).parent().parent().parent().parent().within(($row) => { // Hämta raden med 25% sjukskrivning
                cy.get('[type="text"]').then(($textFields) => {
                    cy.wrap($textFields.eq(0)).clear().type(idagPlus1);
                    cy.wrap($textFields.eq(1)).clear().type(idagPlus11);
                });
            });

            cy.wrap($checkboxes.eq(1)).parent().parent().parent().parent().within(($row) => { // Hämta raden med 50% sjukskrivning
                cy.get('[type="text"]').then(($textFields) => {
                    cy.wrap($textFields.eq(0)).clear().type(idagPlus12);
                    cy.wrap($textFields.eq(1)).clear().type(idagPlus19);
                });
            });

            cy.wrap($checkboxes.eq(2)).parent().parent().parent().parent().within(($row) => { // Hämta raden med 75% sjukskrivning
                cy.get('[type="text"]').then(($textFields) => {
                    cy.wrap($textFields.eq(0)).clear().type(idagPlus20);
                    cy.wrap($textFields.eq(1)).clear().type(idagPlus28);
                });
            });

            cy.wrap($checkboxes.eq(3)).parent().parent().parent().parent().within(($row) => { // Hämta raden med 100% sjukskrivning
                cy.get('[type="text"]').then(($textFields) => {
                    cy.wrap($textFields.eq(0)).clear().type(idagPlus29);
                    cy.wrap($textFields.eq(1)).clear().type(idagPlus41);
                });
            });
        });
    });

    cy
        .contains(aliasesFromCaller.lisjpData.längreNedsattArbetsförmåga)
        .parent().parent().parent()
        .find('textarea').type(aliasesFromCaller.lisjpData.längreNedsattArbetsförmågaText);

    cy.contains(aliasesFromCaller.lisjpData.förläggaArbetstidAnnorlunda)
        .parent().parent().parent().within(($elem) => {
            cy.get('[type="radio"]').eq(0).check(); // Första radioknappen är "Ja"
        });

    cy.contains(aliasesFromCaller.lisjpData.arbetstidAnnorlundaMedicinskaSkäl).parent().parent().parent().find('textarea')
        .type(aliasesFromCaller.lisjpData.arbetstidAnnorlundaMedicinskaSkälBeskrivning);

    cy.contains(aliasesFromCaller.lisjpData.resaMöjliggörArbete)
        .parent().parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

    cy.contains(aliasesFromCaller.lisjpData.arbetsförmågaPrognos).parent().parent().parent().within(($ele) => {
        cy.get('[type="radio"]').eq(0).check() // Översta radioknappen är den som ska anges
    });

    // ----- 'Åtgärder' -----//
    cy.contains(aliasesFromCaller.lisjpData.föreslåÅtgärder).parent().parent().parent().within(($elem) => {
        cy.get('[type="checkbox"]').each(($el, index, $list) => {
            if (index != 0) { // Index 0 är "Inte aktuellt", detta är den enda checkboxen som INTE ska anges
                cy.wrap($el).check();
            }
        });
    });

    cy.wait(3000);

    cy.contains(aliasesFromCaller.lisjpData.flerÅtgärder).should('be.visible').parent().parent().parent().find('textarea')
        .type(aliasesFromCaller.lisjpData.flerÅtgärderBeskrivning);

    // ----- 'Övriga upplysningar' -----//
    //cy.contains('Övriga upplysningar').parent().parent().parent().find('textarea').type('Planerad partus ' + idagPlus41);
    cy.get('[name="ovrigt"]').type(aliasesFromCaller.lisjpData.övrigaUpplysningarBeskrivning + idagPlus41); 	// Tillfällig lösning. Använder attribut 'name' eftersom textareans rubrik finns på fler än ett ställe

    // ----- 'Kontakt' -----//
    cy.contains(aliasesFromCaller.lisjpData.kontaktaMig).parent().parent().parent().within(($elem) => {
        cy.get('[type="checkbox"]').check();
    });

    cy.contains(aliasesFromCaller.lisjpData.anledningKontakt).parent().parent().parent().find('textarea')
        .type(aliasesFromCaller.lisjpData.anledningKontaktBeskrivning);

    // ----- 'Signera intyg' -----//

    // Lägger till en paus här för att se om det är så att Cypress klickar för snabbt på Signera-knappen. I Jenkins
    // failar testfallet ofta p.g.a att det dyker upp en modal som säger att intyger har ändrats av annan person (men personen som
    // vill signera intyget är samma som anges som den som har ändrat det)
    // Just nu verkar testfallet gå igenom men frågan är om detta har något med saken att göra eller om det fungerar bara för att jag bytte till
    // ny slav på Jenkins (det förkortade exekveringstiden till hälften ungefär)
    cy.wait(6000);

    // Om laptop kör på batteri och Wifi så visar Cypress att knappen trycks in,
    // men ofta händer inget
    cy.contains('button', aliasesFromCaller.lisjpData.signeraKnappText, { timeout: 15000 })
        .should('be.enabled')
        .then(($button) => {
            cy.wait(1000);
            cy.wrap($button).click(); // prova att ändra till <button?
        });

    // Paus här också, precis som ovanför "Signera intyg"-knappen. Behövs den nu när vi kör på snabbare slav?
    cy.wait(6000);

    cy.contains('div', aliasesFromCaller.lisjpData.väljIntygsmottagare, { timeout: 20000 })
        .should('be.visible')
        .within(($elem) => {
            cy.get('[type="radio"]').each(($el, index, $list) => { // Hämtar ut samtliga radioknappar... kan förbättras
                if (index >= 2) { // Index 0 och 1 är "Försäkringskassan" 'Ja' och 'Nej'. Hoppas över.
                    if (index % 2 != 0) { // Klicka endast i udda nummer eftersom dessa är "Nej"
                        cy.wrap($el).check();
                    }
                }
            });
        });

    cy.get('[name="approveForm"]').within(($form) => {
        cy.get('[type="button"]').then(($button) => {
            cy.wrap($button).contains(aliasesFromCaller.lisjpData.intygsmottagareKnappText).should('be.visible');
            cy.wrap($button).click({ force: true });
        });
    });

    // Testfallet har failat enstaka gånger i CI p.g.a. timeout på denna knapp, ökar därför timeout
    cy.contains('button', aliasesFromCaller.lisjpData.skickaTillFKKnappText, { timeout: 20000 })
        .should('be.visible').and('be.enabled')
        .click();

    cy.contains(aliasesFromCaller.lisjpData.varningSkickaTillFK).should('be.visible');

    cy.contains('button', aliasesFromCaller.lisjpData.skickaKnappTextEfterVarning)
        .should('be.visible')
        .click();
});