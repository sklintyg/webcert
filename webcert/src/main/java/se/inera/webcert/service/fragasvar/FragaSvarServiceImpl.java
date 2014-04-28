package se.inera.webcert.service.fragasvar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.w3.wsaddressing10.AttributedURIType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.webcert.converter.FKAnswerConverter;
import se.inera.webcert.converter.FKQuestionConverter;
import se.inera.webcert.converter.FragaSvarConverter;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.persistence.fragasvar.model.Amne;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.webcert.persistence.fragasvar.model.Status;
import se.inera.webcert.persistence.fragasvar.model.Vardperson;
import se.inera.webcert.persistence.fragasvar.repository.FragaSvarFilter;
import se.inera.webcert.persistence.fragasvar.repository.FragaSvarRepository;
import se.inera.webcert.persistence.fragasvar.repository.VantarPa;
import se.inera.webcert.sendmedicalcertificateanswer.v1.rivtabp20.SendMedicalCertificateAnswerResponderInterface;
import se.inera.webcert.sendmedicalcertificateanswerresponder.v1.AnswerToFkType;
import se.inera.webcert.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerResponseType;
import se.inera.webcert.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerType;
import se.inera.webcert.sendmedicalcertificatequestion.v1.rivtabp20.SendMedicalCertificateQuestionResponderInterface;
import se.inera.webcert.sendmedicalcertificatequestionsponder.v1.QuestionToFkType;
import se.inera.webcert.sendmedicalcertificatequestionsponder.v1.SendMedicalCertificateQuestionResponseType;
import se.inera.webcert.sendmedicalcertificatequestionsponder.v1.SendMedicalCertificateQuestionType;
import se.inera.webcert.service.IntygService;
import se.inera.webcert.service.MailNotificationService;
import se.inera.webcert.service.dto.IntygContentHolder;
import se.inera.webcert.service.dto.IntygStatus;
import se.inera.webcert.service.dto.Lakare;
import se.inera.webcert.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.fragasvar.dto.QueryFragaSvarParameter;
import se.inera.webcert.service.fragasvar.dto.QueryFragaSvarResponse;
import se.inera.webcert.service.util.FragaSvarSenasteHandelseDatumComparator;
import se.inera.webcert.web.service.WebCertUserService;

/**
 * @author andreaskaltenbach
 */
@Service
@Transactional
public class FragaSvarServiceImpl implements FragaSvarService {

    private static final Logger LOG = LoggerFactory.getLogger(FragaSvarServiceImpl.class);

    private static final String WEBCERT = "WC";
    private static final String FORSAKRINGSKASSAN = "FK";

    private static final String SENT_STATUS_TYPE = "SENT";
    private static final String REVOKED_STATUS_TYPE = "CANCELLED";

    private static final List<Amne> VALID_VARD_AMNEN = Arrays.asList(Amne.ARBETSTIDSFORLAGGNING, Amne.AVSTAMNINGSMOTE,
            Amne.KONTAKT, Amne.OVRIGT);

    private static final Integer DEFAULT_PAGE_SIZE = 10;

    @Autowired
    private MailNotificationService mailNotificationService;

    @Autowired
    private FragaSvarRepository fragaSvarRepository;

    @Autowired
    private IntygService intygService;

    @Autowired
    private WebCertUserService webCertUserService;

    @Autowired
    private SendMedicalCertificateAnswerResponderInterface sendAnswerToFKClient;

    @Autowired
    private SendMedicalCertificateQuestionResponderInterface sendQuestionToFKClient;

    @Value("${sendquestiontofk.logicaladdress}")
    private String sendQuestionToFkLogicalAddress;

    @Value("${sendanswertofk.logicaladdress}")
    private String sendAnswerToFkLogicalAddress;

    private static FragaSvarSenasteHandelseDatumComparator senasteHandelseDatumComparator = new FragaSvarSenasteHandelseDatumComparator();

