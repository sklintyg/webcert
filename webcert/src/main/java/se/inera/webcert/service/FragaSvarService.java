package se.inera.webcert.service;

import se.inera.webcert.persistence.fragasvar.model.FragaSvar;

/**
 * @author andreaskaltenbach
 */
public interface FragaSvarService {

    void processIncomingQuestion(FragaSvar fragaSvar);
    void processIncomingAnswer(FragaSvar fragaSvar);

}
