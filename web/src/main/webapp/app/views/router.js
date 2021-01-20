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

/**
 * Created by stephenwhite on 04/03/15.
 */
angular.module('webcert').config(function($stateProvider, $urlRouterProvider, $httpProvider) {
  'use strict';

  var commonPath = '/web/webjars/common/webcert/';

  $stateProvider.state('normal-origin-enhetsval', {
    url: '/normal-origin-enhetsval',
    params: {
      destinationState: null
    },
    views: {
      'header': {
        templateUrl: commonPath + 'components/headers/wcHeader.partial.html'
      },
      'content@': {
        templateUrl: '/app/views/appStartEnhetsval/normal-enhetsval.page.html',
        controller: 'normal.EnhetsvalPageCtrl'
      }
    }
  }).state('integrationenhetsval', {
    url: '/integration-enhetsval',
    views: {
      'header': {
        templateUrl: commonPath + 'components/headers/wcHeader.partial.html'
      },
      'content@': {
        templateUrl: '/app/views/appStartEnhetsval/integration-enhetsval.page.html',
        controller: 'integration.EnhetsvalPageCtrl'
      }
    }
  }).state('webcert', {
    views: {
      'header': {
        templateUrl: commonPath + 'components/headers/wcHeader.partial.html'
      }
    }
  }).state('webcert.index', {
    url: '/',
    views: {
      'landing@': {
        templateUrl: '/app/views/index/index.html',
        controller: 'webcert.IndexCtrl'
      }
    }
  }).state('webcert.create-index', {
    url: '/create/index',
    views: {
      'content@': {
        templateUrl: '/app/views/sokSkrivIntyg/sokSkrivIntyg.html',
        controller: 'webcert.SokSkrivIntygStartCtrl'
      }
    }
  }).state('webcert.create-choosepatient-index', {
    url: '/create/choose-patient/index',
    views: {
      'content@': {
        templateUrl: '/app/views/sokSkrivIntyg/sokSkrivIntyg.html',
        controller: 'webcert.SokSkrivIntygCtrl'
      }
    }
  }).state('webcert.create-choose-certtype-index', {
    url: '/create/choose-intyg-type/:patientId/index',
    params: {
      patientId: ''
    },
    views: {
      'content@': {
        templateUrl: '/app/views/sokSkrivIntyg/sokSkrivValjUtkastType.html',
        controller: 'webcert.SokSkrivValjUtkastTypeCtrl'
      }
    }
  }).state('webcert.enhet-arenden', {
    url: '/enhet-arenden',
    views: {
      'content@': {
        templateUrl: '/app/views/fragorOchSvar/enhetArenden.html',
        controller: 'webcert.enhetArendenCtrl'
      }
    }
  }).state('webcert.unsigned', {
    url: '/unsigned',
    views: {
      'content@': {
        templateUrl: '/app/views/ejSigneradeUtkast/ejSigneradeUtkast.html',
        controller: 'webcert.EjSigneradeUtkastCtrl'
      }
    }
  }).state('webcert.signed-certificates', {
    url: '/signed-certificates',
    views: {
      'content@': {
        templateUrl: '/app/views/signedCertificates/signedCertificates.html',
        controller: 'webcert.SignedCertificatesCtrl'
      }
    }
  }).state('webcert.intyg', {
    data: {defaultActive: 'index', backState: 'webcert.create-choose-certtype-index'},
    views: {
      'content@': {
        templateUrl: '/app/views/visaIntygFragasvar/intyg.html'
      }
    }
  }).state('webcert.fragasvar', {
    data: {backState: 'webcert.enhet-arenden'},
    views: {
      'content@': {
        templateUrl: '/app/views/visaIntygFragasvar/fragasvar.html'
      }
    }
  }).state('webcert.terms', {
    url: '/terms',
    views: {
      'dialogs@webcert': {
        templateUrl: '/app/views/terms/terms.main.html',
        controller: 'webcert.TermsCtrl'
      }
    }
  });

  $urlRouterProvider.when('', ['$window', 'common.UserModel', 'common.authorityService', 'common.featureService',
    function($window, UserModel, authorityService, featureService) {
      if ((UserModel.isLakare() || UserModel.isTandlakare() || UserModel.isPrivatLakare()) &&
          authorityService.isAuthorityActive({feature: featureService.features.HANTERA_INTYGSUTKAST})) {
        return '/create/index';
      } else if (authorityService.isAuthorityActive({feature: featureService.features.HANTERA_FRAGOR})) {
        return '/enhet-arenden';
      } else {
        if (!UserModel.user) {
          return '/';
        } else {
          $window.location.href = '/error.jsp?reason=denied';
        }
      }
    }]);

});
