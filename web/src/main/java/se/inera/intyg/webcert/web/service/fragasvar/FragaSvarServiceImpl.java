/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

// CHECKSTYLE:OFF LineLength

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.xml.ws.soap.SOAPFaultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3.wsaddressing10.AttributedURIType;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswer.rivtabp20.v1.SendMedicalCertificateAnswerResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.AnswerToFkType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestion.rivtabp20.v1.SendMedicalCertificateQuestionResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.QuestionToFkType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.SendMedicalCertificateQuestionResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.SendMedicalCertificateQuestionType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.GroupableItem;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;
import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.intyg.webcert.persistence.fragasvar.model.Komplettering;
import se.inera.intyg.webcert.persistence.fragasvar.model.Vardperson;
import se.inera.intyg.webcert.persistence.fragasvar.repository.FragaSvarRepository;
import se.inera.intyg.webcert.persistence.model.Filter;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.converter.ArendeListItemConverter;
import se.inera.intyg.webcert.web.converter.FKAnswerConverter;
import se.inera.intyg.webcert.web.converter.FKQuestionConverter;
import se.inera.intyg.webcert.web.converter.FragaSvarConverter;
import se.inera.intyg.webcert.web.converter.util.AnsweredWithIntygUtil;
import se.inera.intyg.webcert.web.converter.util.IntygConverterUtil;
import se.inera.intyg.webcert.web.service.arende.ArendeDraftService;
import se.inera.intyg.webcert.web.service.dto.Lakare;
import se.inera.intyg.webcert.web.service.fragasvar.dto.FrageStallare;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarResponse;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationEvent;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.util.FragaSvarSenasteHandelseDatumComparator;
import se.inera.intyg.webcert.web.service.util.StatisticsGroupByUtil;
import se.inera.intyg.webcert.web.web.controller.api.dto.AnsweredWithIntyg;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;
import se.inera.intyg.webcert.web.web.controller.api.dto.FragaSvarView;

/**
 * @author andreaskaltenbach
 */
