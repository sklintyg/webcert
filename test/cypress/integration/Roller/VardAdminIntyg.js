/* globals context cy */
/// <reference types="Cypress" />
import * as intyg from '../../support/FK_intyg/lisjpIntyg'

describe('Behörigheter för Vårdadmin gällande LISJP-intyg', function () {
    
    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').as('intygsdata');
        cy.fixture('SKR_intyg/maxAG7804Data').as('AGintygsdata');
        cy.fixture('vårdpersonal/annikaLarsson').as('vårdpersonal');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal1');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdtagare/balanarNattjagare').as('vårdtagare');
             
        });
        

    beforeEach(function() {
      
    });

    context('Vårdadmin kan utföra endast behöriga uppgifter på ett intyg i integrerat läge' , function() {
       
        it('Kan Läsa och Skriva ut intyg',function(){
           
            cy.skapaIntygWebcert(this).then((utkastId) =>{
        
                cy.wrap(utkastId).as('utkastId');
                cy.log("LISJP förifyllt utkast med id " + utkastId + " skapat och används i testfallet");
                cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
                const önskadUrl1 = "/visa/intyg/" + utkastId + "?enhet=" + this.vårdenhet.id;
                intyg.besökÖnskadUrl(önskadUrl1, this.vårdpersonal, this.vårdenhet, utkastId);
                cy.wait(1000);
                cy.contains("Intyget är signerat").should('exist');
                intyg.skrivUt("fullständigt", utkastId);

            });        
        });           
        it('Kan Skicka till FK',function(){
            cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
            const önskadUrl2 = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id;
            intyg.besökÖnskadUrl(önskadUrl2, this.vårdpersonal, this.vårdenhet, this.utkastId);
            intyg.skickaTillFk();

        });
       it('Kan Förnya intyg',function(){
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
            const önskadUrl3 = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id;
            intyg.besökÖnskadUrl(önskadUrl3, this.vårdpersonal, this.vårdenhet, this.utkastId);
            intyg.fornya();

        });
         it('Kan skapa ett AG7804 utifrån ett LISJP',function(){
            
            cy.skapaAG7804Utkast(this).then((ag7804Id) => {
                cy.wrap(ag7804Id).as('ag7804Id');
                cy.log("AG7804-utkast med id " + ag7804Id + " skapat och används i testfallet");
                cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
                const önskadUrl = "/visa/intyg/" + ag7804Id + "?enhet=" + this.vårdenhet.id;
                intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, ag7804Id);

                cy.get('#copy-from-candidate-dialog-button1').click().then(() =>{

                    cy.get('#prognos-STOR_SANNOLIKHET').click();
                    cy.get('#onskarFormedlaDiagnosNo').click();                
                    intyg.sektionBedömning(this.AGintygsdata.bedömning);
                    cy.contains("Klart att signera");
                });           
            });
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
            cy.wrap(kompletteringstext).as('kompletteringstext').then(() => {
                cy.skapaKompletteringMotWebcert(this);
            });            
            const önskadUrl5 = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id;
            intyg.besökÖnskadUrl(önskadUrl5, this.vårdpersonal, this.vårdenhet, this.utkastId);
            cy.contains(kompletteringstext);

        });
        
        it('Kan Starta komplettering till FK',function(){

            const önskadUrl6 = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id;
            intyg.besökÖnskadUrl(önskadUrl6, this.vårdpersonal, this.vårdenhet, this.utkastId);
            intyg.komplettera();
            cy.wait(100);
            intyg.sektionBedömning(this.intygsdata.bedömning);
            cy.contains("Klart att signera")          
            
        });
    });
});
