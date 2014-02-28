'use strict';

/* Controllers */

/*
 *  CreateCertCtrl - Controller for logic related to creating a new certificate 
 * 
 */
angular.module('wcDashBoardApp').controller('CreateCertCtrl', [ '$scope', '$window', '$log', '$location', function CreateCertCtrl($scope, $window, $log, $location) {

	$scope.toStep1 = function() {	$location.path("/index"); }
	$scope.toEditPatient = function() { $location.path("/edit-patient/index"); }
	$scope.toStep2 = function() {	$location.path("/choose-cert/index"); }
	$scope.toStep3 = function() {	$location.path("/choose-unit/index");	}
	$scope.editCert = function() {
    $log.debug("edit cert");
    var path = "/m/fk7263/webcert/intyg/new/edit#/edit";
    $window.location.href = path;
	}
	
  $scope.setActiveUnit = function(unit) {
    $log.debug("ActiveUnit is now:" + unit);
    $scope.activeUnit = unit;
/*    $scope.widgetState.queryMode = false;
    $scope.widgetState.queryFormCollapsed = true;

    //If we change enhet then we probably don't want the same filter criterias
    if($cookieStore.get("enhetsId") && $cookieStore.get("enhetsId")!=unit.id){
        $scope.resetSearchForm();
    }
    $cookieStore.put("enhetsId" ,unit.id);
    //If we have a query stored, open the advanced filter
    if($cookieStore.get("query_instance")){
        $scope.widgetState.queryFormCollapsed = false
        $scope.doSearch();
    }
    $scope.initDoctorList(unit.id);
    $scope.widgetState.currentList = $filter('QAEnhetsIdFilter')($scope.qaListUnhandled, $scope.activeUnit.id);
 		*/
  }
	
} ]);

controllers.controller('EditPatientNameCtrl', ['$scope', '$location', 'CertificateDraft',
    function ($scope, $location, CertificateDraft) {
        if (!CertificateDraft.personnummer) {
            $location.url('/create/choose-patient/index', true);
        }

        $scope.personnummer = CertificateDraft.personnummer;
        $scope.firstname = CertificateDraft.firstname;
        $scope.lastname = CertificateDraft.lastname;

        $scope.chooseCertType = function () {
            CertificateDraft.firstname = $scope.firstname;
            CertificateDraft.lastname = $scope.lastname;
            $location.path('/create/choose-cert-type/index');
        };

        $scope.changePatient = function () {
            $location.path('/create/index');
        };
    }]);

