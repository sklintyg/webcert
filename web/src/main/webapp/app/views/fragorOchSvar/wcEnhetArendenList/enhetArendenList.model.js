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

angular.module('webcert').service('webcert.enhetArendenListModel', [
  'webcert.enhetArendenModel',
  function(enhetArendenModel) {
    'use strict';

    this.DEFAULT_PAGE = 1;
    this.DEFAULT_NUMBER_PAGES = 10;
    this.DEFAULT_PAGE_SIZE = 10;
    this.LIST_NAME = 'enhetArendenList';

    this.reset = function() {
      // General directive viewstate
      this.viewState = {
        runningQuery: false,
        activeErrorMessageKey: null,
        fetchingMoreInProgress: false
      };

      // same as filter query above, stores previous request
      this.prevFilterQuery = {};

      // The actual list of arenden fetched from backend
      this.arendenList = [];
      this.totalCount = 0;
    };

    this.reset();
  }]);
