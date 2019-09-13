// Intyget är uppdelat på samma sätt som det är uppdelat när det fylls i genom WebCert

// Datumen är inte specifika för något testfall

// -------------------- 'Smittbärarpenning' --------------------
// Ej implementerad än

// -------------------- 'Kompletterande patientuppgifter' --------------------
export function sektionPatientuppgifter(patientuppgifter) {
    
    cy.get('#identitetStyrkt').type(patientuppgifter.identitet);
    if (patientuppgifter.land.ja) {
        cy.get('#land').type(patientuppgifter.land.land);
    }
}

// -------------------- 'Dödsdatum och dödsplats' --------------------
export function sektionDödsdatumDödsplats(dödsuppgifter) {
    
    if (dödsuppgifter.dödsdatum.säkert) {
        cy.get('#dodsdatumSakertYes').check();
        const igår = Cypress.moment().subtract(1,  'days').format('YYYY-MM-DD');
        cy.get('#datepicker_dodsdatum').type(igår);
    } else {
        cy.get('#dodsdatumSakertNo').check();
        cy.get('#dodsdatum-year-selected-item-label').click();
        const dettaÅr = Cypress.moment().format('YYYY')
        cy.get('#dodsdatum-year-' + dettaÅr + ' > span').click();
        cy.get('#dodsdatum-month-selected-item-label').click();
        cy.get('#dodsdatum-month-00 > span').click();
        const igår = Cypress.moment().subtract(1,  'days').format('YYYY-MM-DD');
        cy.get('#datepicker_antraffatDodDatum').type(igår);
    }

    cy.get('#dodsplatsKommun').type(dödsuppgifter.dödsplats);

    if (dödsuppgifter.påträffades.sjukhus) {
        cy.get('#dodsplatsBoende-SJUKHUS').check();
    }
    if (dödsuppgifter.påträffades.ordinärt) {
        cy.get('#dodsplatsBoende-ORDINART_BOENDE').check();
    }
    if (dödsuppgifter.påträffades.särskilt) {
        cy.get('#dodsplatsBoende-SARSKILT_BOENDE').check();
    }
    if (dödsuppgifter.påträffades.okänd) {
        cy.get('#dodsplatsBoende-ANNAN').check();
    }
}

// -------------------- 'Barn som avlidigt senast 28 dygn efter födseln' --------------------
export function sektionBarnAvlidigt(avlidit28dagar) {
    
    cy.get('#barnYes').then((ele) => {
        cy.log('Är radioknapparna disable:ade för 28 dygn frågan: ' + ele[0].disabled);
        if (!ele[0].disabled) {
            if (avlidit28dagar.ja) {
                cy.get('#barnYes').check();
            } else {
                cy.get('#barnNo').check();
            }
        }
    });
}

