'use strict';

/* Controllers */

/*
 *  CreateCertCtrl - Controller for logic related to creating a new certificate 
 * 
 */
angular.module('wcDashBoardApp').controller('CreateCertCtrl', [ '$scope', '$rootScope', '$window', '$log', '$location', '$filter', '$timeout', 'wcDialogService', 'dashBoardService', function CreateCertCtrl($scope, $rootScope, $window, $log, $location, $filter, $timeout, wcDialogService, dashBoardService) {

	//var currentRoute = $location.path().substr($location.path().lastIndexOf('/') + 1);

	$scope.widgetState = {
			doneLoading : false,
      activeErrorMessageKey : null,
      currentList : undefined,
      showHiddenCerts : false
	};
	
	$scope.personnummer = "";
	if ($rootScope.personnummer != undefined){
		$scope.personnummer = $rootScope.personnummer;
	}
	
	$scope.certType = {
		selected : "default",
		types : [
		  {id:"default", name: "Välj intygstyp"},
			{id:"fk7263", name: "Läkarintyg FK 7263"},
			{id:"ts-bas", name: "Läkarintyg Transportstyrelsen Bas"}
		]
	};

  // Navigation functions
	$scope.toStep1 = function() {	$location.path("/index"); };
	$scope.toEditPatient = function() {
    if ($scope.pnrForm.$valid) {
      $rootScope.personnummer = $scope.personnummer;
      $location.path("/edit-patient/index");
    } else {
      $scope.pnrForm.submitted = true;
    }
	}

	$scope.toStep2 = function() {
		$rootScope.patientNamn = $scope.patientNamn;
		$location.path("/choose-cert/index");	
	}

	$scope.toStep3 = function() {	$location.path("/choose-unit/index");	}

	$scope.editCert = function() {
    $log.debug("edit cert");

    if($scope.certType.selected != "fk7263"){
      $scope.confirmAddressDialog($scope.certType.selected);
    } else {
      $window.location.href = "/m/fk7263/webcert/intyg/new/edit#/edit";
    }
  };

  // List interaction functions
  $scope.openIntyg = function(cert){

  };

  $scope.copyIntyg = function(cert){
    wcDialogService.showDialog(
        $scope,
        {
          dialogId: "copy-dialog",
          titleId: "label.copycert",
          bodyText: "<p>När du kopierar detta intyg får du upp ett nytt intyg av samma typ och med samma information som finns i det intyg som du kopierar. Du får möjlighet att redigera informationen innan du signerar det nya intyget.</p><div class='form-inline'><input id='dontShowAgain' type='checkbox' ng-model='dontShowCopyInfo'> <label for='dontShowAgain'>Visa inte denna information igen</label></div>",
          button1click: function() {
            $log.debug("copy cert");
          },
          button1text: "common.copy",
          button2text: "common.cancel"
        }
    );
  };

  $scope.confirmAddressDialog = function(certType) {

    var address = "Repslagaregatan 25,<br>58222, Linköping";
    var bodyText = "Patienten har tidigare intyg där adressuppgifter har angivits. Vill du återanvända dessa i det nya intyget?<br><br>Adress: "+address;

    wcDialogService.showDialog(
        $scope,
        {
          dialogId: "confirm-address-dialog",
          titleId: "label.confirmaddress",
          bodyText: bodyText,
          button1click: function() {
            $log.debug("confirm address yes");
            $window.location.href = "/m/"+certType+"/webcert/intyg/new/edit#/edit";
          },
          button2click: function() {
            $log.debug("confirm address no");
            $window.location.href = "/m/"+certType+"/webcert/intyg/new/edit#/edit";
          },
          button1text: "common.yes",
          button2text: "common.no",
          button3text: "common.cancel"
        }
    );
  };
		
  $scope.setActiveUnit = function(unit) {
    $log.debug("ActiveUnit is now:" + unit);
    $scope.activeUnit = unit;
/*
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
  };

  $scope.updateCertList = function(){
      $scope.widgetState.currentList = $filter('CertDeletedFilter')($scope.widgetState.certListUnhandled, $scope.widgetState.showHiddenCerts);
  };

  // TEST --------------
  if(!$scope.personnummer || $scope.personnummer == '') $scope.personnummer = "19121212-1212";
  // --------------------

  $scope.widgetState.activeErrorMessageKey = null;
  $scope.widgetState.doneLoading = true;

  $timeout(function() {
    dashBoardService.getCertificatesForPerson($scope.personnummer, function(data) {
      $scope.widgetState.doneLoading = false;
      $scope.widgetState.certListUnhandled = data;
      $scope.updateCertList();
    }, function(errorData) {
      $scope.widgetState.doneLoading = false;
      $log.debug("Query Error"+errorData);
      $scope.widgetState.activeErrorMessageKey = "info.certload.error";
    });

  }, 500);

} ]);

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
  $window.location.href = "/m/" + item.typ.toLowerCase() + "/webcert/intyg/" + item.id + "#/view";
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
    };

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
    };

    $scope.qaCertList = [];

    // Load list
    var requestConfig = {
        "type" : "dashboard_unanswered.json",
        "careUnit" : "",
        "clinic" : []
    };
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
