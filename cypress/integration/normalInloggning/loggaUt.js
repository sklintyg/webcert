/* globals context cy */
/// <reference types="Cypress" />

describe('Logga ut vårdgivare', function () {

    before(function() {
        cy.fixture('arnoldJohansson').as('vårdgivare');
        cy.fixture('alfaEnheten').as('vårdenhet');
    });

    // Funkar inte i Jenkins
    it.skip('är möjligt att logga ut vårdgivare', function() {
        cy.loggaInVårdgivareNormal(this);
        cy.visit('/#/create/choose-patient/index');

        // Klicka på "Logga ut"
        cy.get('[id="logoutLink"]').click();
        cy.url().should('include', "/welcome.html");
    });
});
