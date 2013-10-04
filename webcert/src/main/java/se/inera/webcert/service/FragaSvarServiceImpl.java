package se.inera.webcert.service;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.mail.MessagingException;

import org.apache.cxf.common.util.StringUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.fragasvar.model.Status;
import se.inera.webcert.persistence.fragasvar.repository.FragaSvarRepository;
import se.inera.webcert.security.WebCertUser;
import se.inera.webcert.service.util.FragaSvarSenasteHandelseDatumComparator;
import se.inera.webcert.web.service.WebCertUserService;

import com.google.common.base.Throwables;

/**
 * @author andreaskaltenbach
 */
@Service
public class FragaSvarServiceImpl implements FragaSvarService {

    private static final Logger LOG = LoggerFactory.getLogger(FragaSvarServiceImpl.class);

    @Autowired
    private MailNotificationService mailNotificationService;

    @Autowired
    private FragaSvarRepository fragaSvarRepository;

    @Autowired
    private WebCertUserService webCertUserService;

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

    @Override
    public List<FragaSvar> getFragaSvar(String intygId) {

        List<FragaSvar> fragaSvarList = fragaSvarRepository.findByIntygsReferensIntygsId(intygId);

        WebCertUser user = webCertUserService.getWebCertUser();
        List<String> hsaEnhetIds = user.getVardEnheter();

        // Filter questions to that current user only sees questions issued to units with active employment role
        Iterator<FragaSvar> iterator = fragaSvarList.iterator();
        while (iterator.hasNext()) {
            FragaSvar fragaSvar = iterator.next();

            if (!hsaEnhetIds.contains(fragaSvar.getVardperson().getEnhetsId())) {
                iterator.remove();
            }
        }

        // Finally sort by senasteHandelseDatum
        // We do the sorting in code, since we need to sort on a derived property and not a direct entity persisted
        // property in which case we could have used an order by in the query.
        Collections.sort(fragaSvarList, senasteHandelseDatumComparator);
        return fragaSvarList;
    }

    @Override
    public FragaSvar saveSvar(FragaSvar fragaSvarRemote) {
        //Input sanity check
        if (StringUtils.isEmpty(fragaSvarRemote.getSvarsText())) {
            throw new IllegalArgumentException("SvarsText cannot be empty!");
        }
        
        // Look up entity in repository
        FragaSvar fragaSvarLocal = fragaSvarRepository.findOne(fragaSvarRemote.getInternReferens());
        if (fragaSvarLocal == null) {
            throw new RuntimeException("Could not find FragaSvar with id:" + fragaSvarRemote.getInternReferens());
        }

        //Is user authorized to save an answer to this question?
        WebCertUser user = webCertUserService.getWebCertUser();
        String fragaEnhetsId = fragaSvarLocal.getVardperson().getEnhetsId();
        if (!user.getVardEnheter().contains(fragaEnhetsId))  {
            throw new RuntimeException("User " + user.getHsaId() + " not authorized to answer question for enhet " + fragaEnhetsId);
        }
        
        if (!fragaSvarLocal.getStatus().equals(Status.PENDING_INTERNAL_ACTION)) {
            throw new IllegalStateException("FragaSvar with id " + fragaSvarLocal.getInternReferens().toString() + " has invalid state for saving new answer(" + fragaSvarLocal.getStatus() + ")"); 
        }

        //Ok, lets save the answer
        fragaSvarLocal.setSvarsText(fragaSvarRemote.getSvarsText());
        fragaSvarLocal.setSvarSkickadDatum(new LocalDateTime());
        fragaSvarLocal.setStatus(Status.ANSWERED);
        //TODO: SvarSigneringsDatum??
        fragaSvarRepository.save(fragaSvarLocal);
        
        //TODO: How about actually sending answer to fragestallaren (FK)?
      
        return fragaSvarLocal;
    }
}