// -------------------- 'Läkarens utlåtande om dödsorsaken' --------------------
export function utlatandeOmDödsorsaken(dödsorsak) {
    
    var dodsOrsakField = {
        1: {
            "beskrivning": '#orsak--beskrivning',
            "beskrivningText": dödsorsak.BeskrivningA,
            "debut": '#orsak--datum',
            "tillståndDropDown": '#orsak--specifikation-selected-item-label',
            "tillstånd": '#orsak--specifikation-PLOTSLIG'
        },
        2: {
            "beskrivning": '#orsak-0-beskrivning',
            "beskrivningText": dödsorsak.BeskrivningB,
            "debut": '#orsak-0-datum',
            "tillståndDropDown": '#orsak-0-specifikation-selected-item-label',
            "tillstånd": '#orsak-0-specifikation-KRONISK'
        },
        3: {
            "beskrivning": '#orsak-1-beskrivning',
            "beskrivningText": dödsorsak.BeskrivningC,
            "debut": '#orsak-1-datum',
            "tillståndDropDown": '#orsak-1-specifikation-selected-item-label',
            "tillstånd": '#orsak-1-specifikation-UPPGIFT_SAKNAS'
        },
        4: {
            "beskrivning": '#orsak-2-beskrivning',
            "beskrivningText": dödsorsak.BeskrivningD,
            "debut": '#orsak-2-datum',
            "tillståndDropDown": '#orsak-2-specifikation-selected-item-label',
            "tillstånd": '#orsak-2-specifikation-KRONISK'
        }
    }

    const idagMinus10Dagar  = Cypress.moment().subtract(10,  'days').format('YYYY-MM-DD');

    for (var i=1; i <= dödsorsak.antal; i++) {
        cy.get(dodsOrsakField[i].beskrivning).type(dodsOrsakField[i].beskrivningText);
        cy.get(dodsOrsakField[i].debut).type(idagMinus10Dagar);
        cy.get(dodsOrsakField[i].tillståndDropDown).click();
        cy.get(dodsOrsakField[i].tillstånd).click();
    };

    

    if (dödsorsak.sjukdommar.antal > 0) {
        for (var i = 1; i <= dödsorsak.sjukdommar.antal; i++) {
            if ( 0 < dödsorsak.sjukdommar.antal && dödsorsak.sjukdommar.antal <= 8) {
                switch(i) {
                    case 1:
                        cy.get('#orsak-multi-0-beskrivning').type(dödsorsak.sjukdommar.sjukdomEtt);
                        cy.get('#orsak-multi-0-datum').type(idagMinus10Dagar);
                        cy.get('#orsak-multi-0-specifikation-selected-item-label').click();
                        cy.get('#orsak-multi-0-specifikation-KRONISK').click();
                        break;
                    case 2:
                        cy.contains('Lägg till sjukdom/skada').click();
                        cy.get('#orsak-multi-1-beskrivning').type(dödsorsak.sjukdommar.sjukdomTvå);
                        cy.get('#orsak-multi-1-datum').type(idagMinus10Dagar);
                        cy.get('#orsak-multi-1-specifikation-selected-item-label').click();
                        cy.get('#orsak-multi-1-specifikation-PLOTSLIG').click();
                        break;
                    case 3:
                        cy.contains('Lägg till sjukdom/skada').click();
                        cy.get('#orsak-multi-2-beskrivning').type(dödsorsak.sjukdommar.sjukdomTre);
                        cy.get('#orsak-multi-2-datum').type(idagMinus10Dagar);
                        cy.get('#orsak-multi-2-specifikation-selected-item-label').click();
                        cy.get('#orsak-multi-2-specifikation-UPPGIFT_SAKNAS').click();
                        break;
                    case 4:
                        cy.contains('Lägg till sjukdom/skada').click();
                        cy.get('#orsak-multi-3-beskrivning').type(dödsorsak.sjukdommar.sjukdomFyra);
                        cy.get('#orsak-multi-3-datum').type(idagMinus10Dagar);
                        cy.get('#orsak-multi-3-specifikation-selected-item-label').click();
                        cy.get('#orsak-multi-3-specifikation-KRONISK').click();
                        break;
                    case 5:
                        cy.contains('Lägg till sjukdom/skada').click();
                        cy.get('#orsak-multi-4-beskrivning').type(dödsorsak.sjukdommar.sjukdomFem);
                        cy.get('#orsak-multi-4-datum').type(idagMinus10Dagar);
                        cy.get('#orsak-multi-4-specifikation-selected-item-label').click();
                        cy.get('#orsak-multi-4-specifikation-PLOTSLIG').click();
                        break;
                    case 6:
                        cy.contains('Lägg till sjukdom/skada').click();
                        cy.get('#orsak-multi-5-beskrivning').type(dödsorsak.sjukdommar.sjukdomSex);
                        cy.get('#orsak-multi-5-datum').type(idagMinus10Dagar);
                        cy.get('#orsak-multi-5-specifikation-selected-item-label').click();
                        cy.get('#orsak-multi-5-specifikation-UPPGIFT_SAKNAS').click();
                        break;
                    case 7:
                        cy.contains('Lägg till sjukdom/skada').click();
                        cy.get('#orsak-multi-6-beskrivning').type(dödsorsak.sjukdommar.sjukdomSju);
                        cy.get('#orsak-multi-6-datum').type(idagMinus10Dagar);
                        cy.get('#orsak-multi-6-specifikation-selected-item-label').click();
                        cy.get('#orsak-multi-6-specifikation-KRONISK').click();
                        break;
                    case 8:
                        cy.contains('Lägg till sjukdom/skada').click();
                        cy.get('#orsak-multi-7-beskrivning').type(dödsorsak.sjukdommar.sjukdomÅtta);
                        cy.get('#orsak-multi-7-datum').type(idagMinus10Dagar);
                        cy.get('#orsak-multi-7-specifikation-selected-item-label').click();
                        cy.get('#orsak-multi-7-specifikation-PLOTSLIG').click();
                        break;
                }
            }
            
        }
    };
    
}

