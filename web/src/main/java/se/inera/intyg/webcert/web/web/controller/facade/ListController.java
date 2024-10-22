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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.web.service.facade.list.ListDraftsFacadeServiceImpl;
import se.inera.intyg.webcert.web.service.facade.list.ListPreviousCertificatesFacadeServiceImpl;
import se.inera.intyg.webcert.web.service.facade.list.ListQuestionsFacadeServiceImpl;
import se.inera.intyg.webcert.web.service.facade.list.ListSignedCertificatesFacadeServiceImpl;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ListResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.list.ListRequestDTO;

@Path("/list")
public class ListController {

    private static final Logger LOG = LoggerFactory.getLogger(ListController.class);

    private static final String UTF_8_CHARSET = ";charset=utf-8";

    private final ListDraftsFacadeServiceImpl listDraftsFacadeService;
    private final ListSignedCertificatesFacadeServiceImpl listSignedCertificatesFacadeService;
    private final ListPreviousCertificatesFacadeServiceImpl listPreviousCertificatesFacadeService;
    private final ListQuestionsFacadeServiceImpl listQuestionsFacadeService;

    @Autowired
    public ListController(ListDraftsFacadeServiceImpl listDraftsFacadeService,
        ListSignedCertificatesFacadeServiceImpl listSignedCertificatesFacadeService,
        ListPreviousCertificatesFacadeServiceImpl listPreviousCertificatesFacadeService,
        ListQuestionsFacadeServiceImpl listQuestionsFacadeService) {
        this.listDraftsFacadeService = listDraftsFacadeService;
        this.listSignedCertificatesFacadeService = listSignedCertificatesFacadeService;
        this.listPreviousCertificatesFacadeService = listPreviousCertificatesFacadeService;
        this.listQuestionsFacadeService = listQuestionsFacadeService;
    }

    @POST
    @Path("/draft")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response getListOfDrafts(ListRequestDTO request) {
        LOG.debug("Getting list of drafts");
        final var listInfo = listDraftsFacadeService.get(request.getFilter());
        return Response.ok(ListResponseDTO.create(listInfo.getList(), listInfo.getTotalCount())).build();
    }

    @POST
    @Path("/certificate")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response getListOfSignedCertificates(ListRequestDTO request) {
        final var listInfo = listSignedCertificatesFacadeService.get(request.getFilter());
        return Response.ok().entity(ListResponseDTO.create(listInfo.getList(), listInfo.getTotalCount())).build();
    }

    @POST
    @Path("/previous")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response getListOfPreviousCertificates(ListRequestDTO request) {
        final var listInfo = listPreviousCertificatesFacadeService.get(request.getFilter());
        return Response.ok().entity(ListResponseDTO.create(listInfo.getList(), listInfo.getTotalCount())).build();
    }

    @POST
    @Path("/question")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response getListOfCertificatesWithQuestions(ListRequestDTO request) {
        final var listInfo = listQuestionsFacadeService.get(request.getFilter());
        return Response.ok().entity(ListResponseDTO.create(listInfo.getList(), listInfo.getTotalCount())).build();
    }
}
