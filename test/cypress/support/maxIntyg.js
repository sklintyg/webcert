
// TODO: Få till detta som global istället,
// eventuellt via cypress.json. Finns även i commands.js
const implementeradeIntygEnum = {
    LISJP: "LISJP",
    LUSE: "LUSE",
    LUAE_NA: "LUAE_NA",
    LUAE_FS: "LUAE_FS",
}
const implementeradeIntygArray = Object.values(implementeradeIntygEnum);

/*
TODO: ID:n och text som identifierar element i intygen är duplicerade
i samtliga fixtures. Ska vi separera ID:n osv så att vi får en eller två
filer som innehåller dessa och sen har vi t.ex. texten som läkaren skriver
i andra filer? Ett annat alternativ är att ID:n skrivs ut direkt i koden
nedan så att endast texten som fylls i osv är specifik för intygen.
*/

function sektion_grund_för_medicinskt_underlag(intygsdata, intygstyp) {
    expect(intygsdata).to.exist;
    expect(implementeradeIntygArray).to.include.members([intygstyp]);

    /* TODO: Ta reda på svaret till nedanstående fråga.
    Samtliga datum är i första hand baserade på datum från LISJP-mallen från FK.
    I de fall där ett visst fält inte förekommer i LISJP så är datumet bara höftat.
    ÄR DET VIKTIGT MED "RÄTT" DATUM?
    */
    const idagMinus5 =  Cypress.moment().subtract(5,  'days').format('YYYY-MM-DD');
    const idagMinus6 =  Cypress.moment().subtract(6,  'days').format('YYYY-MM-DD');
    const idagMinus14 = Cypress.moment().subtract(14, 'days').format('YYYY-MM-DD');
    const idagMinus15 = Cypress.moment().subtract(15, 'days').format('YYYY-MM-DD');
    const idagMinus2Mån = Cypress.moment().subtract(2, 'months').format('YYYY-MM-DD');


    // -------------------- 'Intyget/utlåtandet är baserat på' --------------------
    // TODO: Ska vi använda oss av ID:n eller leta efter specifik text och sen navigera
    //       bland elementen med parentsUntil() osv?
    // Fördelar med ID:n: Enkel kod att skriva, enkel att läsa.
    // Nackdelar med ID:n: Så fort ett ID ändras "under huven" så kommer TC att faila
    //                     trots att ingen ändring som drabbar användaren har skett
    // Just nu används en blandning tills beslut är taget.
    cy.contains("Min undersökning av patienten").parentsUntil('.ue-del-fraga').within(($form) => {
        cy.get('[type="checkbox"]').check();
        cy.get("#datepicker_undersokningAvPatienten").clear().type(idagMinus5);
    });

    if (intygstyp === implementeradeIntygEnum.LISJP) {
        cy.contains("Min telefonkontakt med patienten").parentsUntil('.ue-del-fraga').within(($form) => {
            cy.get('[type="checkbox"]').check();
            cy.get("#datepicker_telefonkontaktMedPatienten").clear().type(idagMinus6);
        });
    }

    cy.contains("Journaluppgifter från den").parentsUntil('.ue-del-fraga').within(($form) => {
        cy.wrap($form).get('[type="checkbox"]').check();
        cy.wrap($form).get("#datepicker_journaluppgifter").clear().type(idagMinus15);
    });

    // Gäller ej LISJP
    if (intygstyp !== implementeradeIntygEnum.LISJP) {
        cy.contains("Anhörigs beskrivning av patienten").parentsUntil('.ue-del-fraga').within(($form) => {
            cy.get('[type="checkbox"]').check();
            cy.get("#datepicker_anhorigsBeskrivningAvPatienten").clear().type(idagMinus6);
        });
    }

    cy.contains("Annat").parentsUntil('.ue-del-fraga').within(($form) => {
        cy.get('[type="checkbox"]').check();
        cy.get("#datepicker_annatGrundForMU").clear().type(idagMinus14);
    });

    // Denna textruta dyker upp efter att "Annat" har klickats i
    cy.get("#annatGrundForMUBeskrivning").type(intygsdata.annatTextareaText);

    if (intygstyp === implementeradeIntygEnum.LUSE ||
        intygstyp === implementeradeIntygEnum.LUAE_NA ||
        intygstyp === implementeradeIntygEnum.LUAE_FS) {

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

function sektion_diagnoser_för_sjukdom(intygsdata, intygstyp) {
    cy.contains(intygsdata.diagnoserNedsattFörmåga).parent().parent().parent().within(($form) => {
        // Antag att ICD-10-SE är förvalt
        cy.get('[placeholder=Kod]').then(($codeFields) => {
            cy.wrap($codeFields.eq(0)).type(intygsdata.diagnosKod1).wait(1000).type('{enter}');
            cy.wrap($codeFields.eq(1)).type(intygsdata.diagnosKod2).wait(1000).type('{enter}');
            cy.wrap($codeFields.eq(2)).type(intygsdata.diagnosKod3).wait(1000).type('{enter}');
        });
    });

    if (intygstyp === implementeradeIntygEnum.LUSE ||
        intygstyp === implementeradeIntygEnum.LUAE_NA) {
        cy.get("#diagnosgrund").type(intygsdata.diagnosgrundTextSkriv);

        // Finns skäl att revidera tidigare diagnos?
        cy.get("#nyBedomningDiagnosgrundYes").click();
        cy.get("#diagnosForNyBedomning").type(intygsdata.revideraTidigareDiagnosSkriv);
    }
}

function sektion_aktivitetsbegränsningar(intygsdata) {
    cy.get("#aktivitetsbegransning").type(intygsdata.aktivitetsbegränsningSkriv);
}

function sektion_medicinsk_behandling(intygsdata, intygstyp) {
    if (intygstyp === implementeradeIntygEnum.LUSE ||
        intygstyp === implementeradeIntygEnum.LUAE_NA) {
        cy.get("#avslutadBehandling").type(intygsdata.avslutadBehandlingSkriv);
    }

    cy.get("#pagaendeBehandling").type(intygsdata.pågåendeBehandlingarSkriv);
    cy.get("#planeradBehandling").type(intygsdata.planeradeBehandlingarSkriv);

    if (intygstyp === implementeradeIntygEnum.LUSE ||
        intygstyp === implementeradeIntygEnum.LUAE_NA) {
        cy.get("#substansintag").type(intygsdata.substansintagSkriv);
    }
}

function sektion_medicinska_förutsättningar_för_arbete(intygsdata, intygstyp) {
    cy.get("#medicinskaForutsattningarForArbete").type(intygsdata.förutsättningarFörArbeteSkriv);
    cy.get("#formagaTrotsBegransning").type(intygsdata.förmågaTrotsBegränsningSkriv);
    if (intygstyp === implementeradeIntygEnum.LUAE_NA) {
        cy.get("#forslagTillAtgard").type(intygsdata.förslagTillÅtgärdSkriv);
    }
}

function sektion_övriga_upplysningar(intygsdata, intygstyp) {
    var textAttSkriva = intygsdata.övrigaUpplysningarSkriv;
    if (intygstyp === implementeradeIntygEnum.LISJP) {
        const idagPlus41 = Cypress.moment().add(41, 'days').format('YYYY-MM-DD');
        textAttSkriva += idagPlus41;
    }
    cy.get("#ovrigt").type(textAttSkriva);
}

function sektion_kontakta_mig(intygsdata) {
    cy.get("#kontaktMedFk").check();
    cy.get("#anledningTillKontakt").type(intygsdata.kontaktMedFKSkriv);
}

function sektion_signera_intyg(intygsdata) {
    // cy.click() fungerar inte alltid. Det finns issues rapporterade (stängd pga inaktivitet):
    // https://github.com/cypress-io/cypress/issues/2551
    // Nedanstående steg innan klicket på signera-knappen är en workaround för detta.
    cy.contains("Klart att signera");
    cy.contains("Obligatoriska uppgifter saknas").should('not.exist');
    cy.contains("Utkastet sparas").should('not.exist');
    cy.contains("Utkastet är sparat").should('not.exist');
    cy.get("#signera-utkast-button").invoke('width').should('be.greaterThan', 0);
    cy.get("#signera-utkast-button").should('not.be.disabled');
    cy.get("#signera-utkast-button").click();
}

function skicka_till_FK(intygsdata) {
    cy.get("#sendBtn", { timeout: 60000 }).click();
    cy.get("#button1send-dialog").click(); // Modal som dyker upp och frågar om man verkligen vill skicka
    // TODO: Kontrollera att texten "Intyget är skickat till Försäkringskassan"
    // syns på sidan?
}

function sektion_funktionsnedsättning(intygsdata) {
    cy.get(intygsdata.funknedsättningIntellektuellExpanderaId).click();
    cy.get(intygsdata.funknedsättningIntellektuellTextfältId).type(intygsdata.funknedsättningIntellektuellSkriv);

    cy.get(intygsdata.funknedsättningKommunikationExpanderaId).click();
    cy.get(intygsdata.funknedsättningKommunikationTextfältId).type(intygsdata.funknedsättningKommunikationSkriv);

    cy.get(intygsdata.funknedsättningUppmärksamhetExpanderaId).click();
    cy.get(intygsdata.funknedsättningUppmärksamhetTextfältId).type(intygsdata.funknedsättningUppmärksamhetSkriv);

    cy.get(intygsdata.funknedsättningPsykiskExpanderaId).click();
    cy.get(intygsdata.funknedsättningPsykiskTextfältId).type(intygsdata.funknedsättningPsykiskSkriv);

    cy.get(intygsdata.funknedsättningSinneExpanderaId).click();
    cy.get(intygsdata.funknedsättningSinneTextfältId).type(intygsdata.funknedsättningSinneSkriv);

    cy.get(intygsdata.funknedsättningBalansExpanderaId).click();
    cy.get(intygsdata.funknedsättningBalansTextfältId).type(intygsdata.funknedsättningBalansSkriv);

    cy.get(intygsdata.funknedsättningAnnanExpanderaId).click();
    cy.get(intygsdata.funknedsättningAnnanTextfältId).type(intygsdata.funknedsättningAnnanSkriv);
}

Cypress.Commands.add("fyllIMaxLuaeFs", aliasesFromCaller => {
    const intygsdata = aliasesFromCaller.luaeFsData;
    expect(intygsdata).to.exist;
    const intygstyp = implementeradeIntygEnum.LUAE_FS;

    // ----- Sektion 'Grund för medicinskt underlag' -----
    sektion_grund_för_medicinskt_underlag(intygsdata, intygstyp);

    // ----- Sektion 'Diagnos/Diagnoser för sjukdom som orsakar eller har orsakat funktionsnedsättning' ----- //
    sektion_diagnoser_för_sjukdom(intygsdata, intygstyp);

    // ----- Sektion 'Funktionsnedsättning' ----- //
    cy.get("#funktionsnedsattningDebut").type(intygsdata.funktionsnedsättningDebutSkriv);
    cy.get("#funktionsnedsattningPaverkan").type(intygsdata.funktionsnedsättningPåverkanSkriv);

    // ----- Sektion 'Övriga upplysningar' -----//
    sektion_övriga_upplysningar(intygsdata, intygstyp);

    // ----- Sektion 'Kontakt' -----//
    sektion_kontakta_mig(intygsdata);

    // ----- Sektion 'Signera intyg' -----//
    sektion_signera_intyg(intygsdata);

    // Skicka iväg intyget
    skicka_till_FK(intygsdata);
});

Cypress.Commands.add("fyllIMaxLuaeNa", aliasesFromCaller => {
    const intygsdata = aliasesFromCaller.luaeNaData;
    expect(intygsdata).to.exist;
    const intygstyp = implementeradeIntygEnum.LUAE_NA;

    // ----- Sektion 'Grund för medicinskt underlag' -----
    sektion_grund_för_medicinskt_underlag(intygsdata, intygstyp);

    // ----- Sektion 'Diagnos/Diagnoser för sjukdom som orsakar nedsatt arbetsförmåga' ----- //
    sektion_diagnoser_för_sjukdom(intygsdata, intygstyp);

    // ----- Sektion 'Bakgrund - beskriv kortfattat förloppet för aktuella sjukdomar' ----- //
    cy.get(intygsdata.bakgrundSjukdomsförloppTextfältId).type(intygsdata.bakgrundSjukdomsförloppTextSkriv);

    // ----- Sektion 'Funktionsnedsättning - beskriv undersökningsfynd och ...' ----- //
    sektion_funktionsnedsättning(intygsdata);

    // ----- Sektion 'Aktivitetsbegränsning' ----- //
    sektion_aktivitetsbegränsningar(intygsdata);

    // ----- Sektion 'Medicinsk behandling' ----- //
    sektion_medicinsk_behandling(intygsdata, intygstyp);

    // ----- Sektion 'Medicinska förutsättningar för arbete' ----- //
    sektion_medicinska_förutsättningar_för_arbete(intygsdata, intygstyp);

    // ----- Sektion 'Övriga upplysningar' -----//
    sektion_övriga_upplysningar(intygsdata, intygstyp);

    // ----- Sektion 'Kontakt' -----//
    sektion_kontakta_mig(intygsdata);

    // ----- Sektion 'Signera intyg' -----//
    sektion_signera_intyg(intygsdata);

    // Skicka iväg intyget
    skicka_till_FK(intygsdata);
});

Cypress.Commands.add("fyllIMaxLuse", aliasesFromCaller => {
    const intygsdata = aliasesFromCaller.luseData;
    expect(intygsdata).to.exist;
    const intygstyp = implementeradeIntygEnum.LUSE;

    // ----- Sektion 'Grund för medicinskt underlag' -----
    sektion_grund_för_medicinskt_underlag(intygsdata, intygstyp);

    // ----- Sektion 'Diagnos/Diagnoser för sjukdom som orsakar nedsatt arbetsförmåga' ----- //
    sektion_diagnoser_för_sjukdom(intygsdata, intygstyp);

    // ----- Sektion 'Bakgrund - beskriv kortfattat förloppet för aktuella sjukdomar' ----- //
    cy.get(intygsdata.bakgrundSjukdomsförloppTextfältId).type(intygsdata.bakgrundSjukdomsförloppTextSkriv);

    // ----- Sektion 'Funktionsnedsättning - beskriv undersökningsfynd och ...' ----- //
    sektion_funktionsnedsättning(intygsdata);

    // ----- Sektion 'Aktivitetsbegränsning' ----- //
    sektion_aktivitetsbegränsningar(intygsdata);

    // ----- Sektion 'Medicinsk behandling' ----- //
    sektion_medicinsk_behandling(intygsdata, intygstyp);

    // ----- Sektion 'Medicinska förutsättningar för arbete' ----- //
    sektion_medicinska_förutsättningar_för_arbete(intygsdata, intygstyp);

    // ----- Sektion 'Övriga upplysningar' -----//
    sektion_övriga_upplysningar(intygsdata, intygstyp);

    // ----- Sektion 'Kontakt' -----//
    sektion_kontakta_mig(intygsdata);

    // ----- Sektion 'Signera intyg' -----//
    sektion_signera_intyg(intygsdata);

    // Skicka iväg intyget
    skicka_till_FK(intygsdata);
});


Cypress.Commands.add("fyllIMaxLisjp", aliasesFromCaller => {

    const intygsdata = aliasesFromCaller.lisjpData;
    expect(intygsdata).to.exist;

    const intygstyp = implementeradeIntygEnum.LISJP;

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
    sektion_grund_för_medicinskt_underlag(intygsdata, intygstyp);

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
    sektion_diagnoser_för_sjukdom(intygsdata, intygstyp);

    // ----- Sektion 'Funktionsnedsättning' ----- //
    cy.contains(intygsdata.beskrivObservationer).parent().parent().parent().within(($form) => {
        cy.wrap($form).find('textarea').type(intygsdata.besvärsBeskrivning);
    });

    // ----- Sektion 'Aktivitetsbegränsning' ----- //
    sektion_aktivitetsbegränsningar(intygsdata);

    // ----- Sektion 'Medicinsk behandling' ----- //
    sektion_medicinsk_behandling(intygsdata, intygstyp);

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
    sektion_övriga_upplysningar(intygsdata, intygstyp);

    // ----- Sektion 'Kontakt' -----//
    sektion_kontakta_mig(intygsdata);

    // ----- Sektion 'Signera intyg' -----//
    sektion_signera_intyg(intygsdata);

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
    skicka_till_FK(intygsdata);
});
