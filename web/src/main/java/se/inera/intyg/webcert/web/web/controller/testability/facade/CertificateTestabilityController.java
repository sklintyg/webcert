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
package se.inera.intyg.webcert.web.web.controller.testability.facade;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CertificatePatientsResponseDTO;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CertificateTypesResponseDTO;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateQuestionRequestDTO;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateQuestionResponseDTO;
import se.inera.intyg.webcert.web.web.controller.testability.facade.util.CreateCertificateTestabilityUtil;
import se.inera.intyg.webcert.web.web.controller.testability.facade.util.CreateQuestionTestabilityUtil;
import se.inera.intyg.webcert.web.web.controller.testability.facade.util.SupportedCertificateTypesUtil;
import se.inera.intyg.webcert.web.web.controller.testability.facade.util.SupportedPatientsUtil;

@Path("/certificate")
public class CertificateTestabilityController extends AbstractApiController {

    private final CreateCertificateTestabilityUtil createCertificateTestabilityUtil;
    private final CreateQuestionTestabilityUtil createQuestionTestabilityUtil;
    private final SupportedCertificateTypesUtil supportedCertificateTypesUtil;
    private final SupportedPatientsUtil supportedPatientsUtil;

    @Autowired
    public CertificateTestabilityController(CreateCertificateTestabilityUtil createCertificateTestabilityUtil,
        CreateQuestionTestabilityUtil createQuestionTestabilityUtil,
        SupportedCertificateTypesUtil supportedCertificateTypesUtil,
        SupportedPatientsUtil supportedPatientsUtil) {
        this.createCertificateTestabilityUtil = createCertificateTestabilityUtil;
        this.createQuestionTestabilityUtil = createQuestionTestabilityUtil;
        this.supportedCertificateTypesUtil = supportedCertificateTypesUtil;
        this.supportedPatientsUtil = supportedPatientsUtil;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    public Response createCertificate(@RequestBody @NotNull CreateCertificateRequestDTO createCertificateRequest) {
        final var certificateId = createCertificateTestabilityUtil.createNewCertificate(createCertificateRequest);
        return Response.ok(new CreateCertificateResponseDTO(certificateId)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{certificateId}/question")
    public Response createQuestion(@PathParam("certificateId") @NotNull String certificateId,
        @RequestBody @NotNull CreateQuestionRequestDTO createQuestionRequest) {
        final var questionId = createQuestionTestabilityUtil.createNewQuestion(certificateId, createQuestionRequest);
        return Response.ok(new CreateQuestionResponseDTO(questionId)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{certificateId}/questionDraft")
    public Response createQuestionDraft(@PathParam("certificateId") @NotNull String certificateId,
        @RequestBody @NotNull CreateQuestionRequestDTO createQuestionRequest) {
        final var questionId = createQuestionTestabilityUtil.createNewQuestionDraft(certificateId, createQuestionRequest);
        return Response.ok(new CreateQuestionResponseDTO(questionId)).build();
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/types")
    public Response getSupportedCerificateTypes() {
        final var certificateTypes = supportedCertificateTypesUtil.get();
        return Response.ok(new CertificateTypesResponseDTO(certificateTypes)).build();
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/patients")
    public Response getSuppportedPatients() {
        final var patients = supportedPatientsUtil.get();
        return Response.ok(new CertificatePatientsResponseDTO(patients)).build();
    }
}
