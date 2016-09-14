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

package se.inera.intyg.webcert.specifications.spec.util

class WsClientFixture extends se.inera.intyg.common.specifications.spec.util.WsClientFixture {

    private final static String LOGICAL_ADDRESS = "5565594230"

    public WsClientFixture() {
        super(LOGICAL_ADDRESS, System.getProperty("webcert.baseUrl"))
    }

    public WsClientFixture(String address) {
        super(address, System.getProperty("webcert.baseUrl"))
    }

}

