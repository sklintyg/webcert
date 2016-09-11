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

package se.inera.intyg.webcert.notificationstub;

import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.webcert.notificationstub.v2.NotificationStoreV2;
import se.inera.intyg.webcert.notificationstub.v2.stat.NotificationStubEntry;
import se.inera.intyg.webcert.notificationstub.v2.stat.StatTransformerUtil;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;


public class NotificationStubRestApi {

    @Autowired
    private NotificationStore notificationStore;

    @Autowired
    private NotificationStoreV2 notificationStoreV2;

    @GET
    @Path("/notifieringar")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<CertificateStatusUpdateForCareType> notifieringar() {
        return notificationStore.getNotifications();
    }

    @GET
    @Path("/notifieringar/v2")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v2.CertificateStatusUpdateForCareType> notifieringarV2() {
        return notificationStoreV2.getNotifications();
    }

    @GET
    @Path("/notifieringar/v2/stats")
    @Produces(MediaType.APPLICATION_JSON)
    public Response notifieringarV2Stats() {
        Collection<se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v2.CertificateStatusUpdateForCareType> notifs = notificationStoreV2.getNotifications();
        Map<String, List<NotificationStubEntry>> stringListMap = new StatTransformerUtil().toStat(notifs);
        StringBuilder buf = new StringBuilder();
        for (Map.Entry<String, List<NotificationStubEntry>> entry : stringListMap.entrySet()) {
            buf.append("---- ").append(entry.getKey()).append(" ----\n");
            entry.getValue().stream()
                    .sorted((a, b) -> a.getHandelseTid().compareTo(b.getHandelseTid()))
                    .forEach(ie -> buf.append(ie.getHandelseTid().format(DateTimeFormatter.ofPattern("HH:mm:ss"))).append("\t").append(ie.getHandelseKod()).append("\n"));
            buf.append("-----------------------------------------------\n\n");
        }
        return Response.ok(buf.toString()).build();
    }


    @POST
    @Path("/clear")
    public void clear() {
        notificationStoreV2.clear();
        notificationStore.clear();
    }
}
