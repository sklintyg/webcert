import * as maxIntygFunktioner from './maxIntygFunktioner'
import {implementeradeIntyg} from './../commands'

Cypress.Commands.add("fyllIMaxLuaeFs", aliasesFromCaller => {
    const intygsdata = aliasesFromCaller.luaeFsData;
    expect(intygsdata).to.exist;
    const intygstyp = implementeradeIntyg.LUAE_FS;
    //const intygstyp = "LUAE_FS";

    // ----- Sektion 'Grund för medicinskt underlag' -----
    maxIntygFunktioner.sektion_grund_för_medicinskt_underlag(intygsdata, intygstyp);

    // ----- Sektion 'Diagnos/Diagnoser för sjukdom som orsakar eller har orsakat funktionsnedsättning' ----- //
    maxIntygFunktioner.sektion_diagnoser_för_sjukdom(intygsdata, intygstyp);

    // ----- Sektion 'Funktionsnedsättning' ----- //
    cy.get("#funktionsnedsattningDebut").type(intygsdata.funktionsnedsättningDebutSkriv);
    cy.get("#funktionsnedsattningPaverkan").type(intygsdata.funktionsnedsättningPåverkanSkriv);

    // ----- Sektion 'Övriga upplysningar' -----//
    maxIntygFunktioner.sektion_övriga_upplysningar(intygsdata, intygstyp);

    // ----- Sektion 'Kontakt' -----//
    maxIntygFunktioner.sektion_kontakta_mig(intygsdata);

    // ----- Sektion 'Signera intyg' -----//
    maxIntygFunktioner.sektion_signera_intyg(intygsdata);

    // Skicka iväg intyget
    maxIntygFunktioner.skicka_till_FK(intygsdata);
});

Cypress.Commands.add("fyllIMaxLuaeNa", aliasesFromCaller => {
    const intygsdata = aliasesFromCaller.luaeNaData;
    expect(intygsdata).to.exist;
    const intygstyp = implementeradeIntyg.LUAE_NA;

    // ----- Sektion 'Grund för medicinskt underlag' -----
    maxIntygFunktioner.sektion_grund_för_medicinskt_underlag(intygsdata, intygstyp);

    // ----- Sektion 'Diagnos/Diagnoser för sjukdom som orsakar nedsatt arbetsförmåga' ----- //
    maxIntygFunktioner.sektion_diagnoser_för_sjukdom(intygsdata, intygstyp);

    // ----- Sektion 'Bakgrund - beskriv kortfattat förloppet för aktuella sjukdomar' ----- //
    cy.get(intygsdata.bakgrundSjukdomsförloppTextfältId).type(intygsdata.bakgrundSjukdomsförloppTextSkriv);

    // ----- Sektion 'Funktionsnedsättning - beskriv undersökningsfynd och ...' ----- //
    maxIntygFunktioner.sektion_funktionsnedsättning(intygsdata);

    // ----- Sektion 'Aktivitetsbegränsning' ----- //
    maxIntygFunktioner.sektion_aktivitetsbegränsningar(intygsdata);

    // ----- Sektion 'Medicinsk behandling' ----- //
    maxIntygFunktioner.sektion_medicinsk_behandling(intygsdata, intygstyp);

    // ----- Sektion 'Medicinska förutsättningar för arbete' ----- //
    maxIntygFunktioner.sektion_medicinska_förutsättningar_för_arbete(intygsdata, intygstyp);

    // ----- Sektion 'Övriga upplysningar' -----//
    maxIntygFunktioner.sektion_övriga_upplysningar(intygsdata, intygstyp);

    // ----- Sektion 'Kontakt' -----//
    maxIntygFunktioner.sektion_kontakta_mig(intygsdata);

    // ----- Sektion 'Signera intyg' -----//
    maxIntygFunktioner.sektion_signera_intyg(intygsdata);

    // Skicka iväg intyget
    maxIntygFunktioner.skicka_till_FK(intygsdata);
});