// -------------------- 'Opererad inom fyra veckor före döden' --------------------
export function OpereradInomFyraVeckor(operation) {
    const idagMinus10Dagar  = Cypress.moment().subtract(10,  'days').format('YYYY-MM-DD');
    if (operation.ja) {
        cy.get('#operation-JA').check();
        cy.get('#datepicker_operationDatum').type(idagMinus10Dagar);
        cy.get('#operationAnledning').type(operation.tillstånd);
    }
    if (operation.nej) {
        cy.get('#operation-NEJ').check();
    }
    if (operation.uppgiftSaknas) {
        cy.get('#operation-UPPGIFT_SAKNAS').check();
    }
}

// -------------------- 'Skada/förgiftning' --------------------
export function sektionSkadaFörgiftning(skada) {
    
    const idagMinus14Dagar  = Cypress.moment().subtract(14,  'days').format('YYYY-MM-DD');

    var orsaker = {
        "olycksfall": '#forgiftningOrsak-OLYCKSFALL',
        "självmord": '#forgiftningOrsak-SJALVMORD',
        "avsiktligt": '#forgiftningOrsak-AVSIKTLIGT_VALLAD',
        "oklart": '#forgiftningOrsak-OKLART'
    }

    if (skada.ja) {
        cy.get('#forgiftningYes').check();
        for (var key in skada.orsak) {
            if (skada.orsak[key]) {
                cy.get(orsaker[key]).check();
            }
        };
        cy.get('#datepicker_forgiftningDatum').type(idagMinus14Dagar);
        cy.get('#forgiftningUppkommelse').type(skada.beskrivning);
    } else {
        cy.get('#forgiftningNo').check();
    }
}

// -------------------- 'Dödsorsaksuppgifterna grundar sig på' --------------------
export function sektionDödsorsaksUppgifterna(dödsorsaksuppgifter) {
    
    var DOuppgifter = {
        "föreDöden": '#grunder-UNDERSOKNING_FORE_DODEN',
        "efterDöden": '#grunder-UNDERSOKNING_EFTER_DODEN',
        "kliniskObduktion": '#grunder-KLINISK_OBDUKTION',
        "obduktion": '#grunder-RATTSMEDICINSK_OBDUKTION',
        "likbesiktning": '#grunder-RATTSMEDICINSK_BESIKTNING'
    };

    
    for (var key in dödsorsaksuppgifter) {
        if (dödsorsaksuppgifter[key]) {
            cy.get(DOuppgifter[key]).check();
        }
    };  
}


// -------------------- 'Signera och skicka intyget' --------------------
export function signeraOchSkicka() {
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

    cy.contains("Intyget är skickat till Socialstyrelsen");
}

// -------------------- 'Skriv ut intyget' --------------------
export function skrivUt(typAvUtskrift, intygsId, typAvIntyg){
    switch(typAvUtskrift) {
        case "utkast":
        case "fullständigt":
            cy.request({
                method: 'GET',
                url: 'moduleapi/intyg/' + typAvIntyg +'/' + intygsId + "/pdf",
            });
            cy.log('Skriver ut ett ' + typAvUtskrift+ ' intyg (via cy.request, ej grafiskt)');
            break;
        default:
            cy.log('Ingen korrekt typ av utskrift vald');
    }
}
// ------------------'Ersätta intyg'---------------------------
export function ersatta() {
    cy.get('#ersattBtn').click();
    cy.get('#button1ersatt-dialog').click();
}

// ------------------'Radera utkast'--------------------------
export function raderaUtkast() {
    cy.get('#ta-bort-utkast').click();
    cy.get('#confirm-draft-delete-button').click();   
}

// ------------------'Makulera intyg'-------------------------
export function makuleraIntyg() {
    cy.get('#makuleraBtn').click();
    cy.get('#button1makulera-dialog').click();
}