/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

/*globals element,by,protractor, Promise,browser*/
'use strict';

var BaseTsUtkast = require('../ts.base.utkast.page.js');
var pageHelpers = require('../../../pageHelper.util.js');

var Tstrk1062Utkast = BaseTsUtkast._extend({
    init: function init() {

        init._super.call(this);
        this.intygType = 'tstrk1062';
        this.intygTypeVersion = '1.0';
        this.at = element(by.id('edit-tstrk1062'));

        // this.intygerAvser = {
        //   amId: 'intygetAvser.behorigheter-IAV11',
        //   a1Id: 'intygetAvser.behorigheter-IAV12'
        // };

        this.lakemedelsbehandling = {
            harHaftYes: element(by.id('lakemedelsbehandling-harHaftYes')),
            harHaftNo: element(by.id('lakemedelsbehandling-harHaftNo')),
            pagarYes: element(by.id('lakemedelsbehandling-pagarYes')),
            pagarNo: element(by.id('lakemedelsbehandling-pagarNo')),
            aktuellText: element(by.id('lakemedelsbehandling-aktuell')),
            pagattYes: element(by.id('lakemedelsbehandling-pagattYes')),
            pagattNo: element(by.id('lakemedelsbehandling-pagattNo')),
            effektYes: element(by.id('lakemedelsbehandling-effektYes')),
            effektNo: element(by.id('lakemedelsbehandling-effektNo')),
            foljsamhetYes: element(by.id('lakemedelsbehandling-foljsamhetYes')),
            foljsamhetNo: element(by.id('lakemedelsbehandling-foljsamhetNo'))
            //avslutadTidpunktDatePickerId: 'datepicker_lakemedelsbehandling.avslutadTidpunkt',
            //avslutadOrsakText: element(by.id('lakemedelsbehandling-avslutadOrsak'))
        };
    },
    // fillIntygerAvser: function(utkast) {
    //     var promiseArr = [];
    //
    //     promiseArr.push(pageHelpers.selectCheckBoxesById(this.intygerAvser.am));
    //     promiseArr.push(pageHelpers.selectCheckBoxesById(this.intygerAvser.a1));
    //
    //     return Promise.all(promiseArr);
    // },
    fillLakemedelsbehandling: function(utkastLakemedelsbehandling) {
        var promiseArr = [];

        if (utkastLakemedelsbehandling.harHaft === 'Ja') {
            promiseArr.push(pageHelpers.moveAndSendKeys(this.lakemedelsbehandling.harHaftYes, protractor.Key.SPACE));
        } else {
            promiseArr.push(pageHelpers.moveAndSendKeys(this.lakemedelsbehandling.harHaftNo, protractor.Key.SPACE));
        }
        if (utkastLakemedelsbehandling.pagar === 'Ja') {
            promiseArr.push(pageHelpers.moveAndSendKeys(this.lakemedelsbehandling.pagarYes, protractor.Key.SPACE));
        } else {
            promiseArr.push(pageHelpers.moveAndSendKeys(this.lakemedelsbehandling.pagarNo, protractor.Key.SPACE));
        }
        promiseArr.push(pageHelpers.moveAndSendKeys(this.lakemedelsbehandling.aktuellText, utkastLakemedelsbehandling.aktuell));
        if (utkastLakemedelsbehandling.pagatt === 'Ja') {
            promiseArr.push(pageHelpers.moveAndSendKeys(this.lakemedelsbehandling.pagattYes, protractor.Key.SPACE));
        } else {
            promiseArr.push(pageHelpers.moveAndSendKeys(this.lakemedelsbehandling.pagattNo, protractor.Key.SPACE));
        }
        if (utkastLakemedelsbehandling.effekt === 'Ja') {
            promiseArr.push(pageHelpers.moveAndSendKeys(this.lakemedelsbehandling.effektYes, protractor.Key.SPACE));
        } else {
            promiseArr.push(pageHelpers.moveAndSendKeys(this.lakemedelsbehandling.effektNo, protractor.Key.SPACE));
        }
        if (utkastLakemedelsbehandling.foljsamhet === 'Ja') {
            promiseArr.push(pageHelpers.moveAndSendKeys(this.lakemedelsbehandling.foljsamhetYes, protractor.Key.SPACE));
        } else {
            promiseArr.push(pageHelpers.moveAndSendKeys(this.lakemedelsbehandling.foljsamhetNo, protractor.Key.SPACE));
        }

        return Promise.all(promiseArr);
    }
});

module.exports = new Tstrk1062Utkast();