    @Override
    public void processIncomingQuestion(FragaSvar fragaSvar) {

        // TODO - validation: does certificate exist

        // persist the question
        fragaSvarRepository.save(fragaSvar);

        // send mail to enhet to inform about new question
        try {
            mailNotificationService.sendMailForIncomingQuestion(fragaSvar);
        } catch (MailSendException | MessagingException e) {
            Long frageId = fragaSvar.getInternReferens();
            String intygsId = fragaSvar.getIntygsReferens().getIntygsId();
            String enhetsId = fragaSvar.getVardperson().getEnhetsId();
            String enhetsNamn = fragaSvar.getVardperson().getEnhetsnamn();
            LOG.error("Notification mail for question '" + frageId
                      +  "' concerning certificate '" + intygsId
                      + "' couldn't be sent to " + enhetsId
                      + " (" + enhetsNamn + "): " + e.getMessage());
        }
    }

    @Override
    public void processIncomingAnswer(Long internId, String svarsText, LocalDateTime svarSigneringsDatum) {

        // lookup question in database
        FragaSvar fragaSvar = fragaSvarRepository.findOne(internId);

        if (fragaSvar == null) {
            throw new IllegalStateException("No question found with internal ID " + internId);
        }

        if (FORSAKRINGSKASSAN.equals(fragaSvar.getFrageStallare())) {
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
        } catch (MailSendException | MessagingException e) {
            Long svarsId = fragaSvar.getInternReferens();
            String intygsId = fragaSvar.getIntygsReferens().getIntygsId();
            String enhetsId = fragaSvar.getVardperson().getEnhetsId();
            String enhetsNamn = fragaSvar.getVardperson().getEnhetsnamn();
            LOG.error("Notification mail for answer '" + svarsId
                    +  "' concerning certificate '" + intygsId
                    + "' couldn't be sent to " + enhetsId
                    + " (" + enhetsNamn + "): " + e.getMessage());
        }
    }

    @Override
    public List<FragaSvar> getFragaSvar(List<String> enhetsHsaIds) {
        List<FragaSvar> result = fragaSvarRepository.findByEnhetsId(enhetsHsaIds);
        if (result != null) {
            // We do the sorting in code, since we need to sort on a derived
            // property and not a direct entity persisted
            // proerty in which case we could have used an order by in the
            // query.
            Collections.sort(result, senasteHandelseDatumComparator);
        }
        return result;
    }

    @Override
    public List<FragaSvar> getFragaSvar(String intygId) {

        List<FragaSvar> fragaSvarList = fragaSvarRepository.findByIntygsReferensIntygsId(intygId);

        WebCertUser user = webCertUserService.getWebCertUser();
        List<String> hsaEnhetIds = user.getIdsOfSelectedVardenhet();

        // Filter questions to that current user only sees questions issued to
        // units with active employment role
        Iterator<FragaSvar> iterator = fragaSvarList.iterator();
        while (iterator.hasNext()) {
            FragaSvar fragaSvar = iterator.next();

            if (!hsaEnhetIds.contains(fragaSvar.getVardperson().getEnhetsId())) {
                iterator.remove();
            }
        }

        // Finally sort by senasteHandelseDatum
        // We do the sorting in code, since we need to sort on a derived
        // property and not a direct entity persisted
        // property in which case we could have used an order by in the query.
        Collections.sort(fragaSvarList, senasteHandelseDatumComparator);
        return fragaSvarList;
    }

    @Override
    public FragaSvar saveSvar(Long fragaSvarsId, String svarsText) {
        // Input sanity check
        if (StringUtils.isEmpty(svarsText)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM,
                    "SvarsText cannot be empty!");
        }

