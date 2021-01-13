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

/* globals browser, logger */

'use strict';

module.exports = {
  container: element(by.id('certificate-content-container')),

  get: function(intygTyp, intygVersion, intygsId) {
    return browser.get('/#/intyg-read-only/' + intygTyp + '/' + intygVersion + '/' + intygsId);
  },
  getKompletteringLinkElement: function(meddelandeId) {
    return element(by.css('#kompletteringar-fkKompletteringar-' + meddelandeId + ' button'));
  },
  getKompletteringFrageTextElement: function(meddelandeId) {
    return element(by.id('kompletteringar-arende-fragetext-' + meddelandeId));
  },
  getIntygKompletteringFrageContainer: function(frageId) {
    return element(by.id('inline-komplettering-FRG_' + frageId + '.RBK-0'));
  },
  isAt: function() {
    return this.container.isDisplayed();
  }
};
