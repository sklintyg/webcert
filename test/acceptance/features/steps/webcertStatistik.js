/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

/* globals intyg, browser, logger, wcTestTools, Promise */
'use strict';
/*jshint newcap:false */
//TODO Uppgradera Jshint p.g.a. newcap kommer bli depricated. (klarade inte att ignorera i grunt-task)

/*
 *	Stödlib och ramverk
 *
 */

const {
    Given, // jshint ignore:line
    When, // jshint ignore:line
    Then // jshint ignore:line
} = require('cucumber');

var helpers = require('./helpers');
var db = require('./dbActions');
var loginHelperStatistik = require('./inloggning/login.helpers.statistik.js');
var logInAsUserRoleStatistik = loginHelperStatistik.logInAsUserRoleStatistik;
var lisjpUtkastPage = wcTestTools.pages.intyg.lisjp.utkast;
var diagnosKategorier = wcTestTools.testdata.diagnosKategorier;
var shuffle = wcTestTools.helpers.testdata.shuffle;
var statistikAPI = require('./statistiktjansten/api/statistikAPI.js');

var monthNames = ['jan', 'feb', 'mar', 'apr', 'maj', 'jun', 'jul', 'aug', 'sep', 'okt', 'nov', 'dec'];

/*
 *	Stödfunktioner
 *
 */

global.statistik = {
    diagnosKod: false,
    nrOfSjukfall: {
        totalt: 0,
        kvinna: 0,
        man: 0
    },
    intygsId: '',
    tempArr: []
};

var vardgivare = 'TSTNMT2321000156-107M';
var dateLabel = monthNames[new Date().getMonth()] + ' ' + (1900 + new Date().getYear());

var subjects = {
    'Meddelande per ämne': {
        url: process.env.STATISTIKTJANST_URL + '/#/verksamhet/meddelandenPerAmne?vgid=' + vardgivare,
        labels: dateLabel,
        column: {
            'AVSTMN': {
                totalt: 4,
                kvinnor: 5,
                man: 6
            },
            'OVRIGT': {
                totalt: 10,
                kvinnor: 11,
                man: 12
            }
        }
    },
    'Meddelanden per ämne och landsting': {
        url: process.env.STATISTIKTJANST_URL + '/#/verksamhet/meddelandenPerAmneOchLandsting?vgid=' + vardgivare,
        labels: 'todo'
    }
};

function slumpaDiagnosKod(diagnosKod) {

    if (diagnosKod === 'slumpad') {
        diagnosKod = shuffle(diagnosKategorier)[0].diagnosKod;
        global.statistik.diagnosKod = diagnosKod;
        logger.info('==== Slumpat fram diagnosKod ' + diagnosKod + '====');
    } else if (diagnosKod === 'samma som ovan') {
        diagnosKod = global.statistik.diagnosKod;
        logger.info('==== Använder diagnosKod ' + diagnosKod + '====');
    }
    return diagnosKod;

}

function forvantatAntal(antal, modifier, diff) {
    if (modifier === 'extra') {
        return parseInt(antal, 10) + parseInt(diff, 10);
    } else if (modifier === 'mindre') {
        return parseInt(antal, 10) - parseInt(diff, 10);
    } else {
        throw ('test steget förväntar sig extra eller mindre variabel.');
    }
}

function mergeTables() {
    //Function for merging two UI tables into one object.
    var tempArr = [];
    var table = {};
    return element.all(by.css('.table-condensed')).all(by.tagName('tr')).then(function(arr) {
        arr.forEach(function(entry, index) {
            //var secondIndex = index - arr.length / 2;

            entry.getText().then(function(data) {
                if (arr.length / 2 > index) {
                    tempArr.push(data);
                } else {
                    table[tempArr.shift()] = data;
                }
            });
        });
        return table;
    });
}

function getColumnDataFromTable(table, subject, column) {
    var data = table[subjects[subject].labels].split(' ');
    console.log('data');
    console.log(data);

    var values = {};
    values.totalt = parseInt(data[subjects[subject].column[column].totalt], 10);
    values.man = parseInt(data[subjects[subject].column[column].man], 10);
    values.kvinnor = parseInt(data[subjects[subject].column[column].kvinnor], 10);

    console.log('values');
    console.log(values);

    return values;
}



/*
 *	Test steg
 *
 */


