/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.arende;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.MarshallingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.ts_bas.support.TsBasEntryPoint;
import se.inera.intyg.common.ts_diabetes.support.TsDiabetesEntryPoint;
import se.inera.intyg.infra.integration.hsa.services.HsaEmployeeService;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.client.converter.SendMessageToRecipientTypeConverter;
import se.inera.intyg.webcert.common.model.GroupableItem;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;
import se.inera.intyg.webcert.persistence.arende.model.MedicinsktArende;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeRepository;
import se.inera.intyg.webcert.persistence.model.Filter;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.converter.ArendeConverter;
import se.inera.intyg.webcert.web.converter.ArendeListItemConverter;
import se.inera.intyg.webcert.web.converter.ArendeViewConverter;
import se.inera.intyg.webcert.web.converter.FilterConverter;
import se.inera.intyg.webcert.web.converter.util.AnsweredWithIntygUtil;
import se.inera.intyg.webcert.web.integration.builders.SendMessageToRecipientTypeBuilder;
import se.inera.intyg.webcert.web.service.access.AccessResult;
import se.inera.intyg.webcert.web.service.access.CertificateAccessService;
import se.inera.intyg.webcert.web.service.certificatesender.CertificateSenderException;
import se.inera.intyg.webcert.web.service.certificatesender.CertificateSenderService;
import se.inera.intyg.webcert.web.service.dto.Lakare;
import se.inera.intyg.webcert.web.service.fragasvar.FragaSvarService;
import se.inera.intyg.webcert.web.service.fragasvar.dto.FrageStallare;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarParameter;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarResponse;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.converter.IntygModuleFacade;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationEvent;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.util.StatisticsGroupByUtil;
import se.inera.intyg.webcert.web.web.controller.api.dto.AnsweredWithIntyg;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeConversationView;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygTypeInfo;
import se.inera.intyg.webcert.web.web.util.access.AccessResultExceptionHelper;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientType;

@Service
@Transactional
public class ArendeServiceImpl implements ArendeService {

    private static final String MAKULERING = "MAKULERING";
    private static final String PAMINNELSE = "PAMINNELSE";
    private static final String PAMINN = "PAMINN";
    private static final String KOMPLETTERING_AV_LAKARINTYG = "KOMPLETTERING_AV_LAKARINTYG";
    private static final String KOMPLT = "KOMPLT";

    private static final String INGET = "Inget";
    private static final String HANTERAT_AV_FK = "Hanterat ärende";
    private static final String HANTERAT_AV_ANNAT = "Läs inkommet svar";
    private static final String KOMPLETTERA = "Komplettera";
    private static final String SVARA = "Svara";
    private static final String INVANTA_SVAR = "Invänta svar";

    private static final Logger LOG = LoggerFactory.getLogger(ArendeServiceImpl.class);

    private static final List<String> BLACKLISTED = Arrays.asList(Fk7263EntryPoint.MODULE_ID, TsBasEntryPoint.MODULE_ID,
        TsDiabetesEntryPoint.MODULE_ID);

    private static final List<ArendeAmne> VALID_VARD_AMNEN = Arrays.asList(
        ArendeAmne.AVSTMN,
        ArendeAmne.KONTKT,
        ArendeAmne.OVRIGT);

    private Clock systemClock = Clock.systemDefaultZone();

    private Comparator<Arende> byTimestamp = (left, right) -> left.getTimestamp().isBefore(right.getTimestamp()) ? -1 : 1;

    @Value("${sendmessagetofk.logicaladdress}")
    private String sendMessageToFKLogicalAddress;
    @Autowired
    private ArendeRepository arendeRepository;
    @Autowired
    private UtkastRepository utkastRepository;
    @Autowired
    private WebCertUserService webcertUserService;
    @Autowired
    private AuthoritiesHelper authoritiesHelper;
    @Autowired
    private MonitoringLogService monitoringLog;
    @Autowired
    private ArendeViewConverter arendeViewConverter;
    @Autowired
    private HsaEmployeeService hsaEmployeeService;
    @Autowired
    private FragaSvarService fragaSvarService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private CertificateSenderService certificateSenderService;
    @Autowired
    private ArendeDraftService arendeDraftService;
    @Autowired
    private PatientDetailsResolver patientDetailsResolver;
    @Autowired
    private StatisticsGroupByUtil statisticsGroupByUtil;
    @Autowired
    private IntygModuleFacade modelFacade;
    @Autowired
    private CertificateAccessService certificateAccessService;
    @Autowired
    private AccessResultExceptionHelper accessResultExceptionHelper;
    @Autowired
    private IntygService intygService;

