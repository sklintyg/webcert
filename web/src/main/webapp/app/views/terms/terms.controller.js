angular.module('webcert').controller('webcert.TermsCtrl', ['$log', '$rootScope', '$scope', '$window', '$modal', '$sanitize', '$state',
        'common.AvtalProxy', 'common.UserModel', 'webcert.TermsState',
    function($log, $rootScope, $scope, $window, $modal, $sanitize, $state, AvtalProxy, UserModel, TermsState) {
        'use strict';
        $scope.doneLoading = false;

        TermsState.reset();

        // load the avtal
        AvtalProxy.getLatestAvtal(function(avtalModel){
            $scope.avtal = avtalModel;
            $scope.doneLoading = true;
        }, function(){

            $scope.doneLoading = true;
        });

        function endsWith(str, suffix) {
            return str.indexOf(suffix, str.length - suffix.length) !== -1;
        }

        $scope.modal = {
            titleId : 'avtal.title.text',
            extraDlgClass : undefined,
            width : "80%",
            height : "90%",
            maxWidth : undefined,
            maxHeight : undefined,
            minWidth : undefined,
            minHeight : undefined,
            contentHeight: "100%",
            contentOverflowY : undefined,
            contentMinHeight : "550px",
            bodyOverflowY: 'scroll',
            templateUrl: "/app/views/terms/terms.modal.content.html",
            windowTemplateUrl: "/app/views/terms/modalWindow.html",
            button1text: 'avtal.approve.label',
            button1id: 'button1approve-dialog-terms',
            button2text: 'avtal.print.label',
            button2id: 'button2print-dialog-terms',
            button3text: 'avtal.logout.label',
            button3id: 'button3logout-dialog-terms',
            showClose: false,


            approve : function(){
                AvtalProxy.approve(
                    function(){
                        TermsState.termsAccepted = true;
                        $scope.modalInstance.dismiss('cancel');
                        $state.transitionTo('webcert.create-index');
                    },
                    function(){
                        TermsState.termsAccepted = false;
                        $window.location = '/web/error';
                    }
                );
            },
            print : function(){
                var printContents = $sanitize($scope.avtal.avtalText);

                if (navigator.userAgent.toLowerCase().indexOf('chrome') > -1) {
                    var popupWin = window.open('', '_blank', 'width=400,scrollbars=no,menubar=no,toolbar=no,location=no,status=no,titlebar=no');
                    popupWin.window.focus();
                    popupWin.document.write('<!DOCTYPE html><html><head>' +
                        '<link rel="stylesheet" href="/web/webjars/common/webcert/css/print.css" media="print">' +
                        '</head><body onload="window.print()"><h1>WebCert Användarvilkor</h1>' + printContents + '</html>');
                    popupWin.onbeforeunload = function (event) {
                        popupWin.close();
                        return '.\n';
                    };
                    popupWin.onabort = function (event) {
                        popupWin.document.close();
                        popupWin.close();
                    };
                } else {
                    var popupWin = window.open('', '_blank', 'width=400');
                    popupWin.document.open();
                    popupWin.document.write('<html><head><link rel="stylesheet" href="/web/webjars/common/webcert/css/print.css" media="print"></head><body onload="window.print()"><h1>WebCert Användarvilkor</h1>' + printContents + '</html>');
                    popupWin.document.close();
                }
                popupWin.document.close();

                return true;
            },
            logout : function(){
                if (endsWith(UserModel.userContext.authenticationScheme, ':fake')) {
                    $window.location = '/logout';
                } else {
                    iid_Invoke('Logout');
                    $window.location = '/saml/logout/';
                }
            }
        };

        $scope.open = function ()
        {
            $scope.modalInstance = $modal.open(
                {
                    backdrop: 'static',
                    keyboard: false,
                    modalFade: true,

                    templateUrl: $scope.modal.templateUrl,
                    windowTemplateUrl: $scope.modal.windowTemplateUrl,
                    scope: $scope,
                    //size: size,   - overwritten by the extraDlgClass below (use 'modal-lg' or 'modal-sm' if desired)

                    extraDlgClass: $scope.modal.extraDlgClass,

                    width: $scope.modal.width,
                    height: $scope.modal.height,
                    maxWidth: $scope.modal.maxWidth,
                    maxHeight: $scope.modal.maxHeight,
                    minWidth: $scope.modal.minWidth,
                    minHeight: $scope.modal.minHeight
                });

            $scope.modalInstance.result.then(function ()
                {
                    $log.info('Modal closed at: ' + new Date());
                },
                function ()
                {
                    $log.info('Modal dismissed at: ' + new Date());
                });
        };

        $scope.close = function($event){
            if ($event)
                $event.preventDefault();
            $scope.modalInstance.dismiss('cancel');
        };

        $scope.cancel = function ($event)
        {
            if ($event)
                $event.preventDefault();
            $scope.modalInstance.dismiss('cancel');
        };

        // open the modal
        $scope.open();

    }]

);