Given(/^ska jag se intyget i databasen$/, function(callback) {
    db.statistics.lookUp(1, intyg.id, callback);
});


Given(/^jag går in på Statistiktjänsten$/, function() {
    global.statistik.intygsId = intyg.id;
    var url = process.env.STATISTIKTJANST_URL + '/#/fakelogin';
    return helpers.getUrl(url);
});

Given(/^jag är inloggad som läkare i Statistiktjänsten$/, function() {
    // Setting Statistiktjänsten to new bas url
    browser.baseUrl = process.env.STATISTIKTJANST_URL;
    // VG_TestAutomation => TSTNMT2321000156-107M => TSTNMT2321000156-107Q
    var userObj = {
        fornamn: 'Johan',
        efternamn: 'Johansson',
        hsaId: 'TSTNMT2321000156-107V',
        vardgivarIdSomProcessLedare: [
            'TSTNMT2321000156-107M'
        ],
        vardgivarniva: 'true'
    };

    return logInAsUserRoleStatistik(userObj, 'Läkare', true);
});

Given(/^jag ändrar diagnoskoden till "([^"]*)"$/, function(diagnosKod) {
    diagnosKod = slumpaDiagnosKod(diagnosKod);

    var diagnos = {
        kod: diagnosKod
    };

    return lisjpUtkastPage.diagnoseCode.clear().then(function() {
        return lisjpUtkastPage.angeDiagnos(diagnos);
    });
});

Given(/^jag går till statistiksidan för "([^"]*)"$/, function(sida) {
    var url = '';
    switch (sida) {
        case 'Meddelande per ämne':
            url = process.env.STATISTIKTJANST_URL + '/#/verksamhet/meddelandenPerAmne?vgid=' + vardgivare;
            break;
        case 'Meddelanden per ämne och landsting':
            url = process.env.STATISTIKTJANST_URL + '/#/verksamhet/meddelandenPerAmneOchLandsting?vgid=' + vardgivare;
            break;
    }


    return helpers.getUrl(url);
});

Given(/^jag kollar värdena i tabellen$/, function() {
    //spara undan gamla tabellen
    global.statistik.oldTable = global.statistik.table;

    return mergeTables().then(function(table) {
        global.statistik.table = table;
        console.log('global.statistik.table');
        console.log(global.statistik.table);
        console.log('global.statistik.oldTable');
        console.log(global.statistik.oldTable);
        return;
    });
});


Given(/^jag går till statistiksidan för diagnoskod "([^"]*)"$/, function(diagnosKod) {
    diagnosKod = slumpaDiagnosKod(diagnosKod);


    // Alla kategorier (för många kategorier)
    //var url = process.env.STATISTIKTJANST_URL + '/#/verksamhet/jamforDiagnoser/f07485ae393db737bacab5f416a43a2a?vgid=TSTNMT2321000156-107M';



    var url = {
        A: process.env.STATISTIKTJANST_URL + '/#/verksamhet/jamforDiagnoser/e82017388289b39cd4e8b51a94378d77?vgid=TSTNMT2321000156-107M',
        B: process.env.STATISTIKTJANST_URL + '/#/verksamhet/jamforDiagnoser/3221d5e01e8a13f2ff386f6fb40387c7?vgid=TSTNMT2321000156-107M',
        C: process.env.STATISTIKTJANST_URL + '/#/verksamhet/jamforDiagnoser/5b886bedc781a2c0d4a3afbc6c0ef705?vgid=TSTNMT2321000156-107M',
        D: process.env.STATISTIKTJANST_URL + '/#/verksamhet/jamforDiagnoser/21e26707d18d1cf721f949a2e091c30f?vgid=TSTNMT2321000156-107M',
        E: process.env.STATISTIKTJANST_URL + '/#/verksamhet/jamforDiagnoser/941e5ccdc0cc69b17d583416279e264e?vgid=TSTNMT2321000156-107M',
        F: process.env.STATISTIKTJANST_URL + '/#/verksamhet/jamforDiagnoser/11f649d230516c4caa7242d042ea6deb?vgid=TSTNMT2321000156-107M'
    };

    logger.silly('diagnosKod.charAt(0) - ' + diagnosKod.charAt(0));
    logger.silly('url[diagnosKod.charAt(0)] - ' + url[diagnosKod.charAt(0)]);

    return helpers.getUrl(url[diagnosKod.charAt(0)]).then(function() {
        logger.info('Går till url för diagnoskod ' + diagnosKod + ': ' + url[diagnosKod.charAt(0)]);
    });
});

