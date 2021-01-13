/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

'use strict';

//Krav på teckenlängd - Se "Utformning Dödsbevis & dödsorsaksintyg"

var testdataHelper = require('common-testtools').testdataHelper;
var shuffle = testdataHelper.shuffle;

var today = new Date();
var deathDate = new Date();
deathDate.setDate(today.getDate() - Math.floor(Math.random() * 365));

function getRelativeDeathDate(modifier) {
  // Modifier : days
  var datum = new Date(deathDate);
  datum.setDate(deathDate.getDate() + modifier);
  return datum;
}

function getDodsdatum(datumSakert) {
  if (datumSakert === true) {
    return {
      sakert: {
        datum: testdataHelper.dateFormat(deathDate)
      }
    };
  } else {
    var monthArr = ['00', '01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12'];
    var year = deathDate.getYear() + 1900;
    year = shuffle([String(year), '0000'])[0];
    return {
      inteSakert: {
        year: year,
        month: (year === '0000') ? '00' : shuffle(monthArr.slice(0, today.getMonth() + 1))[0],
        antraffadDod: testdataHelper.dateFormat(today)
      }
    };
  }
}

function getDodsOrsak() {
  var n = Math.floor(Math.random() * 4);

  var obj = {
    a: getDodsOrsakObj(1)
  };
  if (n >= 1) {
    obj.b = getDodsOrsakObj(2);
  }
  if (n >= 2) {
    obj.c = getDodsOrsakObj(3);
  }
  if (n >= 3) {
    obj.d = getDodsOrsakObj(4);
  }

  obj.andraSjukdomarSkador = getDodsOrsakObj(5);

  return obj;
}

function getDodsOrsakObj(n) {

  var obj = {
    beskrivning: testdataHelper.randomTextString(5, 10),
    datum: testdataHelper.dateFormat(getRelativeDeathDate(-n)),
    tillstandSpec: shuffle(['Akut', 'Kronisk', 'Uppgift saknas'])[0]
  };
  return obj;
}

function getSkadaForgiftning() {
  var ja = {
    ja: {
      orsakAvsikt: shuffle(['Olycksfall', 'Självmord', 'Avsiktligt vållad av annan', 'Oklart om avsikt förelegat'])[0],
      datum: testdataHelper.dateFormat(getRelativeDeathDate(-1)),
      beskrivning: testdataHelper.randomTextString(5, 400)
    }
  };

  return shuffle([ja, false])[0];
}

function getOperation() {
  var ja = {
    ja: {
      datum: testdataHelper.dateFormat(getRelativeDeathDate(-1)),
      beskrivning: testdataHelper.randomTextString(5, 100)
    }
  };
  return shuffle([ja, 'Nej', 'Uppgift om operation saknas'])[0];
}

function getDodsOrsakUppgifter() {
  var obj = {
    foreDoden: shuffle(["Undersökning före döden", false, false, false, false, false, false, false, false, false])[0],
    efterDoden: shuffle(["Yttre undersökning efter döden", false, false, false, false, false, false, false, false, false])[0],
    kliniskObduktion: shuffle(["Klinisk obduktion", false, false, false, false, false, false, false, false, false])[0],
    rattsmedicinskObduktion: shuffle(["Rättsmedicinsk obduktion", false, false, false, false, false, false, false, false, false])[0],
    rattsmedicinskBesiktning: shuffle(["Rättsmedicinsk likbesiktning", false, false, false, false, false, false, false, false, false])[0]
  };

  var objHasSomeValue = false;

  for (var key in obj) {
    if (obj.hasOwnProperty(key) && obj[key] !== false) {
      objHasSomeValue = true;
    }
  }

  return (objHasSomeValue === true) ? obj : getDodsOrsakUppgifter();
}

module.exports = {
  get: function(intygsID) {
    intygsID = testdataHelper.generateTestGuid();

    var datumSakert = true

    var obj = {
      id: intygsID,
      typ: "Dödsorsaksintyg",
      deathDate: deathDate, //datumvariabel som används för att ta fram test-data till andra variablar.
      identitetStyrktGenom: "körkort",
      dodsdatum: getDodsdatum(datumSakert),
      dodsPlats: {
        kommun: "Karlstad",
        boende: "Sjukhus"
      },
      dodsorsak: {
        a: getDodsOrsakObj(1)
      },
      operation: 'Nej',
      skadaForgiftning: false,
      dodsorsaksuppgifter: {
        foreDoden: "Undersökning före döden",
        efterDoden: false,
        kliniskObduktion: false,
        rattsmedicinskObduktion: false,
        rattsmedicinskBesiktning: false
      }
    };

    return obj;
  },
  getRandom: function(intygsID, customFields) {
    if (customFields) {
      //deathDate används av många andra funktioner så vi ändrar det först
      deathDate = customFields.deathDate ? customFields.deathDate : deathDate;
    }
    if (!intygsID) {
      intygsID = testdataHelper.generateTestGuid();
    }

    var datumSakert = (customFields && customFields.dodsdatum && customFields.dodsdatum.sakert) ? true : testdataHelper.randomTrueFalse();

    var obj = {
      id: intygsID,
      typ: "Dödsorsaksintyg",
      deathDate: deathDate, //datumvariabel som används för att ta fram test-data till andra variablar.
      identitetStyrktGenom: shuffle(["körkort", "pass", "fingeravtryck", "tandavgjutning", testdataHelper.randomTextString(5, 27)])[0],
      land: shuffle(["Norge", "Danmark", "Finland", "Island", testdataHelper.randomTextString(5, 24)])[0],
      dodsdatum: getDodsdatum(datumSakert),
      dodsPlats: {
        kommun: shuffle(["Karlstad", "Forshaga", "Hagfors", "Munkfors", "Torsby", testdataHelper.randomTextString(5, 28)])[0],
        boende: shuffle(["Sjukhus", "Ordinärt boende", "Särskilt boende", "Annan/okänd"])[0]
      },
      dodsorsak: getDodsOrsak(),
      operation: getOperation(),
      skadaForgiftning: getSkadaForgiftning(),
      dodsorsaksuppgifter: getDodsOrsakUppgifter()
    };
    if (datumSakert === false) {
      obj.barn = (customFields && customFields.barn) ? customFields.barn : testdataHelper.randomTrueFalse();
    }

    function useCustom(field) {
      obj[field] = customFields[field] ? customFields[field] : obj[field];
    }

    if (customFields) {
      //Skriv över 
      useCustom('identitetStyrktGenom');
      useCustom('land');
      useCustom('dodsdatum');
      useCustom('dodsPlats');
    }
    return obj;

  }
};
