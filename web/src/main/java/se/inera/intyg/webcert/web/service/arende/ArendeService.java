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

package se.inera.intyg.webcert.web.service.arende;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.web.service.exception.WebcertServiceException;

/**
 * @author andreaskaltenbach
 */
public interface ArendeService {

    Arende processIncomingMessage(Arende arende) throws WebcertServiceException;

//    Arende processIncomingAnswer(Long internId, String svarsText, LocalDateTime svarSigneringsDatum);

//    List<Arende> getFragaSvar(List<String> enhetsHsaIds);

    /**
     * Returns all the question/answer pairs that exist for the given certificate.
     */
//    List<Arende> getFragaSvar(String intygId);


    /**
     * Create an answer for an existing  question.
     */
//    Arende saveSvar(Long frageSvarId, String svarsText);

    /**
     * Create a new FragaSvar instance for a certificate and send it to external receiver (FK).
     */
//    Arende saveNewQuestion(String intygId, String typ, Amne amne, String frageText);

    /**
     * Set the dispatch state for the specified {@link FragaSvar} entity.
     */
//    Arende setDispatchState(Long frageSvarId, Boolean isDispatched);


    /**
     * A FragaSvar is set as handled.
     * Sets the status of a FragaSvar as "closed"
     *
     * @return the FragaSvar-object that has been closed
     */
//    Arende closeQuestionAsHandled(Long frageSvarId);

    /**
     * Close all questions related to a certificate.
     *
     * @param intygsId the certificates unique identifier
     * @return an array with FragaSvar-objects that has been closed.
     */
//    Arende[] closeAllNonClosedQuestions(String intygsId);

    /**
     * A FragaSvar is set as unhandled.
     * If it has an answer, the status is set to "ANSWERED"
     * If it doesn't have an answer, the status is set to "PENDING_EXTERNAL_ACTION"
     */
//    Arende openQuestionAsUnhandled(Long frageSvarId);

    /**
     * Returns all the question/answer matching filter criteria.
     */
//    QueryFragaSvarResponse filterFragaSvar(QueryFragaSvarParameter filterParameters);

    /**
     * Returns a list of all unique hsaId and name (of vardperson who signed a certificate that a FragaSvar is linked to) that matches the supplied id.
     */
//    List<Lakare> getFragaSvarHsaIdByEnhet(String enhetsId);

    /**
     * Returns a count of unhandled {@link FragaSvar} entities that matches the supplied hsa unit id's vardenheterIds.
     */
//    long getUnhandledFragaSvarForUnitsCount(List<String> vardenheterIds);

    /**
     * Returns a {@link Map} containing the nbr of unhandled {@link FragaSvar} FragaSvar with the HSA id of the care unit as key.
     */
//    Map<String, Long> getNbrOfUnhandledFragaSvarForCareUnits(List<String> vardenheterIds);

}
