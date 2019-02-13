/* globals context cy */
/// <reference types="Cypress" />

describe('Logga in som vårdgivare', function () {

    beforeEach(function() {
        cy.fixture('testData').as('testData')
        cy.visit('/welcome.html');
    });

    it('har korrekt titel', function () {
		cy.title().should('contain', this.testData.titel);
    });

    it('är mojligt att logga in som specifik läkare genom att välja i listan och sen klicka på inloggningsknappen', function() {
        cy.contains(this.testData.doktor).click().then(() => {
			cy.contains("Logga in").click();
        });

        cy.url().should('include', this.testData.destUrlLyckadInloggning);
    });

    it('är möjligt att logga in som specifik läkare genom att dubbelklicka på raden i listan', function() {
        cy.contains(this.testData.doktor).dblclick().then(() => {
			cy.url().should('include', this.testData.destUrlLyckadInloggning);
        });
    });
});