controllers.controller('ChooseCertTypeCtrl', ['$rootScope', '$scope', '$window', '$location', '$filter', '$log', '$timeout', 'wcDialogService',
    'dashBoardService', 'CertificateDraft', 'User',
    function ($rootScope, $scope, $window, $location, $filter, $log, $timeout, wcDialogService, dashBoardService, CertificateDraft, User) {
        if (!CertificateDraft.personnummer || !CertificateDraft.firstname || !CertificateDraft.lastname) {
            $location.url('/create/index', true);
        }

        $scope.personnummer = CertificateDraft.personnummer;
        $scope.firstname = CertificateDraft.firstname;
        $scope.lastname = CertificateDraft.lastname;

        CertificateDraft.getCertTypes(function (types) {
            $scope.certTypes = types;
            $scope.intygType = CertificateDraft.intygType;
        });

        function _createDraft () {
          var valdVardenhet = User.getValdVardenhet();
            CertificateDraft.vardGivareHsaId = valdVardenhet.id;
            CertificateDraft.vardGivareNamn= valdVardenhet.namn;
            CertificateDraft.vardEnhetHsaId = valdVardenhet.id;
            CertificateDraft.vardEnhetNamn = valdVardenhet.namn;

            if (CertificateDraft.intygType === 'fk7263') {
                // TODO: Remove this hard coded redirect.
                $window.location.href = '/m/' + CertificateDraft.intygType + '/webcert/intyg/12345/edit#/edit';
                CertificateDraft.reset();
            } else {
                CertificateDraft.createDraft(function (data) {
                    $window.location.href = '/m/' + CertificateDraft.intygType + '/webcert/intyg/' + data + '/edit#/edit';
                    CertificateDraft.reset();
                });
            }
        }

        $scope.lookupAddress = function () {
            CertificateDraft.intygType = $scope.intygType;
            if (CertificateDraft.address) {
                var bodyText = 'Patienten har tidigare intyg där adressuppgifter har angivits. Vill du återanvända dessa i det nya intyget?<br>' +
                    '<br>Adress: ' + CertificateDraft.address;

                wcDialogService.showDialog($scope, {
                    dialogId : 'confirm-address-dialog',
                    titleId : 'label.confirmaddress',
                    bodyText : bodyText,

                    button1click : function () {
                        $log.debug('confirm address yes');
                        _createDraft();
                    },
                    button2click : function () {
                        $log.debug('confirm address no');
                        CertificateDraft.address = null;
                        _createDraft();
                    },

                    button1text : 'common.yes',
                    button2text : 'common.no',
                    button3text : 'common.cancel'
                });
            } else {
                _createDraft();
            }
        };

        $scope.changePatient = function () {
            $location.path('/create/index');
        };
        $scope.editPatientName = function () {
            $location.path('/create/edit-patient-name/index');
        };

        // List of old certificates.

        $scope.widgetState = {
            doneLoading : false,
            activeErrorMessageKey : null,
            currentList : undefined,
            queryFormCollapsed : true
        };

        $scope.updateCertList = function () {
            $scope.widgetState.currentList = $filter('CertDeletedFilter')($scope.widgetState.certListUnhandled, false); // TODO: Use search filter instead of "false"
        };

        $scope.widgetState.activeErrorMessageKey = null;
        $scope.widgetState.doneLoading = true;

        $timeout(function () {
            dashBoardService.getCertificatesForPerson($scope.personnummer, function (data) {
                $scope.widgetState.doneLoading = false;
                $scope.widgetState.certListUnhandled = data;
                $scope.updateCertList();
            }, function (errorData) {
                $scope.widgetState.doneLoading = false;
                $log.debug('Query Error' + errorData);
                $scope.widgetState.activeErrorMessageKey = 'info.certload.error';
            });
        }, 500);

        $scope.openIntyg = function (cert) {
          $window.location.href = '/m/' + cert.intygType + '/webcert/intyg/' + cert.intygId + '/edit#/edit';
          CertificateDraft.reset();
        };

        $scope.copyIntyg = function (cert) {
          //CertificateDraft.reset();
            wcDialogService.showDialog($scope, {
                dialogId : 'copy-dialog',
                titleId : 'label.copycert',
                bodyText : '<p>När du kopierar detta intyg får du upp ett nytt intyg av samma typ och med samma information som finns i det intyg som du kopierar. Du får möjlighet att redigera informationen innan du signerar det nya intyget.</p><div class=\'form-inline\'><input id=\'dontShowAgain\' type=\'checkbox\' ng-model=\'dontShowCopyInfo\'> <label for=\'dontShowAgain\'>Visa inte denna information igen</label></div>',
                button1click : function () {
                    $log.debug('copy cert');
                },
                button1text : 'common.copy',
                button2text : 'common.cancel'
            });
        };
    }]);


/*
 *  WebCertCtrl - Controller for logic related to displaying the list of a doctors unsigned certificates (mina osignerade intyg) 
 * 
 */

angular.module('wcDashBoardApp').controller('WebCertCtrl', [ '$scope', '$window','$log','$location', function WebCertCtrl($scope, $window, $log, $location) {
  // Main controller

$scope.createCert = function() {
	$location.path("/index");
}

$scope.viewCert = function(item) {
  $log.debug("open " + item.id);
  //listCertService.selectedCertificate = item;
  var path = "/m/" + item.typ.toLowerCase() + "/webcert/intyg/" + item.id + "#/view"
  $window.location.href = path;
}

} ]);


