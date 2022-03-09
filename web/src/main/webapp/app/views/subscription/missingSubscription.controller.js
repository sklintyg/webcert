/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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

angular.module('webcert').controller('webcert.MissingSubscriptionCtrl', ['$log', '$rootScope', '$scope', '$window', '$sanitize', '$state',
  '$location', 'common.UserModel', 'common.subscriptionService', 'common.dynamicLinkService', 'common.SubscriptionProxy',
  'common.messageService',
  function($log, $rootScope, $scope, $window, $sanitize, $state, $location, UserModel, subscriptionService, dynamicLinkService,
      subscriptionProxy, messageService) {
    'use strict';

    UserModel.transitioning = false;

    $scope.modalBody = {
      infoCheck: messageService.getProperty('subscription.missing.info.check'),
      infoGeneral: messageService.getProperty('subscription.missing.info.general'),
      infoLink: 'subscription.missing.info.link'
    };

    $scope.modalOptions = {
      body: $scope.modalBody,
      modalBodyTemplateUrl: '/app/views/subscription/missingSubscription.body.html',
      titleId: 'subscription.missing.title.text',
      buttons: [
        {
          name: 'subscription.missing.modal.close',
          clickFn: function() {
            subscriptionService.acknowledgeSubscriptionModal();
            subscriptionProxy.acknowledgeSubscriptionModal(function() {
              $scope.modalOptions.modalInstance.close();
              $state.go('webcert.create-index');
            });
          },
          text: 'common.close',
          id: 'missingSubscriptionModalClose',
          className: 'btn-default'
        }
      ],
      showClose: false
    };
  }]
);
