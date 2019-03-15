import * as maxIntygFunktioner from './maxIntygFunktioner'
import {implementeradeIntyg} from './../commands'

Cypress.Commands.add("fyllIMaxLuaeFs", aliasesFromCaller => {
    const intygsdata = aliasesFromCaller.luaeFsData;
    expect(intygsdata).to.exist;
    const intygstyp = implementeradeIntyg.LUAE_FS;

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
    cy.get('#sjukdomsforlopp').type(intygsdata.bakgrundSjukdomsförloppTextSkriv);

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
    cy.get('#sjukdomsforlopp').type(intygsdata.bakgrundSjukdomsförloppTextSkriv);

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
    cy.get('#sysselsattning-NUVARANDE_ARBETE').check();
    cy.get('#sysselsattning-ARBETSSOKANDE').check();
    cy.get('#sysselsattning-FORALDRALEDIG').check();
    cy.get('#sysselsattning-STUDIER').check();
    cy.get('#nuvarandeArbete').type(intygsdata.yrkeOchUppgifterText);

    // ----- Sektion 'Diagnos/Diagnoser för sjukdom som orsakar nedsatt arbetsförmåga' ----- //
    maxIntygFunktioner.sektion_diagnoser_för_sjukdom(intygsdata, intygstyp);

    // ----- Sektion 'Funktionsnedsättning' ----- //
    cy.get('#funktionsnedsattning').type(intygsdata.besvärsBeskrivning);

    // ----- Sektion 'Aktivitetsbegränsning' ----- //
    maxIntygFunktioner.sektion_aktivitetsbegränsningar(intygsdata);

    // ----- Sektion 'Medicinsk behandling' ----- //
    maxIntygFunktioner.sektion_medicinsk_behandling(intygsdata, intygstyp);

    // ----- Sektion 'Bedömning' -----//
    // Fyll i datum
    cy.get('#sjukskrivningar-EN_FJARDEDEL-from').type(idagPlus1);
    cy.get('#sjukskrivningar-EN_FJARDEDEL-tom').type(idagPlus11).type('{enter}');
    // TODO: Ska det kontrolleras att checkboxen blir vald?

    cy.get('#sjukskrivningar-HALFTEN-from').type(idagPlus12);
    cy.get('#sjukskrivningar-HALFTEN-tom').type(idagPlus19).type('{enter}');

    cy.get('#sjukskrivningar-TRE_FJARDEDEL-from').type(idagPlus20);
    cy.get('#sjukskrivningar-TRE_FJARDEDEL-tom').type(idagPlus28).type('{enter}');

    cy.get('#sjukskrivningar-HELT_NEDSATT-from').type(idagPlus29);
    cy.get('#sjukskrivningar-HELT_NEDSATT-tom').type(idagPlus41).type('{enter}');

    cy.get('#forsakringsmedicinsktBeslutsstod').type(intygsdata.längreNedsattArbetsförmågaText);

    cy.get('#arbetstidsforlaggningYes').check();
    cy.get('#arbetstidsforlaggningMotivering').type(intygsdata.arbetstidAnnorlundaMedicinskaSkälBeskrivning);

    cy.get('#arbetsresor').check();
    cy.get('#prognos-STOR_SANNOLIKHET').check();

    // ----- Sektion 'Åtgärder' -----//
    cy.get('#arbetslivsinriktadeAtgarder-ARBETSTRANING').check();
    cy.get('#arbetslivsinriktadeAtgarder-ARBETSANPASSNING').check();
    cy.get('#arbetslivsinriktadeAtgarder-SOKA_NYTT_ARBETE').check();
    cy.get('#arbetslivsinriktadeAtgarder-BESOK_ARBETSPLATS').check();
    cy.get('#arbetslivsinriktadeAtgarder-ERGONOMISK').check();
    cy.get('#arbetslivsinriktadeAtgarder-HJALPMEDEL').check();
    cy.get('#arbetslivsinriktadeAtgarder-KONFLIKTHANTERING').check();
    cy.get('#arbetslivsinriktadeAtgarder-KONTAKT_FHV').check();
    cy.get('#arbetslivsinriktadeAtgarder-OMFORDELNING').check();
    cy.get('#arbetslivsinriktadeAtgarder-OVRIGA_ATGARDER').check();

    cy.get('#arbetslivsinriktadeAtgarderBeskrivning').should('be.visible').then((textfält) => {
        cy.wrap(textfält).type(intygsdata.flerÅtgärderBeskrivning);
    });

    // ----- Sektion 'Övriga upplysningar' -----//
    maxIntygFunktioner.sektion_övriga_upplysningar(intygsdata, intygstyp);

    // ----- Sektion 'Kontakt' -----//
    maxIntygFunktioner.sektion_kontakta_mig(intygsdata);

    // ----- Sektion 'Signera intyg' -----//
    maxIntygFunktioner.sektion_signera_intyg(intygsdata);

    // Välj intygsmottagare
    cy.get('#approve-receiver-SKANDIA-radio-no').check();
    cy.get('#save-approval-settings-btn').click();

    // Skicka iväg intyget
    maxIntygFunktioner.skicka_till_FK(intygsdata);
});
