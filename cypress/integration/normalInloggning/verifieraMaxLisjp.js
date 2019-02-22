/* globals context cy */
/// <reference types="Cypress" />

describe('skapa och signera maxifyllt LISJP och skicka till FK', function () {

	before(function() {

	});

	beforeEach(function () {
		cy.fixture('testData').as('testData');
		cy.fixture('valjPatient').as('valjPatient');
		cy.fixture('valjIntyg').as('valjIntyg');
		cy.fixture('lisjpData').as('lisjpData');

		// Flytta dessa ner i testfallet och gör om dem till konstanter istället?
		const today = Cypress.moment().format('YYYY-MM-DD');
		cy.wrap(Cypress.moment().add(1,  'days').format('YYYY-MM-DD')).as('todayPlus1');  // 25%  sjukskrivning start
		cy.wrap(Cypress.moment().add(11, 'days').format('YYYY-MM-DD')).as('todayPlus11'); // 25%  sjukskrivning slut
		cy.wrap(Cypress.moment().add(12, 'days').format('YYYY-MM-DD')).as('todayPlus12'); // 50%  sjukskrivning start
		cy.wrap(Cypress.moment().add(19, 'days').format('YYYY-MM-DD')).as('todayPlus19'); // 50%  sjukskrivning slut
		cy.wrap(Cypress.moment().add(20, 'days').format('YYYY-MM-DD')).as('todayPlus20'); // 75%  sjukskrivning start
		cy.wrap(Cypress.moment().add(28, 'days').format('YYYY-MM-DD')).as('todayPlus28'); // 75%  sjukskrivning slut
		cy.wrap(Cypress.moment().add(29, 'days').format('YYYY-MM-DD')).as('todayPlus29'); // 100% sjukskrivning start
		cy.wrap(Cypress.moment().add(41, 'days').format('YYYY-MM-DD')).as('todayPlus41'); // 100% sjukskrivning slut

		cy.wrap(Cypress.moment().subtract(5,  'days').format('YYYY-MM-DD')).as('todayMinus5');  // Patient examination date
		cy.wrap(Cypress.moment().subtract(6,  'days').format('YYYY-MM-DD')).as('todayMinus6');  // Date of phone contact with patient
		cy.wrap(Cypress.moment().subtract(15, 'days').format('YYYY-MM-DD')).as('todayMinus15'); // Journal entry date
		cy.wrap(Cypress.moment().subtract(14, 'days').format('YYYY-MM-DD')).as('todayMinus14'); // Midwife's assessment date
	});

	it('skapar en maximalt ifylld LISJP och skickar den till FK', function () {
		// Verifiera att Cookie-bannern syns och även att den försvinner när man trycker
		// på dess knapp. Vänta lite längre än normalt eftersom den animeras in.
		/* Får nog vara ett eget cookie-test i så fall
		cy.contains(this.testData.cookieKnappText, {timeout: 10000}).click().then(() => {
			cy.contains('cookies').should('not.exist');
		});

		// Verifiera att inte Cookie-bannern dyker upp eftersom den är accepterad
		cy.contains('cookies').should('not.exist');
		*/

		cy.goToCreateCertForTolvanAsArnold();

	    /* Temporärt bortkommenterad. Om databasen töms (eller alla osignerade intyg tas bort av annan anledning)
	       kommer elementet som letas efter inte finnas alls, och testfallet går fel
		// Spara antal osignerade intyg
		cy.get('#stat-unitstat-unsigned-certs-count').then(($unsignedCertsCount) => {
			const initialNumber = parseInt($unsignedCertsCount.text());
			cy.wrap(initialNumber).as('initialNumberOfUnsignedCerts'); // Skapa alias för att kunna accessa i senare steg
		});
	    */

		// Klicka på "Skapa intyg" for LISJP
		// Detta skapar ett intyg så räknaren ska inkrementera med 1 direkt
		cy.get(this.valjIntyg.lisjp).click()
		/*.then(() => {
			cy.wait(1000); // Wait for one second to give application a chance to update the value for drafts
			cy.get('#stat-unitstat-unsigned-certs-count').then(($unsignedCertsCount) => {
				const incrementedNumber = parseInt($unsignedCertsCount.text())
				cy.log('Num unsigned certs after creating new draft: ' + incrementedNumber);
				expect(incrementedNumber).to.eq(this.initialNumberOfUnsignedCerts + 1);
			});
		});
		*/
		.fillOutMaxLisjpForTolvanTolvansson(this);
	});
});