        // Look up entity in repository
        FragaSvar fragaSvar = fragaSvarRepository.findOne(fragaSvarsId);
        if (fragaSvar == null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM,
                    "Could not find FragaSvar with id:" + fragaSvarsId);
        }

        // Is user authorized to save an answer to this question?
        verifyEnhetsAuth(fragaSvar.getVardperson().getEnhetsId());

        if (!fragaSvar.getStatus().equals(Status.PENDING_INTERNAL_ACTION)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "FragaSvar with id "
                    + fragaSvar.getInternReferens().toString() + " has invalid state for saving answer("
                    + fragaSvar.getStatus() + ")");
        }

        // Implement Business Rule FS-007
        if (Amne.PAMINNELSE.equals(fragaSvar.getAmne())) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "FragaSvar with id "
                    + fragaSvar.getInternReferens().toString() + " has invalid Amne(" + fragaSvar.getAmne()
                    + ") for saving answer");
        }

        // Implement Business Rule FS-005, FS-006
        WebCertUser user = webCertUserService.getWebCertUser();
        if (Amne.KOMPLETTERING_AV_LAKARINTYG.equals(fragaSvar.getAmne()) && !user.isLakare()) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM, "FragaSvar with id "
                    + fragaSvar.getInternReferens().toString() + " and amne (" + fragaSvar.getAmne()
                    + ") can only be answered by user that is Lakare");
        }

        LocalDateTime now = new LocalDateTime();
        // Ok, lets save the answer
        fragaSvar.setVardAktorHsaId(user.getHsaId());
        fragaSvar.setVardAktorNamn(user.getNamn());
        fragaSvar.setSvarsText(svarsText);
        fragaSvar.setSvarSkickadDatum(now);
        fragaSvar.setStatus(Status.CLOSED);
        fragaSvar.setSvarSigneringsDatum(now);

        FragaSvar saved = fragaSvarRepository.save(fragaSvar);

        // Send to external party (FK)
        SendMedicalCertificateAnswerType sendType = new SendMedicalCertificateAnswerType();
        AnswerToFkType answer = FKAnswerConverter.convert(saved);

        sendType.setAnswer(answer);
        AttributedURIType logicalAddress = new AttributedURIType();
        logicalAddress.setValue(sendAnswerToFkLogicalAddress);
        SendMedicalCertificateAnswerResponseType response = sendAnswerToFKClient.sendMedicalCertificateAnswer(logicalAddress,
                sendType);
        if (!response.getResult().getResultCode().equals(ResultCodeEnum.OK)) {
            LOG.error("Failed to send answer to FK, result was " + response.getResult().toString());
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.EXTERNAL_SYSTEM_PROBLEM, response.getResult()
                    .getErrorText());
        }
        return saved;

    }

    @Override
    public FragaSvar saveNewQuestion(String intygId, Amne amne, String frageText) {
        // Input sanity check
        if (StringUtils.isEmpty(frageText)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM,
                    "frageText cannot be empty!");
        }

        if (amne == null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Amne cannot be null!");
        } else if (!VALID_VARD_AMNEN.contains(amne)) {
            // Businessrule RE-013
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Invalid Amne " + amne
                    + " for new question from vard!");
        }

        // Fetch from Intygstjansten
        IntygContentHolder externalIntygData = intygService.fetchExternalIntygData(intygId);

        // Get utfardande vardperson
        Vardperson vardPerson = FragaSvarConverter.convert(externalIntygData.getExternalModel().getSkapadAv());

        // Is user authorized to save an answer to this question?
        verifyEnhetsAuth(vardPerson.getEnhetsId());

        // Verksamhetsregel FS-001 (Is the certificate sent to FK)
        if (!isSentToFK(externalIntygData.getMetaData().getStatuses())) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM,
                    "FS-001: Certificate must be sent to FK first before sending question!");
        }

        // Verify that certificate is not revoked
        if (isRevoked(externalIntygData.getMetaData().getStatuses())) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM,
                    "FS-XXX: Cannot save Fraga when certificate is revoked!");
        }

        IntygsReferens intygsReferens = FragaSvarConverter
                .convertToIntygsReferens(externalIntygData.getExternalModel());

        FragaSvar fraga = new FragaSvar();
        fraga.setFrageStallare(WEBCERT);
        fraga.setAmne(amne);
        fraga.setFrageText(frageText);
        LocalDateTime now = new LocalDateTime();
        fraga.setFrageSkickadDatum(now);
        fraga.setFrageSigneringsDatum(now);

        fraga.setIntygsReferens(intygsReferens);
        fraga.setVardperson(vardPerson);
        fraga.setStatus(Status.PENDING_EXTERNAL_ACTION);

        WebCertUser user = webCertUserService.getWebCertUser();
        fraga.setVardAktorHsaId(user.getHsaId());
        fraga.setVardAktorNamn(user.getNamn());
        // Ok, lets save the question
        FragaSvar saved = fragaSvarRepository.save(fraga);

        // Send to external party (FK)
        SendMedicalCertificateQuestionType sendType = new SendMedicalCertificateQuestionType();
        QuestionToFkType question = FKQuestionConverter.convert(saved);
        sendType.setQuestion(question);
        AttributedURIType logicalAddress = new AttributedURIType();
        logicalAddress.setValue(sendQuestionToFkLogicalAddress);
        SendMedicalCertificateQuestionResponseType response = sendQuestionToFKClient.sendMedicalCertificateQuestion(
                logicalAddress, sendType);
        if (!response.getResult().getResultCode().equals(ResultCodeEnum.OK)) {
            LOG.error("Failed to send question to FK, result was " + response.getResult().toString());
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.EXTERNAL_SYSTEM_PROBLEM, response.getResult()
                    .getErrorText());
        }
        return saved;

    }

    private boolean isRevoked(List<IntygStatus> statuses) {
        if (statuses != null) {
            for (IntygStatus status : statuses) {
                if (REVOKED_STATUS_TYPE.equals(status.getType())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSentToFK(List<IntygStatus> statuses) {
        if (statuses != null) {
            for (IntygStatus status : statuses) {
                if (FORSAKRINGSKASSAN.equals(status.getTarget()) && SENT_STATUS_TYPE.equals(status.getType())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public FragaSvar setDispatchState(Long frageSvarId, Boolean isDispatched) {
        // Look up entity in repository
        FragaSvar fragaSvar = fragaSvarRepository.findOne(frageSvarId);
        if (fragaSvar == null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM,
                    "Could not find FragaSvar with id:" + frageSvarId);
        }
        // Set & save new vidarebefordrad state
        fragaSvar.setVidarebefordrad(isDispatched);
        return fragaSvarRepository.save(fragaSvar);
    }

    @Override
    public FragaSvar closeQuestionAsHandled(Long frageSvarId) {
        FragaSvar fragaSvar = fragaSvarRepository.findOne(frageSvarId);
        if (fragaSvar == null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM,
                    "Could not find FragaSvar with id:" + frageSvarId);
        }

        fragaSvar.setStatus(Status.CLOSED);
        return fragaSvarRepository.save(fragaSvar);
    }

    @Override
    public FragaSvar openQuestionAsUnhandled(Long frageSvarId) {
        FragaSvar fragaSvar = fragaSvarRepository.findOne(frageSvarId);
        if (fragaSvar == null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM,
                    "Could not find FragaSvar with id:" + frageSvarId);
        }

        // Enforce business rule FS-011
        if (!WEBCERT.equals(fragaSvar.getFrageStallare())
                && !StringUtils.isEmpty(fragaSvar.getSvarsText())) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                    "FS-011: Cant revert status for question " + frageSvarId);
        }

        if (fragaSvar.getSvarsText() != null && !fragaSvar.getSvarsText().isEmpty()) {
            fragaSvar.setStatus(Status.ANSWERED);
        } else {
            if (fragaSvar.getFrageStallare().equalsIgnoreCase(WEBCERT)) {
                fragaSvar.setStatus(Status.PENDING_EXTERNAL_ACTION);
            } else {
                fragaSvar.setStatus(Status.PENDING_INTERNAL_ACTION);
            }

        }
        return fragaSvarRepository.save(fragaSvar);
    }


    @Override
    public QueryFragaSvarResponse filterFragaSvar(QueryFragaSvarParameter filterParameters) {

        FragaSvarFilter filter = createFragaSvarFilter(filterParameters);
        List<FragaSvar> results = fragaSvarRepository.filterFragaSvar(filter);

        int totalResultsCount = fragaSvarRepository.filterCountFragaSvar(filter);

        QueryFragaSvarResponse response = new QueryFragaSvarResponse();
        response.setResults(results);
        response.setTotalCount(totalResultsCount);

        return response;
    }

    private FragaSvarFilter createFragaSvarFilter(QueryFragaSvarParameter params) {

        FragaSvarFilter filter = new FragaSvarFilter();

        if (StringUtils.isNotEmpty(params.getEnhetId())) {
            verifyEnhetsAuth(params.getEnhetId());
            filter.getEnhetsIds().add(params.getEnhetId());
        } else {
            WebCertUser user = webCertUserService.getWebCertUser();
            filter.getEnhetsIds().addAll(user.getIdsOfSelectedVardenhet());
        }

        if (StringUtils.isNotEmpty(params.getVantarPa())) {
            filter.setVantarPa(VantarPa.valueOf(params.getVantarPa()));
        }

        filter.setChangedFrom(params.getChangedFrom());
        filter.setChangedTo(params.getChangedTo());
        filter.setHsaId(params.getHsaId());
        filter.setQuestionFromFK(getSafeBooleanValue(params.getQuestionFromFK()));
        filter.setQuestionFromWC(getSafeBooleanValue(params.getQuestionFromWC()));
        filter.setReplyLatest(params.getReplyLatest());
        filter.setVidarebefordrad(params.getVidarebefordrad());

        filter.setPageSize(params.getPageSize() == null ? DEFAULT_PAGE_SIZE : params.getPageSize());
        filter.setStartFrom(params.getStartFrom() == null ? 0 : params.getStartFrom());

        return filter;
    }

    private boolean getSafeBooleanValue(Boolean booleanObj) {
        return (booleanObj != null) && booleanObj;
    }

    protected void verifyEnhetsAuth(String enhetsId) {
        if (!webCertUserService.isAuthorizedForUnit(enhetsId)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
                    "User not authorized for for enhet " + enhetsId);
        }

    }

    @Override
    public List<Lakare> getFragaSvarHsaIdByEnhet(String enhetsId) {

        List<String> enhetsIdParams = new ArrayList<>();

        if (enhetsId != null) {
            verifyEnhetsAuth(enhetsId);
            enhetsIdParams.add(enhetsId);
        } else {
            WebCertUser user = webCertUserService.getWebCertUser();
            enhetsIdParams.addAll(user.getIdsOfSelectedVardenhet());
        }

        List<Lakare> mdList = new ArrayList<>();

        List<Object[]> tempList = fragaSvarRepository.findDistinctFragaSvarHsaIdByEnhet(enhetsIdParams);

        for (Object[] obj : tempList) {
            mdList.add(new Lakare((String) obj[0], (String) obj[1]));
        }
        return mdList;
    }

    @Override
    public long getUnhandledFragaSvarForUnitsCount(List<String> vardenheterIds) {
        return fragaSvarRepository.countUnhandledForEnhetsIds(vardenheterIds);
    }

    public Map<String, Long> getNbrOfUnhandledFragaSvarForCareUnits(List<String> vardenheterIds) {

        Map<String, Long> resultsMap = new HashMap<>();

        List<Object[]> results = fragaSvarRepository.countUnhandledGroupedByEnhetIds(vardenheterIds);

        for (Object[] resArr : results) {
            String id = (String) resArr[0];
            Long nbr = (Long) resArr[1];
            resultsMap.put(id, nbr);
        }

        return resultsMap;
    }
}
