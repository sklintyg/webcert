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

angular.module('webcert').factory('webcert.SignedCertificatesListModel', [
    function() {
    'use strict';

    function SignedCertificatesListModel() {
      this.DEFAULT_NUMBER_PAGES = 10;
      this.DEFAULT_PAGE_SIZE = 10;
      this.DEFAULT_PAGE = 1;
      this.LIST_NAME = 'signedCertificateList';
      this.certificates = [];
      this.totalCount = 0;
      this.nbrOfUnfilteredCertificates = null;
      this.reset();
    }

    SignedCertificatesListModel.prototype.resetListPageNumberValues = function(){
      this.chosenPage = this.DEFAULT_PAGE;
      this.chosenPageList = this.DEFAULT_PAGE;
    };

    SignedCertificatesListModel.prototype.resetNavigationValues = function() {
        this.resetListPageNumberValues();
        this.limit = this.DEFAULT_PAGE_SIZE;
    };

    SignedCertificatesListModel.prototype.reset = function() {
        this.runningQuery = false;
        this.activeErrorMessageKey = null;
        this.resetNavigationValues();
    };

    SignedCertificatesListModel.build = function() {
      return new  SignedCertificatesListModel();
    };

    return  SignedCertificatesListModel;
  }]);
