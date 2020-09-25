/* globals context cy */
/// <reference types="Cypress" />
import * as intyg from '../../support/FK_intyg/lisjpIntyg'

// LISJP = Läkarintyg för sjukpenning, FK 7804

describe('FörifylltLISJP-intyg', function () {

    before(function() {
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdtagare/balanarNattjagare').as('vårdtagare');
    });

    beforeEach(function() {
        cy.skapaLISJPIfylltUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-Förifyllt utkast med id " + utkastId + " skapat och används i testfallet");
            
        });
    });

    it('skapar en maximalt ifylld LISJP och skickar den till FK', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet,true);
        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);
        intyg.signera();
        intyg.skickaTillFk();
    });
});
