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

/**
 * Created by BESA on 2015-11-17.
 * Holds paths to page files for easy inclusion and intellisense support in specs.
 */
'use strict';

var intygPath = './intyg/'; // should point to intyg folder
var WebcertBasePage = require('./webcert.base.page.js');
var BaseUtkastPage = require(intygPath + 'base.utkast.page.js');
var BaseIntygPage = require(intygPath + 'base.intyg.page.js');

module.exports = {
    'webcertBase': new WebcertBasePage(),
    'welcome': require('./welcome.page.js'),
    'sokSkrivIntyg': {
        'pickPatient': require('./sokSkrivIntyg/sokSkrivIntyg.page.js'),
        'valjUtkastType': require('./sokSkrivIntyg/sokSkrivValjUtkastType.page.js'),
        'visaIntyg': require('./sokSkrivIntyg/sokSkrivValjIntyg.page.js')
    },
    fragorOchSvar: require('./fragorOchSvar.js'), //Ärendehanteringsidan
    intyg: {
        hogerfaltet: require(intygPath + 'hogerfaltet.js'),
        fk: {
            '7263': {
                utkast: require(intygPath + 'fk7263/fk.7263.utkast.page.js'),
                intyg: require(intygPath + 'fk7263/fk.7263.intyg.page.js')
            }
        },
        base: {
            intyg: new BaseIntygPage(),
            utkast: new BaseUtkastPage()
        },
        luse: {
            utkast: require(intygPath + 'fk/smi/luse/luse.utkast.page.js'),
            intyg: require(intygPath + 'fk/smi/luse/luse.intyg.page.js')
        },
        lisjp: {
            utkast: require(intygPath + 'fk/smi/lisjp/lisjp.utkast.page.js'),
            intyg: require(intygPath + 'fk/smi/lisjp/lisjp.intyg.page.js')
        },
        luaeFS: {
            utkast: require(intygPath + 'fk/smi/luae_fs/luae_fs.utkast.page.js'),
            intyg: require(intygPath + 'fk/smi/luae_fs/luae_fs.intyg.page.js')
        },
        luaeNA: {
            utkast: require(intygPath + 'fk/smi/luae_na/luae_na.utkast.page.js'),
            intyg: require(intygPath + 'fk/smi/luae_na/luae_na.intyg.page.js')
        },
        ts: {
            diabetes: {
                utkast: require(intygPath + 'ts/ts_diabetes/tsDiabetes.utkast.page.js'),
                intyg: require(intygPath + 'ts/ts_diabetes/tsDiabetes.intyg.page.js')
            },
            bas: {
                utkast: require(intygPath + 'ts/ts_bas/tsBas.utkast.page.js'),
                intyg: require(intygPath + 'ts/ts_bas/tsBas.intyg.page.js')
            }
        },
        skv: {
            db: {
                utkast: require(intygPath + 'skv/db/db.utkast.page.js'),
                intyg: require(intygPath + 'skv/db/db.intyg.page.js')
            }
        },
        soc: {
            doi: {
                utkast: require(intygPath + 'soc/doi/doi.utkast.page.js'),
                intyg: require(intygPath + 'soc/doi/doi.intyg.page.js')
            }
        }
    },
    'unsignedPage': require('./unsignedPage.js'),


    getIntygPageByType: function(typ) {
        switch (typ) {
            case 'Transportstyrelsens läkarintyg':
                return this.intyg.ts.bas.intyg;
            case 'Transportstyrelsens läkarintyg, diabetes':
                return this.intyg.ts.diabetes.intyg;
            case 'Läkarintyg FK 7263':
                return this.intyg.fk['7263'].intyg;
            case 'Läkarutlåtande för sjukersättning':
                return this.intyg.luse.intyg;
            case 'Läkarintyg för sjukpenning':
                return this.intyg.lisjp.intyg;
            case 'Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga':
                return this.intyg.luaeNA.intyg;
            case 'Läkarutlåtande för aktivitetsersättning vid förlängd skolgång':
                return this.intyg.luaeFS.intyg;
            case 'Dödsbevis':
                return this.intyg.skv.db.intyg;
            case 'Dödsorsaksintyg':
                return this.intyg.soc.doi.intyg;
            default:
                throw 'Intyg-typ odefinierad.';
        }
    },
    getUtkastPageByType: function(typ) {
        switch (typ) {
            case 'Transportstyrelsens läkarintyg':
                return this.intyg.ts.bas.utkast;
            case 'Transportstyrelsens läkarintyg, diabetes':
                return this.intyg.ts.diabetes.utkast;
            case 'Läkarintyg FK 7263':
                return this.intyg.fk['7263'].utkast;
            case 'Läkarutlåtande för sjukersättning':
                return this.intyg.luse.utkast;
            case 'Läkarintyg för sjukpenning':
                return this.intyg.lisjp.utkast;
            case 'Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga':
                return this.intyg.luaeNA.utkast;
            case 'Läkarutlåtande för aktivitetsersättning vid förlängd skolgång':
                return this.intyg.luaeFS.utkast;
            case 'Dödsbevis':
                return this.intyg.skv.db.utkast;
            case 'Dödsorsaksintyg':
                return this.intyg.soc.doi.utkast;
            default:
                throw 'Intyg-typ odefinierad.';
        }
    }
};
