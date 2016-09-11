/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.webcert.web.service.fragasvar;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import javax.xml.ws.soap.SOAPFaultException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswer.rivtabp20.v1.SendMedicalCertificateAnswerResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.*;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestion.rivtabp20.v1.SendMedicalCertificateQuestionResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.*;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.intyg.common.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.common.security.common.model.AuthoritiesConstants;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.modules.support.feature.ModuleFeature;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.fragasvar.model.*;
import se.inera.intyg.webcert.persistence.fragasvar.repository.FragaSvarRepository;
import se.inera.intyg.webcert.persistence.model.Filter;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.converter.*;
import se.inera.intyg.webcert.web.service.dto.Lakare;
import se.inera.intyg.webcert.web.service.feature.WebcertFeatureService;
import se.inera.intyg.webcert.web.service.fragasvar.dto.FrageStallare;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarResponse;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationEvent;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.util.FragaSvarSenasteHandelseDatumComparator;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;

/**
 * @author andreaskaltenbach
 */
@Service
@Transactional("jpaTransactionManager")
public class FragaSvarServiceImpl implements FragaSvarService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FragaSvarServiceImpl.class);

    private static final CertificateState SENT_STATUS_TYPE = CertificateState.SENT;

    private static final List<Amne> VALID_VARD_AMNEN = Arrays.asList(
            Amne.ARBETSTIDSFORLAGGNING,
            Amne.AVSTAMNINGSMOTE,
            Amne.KONTAKT,
            Amne.OVRIGT);

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

        fragaSvar.setSvarsText(svarsText);
        fragaSvar.setSvarSigneringsDatum(svarSigneringsDatum);
        fragaSvar.setSvarSkickadDatum(LocalDateTime.now());
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

            if (fragaSvar.getVardperson() != null && !hsaEnhetIds.contains(fragaSvar.getVardperson().getEnhetsId())) {
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

        AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();

        // Implement Business Rule FS-005, FS-006
        WebCertUser user = webCertUserService.getUser();
        if (Amne.KOMPLETTERING_AV_LAKARINTYG.equals(fragaSvar.getAmne())
                && !authoritiesValidator.given(user).privilege(AuthoritiesConstants.PRIVILEGE_BESVARA_KOMPLETTERINGSFRAGA).isVerified()) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM, "FragaSvar with id "
                    + fragaSvar.getInternReferens().toString() + " and amne (" + fragaSvar.getAmne()
                    + ") can only be answered by user that is Lakare");
        }

        LocalDateTime now = LocalDateTime.now();

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
        IntygContentHolder intyg = intygService.fetchIntygData(intygId, typ, false);

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
        LocalDateTime now = LocalDateTime.now();
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
        return closeQuestionAsHandled(lookupFragaSvar(frageSvarId));
    }

    /**
     * Looks upp all questions related to a specific certificate and
     * sets a question's status to CLOSED if not already closed.
     *
     * @param intygsId
     *            the certificates unique identifier
     */
    @Override
    public void closeAllNonClosedQuestions(String intygsId) {

        List<FragaSvar> list = fragaSvarRepository.findByIntygsReferensIntygsId(intygsId);

        for (FragaSvar fragaSvar : list) {
            if (fragaSvar.getStatus() != Status.CLOSED) {
                closeQuestionAsHandled(fragaSvar);
            }
        }
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
    public QueryFragaSvarResponse filterFragaSvar(Filter filter) {
        List<ArendeListItem> results = fragaSvarRepository.filterFragaSvar(filter).stream()
                .map(ArendeListItemConverter::convert)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

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
    public Map<String, Long> getNbrOfUnhandledFragaSvarForCareUnits(List<String> vardenheterIds, Set<String> intygsTyper) {

        Map<String, Long> resultsMap = new HashMap<>();

        if ((vardenheterIds == null) || vardenheterIds.isEmpty()) {
            LOGGER.warn("No ids for Vardenheter was supplied");
            return resultsMap;
        }

        if (intygsTyper == null || intygsTyper.isEmpty()) {
            LOGGER.warn("No intygsTyper for querying FragaSvar was supplied");
            return resultsMap;
        }

        List<Object[]> results = fragaSvarRepository.countUnhandledGroupedByEnhetIdsAndIntygstyper(vardenheterIds, intygsTyper);

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
        NotificationEvent notificationEvent = determineNotificationEvent(fragaSvar);

        fragaSvar.setStatus(Status.CLOSED);
        FragaSvar closedFragaSvar = fragaSvarRepository.save(fragaSvar);

        if (notificationEvent != null) {
            sendNotification(closedFragaSvar, notificationEvent);
        }
        return closedFragaSvar;
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

    private boolean isCertificateSentToFK(List<se.inera.intyg.common.support.model.Status> statuses) {
        if (statuses != null) {
            for (se.inera.intyg.common.support.model.Status status : statuses) {
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
            LOGGER.warn(
                    "FragaSvarServiceImpl.sendNotification(FragaSvar, NotificationEvent) - cannot send notification. Incoming event not handled!");
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
