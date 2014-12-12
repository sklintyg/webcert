package se.inera.webcert.service.fragasvar;

import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;

import se.inera.webcert.persistence.fragasvar.model.Amne;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.service.dto.Lakare;
import se.inera.webcert.service.fragasvar.dto.QueryFragaSvarParameter;
import se.inera.webcert.service.fragasvar.dto.QueryFragaSvarResponse;

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
     *
     * @param frageSvarId
     * @param isDispatched
     * @return
     */
    FragaSvar setDispatchState(Long frageSvarId, Boolean isDispatched);


    /**
     * A FragaSvar is set as handled.
     * Sets the status of a FragaSvar as "closed"
     *
     * @param frageSvarId
     * @return
     */
    FragaSvar closeQuestionAsHandled(Long frageSvarId);

    /**
     * A FragaSvar is set as unhandled.
     * If it has an answer, the status is set to "ANSWERED"
     * If it doesn't have an answer, the status is set to "PENDING_EXTERNAL_ACTION"
     *
     * @param frageSvarId
     * @return
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
     *
     * @param vardenheterIds
     * @return
     */
    Map<String, Long> getNbrOfUnhandledFragaSvarForCareUnits(List<String> vardenheterIds);
}
