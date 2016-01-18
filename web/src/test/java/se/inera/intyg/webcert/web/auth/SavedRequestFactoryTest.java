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

package se.inera.intyg.webcert.web.auth;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.web.savedrequest.SavedRequest;

/**
 * Tests that the {@link SavedRequestFactoryImpl} produces correct SavedRequest instances.
 *
 * (The test actually tests the {@link org.springframework.security.web.savedrequest.DefaultSavedRequest} constructor
 * more than anything else)
 *
 * Created by eriklupander on 2015-10-14.
 */
@RunWith(MockitoJUnitRunner.class)
public class SavedRequestFactoryTest {

    public static final int PORT = 8888;
    public static final String SCHEME = "http";
    public static final String SERVER = "myserver.com";
    public static final String MY_PATH_SEGMENTS = "/my/path/segments";

    private SavedRequestFactoryImpl testee = new SavedRequestFactoryImpl();

    @Mock
    private HttpServletRequest req;

    @Test
    public void testCanProduceSavedRequest() {
        StringBuffer sb = new StringBuffer();
        sb.append(MY_PATH_SEGMENTS);

        // Just mock the bare minimum to produce a valid SavedRequest without having exceptions thrown...
        when(req.getHeaderNames()).thenReturn(new Vector().elements());
        when(req.getLocales()).thenReturn(new Vector().elements());
        when(req.getScheme()).thenReturn(SCHEME);
        when(req.getServerName()).thenReturn(SERVER);
        when(req.getServerPort()).thenReturn(PORT);

        when(req.getRequestURL()).thenReturn(sb);
        when(req.getRequestURI()).thenReturn(sb.toString());

        SavedRequest savedRequest = testee.buildSavedRequest(req);
        assertEquals(req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + req.getRequestURI(), savedRequest.getRedirectUrl());
    }

}
