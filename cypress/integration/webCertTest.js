/* globals context cy */
/// <reference types="Cypress" />

describe('Creating and signing a max filled LISJP and sending it to FK', function () {

	beforeEach(function () {
		cy.viewport(1024, 768);
		cy.fixture('testData').as('testData').then(() => {
			cy.visit(this.testData.webCertUrl);
		});

		cy.fixture('doktorInloggning').as('doktorInloggning')
		cy.fixture('valjPatient').as('valjPatient');
		cy.fixture('valjIntyg').as('valjIntyg');
		cy.fixture('lisjpData').as('lisjpData');

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

	it('har korrekt titel', function () {
		cy.title().should('contain', this.testData.titel);
	});

	it('skapar en maximalt ifylld LISJP och skickar den till FK', function () {
		cy.contains(this.doktorInloggning.doktor).click().then(() => {
			cy.contains(this.doktorInloggning.inloggningsKnappText).click();
		});

		// Verifiera att Cookie-bannern syns och även att den försvinner när man trycker
		// på dess knapp. Vänta lite längre än normalt eftersom den animeras in.
		cy.contains(this.testData.cookieKnappText, {timeout: 10000}).click().then(() => {
			cy.contains('cookies').should('not.exist');
		});

		// Alias för "Fortsätt"-knappen
		cy.contains(this.valjPatient.fortsattKnappText).as('continueBtn');

		cy.get('@continueBtn').should('be.disabled');

		// Mata in patientens personnummer
		cy
			.get('input:first').should('have.attr', 'placeholder', this.valjPatient.placeholderText)
			.type(this.valjPatient.personnummer)
			.should('have.value', this.valjPatient.personnummer);

		// Verfiera att knappen för att gå vidare är aktiverad och klicka på den
		cy.get('@continueBtn').should('be.enabled').then(() => {
			cy.get('@continueBtn').click();
		});

		// Verifiera att inte Cookie-bannern dyker upp eftersom den är accepterad
		cy.contains('cookies').should('not.exist');

	    /* Temporärt bortkommenterad. Om databasen tömt (eller alla osignerade intyg tas bort av annan anledning)
	       kommer elementet som letas efter inte finnas alls, och testfallet går fel
		// Spara antal osignerade intyg
		cy.get('#stat-unitstat-unsigned-certs-count').then(($unsignedCertsCount) => {
			const initialNumber = parseInt($unsignedCertsCount.text());
			cy.wrap(initialNumber).as('initialNumberOfUnsignedCerts'); // Skapa alias för att kunna accessa i senare steg
		});
	    */

		// Klicka på "Skapa intyg" for LISJP
		// Detta skapar ett intyg så räknaren ska inkrementera med 1 direkt
		cy.get(this.valjIntyg.lisjp).click()/*.then(() => {
			cy.wait(1000); // Wait for one second to give application a chance to update the value for drafts
			cy.get('#stat-unitstat-unsigned-certs-count').then(($unsignedCertsCount) => {
				const incrementedNumber = parseInt($unsignedCertsCount.text())
				cy.log('Num unsigned certs after creating new draft: ' + incrementedNumber);
				expect(incrementedNumber).to.eq(this.initialNumberOfUnsignedCerts + 1);
			});
		}); */

		// Fyll i intyget i enlighet med mallen från Försäkringskassan, "max"-varianten
		// -------------------- 'Intyget är baserat på' --------------------
		cy.contains(this.lisjpData.minUndersökning).parentsUntil('.ue-del-fraga').within(($form) => {
			cy.get('[type="checkbox"]').check();
			cy.get(this.lisjpData.datumUndersökning).clear().type(this.todayMinus5);
		});
 
		cy.contains(this.lisjpData.telefonKontakt).parentsUntil('.ue-del-fraga').within(($form) => {
			cy.get('[type="checkbox"]').check();
			cy.get(this.lisjpData.datumTelefonkontakt).clear().type(this.todayMinus6);
		});

		cy.contains(this.lisjpData.journalUppgifterFrån).parentsUntil('.ue-del-fraga').within(($form) => {
			cy.wrap($form).get('[type="checkbox"]').check();
			cy.wrap($form).get(this.lisjpData.datumJournalUppgifterFrån).clear().type(this.todayMinus15);
		});

		cy.contains(this.lisjpData.annat).parentsUntil('.ue-del-fraga').within(($form) => {
			cy.get('[type="checkbox"]').check();
			cy.get(this.lisjpData.datumAnnat).clear().type(this.todayMinus14);
		});

		// Fill out the text box that should appear when clicking the 'Annat' checkbox
		cy.get(this.lisjpData.annatTextarea).type(this.lisjpData.annatTextareaText);


		// ----- 'I relation till vilken sysselsättning bedömer du arbetsförmågan?' -----
		cy.contains(this.lisjpData.nuvarandeArbete).parent().within(($form) => {
			cy.get('[type="checkbox"]').check();
		});

		cy.contains(this.lisjpData.yrkeOchUppgifter).parent().parent().parent().within(($form) => {
			cy.wrap($form).find('textarea').type(this.lisjpData.yrkeOchUppgifterText);
		});

		cy.contains(this.lisjpData.checkboxTextNormaltFörekommandeJobb).parent().within(($form) => {
			cy.get('[type="checkbox"]').check();
		});

		cy.contains(this.lisjpData.checkboxTextVAB).parent().within(($form) => {
			cy.get('[type="checkbox"]').check();
		});

		cy.contains(this.lisjpData.checkboxStudier).parent().within(($form) => {
			cy.get('[type="checkbox"]').check();
		});

		// ----- 'Diagnos' ----- //
		cy.contains(this.lisjpData.diagnoserNedsattArbetsförmåga).parent().parent().parent().within(($form) => {
			// Antag att ICD-10-SE är förvalt
			cy.get('[placeholder=' + this.lisjpData.kodTextareaPlaceholder + ']').then(($codeFields) => {
				cy.wrap($codeFields.eq(0)).type(this.lisjpData.diagnosKod1).wait(1000).type('{enter}');
				cy.wrap($codeFields.eq(1)).type(this.lisjpData.diagnosKod2).wait(1000).type('{enter}');
				cy.wrap($codeFields.eq(2)).type(this.lisjpData.diagnosKod3).wait(1000).type('{enter}');
			});
		});

		// ----- 'Sjukdomens konsekvenser för patienten' ----- //
		cy.contains(this.lisjpData.beskrivObservationer).parent().parent().parent().within(($form) => {
			cy.wrap($form).find('textarea').type(this.lisjpData.besvärsBeskrivning);
		});

		cy.contains(this.lisjpData.beskrivPatientBegränsning).parent().parent().parent().within(($form) => {
			cy.wrap($form).find('textarea')
			.type(this.lisjpData.begränsningsBeskrivning);
		});

		// ----- 'Medicinsk behandling' ----- //
		cy.contains(this.lisjpData.medicinskaBehandlingar).parent().parent().parent().within(($form) => {
			cy.wrap($form).find('textarea')
			.type(this.lisjpData.medicinskaBehandlingarBeskrivning);
		});

		cy.contains(this.lisjpData.planeradeBehandlingar).parent().parent().parent().within(($form) => {
			cy.wrap($form).find('textarea')
			.type(this.lisjpData.planeradeBehandlingarBeskrivning);
		});

		// ----- 'Bedömning' -----//
		cy.contains(this.lisjpData.arbetsförmågaBedömning).parent().parent().parent().within(($form) => {
			cy.get('[type="checkbox"]').within(($checkboxes) => {
				cy.wrap($checkboxes).check(); // Klickar i alla checkboxar i sektionen. "Från"-datum blir dagens datum

				cy.wrap($checkboxes.eq(0)).parent().parent().parent().parent().within(($row) => { // Hämta raden med 25% sjukskrivning
					cy.get('[type="text"]').then(($textFields) => {
						cy.wrap($textFields.eq(0)).clear().type(this.todayPlus1);
						cy.wrap($textFields.eq(1)).clear().type(this.todayPlus11);
					});
				});

				cy.wrap($checkboxes.eq(1)).parent().parent().parent().parent().within(($row) => { // Hämta raden med 50% sjukskrivning
					cy.get('[type="text"]').then(($textFields) => {
						cy.wrap($textFields.eq(0)).clear().type(this.todayPlus12);
						cy.wrap($textFields.eq(1)).clear().type(this.todayPlus19);
					});
				});

				cy.wrap($checkboxes.eq(2)).parent().parent().parent().parent().within(($row) => { // Hämta raden med 75% sjukskrivning
					cy.get('[type="text"]').then(($textFields) => {
						cy.wrap($textFields.eq(0)).clear().type(this.todayPlus20);
						cy.wrap($textFields.eq(1)).clear().type(this.todayPlus28);
					});
				});

				cy.wrap($checkboxes.eq(3)).parent().parent().parent().parent().within(($row) => { // Hämta raden med 100% sjukskrivning
					cy.get('[type="text"]').then(($textFields) => {
						cy.wrap($textFields.eq(0)).clear().type(this.todayPlus29);
						cy.wrap($textFields.eq(1)).clear().type(this.todayPlus41);
					});
				});
			});
		});

		cy
		.contains(this.lisjpData.längreNedsattArbetsförmåga)
		.parent().parent().parent()
		.find('textarea').type(this.lisjpData.längreNedsattArbetsförmågaText);

		cy.contains(this.lisjpData.förläggaArbetstidAnnorlunda)
		.parent().parent().parent().within(($elem) => {
			cy.get('[type="radio"]').eq(0).check(); // Första radioknappen är "Ja"
		});

		cy.contains(this.lisjpData.arbetstidAnnorlundaMedicinskaSkäl).parent().parent().parent().find('textarea')
		.type(this.lisjpData.arbetstidAnnorlundaMedicinskaSkälBeskrivning);

		cy.contains(this.lisjpData.resaMöjliggörArbete)
		.parent().parent().within(() => {
			cy.get('[type="checkbox"]').check();
		});

		cy.contains(this.lisjpData.arbetsförmågaPrognos).parent().parent().parent().within(($ele) => {
			cy.get('[type="radio"]').eq(0).check() // Översta radioknappen är den som ska anges
		});

		// ----- 'Åtgärder' -----//
		cy.contains(this.lisjpData.föreslåÅtgärder).parent().parent().parent().within(($elem) => {
			cy.get('[type="checkbox"]').each(($el, index, $list) => {
				if (index != 0) { // Index 0 är "Inte aktuellt", detta är den enda checkboxen som INTE ska anges
					cy.wrap($el).check();
				}
			});
		});

		cy.wait(3000);

		cy.contains(this.lisjpData.flerÅtgärder).should('be.visible').parent().parent().parent().find('textarea')
		.type(this.lisjpData.flerÅtgärderBeskrivning);

		// ----- 'Övriga upplysningar' -----//
		//cy.contains('Övriga upplysningar').parent().parent().parent().find('textarea').type('Planerad partus ' + this.todayPlus41);
		cy.get('[name="ovrigt"]').type(this.lisjpData.övrigaUpplysningarBeskrivning + this.todayPlus41); 	// Tillfällig lösning. Använder attribut 'name' eftersom textareans rubrik finns på fler än ett ställe

		// ----- 'Kontakt' -----//
		cy.contains(this.lisjpData.kontaktaMig).parent().parent().parent().within(($elem) => {
			cy.get('[type="checkbox"]').check();
		});

		cy.contains(this.lisjpData.anledningKontakt).parent().parent().parent().find('textarea')
			.type(this.lisjpData.anledningKontaktBeskrivning);

		// ----- 'Signera intyg' -----//

		// Lägger till en paus här för att se om det är så att Cypress klickar för snabbt på Signera-knappen. I Jenkins
		// failar testfallet ofta p.g.a att det dyker upp en modal som säger att intyger har ändrats av annan person (men personen som
		// vill signera intyget är samma som anges som den som har ändrat det)
		// Just nu verkar testfallet gå igenom men frågan är om detta har något med saken att göra eller om det fungerar bara för att jag bytte till
		// ny slav på Jenkins (det förkortade exekveringstiden till hälften ungefär)
		cy.wait(6000);

		// Om laptop kör på batteri och Wifi så visar Cypress att knappen trycks in,
		// men ofta händer inget
		cy.contains('button', this.lisjpData.signeraKnappText, {timeout: 15000})
		.should('be.enabled')
		.then(($button) => {
			cy.wait(1000);
			cy.wrap($button).click(); // prova att ändra till <button?
		});

		// Paus här också, precis som ovanför "Signera intyg"-knappen. Behövs den nu när vi kör på snabbare slav?
		cy.wait(6000);

		cy.contains('div', this.lisjpData.väljIntygsmottagare, {timeout: 20000})
		.should('be.visible')
		.within(($elem) => {
			cy.get('[type="radio"]').each(($el, index, $list) => { // Hämtar ut samtliga radioknappar... kan förbättras
				if (index >= 2) { // Index 0 och 1 är "Försäkringskassan" 'Ja' och 'Nej'. Hoppas över.
					if (index % 2 != 0) { // Klicka endast i udda nummer eftersom dessa är "Nej"
						cy.wrap($el).check();
					}
				}
			});
		});

		cy.get('[name="approveForm"]').within(($form) => {
			cy.get('[type="button"]').then(($button) => {
				cy.wrap($button).contains(this.lisjpData.intygsmottagareKnappText).should('be.visible');
				cy.wrap($button).click({force: true});
			});
		});

		// Testfallet har failat enstaka gånger i CI p.g.a. timeout på denna knapp, ökar därför timeout
		cy.contains('button', this.lisjpData.skickaTillFKKnappText, {timeout: 20000})
		.should('be.visible').and('be.enabled')
		.click();

		cy.contains(this.lisjpData.varningSkickaTillFK).should('be.visible');

		cy.contains('button', this.lisjpData.skickaKnappTextEfterVarning)
		.should('be.visible')
		.click();
	});
});
