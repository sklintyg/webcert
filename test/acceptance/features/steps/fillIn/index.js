/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

/*global browser*/
'use strict';

module.exports = {
  fillIn: function(world) {
    return (() => {
      switch (world.intyg.typ) {
      case 'Transportstyrelsens läkarintyg högre körkortsbehörighet':
        return require('./ts.bas.js').fillIn(world.intyg);
      case 'Transportstyrelsens läkarintyg diabetes':
        //return require('./ts.diabetes.js').fillIn(world.intyg);

        //TODO: V2 och V3
        return require('./ts.diabetes.v3.js').fillIn(world.intyg);
      case 'Läkarintyg FK 7263':
        return require('./fk.7263.js').fillIn(world.intyg);
      case 'Läkarutlåtande för sjukersättning':
        return require('./fk.LUSE.js').fillIn(world.intyg);
      case 'Läkarintyg för sjukpenning':
        return require('./fk.LISJP.js').fillIn(world.intyg);
      case 'Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga':
        return require('./fk.LUAE_NA.js').fillIn(world.intyg);
      case 'Läkarutlåtande för aktivitetsersättning vid förlängd skolgång':
        return require('./fk.LUAE_FS.js').fillIn(world.intyg);
      case 'Dödsbevis':
        return require('./skv.db.js').fillIn(world.intyg);
      case 'Dödsorsaksintyg':
        return require('./soc.doi.js').fillIn(world.intyg);
      case 'Arbetsförmedlingens medicinska utlåtande':
        return require('./af.af00213.js').fillIn(world.intyg);
      default:
        throw 'Intyg.typ odefinierad.';
      }
    })().then(() => require('./common.js').fillIn(world)
    .then(() => browser.ignoreSynchronization = false));
  }
};