Cypress.Commands.add("fyllIMaxLuse", aliasesFromCaller => {
    const intygsdata = aliasesFromCaller.luseData;
    expect(intygsdata).to.exist;
    const intygstyp = implementeradeIntyg.LUSE;

    // ----- Sektion 'Grund för medicinskt underlag' -----
    maxIntygFunktioner.sektion_grund_för_medicinskt_underlag(intygsdata, intygstyp);

    // ----- Sektion 'Diagnos/Diagnoser för sjukdom som orsakar nedsatt arbetsförmåga' ----- //
    maxIntygFunktioner.sektion_diagnoser_för_sjukdom(intygsdata, intygstyp);

    // ----- Sektion 'Bakgrund - beskriv kortfattat förloppet för aktuella sjukdomar' ----- //
    cy.get(intygsdata.bakgrundSjukdomsförloppTextfältId).type(intygsdata.bakgrundSjukdomsförloppTextSkriv);

    // ----- Sektion 'Funktionsnedsättning - beskriv undersökningsfynd och ...' ----- //
    maxIntygFunktioner.sektion_funktionsnedsättning(intygsdata);

    // ----- Sektion 'Aktivitetsbegränsning' ----- //
    maxIntygFunktioner.sektion_aktivitetsbegränsningar(intygsdata);

    // ----- Sektion 'Medicinsk behandling' ----- //
    maxIntygFunktioner.sektion_medicinsk_behandling(intygsdata, intygstyp);

    // ----- Sektion 'Medicinska förutsättningar för arbete' ----- //
    maxIntygFunktioner.sektion_medicinska_förutsättningar_för_arbete(intygsdata, intygstyp);

    // ----- Sektion 'Övriga upplysningar' -----//
    maxIntygFunktioner.sektion_övriga_upplysningar(intygsdata, intygstyp);

    // ----- Sektion 'Kontakt' -----//
    maxIntygFunktioner.sektion_kontakta_mig(intygsdata);

    // ----- Sektion 'Signera intyg' -----//
    maxIntygFunktioner.sektion_signera_intyg(intygsdata);

    // Skicka iväg intyget
    maxIntygFunktioner.skicka_till_FK(intygsdata);
});


