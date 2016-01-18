/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

/* globals pages, protractor*/
/* globals browser, intyg, scenario, logg */

'use strict';


var helpers = require('./helpers.js');
var tsDiabIntyg = pages.intyg.ts.diabetes.intyg;


function boolTillJaNej(val){
	if(val){
		return 'Ja';
	}
	else{
		return 'Nej';
	}
}

module.exports ={
	checkTsDiabetesValues:function(intyg, callback){
        var selectedTypes = intyg.korkortstyper.sort(function (a, b) {
        var allTypes = ['AM', 'A1', 'A2', 'A', 'B', 'BE', 'TRAKTOR', 'C1', 'C1E', 'C', 'CE', 'D1', 'D1E', 'D', 'DE', 'TAXI'];
        return allTypes.indexOf(a.toUpperCase()) - allTypes.indexOf(b.toUpperCase());
        });

        selectedTypes = selectedTypes.join(', ').toUpperCase();

        if (intyg.allmant.year !== null){
        expect(tsDiabIntyg.period.getText()).to.eventually.equal(intyg.allmant.year.toString()).then(function(value) {
            logg('OK - Observationsperiod = '+ value);
            }, function(reason) {
                callback('FEL - Observationsperiod: '+ reason);
            });
        }

        if (typeof intyg.allmant.behandling.insulinYear !== 'undefined'){
        expect(tsDiabIntyg.insulPeriod.getText()).to.eventually.equal(intyg.allmant.behandling.insulinYear.toString()).then(function(value) {
            logg('OK - Insulin behandlings period = '+ value);
            }, function(reason) {
                callback('FEL - Insulin behandlings period: '+ reason);
            });
        }

        expect(tsDiabIntyg.dTyp.getText()).to.eventually.equal(intyg.allmant.typ).then(function(value) {
            logg('OK - Insulin behandlings period = '+ value);
            }, function(reason) {
                callback('FEL - Insulin behandlings period: '+ reason);
            });

        expect(tsDiabIntyg.kunskapOmAtgarder.getText()).to.eventually.equal(intyg.hypoglykemier.a).then(function(value) {
            logg('OK - Kunskap om åtgarder = '+ value);
            }, function(reason) {
                callback('FEL - Kunskap om åtgarder: '+ reason);
            }).then(callback);

        expect(tsDiabIntyg.teckenNedsattHjarnfunktion.getText()).to.eventually.equal(intyg.hypoglykemier.b).then(function(value) {
            logg('OK - Tecken nedsatt hjärnfunktion = '+ value);
            }, function(reason) {
                callback('FEL - Tecken nedsatt hjärnfunktion: '+ reason);
            });
        
        expect(tsDiabIntyg.saknarFormagaKannaVarningstecken.getText()).to.eventually.equal(intyg.hypoglykemier.c).then(function(value) {
            logg('OK - Insulin behandlings period = '+ value);
            }, function(reason) {
                callback('FEL - Insulin behandlings period: '+ reason);
            });
        
        expect(tsDiabIntyg.allvarligForekomst.getText()).to.eventually.equal(intyg.hypoglykemier.d).then(function(value) {
            logg('OK - Saknar förmåga känna varningstecken = '+ value);
            }, function(reason) {
                callback('FEL - Saknar förmåga känna varningstecken: '+ reason);
            });
        
        expect(tsDiabIntyg.allvarligForekomstTrafiken.getText()).to.eventually.equal(intyg.hypoglykemier.e).then(function(value) {
            logg('OK - Allvarlig förekomst trafiken = '+ value);
            }, function(reason) {
                callback('FEL - Allvarlig förekomst trafiken: '+ reason);
            });
        
        expect(tsDiabIntyg.egenkontrollBlodsocker.getText()).to.eventually.equal(intyg.hypoglykemier.f).then(function(value) {
            logg('OK - Insulin behandlings period = '+ value);
            }, function(reason) {
                callback('FEL - Insulin behandlings period: '+ reason);
            });
        
        expect(tsDiabIntyg.allvarligForekomstVakenTid.getText()).to.eventually.equal(intyg.hypoglykemier.g).then(function(value) {
            logg('OK - Allvarlig förekomst vaken tid = '+ value);
            }, function(reason) {
                callback('FEL - Allvarlig förekomst vaken tid: '+ reason);
            });

        if (intyg.syn === 'Ja') {
            expect(tsDiabIntyg.synIntyg.getText()).to.eventually.equal(intyg.syn).then(function(value) {
                logg('OK - Synintyg = '+ value);
                }, function(reason) {
                    callback('FEL - Synintyg: '+ reason);
                });
        }

        // ============= PLACEHOLDERS:
        expect(tsDiabIntyg.komment.getText()).to.eventually.equal('Ej angivet').then(function(value) {
            logg('OK - Kommentar = '+ value);
            }, function(reason) {
                callback('FEL - Kommentar: '+ reason);
            });

        expect(tsDiabIntyg.specKomp.getText()).to.eventually.equal('Ej angivet').then(function(value) {
            logg('OK - Läkare Special kompetens = '+ value);
            }, function(reason) {
                callback('FEL - Läkare Special kompetens: '+ reason);
            });
        // ==============

        intyg.allmant.behandling.typer.forEach(function(typ) {
            if(typ === 'Endast kost')
            {
                expect(tsDiabIntyg.eKost.getText()).to.eventually.equal('Ja').then(function(value) {
                    logg('OK - '+typ+' = '+ value);
                    }, function(reason) {
                        callback('FEL - '+typ+' : '+ reason);
                    });
            }
            else if(typ === 'Tabletter')
            {
                expect(tsDiabIntyg.tabl.getText()).to.eventually.equal('Ja').then(function(value) {
                    logg('OK - '+typ+' = '+ value);
                    }, function(reason) {
                        callback('FEL - '+typ+' : '+ reason);
                    });
            }
            else if(typ === 'Insulin')
            {
                expect(tsDiabIntyg.insul.getText()).to.eventually.equal('Ja').then(function(value) {
                    logg('OK - '+typ+' = '+ value);
                    }, function(reason) {
                        callback('FEL - '+typ+' : '+ reason);
                    });
            }
        });

        expect(tsDiabIntyg.bed.getText()).to.eventually.contain(selectedTypes).then(function(value) {
            logg('OK - Bedömningen avser körkortstyper = '+ value);
            }, function(reason) {
                callback('FEL - Bedömningen avser körkortstyper: '+ reason);
            }).then(callback);
    }
};
