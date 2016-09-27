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

angular.module('webcert').factory('webcert.SokSkrivIntygViewstate',
    ['$log', 'common.messageService',
        function($log, messageService) {
            'use strict';

            return {
                build: function() {
                    this.focusFirstInput = true;

                    this.patientLoading = false;
                    this.tidigareIntygLoading = false;

                    this.loadErrorMessageKey = null;
                    this.intygListErrorMessageKey = null;
                    this.createErrorMessageKey = null;
                    this.inlineErrorMessageKey = null;

                    this.currentList = undefined;
                    this.unsigned = 'intyglist-empty'; // unsigned, unsigned-mixed,
                    this.intygFilter = 'current'; // possible values: current, revoked, all
                    this.fornyaTitleText = messageService.getProperty('fk7263.label.fornya.text');
                    return this;
                }
            };
        }
    ]);
