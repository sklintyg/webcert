/* globals context cy */
/// <reference types="Cypress" />
import * as intyg from '../../support/FK_intyg/lisjpIntyg'
//// Testar komplettering på LISJP = Läkarintyg för sjukpenning, FK 7804

describe('Komplettera mellan olika enheter', function () {

    before(function() {
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdtagare/balanarNattjagare').as('vårdtagare');
        cy.fixture('vårdenheter/MottagningAlfaenheten').as('vårdenhetTvå');
        cy.fixture('vårdenheter/MottagningBetaEnheten').as('vårdenhetTre');
            
    });

    beforeEach(function() {
        cy.skapaLISJPIfylltUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-Förifyllt utkast med id " + utkastId + " skapat och används i testfallet");
            
        });
    });

    it('skapar ett maximalt ifylld LISJP och skickar det till FK', function () {
       //Besöker intyget som skapats på överliggande enheter från två underliggande enheter
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhetTvå);
        const önskadUrl2 = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhetTvå.id;
       
        intyg.besökÖnskadUrl(önskadUrl2, this.vårdpersonal, this.vårdenhetTvå, this.utkastId);
        intyg.loggaUtLoggaIn(this.vårdpersonal,this.vårdenhetTre);
        const önskadUrl3 = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhetTre.id;

        intyg.besökÖnskadUrl(önskadUrl3, this.vårdpersonal, this.vårdenhetTre, this.utkastId);
        //signerar intyget och skickar intyget till Försäkringskassan.
        cy.url().should('include', this.utkastId);
        intyg.signera();
        intyg.skickaTillFk();
     //Härifrån skulle jag vilja göra ett nytt test: it('skapar komplettering och testar om man kan gå in på intyget',function ())
        cy.skapaKomplettering(this);
        intyg.loggaUtLoggaIn(this.vårdpersonal,this.vårdenhetTre);
        intyg.besökÖnskadUrl(önskadUrl3, this.vårdpersonal, this.vårdenhetTre, this.utkastId);
        intyg.kompletteraLisjp('ej');
        intyg.loggaUtLoggaIn(this.vårdpersonal,this.vårdenhetTvå);
        intyg.besökÖnskadUrl(önskadUrl2, this.vårdpersonal, this.vårdenhetTvå, this.utkastId);
        intyg.kompletteraLisjp('kompl');
        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id;
        intyg.loggaUtLoggaIn(this.vårdpersonal,this.vårdenhet);
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);
        intyg.kompletteraLisjp('kompl');
        
    });
   
    
});
