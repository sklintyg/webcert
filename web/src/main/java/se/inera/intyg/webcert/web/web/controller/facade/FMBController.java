/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.facade;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.service.facade.ValidateSickLeavePeriodFacadeService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ValidateSickLeavePeriodRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ValidateSickLeavePeriodResponseDTO;

@Path("/fmb")
public class FMBController {

    private static final Logger LOG = LoggerFactory.getLogger(FMBController.class);

    private static final String UTF_8_CHARSET = ";charset=utf-8";

    @Autowired
    private ValidateSickLeavePeriodFacadeService validateSickLeavePeriodFacadeService;

    @POST
    @Path("/validateSickLeavePeriod")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "fmb-validate-sickleave-period", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response validateSickLeavePeriod(@RequestBody @NotNull ValidateSickLeavePeriodRequestDTO validateSickLeavePeriod) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Validating sick leave period");
        }
        final var response = validateSickLeavePeriodFacadeService.validateSickLeavePeriod(validateSickLeavePeriod);
        return Response.ok(ValidateSickLeavePeriodResponseDTO.create(response)).build();
    }
}
