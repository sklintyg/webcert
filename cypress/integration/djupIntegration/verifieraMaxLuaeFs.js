/* globals context cy */
/// <reference types="Cypress" />

// LUAE-FA = Läkarutlåtande för aktivitetsersättning vid förlängd skolgång, FK 7802

describe('(integrerat) skicka maximalt ifyllt LUAE-FS till FK', function () {

    before(function() {
        cy.fixture('FK_intyg/luaeFsData').as('luaeFsData');
        cy.fixture('arnoldJohansson').as('vårdgivare');
        cy.fixture('alfaEnheten').as('vårdenhet');
        cy.fixture('tolvanTolvansson').as('vårdtagare');
    });

    beforeEach(function() {
        cy.skapaLuaeFsUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LUAE-FS-utkast med id " + utkastId + " skapat och används i testfallet");
        });
    });

    it('skapar en maximalt ifylld LUAE-FS och skickar den till FK', function () {
        cy.loggaInVårdgivareIntegrerat(this);

        // Gå till intyget, redigera det, signera och skicka till FK
        cy.visit("/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id).fyllIMaxLuaeFs(this);
    });
});
