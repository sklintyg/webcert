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
package se.inera.intyg.webcert.web.web.controller.internalapi;

import io.swagger.annotations.Api;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.infra.testcertificate.dto.TestCertificateEraseRequest;
import se.inera.intyg.infra.testcertificate.dto.TestCertificateEraseResult;
import se.inera.intyg.webcert.web.service.testcertificate.TestCertificateService;

/**
 * Internal REST endpoint for managing test certificates.
 */
@Path("/testCertificate")
@Api(value = "/internalapi/testCertificate", produces = MediaType.APPLICATION_JSON)
public class TestCertificateController {

    @Autowired
    private TestCertificateService testCertificateService;

    private static final String UTF_8_CHARSET = ";charset=utf-8";

    @PrometheusTimeMethod
    @POST
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Path("/erase")
    public Response eraseTestCertificates(@RequestBody TestCertificateEraseRequest eraseRequest) {

        if (eraseRequest.getTo() == null) {
            return Response.status(400, "Missing date to").build();
        }

        if (eraseRequest.getFrom() != null && eraseRequest.getFrom().isAfter(eraseRequest.getTo())) {
            return Response.status(400, "From date is after to date").build();
        }

        final TestCertificateEraseResult eraseResult =
            testCertificateService.eraseTestCertificates(eraseRequest.getFrom(), eraseRequest.getTo());

        return Response.ok(eraseResult).build();
    }

}
