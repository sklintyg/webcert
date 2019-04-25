// Intyget är uppdelat på samma sätt som det är uppdelat när det fylls i genom WebCert

// Datumen är inte specifika för något testfall

// -------------------- 'Smittbärarpenning' --------------------
// Ej implementerad än

// -------------------- 'Grund för medicinskt underlag' --------------------
export function sektionGrundFörMedicinsktUnderlag(medUnderlag) {
    const idagMinus5  = Cypress.moment().subtract(5,  'days').format('YYYY-MM-DD');
    const idagMinus6  = Cypress.moment().subtract(6,  'days').format('YYYY-MM-DD');
    const idagMinus14 = Cypress.moment().subtract(14, 'days').format('YYYY-MM-DD');
    const idagMinus15 = Cypress.moment().subtract(15, 'days').format('YYYY-MM-DD');

    if (medUnderlag.intygetÄrBaseratPå) {
        const intygBaserat = medUnderlag.intygetÄrBaseratPå;
        if (intygBaserat.minUndersökning) {
            // TODO: Lägger väldigt lång timeout vid första elementet i intyget eftersom
            // sidan ibland inte har hunnit ladda innan den får timeout.
            // Initial analys är att Jenkins är överbelastad då det verkar fungera bra när
            // man kör lokalt.
            cy.get('#checkbox_undersokningAvPatienten', {timeout: 60000}).check();
            cy.get("#datepicker_undersokningAvPatienten").clear().type(idagMinus5);
        }

        if (intygBaserat.minTelefonkontakt) {
            cy.get('#checkbox_telefonkontaktMedPatienten').check();
            cy.get("#datepicker_telefonkontaktMedPatienten").clear().type(idagMinus6);
        }

        if (intygBaserat.journaluppgifter) {
            cy.get('#checkbox_journaluppgifter').check();
            cy.get("#datepicker_journaluppgifter").clear().type(idagMinus15);
        }


        if (intygBaserat.annat) {
            cy.get('#checkbox_annatGrundForMU').check();
            cy.get("#datepicker_annatGrundForMU").clear().type(idagMinus14);

            // cy.type() tar bara in text eller nummer (så vi behöver inte verifiera värdet)
            cy.get("#annatGrundForMUBeskrivning").type(intygBaserat.annatText);
        }
    }
}

