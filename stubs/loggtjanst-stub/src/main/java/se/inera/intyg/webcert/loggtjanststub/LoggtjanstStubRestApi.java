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

package se.inera.intyg.webcert.loggtjanststub;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import se.riv.ehr.log.v1.LogType;

/**
 * @author andreaskaltenbach
 */
public class LoggtjanstStubRestApi {

    @Autowired
    private CopyOnWriteArrayList<LogType> logEntries;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<LogType> getAllLogEntries() {
        return logEntries;
    }

    @DELETE
    public Response deleteMedarbetaruppdrag() {
        logEntries.clear();
        return Response.ok().build();
    }
}
