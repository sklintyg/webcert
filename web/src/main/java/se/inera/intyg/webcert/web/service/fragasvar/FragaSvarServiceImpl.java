package se.inera.intyg.webcert.web.service.fragasvar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.xml.ws.soap.SOAPFaultException;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.certificate.model.CertificateState;
import se.inera.certificate.modules.support.feature.ModuleFeature;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswer.rivtabp20.v1.SendMedicalCertificateAnswerResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.AnswerToFkType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestion.rivtabp20.v1.SendMedicalCertificateQuestionResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.QuestionToFkType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.SendMedicalCertificateQuestionResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.SendMedicalCertificateQuestionType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.webcert.common.security.authority.UserPrivilege;
import se.inera.intyg.webcert.web.converter.FKAnswerConverter;
import se.inera.intyg.webcert.web.converter.FKQuestionConverter;
import se.inera.intyg.webcert.web.converter.FragaSvarConverter;
import se.inera.webcert.persistence.fragasvar.model.Amne;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.webcert.persistence.fragasvar.model.Status;
import se.inera.webcert.persistence.fragasvar.model.Vardperson;
import se.inera.webcert.persistence.fragasvar.repository.FragaSvarFilter;
import se.inera.webcert.persistence.fragasvar.repository.FragaSvarRepository;
import se.inera.webcert.persistence.fragasvar.repository.VantarPa;
import se.inera.intyg.webcert.web.service.dto.Lakare;
import se.inera.webcert.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.feature.WebcertFeatureService;
import se.inera.intyg.webcert.web.service.fragasvar.dto.FrageStallare;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarParameter;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarResponse;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.util.FragaSvarSenasteHandelseDatumComparator;

/**
 * @author andreaskaltenbach
 */
@Service
@Transactional("jpaTransactionManager")
public class FragaSvarServiceImpl implements FragaSvarService {

    private enum NotificationEvent {
        QUESTION_SENT_TO_FK,
        ANSWER_SENT_TO_FK,
        QUESTION_FROM_FK_HANDLED,
        QUESTION_FROM_FK_UNHANDLED,
        ANSWER_FROM_FK_HANDLED,
        ANSWER_FROM_FK_UNHANDLED;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(FragaSvarServiceImpl.class);

    private static final CertificateState SENT_STATUS_TYPE = CertificateState.SENT;

    private static final List<Amne> VALID_VARD_AMNEN = Arrays.asList(
            Amne.ARBETSTIDSFORLAGGNING,
            Amne.AVSTAMNINGSMOTE,
            Amne.KONTAKT,
            Amne.OVRIGT);

    private static final Integer DEFAULT_PAGE_SIZE = 10;

    private static final FragaSvarSenasteHandelseDatumComparator SENASTE_HANDELSE_DATUM_COMPARATOR = new FragaSvarSenasteHandelseDatumComparator();

    @Value("${sendquestiontofk.logicaladdress}")
    private String sendQuestionToFkLogicalAddress;

    @Value("${sendanswertofk.logicaladdress}")
    private String sendAnswerToFkLogicalAddress;

    @Autowired
    private SendMedicalCertificateAnswerResponderInterface sendAnswerToFKClient;

    @Autowired
    private SendMedicalCertificateQuestionResponderInterface sendQuestionToFKClient;

    @Autowired
    private FragaSvarRepository fragaSvarRepository;

    @Autowired
    private IntygService intygService;

    @Autowired
    private WebCertUserService webCertUserService;

    @Autowired
    private WebcertFeatureService webcertFeatureService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MonitoringLogService monitoringService;

    /* --------------------- Public scope --------------------- */

    @Override
    public FragaSvar processIncomingQuestion(FragaSvar fragaSvar) {

        validateAcceptsQuestions(fragaSvar);

        monitoringService.logQuestionReceived(fragaSvar.getFrageStallare(),
                ((fragaSvar.getIntygsReferens() == null) ? null : fragaSvar.getIntygsReferens().getIntygsId()),
                fragaSvar.getExternReferens(),
                fragaSvar.getInternReferens(),
                fragaSvar.getVardAktorHsaId(),
                ((fragaSvar.getAmne() == null) ? null : fragaSvar.getAmne().toString()));

        // persist the question
        return fragaSvarRepository.save(fragaSvar);
    }

