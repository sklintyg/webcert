package se.inera.webcert.service;

import java.util.List;

import se.inera.webcert.persistence.fragasvar.model.FragaSvar;

/**
 * @author andreaskaltenbach
 */
public interface FragaSvarService {

    void processIncomingQuestion(FragaSvar fragaSvar);
    void processIncomingAnswer(FragaSvar fragaSvar);

    List<FragaSvar> getFragaSvar(List<String> enhetsHsaIds);
}
