/* globals context cy */
/// <reference types="Cypress" />

describe('(integrerat) skicka maximalt ifyllt LISJP till FK', function () {

    before(function() {
        cy.fixture('lisjpData').as('lisjpData');
        cy.fixture('arnoldJohansson').as('vårdgivare');
        cy.fixture('alfaEnheten').as('vårdenhet');
        cy.fixture('tolvanTolvansson').as('vårdtagare');
    });

    beforeEach(function() {
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
        });
    });

    it('skapar en maximalt ifylld LISJP och skickar den till FK', function () {
        cy.loggaInVårdgivareIntegrerat(this);

        // Gå till intyget, redigera det, signera och skicka till FK
        cy.visit("/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id).fyllIMaxLisjp(this);
    });
});
