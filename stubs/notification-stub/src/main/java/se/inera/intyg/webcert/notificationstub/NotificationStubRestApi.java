/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.notificationstub;

import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.webcert.notificationstub.v3.NotificationStoreV3;
import se.inera.intyg.webcert.notificationstub.v3.NotificationStubStateBean;
import se.inera.intyg.webcert.notificationstub.v3.stat.NotificationStubEntry;
import se.inera.intyg.webcert.notificationstub.v3.stat.StatTransformerUtil;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

// CHECKSTYLE:OFF LineLength
public class NotificationStubRestApi {

    @Autowired
    private NotificationStoreV3 notificationStoreV3;

    @Autowired
    private NotificationStubStateBean stubStateBean;

    @GET
    @Path("/notifieringar/v3")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType> notifieringarV3() {
        return notificationStoreV3.getNotifications();
    }

    @GET
    @Path("/notifieringar/v3/stats")
    @Produces(MediaType.APPLICATION_JSON)
    public Response notifieringarV3Stats() {
        Collection<se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType> notifs = notificationStoreV3
                .getNotifications();
        Map<String, List<NotificationStubEntry>> stringListMap = new StatTransformerUtil().toStat(notifs);
        StringBuilder buf = new StringBuilder();
        for (Map.Entry<String, List<NotificationStubEntry>> entry : stringListMap.entrySet()) {
            buf.append("---- ").append(entry.getKey()).append(" ----\n");
            entry.getValue().stream()
                    .sorted(Comparator.comparing(NotificationStubEntry::getHandelseTid))
                    .forEach(ie -> buf.append(ie.getHandelseTid().format(DateTimeFormatter.ofPattern("HH:mm:ss"))).append("\t")
                            .append(ie.getHandelseKod()).append("\n"));
            buf.append("-----------------------------------------------\n\n");
        }
        return Response.ok(buf.toString()).build();
    }

    @POST
    @Path("/clear")
    public void clear() {
        notificationStoreV3.clear();
    }

    @GET
    @Path("/notifieringar/v3/emulateError")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getErrorCode() {
        String errorCode = stubStateBean.getErrorCode();
        return Response.ok("Stub is set to emulateError with code " + errorCode).build();
    }

    @GET
    @Path("/notifieringar/v3/emulateError/{errorCode}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setErrorCode(@PathParam("errorCode") String errorCode) {
        stubStateBean.setErrorCode(errorCode);
        return Response.ok("Stub set to emulateError with code " + errorCode).build();
    }
}
