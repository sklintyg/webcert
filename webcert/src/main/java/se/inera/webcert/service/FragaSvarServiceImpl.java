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
import org.springframework.transaction.annotation.Transactional;

import se.inera.certificate.model.Utlatande;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.webcert.converter.FKAnswerConverter;
import se.inera.webcert.converter.FKQuestionConverter;
import se.inera.webcert.converter.FragaSvarConverter;
import se.inera.webcert.persistence.fragasvar.model.Amne;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.webcert.persistence.fragasvar.model.Status;
import se.inera.webcert.persistence.fragasvar.model.Vardperson;
import se.inera.webcert.persistence.fragasvar.repository.FragaSvarRepository;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.sendmedicalcertificateanswer.v1.rivtabp20.SendMedicalCertificateAnswerResponderInterface;
import se.inera.webcert.sendmedicalcertificateanswerresponder.v1.AnswerToFkType;
import se.inera.webcert.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerResponseType;
import se.inera.webcert.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerType;
import se.inera.webcert.sendmedicalcertificatequestion.v1.rivtabp20.SendMedicalCertificateQuestionResponderInterface;
import se.inera.webcert.sendmedicalcertificatequestionsponder.v1.QuestionToFkType;
import se.inera.webcert.sendmedicalcertificatequestionsponder.v1.SendMedicalCertificateQuestionResponseType;
import se.inera.webcert.sendmedicalcertificatequestionsponder.v1.SendMedicalCertificateQuestionType;
import se.inera.webcert.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.util.FragaSvarSenasteHandelseDatumComparator;
import se.inera.webcert.web.service.WebCertUserService;

import com.google.common.base.Throwables;

/**
 * @author andreaskaltenbach
 */
@Service
@Transactional
public class FragaSvarServiceImpl implements FragaSvarService {

    private static final Logger LOG = LoggerFactory.getLogger(FragaSvarServiceImpl.class);

    private static final String FRAGE_STALLARE_WEBCERT = "WC";

    @Autowired
    private MailNotificationService mailNotificationService;

    @Autowired
    private FragaSvarRepository fragaSvarRepository;

    @Autowired
    private IntygService intygService;

    @Autowired
    private WebCertUserService webCertUserService;

    @Autowired
    SendMedicalCertificateAnswerResponderInterface sendAnswerToFKClient;

    @Autowired
    SendMedicalCertificateQuestionResponderInterface sendQuestionToFKClient;

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
    public void processIncomingAnswer(Long internId, String svarsText, LocalDateTime svarSigneringsDatum) {

        // lookup question in database
        FragaSvar fragaSvar = fragaSvarRepository.findOne(internId);

        if (fragaSvar == null) {
            throw new IllegalStateException("No question found with internal ID " + internId);
        }

        if ("FK".equals(fragaSvar.getFrageStallare())) {
            throw new IllegalStateException("Incoming answer refers to question initiated by Försäkringskassan.");
        }

        // TODO - validation: does answer fit to question?

        fragaSvar.setSvarsText(svarsText);
        fragaSvar.setSvarSigneringsDatum(svarSigneringsDatum);
        fragaSvar.setSvarSkickadDatum(new LocalDateTime());

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
        List<String> hsaEnhetIds = user.getVardenheterIds();

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
    public FragaSvar saveSvar(Long fragaSvarsId, String svarsText) {
        // Input sanity check
        if (StringUtils.isEmpty(svarsText)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "SvarsText cannot be empty!");
        }

        // Look up entity in repository
        FragaSvar fragaSvar = fragaSvarRepository.findOne(fragaSvarsId);
        if (fragaSvar == null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Could not find FragaSvar with id:" + fragaSvarsId);
        }

        // Is user authorized to save an answer to this question?
        WebCertUser user = webCertUserService.getWebCertUser();
        String fragaEnhetsId = fragaSvar.getVardperson().getEnhetsId();
        if (!user.getVardenheterIds().contains(fragaEnhetsId)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM, "User " + user.getHsaId() + " not authorized to answer question for enhet "
                    + fragaEnhetsId);
        }

