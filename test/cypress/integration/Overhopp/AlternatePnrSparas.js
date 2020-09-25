/* globals context cy */
/// <reference types="Cypress" />
import * as intyg from '../../support/FK_intyg/lisjpIntyg'

// LISJP = Läkarintyg för sjukpenning, FK 7804

describe('Funktionalitet kring ändrat personnummer', function () {
    var nyttPersonnummer = "194401786530";
    before(function() {
        
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdtagare/balanarNattjagare').as('vårdtagare');
        cy.fixture('vårdtagare/haraldOlsson').as('vårdtagare2');
        cy.fixture('vårdenheter/NMT_vg3_ve1').as('vårdenhetParalell');
        cy.fixture('vårdenheter/NMT_vg1_ve1').as('vårdenhet1');
    });

    beforeEach(function() {
        cy.skapaLISJPIfylltUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-Förifyllt utkast med id " + utkastId + " skapat och används i testfallet");
            
        });
    });
    
    it('skapar en maximalt ifylld LISJP och loggar in med parametern alternatePatientSSn', function () {
        //Loggar in på enhet som inte har skrivrättigheter
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhetParalell,true);//19571031-2686 vid create draft
        const önskadUrl2 = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhetParalell.id + "&alternatePatientSSn=" + this.vårdtagare2.personnummerKompakt ;//19440178-6530
        intyg.besökÖnskadUrl(önskadUrl2, this.vårdpersonal, this.vårdenhetParalell, this.utkastId);
        cy.contains(this.vårdtagare2.personnummer);
        
       //Loggar in på enhet som skapade intyget
        intyg.loggaUtLoggaIn(this.vårdpersonal,this.vårdenhet);
        const sammaPersNrUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id;
        intyg.besökÖnskadUrl(sammaPersNrUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);
        cy.contains(this.vårdtagare.personnummer);  //kontrollera att intyget inte ändrats utan är 19571031-2686 eftersom intygsutkastet är på en enhet som inte har skrivrättigheter
        
       //Loggar in på enhet som skapade intyget med parametern alternatePatientSSn och byter personnummer
        intyg.loggaUtLoggaIn(this.vårdpersonal,this.vårdenhet);
        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + "&alternatePatientSSn=" +this.vårdtagare2.personnummerKompakt;
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);
        cy.contains( ' f.d. ' + this.vårdtagare.personnummer);//Harald Erik Olsson Väster - 19440178-6530 f.d. 19571031-2686 Personnumret ändrades i detta steg
                
        intyg.signera();
        
        //Loggar in på enhet som skapade intyget utan parametern alternatePatientSSn för att se så att ändringen slagit in
        intyg.loggaUtLoggaIn(this.vårdpersonal,this.vårdenhet);
        intyg.besökÖnskadUrl(sammaPersNrUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);
        cy.contains( this.vårdtagare2.personnummer);//19440178-6530 som personnumret ändrades till i steget ovan
        
        //Loggar in på enhet som skapade intyget med parametern alternatePatientSSn för att i nästa steg se så att inte ändringen slagit in eftersom det är ett intyg
        intyg.loggaUtLoggaIn(this.vårdpersonal,this.vårdenhet);
        const önskadUrl3 = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + "&alternatePatientSSn=" + this.vårdtagare.personnummerKompakt;
        intyg.besökÖnskadUrl(önskadUrl3, this.vårdpersonal, this.vårdenhet, this.utkastId);
        cy.contains(this.vårdtagare.personnummer);//kontrollera att intyget är 19571031-2686 intyget är ett intyg där personnumret ändras i vyn men inte i själva intyget        
       
        intyg.loggaUtLoggaIn(this.vårdpersonal,this.vårdenhet);
        intyg.besökÖnskadUrl(sammaPersNrUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);
        cy.contains( this.vårdtagare2.personnummer);//ändrades inte eftersom det är ett intyg
        
       //Loggar in med hjälp av Sammanhållen journalföring. Personnummer ändras inte varaktigt eftersom det är ett intyg
        intyg.loggaUtLoggaIn(this.vårdpersonal,this.vårdenhet1);//loggar in och byter personnummer till 19571031-2686 personnumret ändras inte på intyget men i Webcert
        const önskadurl3 = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet1.id + "&alternatePatientSSn=" + this.vårdtagare.personnummerKompakt + "&sjf=true" ;
        intyg.besökÖnskadUrl(önskadurl3, this.vårdpersonal, this.vårdenhet1, this.utkastId);
        cy.contains( this.vårdtagare.personnummer);
        cy.contains( ' f.d. ' + this.vårdtagare2.personnummer);
       
        //här kontrolleras att personnummer inte ändras på intyget.
        intyg.loggaUtLoggaIn(this.vårdpersonal,this.vårdenhet1);//loggar in och personnumret  har inte ändrats i steget innan utan det visas 19440178-6530
        const önskadurl4 = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet1.id +"&sjf=true" ;
        intyg.besökÖnskadUrl(önskadurl4, this.vårdpersonal, this.vårdenhet1, this.utkastId);
        cy.contains( this.vårdtagare2.personnummer);
                
    });
});