Cypress.Commands.add("fyllIMaxLisjp", aliasesFromCaller => {

    const intygsdata = aliasesFromCaller.lisjpData;
    expect(intygsdata).to.exist;

    const intygstyp = implementeradeIntyg.LISJP;

    // Beräkna datum både framåt och bakåt från idag
    const idagPlus1 = Cypress.moment().add(1,  'days').format('YYYY-MM-DD'); // 25%  sjukskrivning start
    const idagPlus11 = Cypress.moment().add(11, 'days').format('YYYY-MM-DD'); // 25%  sjukskrivning slut
    const idagPlus12 = Cypress.moment().add(12, 'days').format('YYYY-MM-DD'); // 50%  sjukskrivning start
    const idagPlus19 = Cypress.moment().add(19, 'days').format('YYYY-MM-DD'); // 50%  sjukskrivning slut
    const idagPlus20 = Cypress.moment().add(20, 'days').format('YYYY-MM-DD'); // 75%  sjukskrivning start
    const idagPlus28 = Cypress.moment().add(28, 'days').format('YYYY-MM-DD'); // 75%  sjukskrivning slut
    const idagPlus29 = Cypress.moment().add(29, 'days').format('YYYY-MM-DD'); // 100% sjukskrivning start
    const idagPlus41 = Cypress.moment().add(41, 'days').format('YYYY-MM-DD'); // 100% sjukskrivning slut

    // ----- Sektion 'Grund för medicinskt underlag' -----
    maxIntygFunktioner.sektion_grund_för_medicinskt_underlag(intygsdata, intygstyp);

    // ----- 'I relation till vilken sysselsättning bedömer du arbetsförmågan?' -----
    cy.contains(intygsdata.nuvarandeArbete).parent().within(($form) => {
        cy.get('[type="checkbox"]').check();
    });

    cy.contains(intygsdata.yrkeOchUppgifter).parent().parent().parent().within(($form) => {
        cy.wrap($form).find('textarea').type(intygsdata.yrkeOchUppgifterText);
    });

    cy.contains(intygsdata.checkboxTextNormaltFörekommandeJobb).parent().within(($form) => {
        cy.get('[type="checkbox"]').check();
    });

    cy.contains(intygsdata.checkboxTextVAB).parent().within(($form) => {
        cy.get('[type="checkbox"]').check();
    });

    cy.contains(intygsdata.checkboxStudier).parent().within(($form) => {
        cy.get('[type="checkbox"]').check();
    });

    // ----- Sektion 'Diagnos/Diagnoser för sjukdom som orsakar nedsatt arbetsförmåga' ----- //
    maxIntygFunktioner.sektion_diagnoser_för_sjukdom(intygsdata, intygstyp);

    // ----- Sektion 'Funktionsnedsättning' ----- //
    cy.contains(intygsdata.beskrivObservationer).parent().parent().parent().within(($form) => {
        cy.wrap($form).find('textarea').type(intygsdata.besvärsBeskrivning);
    });

    // ----- Sektion 'Aktivitetsbegränsning' ----- //
    maxIntygFunktioner.sektion_aktivitetsbegränsningar(intygsdata);

    // ----- Sektion 'Medicinsk behandling' ----- //
    maxIntygFunktioner.sektion_medicinsk_behandling(intygsdata, intygstyp);

    // ----- Sektion 'Bedömning' -----//
    cy.contains(intygsdata.arbetsförmågaBedömning).parent().parent().parent().within(($form) => {
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
    .contains(intygsdata.längreNedsattArbetsförmåga)
    .parent().parent().parent()
    .find('textarea').type(intygsdata.längreNedsattArbetsförmågaText);

    cy.contains(intygsdata.förläggaArbetstidAnnorlunda)
    .parent().parent().parent().within(($elem) => {
        cy.get('[type="radio"]').eq(0).check(); // Första radioknappen är "Ja"
    });

    cy.contains(intygsdata.arbetstidAnnorlundaMedicinskaSkäl).parent().parent().parent().find('textarea')
    .type(intygsdata.arbetstidAnnorlundaMedicinskaSkälBeskrivning);

    cy.contains(intygsdata.resaMöjliggörArbete)
    .parent().parent().within(() => {
        cy.get('[type="checkbox"]').check();
    });

    cy.contains(intygsdata.arbetsförmågaPrognos).parent().parent().parent().within(($ele) => {
        cy.get('[type="radio"]').eq(0).check() // Översta radioknappen är den som ska anges
    });

    // ----- Sektion 'Åtgärder' -----//
    cy.contains(intygsdata.föreslåÅtgärder).parent().parent().parent().within(($elem) => {
        cy.get('[type="checkbox"]').each(($el, index, $list) => {
            if (index != 0) { // Index 0 är "Inte aktuellt", detta är den enda checkboxen som INTE ska anges
                cy.wrap($el).check();
            }
        });
    });

    cy.wait(3000);

    cy.contains(intygsdata.flerÅtgärder).should('be.visible').parent().parent().parent().find('textarea')
        .type(intygsdata.flerÅtgärderBeskrivning);

    // ----- Sektion 'Övriga upplysningar' -----//
    maxIntygFunktioner.sektion_övriga_upplysningar(intygsdata, intygstyp);

    // ----- Sektion 'Kontakt' -----//
    maxIntygFunktioner.sektion_kontakta_mig(intygsdata);

    // ----- Sektion 'Signera intyg' -----//
    maxIntygFunktioner.sektion_signera_intyg(intygsdata);

    // Välj intygsmottagare
    cy.contains('div', intygsdata.väljIntygsmottagare, { timeout: 20000 })
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
            cy.wrap($button).contains(intygsdata.intygsmottagareKnappText).should('be.visible');
            cy.wrap($button).click({ force: true });
        });
    });

    // Skicka iväg intyget
    maxIntygFunktioner.skicka_till_FK(intygsdata);
});