// -------------------- 'Sysselsättning' --------------------
export function sektionSysselsättning(sysselsättning) {
    if (sysselsättning.iRelationTillVilkenSysselsättningBedömerDuArbetsförmågan) {
        const relation = sysselsättning.iRelationTillVilkenSysselsättningBedömerDuArbetsförmågan;
        if (relation.nuvarandeArbete) {
            cy.get('#sysselsattning-NUVARANDE_ARBETE').check();
            cy.get('#nuvarandeArbete').type(relation.yrkeOchArbetsuppgifter);
        }

        if (relation.arbetssökande) {
            cy.get('#sysselsattning-ARBETSSOKANDE').check();
        }

        if (relation.föräldraledighet) {
            cy.get('#sysselsattning-FORALDRALEDIG').check();
        }

        if (relation.studier) {
            cy.get('#sysselsattning-STUDIER').check();
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

// -------------------- 'Sjukdomens konsekvenser för patienten' --------------------
export function sektionSjukdomensKonsekvenserFörPatienten(konsekvenser) {
    cy.get('#funktionsnedsattning').type(konsekvenser.funktionsnedsättning);
    cy.get("#aktivitetsbegransning").type(konsekvenser.aktivitetsbegränsning);
}

// -------------------- 'Medicinsk behandling' --------------------
export function sektionMedicinskBehandling(medBehandling) {
    // ToDo: Är båda dessa mandatory eller ska de omslutas av if-satser?
    cy.get("#pagaendeBehandling").type(medBehandling.pågåendeBehandling);
    cy.get("#planeradBehandling").type(medBehandling.planeradeBehandling);
}

// -------------------- 'Bedömning' --------------------
export function sektionBedömning(bedömning) {
    // Beräkna datum både framåt och bakåt från idag
    const idagPlus1  = Cypress.moment().add(1,  'days').format('YYYY-MM-DD'); // 25%  sjukskrivning start
    const idagPlus11 = Cypress.moment().add(11, 'days').format('YYYY-MM-DD'); // 25%  sjukskrivning slut
    const idagPlus12 = Cypress.moment().add(12, 'days').format('YYYY-MM-DD'); // 50%  sjukskrivning start
    const idagPlus19 = Cypress.moment().add(19, 'days').format('YYYY-MM-DD'); // 50%  sjukskrivning slut
    const idagPlus20 = Cypress.moment().add(20, 'days').format('YYYY-MM-DD'); // 75%  sjukskrivning start
    const idagPlus28 = Cypress.moment().add(28, 'days').format('YYYY-MM-DD'); // 75%  sjukskrivning slut
    const idagPlus29 = Cypress.moment().add(29, 'days').format('YYYY-MM-DD'); // 100% sjukskrivning start
    const idagPlus41 = Cypress.moment().add(41, 'days').format('YYYY-MM-DD'); // 100% sjukskrivning slut

    const nedsättningArbetsförmåga = bedömning.minBedömningAvPatientensNedsättningAvArbetsförmågan;
    expect(nedsättningArbetsförmåga).to.exist;
    if (nedsättningArbetsförmåga.enFjärdedel) {
        cy.get('#sjukskrivningar-EN_FJARDEDEL-from').type(idagPlus1);
        cy.get('#sjukskrivningar-EN_FJARDEDEL-tom').type(idagPlus11).type('{enter}');
    }

    if (nedsättningArbetsförmåga.hälften) {
        cy.get('#sjukskrivningar-HALFTEN-from').type(idagPlus12);
        cy.get('#sjukskrivningar-HALFTEN-tom').type(idagPlus19).type('{enter}');
    }

    if (nedsättningArbetsförmåga.treFjärdedel) {
        cy.get('#sjukskrivningar-TRE_FJARDEDEL-from').type(idagPlus20);
        cy.get('#sjukskrivningar-TRE_FJARDEDEL-tom').type(idagPlus28).type('{enter}');
    }

    if (nedsättningArbetsförmåga.hel) {
        cy.get('#sjukskrivningar-HELT_NEDSATT-from').type(idagPlus29);
        cy.get('#sjukskrivningar-HELT_NEDSATT-tom').type(idagPlus41).type('{enter}');
    }

    if (bedömning.längreNedsattArbetsförmåga) {
        cy.get('#forsakringsmedicinsktBeslutsstod').type(bedömning.längreNedsattArbetsförmåga);
    }

    if (bedömning.förläggaArbetstidOlika.ja) {
        cy.get('#arbetstidsforlaggningYes').check();
        cy.get('#arbetstidsforlaggningMotivering')
            .type(bedömning.förläggaArbetstidOlika.arbetstidsförläggningstext);
    }

    if (bedömning.resorTillOchFrånArbete) {
        cy.get('#arbetsresor').check();
    }

    const arbetsFörmågaAktuelltTillfälle = bedömning.prognosFörArbetsförmågaUtifrånAktuelltUndersökningstillfälle;
    if (arbetsFörmågaAktuelltTillfälle.återgåHeltEfterSjukskrivning) {
        cy.get('#prognos-STOR_SANNOLIKHET').check();
    }
}

// -------------------- 'Åtgärder' --------------------
export function sektionÅtgärder(åtgärder) {
    if (åtgärder.arbetsträning) {
        cy.get('#arbetslivsinriktadeAtgarder-ARBETSTRANING').check();
    }

    if (åtgärder.arbetsanpassning) {
        cy.get('#arbetslivsinriktadeAtgarder-ARBETSANPASSNING').check();
    }

    if (åtgärder.sökaNyttArbete) {
        cy.get('#arbetslivsinriktadeAtgarder-SOKA_NYTT_ARBETE').check();
    }

    if (åtgärder.besökPåArbetsplatsen) {
        cy.get('#arbetslivsinriktadeAtgarder-BESOK_ARBETSPLATS').check();
    }

    if (åtgärder.ergonomiskBedömning) {
        cy.get('#arbetslivsinriktadeAtgarder-ERGONOMISK').check();
    }

    if (åtgärder.hjälpmedel) {
        cy.get('#arbetslivsinriktadeAtgarder-HJALPMEDEL').check();
    }

    if (åtgärder.konflikthantering) {
        cy.get('#arbetslivsinriktadeAtgarder-KONFLIKTHANTERING').check();
    }

    if (åtgärder.kontaktMedFöretagshälsovård) {
        cy.get('#arbetslivsinriktadeAtgarder-KONTAKT_FHV').check();
    }

    if (åtgärder.omfördelningAvArbetsuppgifter)  {
        cy.get('#arbetslivsinriktadeAtgarder-OMFORDELNING').check();
    }

    if (åtgärder.övrigt) {
        cy.get('#arbetslivsinriktadeAtgarder-OVRIGA_ATGARDER').check();
    }

    if (åtgärder.flerÅtgärder) {
        cy.get('#arbetslivsinriktadeAtgarderBeskrivning').type(åtgärder.flerÅtgärder);
    }
}

// -------------------- 'Övriga upplysningar' --------------------
export function sektionÖvrigaUpplysningar(övrigaUpplysningar) {
    if (övrigaUpplysningar) {
        cy.get("#ovrigt").type(övrigaUpplysningar);
    }
}

// -------------------- 'Kontakt' --------------------
export function sektionKontakt(kontakt) {
    if (kontakt.jagÖnskarKontakt) {
        cy.get("#kontaktMedFk").check();
        if (kontakt.anledningKontaktMedFK) {
            cy.get("#anledningTillKontakt").type(kontakt.anledningKontaktMedFK);
        }
    }
}

// -------------------- 'Vårdenhetens adress' --------------------
// Ej implementerad

// -------------------- 'Signera intyget' --------------------
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

    // Välj intygsmottagare
    // TODO: Ger en utökad timeout då modalen i perioder inte hinner laddas. Detta bör ses över
    cy.get('#approve-receiver-SKANDIA-radio-no', {timeout: 20000}).check();
    cy.get('#save-approval-settings-btn').click();
}

// -------------------- 'Skicka intyget' --------------------
export function skickaTillFk() {
    cy.get("#sendBtn", { timeout: 60000 }).click();

    // Modal som dyker upp och frågar om man verkligen vill skicka
    cy.get("#button1send-dialog").click();
    cy.contains("Intyget är skickat till Försäkringskassan");
}
