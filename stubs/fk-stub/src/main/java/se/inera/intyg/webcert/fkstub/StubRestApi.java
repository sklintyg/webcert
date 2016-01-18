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

package se.inera.intyg.webcert.fkstub;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.AnswerToFkType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.QuestionToFkType;

/**
 * @author andreaskaltenbach
 */
public class StubRestApi {

    @Autowired
    private QuestionAnswerStore questionAnswerStore;

    @GET
    @Path("/fragor")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<QuestionToFkType> fragor() {
        return questionAnswerStore.getQuestions().values();
    }
    @DELETE
    @Path("/fragor")
    public void rensaFragor() {
        questionAnswerStore.getQuestions().clear();
    }
    @DELETE
    @Path("/fragor/{id}")
    public void rensaFraga(@PathParam("id") String id) {
        questionAnswerStore.getQuestions().remove(id);
    }

    @GET
    @Path("/fragor/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public QuestionToFkType fraga(@PathParam("id") String id) {
        return questionAnswerStore.getQuestions().get(id);
    }

    @GET
    @Path("/svar")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<AnswerToFkType> svar() {
        return questionAnswerStore.getAnswers().values();
    }

    @DELETE
    @Path("/svar")
    public void rensaSvar() {
        questionAnswerStore.getAnswers().clear();
    }

    @DELETE
    @Path("/svar/{id}")
    public void rensaSvar(@PathParam("id") String id) {
        questionAnswerStore.getAnswers().remove(id);
    }

    @GET
    @Path("/svar/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public AnswerToFkType svar(@PathParam("id") String id) {
        return questionAnswerStore.getAnswers().get(id);
    }
}
