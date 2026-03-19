/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.api;

import io.swagger.annotations.Api;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.privatepractitioner.PrivatePractitionerService;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.HospInformationResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerConfigResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerDetails;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.PrivatePractitionerResponse;

@Api(value = "private-practitioner", produces = MediaType.APPLICATION_JSON)
public class PrivatePractitionerApiController {

  private static final String UTF_8_CHARSET = ";charset=utf-8";

  private PrivatePractitionerService service;

  public PrivatePractitionerApiController(PrivatePractitionerService service) {
    this.service = service;
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
  @PerformanceLogging(
      eventAction = "register-private-practitioner",
      eventType = MdcLogConstants.EVENT_TYPE_CREATION)
  public Response registerPractitioner(
      PrivatePractitionerDetails registerPrivatePractitionerRequest) {
    service.registerPrivatePractitioner(registerPrivatePractitionerRequest);
    return Response.ok().build();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
  @PerformanceLogging(
      eventAction = "get-private-practitioner",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public PrivatePractitionerResponse getPrivatePractitioner() {
    return service.getLoggedInPrivatePractitioner();
  }

  @GET
  @Path("/config")
  @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
  @PerformanceLogging(
      eventAction = "get-private-practitioner-config",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public PrivatePractitionerConfigResponse getPrivatePractitionerConfig() {
    return service.getPrivatePractitionerConfig();
  }

  @GET
  @Path("/hospInformation")
  @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
  @PerformanceLogging(
      eventAction = "get-private-practitioner-hosp-information",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public HospInformationResponse getHospInformation() {
    return service.getHospInformation();
  }

  @PUT
  @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
  @PerformanceLogging(
      eventAction = "update-private-practitioner",
      eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
  public PrivatePractitionerResponse updatePrivatePractitioner(
      PrivatePractitionerDetails updatePrivatePractitionerRequest) {
    return service.editPrivatePractitioner(updatePrivatePractitionerRequest);
  }
}
