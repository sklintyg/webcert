angular.module('webcertTest', []);
angular.module('webcertTest').factory('mockFactory', function() {
    'use strict';

    return {
        buildUserMinimal: function() {
            return {
                getValdVardenhet: function() {
                    return {
                        id: 'enhet1',
                        namn: 'Vårdenheten'
                    };
                }
            };
        },
        buildDialogService: function() {
            var modalMock;
            var dialogService = jasmine.createSpyObj('common.dialogService', [ 'showDialog' ]);
            modalMock = jasmine.createSpyObj('modal', [ 'close' ]);
            dialogService.showDialog.and.callFake(function() {
                return modalMock;
            });
            return dialogService;
        }
    };
});
