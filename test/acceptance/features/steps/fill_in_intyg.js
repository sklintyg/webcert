/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

/*global
testdata, intyg, browser, pages*/
'use strict';

var tsdUtkastPage = pages.intyg['ts-diabetesUtkast'];
var tsBasUtkastPage = pages.intyg['ts-basUtkast'];
var fkUtkastPage = pages.intyg.fk7263Utkast;

module.exports = function() {

    this.Given(/^jag fyller i alla nödvändiga fält för intyget$/, function(callback) {
        if (intyg.typ === 'Transportstyrelsens läkarintyg') {
            global.intyg = testdata.getRandomTsBasIntyg();

            // browser.ignoreSynchronization = true;
            // Intyget avser
            tsBasUtkastPage.fillInKorkortstyper(global.intyg.korkortstyper, 'intygetAvserForm');
            // Identiteten är styrkt genom
            tsBasUtkastPage.fillInIdentitetStyrktGenom(intyg.identitetStyrktGenom);

            // Synfunktioner

            browser.ignoreSynchronization = true;
            tsBasUtkastPage.fillInSynfunktioner(global.intyg);
            tsBasUtkastPage.fillInHorselOchBalanssinne(global.intyg);
            tsBasUtkastPage.fillInRorelseorganensFunktioner(global.intyg);
            tsBasUtkastPage.fillInHjartOchKarlsjukdomar(global.intyg);
            tsBasUtkastPage.fillInDiabetes(global.intyg);
            tsBasUtkastPage.fillInHorselOchBalanssinne(global.intyg);
            tsBasUtkastPage.fillInNeurologiskaSjukdomar(global.intyg);
            tsBasUtkastPage.fillInEpilepsi(global.intyg);
            tsBasUtkastPage.fillInNjursjukdomar(global.intyg);
            tsBasUtkastPage.fillInDemens(global.intyg);
            tsBasUtkastPage.fillInSomnOchVakenhet(global.intyg);
            tsBasUtkastPage.fillInAlkoholNarkotikaLakemedel(global.intyg);
            tsBasUtkastPage.fillInPsykiska(global.intyg);
            tsBasUtkastPage.fillInAdhd(global.intyg);
            tsBasUtkastPage.fillInSjukhusvard(global.intyg);

            tsBasUtkastPage.fillInOvrigMedicinering(global.intyg);

            browser.ignoreSynchronization = false;
            tsBasUtkastPage.fillInBedomning(intyg.bedomning);
            callback();
        } else if (intyg.typ === 'Transportstyrelsens läkarintyg, diabetes') {
            global.intyg = testdata.getRandomTsDiabetesIntyg();

            tsdUtkastPage.fillInKorkortstyper(intyg.korkortstyper);
            tsdUtkastPage.fillInIdentitetStyrktGenom(intyg.identitetStyrktGenom);
            tsdUtkastPage.fillInAllmant(intyg.allmant);
            tsdUtkastPage.fillInHypoglykemier(intyg.hypoglykemier);
            tsdUtkastPage.fillInSynintyg(intyg.synintyg);
            tsdUtkastPage.fillInBedomning(intyg.bedomning);

            callback();
        } else if (intyg.typ === 'Läkarintyg FK 7263') {

            global.intyg = testdata.fk.sjukintyg.getRandom();
            fkUtkastPage.angeSmittskydd(intyg.smittskydd);
            fkUtkastPage.angeIntygetBaserasPa(intyg.baserasPa);
            fkUtkastPage.angeDiagnoser(intyg.diagnos);
            fkUtkastPage.angeAktuelltSjukdomsForlopp(intyg.aktuelltSjukdomsforlopp);
            fkUtkastPage.angeArbetsformaga(intyg.arbetsformaga);
            fkUtkastPage.angeArbetsformagaFMB(intyg.arbetsformagaFMB);
            fkUtkastPage.angePrognos(intyg.prognos);
            callback();
        }
    });

  


};
