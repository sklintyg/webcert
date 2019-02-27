/* globals context cy */
/// <reference types="Cypress" />

describe('skapa och signera maximalt ifyllt LISJP och skicka till FK', function () {

	before(function () {
		cy.fixture('lisjpData').as('lisjpData');
		cy.fixture('arnoldJohansson').as('vårdgivare');
		cy.fixture('alfaEnheten').as('vårdenhet');
		cy.fixture('tolvanTolvansson').as('vårdtagare');
	});

	it('skapar en maximalt ifylld LISJP och skickar den till FK', function () {
		/*
		cy.loggaInVårdgivareNormal(this).then(() => { // ta bort THEN
			cy.visit('/#/create/choose-intyg-type/' + this.vårdtagare.personnummer + '/index')
			.then(() => {
				cy.get("#intygTypeFortsatt-lisjp").click().fillOutMaxLisjp(this);
			});
		});
		*/

		cy.loggaInVårdgivareNormal(this);
		cy.visit('/#/create/choose-intyg-type/' + this.vårdtagare.personnummer + '/index');
		cy.get("#intygTypeFortsatt-lisjp").click().fillOutMaxLisjp(this);
	});
});
