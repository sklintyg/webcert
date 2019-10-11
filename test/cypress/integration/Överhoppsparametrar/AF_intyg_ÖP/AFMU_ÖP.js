/* globals context cy */
/// <reference types="Cypress" />
import * as intyg from '../../../support/AF_intyg/afmuIntyg'
import * as overhopp from '../../../support/overhopp_helpers'

// LUAE_FS = Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga

describe('AFMU-intyg', function () {
    
    before(function() {
        cy.fixture('AF_intyg/minAFMUData').as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').as('vårdtagare');
    })

    beforeEach(function() {
        cy.skapaAFMUUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("AFMU-utkast med id " + utkastId + " skapat och används i testfallet");
        });
    });

    it('skapar ett AFMU intyg', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const normalUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id;
        const avlidenUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + '&avliden=true';
        const ändratNamnUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + '&fornamn=Gunilla&efternamn=Karlsson';
        const originalPnrUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + '&alternatePatientSSn=' + this.vårdtagare.personnummerKompakt;
        const ändratPnrUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + '&alternatePatientSSn=191212121212';
        const reservNrUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + '&alternatePatientSSn=19270926308A';
        //reservnummer = 19270926308A
        //samordningsnummer = 196812732391

        // --- Verifera observandum triggade utifrån olika uthoppsparametrar i intygsutkast --- //

        // Verifiera att inga observandum visas när inga överhoppsparametrar skickas med
        intyg.besökÖnskadUrl(normalUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);
        cy.contains("Funktionsnedsättning").should('exist');
        cy.log('Överhoppsparametrar: Ingen');
        overhopp.verifyPatStatus("utanParameter");

        // Verifiera att endast observandum med information om att patienten är avliden visas
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(avlidenUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Överhoppsparametrar: avliden');
        cy.contains("Funktionsnedsättning");
        overhopp.verifyPatStatus("avliden");

        // Verifiera att endast observandum om att patientens namn skiljer sig från Journalsystemet visas
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(ändratNamnUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Överhoppsparametrar: fornamn & efternamn');
        cy.contains("Funktionsnedsättning");
        overhopp.verifyPatStatus("patientNamn");

        // Verifiera att endast observandum om att personen har ett sammordningsnummer kopplat till ett reservnummer
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(reservNrUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Överhoppsparametrar: alternatePatientSSn (reservnummer)');
        cy.contains("Funktionsnedsättning");
        overhopp.verifyPatStatus("reservnummer");

        // Verifiera att inga observandum visas när inga överhoppsparametrar skickas med
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(normalUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Överhoppsparametrar: Ingen');
        cy.contains("Funktionsnedsättning");
        overhopp.verifyPatStatus("utanParameter");

        // Verifiera att observandum om att patientens personnummer har ändrats visas
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(ändratPnrUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Överhoppsparametrar: alternatePatientSSn (ändrat personnummer)');
        cy.contains("Funktionsnedsättning");
        overhopp.verifyPatStatus("ändratPnr");

        // Verifiera att inga observandum visas när inga överhoppsparametrar skickas med
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(originalPnrUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Överhoppsparametrar: alternatePatientSSn (ändrat tillbaka personnummer)');
        cy.contains("Funktionsnedsättning");
        overhopp.verifyPatStatus("ändratPnr");

        //Fyll i och signera intyget
        intyg.sektionFunktionsnedsättning(this.intygsdata.funktionsnedsättning);
        intyg.sektionAktivitetsbegränsning(this.intygsdata.funktionsnedsättning, this.intygsdata.aktivitetsbegränsning);
        intyg.sektionUtredningBehandling(this.intygsdata.utredning);
        intyg.sektionArbetetsPåverkanSjukdomSkada(this.intygsdata.påverkan);
        intyg.sektionÖvrigt(this.intygsdata.övrigt);
        intyg.signeraOchSkicka();

        // --- Verifera observandum triggade utifrån olika uthoppsparametrar i intyg --- //

        // Verifiera att endast observandum med information om att patienten är avliden visas
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(avlidenUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Överhoppsparametrar: avliden');
        cy.contains("Funktionsnedsättning");
        overhopp.verifyPatStatus("avliden");

        // Verifiera att endast observandum om att patientens namn skiljer sig från Journalsystemet visas
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(ändratNamnUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Överhoppsparametrar: fornamn & efternamn');
        cy.contains("Funktionsnedsättning");
        overhopp.verifyPatStatus("patientNamn");

        // Verifiera att endast observandum om att personen har ett sammordningsnummer kopplat till ett reservnummer
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(reservNrUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Överhoppsparametrar: alternatePatientSSn (reservnummer)');
        cy.contains("Funktionsnedsättning");
        overhopp.verifyPatStatus("reservnummer");

        // Verifiera att inga observandum visas när inga överhoppsparametrar skickas med
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(normalUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Överhoppsparametrar: Ingen');
        cy.contains("Funktionsnedsättning");
        overhopp.verifyPatStatus("utanParameter");

        // Verifiera att observandum om att patientens personnummer har ändrats visas
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(ändratPnrUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Överhoppsparametrar: alternatePatientSSn (ändrat personnummer)');
        cy.contains("Funktionsnedsättning");
        overhopp.verifyPatStatus("ändratPnr");

        // Verifiera att inga observandum visas när inga överhoppsparametrar skickas med
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(originalPnrUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Överhoppsparametrar: alternatePatientSSn (ändrat tillbaka personnummer)');
        cy.contains("Funktionsnedsättning");
        overhopp.verifyPatStatus("utanParameter");

        // // Skickar intyget till FK samt populerar pdl-arrayen med förväntad logpost "Utskrift" med argument att det är skickat till FK
        // intyg.skickaTillFk();
    });
});
