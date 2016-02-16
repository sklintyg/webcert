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

/**
 * Created by bennysce on 09/12/15.
 */
/*globals element,by, protractor, Promise*/
'use strict';

var BaseUtkast = require('./base.utkast.page.js');
var pageHelpers = require('./../pageHelper.util.js');

var TsDiabetesUtkast = BaseUtkast._extend({
  init: function init() {
    init._super.call(this);
    this.at = element(by.id('edit-ts-diabetes'));

    this.korkortsTyperChecks = element(by.id('intygetAvserForm')).all(by.css('label.checkbox'));

    this.identitetForm = element(by.id('identitetForm'));

    this.allmant = {
      form: element(by.id('allmantForm')),
      insulinbehandlingsperiod: element(by.id('insulinBehandlingsperiod'))
    };
    this.allmant.diabetesyear = this.allmant.form.element(by.id('diabetesyear'));

    this.hypoglykemier = {
      a: {
        yes: element(by.id('hypoay')),
        no: element(by.id('hypoan'))
      },
      b: {
        yes: element(by.id('hypoby')),
        no: element(by.id('hypobn'))
      },
      c: {
        yes: element(by.id('hypocy')),
        no: element(by.id('hypocn'))
      },
      d: {
        yes: element(by.id('hypody')),
        no: element(by.id('hypodn'))
      },
      e: {
        yes: element(by.id('hypoey')),
        no: element(by.id('hypoen'))
      },
      f: {
        yes: element(by.id('hypofy')),
        no: element(by.id('hypofn'))
      },
      g: {
        yes: element(by.id('hypogy')),
        no: element(by.id('hypogn'))
      }
    };

    this.korkortsTyperChecks = element(by.id('intygetAvserForm')).all(by.css('label.checkbox'));

    this.syn = {
      a: {
        yes: element(by.id('synay')),
        no: element(by.id('synan'))
      }
    };

    this.bedomning = {
      form: element(by.id('bedomningForm')),
      yes: element(by.id('bedomningy')),
      no: element(by.id('bedomningn'))
    };
  },
  get: function get(intygId) {
    get._super.call(this, 'ts-diabetes', intygId);
  },
  fillInKorkortstyper: function (typer) {

    // this.korkortsTyperChecks.filter(function(elem) {
    //     return elem.getText().then(function(text) {
    //         return (typer.indexOf(text) >= 0);
    //     });
    // }).then(function(filteredElements) {
    //     for (var i = 0; i < filteredElements.length; i++) {
    //         filteredElements[i].sendKeys(protractor.Key.SPACE);
    //     }
    // });

    return pageHelpers.clickAll(this.korkortsTyperChecks, typer);
  },
  fillInIdentitetStyrktGenom: function (idtyp) {
    return this.identitetForm.element(by.cssContainingText('label.radio', idtyp)).sendKeys(protractor.Key.SPACE);
  },

  /**
   *
   * @param allmant
   *      {
   *          year,
   *          typ,
   *          behandling: {
   *              typer,
   *              insulinYear
   *          }
   *      }
   */
  fillInAllmant: function (allmant) {

    var promisesArr = [];

    // Ange år då diagnos ställts
    this.allmant.diabetesyear.sendKeys(allmant.year);

    var form = this.allmant.form;

    // Ange diabetestyp
    promisesArr.push(form.element(by.cssContainingText('label.radio', allmant.typ)).sendKeys(protractor.Key.SPACE));

    // Ange behandlingstyp
    var typer = allmant.behandling.typer;
    typer.forEach(function (typ) {
      promisesArr.push(form.element(by.cssContainingText('label.checkbox', typ)).sendKeys(protractor.Key.SPACE));
    });

    if (allmant.behandling.insulinYear) {
      promisesArr.push(this.allmant.insulinbehandlingsperiod.sendKeys(allmant.behandling.insulinYear));
    }

    return Promise.all(promisesArr);

  },
  fillInHypoglykemier: function (hypoglykemierObj) {

    //console.log('Anger hypoglykemier:' + hypoglykemierObj.toString());
    var promisesArr = [];
    // a)
    if (hypoglykemierObj.a === 'Ja') {
      promisesArr.push(this.hypoglykemier.a.yes.sendKeys(protractor.Key.SPACE));
    } else {
      promisesArr.push(this.hypoglykemier.a.no.sendKeys(protractor.Key.SPACE));
    }

    // b)
    if (hypoglykemierObj.b === 'Ja') {
      promisesArr.push(this.hypoglykemier.b.yes.sendKeys(protractor.Key.SPACE));
    } else {
      promisesArr.push(this.hypoglykemier.b.no.sendKeys(protractor.Key.SPACE));
    }

    // f)
    if (hypoglykemierObj.f) {
      if (hypoglykemierObj.f === 'Ja') {
        promisesArr.push(this.hypoglykemier.f.yes.sendKeys(protractor.Key.SPACE));
      } else {
        promisesArr.push(this.hypoglykemier.f.no.sendKeys(protractor.Key.SPACE));
      }
    }

    // g)
    if (hypoglykemierObj.g) {
      if (hypoglykemierObj.g === 'Ja') {
        promisesArr.push(this.hypoglykemier.g.yes.sendKeys(protractor.Key.SPACE));
      } else {
        promisesArr.push(this.hypoglykemier.g.no.sendKeys(protractor.Key.SPACE));
      }
    }

    return Promise.all(promisesArr);
  },
  fillInSynintyg: function (synintygObj) {
    // a)
    if (synintygObj.a === 'Ja') {
      return this.syn.a.yes.sendKeys(protractor.Key.SPACE);
    } else {
      return this.syn.a.no.sendKeys(protractor.Key.SPACE);
    }
  },
  fillInBedomning: function (bedomningObj) {
    var promisesArr = [];
    promisesArr.push(this.bedomning.form.element(by.id(bedomningObj.stallningstagande)).sendKeys(protractor.Key.SPACE));
    promisesArr.push(pageHelpers.clickAll(this.bedomning.form.all(by.css('label.checkbox')), bedomningObj.behorigheter));

    if (bedomningObj.lamplighet) {
      if (bedomningObj.lamplighet === 'Ja') {
        promisesArr.push(this.bedomning.yes.sendKeys(protractor.Key.SPACE));
      } else {
        promisesArr.push(this.bedomning.no.sendKeys(protractor.Key.SPACE));
      }
    }
    return Promise.all(promisesArr);
  }
});

module.exports = new TsDiabetesUtkast();
