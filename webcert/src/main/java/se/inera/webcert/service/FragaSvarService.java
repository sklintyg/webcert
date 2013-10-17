package se.inera.webcert.service;

import java.util.List;

import org.joda.time.LocalDateTime;

import se.inera.webcert.persistence.fragasvar.model.Amne;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;

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
    
}
