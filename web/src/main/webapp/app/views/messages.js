/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/* jshint maxlen: false */
angular.module('webcert').constant('webcert.messages', {
    'sv': {
        'webcert.header': 'Webcert',
        'webcert.description': 'Välkommen till Webcert.',
        'dashboard.title': 'Mina andra enheter',
        'dashboard.unanswered.title': 'Frågor och svar',
        'dashboard.unsigned.title': 'Ej signerade utkast',
        'dashboard.about.title': 'Om Webcert',

        //labels
        'label.default-intyg-type': 'Välj typ av intyg',

        'label.unsignedintyg': 'Ej signerade intyg',
        'label.unansweredintyg': 'Intyg med ej hanterad fråga',
        'label.readytosignintyg': 'Intyg färdiga att signera (massignering)',
        'label.showallintyg': 'Visa alla intyg',
        'label.showfewerintyg': 'Visa färre intyg',
        'label.patient': 'Patient:',
        'label.signselectedintyg': 'Signera valda intyg',

        'label.confirmaddress': 'Återanvänd uppgifter',
        'label.confirmsign': 'Signera intyget',
        'label.copyintyg': 'Kopiera intyg',
        'label.fornyaintyg': 'Förnya intyg',
        'label.ersattintyg': 'Ersätt intyg',

        'label.qaonlywarning': 'Du har valt att lämna frågor och svar',
        'label.qaonlywarning.body': '<p>Observera att intyg ska utfärdas via journalsystemet och inte via Webcert.</p><p>Information i Webcert som inte är frågor och svar kan inte visas i journalsystemet.</p>',

        'label.qacheckhanterad.title': 'Markera besvarade frågor som hanterade',
        'label.qacheckhanterad.body': '<p>Det finns besvarade frågor som inte är markerade som hanterade.<br>En besvarad fråga som markeras som hanterad anses avslutad och flyttas till \"Hanterade frågor och svar\"</p>',
        'label.qacheckhanterad.igen': 'Visa inte det här meddelandet igen',
        'label.qacheckhanterad.checkbox': 'Visa inte det här meddelandet igen',
        'label.qacheckhanterad.help.checkbox': 'Visa inte det här meddelandet igen',
        'label.qacheckhanterad.hanterad': 'Hanterade',
        'label.qacheckhanterad.ejhanterad': 'Ej Hanterade',
        'label.qacheckhanterad.tillbaka': 'Tillbaka',

        // validation messages
        'validation.invalidfromdate': 'Från-datum är felaktigt. Använd formatet ÅÅÅÅ-MM-DD',
        'validation.invalidtodate': 'Till-datum är felaktigt. Använd formatet ÅÅÅÅ-MM-DD',
        'validation.invalidtobeforefromdate': 'Till-datum är före från-datum.',

        //info messages
        'info.nounsignedintygfound': '<strong>Inga ej signerade intyg hittades.</strong>',
        'info.nounsigned.intyg.for.unit': '<strong>Inga ej signerade intyg hittades på enheten.</strong>',
        'info.nounansweredintygfound': '<strong>Inga intyg med ohanterade frågor hittades.</strong>',
        'info.noreadytosignintygfound': '<strong>Inga klarmarkerade intyg hittades.</strong>',
        'info.loadingintyg': '<strong>Laddar intyg...</strong>',
        'info.loadingdata': '<strong>Uppdaterar lista...</strong>',
        'info.nounanswered.qa.for.unit': '<strong>Samtliga frågor och svar är hanterade. Det finns inget att åtgärda.</strong>',
        'info.nointygfound': '<strong>Inga intyg hittades.</strong>',
        'info.query.noresults': '<strong>Sökningen gav inga resultat.</strong>',
        'info.query.error': '<strong>Sökningen kunde inte utföras.</strong>',
        'info.intygload.error': '<strong>Kunde inte hämta intyg.</strong>',
        'info.intygload.offline': '<strong>Intygstjänsten ej tillgänglig, endast Intyg utfärdade av Webcert visas.</strong>',
        'info.running.query': '<strong>Söker...</strong>',
        'info.intygstyp.replaced': '${oldIntygstyp} är ersatt av ${newIntygstyp}. För att gå vidare, välj det nya intyget i listan istället.',

        //error messages
        'error.unsignedintyg.couldnotbeloaded': '<strong>Kunde inte hämta ej signerade intyg.</strong>',
        'error.unansweredintyg.couldnotbeloaded': '<strong>Kunde inte hämta listan med ej hanterade frågor och svar.</strong>',
        'error.readytosignintyg.couldnotbeloaded': '<strong>Kunde inte hämta intyg klara för signering.</strong>',
        'error.failedtocreateintyg': 'Kunde inte skapa intyget. Försök igen senare.',
        'error.failedtomakuleraintyg': 'Kunde inte makulera intyget. Försök igen senare.',
        'error.failedtocopyintyg': 'Kunde inte kopiera intyget. Försök igen senare.',
        'error.failedtocopyintyg.personidnotfound': 'Kunde inte kopiera intyget. Det nya person-id:t kunde inte hittas.',
        'error.failedtocopyintyg.replaced': 'Intyget kunde inte kopieras eftersom det har blivit ersatt av ett senare intyg. Kopiera ett annat intyg istället.',
        'error.failedtocopyintyg.complemented': 'Intyget kunde inte kopieras eftersom det har blivit kompletterat av ett senare intyg. Kopiera ett annat intyg istället.',
        'error.failedtofornyaintyg': 'Kunde inte förnya intyget. Försök igen senare.',
        'error.failedtofornyaintyg.personidnotfound': 'Kunde inte förnya intyget. Det nya person-id:t kunde inte hittas.',
        'error.failedtofornyaintyg.replaced': 'Intyget kunde inte förnyas eftersom det har blivit ersatt av ett senare intyg.',
        'error.failedtofornyaintyg.complemented': 'Intyget kunde inte förnyas eftersom det har blivit kompletterat av ett senare intyg.',
        'error.failedtoersattintyg': 'Kunde inte ersätta intyget. Försök igen senare.',
        'error.failedtosendintyg': 'Kunde inte skicka intyget. Försök igen senare.',
        'error.pu.namenotfound': 'Personnumret du har angivit finns inte i folkbokföringsregistret. Kontrollera om du har skrivit rätt.<br>Observera att det inte går att ange reservnummer. Webcert hanterar enbart person- och samordningsnummer.',
        'error.pu.samordningsnummernotfound': 'Samordningsnumret du har angivit finns inte i folkbokföringsregistret. Kontrollera om du har skrivit rätt.<br>Observera att det inte går att ange reservnummer. Webcert hanterar enbart person- och samordningsnummer.',
        'error.pu.nopersonnummer': 'Det personnummer du har angett kunde tyvärr inte hämtas från folkbokföringsregistret. Försök igen senare.',
        'error.pu.noname': 'Namn för det nummer du har angett kunde tyvärr inte hämtas från folkbokföringsregistret. Försök igen senare.',
        'error.pu.unknownerror': 'Kunde inte kontakta PU-tjänsten.',

        //Table headings
        //Tidigare intyg tabell
        'th.label.intyg-type': 'Typ av intyg',
        'th.help.intyg-type': 'Typ av intyg.',
        'th.label.status': 'Status',
        'th.help.status': ' Visar utkastets/intygets status:<ul><li>Utkast, uppgifter saknas = utkastet är sparat, men obligatoriska uppgifter saknas</li><li>Utkast, kan signeras = utkastet är komplett, sparat och kan signeras</li><li>Signerat = intyget är signerat</li><li>Skickat = intyget är skickat till mottagaren</li><li>Makulerat = intyget är makulerat</li></ul>',
        'th.label.saved-date': 'Senast sparat',
        'th.help.saved-date': 'Datum och klockslag då utkastet senast sparades.',
        'th.label.saved-signed-by': 'Sparat/signerat av',
        'th.help.saved-signed-by': 'Person som senast sparade utkastet/intyget alternativt person som signerade intyget.',

        //Ej signerade utkast
        'th.label.draft-forwarded': 'Vidarebefordrad',
        'th.help.draft-forwarded': 'Här markerar du om utkastet är vidarebefordrat till den som ska signera det.',
        'th.label.draft-type': 'Typ av intyg',
        'th.help.draft-type': 'Typ av intyg.',
        'th.label.draft-status': 'Status',
        'th.help.draft-status': 'Visar utkastets status:<ul><li>Utkast, uppgifter saknas = utkastet är sparat, men obligatoriska uppgifter saknas</li><li>Utkast, kan signeras = utkastet är komplett, sparat och kan signeras</li></ul>',
        'th.label.draft-saved-date': 'Senast sparat',
        'th.help.draft-saved-date': 'Datum och klockslag då utkastet senast sparades.',
        'th.label.draft-patient': 'Patient',
        'th.help.draft-patient': 'Personnummer för patient som utkastet gäller.',
        'th.label.draft-saved-by': 'Sparat av',
        'th.help.draft-saved-by': 'Person som senast sparade utkastet.',

        //Fraga/Svar
        'th.label.qa-forwarded': 'Vidarebefordrad',
        'th.help.qa-forwarded': 'Markera om fråga-svar är vidarebefordrat till den som ska hantera det.',
        'th.label.qa-action': 'Att åtgärda',
        'th.help.qa-action': 'Åtgärd som krävs för att fråga-svar ska anses som hanterad och avslutad.',
        'th.label.qa-sender': 'Avsändare',
        'th.help.qa-sender': 'Vem som initierade frågan.',
        'th.label.qa-patient': 'Patient',
        'th.help.qa-patient': 'Berörd patients personnummer.',
        'th.label.qa-signed-by': 'Signerat av',
        'th.help.qa-signed-by': 'Läkare som har signerat intyget.',
        'th.label.qa-sent-recv-date': 'Skickat/mottaget',
        'th.help.qa-sent-recv-date': 'Datum och klockslag för senaste händelse. Exempelvis när fråga skickades eller när ett svar inkom.'

    },
    'en': {
        'webcert.header': 'Webcert Application (en)'
    }
});
