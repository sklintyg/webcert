/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.auth.eleg;

import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * Simple auth token for fake logins for e-leg.
 *
 * Created by eriklupander on 2015-06-16.
 */
public class FakeElegAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = -2796850504529240890L;

    private FakeElegCredentials fakeElegCredentials;

    public FakeElegAuthenticationToken(FakeElegCredentials fakeElegCredentials) {
        super(null);
        this.fakeElegCredentials = fakeElegCredentials;
    }

    @Override
    public Object getCredentials() {
        return fakeElegCredentials;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }
}