/*
 *  ListUnsignedCertCtrl - Controller for logic related to displaying the list of unsigned certificates 
 * 
 */
angular.module('wcDashBoardApp').controller('ListUnsignedCertCtrl', [ '$scope', 'dashBoardService', '$log', '$timeout', function ListUnsignedCertCtrl($scope, dashBoardService, $log, $timeout) {
    $log.debug("ListUnsignedCertCtrl init()");
    // init state
    $scope.widgetState = {
        showMin : 2,
        showMax : 99,
        pageSize : 2,
        doneLoading : false,
        hasError : false
    }

    $scope.wipCertList = [];

    $scope.$on("vardenhet", function(event, vardenhet) {
        // Make new call with careUnit
    });
    $scope.$on("mottagning", function(event, mottagning) {
        // Make new call with clinic
    });

    // Load list
    var requestConfig = {
        "type" : "dashboard_unsigned.json",
        "careUnit" : "",
        "clinic" : []
    }
    $timeout(function() { // wrap in timeout to simulate latency - remove soon
        dashBoardService.getCertificates(requestConfig, function(data) {
            $scope.widgetState.doneLoading = true;
            if (data != null) {
                $scope.wipCertList = data;
            } else {
                $scope.widgetState.hasError = true;
            }
        });
    }, 1000);
} ]);


/*
 *  UnansweredCertCtrl - Controller for logic related to displaying the list of unanswered questions 
 *  for a certificate on the dashboard.
 * 
 */
angular.module('wcDashBoardApp').controller('UnansweredCertCtrl', [ '$scope', 'dashBoardService', '$log', '$timeout', function UnansweredCertCtrl($scope, dashBoardService, $log, $timeout) {
    $log.debug("UnansweredCertCtrl init()");
    // init state
    $scope.widgetState = {
        showMin : 2,
        showMax : 99,
        pageSize : 2,
        doneLoading : false,
        hasError : false
    }

    $scope.qaCertList = [];

    // Load list
    var requestConfig = {
        "type" : "dashboard_unanswered.json",
        "careUnit" : "",
        "clinic" : []
    }
    $timeout(function() { // wrap in timeout to simulate latency - remove soon
        dashBoardService.getCertificates(requestConfig, function(data) {
            $scope.widgetState.doneLoading = true;
            if (data != null) {
                $scope.qaCertList = data;
            } else {
                $scope.widgetState.hasError = true;
            }
        });
    }, 500);
    
} ]);

/*
 *  ReadyToSignCertCtrl - Controller for logic related to displaying the list of certificates ready to mass-sign on the dashboard 
 *  
 * 
 */
angular.module('wcDashBoardApp').controller('ReadyToSignCertCtrl', [ '$scope', 'dashBoardService', '$log', '$timeout', function ReadyToSignCertCtrl($scope, dashBoardService, $log, $timeout) {
    $log.debug("ReadyToSignCertCtrl init()");
    // init state
    $scope.widgetState = {
        showMin : 2,
        showMax : 99,
        pageSize : 2,
        doneLoading : false,
        hasError : false
    }

    $scope.signCertList = [];

    // Load list
    var requestConfig = {
        "type" : "dashboard_readytosign.json",
        "careUnit" : "",
        "clinic" : []
    }
    $timeout(function() { // wrap in timeout to simulate latency - remove soon
        dashBoardService.getCertificates(requestConfig, function(data) {
            $scope.widgetState.doneLoading = true;
            if (data != null) {
                $scope.signCertList = data;
            } else {
                $scope.widgetState.hasError = true;
            }
        });
    }, 500);
} ]);

/*
 *  AboutWebcertCtrl - Controller for logic related to creating a new certificate 
 * 
 */
angular.module('wcDashBoardApp').controller('AboutWebcertCtrl', [ '$scope', '$window', function AboutWebcertCtrl($scope, $window) {

} ]);
