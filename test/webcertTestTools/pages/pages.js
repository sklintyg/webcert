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

/**
 * Created by BESA on 2015-11-17.
 * Holds paths to page files for easy inclusion and intellisense support in specs.
 */
'use strict';

var intygPath = './intyg/'; // should point to intyg folder
var WebcertBasePage = require('./webcert.base.page.js');
var BaseUtkastPage = require(intygPath + 'base.utkast.page.js');
var BaseIntygPage = require(intygPath + 'base.intyg.page.js');
var baseSMIIntyg = require(intygPath + 'fk/smi/smi.base.intyg.page.js');
var baseSMIUtkast = require(intygPath + 'fk/smi/smi.base.utkast.page.js');


module.exports = {
    'webcertBase': new WebcertBasePage(),
    'landing': require('./landing.page.js'),
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
        smi: {
            utkast: baseSMIUtkast(),
            intyg: new baseSMIIntyg()
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
                v2: {
                    utkast: require(intygPath + 'ts/ts_diabetes/v2/tsDiabetes.utkast.page.js'),
                    intyg: require(intygPath + 'ts/ts_diabetes/v2/tsDiabetes.intyg.page.js')
                },
                v3: {
                    utkast: require(intygPath + 'ts/ts_diabetes/v3/tsDiabetes.utkast.page.js'),
                    intyg: require(intygPath + 'ts/ts_diabetes/v3/tsDiabetes.intyg.page.js')
                }
            },
            bas: {
                utkast: require(intygPath + 'ts/ts_bas/tsBas.utkast.page.js'),
                intyg: require(intygPath + 'ts/ts_bas/tsBas.intyg.page.js')
            },
            trk1009: {
                utkast: require(intygPath + 'ts/tstrk1009/tstrk1009.utkast.page.js'),
                intyg: require(intygPath + 'ts/tstrk1009/tstrk1009.intyg.page.js')
            },
            trk1062: {
                utkast: require(intygPath + 'ts/tstrk1062/tstrk1062.utkast.page.js'),
                intyg: require(intygPath + 'ts/tstrk1062/tstrk1062.intyg.page.js')
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
        },
        af: {
            af00213: {
                utkast: require(intygPath + 'af/af00213/af00213.utkast.page.js'),
                intyg: require(intygPath + 'af/af00213/af00213.intyg.page.js')
            },
            af00251: {
                utkast: require(intygPath + 'af/af00251/af00251.utkast.page.js'),
                intyg: require(intygPath + 'af/af00251/af00251.intyg.page.js')
            }
        },
        ag: {
            ag114: {
                utkast: require(intygPath + 'ag/ag114/ag114.utkast.page.js'),
                intyg: require(intygPath + 'ag/ag114/ag114.intyg.page.js')
            },
            ag7804: {
                utkast: require(intygPath + 'ag/ag7804/ag7804.utkast.page.js'),
                intyg: require(intygPath + 'ag/ag7804/ag7804.intyg.page.js')
            }
        }
    },
    'unsignedPage': require('./unsignedPage.js'),


    getIntygPageByType: function(typ) {
        switch (typ) {
            case 'Transportstyrelsens läkarintyg högre körkortsbehörighet':
                return this.intyg.ts.bas.intyg;
            case 'Transportstyrelsens läkarintyg diabetes':
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
            case 'Arbetsförmedlingens medicinska utlåtande':
                return this.intyg.af.af00213.intyg;
            case 'Läkarintyg för deltagare i arbetsmarknadspolitiska program':
                return this.intyg.af.af00251.intyg;
            default:
                console.trace(typ);
                throw 'Intyg-typ ' + typ + 'hittades inte i pages i getIntygPageByType.';
        }
    },
    getUtkastPageByType: function(typ) {
        switch (typ) {
            case 'Transportstyrelsens läkarintyg högre körkortsbehörighet':
                return this.intyg.ts.bas.utkast;
            case 'Transportstyrelsens läkarintyg diabetes':
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
            case 'Arbetsförmedlingens medicinska utlåtande':
                return this.intyg.af.af00213.utkast;
            case 'Läkarintyg för deltagare i arbetsmarknadspolitiska program':
                return this.intyg.af.af00251.utkast;
            default:
                console.trace(typ);
                throw 'Intyg-typ ' + typ + 'hittades inte i pages i getUtkastPageByType.';
        }
    }
};
