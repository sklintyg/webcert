 /*globals Handlebars*/


 'use strict';

 var accResults;
 /*AddResultHere*/

 Handlebars.registerHelper('if_eq', function(a, b, opts) {
     if (a === b) { // Or === depending on your needs
         return opts.fn(this);
     } else {
         return opts.inverse(this);
     }
 });

 Handlebars.registerHelper('atobData', function() {
     return atob(this.data);
 });

 Handlebars.registerHelper('statusAsBoostrap', function() {
     var status = '';
     if (this.result && this.result.status) {
         status = this.result.status;
     } else if (this.status) {
         status = this.status;
     }

     if (status === 'passed') {
         return 'success';
     } else if (status === 'failed') {
         return 'danger';
     } else {
         return 'default';
     }
 });

 function getScenarioResult(steps) {
     if (!steps) {
         return 'pending';
     }
     var scenarioResult = 'passed';
     console.log('steps:' + steps);
     $.each(steps, function(sIndex, step) {
         if (step.result && step.result.status !== 'passed') {
             scenarioResult = step.result.status;
             return false;
         }
     });
     return scenarioResult;
 }

 function compare(a, b) {
     if (a.name < b.name) {
         return -1;
     }
     if (a.name > b.name) {
         return 1;
     }
     return 0;
 }

 function drawResults(features) {
     var template = $('#tpl').html();


     //Sortera features


     features.sort(compare);

     //Loop features
     var featureResult = '';
     console.log('features:' + features);
     $.each(features, function(fIndex, feature) {
         featureResult = 'passed';
         // Loop scenarios
         console.log('elements:' + feature.elements);
         $.each(feature.elements, function(eIndex, scenario) {
             var scenarioResult = getScenarioResult(scenario.steps);
             features[fIndex].elements[eIndex].status = scenarioResult;

             if (scenarioResult !== 'passed') {
                 featureResult = scenarioResult;
             }
         });

         features[fIndex].status = featureResult;
     });
     var stone = Handlebars.compile(template)(features);
     $('#right').append(stone);



     // Draw left
     var leftTemplate = $('#tplLeft').html();
     var leftRendered = Handlebars.compile(leftTemplate)(features);
     $('#left').append(leftRendered);


     $('img').click(function() {
         $(this).toggleClass('bigger');
     });

     $('.featureTitle').click(function() {
         var featureID = $(this).attr('featureID');
         var element = document.getElementById(featureID);
         element.scrollIntoView();
     });


     var totAntal = $('.panel-title').length;
     var totFailed = $('.panel-danger').length;
     var totSuccess = $('.panel-success').length;

     var percentSuccess = Math.round((totSuccess / totAntal) * 100);
     var precentFailed = 100 - percentSuccess;

     $('#left').prepend('<br>' +
         '<div class="progress">' +
         '<div class="progress-bar progress-bar-success" style="width: ' + percentSuccess + '%;">' +
         totSuccess +
         '</div>' +
         '<div class="progress-bar progress-bar-danger" style="width: ' + precentFailed + '%;">' +
         totFailed +
         '</div>' +
         '</div>');


 }




 window.onload = function() {
     if (accResults) {
         drawResults(accResults);
     } else {
         $.getJSON("acc_results.json?callback=?")
             .done(function(result) {
                 console.log("success");
                 console.log(result);
                 drawResults(result);

             })
             .fail(function(result) {
                 console.log("error");
                 drawResults(JSON.parse(result.responseText));
             });
     }

 };
