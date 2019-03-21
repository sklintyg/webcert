/* globals context cy */
/// <reference types="Cypress" />

// LUAE-FA = Läkarutlåtande för aktivitetsersättning vid förlängd skolgång, FK 7802

describe('LUAE-FS-intyg', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLuaeFsData').as('luaeFsData');
        cy.fixture('vårdgivare/arnoldJohansson').as('vårdgivare');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdtagare/tolvanTolvansson').as('vårdtagare');
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
