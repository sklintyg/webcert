/* globals context cy */
/// <reference types="Cypress" />
import * as intyg from '../../../support/SKR_intyg/AG114intyg'
import * as pdl from '../../../support/pdl_helpers'

// AG114 = Läkarintyg om arbetsförmåga – arbetsgivaren, AG 114

var pdlEventArray = [];

function ag114PdlEvent(env, actType, actArgs, actLevel, assignment, vgId_mod, vgNamn_mod, veId_mod, veNamn_mod) {
    return pdl.pdlEvent(env, actType, actArgs, actLevel, env.vårdpersonal.hsaId, assignment, env.vårdpersonal.titel, vgId_mod, vgNamn_mod, veId_mod, 
        veNamn_mod, env.vårdtagare.personnummerKompakt, env.vårdenhet.vårdgivareId, env.vårdenhet.vårdgivareNamn, env.vårdenhet.id, env.vårdenhet.namn)   
};

describe('Läkarintyg om arbetsförmåga – sjuklöneperioden – arbetsgivaren PDL loggning', function () {
    
    before(function() {
        cy.fixture('SKR_intyg/maxAG114Data').as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/nmt_vg2_ve1').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').as('vårdtagare');
    });

    beforeEach(function() {
        pdlEventArray = [];
        cy.skapaAG114Utkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("AG114-utkast med id " + utkastId + " skapat och används i testfallet");
            pdlEventArray.push(ag114PdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en maximalt ifylld AG114', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        pdlEventArray.push(ag114PdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag);
        intyg.sektionSysselsättning(this.intygsdata.sysselsättning);
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        pdlEventArray.push(ag114PdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        intyg.loggaUtLoggaIn(this.vårdpersonal, this.vårdenhet);
        cy.visit(önskadUrl);
        cy.url().should('include', this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i resten av intyget
        pdlEventArray.push(ag114PdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        //intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten);
        intyg.sektionBedömning(this.intygsdata.bedömning);
       // intyg.sektionÅtgärder(this.intygsdata.åtgärder);
        pdlEventArray.push(ag114PdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Signerar intyget och populerar pdl-arrayen med förväntade logposter "Signera" och "Läsa"
        intyg.sektionArbetsförmåga(this.intygsdata.arbetsförmåga);
        intyg.signera();
        pdlEventArray.push(ag114PdlEvent(this, pdl.enumHandelse.SIGNERA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        pdlEventArray.push(ag114PdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        
        // Introducerar en wait då skrivUt går så fort att man riskerar att få samma timestamp som för "skicka"
        cy.wait(1500);

        // Skriver ut intyget samt populerar pdl-arrayen med förväntad logpost "Utskrift"
        intyg.skrivUt("fullständigt", this.utkastId);
        pdlEventArray.push(ag114PdlEvent(this, pdl.enumHandelse.UTSKRIFT, pdl.enumHandelseArgument.UTSKRIFT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        cy.log("Testar SJF");
        intyg.loggaUtLoggaIn(this.vårdpersonal, this.vårdenhet_2);
        const sjfUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet_2.id + "&sjf=true";
        cy.visit(sjfUrl);

        // Om vi inte väntar på (valfritt) elementet nedan i intyget
        // så kommer "utskrift" att inträffa före "läsa"
        cy.contains("Grund för medicinskt underlag"); // Vänta på att intyget ska laddas färdigt

        cy.url().should('include', this.utkastId);
        pdlEventArray.push(ag114PdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.LÄSASJF, this.utkastId, this.vårdenhet_2.uppdragsnamn, this.vårdenhet_2.vårdgivareId, this.vårdenhet_2.vårdgivareNamn, this.vårdenhet_2.id, this.vårdenhet_2.namn));
        cy.log(this.utkastId + this.vårdenhet_2.vårdgivareId + this.vårdenhet_2.vårdgivareNamn + this.vårdenhet_2.id + this.vårdenhet_2.namn);

        cy.log("Testar återigen utan SJF");
        intyg.loggaUtLoggaIn(this.vårdpersonal, this.vårdenhet);
        cy.visit(önskadUrl);
        cy.contains("Grund för medicinskt underlag"); // Vänta på att intyget ska laddas färdigt
        pdlEventArray.push(ag114PdlEvent(this, "Läsa", undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: Bug?! Varför blir det 2 "Läsa" på rad?
        pdlEventArray.push(ag114PdlEvent(this, "Läsa", undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        
        cy.url().should('include', this.utkastId);
       
        intyg.makuleraIntyg();
        pdlEventArray.push(ag114PdlEvent(this, pdl.enumHandelse.MAKULERA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
       
       
        // Skapa nytt intyg-> utkast skapas. Populerar pdl-arrayen med förväntade logposter "Skriva" och "Läsa" samt nytt intygsID.
        cy.skapaAG114Utkast(this).then((utkastId2) => {
            cy.wrap(utkastId2).as('utkastId2');
            cy.log("AG114-utkast med id " + utkastId2 + " skapat och används i testfallet");
            pdlEventArray.push(ag114PdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId2, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
            intyg.skrivUt("fullständigt", utkastId2);
            pdlEventArray.push(ag114PdlEvent(this, pdl.enumHandelse.UTSKRIFT, pdl.enumHandelseArgument.UTSKRIFTUTKAST, utkastId2, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
            intyg.loggaUtLoggaIn(this.vårdpersonal, this.vårdenhet);
            const önskadUrl2 = "/visa/intyg/" + utkastId2 + "?enhet=" + this.vårdenhet.id
           
            intyg.besökÖnskadUrl(önskadUrl2, this.vårdpersonal, this.vårdenhet, utkastId2);
            pdlEventArray.push(ag114PdlEvent(this, pdl.enumHandelse.LÄSA, undefined, utkastId2, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
            intyg.raderaUtkast();
            cy.get('@utkastId2').then((utkastId2)=> {
                pdlEventArray.push(ag114PdlEvent(this, pdl.enumHandelse.RADERA, undefined, utkastId2, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
                
            });
        });
        
        cy.verifieraPdlLoggar(pdlEventArray);
    });
});

