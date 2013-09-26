package se.inera.webcert.service;

import java.util.Collections;
import java.util.List;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.fragasvar.repository.FragaSvarRepository;
import se.inera.webcert.service.util.FragaSvarSenasteHandelseDatumComparator;

import com.google.common.base.Throwables;

/**
 * @author andreaskaltenbach
 */
@Service
public class FragaSvarServiceImpl implements FragaSvarService {

    @Autowired
    private MailNotificationService mailNotificationService;

    @Autowired
    private FragaSvarRepository fragaSvarRepository;

    private static FragaSvarSenasteHandelseDatumComparator senasteHandelseDatumComparator = new FragaSvarSenasteHandelseDatumComparator();

    @Override
    public void processIncomingQuestion(FragaSvar fragaSvar) {

        // TODO - validation: does certificate exist

        // persist the question
        fragaSvarRepository.save(fragaSvar);

        // send mail to enhet to inform about new question
        try {
            mailNotificationService.sendMailForIncomingQuestion(fragaSvar);
        } catch (MessagingException e) {
            Throwables.propagate(e);
        }
    }

    @Override
    public void processIncomingAnswer(FragaSvar fragaSvar) {

        // TODO - validation: does answer fit to question?

        // update the FragaSvar
        fragaSvarRepository.save(fragaSvar);

        // send mail to enhet to inform about new question
        try {
            mailNotificationService.sendMailForIncomingAnswer(fragaSvar);
        } catch (MessagingException e) {
            Throwables.propagate(e);
        }
    }

    @Override
    public List<FragaSvar> getFragaSvar(List<String> enhetsHsaIds) {
        List<FragaSvar> result = fragaSvarRepository.findByEnhetsId(enhetsHsaIds);
        if (result != null) {
            // We do the sorting in code, since we need to sort on a derived property and not a direct entity persisted
            // proerty in which case we could have used an order by in the query.
            Collections.sort(result, senasteHandelseDatumComparator);
        }
        return result;
    }
}
