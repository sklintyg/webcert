/* globals context cy */
/// <reference types="Cypress" />

describe('Skapa nytt intygsId via djupintegration', function () {

    beforeEach(function() {
        cy.fixture('testData').as('testData')
    });

    it('kan skapa LISJP', function () {
        // Testfallsskelett som inte gör något än.
        cy.loginArnoldDeep();
    });

});