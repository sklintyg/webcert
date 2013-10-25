package se.inera.webcert.service;

import java.util.List;

import org.joda.time.LocalDateTime;

import se.inera.webcert.persistence.fragasvar.model.Amne;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.fragasvar.repository.FragaSvarFilter;

/**
 * @author andreaskaltenbach
 */
public interface FragaSvarService {

    void processIncomingQuestion(FragaSvar fragaSvar);

    void processIncomingAnswer(Long internId, String svarsText, LocalDateTime svarSigneringsDatum);

    List<FragaSvar> getFragaSvar(List<String> enhetsHsaIds);

    /**
     * Returns all the question/answer pairs that exist for the given certificate.
     */
    List<FragaSvar> getFragaSvar(String intygId);
    

    /**
     * Create an answer for an existing  question
     */
    FragaSvar saveSvar(Long frageSvarId, String svarsText);
    
    /**
     * Create a new FragaSvar instance for a certificate and send it to external receiver (FK)
     */
    FragaSvar saveNewQuestion(String intygId, Amne amne, String frageText);

    /**
     * Set the dispatch state for the specified {@link FragaSvar} entity
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
     * @param frageSvarId
     * @return
     */
    FragaSvar openQuestionAsUnhandled(Long frageSvarId);
    
    /**
     * Returns all the question/answer matching filter criteria.
     */
    List<FragaSvar> getFragaSvarByFilter(FragaSvarFilter filter, int startFrom, int pageSize);
    
    /**
     * Returns total count of question/answers matching filter criteria.
     */
    int getFragaSvarByFilterCount(FragaSvarFilter filter);

}
