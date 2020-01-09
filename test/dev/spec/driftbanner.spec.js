/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

/*globals browser */
/*globals pages */
/*globals describe,it,helpers */
'use strict';
var wcTestTools = require('webcert-testtools');
var testdataHelper = wcTestTools.helpers.restTestdata;

describe('Driftbanner', function() {

  var banners = $$('#service-banners .alert');

  beforeAll(function() {
    browser.get('/');
  });

  it('Inga banners default', function() {
    expect(banners.count()).toBe(0);
  });

  it('Skapa banner', function() {
    testdataHelper.createBanners("message", "HOG");
    browser.refresh();
    expect(banners.count()).toBe(1);
    expect(banners.first().$('span').getText()).toEqual('message');
  });

  it('Ta bort banner', function() {
    testdataHelper.clearBanners();
    browser.refresh();

    expect(banners.count()).toBe(0);
  });
});
