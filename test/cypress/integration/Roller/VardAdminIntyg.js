/* globals context cy */
/// <reference types="Cypress" />
import * as intyg from '../../support/FK_intyg/lisjpIntyg'
import * as agIntyg from '../../support/SKR_intyg/AG7804intyg'

describe('Behörigheter för Vårdadmin gällande LISJP-intyg', function () {
    
    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').as('intygsdata');
        cy.fixture('SKR_intyg/maxAG7804Data').as('AGintygsdata');
        cy.fixture('vårdpersonal/annikaLarsson').as('vårdpersonal');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal1');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdtagare/balanarNattjagare').as('vårdtagare');
        cy.skapaSigneratIntygWebcert(this).then((utkastId) =>{
        
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP förifyllt utkast med id " + utkastId + " skapat och används i testfallet");
           
        }); 
    });       

    context('Vårdadmin kan utföra endast behöriga uppgifter på ett LISJP intyg i integrerat läge' , function() {

        describe('Vårdadmin och LISJP', () => {
            it('Kan Läsa och Skriva ut intyg',function(){
           
                cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
                const önskadUrl1 = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id;
                intyg.besökÖnskadUrl(önskadUrl1, this.vårdpersonal, this.vårdenhet, this.utkastId);
                cy.wait(1000);
                cy.contains("Intyget är signerat").should('exist');
                intyg.skrivUt("fullständigt", this.utkastId);

            });           
            it('Kan Skicka till FK',function(){

                cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
                const önskadUrl2 = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id;
                intyg.besökÖnskadUrl(önskadUrl2, this.vårdpersonal, this.vårdenhet, this.utkastId);
                intyg.skickaTillFk();
                cy.contains("Intyget är skickat till Försäkringskassan");

            });
           
         
            it('Kan Ställa Administrativ fråga till FK',function(){

                cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
                const önskadUrl4 = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id;
                intyg.besökÖnskadUrl(önskadUrl4, this.vårdpersonal, this.vårdenhet, this.utkastId);
                intyg.stallaFragaTillFK('Administrativ');

            });
            it('Kan Läsa fråga från FK',function(){

                cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
                const kompletteringstext = "Denna kompletteringstext ska vi kunna se";
                cy.wrap(kompletteringstext).as('kompletteringstext');
                cy.skapaKompletteringMotWebcert(this);
                const önskadUrl5 = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id;
                intyg.besökÖnskadUrl(önskadUrl5, this.vårdpersonal, this.vårdenhet, this.utkastId);
                cy.contains(kompletteringstext);

            });
        
            it('Kan Starta komplettering till FK',function(){

                const önskadUrl6 = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id;
                intyg.besökÖnskadUrl(önskadUrl6, this.vårdpersonal, this.vårdenhet, this.utkastId);
                intyg.komplettera();
                cy.wait(1000);
                intyg.sektionBedömning75Nedsatt(this.intygsdata.bedömning);
                cy.get('#sjukskrivningarHELT_NEDSATT').click();
                intyg.sektionDelAvBedömning(this.intygsdata.bedömning);
                cy.contains("Klart att signera")          
            
            });
            it('Kan Förnya intyg',function(){

                cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
                const önskadUrl3 = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id;
                intyg.besökÖnskadUrl(önskadUrl3, this.vårdpersonal, this.vårdenhet, this.utkastId);
                intyg.fornya();
                cy.contains("Obligatoriska uppgifter saknas");

            });
        });

    });
    context('Vårdadmin kan utföra endast behöriga uppgifter på ett AG7804-intyg i integrerat läge' , function() {
        beforeEach(function() {
            cy.skapaAG7804Utkast(this).then((ag7804Id) => {
            cy.wrap(ag7804Id).as('ag7804Id');
            cy.log("AG7804-utkast med id " + this.ag7804Id + " skapat och används i testfallet");
            });
        });
        describe('Vårdadmin och AG7804', function() {
          
            it('Kan skapa  och fylla i ett AG7804 utifrån ett LISJP', function(){

                cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
                const önskadUrl = "/visa/intyg/" + this.ag7804Id + "?enhet=" + this.vårdenhet.id;
                intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.ag7804Id);
                cy.get('#copy-from-candidate-dialog-button1').click();
                cy.get('.intygs-id').contains(this.ag7804Id);
                intyg.sektionBedömning75Nedsatt(this.AGintygsdata.bedömning);
                cy.get('#onskarFormedlaDiagnosNo').click(); 
                cy.get('#sjukskrivningarHELT_NEDSATT').click();
                agIntyg.sektionDelAvBedömning(this.AGintygsdata.bedömning);               
                cy.contains("Klart att signera"); 
                cy.get('#markeraKlartForSigneringButton').click(); 
                cy.contains("Klart att signera"); 
            });
        });
      
    }); 
    
});

