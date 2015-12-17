/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

module.exports = {
    options: {
        ignorePath : [
            'src/main/webapp/',
            '../../common/web/src/main/resources/META-INF/resources/',
            '../../intygstyper/fk7263/src/main/resources/META-INF/resources/'],
        addRootSlash : false
    },
    local_dependencies: {
        files: {
            'src/main/webapp/index.html': [
                'src/main/webapp/js/**/*.js', '!src/main/webapp/js/**/*.min.js',
                'src/main/webapp/vendor/**/*.js', '!src/main/webapp/vendor/**/*.min.js',
                global.paths.common + '/css/**/*.css',
                global.paths.common + '/webcert/css/**/*.css',
                global.paths.common + '/webcert/js/**/*.js',
                global.paths.fk7263 + '/js/**/*.js',
            ]
        }
    }
}
