import * as fk from '../FK_intyg/fk_helpers';

// Intyget är uppdelat på samma sätt som det är uppdelat när det fylls i genom WebCert

// Datumen är inte specifika för något testfall

// De funktioner etc. som är gemensamma för alla intyg kan exporteras direkt
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
                cy.get('#nuvarandeArbete').type(relation.yrkeOchArbetsuppgifter);
            }
    
        }
    }
    // -------------------- 'Diagnos' --------------------
    export function sektionDiagnos(diagnos) {
        if(diagnos.formedlaDiagnos === true)
        {
            cy.get('#onskarFormedlaDiagnosYes').click();
        }
    
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
    
    // -------------------- 'Bedömning' --------------------
    export function sektionBedömning(bedömning) {
         cy.get('#sjukskrivningsgrad').type('100');
          // Beräkna datum 
        const idagPlus1  = Cypress.moment().add(1,  'days').format('YYYY-MM-DD'); // 25%  sjukskrivning start
        const idagPlus11 = Cypress.moment().add(11, 'days').format('YYYY-MM-DD'); // 25%  sjukskrivning slut
        cy.get('#datepicker_sjukskrivningsperiod\\.from').type(idagPlus1);
        cy.get('#datepicker_sjukskrivningsperiod\\.tom').type(idagPlus11).type('{enter}');
      
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
                    url: 'moduleapi/intyg/ag114/' + intygsId + "/pdf",
                });
                cy.log('Skriver ut ett ' + typAvUtskrift+ ' intyg (via cy.request, ej grafiskt)');
                break;
            case "minimalt":
                cy.request({
                    method: 'GET',
                    url: 'moduleapi/intyg/ag114/' + intygsId + "/pdf/arbetsgivarutskrift",
                })
                cy.log('Skriver ut ett minimalt intyg (via cy.request, ej grafiskt)');
                break;
            default:
                cy.log('Ingen korrekt typ av utskrift vald');
        }
    }
    //-------------'Övriga Upplysningar'----------------------------        
    export function sektionÖvrigaUpplysningar(övrigt) {
        cy.get("#ovrigaUpplysningar").type(övrigt.text);
    }
    //-----------------'Kontakt med Arbetsgivaren'------------------
    export function sektionKontaktArbetsgivaren(kontakt) {
        if (kontakt.ja) {
            cy.get("#kontaktMedArbetsgivaren").check();
    
            if (kontakt.text) {
                cy.get("#anledningTillKontakt").type(kontakt.text);
            }
        }
    }
    //---------------'Arbetsförmåga'------------------------------
    export function sektionArbetsförmåga(arbetsförmåga){
        cy.get('#nedsattArbetsformaga').type(arbetsförmåga.text);
        if(arbetsförmåga.ja){
            cy.get('#arbetsformagaTrotsSjukdomYes').click();
            cy.get('#arbetsformagaTrotsSjukdomBeskrivning').type(arbetsförmåga.beskrivning)
        }
        else{
            cy.get('#arbetsformagaTrotsSjukdomNo').click();
        }
    }
