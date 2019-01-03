/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

/* globals logger, pages, Promise */

'use strict';
var tsDiabIntyg = pages.intyg.ts.diabetes.v2.intyg;

function ejAngivetIfUndef(obj) {
    if (obj === null || obj === undefined) {
        return 'Ej angivet';

    }
    return obj.toString();
}

function checkAllmant(allmant) {
    var promiseArr = [];

    promiseArr.push(expect(tsDiabIntyg.period.getText()).to.eventually.equal(allmant.year.toString()).then(function(value) {
        logger.info('OK - Observationsperiod = ' + value);
    }, function(reason) {
        throw ('FEL - Observationsperiod: ' + reason);
    }));
    if (allmant.behandling.insulinYear) {
        promiseArr.push(expect(tsDiabIntyg.insulPeriod.getText()).to.eventually.equal(ejAngivetIfUndef(allmant.behandling.insulinYear)).then(function(value) {
            logger.info('OK - Insulin behandlings period = ' + value);
        }, function(reason) {
            throw ('FEL - Insulin behandlings period: ' + reason);
        }));
    }

    promiseArr.push(expect(tsDiabIntyg.dTyp.getText()).to.eventually.equal(allmant.typ).then(function(value) {
        logger.info('OK - diagnostyp = ' + value);
    }, function(reason) {
        throw ('FEL - diagnostyp: ' + reason);
    }));
    promiseArr.push(expect(tsDiabIntyg.falt1.annanBehandling.getText()).to.eventually.equal(allmant.annanbehandling).then(function(value) {
        logger.info('OK - annan behandling = ' + value);
    }, function(reason) {
        throw ('FEL - annan behandling: ' + reason);
    }));
    return Promise.all(promiseArr);
}

function checkHypoglykemier(hypo) {
    return Promise.all([
        expect(tsDiabIntyg.kunskapOmAtgarder.getText()).to.eventually.equal(ejAngivetIfUndef(hypo.a)).then(function(value) {
            logger.info('OK -(Hypo A) Kunskap om åtgarder = ' + value);
        }, function(reason) {
            throw ('FEL -(Hypo A) Kunskap om åtgarder: ' + reason);
        }),

        expect(tsDiabIntyg.teckenNedsattHjarnfunktion.getText()).to.eventually.equal(ejAngivetIfUndef(hypo.b)).then(function(value) {
            logger.info('OK -(Hypo B) Tecken nedsatt hjärnfunktion = ' + value);
        }, function(reason) {
            throw ('FEL -(Hypo B) Tecken nedsatt hjärnfunktion: ' + reason);
        }),

        expect(tsDiabIntyg.saknarFormagaKannaVarningstecken.getText()).to.eventually.equal(ejAngivetIfUndef(hypo.c)).then(function(value) {
            logger.info('OK -(Hypo C) saknarFormagaKannaVarningstecken = ' + value);
        }, function(reason) {
            throw ('FEL -(Hypo C) saknarFormagaKannaVarningstecken: ' + reason);
        }),

        expect(tsDiabIntyg.allvarligForekomst.getText()).to.eventually.equal(ejAngivetIfUndef(ejAngivetIfUndef(hypo.d))).then(function(value) {
            logger.info('OK -(Hypo D) ' + value);
            if (value === 'Ja') {
                var text = tsDiabIntyg.allvarligForekomstBeskrivning.getText();
                return expect(text).to.eventually.equal(hypo.dAntalEpisoder).then(function(val) {
                    logger.info('OK -(Hypo D antal episoder) Allvarlig förekomst = ' + val);
                }, function(orsak) {
                    throw ('Fel -(Hypo D antal episoder) Allvarlig förekomst = ' + orsak);
                });

            }
        }, function(reason) {
            throw ('FEL -(Hypo D) allvarligForekomst: ' + reason);
        }),

        expect(tsDiabIntyg.allvarligForekomstTrafiken.getText()).to.eventually.equal(ejAngivetIfUndef(hypo.e)).then(function(value) {
            logger.info('OK -(Hypo E) Allvarlig förekomst trafiken = ' + value);
            if (value === 'Ja') {
                var text = tsDiabIntyg.allvarligForekomstTrafikenBeskrivning.getText();
                return expect(text).to.eventually.equal(hypo.eAntalEpisoder).then(function(val) {
                    logger.info('OK -(Hypo E antal episoder) Allvarlig förekomst trafiken = ' + val);
                }, function(orsak) {
                    throw ('Fel -(Hypo E antal episoder) Allvarlig förekomst trafiken= ' + orsak);
                });

            }
        }, function(reason) {
            throw ('FEL -(Hypo E) Allvarlig förekomst trafiken: ' + reason);
        }),

        expect(tsDiabIntyg.egenkontrollBlodsocker.getText()).to.eventually.equal(ejAngivetIfUndef(hypo.f)).then(function(value) {
            logger.info('OK -(Hypo F) egenkontrollBlodsocker = ' + value);
        }, function(reason) {
            throw ('FEL -(Hypo F) egenkontrollBlodsocker: ' + reason);
        }),

        expect(tsDiabIntyg.allvarligForekomstVakenTid.getText()).to.eventually.equal(ejAngivetIfUndef(hypo.g)).then(function(value) {
            logger.info('OK -(Hypo G) Allvarlig förekomst vaken tid = ' + value);
            if (value === 'Ja') {
                var text = tsDiabIntyg.vakenTidObservationsTid.getText();
                return expect(text).to.eventually.equal(hypo.gDatum).then(function(val) {
                    logger.info('OK -(Hypo G datum) Allvarlig förekomst vaken tid observationstid = ' + val);
                }, function(orsak) {
                    throw ('Fel -(Hypo G datum) Allvarlig förekomst vaken tid observationstid = ' + orsak);
                });
            }

        }, function(reason) {
            throw ('FEL -(Hypo G) Allvarlig förekomst vaken tid: ' + reason);
        })

    ]);

}

