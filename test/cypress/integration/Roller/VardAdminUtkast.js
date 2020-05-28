/* globals context cy */
/// <reference types="Cypress" />
import * as intyg from '../../support/FK_intyg/lisjpIntyg'

describe('Behörigheter för Vårdadmin gällande LISJP-utkast', function () {
    
    before(function() {
        cy.fixture('FK_intyg/minLisjpData').as('intygsdata');
        cy.fixture('vårdpersonal/annikaLarsson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdtagare/balanarNattjagare').as('vårdtagare');
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP utkast med id " + utkastId + " skapat och används i testfallet");
            });  
    
        });

    beforeEach(function() {
        
    })
    context('Vårdadmin kan utföra endast behöriga uppgifter på ett utkast i normalt läge' , function() {

        it('Kan vidarebefordra utkast',function(){
            const vidarebefordraknapp = '#vidarebefordraBtn-' + this.utkastId;
            cy.log(vidarebefordraknapp);
            cy.loggaInVårdpersonalNormal(this.vårdpersonal,this.vårdenhet);
            cy.visit("/#/unsigned" );
            cy.get(vidarebefordraknapp).should('be.visible');
        })
    });

    context('Vårdadmin kan utföra endast behöriga uppgifter på ett utkast integrerat' , function() {
        
        it('Kan skapa,öppna och läsa utkast',function(){
            cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
            const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id;
            intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);
        })
        it('Kan editera ett utkast' , function(){
            cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
            const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id;
            intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);
            intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag);
            intyg.sektionDiagnos(this.intygsdata.diagnos);
            intyg.sektionSysselsättning(this.intygsdata.sysselsättning);
            intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten);
            intyg.sektionBedömning(this.intygsdata.bedömning);
            intyg.sektionÅtgärder(this.intygsdata.åtgärder);
            intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling);
            cy.contains("Klart att signera");
            
        })
        it('Kan inte signera', function(){
            cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
            const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id;
            intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);
            cy.get('.intygs-id').should('be.visible')
            cy.contains("Klart att signera");
            cy.get('#signera-utkast-button').should('not.be.visible');
                        
        })
        it('Kan skriva ut utkast',function(){
            
            cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
            const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id;
            intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);
            intyg.skrivUt("fullständigt", this.utkastId);
        });
     context('Vårdadmin kan utföra radering av utkast' , function() { 
        beforeEach(function() {
            cy.skapaLisjpUtkast(this).then((utkastId1) => {
                cy.wrap(utkastId1).as('utkastId1');
                cy.log("LISJP utkast med id " + utkastId1 + " skapat och används i testfallet");
            });
        }); 
           
        it('Kan radera utkast',function(){
            cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
            const önskadUrl = "/visa/intyg/" + this.utkastId1 + "?enhet=" + this.vårdenhet.id
            intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId1);
            intyg.raderaUtkast();
        });
    });
        
    });
        
});
