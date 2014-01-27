'use strict';

/* Controllers */

/*
 *  CreateCertCtrl - Controller for logic related to creating a new certificate 
 * 
 */
angular.module('wcDashBoardApp').controller('CreateCertCtrl', [ '$scope', '$rootScope', '$window', '$log', '$location', 'wcDialogService', function CreateCertCtrl($scope, $rootScope, $window, $log, $location, wcDialogService) {

	//var currentRoute = $location.path().substr($location.path().lastIndexOf('/') + 1);

	$scope.widgetState = {
			doneLoading : false,
			runningQuery : false,
      activeErrorMessageKey : null,
      queryMode : false,
      currentList : undefined
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
			{id:"tsbas", name: "Transportstyrelsen Läkarintyg Bas"}
		]
	};
	
	$scope.widgetState.currentList = [
	 {type: "fk7263", status: "Signerat", senastSparat:"2014-01-01", sparatAv: "Gunilla Andersson"},
	 {type: "fk7263", status: "Signerat", senastSparat:"2014-01-01", sparatAv: "Gunilla Andersson"}
	];
	
	$scope.toStep1 = function() {	$location.path("/index"); };
	$scope.toEditPatient = function() {
		$rootScope.personnummer = $scope.personnummer;
		$location.path("/edit-patient/index");
	}

	$scope.toStep2 = function() {
		$rootScope.patientNamn = $scope.patientNamn;
		$location.path("/choose-cert/index");	
	}

	$scope.toStep3 = function() {	$location.path("/choose-unit/index");	}

	$scope.editCert = function() {
    $log.debug("edit cert");

    if($scope.certType.selected != "fk7263"){
      $scope.confirmAddressDialog();
    } else {
      $window.location.href = "/m/fk7263/webcert/intyg/new/edit#/edit";
    }
  }

  $scope.openIntyg = function(cert){

  }

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
  }

  $scope.confirmAddressDialog = function() {

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
          },
          button2click: function() {
            $log.debug("confirm address no");
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
