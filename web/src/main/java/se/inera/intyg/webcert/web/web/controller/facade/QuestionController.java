/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

import java.util.List;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.web.service.facade.question.GetQuestionsFacadeService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.QuestionsResponseDTO;

@Path("/question")
public class QuestionController {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateController.class);
    private static final String UTF_8_CHARSET = ";charset=utf-8";

    private final GetQuestionsFacadeService getQuestionsFacadeService;

    @Autowired
    public QuestionController(GetQuestionsFacadeService getQuestionsFacadeService) {
        this.getQuestionsFacadeService = getQuestionsFacadeService;
    }

    @GET
    @Path("/{certificateId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response getQuestions(@PathParam("certificateId") @NotNull String certificateId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Getting questions for certificate with id: '{}'", certificateId);
        }

        List<Question> questions = getQuestionsFacadeService.getQuestions(certificateId);
        return Response.ok(QuestionsResponseDTO.create(questions)).build();
    }

}
