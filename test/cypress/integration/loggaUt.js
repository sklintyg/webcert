/* globals context cy */
/// <reference types="Cypress" />

describe('Logga ut vårdgivare', function () {

    before(function() {
        cy.fixture('vårdgivare/arnoldJohansson').as('vårdgivare');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
    });


});
