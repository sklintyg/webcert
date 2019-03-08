/* globals context cy */
/// <reference types="Cypress" />

describe('Logga in som vårdgivare', function () {

    const destUrlLyckadInloggning = "/#/create/choose-patient/index";

    beforeEach(function() {
        cy.fixture('arnoldJohansson').as('vårdgivare');
        cy.visit('/welcome.html');
    });

    it('har korrekt titel', function () {
		cy.title().should('contain', "Webcert test inloggning");
    });

    it('är mojligt att logga in som specifik läkare genom att välja i listan och sen klicka på inloggningsknappen', function() {
        cy.contains(this.vårdgivare.namnSträngInloggning).click().then(() => {
			cy.contains("Logga in").click();
        });

        cy.url().should('include', destUrlLyckadInloggning);
    });

    it('är möjligt att logga in som specifik läkare genom att dubbelklicka på raden i listan', function() {
        cy.contains(this.vårdgivare.namnSträngInloggning).dblclick().then(() => {
			cy.url().should('include', destUrlLyckadInloggning);
        });
    });
});
