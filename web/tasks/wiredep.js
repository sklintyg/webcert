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

module.exports = {
		  target: {
			// Point to the files that should be updated when
		    // you run `grunt wiredep`
		    src: [
		      'src/main/webapp/index.html'
		    ],
            ignorePath: '../../../',
            fileTypes: {

                // defaults:
                html: {
                  block: /(([ \t]*)<!--\s*bower:*(\S*)\s*-->)(\n|\r|.)*?(<!--\s*endbower\s*-->)/gi,
                  detect: {
                    js: /<script.*src=['"]([^'"]+)/gi,
                    css: /<link.*href=['"]([^'"]+)/gi
                  },
                  replace: {
                    js: '<script src="{{filePath}}"></script>',
                    css: '<link rel="stylesheet" href="{{filePath}}" />'
                  }
                },
                jsp: {
                  block: /(([ \t]*)<!--\s*bower:*(\S*)\s*-->)(\n|\r|.)*?(<!--\s*endbower\s*-->)/gi,
                  detect: {
                    js: /<script.*src=['"]([^'"]+)/gi,
                    css: /<link.*href=['"]([^'"]+)/gi
                  },
                  replace: {
                    js: '<script src="{{filePath}}"></script>',
                    css: '<link rel="stylesheet" href="{{filePath}}" />'
                  }
                }
            }
		  }
}