module.exports = {
    checkValues: function(intyg) {
        logger.info('-- Kontrollerar Transportstyrelsens läkarintyg diabetes --');
        var promiseArr = [];

        var selectedTypes = intyg.korkortstyper.sort(function(a, b) {
            var allTypes = ['AM', 'A1', 'A2', 'A', 'B', 'BE', 'Traktor', 'C1', 'C1E', 'C', 'CE', 'D1', 'D1E', 'D', 'DE', 'Taxi'];
            return allTypes.indexOf(a) - allTypes.indexOf(b);
        });

        selectedTypes = selectedTypes.join(', ');
        promiseArr.push(checkAllmant(intyg.allmant));
        promiseArr.push(checkHypoglykemier(intyg.hypoglykemier));

        if (intyg.syn === 'Ja') {
            promiseArr.push(expect(tsDiabIntyg.synIntyg.getText()).to.eventually.equal(intyg.syn).then(function(value) {
                logger.info('OK - Synintyg = ' + value);
            }, function(reason) {
                throw ('FEL - Synintyg: ' + reason);
            }));
        }

        // ============= PLACEHOLDERS:
        promiseArr.push(expect(tsDiabIntyg.kommentar.getText()).to.eventually.equal('Ej angivet').then(function(value) {
            logger.info('OK - Kommentar = ' + value);
        }, function(reason) {
            throw ('FEL - Kommentar: ' + reason);
        }));

        promiseArr.push(expect(tsDiabIntyg.specKomp.getText()).to.eventually.equal('Ej angivet').then(function(value) {
            logger.info('OK - Läkare Special kompetens = ' + value);
        }, function(reason) {
            throw ('FEL - Läkare Special kompetens: ' + reason);
        }));
        // ==============

        intyg.allmant.behandling.typer.forEach(function(typ, index) {
            logger.silly('Behandling: ' + typ);
            if (typ === 'Endast kost') {
                promiseArr.push(expect(tsDiabIntyg.getBehandlingsTyp(index).getText()).to.eventually.equal('Endast kost').then(function(value) {
                    logger.info('OK - ' + typ + ' = ' + value);
                }, function(reason) {
                    throw ('FEL - ' + typ + ' : ' + reason);
                }));
            } else if (typ === 'Tabletter') {
                promiseArr.push(expect(tsDiabIntyg.getBehandlingsTyp(index).getText()).to.eventually.equal('Tabletter').then(function(value) {
                    logger.info('OK - ' + typ + ' = ' + value);
                }, function(reason) {
                    throw ('FEL - ' + typ + ' : ' + reason);
                }));
            } else if (typ === 'Insulin') {
                promiseArr.push(expect(tsDiabIntyg.getBehandlingsTyp(index).getText()).to.eventually.equal('Insulin').then(function(value) {
                    logger.info('OK - ' + typ + ' = ' + value);
                }, function(reason) {
                    throw ('FEL - ' + typ + ' : ' + reason);
                }));
            }
        });


        return Promise.all(promiseArr);
    }
};
