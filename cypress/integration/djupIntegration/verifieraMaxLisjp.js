/* globals context cy */
/// <reference types="Cypress" />

describe('skapa och signera maximalt ifyllt LISJP och skicka till FK', function () {

    before(function() {
		cy.fixture('lisjpData').as('lisjpData');
    });

    beforeEach(function() {
        cy.createLisjpDraftNonGeneric().then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("Utkast med id " + utkastId + " skapat och används i testfallet");
        });
    });

    it('skapar en maximalt ifylld LISJP och skickar den till FK', function () {
        cy.loginArnoldDeep();

        // Gå till intyget, redigera det, signera och skicka till FK
        cy.visit("/visa/intyg/" + this.utkastId + "?enhet=TSTNMT2321000156-1077").fillOutMaxLisjp(this);
    });
});