Given(/^jag kollar totala "([^"]*)" diagnoser som finns$/, function(diagnosKod) {
    diagnosKod = slumpaDiagnosKod(diagnosKod).substring(0, 3);


    return element.all(by.css('.table-condensed')).all(by.tagName('tr')).then(function(arr) {
        if (arr.length === 0) {
            throw ('Inga rader i tabellen hittades!');
        }

        //return
        var statistik = [];
        logger.silly('Statistik Tabell Längd (arr.length): ' + arr.length);

        arr.forEach(function(entry, index) {

            entry.getText().then(function(txt) {

                // Devide array in half and merge the data into statistik object based on index.
                var secondIndex = index - arr.length / 2;

                if (arr.length / 2 > index) {
                    statistik.push({
                        diagnosKod: txt
                    });
                } else {
                    statistik[secondIndex].totalt = parseInt(txt.split(' ')[0], 10);
                    statistik[secondIndex].kvinna = parseInt(txt.split(' ')[1], 10);
                    statistik[secondIndex].man = parseInt(txt.split(' ')[2], 10);

                    logger.silly(statistik[secondIndex]);

                    if (statistik[secondIndex].diagnosKod.split(' ')[0] === diagnosKod) {
                        global.statistik.nrOfSjukfall = statistik[secondIndex];
                        logger.info('global.statistik.nrOfSjukfall.totalt: ' + global.statistik.nrOfSjukfall.totalt);
                        return;
                    }
                }
            });
            if (index === arr.length) {
                throw ('Kunde inte hitta aktuellt antal intyg för ' + diagnosKod);
            }
        });
    });
});



Given(/^jag hämtar "([^"]*)" från Statistik APIet \- getMeddelandenPerAmne$/, function(beskrivning) {
    return statistikAPI.getMeddelandenPerAmne('TSTNMT2321000156-107M').then(function(statistik) {
        global.statistik.tempArr.push(statistik);
    });
});
Given(/^jag hämtar "([^"]*)" från Statistik APIet \- getMeddelandenPerAmneLandsting$/, function(beskrivning) {
    return statistikAPI.getMeddelandenPerAmneLandsting().then(function(statistik) {
        global.statistik.tempArr.push(statistik);
    });
});
Given(/^jag hämtar "([^"]*)" från Statistik APIet \- getMeddelandenPerAmneOchEnhetLandsting$/, function(beskrivning) {
    return statistikAPI.getMeddelandenPerAmneOchEnhetLandsting().then(function(statistik) {
        global.statistik.tempArr.push(statistik);
    });
});
Given(/^jag hämtar "([^"]*)" från Statistik APIet \- getMeddelandenPerAmneOchEnhetTvarsnittVerksamhet$/, function(beskrivning) {
    return statistikAPI.getMeddelandenPerAmneOchEnhetTvarsnittVerksamhet().then(function(statistik) {
        global.statistik.tempArr.push(statistik);
    });
});
Given(/^jag hämtar "([^"]*)" från Statistik APIet \- getMeddelandenPerAmneOchEnhetVerksamhet$/, function(beskrivning) {
    return statistikAPI.getMeddelandenPerAmneOchEnhetVerksamhet().then(function(statistik) {
        global.statistik.tempArr.push(statistik);
    });
});

Then(/^ska "([^"]*)" i "([^"]*)" vara "([^"]*)" (extra|mindre)$/, function(column, typ, antal, modifier) {


    var nyaVarden = getColumnDataFromTable(global.statistik.table, typ, column);

    var gammlaVarden = getColumnDataFromTable(global.statistik.oldTable, typ, column);
    var forvantatVarde = {};
    forvantatVarde.totalt = forvantatAntal(gammlaVarden.totalt, modifier, antal);
    forvantatVarde.man = forvantatAntal(gammlaVarden.man, modifier, antal);
    forvantatVarde.kvinna = forvantatAntal(gammlaVarden.kvinna, modifier, antal);

    return expect(nyaVarden.totalt).to.equal(forvantatVarde.totalt);
});


