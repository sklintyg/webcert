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
    cy.get("#funktionsnedsattningDebut").type(intygsdata.funktionsnedsättningDebut);
    cy.get("#funktionsnedsattningPaverkan").type(intygsdata.funktionsnedsättningPåverkan);

    // ----- Sektion 'Övriga upplysningar' -----//
    maxIntygFunktioner.sektion_övriga_upplysningar(intygsdata);

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
    cy.get('#sjukdomsforlopp').type(intygsdata.bakgrundSjukdomsförlopp);

    // ----- Sektion 'Funktionsnedsättning - beskriv undersökningsfynd och ...' ----- //
    maxIntygFunktioner.sektion_funktionsnedsättning(intygsdata);

    // ----- Sektion 'Aktivitetsbegränsning' ----- //
    maxIntygFunktioner.sektion_aktivitetsbegränsningar(intygsdata);

    // ----- Sektion 'Medicinsk behandling' ----- //
    maxIntygFunktioner.sektion_medicinsk_behandling(intygsdata, intygstyp);

    // ----- Sektion 'Medicinska förutsättningar för arbete' ----- //
    maxIntygFunktioner.sektion_medicinska_förutsättningar_för_arbete(intygsdata, intygstyp);

    // ----- Sektion 'Övriga upplysningar' -----//
    maxIntygFunktioner.sektion_övriga_upplysningar(intygsdata);

    // ----- Sektion 'Kontakt' -----//
    maxIntygFunktioner.sektion_kontakta_mig(intygsdata);

    // ----- Sektion 'Signera intyg' -----//
    maxIntygFunktioner.sektion_signera_intyg(intygsdata);

    // Skicka iväg intyget
    maxIntygFunktioner.skicka_till_FK(intygsdata);
});
