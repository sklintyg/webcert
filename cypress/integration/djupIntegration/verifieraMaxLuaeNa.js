/* globals context cy */
/// <reference types="Cypress" />

// LUAE-NA = Läkarutlåtande för aktivitetsersättning vid nedssatt arbetsförmånga, FK 7801

describe('(integrerat) skicka maximalt ifyllt LUAE-NA till FK', function () {

    before(function() {
        cy.fixture('luaeNaData').as('luaeNaData');
        cy.fixture('arnoldJohansson').as('vårdgivare');
        cy.fixture('alfaEnheten').as('vårdenhet');
        cy.fixture('tolvanTolvansson').as('vårdtagare');
    });

    beforeEach(function() {
        cy.skapaLuaeNaUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LUAE-NA-utkast med id " + utkastId + " skapat och används i testfallet");
        });
    });

    it('skapar en maximalt ifylld LUAE-NA och skickar den till FK', function () {
        cy.loggaInVårdgivareIntegrerat(this);

        // Gå till intyget, redigera det, signera och skicka till FK
        cy.visit("/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id).fyllIMaxLuaeNa(this);
    });
});