@Service
@Transactional
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
    @Value("${fk7263.send.medical.certificate.answer.force.fullstandigtnamn}")
    private String forceFullstandigtNamn;
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
    private AuthoritiesHelper authoritiesHelper;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private MonitoringLogService monitoringService;
    @Autowired
    private UtkastRepository utkastRepository;
    @Autowired
    private ArendeDraftService arendeDraftService;
    @Autowired
    private StatisticsGroupByUtil statisticsGroupByUtil;
    @Autowired
    private PatientDetailsResolver patientDetailsResolver;
    private AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();

    private static Predicate<FragaSvar> isCorrectAmne(Amne amne) {
        return a -> a.getAmne().equals(amne);
    }

    @Override
    public FragaSvar processIncomingQuestion(FragaSvar fragaSvar) {

        validateAcceptsQuestions(fragaSvar);

        monitoringService.logQuestionReceived(fragaSvar.getFrageStallare(),
                ((fragaSvar.getIntygsReferens() == null) ? null : fragaSvar.getIntygsReferens().getIntygsId()),
                fragaSvar.getExternReferens(),
                fragaSvar.getInternReferens(), fragaSvar.getVardAktorHsaId(), fragaSvar.getAmne(),
                fragaSvar.getKompletteringar().stream().map(Komplettering::getFalt).collect(Collectors.toList()));

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

        monitoringService.logAnswerReceived(fragaSvar.getExternReferens(), fragaSvar.getInternReferens(),
                ((fragaSvar.getIntygsReferens() == null) ? null : fragaSvar.getIntygsReferens().getIntygsId()),
                fragaSvar.getVardAktorHsaId(),
                fragaSvar.getAmne());

        // update the FragaSvar
        return fragaSvarRepository.save(fragaSvar);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FragaSvarView> getFragaSvar(String intygId) {

        List<FragaSvar> fragaSvarList = fragaSvarRepository.findByIntygsReferensIntygsId(intygId);

        WebCertUser user = webCertUserService.getUser();
        validateSekretessmarkering(intygId, fragaSvarList, user);

        List<String> hsaEnhetIds = user.getIdsOfSelectedVardenhet();

        // Filter questions to that current user only sees questions issued to
        // units with active employment role
        fragaSvarList
                .removeIf(fragaSvar -> fragaSvar.getVardperson() != null && !hsaEnhetIds.contains(fragaSvar.getVardperson().getEnhetsId()));

        // Finally sort by senasteHandelseDatum
        // We do the sorting in code, since we need to sort on a derived
        // property and not a direct entity persisted
        // property in which case we could have used an order by in the query.
        fragaSvarList.sort(SENASTE_HANDELSE_DATUM_COMPARATOR);

        List<ArendeDraft> drafts = arendeDraftService.listAnswerDrafts(intygId);

        List<AnsweredWithIntyg> bmi = AnsweredWithIntygUtil.findAllKomplementForGivenIntyg(intygId, utkastRepository);
        List<FragaSvarView> fragaSvarWithBesvaratMedIntygInfo = fragaSvarList.stream()
                .map(fs -> FragaSvarView.create(fs,
                        fs.getFrageSkickadDatum() == null ? null
                                : AnsweredWithIntygUtil.returnOldestKompltOlderThan(fs.getFrageSkickadDatum(), bmi),
                        drafts.stream()
                                .filter(d -> Long.toString(fs.getInternReferens()).equals(d.getQuestionId()))
                                .findAny()
                                .map(ArendeDraft::getText)
                                .orElse(null)))
                .collect(Collectors.toList());
        return fragaSvarWithBesvaratMedIntygInfo;
    }

    /**
     * If there is at least one fragaSvar in the response, we fetch the personId and check for sekretessmarkering.
     */
    private void validateSekretessmarkering(String intygsId, List<FragaSvar> fragaSvarList, WebCertUser user) {
        if (fragaSvarList.size() > 0) {

            Personnummer pnr = fragaSvarList.get(0).getIntygsReferens().getPatientId();
            String intygsTyp = fragaSvarList.get(0).getIntygsReferens().getIntygsTyp();
            SekretessStatus sekretessStatus = patientDetailsResolver.getSekretessStatus(pnr);
            if (sekretessStatus == SekretessStatus.UNDEFINED) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.PU_PROBLEM, "Cannot list fraga/svar for '"
                        + intygsId + "'. PU service unavailable or personnummer " + pnr.getPersonnummerHash() + " not valid");
            }

            authoritiesValidator.given(user, intygsTyp)
                    .privilegeIf(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT, sekretessStatus == SekretessStatus.TRUE)
                    .orThrow(new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM_SEKRETESSMARKERING,
                            "User is not allowed to handle sekretessmarkerad patient"));
        }
    }

    @Override
    public FragaSvar saveSvar(Long fragaSvarsId, String svarsText) {

        // Input sanity check
        if (Strings.isNullOrEmpty(svarsText)) {
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

        WebCertUser user = webCertUserService.getUser();
        if (Amne.KOMPLETTERING_AV_LAKARINTYG.equals(fragaSvar.getAmne())) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "FragaSvar with id "
                    + fragaSvar.getInternReferens().toString() + " has invalid Amne(" + fragaSvar.getAmne()
                    + ") for saving answer");
        }

        createSvar(user, svarsText, fragaSvar);

        FragaSvar saved = fragaSvarRepository.save(fragaSvar);

        sendFragaSvarToExternalParty(saved);

        arendeDraftService.delete(fragaSvar.getIntygsReferens().getIntygsId(), Long.toString(fragaSvar.getInternReferens()));

        return saved;
    }

    @Override
    public List<FragaSvarView> answerKomplettering(final String intygsId, final String svarsText) {

        Preconditions.checkArgument(!Strings.isNullOrEmpty(intygsId), "intygsId may not be null or empty");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(svarsText), "svarsText may not be null or empty");

        final List<FragaSvar> fragaSvarList = fragaSvarRepository.findByIntygsReferensIntygsId(intygsId);

        final FragaSvar komplFragaSvar = fragaSvarList.stream()
                .filter(isCorrectAmne(Amne.KOMPLETTERING_AV_LAKARINTYG))
                .max(SENASTE_HANDELSE_DATUM_COMPARATOR)
                .orElseThrow(() -> new IllegalArgumentException("No fragasvar of type KOMPLT exist for intyg: " + intygsId));

        WebCertUser user = webCertUserService.getUser();

        if (!authoritiesValidator.given(user).privilege(AuthoritiesConstants.PRIVILEGE_BESVARA_KOMPLETTERINGSFRAGA).isVerified()) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM, "FragaSvar with id "
                    + komplFragaSvar.getInternReferens().toString() + " and amne (" + Amne.KOMPLETTERING_AV_LAKARINTYG
                    + ") can only be answered by user that is Lakare");
        }

        createSvar(user, svarsText, komplFragaSvar);

        FragaSvar saved = fragaSvarRepository.save(komplFragaSvar);
        sendFragaSvarToExternalParty(saved);

        arendeDraftService.delete(komplFragaSvar.getIntygsReferens().getIntygsId(), Long.toString(komplFragaSvar.getInternReferens()));

        closeCompletionsAsHandled(intygsId);

        return getFragaSvar(intygsId);
    }

    @Override
    public FragaSvar saveNewQuestion(String intygId, String typ, Amne amne, String frageText) {
        // Argument check
        if (Strings.isNullOrEmpty(frageText)) {
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

        WebCertUser user = webCertUserService.getUser();

        // Get vardperson that posed the question

        // Is user authorized to save an answer to this question?
        verifyEnhetsAuth(intyg.getUtlatande().getGrundData().getSkapadAv().getVardenhet().getEnhetsid(), false);
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
        HoSPersonal hoSPersonal = IntygConverterUtil.buildHosPersonalFromWebCertUser(user, null);
        Vardperson vardPerson = FragaSvarConverter.convert(hoSPersonal);

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

        fraga.setVardAktorHsaId(user.getHsaId());
        fraga.setVardAktorNamn(user.getNamn());

        // Ok, lets save the question
        FragaSvar saved = fragaSvarRepository.save(fraga);

        // Send to external party (FK)
        SendMedicalCertificateQuestionType sendType = new SendMedicalCertificateQuestionType();
        QuestionToFkType question = FKQuestionConverter.convert(saved);

        // INTYG-4447: Temporary hack to mitigate problems in Anpassningsplattform requiring fullstandigtNamn to be present.
        // Remove ASAP.
        if ("true".equalsIgnoreCase(forceFullstandigtNamn)) {
            question.getLakarutlatande().getPatient().setFullstandigtNamn("---");
        }

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

        monitoringService.logQuestionSent(saved.getExternReferens(), saved.getInternReferens(),
                (saved.getIntygsReferens() == null) ? null : saved.getIntygsReferens().getIntygsId(), saved.getVardAktorHsaId(),
                saved.getAmne());

        // Notify stakeholders
        sendNotification(saved, NotificationEvent.NEW_QUESTION_FROM_CARE);

        arendeDraftService.delete(intygId, null);

        return saved;
    }

    @Override
    public List<FragaSvar> setVidareBefordrad(final String intygsId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(intygsId), "intygsId may not be null or empty");

        final WebCertUser user = webCertUserService.getUser();

        final List<FragaSvar> fragaSvarList = fragaSvarRepository.findByIntygsReferensIntygsId(intygsId)
                .stream()
                .peek(fs -> authoritiesValidator.given(user, fs.getIntygsReferens().getIntygsTyp())
                        .features(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR)
                        .privilege(AuthoritiesConstants.PRIVILEGE_VIDAREBEFORDRA_FRAGASVAR)
                        .orThrow())
                .peek(FragaSvar::setToVidareBefordrad)
                .collect(Collectors.toList());

        return fragaSvarRepository.save(fragaSvarList);
    }

    @Override
    public FragaSvar closeQuestionAsHandled(Long frageSvarId) {
        return closeQuestionAsHandled(lookupFragaSvar(frageSvarId));
    }

    @Override
    public void closeCompletionsAsHandled(String intygId) {
        List<FragaSvar> completionFragaSvar = fragaSvarRepository.findByIntygsReferensIntygsId(intygId).stream()
                .filter(fs -> Amne.KOMPLETTERING_AV_LAKARINTYG == fs.getAmne()).collect(Collectors.toList());
        for (FragaSvar completion : completionFragaSvar) {
            if (Status.CLOSED != completion.getStatus()) {
                closeQuestionAsHandled(completion);
            }
        }
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
                && !Strings.isNullOrEmpty(fragaSvar.getSvarsText())) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                    "FS-011: Cant revert status for question " + frageSvarId);
        }

        NotificationEvent notificationEvent = determineNotificationEvent(fragaSvar);

        if (!Strings.isNullOrEmpty(fragaSvar.getSvarsText())) {
            fragaSvar.setStatus(Status.ANSWERED);
        } else {
            if (FrageStallare.WEBCERT.isKodEqual(fragaSvar.getFrageStallare())) {
                fragaSvar.setStatus(Status.PENDING_EXTERNAL_ACTION);
            } else {
                fragaSvar.setStatus(Status.PENDING_INTERNAL_ACTION);
            }

        }
        FragaSvar openedFragaSvar = fragaSvarRepository.save(fragaSvar);
        sendNotification(openedFragaSvar, notificationEvent);

        return openedFragaSvar;
    }

    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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

        if (vardenheterIds == null || vardenheterIds.isEmpty()) {
            LOGGER.warn("No ids for Vardenheter was supplied");
            return resultsMap;
        }

        if (intygsTyper == null || intygsTyper.isEmpty()) {
            LOGGER.warn("No intygsTyper for querying FragaSvar was supplied");
            return resultsMap;
        }

        List<GroupableItem> results = fragaSvarRepository.getUnhandledWithEnhetIdsAndIntygstyper(vardenheterIds, intygsTyper);
        return statisticsGroupByUtil.toSekretessFilteredMap(results);
    }

    protected void verifyEnhetsAuth(String enhetsId) {
        if (!webCertUserService.isAuthorizedForUnit(enhetsId, false)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
                    "User not authorized for for enhet " + enhetsId);
        }

    }

    private void verifyEnhetsAuth(String enhetsId, boolean isReadOnlyOperation) {
        if (!webCertUserService.isAuthorizedForUnit(enhetsId, isReadOnlyOperation)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
                    "User not authorized for for enhet " + enhetsId);
        }
    }

    private FragaSvar closeQuestionAsHandled(FragaSvar fragaSvar) {
        NotificationEvent notificationEvent = determineNotificationEvent(fragaSvar);

        fragaSvar.setStatus(Status.CLOSED);
        FragaSvar closedFragaSvar = fragaSvarRepository.save(fragaSvar);
        sendNotification(closedFragaSvar, notificationEvent);

        if (!fragaSvar.getFrageStallare().equals(FrageStallare.WEBCERT.getKod())) {
            arendeDraftService.delete(fragaSvar.getIntygsReferens().getIntygsId(), Long.toString(fragaSvar.getInternReferens()));
        }

        return closedFragaSvar;
    }

    private NotificationEvent determineNotificationEvent(FragaSvar fragaSvar) {

        FrageStallare frageStallare = FrageStallare.getByKod(fragaSvar.getFrageStallare());
        Status fragaSvarStatus = fragaSvar.getStatus();

        if (FrageStallare.FORSAKRINGSKASSAN.equals(frageStallare)) {
            if (Status.PENDING_INTERNAL_ACTION.equals(fragaSvarStatus)) {
                return NotificationEvent.QUESTION_FROM_RECIPIENT_HANDLED;
            } else if (Status.CLOSED.equals(fragaSvarStatus)) {
                return NotificationEvent.QUESTION_FROM_RECIPIENT_UNHANDLED;
            }
        }

        if (FrageStallare.WEBCERT.equals(frageStallare)) {
            if (Status.ANSWERED.equals(fragaSvarStatus)) {
                return NotificationEvent.QUESTION_FROM_CARE_WITH_ANSWER_HANDLED;
            } else if (Status.CLOSED.equals(fragaSvarStatus) && !Strings.isNullOrEmpty(fragaSvar.getSvarsText())) {
                return NotificationEvent.QUESTION_FROM_CARE_WITH_ANSWER_UNHANDLED;
            } else if (Status.CLOSED.equals(fragaSvarStatus)) {
                return NotificationEvent.QUESTION_FROM_CARE_UNHANDLED;
            } else {
                return NotificationEvent.QUESTION_FROM_CARE_HANDLED;
            }
        }

        return null;
    }

    private boolean isCertificateSentToFK(List<se.inera.intyg.common.support.model.Status> statuses) {
        if (statuses != null) {
            for (se.inera.intyg.common.support.model.Status status : statuses) {
                if ("FKASSA".equals(status.getTarget()) && SENT_STATUS_TYPE.equals(status.getType())) {
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
        if (event != null) {
            notificationService.sendNotificationForQAs(fragaSvar.getIntygsReferens().getIntygsId(), event);
        }
    }

    private void validateAcceptsQuestions(FragaSvar fragaSvar) {
        String intygsTyp = fragaSvar.getIntygsReferens().getIntygsTyp();
        if (!authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR, intygsTyp)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.EXTERNAL_SYSTEM_PROBLEM, "Intygstyp '" + intygsTyp
                    + "' stödjer ej fragasvar.");
        }
    }

    private void createSvar(final WebCertUser user, final String svarsText, final FragaSvar fragasvar) {
        final LocalDateTime now = LocalDateTime.now();
        fragasvar.setVardAktorHsaId(user.getHsaId());
        fragasvar.setVardAktorNamn(user.getNamn());
        fragasvar.setSvarsText(svarsText);
        fragasvar.setSvarSkickadDatum(now);
        fragasvar.setStatus(Status.CLOSED);
        fragasvar.setSvarSigneringsDatum(now);
    }

    private void sendFragaSvarToExternalParty(final FragaSvar fragaSvar) {

        // Send to external party (FK)
        SendMedicalCertificateAnswerType sendType = new SendMedicalCertificateAnswerType();

        AnswerToFkType answer = FKAnswerConverter.convert(fragaSvar);
        sendType.setAnswer(answer);

        // INTYG-4447: Temporary hack to mitigate problems in Anpassningsplattform requiring fullstandigtNamn to be present.
        // Remove ASAP.
        if ("true".equalsIgnoreCase(forceFullstandigtNamn)) {
            answer.getLakarutlatande().getPatient().setFullstandigtNamn("---");
        }

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
            LOGGER.error("Failed to send answer to FK, result was " + response.getResult().getErrorText());
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.EXTERNAL_SYSTEM_PROBLEM, response.getResult()
                    .getErrorText());
        }

        monitoringService.logAnswerSent(fragaSvar.getExternReferens(), fragaSvar.getInternReferens(),
                (fragaSvar.getIntygsReferens() == null) ? null : fragaSvar.getIntygsReferens().getIntygsId(), fragaSvar.getVardAktorHsaId(),
                fragaSvar.getAmne());

        // Notify stakeholders
        sendNotification(fragaSvar, NotificationEvent.NEW_ANSWER_FROM_CARE);

    }
}