    private AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();

    private static Predicate<Arende> isQuestion() {
        return a -> a.getSvarPaId() == null;
    }

    private static Predicate<Arende> isCorrectEnhet(WebCertUser user) {
        return a -> user.getIdsOfSelectedVardenhet().contains(a.getEnhetId());
    }

    private static Predicate<Arende> isCorrectAmne(ArendeAmne arendeAmne) {
        return a -> a.getAmne().equals(arendeAmne);
    }

    @Override
    public Arende processIncomingMessage(Arende arende) {
        if (arendeRepository.findOneByMeddelandeId(arende.getMeddelandeId()) != null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "meddelandeId not unique");
        }

        if (arende.getSvarPaId() != null && !arendeRepository.findBySvarPaId(arende.getSvarPaId()).isEmpty()) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "answer already exist for this message");
        }

        Utkast utkast = utkastRepository.findOne(arende.getIntygsId());
        validateArende(arende.getIntygsId(), utkast);

        ArendeConverter.decorateArendeFromUtkast(arende, utkast, LocalDateTime.now(systemClock), hsaEmployeeService);

        updateSenasteHandelseAndStatusForRelatedArende(arende);

        monitoringLog.logArendeReceived(arende.getIntygsId(), utkast.getIntygsTyp(), utkast.getEnhetsId(), arende.getAmne(),
            arende.getKomplettering().stream().map(MedicinsktArende::getFrageId).collect(Collectors.toList()),
            arende.getSvarPaId() != null);

        Arende saved = arendeRepository.save(arende);

        if (ArendeAmne.PAMINN == saved.getAmne() || saved.getSvarPaId() == null) {
            notificationService.sendNotificationForQuestionReceived(saved);
        } else {
            notificationService.sendNotificationForAnswerRecieved(saved);
        }
        return saved;
    }

    @Override
    public ArendeConversationView createMessage(String intygId, ArendeAmne amne, String rubrik, String meddelande) {
        if (!VALID_VARD_AMNEN.contains(amne)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Invalid Amne " + amne
                + " for new question from vard!");
        }
        Utkast utkast = utkastRepository.findOne(intygId);

        validateArende(intygId, utkast);

        validateAccessRightsToCreateQuestion(utkast);

        Arende arende = ArendeConverter.createArendeFromUtkast(amne, rubrik, meddelande, utkast, LocalDateTime.now(systemClock),
            webcertUserService.getUser().getNamn(), hsaEmployeeService);

        Arende saved = processOutgoingMessage(arende, NotificationEvent.NEW_QUESTION_FROM_CARE);

        arendeDraftService.delete(intygId, null);
        return arendeViewConverter.convertToArendeConversationView(saved, null, null, new ArrayList<>(), null);
    }

    @Override
    public ArendeConversationView answer(String svarPaMeddelandeId, String meddelande) {
        if (Strings.isNullOrEmpty(meddelande)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM,
                "SvarsText cannot be empty!");
        }
        Arende svarPaMeddelande = lookupArende(svarPaMeddelandeId);

        validateAccessRightsToAnswerComplement(svarPaMeddelande.getIntygsId(), false);

        if (!Status.PENDING_INTERNAL_ACTION.equals(svarPaMeddelande.getStatus())) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Arende with id "
                + svarPaMeddelandeId + " has invalid state for saving answer("
                + svarPaMeddelande.getStatus() + ")");
        }

        // Implement Business Rule FS-007
        if (ArendeAmne.PAMINN.equals(svarPaMeddelande.getAmne())) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Arende with id "
                + svarPaMeddelandeId + " has invalid Amne(" + svarPaMeddelande.getAmne()
                + ") for saving answer");
        }

        if (ArendeAmne.KOMPLT.equals(svarPaMeddelande.getAmne())) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Arende with id "
                + svarPaMeddelandeId + " has invalid Amne(" + svarPaMeddelande.getAmne()
                + ") for saving answer");
        }

        Arende arende = ArendeConverter.createAnswerFromArende(meddelande, svarPaMeddelande, LocalDateTime.now(systemClock),
            webcertUserService.getUser().getNamn());

        Arende saved = processOutgoingMessage(arende, NotificationEvent.NEW_ANSWER_FROM_CARE);

        arendeDraftService.delete(svarPaMeddelande.getIntygsId(), svarPaMeddelandeId);
        return arendeViewConverter.convertToArendeConversationView(svarPaMeddelande, saved, null,
            arendeRepository.findByPaminnelseMeddelandeId(svarPaMeddelandeId), null);
    }

    @Override
    @Transactional
    public List<ArendeConversationView> answerKomplettering(final String intygsId, final String meddelande) {

        Preconditions.checkArgument(!Strings.isNullOrEmpty(intygsId));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(meddelande));

        WebCertUser user = webcertUserService.getUser();

        List<Arende> allArende = getArendeForIntygId(intygsId, user);
        List<Arende> arendeList = filterKompletteringar(allArende);

        Arende latestKomplArende = getLatestKomplArende(intygsId, arendeList);

        validateAccessRightsToAnswerComplement(intygsId, false);

        // Close all Arende for intyg, _except_ question from recipient (latestKomplArende) which we handle separately below.
        arendeList.stream()
            .filter(arende -> !Objects.equals(arende.getMeddelandeId(), latestKomplArende.getMeddelandeId()))
            .forEach(this::closeArendeAsHandled);

        Arende answer = ArendeConverter.createAnswerFromArende(
            meddelande,
            latestKomplArende,
            LocalDateTime.now(systemClock),
            user.getNamn());

        arendeDraftService.delete(latestKomplArende.getIntygsId(), latestKomplArende.getMeddelandeId());
        // processOutgoingMessage modifies latestKomplArende in arendeList, which is invalidated from here on.
        Arende saved = processOutgoingMessage(answer, NotificationEvent.NEW_ANSWER_FROM_CARE);

        allArende.add(saved);

        return getArendeConversationViewList(intygsId, allArende);
    }

    private Arende getLatestKomplArende(String intygsId, List<Arende> arendeList) {
        return arendeList
            .stream()
            .max(byTimestamp)
            .orElseThrow(() -> new IllegalArgumentException("No arende of type KOMPLT exist for intyg: " + intygsId));
    }

    private List<Arende> filterKompletteringar(List<Arende> list) {
        return list
            .stream()
            .filter(isQuestion())
            .filter(isCorrectAmne(ArendeAmne.KOMPLT))
            .collect(Collectors.toList());
    }

    private List<Arende> getArendeForIntygId(String intygsId, WebCertUser user) {
        return arendeRepository.findByIntygsId(intygsId);
    }

    @Override
    @Transactional
    public List<ArendeConversationView> setForwarded(String intygsId) {

        WebCertUser user = webcertUserService.getUser();

        List<Arende> allArende = arendeRepository.findByIntygsId(intygsId);

        validateAccessRightsToForwardQuestions(intygsId);

        List<Arende> arendenToForward = arendeRepository.save(
            allArende
                .stream()
                .filter(isCorrectEnhet(user))
                .peek(Arende::setArendeToVidareBerordrat)
                .collect(Collectors.toList()));

        if (arendenToForward.isEmpty()) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
                "Could not find any arende related to IntygsId: " + intygsId);
        }

        return getArendeConversationViewList(intygsId, allArende);
    }

    @Override
    public ArendeConversationView openArendeAsUnhandled(String meddelandeId) {

        Arende arende = lookupArende(meddelandeId);

        validateAccessRightsToAnswerComplement(arende.getIntygsId(), false);

        boolean arendeIsAnswered = !arendeRepository.findBySvarPaId(meddelandeId).isEmpty();

        // Enforce business rule FS-011, from FK + answer should remain closed
        if (!FrageStallare.WEBCERT.isKodEqual(arende.getSkickatAv())
            && arendeIsAnswered) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                "FS-011: Cant revert status for question " + meddelandeId);
        }

        NotificationEvent notificationEvent = determineNotificationEvent(arende, arendeIsAnswered);

        if (arendeIsAnswered) {
            arende.setStatus(Status.ANSWERED);
        } else {
            if (FrageStallare.WEBCERT.isKodEqual(arende.getSkickatAv())) {
                arende.setStatus(Status.PENDING_EXTERNAL_ACTION);
            } else {
                arende.setStatus(Status.PENDING_INTERNAL_ACTION);
            }
        }
        Arende openedArende = arendeRepository.save(arende);

        sendNotification(openedArende, notificationEvent);

        return arendeViewConverter.convertToArendeConversationView(openedArende,
            arendeRepository.findBySvarPaId(meddelandeId).stream().findFirst().orElse(null),
            null,
            arendeRepository.findByPaminnelseMeddelandeId(meddelandeId),
            null);
    }

    @Override
    public List<Lakare> listSignedByForUnits(String enhetsId) {

        List<String> enhetsIdParams = new ArrayList<>();
        if (enhetsId != null) {
            verifyEnhetsAuth(enhetsId, true);
            enhetsIdParams.add(enhetsId);
        } else {
            enhetsIdParams.addAll(webcertUserService.getUser().getIdsOfSelectedVardenhet());
        }

        List<String> arendeListHsaId = arendeRepository.findSigneratAvByEnhet(enhetsIdParams).stream()
            .map(arr -> (String) arr[0])
            .collect(Collectors.toList());

        // We need to maintain backwards compatibility. When FragaSvar no longer exist remove this part and return above
        // arendeList
        List<String> fragaSvarListHsaId = fragaSvarService.getFragaSvarHsaIdByEnhet(enhetsId).stream()
            .map(Lakare::getHsaId)
            .collect(Collectors.toList());

        Set<String> hsaIds = new HashSet<>(arendeListHsaId);
        hsaIds.addAll(fragaSvarListHsaId);

        return hsaIds.stream()
            .map(hsaId -> new Lakare(hsaId, getLakareName(hsaId)))
            .sorted(Comparator.comparing(Lakare::getName))
            .collect(Collectors.toList());
    }

    private String getLakareName(String hsaId) {
        return ArendeConverter.getNameByHsaId(hsaId, hsaEmployeeService);
    }

    @Override
    public List<ArendeConversationView> getArenden(String intygsId) {
        WebCertUser user = webcertUserService.getUser();
        List<Arende> arendeList = getArendeForIntygId(intygsId, user);

        validateAccessRightsToReadArenden(intygsId);

        return getArendeConversationViewList(intygsId, arendeList);
    }

    @Override
    @Transactional(readOnly = true)
    public QueryFragaSvarResponse filterArende(QueryFragaSvarParameter filterParameters) {

        WebCertUser user = webcertUserService.getUser();
        Set<String> intygstyperForPrivilege = authoritiesHelper.getIntygstyperForPrivilege(user, AuthoritiesConstants.PRIVILEGE_VISA_INTYG);

        Filter filter;
        if (!Strings.isNullOrEmpty(filterParameters.getEnhetId())) {
            verifyEnhetsAuth(filterParameters.getEnhetId(), true);
            filter = FilterConverter.convert(filterParameters, Arrays.asList(filterParameters.getEnhetId()), intygstyperForPrivilege);
        } else {
            filter = FilterConverter.convert(filterParameters, user.getIdsOfSelectedVardenhet(), intygstyperForPrivilege);
        }

        int originalStartFrom = filter.getStartFrom();
        int originalPageSize = filter.getPageSize();

        // INTYG-4086: Do NOT perform any paging. We must first load all applicable QA / ärenden, then apply
        // sekretessmarkering filtering. THEN - we can do paging stuff in-memory. Very inefficient...
        filter.setStartFrom(null);
        filter.setPageSize(null);

        List<ArendeListItem> results = arendeRepository.filterArende(filter).stream()
            .map(ArendeListItemConverter::convert)
            .filter(Objects::nonNull)
            // We need to decorate the ArendeListItem with information whether there exist a reminder or not because
            // they want to display this information to the user. We cannot do this without a database access, hence
            // we do it after the convertToDto
            .peek(item -> item.setPaminnelse(!arendeRepository.findByPaminnelseMeddelandeId(item.getMeddelandeId()).isEmpty()))
            .collect(Collectors.toList());

        QueryFragaSvarResponse fsResults = fragaSvarService.filterFragaSvar(filter);
        results.addAll(fsResults.getResults());
        results.sort(getComparator(filterParameters.getOrderBy(), filterParameters.getOrderAscending()));
        QueryFragaSvarResponse response = new QueryFragaSvarResponse();

        Map<Personnummer, SekretessStatus> sekretessStatusMap = patientDetailsResolver.getSekretessStatusForList(results.stream()
            .map(ali -> Personnummer.createPersonnummer(ali.getPatientId()).get())
            .collect(Collectors.toList()));

        // INTYG-4086, INTYG-4486: Filter out any items that doesn't pass sekretessmarkering rules
        results = results.stream()
            .filter(ali -> this.passesSekretessCheck(ali.getPatientId(), ali.getIntygTyp(), user, sekretessStatusMap))
            .collect(Collectors.toList());

        // We must mark all items having patient with sekretessmarkering
        results.stream()
            .filter(ali -> hasSekretessStatus(ali, SekretessStatus.TRUE, sekretessStatusMap))
            .forEach(ali -> ali.setSekretessmarkering(true));

        response.setTotalCount(results.size());

        if (originalStartFrom >= results.size()) {
            response.setResults(new ArrayList<>());
        } else {
            List<ArendeListItem> resultList = results
                .subList(originalStartFrom, Math.min(originalPageSize + originalStartFrom, results.size()));

            // Get lakare name
            Set<String> hsaIds = resultList.stream().map(ArendeListItem::getSigneratAv).collect(Collectors.toSet());
            Map<String, String> hsaIdNameMap = hsaIds.stream().collect(Collectors.toMap(a -> a, this::getLakareName));

            // Update lakare name
            resultList.forEach(row -> {
                if (hsaIdNameMap.containsKey(row.getSigneratAv())) {
                    row.setSigneratAvNamn(hsaIdNameMap.get(row.getSigneratAv()));
                }
            });

            response.setResults(resultList);
        }
        return response;
    }

    private static String getAmneString(String amne, Status status, Boolean paminnelse, String fragestallare) {
        String amneString = "";
        if (paminnelse) {
            amneString = "Påminnelse: ";
        }
        if (status == Status.CLOSED) {
            return amneString + INGET;
        } else if (status == Status.ANSWERED || amne.equals(MAKULERING) || amne.equals(PAMINNELSE) || amne.equals(PAMINN)) {
            if ("FK".equals(fragestallare)) {
                return amneString + HANTERAT_AV_FK;
            } else {
                return amneString + HANTERAT_AV_ANNAT;
            }
        } else if (amne.equals(KOMPLETTERING_AV_LAKARINTYG) || amne.equals(KOMPLT)) {
            return amneString + KOMPLETTERA;
        } else if (status == Status.PENDING_INTERNAL_ACTION) {
            return amneString + SVARA;
        } else if (status == Status.PENDING_EXTERNAL_ACTION) {
            return amneString + INVANTA_SVAR;
        }
        return "";
    }

    private Comparator<ArendeListItem> getComparator(String orderBy, Boolean ascending) {
        Comparator<ArendeListItem> comparator;
        if (orderBy == null) {
            comparator = Comparator.comparing(ArendeListItem::getReceivedDate);
        } else {
            switch (orderBy) {
                case "amne":
                    comparator = (a1, a2) -> {
                        return getAmneString(a1.getAmne(), a1.getStatus(), a1.isPaminnelse(), a1.getFragestallare())
                            .compareTo(getAmneString(a2.getAmne(), a2.getStatus(), a2.isPaminnelse(), a2.getFragestallare()));
                    };
                    break;
                case "fragestallare":
                    comparator = Comparator.comparing(ArendeListItem::getFragestallare);
                    break;
                case "patientId":
                    comparator = Comparator.comparing(ArendeListItem::getPatientId);
                    break;
                case "signeratAvNamn":
                    comparator = Comparator.comparing(ArendeListItem::getSigneratAvNamn);
                    break;
                case "vidarebefordrad":
                    comparator = Comparator.comparing(ArendeListItem::isVidarebefordrad);
                    break;
                case "receivedDate":
                default:
                    comparator = Comparator.comparing(ArendeListItem::getReceivedDate);
                    break;
            }
        }

        if (ascending == null || !ascending) {
            comparator = comparator.reversed();
        }
        return comparator;
    }

    private boolean passesSekretessCheck(String patientId, String intygsTyp, WebCertUser user,
        Map<Personnummer, SekretessStatus> sekretessStatusMap) {

        final SekretessStatus sekretessStatus = sekretessStatusMap.get(Personnummer.createPersonnummer(patientId).get());

        if (sekretessStatus == SekretessStatus.UNDEFINED) {
            return false;
        } else {
            return sekretessStatus == SekretessStatus.FALSE || authoritiesValidator.given(user, intygsTyp)
                .privilege(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT)
                .isVerified();
        }

    }

    private boolean hasSekretessStatus(ArendeListItem ali, SekretessStatus sekretessStatus,
        Map<Personnummer, SekretessStatus> sekretessStatusMap) {
        return sekretessStatusMap.get(Personnummer.createPersonnummer(ali.getPatientId()).get()) == sekretessStatus;
    }

    @Override
    @Transactional
    public ArendeConversationView closeArendeAsHandled(String meddelandeId, String intygTyp) {
        final Arende arende = lookupArende(meddelandeId);

        validateAccessRightsToAnswerComplement(arende.getIntygsId(), false);

        if (Fk7263EntryPoint.MODULE_ID.equalsIgnoreCase(intygTyp)) {
            fragaSvarService.closeQuestionAsHandled(Long.parseLong(meddelandeId));
            return null;
        } else {
            Arende closedArende = closeArendeAsHandled(arende);
            return arendeViewConverter.convertToArendeConversationView(closedArende,
                arendeRepository.findBySvarPaId(meddelandeId).stream().findFirst().orElse(null),
                null,
                arendeRepository.findByPaminnelseMeddelandeId(meddelandeId),
                null);
        }
    }

    @Override
    @Transactional
    public void closeCompletionsAsHandled(String intygId, String intygTyp) {
        if (Fk7263EntryPoint.MODULE_ID.equalsIgnoreCase(intygTyp)) {
            fragaSvarService.closeCompletionsAsHandled(intygId);
        } else {
            List<Arende> completionArenden = arendeRepository.findByIntygsId(intygId)
                .stream().filter(a -> ArendeAmne.KOMPLT == a.getAmne() && a.getSvarPaId() == null).collect(Collectors.toList());
            for (Arende completion : completionArenden) {
                if (Status.CLOSED != completion.getStatus()) {
                    closeArendeAsHandled(completion);
                }
            }
        }
    }

    @Override
    public void closeAllNonClosedQuestions(String intygsId) {

        List<Arende> list = arendeRepository.findByIntygsId(intygsId);

        for (Arende arende : list) {
            if (arende.getAmne() != ArendeAmne.PAMINN
                && arende.getSvarPaId() == null
                && arende.getStatus() != Status.CLOSED) {
                closeArendeAsHandled(arende);
            }
        }

        fragaSvarService.closeAllNonClosedQuestions(intygsId);
    }

    @Override
    public Arende getArende(String meddelandeId) {
        return arendeRepository.findOneByMeddelandeId(meddelandeId);
    }

    @Override
    public Map<String, Long> getNbrOfUnhandledArendenForCareUnits(List<String> vardenheterIds, Set<String> intygsTyper) {
        if (vardenheterIds == null || vardenheterIds.isEmpty()) {
            LOG.warn("No ids for Vardenheter was supplied");
            return new HashMap<>();
        }

        if (intygsTyper == null || intygsTyper.isEmpty()) {
            LOG.warn("No intygsTyper for querying Arenden was supplied");
            return new HashMap<>();
        }

        List<GroupableItem> results = arendeRepository.getUnhandledByEnhetIdsAndIntygstyper(vardenheterIds, intygsTyper);
        return statisticsGroupByUtil.toSekretessFilteredMap(results);
    }

    @Override
    public String getLatestMeddelandeIdForCurrentCareUnit(String intygsId) {
        WebCertUser user = webcertUserService.getUser();
        List<Arende> arendeList = filterKompletteringar(getArendeForIntygId(intygsId, user));

        return getLatestKomplArende(intygsId, arendeList).getMeddelandeId();
    }

    @Override
    public List<Arende> getKompletteringar(List<String> intygsIds) {
        return arendeRepository.findByIntygsIdAndType(intygsIds, ArendeAmne.KOMPLT);
    }

    @VisibleForTesting
    void setMockSystemClock(Clock systemClock) {
        this.systemClock = systemClock;
    }

    private void verifyEnhetsAuth(String enhetsId, boolean isReadOnlyOperation) {
        if (!webcertUserService.isAuthorizedForUnit(enhetsId, isReadOnlyOperation)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
                "User not authorized for enhet " + enhetsId);
        }
    }

    private List<ArendeConversationView> getArendeConversationViewList(String intygsId, List<Arende> arendeList) {

        List<AnsweredWithIntyg> kompltToIntyg = AnsweredWithIntygUtil.findAllKomplementForGivenIntyg(intygsId, utkastRepository);

        List<ArendeDraft> arendeDraftList = arendeDraftService.listAnswerDrafts(intygsId);

        return arendeViewConverter.buildArendeConversations(
            intygsId,
            arendeList,
            kompltToIntyg,
            arendeDraftList);

    }

    private NotificationEvent determineNotificationEvent(Arende arende, boolean arendeIsAnswered) {
        FrageStallare frageStallare = FrageStallare.getByKod(arende.getSkickatAv());
        Status arendeSvarStatus = arende.getStatus();

        if (FrageStallare.FORSAKRINGSKASSAN.equals(frageStallare)) {
            if (Status.PENDING_INTERNAL_ACTION.equals(arendeSvarStatus)) {
                return NotificationEvent.QUESTION_FROM_RECIPIENT_HANDLED;
            } else if (Status.CLOSED.equals(arendeSvarStatus)) {
                return NotificationEvent.QUESTION_FROM_RECIPIENT_UNHANDLED;
            }
        }

        if (FrageStallare.WEBCERT.equals(frageStallare)) {
            if (Status.ANSWERED.equals(arendeSvarStatus)) {
                return NotificationEvent.QUESTION_FROM_CARE_WITH_ANSWER_HANDLED;
            } else if (Status.CLOSED.equals(arendeSvarStatus) && arendeIsAnswered) {
                return NotificationEvent.QUESTION_FROM_CARE_WITH_ANSWER_UNHANDLED;
            } else if (Status.CLOSED.equals(arendeSvarStatus)) {
                return NotificationEvent.QUESTION_FROM_CARE_UNHANDLED;
            } else {
                return NotificationEvent.QUESTION_FROM_CARE_HANDLED;
            }
        }

        return null;
    }

    private void sendNotification(Arende arende, NotificationEvent event) {
        if (event != null) {
            notificationService.sendNotificationForQAs(arende.getIntygsId(), event);
        }
    }

    private Arende lookupArende(String meddelandeId) {
        Arende arende = arendeRepository.findOneByMeddelandeId(meddelandeId);

        if (arende == null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
                "Could not find Arende with id:" + meddelandeId);
        }
        return arende;
    }

    private void validateArende(String arendeIntygsId, Utkast utkast) {
        if (utkast == null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
                "Certificate " + arendeIntygsId + " not found.");
        } else if (utkast.getSignatur() == null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                "Certificate " + arendeIntygsId + " not signed.");
        } else if (BLACKLISTED.contains(utkast.getIntygsTyp())) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                "Certificate " + arendeIntygsId + " has wrong type. " + utkast.getIntygsTyp() + " is blacklisted.");
        } else if (utkast.getAterkalladDatum() != null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.CERTIFICATE_REVOKED,
                "Certificate " + arendeIntygsId + " is revoked.");
        }
    }

    private Arende processOutgoingMessage(Arende arende, NotificationEvent notificationEvent) {
        Arende saved = arendeRepository.save(arende);
        monitoringLog.logArendeCreated(arende.getIntygsId(), arende.getIntygTyp(), arende.getEnhetId(), arende.getAmne(),
            arende.getSvarPaId() != null);

        updateSenasteHandelseAndStatusForRelatedArende(arende);

        SendMessageToRecipientType request = SendMessageToRecipientTypeBuilder.build(arende, webcertUserService.getUser(),
            sendMessageToFKLogicalAddress);

        // Send to recipient
        try {
            certificateSenderService.sendMessageToRecipient(arende.getIntygsId(), SendMessageToRecipientTypeConverter.toXml(request));
        } catch (MarshallingFailureException | CertificateSenderException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, e.getMessage());
        }

        sendNotification(saved, notificationEvent);

        return saved;
    }

    private void updateSenasteHandelseAndStatusForRelatedArende(Arende arende) {
        Arende orig;
        if (arende.getSvarPaId() != null) {
            orig = arendeRepository.findOneByMeddelandeId(arende.getSvarPaId());
            if (orig != null) {
                orig.setSenasteHandelse(arende.getSenasteHandelse());
                orig.setStatus(arende.getStatus());
                arendeRepository.save(orig);
            }
        } else if (arende.getPaminnelseMeddelandeId() != null) {
            orig = arendeRepository.findOneByMeddelandeId(arende.getPaminnelseMeddelandeId());
            if (orig != null) {
                orig.setSenasteHandelse(arende.getSenasteHandelse());
                arendeRepository.save(orig);
            }
        }
    }

    private Arende closeArendeAsHandled(Arende arendeToClose) {
        if (arendeToClose.getStatus() == Status.CLOSED) {
            return arendeToClose;
        }

        // determineNotficationEvent() has to be called before closing arende for correct behaviour.
        NotificationEvent notificationEvent = determineNotificationEvent(arendeToClose, false);

        arendeToClose.setStatus(Status.CLOSED);
        arendeToClose.setSenasteHandelse(LocalDateTime.now(systemClock));
        Arende closedArende = arendeRepository.save(arendeToClose);

        arendeDraftService.delete(closedArende.getIntygsId(), closedArende.getMeddelandeId());
        sendNotification(closedArende, notificationEvent);

        return closedArende;
    }

    private void validateAccessRightsToAnswerComplement(String intygsId, boolean newCertificate) {
        final Utlatande utlatande = getUtlatande(intygsId);
        final AccessResult accessResult = certificateAccessService.allowToAnswerComplementQuestion(
            utlatande.getTyp(),
            getVardenhet(utlatande),
            getPersonnummer(utlatande),
            newCertificate);

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private void validateAccessRightsToForwardQuestions(String intygsId) {
        final Utlatande utlatande = getUtlatande(intygsId);
        final AccessResult accessResult = certificateAccessService.allowToForwardQuestions(
            utlatande.getTyp(),
            getVardenhet(utlatande),
            getPersonnummer(utlatande));

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private void validateAccessRightsToCreateQuestion(Utkast utkast) {
        final Utlatande utlatande = getUtlatande(utkast);
        final AccessResult accessResult = certificateAccessService.allowToCreateQuestion(
            utlatande.getTyp(),
            getVardenhet(utlatande),
            getPersonnummer(utlatande));

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private void validateAccessRightsToReadArenden(String intygsId) {
        final Utlatande utlatande = getUtlatande(intygsId);
        final AccessResult accessResult = certificateAccessService.allowToReadQuestions(
            utlatande.getTyp(),
            getVardenhet(utlatande),
            getPersonnummer(utlatande));

        accessResultExceptionHelper.throwExceptionIfDenied(accessResult);
    }

    private Utlatande getUtlatande(String intygsId) {
        final Utkast utkast = utkastRepository.findOne(intygsId);
        if (utkast == null) {
            final IntygTypeInfo intygTypInfo = intygService.getIntygTypeInfo(intygsId, null);
            final IntygContentHolder intygContentHolder = intygService.fetchIntygData(intygsId, intygTypInfo.getIntygType(), true, false);
            return intygContentHolder.getUtlatande();
        }
        return getUtlatande(utkast);
    }

    private Utlatande getUtlatande(Utkast utkast) {
        return modelFacade.getUtlatandeFromInternalModel(utkast.getIntygsTyp(), utkast.getModel());
    }

    private Vardenhet getVardenhet(Utlatande utlatande) {
        return utlatande.getGrundData().getSkapadAv().getVardenhet();
    }

    private Personnummer getPersonnummer(Utlatande utlatande) {
        return utlatande.getGrundData().getPatient().getPersonId();
    }
}
