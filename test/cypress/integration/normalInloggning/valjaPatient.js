/* globals context cy */
/// <reference types="Cypress" />

describe('Välja patient', function () {

    before(function() {
        cy.fixture('vårdgivare/arnoldJohansson').as('vårdgivare');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdtagare/tolvanTolvansson').as('vårdtagare');
    });

    beforeEach(function() {
        cy.loggaInVårdgivareNormal(this).then(() => {
            cy.visit('/#/create/choose-patient/index');
        })
    });

    it('är möjligt att ange korrekt personnummer och komma vidare', function() {
        // Alias för "Fortsätt"-knappen
		cy.contains("Fortsätt").as('continueBtn');
		cy.get('@continueBtn').should('be.disabled');

        // Mata in patientens personnummer
        cy
        .get('input:first').should('have.attr', 'placeholder', "ååååmmdd-nnnn")
		.type(this.vårdtagare.personnummer)
		.should('have.value', this.vårdtagare.personnummer);

		// Verfiera att knappen för att gå vidare är aktiverad och klicka på den
		cy.get('@continueBtn').should('be.enabled').then(() => {
			cy.get('@continueBtn').click();
        });

        cy.url().should('include', "#/create/choose-intyg-type/" + this.vårdtagare.personnummer + "/index");
    });
});
