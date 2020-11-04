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

angular.module('webcert').factory('webcert.SignedCertificatesFilterModel', ['$filter', function($filter) {
  'use strict';

  /**
   * Constructor
   */
  function SignedCertificatesFilterModel() {
    this.filterForm = {};
    this.pageSize = 0;
    this.reset();
  }

  SignedCertificatesFilterModel.prototype.reset = function() {
    this.startFrom = 0;
    this.filterForm.patientId = '';
    this.filterForm.orderBy = 'signedDate';
    this.filterForm.orderAscending = false;
    this.filterForm.signedTo = null;
    this.filterForm.signedFrom = moment().subtract(3, 'month').format('YYYY-MM-DD');
  };

  SignedCertificatesFilterModel.prototype.convertToPayload = function() {

    var query = {
      pageSize: this.pageSize,
      startFrom: this.startFrom,
      patientId: this.filterForm.patientId,
      orderBy: this.filterForm.orderBy,
      orderAscending: this.filterForm.orderAscending,
      signedTo: this.filterForm.signedTo,
      signedFrom: this.filterForm.signedFrom
    };

    return query;
  };

  SignedCertificatesFilterModel.build = function() {
    return new SignedCertificatesFilterModel();
  };

  return SignedCertificatesFilterModel;
}]);

