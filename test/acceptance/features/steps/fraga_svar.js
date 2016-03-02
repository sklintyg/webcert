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

/* globals pages, intyg, protractor, logger*/

'use strict';
var fkIntygPage = pages.intyg.fk['7263'].intyg;
module.exports = function() {
    this.Given(/^jag skickar en fråga med ämnet "([^"]*)" till Försäkringskassan$/, function(amne, callback) {
        fkIntygPage.question.newQuestionButton.sendKeys(protractor.Key.SPACE);
        fkIntygPage.question.text.sendKeys('En ' + amne + '-fråga');
        fkIntygPage.selectQuestionTopic(amne);

        fkIntygPage.question.sendButton.sendKeys(protractor.Key.SPACE);

        fkIntygPage.qaPanel.getAttribute('id').then(function(result) {
            intyg.fragaId = result.split('-')[1];
            logger.debug('Frågans ID: ' + intyg.fragaId);
            callback();
        });
    });
};
