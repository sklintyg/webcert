/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

'use strict';
var testdataHelper = require('common-testtools').testdataHelper;
var shuffle = testdataHelper.shuffle;

module.exports = {
    ts: require('./testvalues.ts.js'),
    fk: require('./testvalues.fk.js'),
    patienter: [{
        id: '200001152388', // Boende utomlands
        kon: 'kvinna',
        namn: 'Anna Karin',
        efternamn: 'Levin',
        adress: {
            postadress: 'EU',
            postort: 'UK',
            postnummer: '12345'
        }
    }, {
        //Testfamilj
        //Är vårdnadshavare för barn 1 och barn 2.
        id: '198311209285',
        kon: 'kvinna',
        namn: 'Pernilla',
        efternamn: 'Backman',
        adress: {
            postadress: 'Testfamiljgatan 10',
            postort: 'Lönneberga',
            postnummer: '20000'
        }
    }, {
        //Är vårdnadshavare för barn 1.
        id: '199003122398',
        kon: 'man',
        namn: 'Sune',
        efternamn: 'Backman',
        adress: {
            postadress: 'Testfamiljgatan 10',
            postort: 'Lönneberga',
            postnummer: '20000'
        }
    }, {
        //Barn 1, Har mor och far där båda är vårdnadshavare.		
        id: '201602092387',
        kon: 'kvinna',
        namn: 'Malin',
        efternamn: 'Backman',
        adress: {
            postadress: 'Testfamiljgatan 10',
            postort: 'Lönneberga',
            postnummer: '20000'
        }
    }, {
        //Barn 2 Har mor och far men endast mor är vårdnadshavare.
        id: '201510262395',
        kon: 'man',
        namn: 'Erik',
        efternamn: 'Backman',
        adress: {
            postadress: 'Testfamiljgatan 10',
            postort: 'Lönneberga',
            postnummer: '20000'
        }
    }, {
        id: '190007179815', // Har adress i PU
        kon: 'man',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        }
    }, {
        id: '199004242385',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
        kon: 'kvinna'
    }, {
        id: '199004242393',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
        kon: 'man'
    }, {
        id: '199004252384',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
        kon: 'kvinna'
    }, {
        id: '199004252392',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
        kon: 'man'
    }, {
        id: '199004262383',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
        kon: 'kvinna'
    }, {
        id: '199004262391',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
        kon: 'man'
    }, {
        id: '199004272382',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
        kon: 'kvinna'
    }, {
        id: '199004272390',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
        kon: 'man'
    }, {
        id: '199004282381',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
        kon: 'kvinna'
    }, {
        id: '199004282399',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
        kon: 'man'
    }, {
        id: '199908102388',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
        kon: 'kvinna'
    }, {
        id: '199908112395',
        adress: {
            postadress: 'Norra storgatan 30',
            postort: 'Katthult',
            postnummer: '10000'
        },
        kon: 'man'
    }],
    dedikeradeTestPatienter: {
        medSyfte: function(syfte) {
            let patienter = {
                'avliden': [{
                    id: '190001309814',
                    namn: '',
                    efternamn: ''
                }],
                'Dödsorsaksintyg': [{
                    id: '199912212397',
                    adress: {
                        postadress: 'Norra storgatan 30',
                        postort: 'Katthult',
                        postnummer: '10000'
                    },
                    kon: 'man'
                }, {
                    id: '199912222388',
                    adress: {
                        postadress: 'Norra storgatan 30',
                        postort: 'Katthult',
                        postnummer: '10000'
                    },
                    kon: 'kvinna'
                }, {
                    id: '199912202380',
                    adress: {
                        postadress: 'Norra storgatan 30',
                        postort: 'Katthult',
                        postnummer: '10000'
                    },
                    kon: 'kvinna'
                }, {
                    id: '199912142388',
                    adress: {
                        postadress: 'Norra storgatan 30',
                        postort: 'Katthult',
                        postnummer: '10000'
                    },
                    kon: 'kvinna'
                }, {
                    id: '199912152395',
                    adress: {
                        postadress: 'Norra storgatan 30',
                        postort: 'Katthult',
                        postnummer: '10000'
                    },
                    kon: 'man'
                }],
                'Dödsbevis': [{
                    id: '199912182384',
                    adress: {
                        postadress: 'Norra storgatan 30',
                        postort: 'Katthult',
                        postnummer: '10000'
                    },
                    kon: 'kvinna'
                }, {
                    id: '199912172393',
                    adress: {
                        postadress: 'Norra storgatan 30',
                        postort: 'Katthult',
                        postnummer: '10000'
                    },
                    kon: 'man'
                }, {
                    id: '199912232395',
                    adress: {
                        postadress: 'Norra storgatan 30',
                        postort: 'Katthult',
                        postnummer: '10000'
                    },
                    kon: 'man'
                }, {
                    id: '199912252393',
                    adress: {
                        postadress: 'Norra storgatan 30',
                        postort: 'Katthult',
                        postnummer: '10000'
                    },
                    kon: 'man'
                }, {
                    id: '190001289818',
                    adress: {
                        postadress: 'Norra storgatan 30',
                        postort: 'Katthult',
                        postnummer: '10000'
                    },
                    kon: 'man'
                }],
                'intygsstatistik': [],
                'rehabstöd': [],
                'sekretessmarkering': [{
                    id: '199912192391',
                    namn: '',
                    efternamn: '',
                    adress: {
                        postadress: 'Norra storgatan 30',
                        postort: 'Katthult',
                        postnummer: '10000'
                    },
                    kon: 'man'
                }],
                'fältvalidering': [{
                    id: '199912162386',
                    adress: {
                        postadress: 'Norra storgatan 30',
                        postort: 'Katthult',
                        postnummer: '10000'
                    },
                    kon: 'kvinna'
                }]
            };
            return patienter[syfte];
        }
    },
    patienterMedSamordningsnummer: [{
        id: '194401786530',
        namn: '',
        efternamn: ''
    }],
    patienterMedSamordningsnummerEjPU: [{
        id: '200402712384',
        namn: '',
        efternamn: ''
    }],

    patienterMedSekretessmarkering: [{
        id: '199912192391',
        namn: '',
        efternamn: ''
    }],
    patienterAvlidna: [{
        id: '190001309814',
        namn: '',
        efternamn: ''
    }],
    patienterEjPU: [{
        id: '201212121212',
        namn: '',
        efternamn: ''
    }],

    enhetsAdress: function() {
        return {
            gata: shuffle(['Bryggaregatan 1', 'Svampstigen 2'])[0],
            postnummer: shuffle(['655 91', '655 90'])[0],
            postadress: shuffle(['Karlstad', 'Säffle'])[0],
            telefon: shuffle(['0705121314', '054121314'][0])


        };
    }


};
