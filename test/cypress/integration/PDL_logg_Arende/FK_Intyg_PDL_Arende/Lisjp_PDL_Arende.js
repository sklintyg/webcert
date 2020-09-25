/* globals context cy */
/// <reference types="Cypress" />
import * as intyg from '../../../support/FK_intyg/lisjpIntyg'
import * as pdl from '../../../support/pdl_helpers'
//import * as agIntyg from '../../../support/SKR_intyg/AG7804intyg'

// LISJP = Läkarintyg för sjukpenning, FK 7804

var pdlEventArray = [];

function lisjpPdlEvent(env, actType, actArgs, actLevel, assignment, vgId_mod, vgNamn_mod, veId_mod, veNamn_mod) {
    return pdl.pdlEvent(env, actType, actArgs, actLevel, env.vårdpersonal1.hsaId, assignment, env.vårdpersonal1.titel, vgId_mod, vgNamn_mod, veId_mod, 
        veNamn_mod, env.vårdtagare.personnummerKompakt, env.vårdenhet.vårdgivareId, env.vårdenhet.vårdgivareNamn, env.vårdenhet.id, env.vårdenhet.namn)   
};
    
describe('PDL loggning för Ärendehantering av LISJP-intyg', function () {
    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').as('intygsdata');
        cy.fixture('vårdpersonal/annikaLarsson').as('vårdpersonal');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal1');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdtagare/balanarNattjagare').as('vårdtagare');
        cy.skapaSigneratIntygWebcert(this).then((utkastId) =>{
        
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP förifyllt utkast med id " + utkastId + " skapat och används i testfallet");
           
        }); 
    });
    
   

    beforeEach(function() {
        pdlEventArray = [];
        
    });

    it('Skickar fråga på ett intyg', function () {
        // Signerar intyget och populerar pdl-arrayen med förväntade loggposter "Signera" och "Läsa"
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal1, this.vårdenhet);
        const önskadUrl1 = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id;
        intyg.besökÖnskadUrl(önskadUrl1, this.vårdpersonal1, this.vårdenhet, this.utkastId);
        cy.wait(1000);
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SIGNERA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.skickaTillFk();
        pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.UTSKRIFT, pdl.enumHandelseArgument.FKASSA, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Introducerar en wait då skrivUt går så fort att man riskerar att få samma timestamp som för "skicka"
        cy.wait(1500);
        

       

        cy.verifieraPdlLoggar(pdlEventArray);
    });


});
