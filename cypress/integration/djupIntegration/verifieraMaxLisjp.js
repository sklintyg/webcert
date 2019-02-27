/* globals context cy */
/// <reference types="Cypress" />

describe('skapa och signera maximalt ifyllt LISJP och skicka till FK', function () {

    before(function() {
        cy.fixture('lisjpData').as('lisjpData');
        cy.fixture('arnoldJohansson').as('vårdgivare');
        cy.fixture('alfaEnheten').as('vårdenhet');
    });

    beforeEach(function() {
        cy.createLisjpDraftNonGeneric().then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("Utkast med id " + utkastId + " skapat och används i testfallet");

        });
    });

    it('skapar en maximalt ifylld LISJP och skickar den till FK', function () {
        cy.loggaInVårdgivareIntegrerat(this);

        // Gå till intyget, redigera det, signera och skicka till FK
        cy.visit("/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id).fillOutMaxLisjp(this);
    });
});