        if (!fragaSvar.getStatus().equals(Status.PENDING_INTERNAL_ACTION)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "FragaSvar with id " + fragaSvar.getInternReferens().toString()
                    + " has invalid state for saving answer(" + fragaSvar.getStatus() + ")");
        }

        // Implement Business Rule RE-20
        if (Amne.PAMINNELSE.equals(fragaSvar.getAmne())) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "FragaSvar with id " + fragaSvar.getInternReferens().toString()
                    + " has invalid Amne(" + fragaSvar.getAmne() + ") for saving answer");
        }

        // Implement Business Rule RE-06
        if (Amne.KOMPLETTERING_AV_LAKARINTYG.equals(fragaSvar.getAmne()) && !user.isLakare()) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM, "FragaSvar with id " + fragaSvar.getInternReferens().toString()
                    + " and amne (" + fragaSvar.getAmne() + ") can only be answered by user that is Lakare");
        }
        // Ok, lets save the answer
        fragaSvar.setSvarsText(svarsText);
        fragaSvar.setSvarSkickadDatum(new LocalDateTime());
        fragaSvar.setStatus(Status.CLOSED);
        // TODO: SvarSigneringsDatum??
        FragaSvar saved = fragaSvarRepository.save(fragaSvar);

        // Send to external party (FK)
        SendMedicalCertificateAnswerType sendType = new SendMedicalCertificateAnswerType();
        AnswerToFkType answer = FKAnswerConverter.convert(saved);
        sendType.setAnswer(answer);
        SendMedicalCertificateAnswerResponseType response = sendAnswerToFKClient.sendMedicalCertificateAnswer(null,
                sendType);
        if (!response.getResult().getResultCode().equals(ResultCodeEnum.OK)) {
            LOG.error("Failed to send answer to FK, result was " + response.getResult().toString());
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.EXTERNAL_SYSTEM_PROBLEM, response.getResult().getErrorText());
        }
        return saved;

    }

    @Override
    public FragaSvar saveNewQuestion(String intygId, Amne amne, String frageText) {
        // Input sanity check
        if (StringUtils.isEmpty(frageText)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "frageText cannot be empty!");
        }

        if (amne == null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Amne cannot be null!");
        }

        // Fetch from Intygstjansten
        Utlatande utlatande = intygService.fetchIntygCommonModel(intygId);

        // Get utfardande vardperson
        Vardperson vardPerson = FragaSvarConverter.convert(utlatande.getSkapadAv());

        // Is user authorized to save an answer to this question?
        // Yes, if current user has the cerificate issuers unit in his list of authorized units
        WebCertUser user = webCertUserService.getWebCertUser();
        String fragaEnhetsId = vardPerson.getEnhetsId();
        if (!user.getVardenheterIds().contains(fragaEnhetsId)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM, "User " + user.getHsaId() + " not authorized to answer question for enhet "
                    + fragaEnhetsId);
        }

        IntygsReferens intygsReferens = FragaSvarConverter.convertToIntygsReferens(utlatande);

        FragaSvar fraga = new FragaSvar();
        fraga.setFrageStallare(FRAGE_STALLARE_WEBCERT);
        fraga.setAmne(amne);
        fraga.setFrageText(frageText);
        fraga.setFrageSkickadDatum(new LocalDateTime());

        fraga.setIntygsReferens(intygsReferens);
        fraga.setVardperson(vardPerson);
        fraga.setStatus(Status.PENDING_EXTERNAL_ACTION);

        // Ok, lets save the question
        FragaSvar saved = fragaSvarRepository.save(fraga);

        // Send to external party (FK)
        SendMedicalCertificateQuestionType sendType = new SendMedicalCertificateQuestionType();
        QuestionToFkType question = FKQuestionConverter.convert(saved);
        sendType.setQuestion(question);
        SendMedicalCertificateQuestionResponseType response = sendQuestionToFKClient.sendMedicalCertificateQuestion(
                null, sendType);
        if (!response.getResult().getResultCode().equals(ResultCodeEnum.OK)) {
            LOG.error("Failed to send question to FK, result was " + response.getResult().toString());
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.EXTERNAL_SYSTEM_PROBLEM, response.getResult().getErrorText());
        }
        return saved;

    }

    @Override
    public FragaSvar setDispatchState(Long frageSvarId, Boolean isDispatched) {
        // Look up entity in repository
        FragaSvar fragaSvar = fragaSvarRepository.findOne(frageSvarId);
        if (fragaSvar == null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Could not find FragaSvar with id:" + frageSvarId);
        }
        // Set & save new vidarebefordrad state
        fragaSvar.setVidarebefordrad(isDispatched);
        return fragaSvarRepository.save(fragaSvar);
    }

    @Override
    public FragaSvar closeQuestionAsHandled(Long frageSvarId){
        FragaSvar fragaSvar = fragaSvarRepository.findOne(frageSvarId);
        if (fragaSvar == null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Could not find FragaSvar with id:" + frageSvarId);
        }

        fragaSvar.setStatus(Status.CLOSED);
        FragaSvar saved = fragaSvarRepository.save(fragaSvar);


        return saved;
    }

    @Override
    public FragaSvar openQuestionAsUnhandled(Long frageSvarId){
        FragaSvar fragaSvar = fragaSvarRepository.findOne(frageSvarId);
        if (fragaSvar == null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Could not find FragaSvar with id:" + frageSvarId);
        }

        if (fragaSvar.getSvarsText() != null && !fragaSvar.getSvarsText().isEmpty()) {
            fragaSvar.setStatus(Status.ANSWERED);
        } else {
            if(fragaSvar.getFrageStallare().equalsIgnoreCase(FRAGE_STALLARE_WEBCERT)){
                fragaSvar.setStatus(Status.PENDING_EXTERNAL_ACTION);
            }else{
                fragaSvar.setStatus(Status.PENDING_INTERNAL_ACTION);
            }


        }
        FragaSvar saved = fragaSvarRepository.save(fragaSvar);


        return saved;
    }
}
