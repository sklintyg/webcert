/* globals context cy */
/// <reference types="Cypress" />
import * as intyg from '../../../support/FK_intyg/lisjpIntyg'
import * as pdl from '../../../support/pdl_helpers'

/*

Instruktioner för bulk-körning lisjp med srs-riskprediktion.

 - Korta ned teststrängar till så korta som möjligt t.ex. "a" i maxLisjpData för snabbare test
 - Ta bort rad2 och rad3 under diagnos i maxLisjpData för snabbare test
 - Kör integrationen

Användningområden
 - Rehabstöd pågående sjukfall vy
 
*/


describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
		//Ändra diagnoskod och text i maxLisjpData till en med SRS-stöd
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
			fk.diagnos['rad1'].kod = "M79" 
			fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
		}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => 
				{
				patient.personnummerKompakt = "190304159817"
				patient.personnummer = "19030415-9817"
				}
		
		).as('vårdtagare');
	})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));


		intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag);
        intyg.sektionSysselsättning(this.intygsdata.sysselsättning);
        intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten);
        intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling);
        intyg.sektionBedömning(this.intygsdata.bedömning);
        intyg.sektionÅtgärder(this.intygsdata.åtgärder);
        intyg.sektionÖvrigt(this.intygsdata.övrigt);
        intyg.sektionKontakt(this.intygsdata.kontakt);
        intyg.signera();
		

    });
});

describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190304199813"
			patient.personnummer="19030419-9813"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190304199813",
			"personnummer": "19030419-9813",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190304209802"
			patient.personnummer="19030420-9802"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190304209802",
			"personnummer": "19030420-9802",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190304219819"
			patient.personnummer="19030421-9819"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190304219819",
			"personnummer": "19030421-9819",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190304219819"
			patient.personnummer="19030421-9819"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190304219819",
			"personnummer": "19030421-9819",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190304239817"
			patient.personnummer="19030423-9817"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190304239817",
			"personnummer": "19030423-9817",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190304249808"
			patient.personnummer="19030424-9808"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190304249808",
			"personnummer": "19030424-9808",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190304259815"
			patient.personnummer="19030425-9815"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190304259815",
			"personnummer": "19030425-9815",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190304269806"
			patient.personnummer="19030426-9806"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190304269806",
			"personnummer": "19030426-9806",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190304279813"
			patient.personnummer="19030427-9813"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190304279813",
			"personnummer": "19030427-9813",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190304289804"
			patient.personnummer="19030428-9804"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190304289804",
			"personnummer": "",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190304299811"
			patient.personnummer="19030429-9811"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190304299811",
			"personnummer": "19030429-9811",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190304309800"
			patient.personnummer="19030430-9800"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190304309800",
			"personnummer": "19030430-9800",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190305019812"
			patient.personnummer="19030501-9812"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190305019812",
			"personnummer": "19030501-9812",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190305029803"
			patient.personnummer="19030502-9803"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190305029803",
			"personnummer": "19030502-9803",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190305039810"
			patient.personnummer="19030503-9810"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190305039810",
			"personnummer": "19030503-9810",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190305049801"
			patient.personnummer="19030504-9801"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190305049801",
			"personnummer": "19030504-9801",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190305059818"
			patient.personnummer="19030505-9818"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190305059818",
			"personnummer": "19030505-9818",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190305069809"
			patient.personnummer="19030506-9809"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190305069809",
			"personnummer": "19030506-9809",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190305079816"
			patient.personnummer="19030507-9816"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190305079816",
			"personnummer": "19030507-9816",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190305089807"
			patient.personnummer="19030508-9807"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190305089807",
			"personnummer": "19030508-9807",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190305099814"
			patient.personnummer="19030509-9814"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190305099814",
			"personnummer": "19030509-9814",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190305109803"
			patient.personnummer="19030510-9803"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190305109803",
			"personnummer": "19030510-9803",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190305119810"
			patient.personnummer="19030511-9810"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190305119810",
			"personnummer": "19030511-9810",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190305129801"
			patient.personnummer="19030512-9801"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190305129801",
			"personnummer": "19030512-9801",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190305139818"
			patient.personnummer="19030513-9818"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190305139818",
			"personnummer": "19030513-9818",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190305149809"
			patient.personnummer="19030514-9809"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190305149809",
			"personnummer": "19030514-9809",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190305159816"
			patient.personnummer="19030515-9816"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190305159816",
			"personnummer": "19030515-9816",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190305169807"
			patient.personnummer="19030516-9807"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190305169807",
			"personnummer": "",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190305179814"
			patient.personnummer="19030517-9814"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190305179814",
			"personnummer": "19030517-9814",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190305189805"
			patient.personnummer="19030518-9805"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190305189805",
			"personnummer": "",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190305199812"
			patient.personnummer="19030519-9812"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190305199812",
			"personnummer": "19030519-9812",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190305209801"
			patient.personnummer="19030520-9801"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190305209801",
			"personnummer": "19030520-9801",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190305219818"
			patient.personnummer="19030521-9818"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190305219818",
			"personnummer": "19030521-9818",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190305229809"
			patient.personnummer="19030522-9809"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190305229809",
			"personnummer": "19030522-9809",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190305239816"
			patient.personnummer="19030523-9816"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190305239816",
			"personnummer": "19030523-9816",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190305249807"
			patient.personnummer="19030524-9807"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190305249807",
			"personnummer": "19030524-9807",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190305259814"
			patient.personnummer="19030525-9814"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190305259814",
			"personnummer": "19030525-9814",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190305269821"
			patient.personnummer="19030526-9821"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190305269821",
			"personnummer": "19030526-9821",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190305279812"
			patient.personnummer="19030527-9812"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190305279812",
			"personnummer": "19030527-9812",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190305289803"
			patient.personnummer="19030528-9803"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190305289803",
			"personnummer": "19030528-9803",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190305299810"
			patient.personnummer="19030529-9810"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190305299810",
			"personnummer": "19030529-9810",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190305309809"
			patient.personnummer="19030530-9809"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190305309809",
			"personnummer": "19030530-9809",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190305319816"
			patient.personnummer="19030531-9816"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190305319816",
			"personnummer": "19030531-9816",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190306019803"
			patient.personnummer="19030601-9803"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190306019803",
			"personnummer": "19030601-9803",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190306029810"
			patient.personnummer="19030602-9810"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190306029810",
			"personnummer": "19030602-9810",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190306039801"
			patient.personnummer="19030603-9801"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190306039801",
			"personnummer": "19030603-9801",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190306049818"
			patient.personnummer="19030604-9818"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190306049818",
			"personnummer": "",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190306059809"
			patient.personnummer="19030605-9809"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190306059809",
			"personnummer": "19030605-9809",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190306069816"
			patient.personnummer="19030606-9816"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190306069816",
			"personnummer": "19030606-9816",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190306079807"
			patient.personnummer="19030607-9807"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190306079807",
			"personnummer": "19030607-9807",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190306089814"
			patient.personnummer="19030608-9814"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190306089814",
			"personnummer": "19030608-9814",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190306099805"
			patient.personnummer="19030609-9805"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190306099805",
			"personnummer": "19030609-9805",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190306109810"
			patient.personnummer="19030610-9810"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190306109810",
			"personnummer": "19030610-9810",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190306119801"
			patient.personnummer="19030611-9801"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190306119801",
			"personnummer": "19030611-9801",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190306129818"
			patient.personnummer="19030612-9818"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190306129818",
			"personnummer": "19030612-9818",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190306139809"
			patient.personnummer="19030613-9809"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190306139809",
			"personnummer": "19030613-9809",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190306149816"
			patient.personnummer="19030614-9816"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190306149816",
			"personnummer": "19030614-9816",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190306159823"
			patient.personnummer="19030615-9823"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190306159823",
			"personnummer": "",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190306169814"
			patient.personnummer="19030616-9814"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190306169814",
			"personnummer": "19030616-9814",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190306179805"
			patient.personnummer="19030617-9805"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190306179805",
			"personnummer": "19030617-9805",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190306189812"
			patient.personnummer="19030618-9812"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190306189812",
			"personnummer": "19030618-9812",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190306199803"
			patient.personnummer="19030619-9803"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190306199803",
			"personnummer": "19030619-9803",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190306209818"
			patient.personnummer="19030620-9818"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190306209818",
			"personnummer": "19030620-9818",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190306219809"
			patient.personnummer="19030621-9809"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190306219809",
			"personnummer": "19030621-9809",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190306229816"
			patient.personnummer="19030622-9816"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190306229816",
			"personnummer": "19030622-9816",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190306239807"
			patient.personnummer="19030623-9807"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190306239807",
			"personnummer": "19030623-9807",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190306249814"
			patient.personnummer="19030624-9814"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190306249814",
			"personnummer": "19030624-9814",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190306259805"
			patient.personnummer="19030625-9805"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190306259805",
			"personnummer": "19030625-9805",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190306269812"
			patient.personnummer="19030626-9812"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190306269812",
			"personnummer": "19030626-9812",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190306279803"
			patient.personnummer="19030627-9803"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190306279803",
			"personnummer": "19030627-9803",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190306289810"
			patient.personnummer="19030628-9810"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190306289810",
			"personnummer": "19030628-9810",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190306299801"
			patient.personnummer="19030629-9801"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190306299801",
			"personnummer": "19030629-9801",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190306309816"
			patient.personnummer="19030630-9816"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190306309816",
			"personnummer": "19030630-9816",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190307019802"
			patient.personnummer="19030701-9802"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190307019802",
			"personnummer": "19030701-9802",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190307029819"
			patient.personnummer="19030702-9819"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190307029819",
			"personnummer": "19030702-9819",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190307039800"
			patient.personnummer="19030703-9800"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190307039800",
			"personnummer": "19030703-9800",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190307049817"
			patient.personnummer="19030704-9817"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190307049817",
			"personnummer": "19030704-9817",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190307059808"
			patient.personnummer="19030705-9808"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190307059808",
			"personnummer": "19030705-9808",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190307069815"
			patient.personnummer="19030706-9815"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190307069815",
			"personnummer": "19030706-9815",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190307079806"
			patient.personnummer="19030707-9806"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190307079806",
			"personnummer": "19030707-9806",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190307089813"
			patient.personnummer="19030708-9813"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190307089813",
			"personnummer": "19030708-9813",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190307099804"
			patient.personnummer="19030709-9804"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190307099804",
			"personnummer": "19030709-9804",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190307109819"
			patient.personnummer="19030710-9819"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190307109819",
			"personnummer": "19030710-9819",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190307119800"
			patient.personnummer="19030711-9800"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190307119800",
			"personnummer": "19030711-9800",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190307129817"
			patient.personnummer="19030712-9817"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190307129817",
			"personnummer": "19030712-9817",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190307139808"
			patient.personnummer="19030713-9808"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190307139808",
			"personnummer": "19030713-9808",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190307149815"
			patient.personnummer="19030714-9815"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190307149815",
			"personnummer": "19030714-9815",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190307159806"
			patient.personnummer="19030715-9806"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190307159806",
			"personnummer": "19030715-9806",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190307169813"
			patient.personnummer="19030716-9813"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190307169813",
			"personnummer": "19030716-9813",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190307179804"
			patient.personnummer="19030717-9804"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190307179804",
			"personnummer": "19030717-9804",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190307189811"
			patient.personnummer="19030718-9811"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190307189811",
			"personnummer": "19030718-9811",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190307199802"
			patient.personnummer="19030719-9802"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190307199802",
			"personnummer": "19030719-9802",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190307209833"
			patient.personnummer="19030720-9833"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190307209833",
			"personnummer": "19030720-9833",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190307219808"
			patient.personnummer="19030721-9808"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190307219808",
			"personnummer": "19030721-9808",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190307229815"
			patient.personnummer="19030722-9815"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190307229815",
			"personnummer": "19030722-9815",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190307239806"
			patient.personnummer="19030723-9806"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190307239806",
			"personnummer": "19030723-9806",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190307249813"
			patient.personnummer="19030724-9813"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190307249813",
			"personnummer": "19030724-9813",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190307259804"
			patient.personnummer="19030725-9804"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190307259804",
			"personnummer": "19030725-9804",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190307269811"
			patient.personnummer="19030726-9811"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190307269811",
			"personnummer": "19030726-9811",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190307279802"
			patient.personnummer="19030727-9802"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190307279802",
			"personnummer": "19030727-9802",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190307289819"
			patient.personnummer="19030728-9819"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190307289819",
			"personnummer": "19030728-9819",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190307299800"
			patient.personnummer="19030729-9800"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190307299800",
			"personnummer": "19030729-9800",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190307309815"
			patient.personnummer="19030730-9815"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190307309815",
			"personnummer": "19030730-9815",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190307319806"
			patient.personnummer="19030731-9806"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190307319806",
			"personnummer": "19030731-9806",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190308019819"
			patient.personnummer="19030801-9819"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190308019819",
			"personnummer": "19030801-9819",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190308029800"
			patient.personnummer="19030802-9800"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190308029800",
			"personnummer": "19030802-9800",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190308039817"
			patient.personnummer="19030803-9817"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190308039817",
			"personnummer": "19030803-9817",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190308049808"
			patient.personnummer="19030804-9808"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190308049808",
			"personnummer": "19030804-9808",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190308059815"
			patient.personnummer="19030805-9815"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190308059815",
			"personnummer": "19030805-9815",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190308069806"
			patient.personnummer="19030806-9806"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190308069806",
			"personnummer": "19030806-9806",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190308079813"
			patient.personnummer="19030807-9813"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190308079813",
			"personnummer": "19030807-9813",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190308089804"
			patient.personnummer="19030808-9804"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190308089804",
			"personnummer": "19030808-9804",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190308099811"
			patient.personnummer="19030809-9811"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190308099811",
			"personnummer": "19030809-9811",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190308109800"
			patient.personnummer="19030810-9800"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190308109800",
			"personnummer": "19030810-9800",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190308119817"
			patient.personnummer="19030811-9817"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190308119817",
			"personnummer": "19030811-9817",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190308129808"
			patient.personnummer="19030812-9808"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190308129808",
			"personnummer": "19030812-9808",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190308139815"
			patient.personnummer="19030813-9815"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190308139815",
			"personnummer": "19030813-9815",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190308149806"
			patient.personnummer="19030814-9806"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190308149806",
			"personnummer": "19030814-9806",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190308159813"
			patient.personnummer="19030815-9813"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190308159813",
			"personnummer": "19030815-9813",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190308169804"
			patient.personnummer="19030816-9804"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190308169804",
			"personnummer": "19030816-9804",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190308179811"
			patient.personnummer="19030817-9811"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190308179811",
			"personnummer": "19030817-9811",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190308189802"
			patient.personnummer="19030818-9802"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190308189802",
			"personnummer": "19030818-9802",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190308199819"
			patient.personnummer="19030819-9819"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190308199819",
			"personnummer": "19030819-9819",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190308209808"
			patient.personnummer="19030820-9808"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190308209808",
			"personnummer": "19030820-9808",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190308219815"
			patient.personnummer="19030821-9815"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190308219815",
			"personnummer": "19030821-9815",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190308229806"
			patient.personnummer="19030822-9806"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190308229806",
			"personnummer": "19030822-9806",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190308239839"
			patient.personnummer="19030823-9839"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190308239839",
			"personnummer": "19030823-9839",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190308249804"
			patient.personnummer="19030824-9804"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190308249804",
			"personnummer": "19030824-9804",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190308259811"
			patient.personnummer="19030825-9811"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190308259811",
			"personnummer": "19030825-9811",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190308269802"
			patient.personnummer="19030826-9802"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190308269802",
			"personnummer": "19030826-9802",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190308279819"
			patient.personnummer="19030827-9819"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190308279819",
			"personnummer": "19030827-9819",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190308289800"
			patient.personnummer="19030828-9800"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190308289800",
			"personnummer": "19030828-9800",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190308299858"
			patient.personnummer="19030829-9858"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190308299858",
			"personnummer": "19030829-9858",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190308309806"
			patient.personnummer="19030830-9806"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190308309806",
			"personnummer": "19030830-9806",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190308319813"
			patient.personnummer="19030831-9813"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190308319813",
			"personnummer": "19030831-9813",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190309019800"
			patient.personnummer="19030901-9800"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190309019800",
			"personnummer": "19030901-9800",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190309029817"
			patient.personnummer="19030902-9817"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190309029817",
			"personnummer": "19030902-9817",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190309039808"
			patient.personnummer="19030903-9808"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190309039808",
			"personnummer": "19030903-9808",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190309049815"
			patient.personnummer="19030904-9815"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190309049815",
			"personnummer": "19030904-9815",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190309059806"
			patient.personnummer="19030905-9806"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190309059806",
			"personnummer": "19030905-9806",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190309069813"
			patient.personnummer="19030906-9813"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190309069813",
			"personnummer": "19030906-9813",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190309079804"
			patient.personnummer="19030907-9804"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190309079804",
			"personnummer": "19030907-9804",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190309089811"
			patient.personnummer="19030908-9811"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190309089811",
			"personnummer": "19030908-9811",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190309099802"
			patient.personnummer="19030909-9802"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190309099802",
			"personnummer": "19030909-9802",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190309109817"
			patient.personnummer="19030910-9817"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190309109817",
			"personnummer": "19030910-9817",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190309119808"
			patient.personnummer="19030911-9808"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190309119808",
			"personnummer": "19030911-9808",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190309129815"
			patient.personnummer="19030912-9815"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190309129815",
			"personnummer": "19030912-9815",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190309139806"
			patient.personnummer="19030913-9806"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190309139806",
			"personnummer": "19030913-9806",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190309149813"
			patient.personnummer="19030914-9813"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190309149813",
			"personnummer": "19030914-9813",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190309159804"
			patient.personnummer="19030915-9804"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190309159804",
			"personnummer": "19030915-9804",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190309169811"
			patient.personnummer="19030916-9811"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190309169811",
			"personnummer": "19030916-9811",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190309179802"
			patient.personnummer="19030917-9802"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190309179802",
			"personnummer": "19030917-9802",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190309189819"
			patient.personnummer="19030918-9819"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190309189819",
			"personnummer": "19030918-9819",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190309199800"
			patient.personnummer="19030919-9800"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190309199800",
			"personnummer": "19030919-9800",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190309209815"
			patient.personnummer="19030920-9815"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190309209815",
			"personnummer": "19030920-9815",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190309219806"
			patient.personnummer="19030921-9806"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190309219806",
			"personnummer": "19030921-9806",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190309229813"
			patient.personnummer="19030922-9813"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190309229813",
			"personnummer": "19030922-9813",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190309239804"
			patient.personnummer="19030923-9804"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190309239804",
			"personnummer": "19030923-9804",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190309249811"
			patient.personnummer="19030924-9811"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190309249811",
			"personnummer": "19030924-9811",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190309259802"
			patient.personnummer="19030925-9802"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190309259802",
			"personnummer": "19030925-9802",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190309269819"
			patient.personnummer="19030926-9819"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190309269819",
			"personnummer": "19030926-9819",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190309279800"
			patient.personnummer="19030927-9800"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190309279800",
			"personnummer": "19030927-9800",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190309289817"
			patient.personnummer="19030928-9817"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190309289817",
			"personnummer": "19030928-9817",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190309299808"
			patient.personnummer="19030929-9808"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190309299808",
			"personnummer": "19030929-9808",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190309309813"
			patient.personnummer="19030930-9813"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190309309813",
			"personnummer": "19030930-9813",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190310019807"
			patient.personnummer="19031001-9807"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190310019807",
			"personnummer": "19031001-9807",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190310029814"
			patient.personnummer="19031002-9814"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190310029814",
			"personnummer": "19031002-9814",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190310039805"
			patient.personnummer="19031003-9805"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190310039805",
			"personnummer": "19031003-9805",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190310049812"
			patient.personnummer="19031004-9812"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190310049812",
			"personnummer": "19031004-9812",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190310059803"
			patient.personnummer="19031005-9803"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190310059803",
			"personnummer": "19031005-9803",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190310069810"
			patient.personnummer="19031006-9810"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190310069810",
			"personnummer": "19031006-9810",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190310079801"
			patient.personnummer="19031007-9801"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190310079801",
			"personnummer": "19031007-9801",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190310089818"
			patient.personnummer="19031008-9818"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190310089818",
			"personnummer": "19031008-9818",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190310099809"
			patient.personnummer="19031009-9809"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190310099809",
			"personnummer": "19031009-9809",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190310109814"
			patient.personnummer="19031010-9814"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190310109814",
			"personnummer": "19031010-9814",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190310119805"
			patient.personnummer="19031011-9805"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190310119805",
			"personnummer": "19031011-9805",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190310129812"
			patient.personnummer="19031012-9812"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190310129812",
			"personnummer": "19031012-9812",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190310139803"
			patient.personnummer="19031013-9803"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190310139803",
			"personnummer": "19031013-9803",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190310149810"
			patient.personnummer="19031014-9810"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190310149810",
			"personnummer": "19031014-9810",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190310159801"
			patient.personnummer="19031015-9801"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190310159801",
			"personnummer": "19031015-9801",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190310169818"
			patient.personnummer="19031016-9818"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190310169818",
			"personnummer": "19031016-9818",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190310179809"
			patient.personnummer="19031017-9809"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190310179809",
			"personnummer": "19031017-9809",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190310189816"
			patient.personnummer="19031018-9816"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190310189816",
			"personnummer": "19031018-9816",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190310199807"
			patient.personnummer="19031019-9807"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190310199807",
			"personnummer": "19031019-9807",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190310209812"
			patient.personnummer="19031020-9812"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190310209812",
			"personnummer": "19031020-9812",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190310219803"
			patient.personnummer="19031021-9803"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190310219803",
			"personnummer": "19031021-9803",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190310229810"
			patient.personnummer="19031022-9810"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190310229810",
			"personnummer": "19031022-9810",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190310239801"
			patient.personnummer="19031023-9801"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190310239801",
			"personnummer": "19031023-9801",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190310249818"
			patient.personnummer="19031024-9818"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190310249818",
			"personnummer": "19031024-9818",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190310259809"
			patient.personnummer="19031025-9809"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190310259809",
			"personnummer": "19031025-9809",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190310269816"
			patient.personnummer="19031026-9816"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190310269816",
			"personnummer": "19031026-9816",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190310279807"
			patient.personnummer="19031027-9807"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190310279807",
			"personnummer": "19031027-9807",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190310289830"
			patient.personnummer="19031028-9830"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190310289830",
			"personnummer": "19031028-9830",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190310299805"
			patient.personnummer="19031029-9805"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190310299805",
			"personnummer": "19031029-9805",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190310309810"
			patient.personnummer="19031030-9810"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190310309810",
			"personnummer": "19031030-9810",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});
describe.skip('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').then((fk) => {
fk.diagnos['rad1'].kod = "M79"
fk.diagnos['rad1'].text = "Andra sjukdomstillstånd"
}).as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').then((patient) => {
			patient.personnummerKompakt="190310319801"
			patient.personnummer="19031031-9801"
		
		}).as('vårdtagare');
		this.vårdtagare = {
			"förnamn": "Dummy",
			"efternamn": "SRS last",
			"personnummerKompakt": "190310319801",
			"personnummer": "19031031-9801",
			"postadress": "Bryggaregatan 11",
			"postnummer": "65340",
			"postort": "Karlstad"
			};
		})

    beforeEach(function() {
        //pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        //pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag); 
 intyg.sektionSysselsättning(this.intygsdata.sysselsättning); 
intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten); 
intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling); 
intyg.sektionBedömning(this.intygsdata.bedömning);
intyg.sektionÅtgärder(this.intygsdata.åtgärder);
intyg.sektionÖvrigt(this.intygsdata.övrigt);
intyg.sektionKontakt(this.intygsdata.kontakt);
intyg.signera();
    });
});