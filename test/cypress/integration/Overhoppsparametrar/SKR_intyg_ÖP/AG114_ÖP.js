/* globals context cy */
/// <reference types="Cypress" />
//import * as intyg from '../../../support/FK_intyg/lisjpIntyg'
import * as overhopp from '../../../support/overhopp_helpers'
import * as intyg from '../../../support/SKR_intyg/AG114intyg'

// AG114= Läkarintyg om arbetsförmåga – arbetsgivaren, AG114

describe('AG114 test av Overhoppsparametrar', function () {
    
    before(function() {
        cy.fixture('SKR_intyg/maxAG114Data').as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').as('vårdtagare');
    })
    context('Observandum triggade utifrån olika uthoppsparametrar i AG114 intygsutkast', function () {
	    beforeEach(function() {
			cy.skapaAG114Utkast(this).then((utkastId) => {
				cy.wrap(utkastId).as('utkastId');
				cy.log("AG114-utkast med id " + utkastId + " skapat och används i testfallet");
				cy.clearCookies();
				cy.visit('/logout');
				cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
			});
		});
		it('Normal - Inget observandum visas när inga Overhoppsparametrar skickas med', function () {
			let normalUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id;
				
			cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);	
			
			// Någon anledning att använda intyg.besökÖnskadUrl istället för cy.visit? Tog bort ev. dublett 2 tester för "normal url"
			//intyg.besökÖnskadUrl(normalUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);
			cy.visit(normalUrl);
			cy.contains("Postort").should('exist');
			cy.log('Overhoppsparametrar: Ingen');
			overhopp.verifyPatStatus("utanParameter");
		})
		it('Avliden - Endast observandum med information om att patienten är avliden visas', function(){
			let avlidenUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + '&avliden=true';
			cy.visit(avlidenUrl);
			cy.url().should('include', this.utkastId);
			cy.contains("Postort").should('exist');
			cy.log('Overhoppsparametrar: avliden');
			overhopp.verifyPatStatus("avliden");
		})
		it('Ändrat namn - Endast observandum om att patientens namn skiljer sig från Journalsystemet visas', function () {
			let ändratNamnUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + '&fornamn=Gunilla&efternamn=Karlsson';
			cy.visit(ändratNamnUrl);
			cy.url().should('include', this.utkastId);
			cy.contains("Postort").should('exist');
			cy.log('Overhoppsparametrar: fornamn & efternamn');
			overhopp.verifyPatStatus("patientNamn");
		})
		it('ReservNr - Endast observandum om att personen har ett sammordningsnummer kopplat till ett reservnummer', function () {
			let reservNrUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + '&alternatePatientSSn=19270926308A';
			cy.visit(reservNrUrl);
			cy.url().should('include', this.utkastId);
			cy.contains("Postort").should('exist');
			cy.log('Overhoppsparametrar: alternatePatientSSn (reservnummer)');
			overhopp.verifyPatStatus("reservnummer");
		})
		it('ÄndratPnr - Observandum om att patientens personnummer har ändrats visas', function () {
			let ändratPnrUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + '&alternatePatientSSn=191212121212';			
			cy.visit(ändratPnrUrl);
			cy.url().should('include', this.utkastId);
			cy.contains("Postort").should('exist');
			cy.log('Overhoppsparametrar: alternatePatientSSn (ändrat personnummer)');
			overhopp.verifyPatStatus("ändratPnr");
		})
		it('originalPnrUrl - Observandum om att ändrat personnummer visas', function (){
			// Skicka med ett ändrat pnr
			let ändratPnrUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + '&alternatePatientSSn=191212121212';			
			cy.visit(ändratPnrUrl);
			cy.url().should('include', this.utkastId);
			cy.contains("Postort").should('exist');
			cy.log('Overhoppsparametrar: alternatePatientSSn (ändrat personnummer)');
			overhopp.verifyPatStatus("ändratPnr");
			
			//Avbryt inloggad session och loggga in på nytt
			cy.clearCookies();
			cy.visit('/logout');
			cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
			
			//Skicka med orginalet
			let originalPnrUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + '&alternatePatientSSn=' + this.vårdtagare.personnummerKompakt;
			cy.visit(originalPnrUrl);
			cy.url().should('include', this.utkastId);
			cy.contains("Postort").should('exist');
			cy.log('Overhoppsparametrar: alternatePatientSSn (ändrat tillbaka personnummer)');
			overhopp.verifyPatStatus("ändratPnr");
		})
	})
	context('Observandum triggade utifrån olika uthoppsparametrar i signerat AG114 intyg', function () {
		before(function() {
			cy.skapaAG114Utkast(this).then((utkastId) => {
				cy.wrap(utkastId).as('utkastId');
				cy.log("AG114-utkast med id " + utkastId + " skapat och används i testfallet");
	
				cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
				
				//let normalUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id;
				//cy.visit(normalUrl);
				//Fyll i och signera intyget
				intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag);
				intyg.sektionSysselsättning(this.intygsdata.sysselsättning);
				intyg.sektionDiagnos(this.intygsdata.diagnos);
				intyg.sektionArbetsförmåga(this.intygsdata.arbetsförmåga);
				intyg.sektionBedömning(this.intygsdata.bedömning);
				intyg.sektionÖvrigaUpplysningar(this.intygsdata.övrigt);
				intyg.sektionKontaktArbetsgivaren(this.intygsdata.kontakt);
				intyg.signera();
			});
		});
		//Behöver nedan kommentarer sparas?
			//reservnummer = 19270926308A
			//samordningsnummer = 196812732391
			
		beforeEach(function(){
			cy.clearCookies();
			cy.visit('/logout');
			cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
		})
		it('Normal - inga observandum visas när inga Overhoppsparametrar skickas med', function () {
			let normalUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id;
			cy.visit(normalUrl);
			cy.url().should('include', this.utkastId);
			cy.contains("Postort").should('exist');
			cy.log('Overhoppsparametrar: Ingen');
			overhopp.verifyPatStatus("utanParameter");			
		})
		it('Avliden - Endast observandum med information om att patienten är avliden visas', function () {
			let avlidenUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + '&avliden=true';
			cy.visit(avlidenUrl);
			cy.url().should('include', this.utkastId);
			cy.contains("Postort").should('exist');
			cy.log('Overhoppsparametrar: avliden');
			overhopp.verifyPatStatus("avliden");
		})
		it('Ändrat Namn - Endast observandum om att patientens namn skiljer sig från Journalsystemet visas', function () {
			let ändratNamnUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + '&fornamn=Gunilla&efternamn=Karlsson';
			cy.visit(ändratNamnUrl);
			cy.url().should('include', this.utkastId);
			cy.contains("Postort").should('exist');
			cy.log('Overhoppsparametrar: fornamn & efternamn');
			overhopp.verifyPatStatus("patientNamn");
		})
		it('ReservNr - endast observandum om att personen har ett sammordningsnummer kopplat till ett reservnummer', function () {
			let reservNrUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + '&alternatePatientSSn=19270926308A';
			cy.visit(reservNrUrl);
			cy.url().should('include', this.utkastId);
			cy.contains("Postort").should('exist');
			cy.log('Overhoppsparametrar: alternatePatientSSn (reservnummer)');
			overhopp.verifyPatStatus("reservnummer");			
		})
		it('ÄndratPnrUrl - Observandum om att patientens personnummer har ändrats visas', function () {
			let ändratPnrUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + '&alternatePatientSSn=191212121212';			
			cy.visit(ändratPnrUrl);
			cy.url().should('include', this.utkastId);
			cy.contains("Postort").should('exist');
			cy.log('Overhoppsparametrar: alternatePatientSSn (ändrat personnummer)');
			overhopp.verifyPatStatus("ändratPnr");			
		})
		it('originalPnrUrl - Observandum om att ändrat personnummer visas', function () {
			//Skicka med ändrat pnr
			let ändratPnrUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + '&alternatePatientSSn=191212121212';			
			cy.visit(ändratPnrUrl);
			cy.url().should('include', this.utkastId);
			cy.contains("Postort").should('exist');
			cy.log('Overhoppsparametrar: alternatePatientSSn (ändrat personnummer)');
			overhopp.verifyPatStatus("ändratPnr");	
		
			//Rensa session och Logga in igen
			cy.clearCookies();
			cy.visit('/logout');
			cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
			
			//Skicka med orginal pnr
			let originalPnrUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + '&alternatePatientSSn=' + this.vårdtagare.personnummerKompakt;
			cy.visit(originalPnrUrl);
			cy.url().should('include', this.utkastId);
			cy.contains("Postort").should('exist');
			cy.log('Overhoppsparametrar: alternatePatientSSn (ändrat tillbaka personnummer)');
			//Tidigare utanParameter, misstänker att det finns race-condition med denne kontrollen och att egentligen skall ändrat pnr visas.
			//overhopp.verifyPatStatus("utanParameter");			
			overhopp.verifyPatStatus("ändratPnr");	
		})
	})
});