    @Override
    public FragaSvar processIncomingAnswer(Long internId, String svarsText, LocalDateTime svarSigneringsDatum) {

        // lookup question in database
        FragaSvar fragaSvar = fragaSvarRepository.findOne(internId);

        if (fragaSvar == null) {
            throw new IllegalStateException("No question found with internal ID " + internId);
        }

        if (FrageStallare.FORSAKRINGSKASSAN.isKodEqual(fragaSvar.getFrageStallare())) {
            throw new IllegalStateException("Incoming answer refers to question initiated by Försäkringskassan.");
        }

        // TODO - validation: does answer fit to question?

        fragaSvar.setSvarsText(svarsText);
        fragaSvar.setSvarSigneringsDatum(svarSigneringsDatum);
        fragaSvar.setSvarSkickadDatum(new LocalDateTime());
        fragaSvar.setStatus(Status.ANSWERED);

        monitoringService.logAnswerReceived(fragaSvar.getExternReferens(),
                fragaSvar.getInternReferens(),
                ((fragaSvar.getIntygsReferens() == null) ? null : fragaSvar.getIntygsReferens().getIntygsId()),
                fragaSvar.getVardAktorHsaId(),
                ((fragaSvar.getAmne() == null) ? null : fragaSvar.getAmne().toString()));

        // update the FragaSvar
        return fragaSvarRepository.save(fragaSvar);
    }

    @Override
    @Transactional(value = "jpaTransactionManager", readOnly = true)
    public List<FragaSvar> getFragaSvar(List<String> enhetsHsaIds) {
        List<FragaSvar> result = fragaSvarRepository.findByEnhetsId(enhetsHsaIds);
        if (result != null) {
            // We do the sorting in code, since we need to sort on a derived
            // property and not a direct entity persisted
            // proerty in which case we could have used an order by in the
            // query.
            Collections.sort(result, SENASTE_HANDELSE_DATUM_COMPARATOR);
        }
        return result;
    }

    @Override
    @Transactional(value = "jpaTransactionManager", readOnly = true)
    public List<FragaSvar> getFragaSvar(String intygId) {

        List<FragaSvar> fragaSvarList = fragaSvarRepository.findByIntygsReferensIntygsId(intygId);

        WebCertUser user = webCertUserService.getUser();
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
        Collections.sort(fragaSvarList, SENASTE_HANDELSE_DATUM_COMPARATOR);
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
        FragaSvar fragaSvar = lookupFragaSvar(fragaSvarsId);

        // Is user authorized to save an answer to this question?
        verifyEnhetsAuth(fragaSvar.getVardperson().getEnhetsId(), false);

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
        WebCertUser user = webCertUserService.getUser();
        if (Amne.KOMPLETTERING_AV_LAKARINTYG.equals(fragaSvar.getAmne()) && !user.hasPrivilege(UserPrivilege.PRIVILEGE_BESVARA_KOMPLETTERINGSFRAGA)) {
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

        SendMedicalCertificateAnswerResponseType response;
        try {
            response = sendAnswerToFKClient.sendMedicalCertificateAnswer(logicalAddress, sendType);
        } catch (SOAPFaultException e) {
            LOGGER.error("Failed to send answer to FK, error was: " + e.getMessage());
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.EXTERNAL_SYSTEM_PROBLEM, e.getMessage());
        }

        if (!response.getResult().getResultCode().equals(ResultCodeEnum.OK)) {
            LOGGER.error("Failed to send answer to FK, result was " + response.getResult().toString());
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.EXTERNAL_SYSTEM_PROBLEM, response.getResult()
                    .getErrorText());
        }

        monitoringService.logAnswerSent(saved.getExternReferens(),
                saved.getInternReferens(),
                ((saved.getIntygsReferens() == null) ? null : saved.getIntygsReferens().getIntygsId()),
                saved.getVardAktorHsaId(),
                ((saved.getAmne() == null) ? null : saved.getAmne().toString()));

        // Notify stakeholders
        sendNotification(saved, NotificationEvent.ANSWER_SENT_TO_FK);

        return saved;

    }

