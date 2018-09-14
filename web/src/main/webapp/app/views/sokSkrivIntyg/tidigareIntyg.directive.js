angular.module('webcert').directive('wcTidigareIntyg',
    ['$filter','common.PatientModel', 'common.messageService', 'common.authorityService', 'common.UserModel',
    '$location', 'common.IntygCopyActions',
    'common.IntygFornyaRequestModel', 'webcert.SokSkrivValjUtkastService',
    function($filter, PatientModel, messageService, authorityService, UserModel,
        $location, CommonIntygCopyActions, IntygFornyaRequestModel, SokSkrivValjUtkastService) {
        'use strict';

        return {
            restrict: 'E',
            scope: {
                viewState: '='
            },
            link: function(scope, element, attrs) {
                scope.PatientModel = PatientModel;
                scope.openIntyg = function (intyg) {
                    if (intyg.status === 'DRAFT_INCOMPLETE' || intyg.status === 'DRAFT_COMPLETE' || intyg.status === 'DRAFT_LOCKED' || intyg.status === 'DRAFT_LOCKED_CANCELLED') {
                        $location.path('/' + intyg.intygType + '/' + intyg.intygTypeVersion + '/edit/' + intyg.intygId + '/');
                    } else {
                        $location.path('/intyg/' + intyg.intygType + '/' + intyg.intygTypeVersion + '/' + intyg.intygId + '/');
                    }
                };
                scope.messageService = messageService;
                scope.isRenewalAllowed = function (intyg) {
                    var renewable = authorityService.isAuthorityActive(
                        {
                            requestOrigin: UserModel.user.origin,
                            authority: UserModel.privileges.FORNYA_INTYG,
                            intygstyp: intyg.intygType
                        });
                    var statusAllowed = intyg.status.indexOf('DRAFT') === -1 && intyg.status !== 'CANCELLED';
    
                    return renewable &&
                        statusAllowed &&
                        !(intyg.relations.latestChildRelations.replacedByIntyg ||
                            intyg.relations.latestChildRelations.complementedByIntyg);
                };
                scope.fornyaIntyg = function (intyg) {
                    
                    scope.viewState.createErrorMessageKey = null;
    
                    // We don't have the required info about issuing unit in the supplied 'intyg' object, always set to true.
                    // It only affects a piece of text in the Kopiera-dialog anyway.
                    var isOtherCareUnit = true;
    
                    CommonIntygCopyActions.fornya(scope.viewState,
                        IntygFornyaRequestModel.build({
                            intygId: intyg.intygId,
                            intygType: intyg.intygType,
                            patientPersonnummer: PatientModel.personnummer,
                            nyttPatientPersonnummer: null
                        }),
                        isOtherCareUnit
                    );
                };
                scope.resolveTooltipText = function (intyg) {
                    if(intyg.intygType === 'lisjp' || intyg.intygType === 'fk7263') {
                        return messageService.getProperty('common.fornya.sjukskrivning.tooltip');
                    }
                    return messageService.getProperty('common.fornya.tooltip');
                };
                scope.$watch('viewState.intygFilter', function() {
                    SokSkrivValjUtkastService.updateIntygList(scope.viewState);
                });

                scope.orderProperty = 'lastUpdatedSigned';
                scope.orderAscending = false;
                scope.orderByProperty = function(property) {
                    if (scope.orderProperty === property) {
                        scope.orderAscending = !scope.orderAscending;
                    } else {
                        scope.orderAscending = true;
                    }
                    scope.orderProperty = property;
                };
            },
            templateUrl: '/app/views/sokSkrivIntyg/tidigareIntyg.directive.html'
        };
    }]
);
