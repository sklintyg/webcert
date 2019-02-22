/* globals context cy */
/// <reference types="Cypress" />

describe('skapa och signera maximalt ifyllt LISJP och skicka till FK', function () {

	before(function () {
		cy.fixture('lisjpData').as('lisjpData');
	});

	it('skapar en maximalt ifylld LISJP och skickar den till FK', function () {
		cy.goToCreateCertForTolvanAsArnold();
		cy.get("#intygTypeFortsatt-lisjp").click().fillOutMaxLisjp(this);
	});
});
