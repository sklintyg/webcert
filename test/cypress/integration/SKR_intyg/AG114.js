/* globals context cy */
/// <reference types="Cypress" />
import * as intyg from '../../support/SKR_intyg/AG114intyg'

// AG114= Läkarintyg om arbetsförmåga – arbetsgivaren, AG 114

describe('AG114-intyg', function () {

    before(function() {
        cy.fixture('SKR_intyg/maxAG114Data').as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/nmt_vg2_ve1').as('vårdenhet');
        cy.fixture('vårdtagare/balanarNattjagare').as('vårdtagare');
    });

    beforeEach(function() {
        cy.skapaAG114Utkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("AG114-utkast med id " + utkastId + " skapat och används i testfallet");
        });
    });

    it('skapar ett maximalt ifyllt AG7114 ', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);
        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag);
        intyg.sektionSysselsättning(this.intygsdata.sysselsättning);
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        intyg.sektionArbetsförmåga(this.intygsdata.arbetsförmåga);
        intyg.sektionBedömning(this.intygsdata.bedömning);
        intyg.sektionÖvrigaUpplysningar(this.intygsdata.övrigt);
        intyg.sektionKontaktArbetsgivaren(this.intygsdata.kontakt);
        intyg.signera();
    });
});