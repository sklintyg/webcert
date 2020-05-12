import * as fk from '../FK_intyg/fk_helpers';

// Intyget är uppdelat på samma sätt som det är uppdelat när det fylls i genom WebCert

// Datumen är inte specifika för något testfall

// De funktioner etc. som är gemensamma för många intyg kan exporteras direkt
export {besökÖnskadUrl, loggaUtLoggaIn, sektionÖvrigt, sektionKontakt,
        fornya, raderaUtkast, makuleraIntyg,komplettera} from '../FK_intyg/fk_helpers';
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
        if(diagnos.formedlaDiagnos === true)
        {
            cy.get('#onskarFormedlaDiagnosYes').click();
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
       
    }
    
    // Raderar diagnoskoden på angiven rad (1-3)
    export function raderaDiagnoskod(rad) {
        assert.isTrue(rad >= 1 && rad <= 3);
    
        if (rad === 1) {
            cy.get('#diagnoseCode-0').parent().within(($diagnoskodrad) => {
                cy.get('#diagnoseCode-0').clear();
            });
        } else if (rad === 2) {
            cy.get('#diagnoseCode-1').parent().within(($diagnoskodrad) => {
                cy.get('#diagnoseCode-1').clear();
            });
        } else {
            cy.get('#diagnoseCode-2').parent().within(($diagnoskodrad) => {
                cy.get('#diagnoseCode-2').clear();
            });
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
    //-------------------- 'Bedömning Helt Nedsatt' --------------------
    export function sektionBedömning75Nedsatt(bedömning) {
        // Beräkna datum både framåt och bakåt från idag
        const idagPlus1  = Cypress.moment().add(1,  'days').format('YYYY-MM-DD'); //start
        const idagPlus11 = Cypress.moment().add(11, 'days').format('YYYY-MM-DD'); //slut
        const nedsättningArbetsförmåga = bedömning.minBedömningAvPatientensNedsättningAvArbetsförmågan;
        expect(nedsättningArbetsförmåga).to.exist;
        if (nedsättningArbetsförmåga.treFjärdedel) {
            cy.get('#sjukskrivningar-TRE_FJARDEDEL-from').type(idagPlus1);
            cy.get('#sjukskrivningar-TRE_FJARDEDEL-tom').type(idagPlus11).type('{enter}');
        }
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
           
            cy.log("100 % första datum" + idagPlus29);
            cy.get('#sjukskrivningar-HELT_NEDSATT-tom').type(idagPlus41).type('{enter}');
        }
    
        if (bedömning.längreNedsattArbetsförmåga) {
            cy.get('#forsakringsmedicinsktBeslutsstod').type(bedömning.längreNedsattArbetsförmåga);
        }
    
        if (bedömning.förläggaArbetstidOlika.ja) {
            cy.get('#arbetstidsforlaggningYes').check();
            cy.get('#arbetstidsforlaggningMotivering')
                .type(bedömning.förläggaArbetstidOlika.arbetstidsförläggningstext);
        } else {
            cy.get('#arbetstidsforlaggningNo').check();
        }
    
        if (bedömning.resorTillOchFrånArbete) {
            cy.get('#arbetsresor').check();
        }
    
        const arbetsFörmågaAktuelltTillfälle = bedömning.prognosFörArbetsförmågaUtifrånAktuelltUndersökningstillfälle;
        if (arbetsFörmågaAktuelltTillfälle.återgåHeltEfterSjukskrivning) {
            cy.get('#prognos-STOR_SANNOLIKHET').check();
        }
    }
     // -------------------- 'Del Av Bedömning' --------------------
     export function sektionDelAvBedömning(bedömning) {
        const nedsättningArbetsförmåga = bedömning.minBedömningAvPatientensNedsättningAvArbetsförmågan;
        expect(nedsättningArbetsförmåga).to.exist;
        
        if (bedömning.längreNedsattArbetsförmåga) {
            cy.get('#forsakringsmedicinsktBeslutsstod').type(bedömning.längreNedsattArbetsförmåga);
        }
    
        if (bedömning.förläggaArbetstidOlika.ja) {
            cy.get('#arbetstidsforlaggningYes').check();
            cy.get('#arbetstidsforlaggningMotivering')
                .type(bedömning.förläggaArbetstidOlika.arbetstidsförläggningstext);
        } else {
            cy.get('#arbetstidsforlaggningNo').check();
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
       /* if (åtgärder.inteAktuellt) {
            cy.get('#arbetslivsinriktadeAtgarder-EJ_AKTUELLT').check();
        }*/
        if (åtgärder.arbetsträning) {
            cy.get('#arbetslivsinriktadeAtgarder-ARBETSTRANING').check();
        }
    
        if (åtgärder.arbetsanpassning) {
            cy.get('#arbetslivsinriktadeAtgarder-ARBETSANPASSNING').check();
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
    
    // -------------------- 'Vårdenhetens adress' --------------------
    // Ej implementerad
    
    // -------------------- 'Signera intyget' --------------------
    export function signera() {
        fk.signera();
    
    }
    
    // -------------------- 'Skriv ut intyget' --------------------
    export function skrivUt(typAvUtskrift, intygsId){
        switch(typAvUtskrift) {
            case "utkast":
            case "fullständigt":
                cy.request({
                    method: 'GET',
                    url: 'moduleapi/intyg/ag7804/' + intygsId + "/pdf",
                });
                cy.log('Skriver ut ett ' + typAvUtskrift+ ' intyg (via cy.request, ej grafiskt)');
                break;
            case "minimalt":
                cy.request({
                    method: 'GET',
                    url: 'moduleapi/intyg/ag7804/' + intygsId + "/pdf/arbetsgivarutskrift",
                })
                cy.log('Skriver ut ett minimalt intyg (via cy.request, ej grafiskt)');
                break;
            default:
                cy.log('Ingen korrekt typ av utskrift vald');
        }
    }
    
 //-----------------'Kontakt med Arbetsgivaren'------------------
 export function sektionKontaktArbetsgivaren(kontakt) {
    
    if (kontakt.ja) {
        cy.get("#kontaktMedAg").check();

        if (kontakt.text) {
            cy.get("#anledningTillKontakt").type(kontakt.text);
        }
    }
}
//-----------------'Ifyllnadsstöd'------------------
export function ifyllnadsstod(){
        cy.contains('Det finns ett Läkarintyg för sjukpenning för denna patient').then((ele) => {
          
            if(ele.text().includes('Det finns ett Läkarintyg för sjukpenning för denna patient')){
                cy.get('#copy-from-candidate-dialog-button2').click();
                cy.log('Klickar på dialogen som kollar om det redan finns ett LISJP');
        } 
    });
}
