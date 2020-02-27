/* globals context cy */
/// <reference types="Cypress" />
//import * as intyg from '../../../support/FK_intyg/lisjpIntyg'
import * as overhopp from '../../../support/overhopp_helpers'
import * as intyg from '../../../support/SKR_intyg/AG114intyg'

// AG114= Läkarintyg om arbetsförmåga – arbetsgivaren, AG114

describe('AG114 test av Överhoppsparametrar', function () {
    
    before(function() {
        cy.fixture('SKR_intyg/maxAG114Data').as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').as('vårdtagare');
    })

    beforeEach(function() {
        cy.skapaAG114Utkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("AG114-utkast med id " + utkastId + " skapat och används i testfallet");
        });
    });

    it('skapar ett AG114 intyg', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const normalUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id;
        const avlidenUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + '&avliden=true';
        const ändratNamnUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + '&fornamn=Gunilla&efternamn=Karlsson';
        const originalPnrUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + '&alternatePatientSSn=' + this.vårdtagare.personnummerKompakt;
        const ändratPnrUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + '&alternatePatientSSn=191212121212';
        const reservNrUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + '&alternatePatientSSn=19270926308A';
        //reservnummer = 19270926308A
        //samordningsnummer = 196812732391

        
        // Verifiera att inga observandum visas när inga överhoppsparametrar skickas med
        intyg.besökÖnskadUrl(normalUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);
        
        cy.contains("Grund för medicinskt underlag").should('exist');
        cy.log('Överhoppsparametrar: Ingen');
        overhopp.verifyPatStatus("utanParameter");
        
        // Verifiera att endast observandum med information om att patienten är avliden inte visas
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(avlidenUrl);
        cy.url().should('include', this.utkastId);
        cy.contains("Grund för medicinskt underlag");
        cy.log('Överhoppsparametrar: avliden');
        overhopp.verifyPatStatus("avliden");
        
        // Verifiera att endast observandum om att patientens namn skiljer sig från Journalsystemet visas
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(ändratNamnUrl);
        cy.url().should('include', this.utkastId);
        cy.contains("Grund för medicinskt underlag");
        cy.log('Överhoppsparametrar: fornamn & efternamn');
        overhopp.verifyPatStatus("patientNamn");
        
        // Verifiera att endast observandum om att personen har ett sammordningsnummer kopplat till ett reservnummer
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(reservNrUrl);
        cy.url().should('include', this.utkastId);
        cy.contains("Grund för medicinskt underlag");
        cy.log('Överhoppsparametrar: alternatePatientSSn (reservnummer)');
        overhopp.verifyPatStatus("reservnummer");
        
        // Verifiera att inga observandum visas när inga överhoppsparametrar skickas med
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(normalUrl);
        cy.url().should('include', this.utkastId);
        cy.contains("Grund för medicinskt underlag");
        cy.log('Överhoppsparametrar: Ingen');
        overhopp.verifyPatStatus("utanParameter");
        
        // Verifiera att observandum om att patientens personnummer har ändrats visas
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(ändratPnrUrl);
        cy.url().should('include', this.utkastId);
        cy.contains("Grund för medicinskt underlag");
        cy.log('Överhoppsparametrar: alternatePatientSSn (ändrat personnummer)');
        overhopp.verifyPatStatus("ändratPnr");
        
        // Verifiera att ändrat personnummer visas
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(originalPnrUrl);
        cy.url().should('include', this.utkastId);
        cy.contains("Grund för medicinskt underlag");
        cy.log('Överhoppsparametrar: alternatePatientSSn (ändrat tillbaka personnummer)');
        overhopp.verifyPatStatus("ändratPnr");
       
        //Fyll i och signera intyget
        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag);
        intyg.sektionSysselsättning(this.intygsdata.sysselsättning);
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        intyg.sektionArbetsförmåga(this.intygsdata.arbetsförmåga);
        intyg.sektionBedömning(this.intygsdata.bedömning);
        intyg.sektionÖvrigaUpplysningar(this.intygsdata.övrigt);
        intyg.sektionKontaktArbetsgivaren(this.intygsdata.kontakt);
        intyg.signera();

        
 // --- Verifera observandum triggade utifrån olika uthoppsparametrar i intyg --- //

        // Verifiera att endast observandum med information om att patienten är avliden  inte visas
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(avlidenUrl);
        cy.url().should('include', this.utkastId);
        cy.contains("Grund för medicinskt underlag");
        cy.log('Överhoppsparametrar: avliden');
        overhopp.verifyPatStatus("avliden");

        // Verifiera att endast observandum om att patientens namn skiljer sig från Journalsystemet visas
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(ändratNamnUrl);
        cy.url().should('include', this.utkastId);
        cy.contains("Grund för medicinskt underlag");
        cy.log('Överhoppsparametrar: fornamn & efternamn');
        overhopp.verifyPatStatus("patientNamn");

        // Verifiera att endast observandum om att personen har ett sammordningsnummer kopplat till ett reservnummer
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(reservNrUrl);
        cy.url().should('include', this.utkastId);
        cy.contains("Grund för medicinskt underlag");
        cy.log('Överhoppsparametrar: alternatePatientSSn (reservnummer)');
        overhopp.verifyPatStatus("reservnummer");

        // Verifiera att inga observandum visas när inga överhoppsparametrar skickas med
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(normalUrl);
        cy.url().should('include', this.utkastId);
        cy.contains("Grund för medicinskt underlag");
        cy.log('Överhoppsparametrar: Ingen');
        overhopp.verifyPatStatus("utanParameter");

        // Verifiera att observandum om att patientens personnummer har ändrats visas
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(ändratPnrUrl);
        cy.url().should('include', this.utkastId);
        cy.contains("Grund för medicinskt underlag");
        cy.log('Överhoppsparametrar: alternatePatientSSn (ändrat personnummer)');
        overhopp.verifyPatStatus("ändratPnr");

        // Verifiera att inga observandum visas när inga överhoppsparametrar skickas med
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(originalPnrUrl);
        cy.url().should('include', this.utkastId);
        cy.contains("Grund för medicinskt underlag");
        cy.log('Överhoppsparametrar: alternatePatientSSn (ändrat tillbaka personnummer)');
        overhopp.verifyPatStatus("utanParameter");

    });
});
