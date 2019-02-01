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

		// Skapa alias för "Fortsätt"-knappen
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
		cy.contains(this.lisjpData.diagnoserNedsattArbetsförmåga).parent().parent().parent().within(($form) => { // Ugly....
			// Assume ICD-10-SE is selected since that is default (will cause errors if it is NOT selected)
			cy.get('[placeholder=' + this.lisjpData.kodTextareaPlaceholder + ']').then(($codeFields) => {
				cy.wrap($codeFields.eq(0)).type('O267').wait(1000).type('{enter}');
				cy.wrap($codeFields.eq(1)).type('O470A').wait(1000).type('{enter}');
				cy.wrap($codeFields.eq(2)).type('O210').wait(1000).type('{enter}');
			});
		});

		// ----- 'Sjukdomens konsekvenser för patienten' ----- //
		cy.contains('Beskriv undersökningsfynd, testresultat och observationer').parent().parent().parent().within(($form) => {
			cy.wrap($form).find('textarea').type('Uttalade besvär från bäcken, svår smärta vid lägesförändring, hälta, instabilitet bäcken');
		});

		cy.contains('Beskriv vad patienten inte kan göra på grund av sin sjukdom. Ange vad uppgiften grundas på.').parent().parent().parent().within(($form) => {
			cy.wrap($form).find('textarea')
			.type('starka bäckensmärtor som ger uttalad aktivitetsbegränsning vid fysisk aktivitet och kontorsarbete samt koncentrationssvårigheter');
		});

		// ----- 'Medicinsk behandling' ----- //
		cy.contains('Pågående medicinska behandlingar/åtgärder').parent().parent().parent().within(($form) => {
			cy.wrap($form).find('textarea')
			.type('Sjukgymnastik och rörelseprogram för att lindra bäckenuppluckring');
		});

		cy.contains('Planerade medicinska behandlingar/åtgärder.').parent().parent().parent().within(($form) => {
			cy.wrap($form).find('textarea')
			.type('Specialist, ultraljudskontroller');
		});

		// ----- 'Bedömning' -----//
		cy.contains('Min bedömning av patientens nedsättning av arbetsförmågan').parent().parent().parent().within(($form) => {
			cy.get('[type="checkbox"]').within(($checkboxes) => {
				cy.wrap($checkboxes).check(); // Will check all checkboxes in this section. The "from" date field will get today's date

				cy.wrap($checkboxes.eq(0)).parent().parent().parent().parent().within(($row) => {  // Get row with first checkbox (i.e. 25% sjukskrivning)
					cy.get('[type="text"]').then(($textFields) => {
						cy.wrap($textFields.eq(0)).clear().type(this.todayPlus1);
						cy.wrap($textFields.eq(1)).clear().type(this.todayPlus11);
					});
				});

				cy.wrap($checkboxes.eq(1)).parent().parent().parent().parent().within(($row) => {  // Get row with second checkbox (i.e. 50% sjukskrivning)
					cy.get('[type="text"]').then(($textFields) => {
						cy.wrap($textFields.eq(0)).clear().type(this.todayPlus12);
						cy.wrap($textFields.eq(1)).clear().type(this.todayPlus19);
					});
				});

				cy.wrap($checkboxes.eq(2)).parent().parent().parent().parent().within(($row) => {  // Get row with third checkbox (i.e. 75% sjukskrivning)
					cy.get('[type="text"]').then(($textFields) => {
						cy.wrap($textFields.eq(0)).clear().type(this.todayPlus20);
						cy.wrap($textFields.eq(1)).clear().type(this.todayPlus28);
					});
				});

				cy.wrap($checkboxes.eq(3)).parent().parent().parent().parent().within(($row) => {  // Get row with fourth checkbox (i.e. 100% sjukskrivning)
					cy.get('[type="text"]').then(($textFields) => {
						cy.wrap($textFields.eq(0)).clear().type(this.todayPlus29);
						cy.wrap($textFields.eq(1)).clear().type(this.todayPlus41);
					});
				});
			});
		});

		cy
		.contains('Patientens arbetsförmåga bedöms nedsatt längre tid än den som Socialstyrelsens försäkringsmedicinska beslutsstöd anger, därför att')
		.parent().parent().parent()
		.find('textarea').type('Haft tidigare bäckeninsuffiens vid graviditet, haft tidigare missfall och kräver nu fler kontroller och avlastning');

		cy.contains('Finns det medicinska skäl att förlägga arbetstiden på något annat sätt än att minska arbetstiden lika mycket varje dag?')
		.parent().parent().parent().within(($elem) => {
			cy.get('[type="radio"]').eq(0).check(); // The first radio button is 'YES'
		});

		cy.contains('Beskriv medicinska skäl till annan förläggning av arbetstiden').parent().parent().parent().find('textarea')
		.type('Har lättare kontorsarbete på måndagar som hon klarar av med nuvarande besvär om ingen försämring sker.');

		cy.contains('Resor till och från arbetet med annat färdmedel än normalt kan göra det möjligt för patienten att återgå till arbetet under sjukskrivningsperioden.')
		.parent().parent().within(() => {
			cy.get('[type="checkbox"]').check();
		});

		cy.contains('Prognos för arbetsförmåga utifrån aktuellt undersökningstillfälle').parent().parent().parent().within(($ele) => {
			cy.get('[type="radio"]').eq(0).check() // The wanted radio button is the top one
		});

		// ----- 'Åtgärder' -----//
		cy.contains('Här kan du ange åtgärder som du tror skulle göra det lättare för patienten att återgå i arbete').parent().parent().parent().within(($elem) => {
			cy.get('[type="checkbox"]').each(($el, index, $list) => {
				if (index != 0) { // Index 0 is "Inte aktuellt". This checkbox should NOT be checked
					cy.wrap($el).check();
				}
			});
		});

		cy.wait(3000);

		cy.contains('Här kan du ange fler åtgärder. Du kan också beskriva hur åtgärderna kan underlätta återgång i arbete.').should('be.visible').parent().parent().parent().find('textarea')
		.type('Om anpassningar till lättare uppgifter utan tunga lyft och mycket gående kan ordnas kvarstår arbetsförmåga under en tid framöver.');

		// ----- 'Övriga upplysningar' -----//
		//cy.contains('Övriga upplysningar').parent().parent().parent().find('textarea').type('Planerad partus ' + this.todayPlus41);
		cy.get('[name="ovrigt"]').type('Planerad partus ' + this.todayPlus41); 	// Temporary workaround. Using attribute 'name' since the textarea
																				// heading is present in more than one place
		// ----- 'Kontakt' -----//
		cy.contains('Jag önskar att Försäkringskassan kontaktar mig.').parent().parent().parent().within(($elem) => {
			cy.get('[type="checkbox"]').check();
		});

		cy.contains('Ange gärna varför du vill ha kontakt.').parent().parent().parent().find('textarea')
			.type('För att diskutera sjukskrivningsperiod och tydliggörande kring besvärsbilden samt framtida arbetsförmåga.');

		// ----- 'Signera intyg' -----//
		// On battery and Wifi, Cypress too often says that it presses the button but it doesn't
		cy.contains('button', 'Signera intyget', {timeout: 15000}).should('be.enabled').then(($button) => {
			cy.wait(1000); // This is a workaround when running on battery and wifi.... Cypress claims to click but nothing happen. Trying with a wait
			cy.wrap($button).click(); // prova att ändra till <button?
		});

		cy.contains('div', 'Välj vilka intygsmottagare invånaren kan skicka intyget till via Mina intyg.', {timeout: 20000}).should('be.visible').within(($elem) => {
			cy.get('[type="radio"]').each(($el, index, $list) => { // Just picking out all radio buttons... this can be improved
				if (index >= 2) { // Index 0 and 1 is "Försäkringskassan" 'Ja' and 'Nej'. Skip those.
					if (index % 2 != 0) { // Only check odd numbers since those are "Nej"
						cy.wrap($el).check();
					}
				}
			});
		});

		cy.get('[name="approveForm"]').within(($form) => {
			cy.get('[type="button"]').then(($button) => {
				cy.wrap($button).contains('Spara').should('be.visible');
				cy.wrap($button).click({force: true});
			});
		});

		cy.contains('button','Skicka till Försäkringskassan').should('be.visible').and('be.enabled').click();
		cy.contains('Om du går vidare kommer intyget skickas direkt till Försäkringskassans system vilket ska göras i samråd med patienten.')
			.should('be.visible');
		cy.contains('button', "Skicka").should('be.visible').click();
	});
});