Given(/^ska totala "([^"]*)" diagnoser som finns (?:vara|är) "([^"]*)" (extra|mindre)$/, function(diagnosKod, nrOfIntyg, modifier) {
    diagnosKod = slumpaDiagnosKod(diagnosKod).substring(0, 3);


    logger.silly(global.person);

    var gender = global.person.kon;

    logger.info('====== Kollar statistik på kön: ' + gender + '==========');

    if (!global.statistik.nrOfSjukfall) {
        throw ('test steget förväntar sig att tidigare steg kollat aktuell statistik.');
    }

    var nuvarandeStatistik = global.statistik.nrOfSjukfall;

    if (!diagnosKod || !nrOfIntyg) {
        throw ('diagnosKod och nrOfIntyg får inte vara tomma.');
    } else {
        logger.silly('nuvarandeStatistik.totalt: ' + nuvarandeStatistik.totalt);
        nuvarandeStatistik.totalt = forvantatAntal(nuvarandeStatistik.totalt, modifier, nrOfIntyg);
        logger.silly('nuvarandeStatistik.totalt: ' + nuvarandeStatistik.totalt);

        if (gender === 'man') {
            nuvarandeStatistik.man = forvantatAntal(nuvarandeStatistik.man, modifier, nrOfIntyg);
        } else if (gender === 'kvinna') {
            nuvarandeStatistik.kvinna = forvantatAntal(nuvarandeStatistik.kvinna, modifier, nrOfIntyg);
        } else {
            throw ('Kunde inte fastställa kön på person: ' + global.person);
        }
    }
    global.statistik.nrOfSjukfall = nuvarandeStatistik;
    logger.silly('global.statistik.nrOfSjukfall.totalt: ' + global.statistik.nrOfSjukfall.totalt);

    return element.all(by.css('.table-condensed')).all(by.tagName('tr')).then(function(arr) {
        //return
        var statistik = [];
        logger.info('Antal rader i tabellen: ' + arr.length);
        if (arr.length < 1) {
            throw ('Inga rader hittades i tabellen');
        }

        arr.forEach(function(entry, index) {

            entry.getText().then(function(txt) {

                // Devide array in half and merge the data into statistik object based on index.
                var secondIndex = index - arr.length / 2;

                if (arr.length / 2 > index) {
                    statistik.push({
                        diagnosKod: txt
                    });
                } else {
                    statistik[secondIndex].totalt = parseInt(txt.split(' ')[0], 10);
                    statistik[secondIndex].kvinna = parseInt(txt.split(' ')[1], 10);
                    statistik[secondIndex].man = parseInt(txt.split(' ')[2], 10);

                    logger.silly('Data på rad ' + secondIndex);
                    logger.silly(statistik[secondIndex]);

                    if (statistik[secondIndex].diagnosKod.split(' ')[0] === diagnosKod) {
                        logger.info('global.statistik.nrOfSjukfall.totalt: ' + global.statistik.nrOfSjukfall.totalt);
                        logger.silly('statistik[secondIndex].totalt: ' + statistik[secondIndex].totalt);

                        logger.info('global.statistik.nrOfSjukfall.kvinna: ' + global.statistik.nrOfSjukfall.kvinna);
                        logger.silly('statistik[secondIndex].kvinna: ' + statistik[secondIndex].kvinna);

                        logger.info('global.statistik.nrOfSjukfall.man: ' + global.statistik.nrOfSjukfall.man);
                        logger.silly('statistik[secondIndex].man: ' + statistik[secondIndex].man);

                        var promiseArr = [];
                        promiseArr.push(expect(global.statistik.nrOfSjukfall.totalt).to.equal(statistik[secondIndex].totalt));
                        if (modifier < 2 && modifier > -2) {
                            //Kontrollera bara man/kvinna statistik om modifer är -1, 0 eller 1 eftersom om det är fler så vet vi inte kön på samtliga intyg.
                            promiseArr.push(expect(global.statistik.nrOfSjukfall.kvinna).to.equal(statistik[secondIndex].kvinna));
                            promiseArr.push(expect(global.statistik.nrOfSjukfall.man).to.equal(statistik[secondIndex].man));
                        }

                        return Promise.all(promiseArr);

                    }
                }
            });
            if (index === arr.length) {
                throw ('Kunde inte hitta aktuellt antal intyg för ' + diagnosKod);
            }
        });
    });
});

Given(/^jag anropar statitisk-APIet processIntyg$/, function() {
    return statistikAPI.processIntyg().then(function() {
        return helpers.pageReloadDelay();
    });
});
