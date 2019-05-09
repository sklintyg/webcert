/* globals context cy */
/// <reference types="Cypress" />
import * as intyg from '../../support/FK_intyg/lisjpIntyg'

// LISJP = Läkarintyg för sjukpenning, FK 7804

var pdlEventArray = [];
function pdlEvent(env, actType, actArgs, actLevel, assignment, vgId_mod, vgNamn_mod, veId_mod, veNamn_mod) {
    var loggHandelse = {
        activity: {
            activityType: actType,
            activityLevel: actLevel,
            activityArgs: actArgs,
        },
        user: {
            userId: env.vårdpersonal.hsaId,
            assignment: assignment,
            title: env.vårdpersonal.titel,
            careProvider: {
                careProviderId: vgId_mod,
                careProviderName: vgNamn_mod
            },
            careUnit: {
                careUnitId: veId_mod,
                careUnitName: veNamn_mod
            }
        },
        resources: {
            resource: {
                patient: {
                    patientId: env.vårdtagare.personnummerKompakt
                },
                careProvider: {
                    careProviderId: env.vårdenhet.vårdgivareId,
                    careProviderName: env.vårdenhet.vårdgivareNamn
                },
                careUnit: {
                    careUnitId: env.vårdenhet.id,
                    careUnitName: env.vårdenhet.namn
                }
            }
        }
    }
    return loggHandelse;
}

describe('LISJP-intyg', function () {
    
    before(function() {
        cy.fixture('FK_intyg/minLisjpData').as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/tolvanTolvansson').as('vårdtagare');
    })

    beforeEach(function() {
        pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            pdlEventArray.push(pdlEvent(this, "Skriva", undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));    
        });
    });

    it('skapar en minimalt ifylld LISJP', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        cy.visit(önskadUrl);

        // Om vi dirigeras till sidan som säger att 'Intygsutkastet är raderat'
        // så försöker vi igen eftersom det antagligen gick för snabbt.
        cy.get('body').then(($body) => {
            if ($body.text().includes('Intygsutkastet är raderat och kan därför inte längre visas.')) {
                cy.log("Kom till 'Intygetsutkastet är raderat', antagligen gick det för snabbt. Provar igen.");
                cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet); // Vi behöver logga in igen
                cy.visit(önskadUrl);
            }
        });
        cy.url().should('include', this.utkastId);
        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        pdlEventArray.push(pdlEvent(this, "Läsa", undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag);
        intyg.sektionSysselsättning(this.intygsdata.sysselsättning);
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        pdlEventArray.push(pdlEvent(this, "Skriva", undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        
        // Lite special logga ut/logga in -variant för att sedan öppna intyget på nytt med en ny session
        cy.visit('/error.jsp?reason=logout');
        cy.clearCookies();
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(önskadUrl);
        cy.url().should('include', this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i resten av intyget
        pdlEventArray.push(pdlEvent(this, "Läsa", undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten);
        intyg.sektionBedömning(this.intygsdata.bedömning);
        intyg.sektionÅtgärder(this.intygsdata.åtgärder);
        pdlEventArray.push(pdlEvent(this, "Skriva", undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Signerar intyget och populerar pdl-arrayen med förväntade logposter "Signera" och "Läsa"
        intyg.signera();
        pdlEventArray.push(pdlEvent(this, "Signera", undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        pdlEventArray.push(pdlEvent(this, "Läsa", undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Skickar intyget till FK samt populerar pdl-arrayen med förväntad logpost "Utskrift" med argument att det är skickat till FK
        intyg.skickaTillFk();
        pdlEventArray.push(pdlEvent(this, "Utskrift", "Intyg skickat till mottagare FKASSA", this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Introducerar en wait då skrivUt går så fort att man riskerar att få samma timestamp som för "skicka"
        cy.wait(1500);

        // Skriver ut intyget samt populerar pdl-arrayen med förväntad logpost "Utskrift"
        intyg.skrivUt("fullständigt", this.utkastId);
        pdlEventArray.push(pdlEvent(this, "Utskrift", "Intyg utskrivet", this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        cy.log("Testar SJF");

        // Lite special logga ut/logga in -variant för att sedan öppna intyget på nytt med en ny session och SJF (Sammanhållen journalföring)
        cy.visit('/error.jsp?reason=logout');
        cy.clearCookies();
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet_2);

        const sjfUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet_2.id + "&sjf=true";
        cy.visit(sjfUrl);

        // Om vi inte väntar på (valfritt) elementet nedan i intyget
        // så kommer "utskrift" att inträffa före "läsa"
        cy.contains("Smittbärarpenning");

        cy.url().should('include', this.utkastId);
        pdlEventArray.push(pdlEvent(this, "Läsa", "Läsning i enlighet med sammanhållen journalföring", this.utkastId, this.vårdenhet_2.uppdragsnamn, this.vårdenhet_2.vårdgivareId, this.vårdenhet_2.vårdgivareNamn, this.vårdenhet_2.id, this.vårdenhet_2.namn));
        cy.log(this.utkastId + this.vårdenhet_2.vårdgivareId + this.vårdenhet_2.vårdgivareNamn + this.vårdenhet_2.id + this.vårdenhet_2.namn);

        // Skriver ut intyget samt populerar pdl-arrayen med förväntad logpost "Utskrift"
        intyg.skrivUt("minimalt", this.utkastId);
        pdlEventArray.push(pdlEvent(this, "Utskrift", "Intyg utskrivet. Läsning i enlighet med sammanhållen journalföring", 
            this.utkastId, this.vårdenhet_2.uppdragsnamn, this.vårdenhet_2.vårdgivareId, this.vårdenhet_2.vårdgivareNamn, this.vårdenhet_2.id, this.vårdenhet_2.namn));

        cy.log("Testar återigen utan SJF");

        // Lite special logga ut/logga in -variant för att sedan öppna intyget på nytt med en ny session
        cy.visit('/error.jsp?reason=logout');
        cy.clearCookies();
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        cy.visit(önskadUrl);
        cy.contains("Smittbärarpenning"); // Vänta på att intyget ska laddas färdigt
        pdlEventArray.push(pdlEvent(this, "Läsa", undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: Bug?! Varför blir det 2 "Läsa" på rad?
        pdlEventArray.push(pdlEvent(this, "Läsa", undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        /*
        // Förnya intyget -> utkast skapas. Populerar pdl-arrayen med förväntade logposter "Skriva" och "Läsa"
        cy.url().should('include', this.utkastId).then(() => {
            cy.contains('Intyget är tillgängligt för patienten').should('exist');
        })
        //intyg.fornya();
        */

        cy.verifieraPdlLoggar(pdlEventArray);
    });
});
