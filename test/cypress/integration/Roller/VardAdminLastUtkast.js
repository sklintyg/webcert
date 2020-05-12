/* globals context cy */
/// <reference types="Cypress" />
import * as intyg from '../../support/FK_intyg/lisjpIntyg'

describe('Behörigheter för Vårdadmin gällande LISJP-intyg', function () {
    
    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').as('intygsdata');
        cy.fixture('vårdpersonal/annikaLarsson').as('vårdpersonal');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal1');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdtagare/balanarNattjagare').as('vårdtagare');
             
        });
        

    beforeEach(function() {
      
    });

    context('Vårdadmin kan utföra endast behöriga uppgifter på ett låst utkast i integrerat läge' , function() {
       
        it('Kan Läsa och Skriva ut låst utkast',function(){
           
            cy.skapaLåstIntygWebcert(this).then((utkastId) =>{
        
                cy.wrap(utkastId).as('utkastId');
                cy.log("LISJP förifyllt utkast med id " + utkastId + " skapat och används i testfallet");
                cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
                const önskadUrl1 = "/visa/intyg/" + utkastId + "?enhet=" + this.vårdenhet.id;
                intyg.besökÖnskadUrl(önskadUrl1, this.vårdpersonal, this.vårdenhet, utkastId);
                cy.wait(1000);
                cy.contains("Utkastet är låst").should('exist');
                intyg.skrivUt("fullständigt", utkastId);

            });        
        });           
        it('Kan inte makulera låst utkast',function(){
            cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
            const önskadUrl2 = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id;
          
            intyg.besökÖnskadUrl(önskadUrl2, this.vårdpersonal, this.vårdenhet, this.utkastId);
            cy.get('wc-utkast-button-bar').should('not.contain', 'Makulera');

          });
       it('Kan Kopiera låst utkast',function(){
            cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
            const önskadUrl3 = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id;
            intyg.besökÖnskadUrl(önskadUrl3, this.vårdpersonal, this.vårdenhet, this.utkastId);
            intyg.kopiera();
            cy.wait(500);
            cy.url().should('not.include', this.utkastId);           
           
        });        
      
    });
});
