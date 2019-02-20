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

/*globals describe,it,browser */
'use strict';
var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var restUtil = wcTestTools.restUtil;
var SokSkrivIntygPage = wcTestTools.pages.sokSkrivIntyg.pickPatient;
var SokSkrivValjUtkastType = wcTestTools.pages.sokSkrivIntyg.valjUtkastType;

describe('Verify favoritmarkerad', function() {

    var intygsTyp = 'ag114';

    it('can add and remove favoritmarkerad intygstyp', function() {
        browser.ignoreSynchronization = false;
        restUtil.deleteAnvandarPreference('TSTNMT2321000156-103F', 'wc.favoritIntyg');
        specHelper.login();

        SokSkrivIntygPage.selectPersonnummer('191212121212');
        expect(SokSkrivValjUtkastType.isAt());

        // check intygstyp is moved to top when selected as favourite
        SokSkrivValjUtkastType.clickToggleFavourite(intygsTyp);
        expect(SokSkrivValjUtkastType.verifyTypeIsAtIndex(intygsTyp, 0)).toBe(true);

        // reload page and check favourite is still on top
        browser.refresh();
        expect(SokSkrivValjUtkastType.verifyTypeIsAtIndex(intygsTyp, 0)).toBe(true);

        // Remove intyg from favourites, and then it should not be on top of list
        SokSkrivValjUtkastType.clickToggleFavourite(intygsTyp);
        expect(SokSkrivValjUtkastType.verifyTypeIsAtIndex(intygsTyp, 0)).toBe(false);

    });
});
