package se.inera.intyg.webcert.web.service.fragasvar;

import org.joda.time.LocalDateTime;
import se.inera.webcert.persistence.fragasvar.model.Amne;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.web.service.dto.Lakare;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarParameter;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarResponse;

import java.util.List;
import java.util.Map;

/**
 * @author andreaskaltenbach
 */
public interface FragaSvarService {

    FragaSvar processIncomingQuestion(FragaSvar fragaSvar);

    FragaSvar processIncomingAnswer(Long internId, String svarsText, LocalDateTime svarSigneringsDatum);

    List<FragaSvar> getFragaSvar(List<String> enhetsHsaIds);

    /**
     * Returns all the question/answer pairs that exist for the given certificate.
     */
    List<FragaSvar> getFragaSvar(String intygId);


    /**
     * Create an answer for an existing  question.
     */
    FragaSvar saveSvar(Long frageSvarId, String svarsText);

    /**
     * Create a new FragaSvar instance for a certificate and send it to external receiver (FK).
     */
    FragaSvar saveNewQuestion(String intygId, String typ, Amne amne, String frageText);

    /**
     * Set the dispatch state for the specified {@link FragaSvar} entity.
     */
    FragaSvar setDispatchState(Long frageSvarId, Boolean isDispatched);


    /**
     * A FragaSvar is set as handled.
     * Sets the status of a FragaSvar as "closed"
     *
     * @return the FragaSvar-object that has been closed
     */
    FragaSvar closeQuestionAsHandled(Long frageSvarId);

    /**
     * Close all questions related to a certificate.
     *
     * @param intygsId the certificates unique identifier
     * @return an array with FragaSvar-objects that has been closed.
     */
    FragaSvar[] closeAllNonClosedQuestions(String intygsId);

    /**
     * A FragaSvar is set as unhandled.
     * If it has an answer, the status is set to "ANSWERED"
     * If it doesn't have an answer, the status is set to "PENDING_EXTERNAL_ACTION"
     */
    FragaSvar openQuestionAsUnhandled(Long frageSvarId);

    /**
     * Returns all the question/answer matching filter criteria.
     */
    QueryFragaSvarResponse filterFragaSvar(QueryFragaSvarParameter filterParameters);

    /**
     * Returns a list of all unique hsaId and name (of vardperson who signed a certificate that a FragaSvar is linked to) that matches the supplied id.
     */
    List<Lakare> getFragaSvarHsaIdByEnhet(String enhetsId);

    /**
     * Returns a count of unhandled {@link FragaSvar} entities that matches the supplied hsa unit id's vardenheterIds.
     */
    long getUnhandledFragaSvarForUnitsCount(List<String> vardenheterIds);

    /**
     * Returns a {@link Map} containing the nbr of unhandled {@link FragaSvar} FragaSvar with the HSA id of the care unit as key.
     */
    Map<String, Long> getNbrOfUnhandledFragaSvarForCareUnits(List<String> vardenheterIds);

}
