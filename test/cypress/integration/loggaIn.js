/* globals context cy */
/// <reference types="Cypress" />

describe('Testinloggningssidan', function () {

    const destUrlLyckadInloggning = "/#/create/choose-patient/index";

    beforeEach(function() {
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.visit('/welcome.html');
    });

    it('har korrekt titel', function () {
		cy.title().should('contain', "Webcert test inloggning");
    });

    it('är möjligt att logga in vårdpersonal genom inloggningsknappen', function() {
        cy.contains(this.vårdpersonal.namnSträngInloggning).click().then(() => {
			cy.contains("Logga in").click();
        });

        cy.url().should('include', destUrlLyckadInloggning);
    });

    it('är möjligt att logga in vårdpersonal genom dubbelklick i namnlistan', function() {
        cy.contains(this.vårdpersonal.namnSträngInloggning).dblclick().then(() => {
			cy.url().should('include', destUrlLyckadInloggning);
        });
    });
});
