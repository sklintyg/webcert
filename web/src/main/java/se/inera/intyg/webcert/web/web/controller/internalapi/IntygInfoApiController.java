/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.internalapi;

import io.swagger.annotations.Api;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import se.inera.intyg.infra.intyginfo.dto.WcIntygInfo;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.service.intyginfo.IntygInfoServiceInterface;

@Path("/intygInfo")
@Api(value = "/internalapi/intygInfo", produces = MediaType.APPLICATION_JSON)
public class IntygInfoApiController {

    private static final String UTF_8_CHARSET = ";charset=utf-8";

    private final IntygInfoServiceInterface intygInfoService;

    public IntygInfoApiController(@Qualifier("intygInfoAggregator")
    IntygInfoServiceInterface intygInfoService) {
        this.intygInfoService = intygInfoService;
    }

    @GET
    @Path("/{intygId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "intyg-info-get-certificate-info", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response getIntygInfo(@PathParam("intygId") String intygId) {

        Optional<WcIntygInfo> wcIntygInfo = intygInfoService.getIntygInfo(intygId);

        if (!wcIntygInfo.isPresent()) {
            return Response.status(Status.NOT_FOUND).build();
        }

        return Response.ok(wcIntygInfo.get()).build();
    }

}
