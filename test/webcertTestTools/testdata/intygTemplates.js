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

/**
 * Created by BESA on 2015-11-17.
 */

var intygTemplates = {
    'fkMax': {
        personnr : '19121212-1212',
        patientNamn : 'Tolvan Tolvansson',
        //issuerId : '',
        issuer : 'IFV1239877878-104B',
        issued : '2013-04-01',
        validFrom : '2013-04-01',
        validTo : '2013-04-11',
        enhetId : 'IFV1239877878-1042',
        vardgivarId : 'IFV1239877878-1041',
        intygType : 'fk7263',
        intygId : 'fk-ptor-max',
        sent : false,
        revoked : false
    }
};

module.exports = intygTemplates;
