/* globals context cy */
/// <reference types="Cypress" />

// LUSE = Läkarutlåtande för sjukersättning, FK 7800

describe('(integrerat) skicka maximalt ifyllt LUSE till FK', function () {

    before(function() {
        cy.fixture('FK_intyg/luseData').as('luseData');
        cy.fixture('vårdgivare/arnoldJohansson').as('vårdgivare');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdtagare/tolvanTolvansson').as('vårdtagare');
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
