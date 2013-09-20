package se.inera.webcert.service;

import org.springframework.stereotype.Service;

import se.inera.webcert.persistence.fragasvar.model.FragaSvar;

/**
 * @author andreaskaltenbach
 */
@Service
public class FragaSvarServiceImpl implements FragaSvarService {


    @Override
    public void processIncomingQuestion(FragaSvar fragaSvar) {

        // persist the question

        // send mail to enhet

    }

    @Override
    public void processIncomingAnswer(FragaSvar fragaSvar) {

        // update the FragaSvar

    }
}
