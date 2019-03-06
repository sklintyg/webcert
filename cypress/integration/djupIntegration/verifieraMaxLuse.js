/* globals context cy */
/// <reference types="Cypress" />

describe('(integrerat) skicka maximalt ifyllt LUSE till FK', function () {

    before(function() {
        cy.fixture('luseData').as('luseData');
        cy.fixture('arnoldJohansson').as('vårdgivare');
        cy.fixture('alfaEnheten').as('vårdenhet');
        cy.fixture('tolvanTolvansson').as('vårdtagare');
    });

    beforeEach(function() {
        cy.skapaLuseUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LUSE-utkast med id " + utkastId + " skapat och används i testfallet");
        });
    });

    it('skapar en maximalt ifylld LUSE och skickar den till FK', function () {
        cy.loggaInVårdgivareIntegrerat(this);

        // Gå till intyget, redigera det, signera och skicka till FK
        cy.visit("/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id).fyllIMaxLuse(this);
    });
});
