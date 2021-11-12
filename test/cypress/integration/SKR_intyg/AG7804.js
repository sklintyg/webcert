/* globals context cy */
/// <reference types="Cypress" />
import * as intyg from '../../support/SKR_intyg/AG7804Intyg'

// AG7804= Läkarintyg om arbetsförmåga – arbetsgivaren, AG 7804

describe('AG7804-intyg', function () {

    before(function() {
        cy.fixture('SKR_intyg/maxAG7804Data').as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/nmt_vg2_ve1').as('vårdenhet');
        cy.fixture('vårdtagare/balanarNattjagare').as('vårdtagare');
    });

    beforeEach(function() {
        //cy.rensaIntyg(this.vårdtagare);
        cy.skapaAG7804Utkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("AG7804-utkast med id " + utkastId + " skapat och används i testfallet");
        });
    });

    it('skapar en maximalt ifylld AG7804 ', function () {
       
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);
        intyg.ifyllnadsstod(); //finns det redan ett lisjp dyker det upp en dialog som måste klickas bort
        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag);
        intyg.sektionSysselsättning(this.intygsdata.sysselsättning);
        cy.wait(100);
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten);
        intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling);
        intyg.sektionBedömning(this.intygsdata.bedömning);
        intyg.sektionÅtgärder(this.intygsdata.åtgärder);
        intyg.sektionÖvrigt(this.intygsdata.övrigt);
        intyg.sektionKontaktArbetsgivaren(this.intygsdata.kontakt);
        intyg.signera();
    });
});