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
angular.module('webcertTest').value('mockResponse', {
    utkastList: {
        'totalCount':1,
        'results':[
            {'intygId':'2885f159-51e7-41bc-aba0-88fb02b2d667','patientId':'19121212-1212','source':'WC',
                'intygType':'fk7263','status':'DRAFT_COMPLETE','lastUpdatedSigned':'2015-01-28T13:47:22.071',
                'updatedSignedBy':'Jan Nilsson','vidarebefordrad':false}
        ]
    },
    utkast: {
        'intygId': '6c10506a-1ef1-4ce2-a4c8-a82fa43a1e26',
        'intygType': 'fk7263',
        'patientNamn': 'Tolvan Tolvansson',
        'signeringsDatum': '2015-01-27T16:01:10.000',
        'patientId': {
            'patientIdRoot': '1.2.752.129.2.1.3.1', 'patientIdExtension': '19121212-1212'}
    }
});
