package se.inera.webcert.service;

import se.inera.webcert.persistence.FragaSvar;

/**
 * @author andreaskaltenbach
 */
public interface FragaSvarService {

    void processIncomingQuestion(FragaSvar fragaSvar);
    void processIncomingAnswer(FragaSvar fragaSvar);

}
