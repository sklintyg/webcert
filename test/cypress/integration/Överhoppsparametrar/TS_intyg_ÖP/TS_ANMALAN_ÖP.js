/* globals context cy */
/// <reference types="Cypress" />
import * as intyg from '../../../support/TS_intyg/tsAnmalanIntyg'
import * as overhopp from '../../../support/overhopp_helpers'

var utkstId = "";
var vpersonal = "";
var venhet = "";

// TSTRK1031 = Transportstyrelsens läkarintyg, diabetes

describe('TS-Anmälan-intyg', function () {
    
    before(function() {
        cy.fixture('TS_intyg/minTsAnmalan').as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').as('vårdtagare');
    })

    beforeEach(function() {
        cy.skapaTsAnmälanUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("TS-Anmälan-utkast med id " + utkastId + " skapat och används i testfallet");
        });
    });

    it('skapar ett TS-Anmälan intyg', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const normalUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + 
        '&fornamn=Balanar&efternamn=Nattj%C3%A4gare&postadress=Bryggaregatan%2011&postnummer=65340&postort=Karlstad';
        const avlidenUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + '&avliden=true';
        const ändratNamnUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + 
        '&fornamn=Gunilla&efternamn=Karlsson&postadress=Bryggaregatan%2011&postnummer=65340&postort=Karlstad';
        const originalPnrUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + 
        '&postadress=Bryggaregatan%2011&postnummer=65340&postort=Karlstad&alternatePatientSSn=' + this.vårdtagare.personnummerKompakt;
        const ändratPnrUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + 
        '&postadress=Bryggaregatan%2011&postnummer=65340&postort=Karlstad&alternatePatientSSn=191212121212';
        const reservNrUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + 
        '&postadress=Bryggaregatan%2011&postnummer=65340&postort=Karlstad&alternatePatientSSn=19270926308A';
        //reservnummer = 19270926308A
        //samordningsnummer = 196812732391

        // --- Verifera observandum triggade utifrån olika uthoppsparametrar i intygsutkast --- //

        // Verifiera att inga observandum visas när inga överhoppsparametrar skickas med
        intyg.besökÖnskadUrl(normalUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);
        cy.contains("Anmälan avser").should('exist');
        cy.get('#ta-bort-utkast').should('exist');
        cy.log('Överhoppsparametrar: Ingen');
        overhopp.verifyPatStatus("utanParameter");

        var utkstId = "";
        cy.get('@utkastId').then((ui) => {
            utkstId = ui
        });

        function loggaUtOchInVerifieraPara(vpersonal, venhet, url, paraNamn, infoIIntyg, statusAttVerifiera, intygStatus) {
            cy.clearCookies();
            cy.visit('/logout');
            cy.loggaInVårdpersonalIntegrerat(vpersonal, venhet);
            cy.visit(url);
            cy.url().should('include', utkstId);
            cy.log('Överhoppsparametrar: ' + paraNamn);
            cy.contains(infoIIntyg);
            intygStatus === "utkast" ? cy.get('#ta-bort-utkast').should('exist') : cy.get('#makuleraBtn').should('exist');
            overhopp.verifyPatStatus(statusAttVerifiera);
        }

        // Verifiera att endast observandum med information om att patienten är avliden visas
        loggaUtOchInVerifieraPara(this.vårdpersonal, this.vårdenhet, avlidenUrl, "avliden", "Anmälan avser", "avliden", "utkast");
        // cy.clearCookies();
        // cy.visit('/logout');
        // cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        // cy.visit(avlidenUrl);
        // cy.url().should('include', this.utkastId);
        // cy.log('Överhoppsparametrar: avliden');
        // cy.contains("Anmälan avser");
        // cy.get('#ta-bort-utkast').should('exist');
        // overhopp.verifyPatStatus("avliden");

        // Verifiera att endast observandum om att patientens namn skiljer sig från Journalsystemet visas
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(ändratNamnUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Överhoppsparametrar: fornamn & efternamn');
        cy.contains("Anmälan avser");
        cy.get('#ta-bort-utkast').should('exist');
        overhopp.verifyPatStatus("patientNamn");

        // Verifiera att endast observandum om att personen har ett sammordningsnummer kopplat till ett reservnummer
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(reservNrUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Överhoppsparametrar: alternatePatientSSn (reservnummer)');
        cy.contains("Anmälan avser");
        cy.get('#ta-bort-utkast').should('exist');
        overhopp.verifyPatStatus("reservnummer");

        // Verifiera att inga observandum visas när inga överhoppsparametrar skickas med
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(normalUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Överhoppsparametrar: Ingen');
        cy.contains("Anmälan avser");
        cy.get('#ta-bort-utkast').should('exist');
        overhopp.verifyPatStatus("utanParameter");

        // Verifiera att observandum om att patientens personnummer har ändrats visas
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(ändratPnrUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Överhoppsparametrar: alternatePatientSSn (ändrat personnummer)');
        cy.contains("Anmälan avser");
        cy.get('#ta-bort-utkast').should('exist');
        overhopp.verifyPatStatus("ändratPnr");

        // Verifiera att inga observandum visas när inga överhoppsparametrar skickas med
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(originalPnrUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Överhoppsparametrar: alternatePatientSSn (ändrat tillbaka personnummer)');
        cy.contains("Anmälan avser");
        cy.get('#ta-bort-utkast').should('exist');
        overhopp.verifyPatStatus("ändratPnr");

        //Fyll i och signera intyget
        intyg.sektionIdentitet(this.intygsdata.identitet);
        intyg.sektionAnmälan(this.intygsdata.anmälan);
        intyg.sektionMedicinskaFörhållanden(this.intygsdata.medicinskaFörhållanden);
        intyg.sektionBedömning(this.intygsdata.bedömning);
        intyg.sektionInfoOmBeslut(this.intygsdata.info);
        intyg.signeraOchSkicka();

        // --- Verifera observandum triggade utifrån olika uthoppsparametrar i intyg --- //

        // Verifiera att endast observandum med information om att patienten är avliden visas
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(avlidenUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Överhoppsparametrar: avliden');
        cy.contains("Anmälan avser");
        cy.get('#makuleraBtn').should('exist');
        overhopp.verifyPatStatus("avliden");

        // Verifiera att endast observandum om att patientens namn skiljer sig från Journalsystemet visas
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(ändratNamnUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Överhoppsparametrar: fornamn & efternamn');
        cy.contains("Anmälan avser");
        cy.get('#makuleraBtn').should('exist');
        overhopp.verifyPatStatus("patientNamn");

        // Verifiera att endast observandum om att personen har ett sammordningsnummer kopplat till ett reservnummer
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(reservNrUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Överhoppsparametrar: alternatePatientSSn (reservnummer)');
        cy.contains("Anmälan avser");
        cy.get('#makuleraBtn').should('exist');
        overhopp.verifyPatStatus("reservnummer");

        // Verifiera att inga observandum visas när inga överhoppsparametrar skickas med
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(normalUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Överhoppsparametrar: Ingen');
        cy.contains("Anmälan avser");
        cy.get('#makuleraBtn').should('exist');
        overhopp.verifyPatStatus("utanParameter");

        // Verifiera att observandum om att patientens personnummer har ändrats visas
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(ändratPnrUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Överhoppsparametrar: alternatePatientSSn (ändrat personnummer)');
        cy.contains("Anmälan avser");
        cy.get('#makuleraBtn').should('exist');
        overhopp.verifyPatStatus("ändratPnr");

        // Verifiera att inga observandum visas när inga överhoppsparametrar skickas med
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(originalPnrUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Överhoppsparametrar: alternatePatientSSn (ändrat tillbaka personnummer)');
        cy.contains("Anmälan avser");
        cy.get('#makuleraBtn').should('exist');
        overhopp.verifyPatStatus("utanParameter");

        // // Skickar intyget till FK samt populerar pdl-arrayen med förväntad logpost "Utskrift" med argument att det är skickat till FK
        // intyg.skickaTillFk();
    });
});