    @Override
    public FragaSvar saveNewQuestion(String intygId, String typ, Amne amne, String frageText) {
        // Argument check
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

        // Fetch from Intygstjansten. Note that if Intygstjansten is unresponsive, the Intyg will be loaded from WebCert
        // if possible.
        IntygContentHolder intyg = intygService.fetchIntygData(intygId, typ);

        // Get utfardande vardperson
        Vardperson vardPerson = FragaSvarConverter.convert(intyg.getUtlatande().getGrundData().getSkapadAv());

        // Is user authorized to save an answer to this question?
        verifyEnhetsAuth(vardPerson.getEnhetsId(), false);
        // Verksamhetsregel FS-001 (Is the certificate sent to FK)
        if (!isCertificateSentToFK(intyg.getStatuses())) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM,
                    "FS-001: Certificate must be sent to FK first before sending question!");
        }

        // Verify that certificate is not revoked
        if (intyg.isRevoked()) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM,
                    "FS-XXX: Cannot save Fraga when certificate is revoked!");
        }

        IntygsReferens intygsReferens = FragaSvarConverter.convertToIntygsReferens(intyg.getUtlatande());

        FragaSvar fraga = new FragaSvar();
        fraga.setFrageStallare(FrageStallare.WEBCERT.getKod());
        fraga.setAmne(amne);
        fraga.setFrageText(frageText);
        LocalDateTime now = new LocalDateTime();
        fraga.setFrageSkickadDatum(now);
        fraga.setFrageSigneringsDatum(now);

        fraga.setIntygsReferens(intygsReferens);
        fraga.setVardperson(vardPerson);
        fraga.setStatus(Status.PENDING_EXTERNAL_ACTION);

        WebCertUser user = webCertUserService.getUser();
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

        SendMedicalCertificateQuestionResponseType response;
        try {
            response = sendQuestionToFKClient.sendMedicalCertificateQuestion(logicalAddress, sendType);
        } catch (SOAPFaultException e) {
            LOGGER.error("Failed to send question to FK, error was: " + e.getMessage());
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.EXTERNAL_SYSTEM_PROBLEM, e.getMessage());
        }

        if (!response.getResult().getResultCode().equals(ResultCodeEnum.OK)) {
            LOGGER.error("Failed to send question to FK, result was " + response.getResult().toString());
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.EXTERNAL_SYSTEM_PROBLEM, response.getResult()
                    .getErrorText());
        }

        monitoringService.logQuestionSent(fraga.getExternReferens(),
                fraga.getInternReferens(),
                ((fraga.getIntygsReferens() == null) ? null : fraga.getIntygsReferens().getIntygsId()),
                fraga.getVardAktorHsaId(),
                ((fraga.getAmne() == null) ? null : fraga.getAmne().toString()));

        // Notify stakeholders
        sendNotification(saved, NotificationEvent.QUESTION_SENT_TO_FK);

        return saved;
    }

    @Override
    public FragaSvar setDispatchState(Long frageSvarId, Boolean isDispatched) {
        // Look up entity in repository
        FragaSvar fragaSvar = lookupFragaSvar(frageSvarId);
        // Set & save new vidarebefordrad state
        fragaSvar.setVidarebefordrad(isDispatched);
        return fragaSvarRepository.save(fragaSvar);
    }

    @Override
    public FragaSvar closeQuestionAsHandled(Long frageSvarId) {
        FragaSvar fragaSvar = lookupFragaSvar(frageSvarId);
        NotificationEvent notificationEvent = determineNotificationEvent(fragaSvar);

        FragaSvar closedFragaSvar = closeQuestionAsHandled(fragaSvar);

        if (notificationEvent != null) {
            sendNotification(closedFragaSvar, notificationEvent);
        }

        return closedFragaSvar;
    }

    /**
     * Looks upp all questions related to a specific certificate and
     * sets a question's status to CLOSED if not already closed.
     *
     * @param intygsId
     *            the certificates unique identifier
     * @return an array with FragaSvar objects whose status has been set to closed
     */
    @Override
    public FragaSvar[] closeAllNonClosedQuestions(String intygsId) {

        List<FragaSvar> list = fragaSvarRepository.findByIntygsReferensIntygsId(intygsId);
        ListIterator<FragaSvar> iterator = list.listIterator();

        List<FragaSvar> al = new ArrayList<>();

        while (iterator.hasNext()) {
            FragaSvar fragaSvar = iterator.next();
            if (fragaSvar.getStatus() != Status.CLOSED) {
                al.add(closeQuestionAsHandled(fragaSvar));
            }
        }

        if (al.isEmpty()) {
            return new FragaSvar[0];
        }

        return al.toArray(new FragaSvar[al.size()]);
    }

    @Override
    public FragaSvar openQuestionAsUnhandled(Long frageSvarId) {
        FragaSvar fragaSvar = lookupFragaSvar(frageSvarId);

        // Enforce business rule FS-011, from FK + answer should remain closed
        if (!FrageStallare.WEBCERT.isKodEqual(fragaSvar.getFrageStallare())
                && StringUtils.isNotEmpty(fragaSvar.getSvarsText())) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                    "FS-011: Cant revert status for question " + frageSvarId);
        }

        NotificationEvent notificationEvent = determineNotificationEvent(fragaSvar);

        if (StringUtils.isNotEmpty(fragaSvar.getSvarsText())) {
            fragaSvar.setStatus(Status.ANSWERED);
        } else {
            if (FrageStallare.WEBCERT.isKodEqual(fragaSvar.getFrageStallare())) {
                fragaSvar.setStatus(Status.PENDING_EXTERNAL_ACTION);
            } else {
                fragaSvar.setStatus(Status.PENDING_INTERNAL_ACTION);
            }

        }
        FragaSvar openedFragaSvar = fragaSvarRepository.save(fragaSvar);

        if (notificationEvent != null) {
            sendNotification(openedFragaSvar, notificationEvent);
        }

        return openedFragaSvar;
    }

    @Override
    @Transactional(value = "jpaTransactionManager", readOnly = true)
    public QueryFragaSvarResponse filterFragaSvar(QueryFragaSvarParameter filterParameters) {

        FragaSvarFilter filter = createFragaSvarFilter(filterParameters);
        List<FragaSvar> results = fragaSvarRepository.filterFragaSvar(filter);

        int totalResultsCount = fragaSvarRepository.filterCountFragaSvar(filter);

        QueryFragaSvarResponse response = new QueryFragaSvarResponse();
        response.setResults(results);
        response.setTotalCount(totalResultsCount);

        return response;
    }

    @Override
    @Transactional(value = "jpaTransactionManager", readOnly = true)
    public List<Lakare> getFragaSvarHsaIdByEnhet(String enhetsId) {

        List<String> enhetsIdParams = new ArrayList<>();

        if (enhetsId != null) {
            verifyEnhetsAuth(enhetsId);
            enhetsIdParams.add(enhetsId);
        } else {
            WebCertUser user = webCertUserService.getUser();
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
    @Transactional(value = "jpaTransactionManager", readOnly = true)
    public long getUnhandledFragaSvarForUnitsCount(List<String> vardenheterIds) {
        return fragaSvarRepository.countUnhandledForEnhetsIds(vardenheterIds);
    }

    @Override
    public Map<String, Long> getNbrOfUnhandledFragaSvarForCareUnits(List<String> vardenheterIds) {

        Map<String, Long> resultsMap = new HashMap<>();

        if ((vardenheterIds == null) || vardenheterIds.isEmpty()) {
            LOGGER.warn("No ids for Vardenheter was supplied");
            return resultsMap;
        }

        List<Object[]> results = fragaSvarRepository.countUnhandledGroupedByEnhetIds(vardenheterIds);

        for (Object[] resArr : results) {
            String id = (String) resArr[0];
            Long nbr = (Long) resArr[1];
            resultsMap.put(id, nbr);
        }

        return resultsMap;
    }

    /* --------------------- Protected scope --------------------- */

    protected void verifyEnhetsAuth(String enhetsId) {
        if (!webCertUserService.isAuthorizedForUnit(enhetsId, false)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
                    "User not authorized for for enhet " + enhetsId);
        }

    }

    protected void verifyEnhetsAuth(String enhetsId, boolean isReadOnlyOperation) {
        if (!webCertUserService.isAuthorizedForUnit(enhetsId, isReadOnlyOperation)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
                    "User not authorized for for enhet " + enhetsId);
        }
    }

    /* --------------------- Private scope --------------------- */

    private FragaSvar closeQuestionAsHandled(FragaSvar fragaSvar) {
        fragaSvar.setStatus(Status.CLOSED);
        return fragaSvarRepository.save(fragaSvar);
    }

    private FragaSvarFilter createFragaSvarFilter(QueryFragaSvarParameter params) {

        FragaSvarFilter filter = new FragaSvarFilter();

        if (StringUtils.isNotEmpty(params.getEnhetId())) {
            verifyEnhetsAuth(params.getEnhetId(), true);
            filter.getEnhetsIds().add(params.getEnhetId());
        } else {
            WebCertUser user = webCertUserService.getUser();
            filter.getEnhetsIds().addAll(user.getIdsOfSelectedVardenhet());
        }

        if (StringUtils.isNotEmpty(params.getVantarPa())) {
            filter.setVantarPa(VantarPa.valueOf(params.getVantarPa()));
        }

        filter.setChangedFrom(params.getChangedFrom());
        if (params.getChangedTo() != null) {
            filter.setChangedTo(params.getChangedTo().plusDays(1));
        }
        filter.setHsaId(params.getHsaId());
        filter.setQuestionFromFK(getSafeBooleanValue(params.getQuestionFromFK()));
        filter.setQuestionFromWC(getSafeBooleanValue(params.getQuestionFromWC()));
        filter.setReplyLatest(params.getReplyLatest());
        filter.setVidarebefordrad(params.getVidarebefordrad());

        filter.setPageSize((params.getPageSize() == null) ? DEFAULT_PAGE_SIZE : params.getPageSize());
        filter.setStartFrom((params.getStartFrom() == null) ? Integer.valueOf(0) : params.getStartFrom());

        return filter;
    }

    private NotificationEvent determineNotificationEvent(FragaSvar fragaSvar) {

        FrageStallare frageStallare = FrageStallare.getByKod(fragaSvar.getFrageStallare());
        Status fragaSvarStatus = fragaSvar.getStatus();

        if (FrageStallare.FORSAKRINGSKASSAN.equals(frageStallare)) {
            if (Status.PENDING_INTERNAL_ACTION.equals(fragaSvarStatus)) {
                return NotificationEvent.QUESTION_FROM_FK_HANDLED;
            } else if (Status.CLOSED.equals(fragaSvarStatus)) {
                return NotificationEvent.QUESTION_FROM_FK_UNHANDLED;
            }
        }

        if (FrageStallare.WEBCERT.equals(frageStallare)) {
            if (Status.ANSWERED.equals(fragaSvarStatus)) {
                return NotificationEvent.ANSWER_FROM_FK_HANDLED;
            } else if (Status.CLOSED.equals(fragaSvarStatus) && StringUtils.isNotEmpty(fragaSvar.getSvarsText())) {
                return NotificationEvent.ANSWER_FROM_FK_UNHANDLED;
            }
        }

        return null;
    }

    private boolean getSafeBooleanValue(Boolean booleanObj) {
        return (booleanObj != null) && booleanObj;
    }

    private boolean isCertificateSentToFK(List<se.inera.certificate.model.Status> statuses) {
        if (statuses != null) {
            for (se.inera.certificate.model.Status status : statuses) {
                if (FrageStallare.FORSAKRINGSKASSAN.isKodEqual(status.getTarget()) && SENT_STATUS_TYPE.equals(status.getType())) {
                    return true;
                }
            }
        }
        return false;
    }

    private FragaSvar lookupFragaSvar(Long fragaSvarId) {
        FragaSvar fragaSvar = fragaSvarRepository.findOne(fragaSvarId);
        if (fragaSvar == null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM,
                    "Could not find FragaSvar with id:" + fragaSvarId);
        }
        return fragaSvar;
    }

    private void sendNotification(FragaSvar fragaSvar, NotificationEvent event) {

        switch (event) {
        case ANSWER_FROM_FK_HANDLED:
            notificationService.sendNotificationForAnswerHandled(fragaSvar);
            break;
        case ANSWER_FROM_FK_UNHANDLED:
            notificationService.sendNotificationForAnswerRecieved(fragaSvar);
            break;
        case ANSWER_SENT_TO_FK:
            notificationService.sendNotificationForQuestionHandled(fragaSvar);
            break;
        case QUESTION_FROM_FK_HANDLED:
            notificationService.sendNotificationForQuestionHandled(fragaSvar);
            break;
        case QUESTION_FROM_FK_UNHANDLED:
            notificationService.sendNotificationForQuestionReceived(fragaSvar);
            break;
        case QUESTION_SENT_TO_FK:
            notificationService.sendNotificationForQuestionSent(fragaSvar);
            break;
        default:
            LOGGER.warn("FragaSvarServiceImpl.sendNotification(FragaSvar, NotificationEvent) - cannot send notification. Incoming event not handled!");
        }

    }

    private void validateAcceptsQuestions(FragaSvar fragaSvar) {
        String intygsTyp = fragaSvar.getIntygsReferens().getIntygsTyp();
        if (!webcertFeatureService.isModuleFeatureActive(ModuleFeature.HANTERA_FRAGOR, intygsTyp)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.EXTERNAL_SYSTEM_PROBLEM, "Intygstyp '" + intygsTyp
                    + "' stödjer ej fragasvar.");
        }
    }
}
