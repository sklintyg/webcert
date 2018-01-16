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

describe('Siths.jsp', function() {
    'use strict';

    // Synced with siths.jsp to be able to test it.
    function getSerialFromSubject(subject) {

        // Find where the serialnumber starts and remove everything before
        var subjectSerial = subject.substring(subject.indexOf('2.5.4.5=') + 8);

        // Find where the serialnumber ends and remove everything after
        var subjectSerialEndIndex = subjectSerial.indexOf(',');
        if (subjectSerialEndIndex === -1) {
            // There are no more commas in the string, assume serial runs to the end of it
            subjectSerial = subjectSerial.substring(0);
        } else {
            // There are more commas, cut the serial from the '=' to the next ','
            subjectSerial = subjectSerial.substring(0, subjectSerialEndIndex);
        }

        return subjectSerial;
    }

    describe('getHcc', function() {
        it('should be able to extract serial when it is at the end of the string', function() {
            var subject = '2.5.4.6=SE, 2.5.4.7=Kronobergs län, 2.5.4.10=Landstinget Kronoberg, 2.5.4.3=Medcert Vårdtjänst, 2.5.4.4=Vårdtjänst, 2.5.4.5=SE2321000065-TEST0019';
            var serial = getSerialFromSubject(subject);
            expect(serial).toBe('SE2321000065-TEST0019');
        });

        it('should be able to extract serial when it is at NOT the end of the string', function() {
            var subject = '2.5.4.6=SE, 2.5.4.7=Kronobergs län, 2.5.4.10=Landstinget Kronoberg, 2.5.4.3=Medcert Vårdtjänst, 2.5.4.5=SE2321000065-TEST0019, 2.5.4.4=Vårdtjänst';
            var serial = getSerialFromSubject(subject);
            expect(serial).toBe('SE2321000065-TEST0019');
        });
    });

});
