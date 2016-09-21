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

/*global testdata,intyg,logger,pages,Promise*/
'use strict';
var fkIntygPage = pages.intyg.fk['7263'].intyg;
var fkLusePage = pages.intyg.luse.intyg;

module.exports = {
    generateIntygByType: function(typ, id) {
        if (typ === 'Transportstyrelsens läkarintyg') {
            return testdata.ts.bas.getRandom(id);
        } else if (typ === 'Transportstyrelsens läkarintyg, diabetes') {
            return testdata.ts.diabetes.getRandom(id);
        } else if (typ === 'Läkarintyg FK 7263') {
            return testdata.fk['7263'].getRandom(id);
        } else if (typ === 'Läkarutlåtande för sjukersättning') {
            return testdata.fk.LUSE.getRandom(id);
        } else if (typ === 'Läkarintyg för sjukpenning utökat') {
            return testdata.fk.LISU.getRandom(id);
        }
    },
    fetchMessageIds: function(intygtyp) {
        console.log('Hämtar meddelande-id:n');

        var isSMIIntyg = this.isSMIIntyg(intygtyp);

        if (!intyg.messages) {
            intyg.messages = [];
        }
        var panels;

        if (isSMIIntyg) {
            panels = fkLusePage.qaPanels;
        } else {
            panels = fkIntygPage.qaPanels;
        }

        if (typeof panels === 'undefined') {
            return Promise.resolve('Inga frågor hittades');
        } else {
            var messageIdAttributes = panels.map(function(elm) {
                return elm.getAttribute('id');
            });

            return messageIdAttributes.then(function(attr) {
                for (var i = 0; i < attr.length; i++) {
                    var messageId;

                    if (isSMIIntyg) {
                        messageId = attr[i].replace('arende-unhandled-', ''); // arende-unhandled-4c78e939-e187-122b-ce86-66937dfbe012
                    } else {
                        messageId = attr[i].split('-')[1];
                    }
                    logger.info('Meddelande-id som finns på intyget: ' + messageId);
                    intyg.messages.push({
                        id: messageId
                    });
                }
            });
        }

    },
    stripTrailingSlash: function(str) {
        if (str.substr(-1) === '/') {
            return str.substr(0, str.length - 1);
        }
        return str;
    },
    getIntygElementRow: function(intygstyp, status, cb) {
        var qaTable = element(by.css('table.table-qa'));
        qaTable.all(by.cssContainingText('tr', status)).filter(function(elem, index) {
            return elem.all(by.css('td')).get(2).getText().then(function(text) {
                return (text === intygstyp);
            });
        }).then(function(filteredElements) {
            cb(filteredElements[0]);
        });
    },
    // whichSMIIntyg: function(intygsType) {
    //     var regex = /(Läkarintyg för|Läkarutlåtande för)/g;
    //     return (intygsType) ? (intygsType.match(regex) ? (intygsType === this.smiIntyg.LISU ? this.getSMIAbbrev(this.smiIntyg.LISU) : this.getSMIAbbrev(this.smiIntyg.LUSE)) : false) : false;
    // },
    getAbbrev: function(value) {
        for (var key in this.smiIntyg) {
            if (this.smiIntyg[key] === value) {
                return key.toString();
            }
        }
        return null;
    },
    smiIntyg: {
        'LISU': 'Läkarintyg för sjukpenning utökat',
        'LUSE': 'Läkarutlåtande för sjukersättning',
        'FK7263': 'Läkarintyg FK 7263'
    },
    isSMIIntyg: function(intygsType) {
        var regex = /(Läkarintyg för|Läkarutlåtande för)/g;
        return (intygsType) ? (intygsType.match(regex) ? true : false) : false;
    },
    subjectCodes: {
        'Komplettering': 'KOMPLT',
        'Paminnelse': 'PAMINN',
        'Arbetstidsförläggning': 'ARBTID',
        'Avstämningsmöte': 'AVSTMN',
        'Kontakt': 'KONTKT',
        'Övrigt': 'OVRIGT'
    },
    getSubjectFromCode: function(value) {
        for (var key in this.subjectCodes) {
            if (this.subjectCodes[key] === value) {
                return key.toString();
            }
        }
        return null;
    },
    randomTextString: function() {
        var text = '';
        var possible = 'ABCDEFGHIJKLMNOPQRSTUVWXYZÅÄÖabcdefghijklmnopqrstuvwxyzåäö0123456789';

        for (var i = 0; i < 16; i++) {
            text += possible.charAt(Math.floor(Math.random() * possible.length));
        }
        return text;
    },
    randomPageField: function(isSMIIntyg, intygAbbrev) {
        var index = Math.floor(Math.random() * 3);
        if (isSMIIntyg) {
            if (intygAbbrev === 'LISU') {
                return this.pageField[intygAbbrev][index];
            } else if (intygAbbrev === 'LUSE') {
                return this.pageField[intygAbbrev][index];
            }
        } else {
            return this.pageField.FK7263[index];
        }
    },
    pageField: {
        'LISU': ['aktivitetsbegransning', 'sysselsattning', 'funktionsnedsattning'],
        'LUSE': ['aktivitetsbegransning', 'sjukdomsforlopp', 'funktionsnedsattning'],
        'FK7263': ['diagnoskod', 'arbetsförmåga', 'sjukskrivningsperiod']
    },
    getUserObj: function(userKey) {
        return this.userObj[userKey];
    },
    userObj: {
        UserKey: {
            EN: 'EN',
            ÅS: 'ÅS'
        },
        Role: {
            DOCTOR: 'Läkare'
        },
        EN: {
            fornamn: 'Erik',
            efternamn: 'Nilsson',
            hsaId: 'TSTNMT2321000156-105H',
            enhetId: 'TSTNMT2321000156-105F'
        },
        ÅS: {
            fornamn: 'Åsa Svensson',
            efternamn: 'Nilsson',
            hsaId: 'TSTNMT2321000156-100L',
            enhetId: 'TSTNMT2321000156-1003'
        }
    },
    makuleraReason: [
        'dialogRadioFelaktigtIntyg',
        'dialogRadioPatientNyInfo',
        'dialogRadioMinBedomingAndrad',
        'dialogRadioOvrigt'
    ]

};
