/* globals context cy */
/// <reference types="Cypress" />
import * as intyg from '../../support/FK_intyg/lisjpIntyg'
//import * as registerIntyg from '../../support/FK_intyg/registerLisjp'

describe('registrera LISJP-intyg likt fristående', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet');
        cy.fixture('vårdtagare/balanarNattjagare').as('vårdtagare');
    });

    beforeEach(function() {
        cy.skickaRegisterLisjp(this).then((intygsID) => {
            cy.wrap(intygsID).as('intygsID');
            cy.log("LISJP-intyg med id " + intygsID + " skapat och används i testfallet");
       });
    });

    it('skapar LISJP genom registerCertificate fristående', function(){
        const intygsUrl = '/#/intyg/lisjp/1.1/' + this.intygsID + '/';
        cy.log(intygsUrl);
        cy.loggaInVårdpersonalNormal(this.vårdpersonal, this.vårdenhet);
        cy.visit('/#/create/choose-patient/index');
        
        cy
            .get('input').type(this.vårdtagare.personnummerKompakt)
            .get('#skapapersonnummerfortsatt').click();
        const knappen = '#showBtn-' + this.intygsID;
        cy.get(knappen).click();
        cy.wait(1000);
       // intyg.besökÖnskadUrl(intygsUrl,this.vårdpersonal,this.vårdenhet,this.intygsID);
        intyg.fornya();
        intyg.sektionBedömning(this.intygsdata.bedömning);
        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag);
        
       // cy.wait(1000);
        intyg.signera();
        cy.taBortIntyg(this);
        let intygsUrl1;
         cy.location('href').then((loc) => {
            cy.log(loc);
            intygsUrl1 = loc;
            cy.log(intygsUrl1);
           
         });
        // intyg.makuleraIntyg("Annat allvarligt fel");
         /*var start =  "/1.1/";
         var stop = "/?signed"
         
         var startIdx = body.indexOf(start);
         var stopIdx = body.indexOf(stop);
         var förnyatID = body.substring(startIdx + start.length, stopIdx);*/
         //plocka fram intygsId för det förnyade intyget och ta bort även det
          
        
    });
});
