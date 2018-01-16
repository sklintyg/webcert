angular.module('webcert').directive('wcTidigareIntyg',
    ['common.PatientModel', 'common.messageService', 'common.authorityService', 'common.UserModel',
    'webcert.IntygTypeSelectorModel', '$location', 'common.IntygCopyActions',
    'common.IntygFornyaRequestModel',
    function(PatientModel, messageService, authorityService, UserModel, IntygTypeSelectorModel, 
        $location, CommonIntygCopyActions, IntygFornyaRequestModel) {
        'use strict';

        return {
            restrict: 'E',
            scope: {
                viewState: '='
            },
            link: function(scope, element, attrs) {
                scope.PatientModel = PatientModel;
                scope.openIntyg = function (intyg) {
                    if (intyg.status === 'DRAFT_INCOMPLETE' || intyg.status === 'DRAFT_COMPLETE') {
                        $location.path('/' + intyg.intygType + '/edit/' + intyg.intygId + '/');
                    } else {
                        $location.path('/intyg/' + intyg.intygType + '/' + intyg.intygId + '/');
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
                    return messageService.getProperty(intyg.intygType + '.fornya.tooltip');
                };
                //Use loaded module metadata to look up name for a intygsType
                scope.getTypeName = function (intygsType) {
                    var intygTypes = IntygTypeSelectorModel.intygTypes.filter(function (intygType) {
                        return (intygType.id === intygsType);
                    });
                    if (intygTypes && intygTypes.length > 0) {
                        return intygTypes[0].label;
                    }
                };
            },
            templateUrl: '/app/views/sokSkrivIntyg/tidigareIntyg.directive.html'
        };
    }]
);
