/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.MarshallingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.support.common.enumerations.EventCode;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.ts_bas.support.TsBasEntryPoint;
import se.inera.intyg.common.ts_diabetes.support.TsDiabetesEntryPoint;
import se.inera.intyg.infra.integration.hsatk.services.HsatkEmployeeService;
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
import se.inera.intyg.webcert.web.event.CertificateEventService;
import se.inera.intyg.webcert.web.integration.builders.SendMessageToRecipientTypeBuilder;
import se.inera.intyg.webcert.web.service.access.CertificateAccessServiceHelper;
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
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.message.MessageImportService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationEvent;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolverResponse;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.util.StatisticsGroupByUtil;
import se.inera.intyg.webcert.web.web.controller.api.dto.AnsweredWithIntyg;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeConversationView;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygTypeInfo;
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

    private final Comparator<Arende> byTimestamp = (left, right) -> left.getTimestamp().isBefore(right.getTimestamp()) ? -1 : 1;

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
    private HsatkEmployeeService hsaEmployeeService;
    @Autowired
    private FragaSvarService fragaSvarService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private CertificateEventService certificateEventService;
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
    private CertificateAccessServiceHelper certificateAccessServiceHelper;
    @Autowired
    private IntygService intygService;
    @Autowired
    private LogService logService;
    @Autowired
    private MessageImportService messageImportService;

    private final AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();

    private static Predicate<Arende> isQuestion() {
        return a -> a.getSvarPaId() == null;
    }

    private static Predicate<Arende> isCorrectEnhet(WebCertUserService webcertUserService) {
        return a -> webcertUserService.isAuthorizedForUnit(a.getEnhetId(), false);
    }

    private static Predicate<Arende> isCorrectAmne(ArendeAmne arendeAmne) {
        return a -> a.getAmne().equals(arendeAmne);
    }

    @Override
    public Arende processIncomingMessage(Arende arende) {
        final var certificateId = arende.getIntygsId();
        if (messageImportService.isImportNeeded(certificateId)) {
            messageImportService.importMessages(certificateId, arende.getMeddelandeId());
        }

        if (arendeRepository.findOneByMeddelandeId(arende.getMeddelandeId()) != null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MESSAGE_ALREADY_EXISTS,
                "This message has already been received.");
        }

        if (arende.getSvarPaId() != null && !arendeRepository.findBySvarPaId(arende.getSvarPaId()).isEmpty()) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Answer already exist for this message");
        }

        Utkast utkast = utkastRepository.findById(certificateId).orElse(null);
        if (utkast != null) {
            validateArende(certificateId, utkast);

            ArendeConverter.decorateArendeFromUtkast(arende, utkast, LocalDateTime.now(systemClock), hsaEmployeeService);

            updateSenasteHandelseAndStatusForRelatedArende(arende);

            monitoringLog.logArendeReceived(certificateId, utkast.getIntygsTyp(), utkast.getEnhetsId(), arende.getAmne(),
                arende.getKomplettering().stream().map(MedicinsktArende::getFrageId).collect(Collectors.toList()),
                arende.getSvarPaId() != null);
        } else {
            final var certificate = intygService.fetchIntygDataForInternalUse(certificateId, false);

            validateArende(certificate);

            ArendeConverter.decorateMessageFromCertificate(arende, certificate.getUtlatande(), LocalDateTime.now(systemClock));

            updateSenasteHandelseAndStatusForRelatedArende(arende);

            monitoringLog.logArendeReceived(certificateId, certificate.getUtlatande().getTyp(),
                certificate.getUtlatande().getGrundData().getSkapadAv().getVardenhet().getEnhetsid(), arende.getAmne(),
                arende.getKomplettering().stream().map(MedicinsktArende::getFrageId).collect(Collectors.toList()),
                arende.getSvarPaId() != null);
        }

        Arende saved = arendeRepository.save(arende);

        sendNotificationAndCreateEventForIncomingMessage(saved);

        return saved;
    }


    @Override
    public ArendeConversationView createMessage(String intygId, ArendeAmne amne, String rubrik, String meddelande) {
        Arende saved = internalCreateMessage(intygId, amne, rubrik, meddelande);
        return arendeViewConverter.convertToArendeConversationView(saved, null, null, new ArrayList<>(), null);
    }

    private Arende internalCreateMessage(String intygId, ArendeAmne amne, String rubrik, String meddelande) {
        if (!VALID_VARD_AMNEN.contains(amne)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Invalid Amne " + amne
                + " for new question from vard!");
        }

        validateAccessRightsToCreateQuestion(intygId);

        Utkast utkast = utkastRepository.findById(intygId).orElse(null);

        Arende arende;
        if (utkast != null) {
            validateArende(intygId, utkast);

            arende = ArendeConverter.createArendeFromUtkast(amne, rubrik, meddelande, utkast, LocalDateTime.now(systemClock),
                webcertUserService.getUser().getNamn(), hsaEmployeeService);
        } else {
            final var certificateTypeInformation = intygService.getIntygTypeInfo(intygId, null);
            final var certificate = intygService.fetchIntygData(intygId, certificateTypeInformation.getIntygType(), false);

            validateArende(certificate);

            arende = ArendeConverter.createMessageFromCertificate(amne, rubrik, meddelande, certificate.getUtlatande(),
                LocalDateTime.now(systemClock), webcertUserService.getUser().getNamn(), hsaEmployeeService);
        }

        Arende saved = processOutgoingMessage(arende, NotificationEvent.NEW_QUESTION_FROM_CARE, EventCode.NYFRFV, true);

        logService.logCreateMessage(webcertUserService.getUser(), saved);

        arendeDraftService.delete(intygId, null);
        return saved;
    }

    @Override
    public Arende sendMessage(ArendeDraft arendeDraft) {
        final var amne = ArendeAmne.valueOf(arendeDraft.getAmne());
        return internalCreateMessage(arendeDraft.getIntygId(), amne, "", arendeDraft.getText());
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

        Arende saved = processOutgoingMessage(arende, NotificationEvent.NEW_ANSWER_FROM_CARE, EventCode.HANFRFM, true);

        logService.logCreateMessage(webcertUserService.getUser(), saved);

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

        List<Arende> allArende = getArendeForIntygId(intygsId);
        List<Arende> arendeList = filterKompletteringar(allArende);

        validateAccessRightsToAnswerComplement(intygsId, false);

        Arende latest = getLatestKomplArende(intygsId, arendeList);
        for (Arende arende : arendeList) {
            if (arende.getStatus() != Status.CLOSED) {
                Arende answer = ArendeConverter.createAnswerFromArende(
                    meddelande,
                    arende,
                    LocalDateTime.now(systemClock),
                    user.getNamn());

                arendeDraftService.delete(arende.getIntygsId(), arende.getMeddelandeId());
                Arende saved = processOutgoingMessage(answer, NotificationEvent.NEW_ANSWER_FROM_CARE, EventCode.HANFRFM,
                    Objects.equals(arende.getMeddelandeId(), latest.getMeddelandeId()));

                allArende.add(saved);
            }
        }

        arendeList.stream().filter(arende -> arende.getStatus() != Status.CLOSED).forEach(this::closeArendeAsHandled);

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

    private List<Arende> getArendeForIntygId(String intygsId) {
        return arendeRepository.findByIntygsId(intygsId);
    }

    @Override
    @Transactional
    public List<ArendeConversationView> setForwarded(String intygsId) {

        List<Arende> allArende = arendeRepository.findByIntygsId(intygsId);

        validateAccessRightsToForwardQuestions(intygsId);

        List<Arende> arendenToForward = arendeRepository.saveAll(
            allArende
                .stream()
                .filter(isCorrectEnhet(webcertUserService))
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

        sendNotificationAndCreateEvent(openedArende, notificationEvent);

        logService.logCreateMessage(webcertUserService.getUser(), openedArende);

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

        Map<String, String> lakareArendeList = arendeRepository.findSigneratAvByEnhet(enhetsIdParams).stream()
            .collect(Collectors.toMap(arr -> (String) arr[0], arr -> (String) arr[1], (a1, a2) -> a1));

        // We need to maintain backwards compatibility. When FragaSvar no longer exist remove this part and return above
        // arendeList
        Map<String, String> lakareFragaSvarList = fragaSvarService.getFragaSvarHsaIdByEnhet(enhetsId).stream()
            .collect(Collectors.toMap(Lakare::getHsaId, Lakare::getName, (a1, a2) -> a1));

        lakareFragaSvarList.putAll(lakareArendeList);

        Map<String, String> hsaToNameMap = ArendeConverter.getNamesByHsaIds(lakareFragaSvarList.keySet(), hsaEmployeeService);

        return lakareFragaSvarList.entrySet().stream()
            .map(lakare -> {
                String hsaId = lakare.getKey();
                String name = lakare.getValue();

                if (hsaToNameMap.containsKey(hsaId)) {
                    name = hsaToNameMap.get(hsaId);
                }

                return new Lakare(hsaId, name);
            })
            .sorted(Comparator.comparing(Lakare::getName))
            .collect(Collectors.toList());
    }


    @Override
    public List<ArendeConversationView> getArenden(String intygsId) {
        if (messageImportService.isImportNeeded(intygsId)) {
            messageImportService.importMessages(intygsId);
        }

        validateAccessRightsToReadArenden(intygsId);

        List<Arende> arendeList = getArendeForIntygId(intygsId);

        return getArendeConversationViewList(intygsId, arendeList);
    }

    @Override
    public QueryFragaSvarResponse filterArende(QueryFragaSvarParameter filterParameters) {
        return filterArende(filterParameters, false);
    }

    @Override
    @Transactional(readOnly = true)
    public QueryFragaSvarResponse filterArende(QueryFragaSvarParameter filterParameters, boolean excludeUnhandledQuestions) {

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
            .filter(this::isUnhandled)
            // We need to decorate the ArendeListItem with information whether there exist a reminder or not because
            // they want to display this information to the user. We cannot do this without a database access, hence
            // we do it after the convertToDto
            .peek(item -> item.setPaminnelse(!arendeRepository.findByPaminnelseMeddelandeId(item.getMeddelandeId()).isEmpty()))
            .collect(Collectors.toList());

        QueryFragaSvarResponse fsResults = fragaSvarService.filterFragaSvar(filter);
        results.addAll(fsResults.getResults());
        QueryFragaSvarResponse response = new QueryFragaSvarResponse();

        Map<Personnummer, PatientDetailsResolverResponse> statusMap = patientDetailsResolver.getPersonStatusesForList(results.stream()
            .map(ali -> Personnummer.createPersonnummer(ali.getPatientId()).get())
            .collect(Collectors.toList()));

        // INTYG-4086, INTYG-4486: Filter out any items that doesn't pass sekretessmarkering rules
        results = results.stream()
            .filter(ali -> this.passesSekretessCheck(ali.getIntygTyp(), user,
                statusMap.get(Personnummer.createPersonnummer(ali.getPatientId()).get())))
            .collect(Collectors.toList());

        results.stream().forEach(ali -> markStatuses(ali, statusMap.get(Personnummer.createPersonnummer(ali.getPatientId()).get())));

        response.setTotalCount(results.size());

        if (originalStartFrom >= results.size()) {
            response.setResults(new ArrayList<>());
        } else {
            // Get lakare name
            Set<String> hsaIds = results.stream().map(ArendeListItem::getSigneratAv).collect(Collectors.toSet());
            Map<String, String> hsaIdNameMap = getNamesByHsaIds(hsaIds);

            // Update lakare name
            results.forEach(row -> {
                if (hsaIdNameMap.containsKey(row.getSigneratAv())) {
                    row.setSigneratAvNamn(hsaIdNameMap.get(row.getSigneratAv()));
                }
            });

            results.sort(getComparator(filterParameters.getOrderBy(), filterParameters.getOrderAscending()));

            List<ArendeListItem> resultList = results
                .subList(originalStartFrom, Math.min(originalPageSize + originalStartFrom, results.size()));

            response.setResults(resultList);

            // PDL Logging
            resultList.stream().map(ArendeListItem::getPatientId).distinct().forEach(patient -> {
                logService.logListIntyg(user, patient);
            });
        }

        return response;
    }

    private boolean isUnhandled(ArendeListItem item) {
        return !((item.getStatus() == Status.PENDING_INTERNAL_ACTION && isReminder(item) && item.getFragestallare().equals("FK"))
                || item.getStatus() == Status.ANSWERED
                || item.getStatus() == Status.CLOSED
                || item.getAmne().equals("MAKULERING"));
    }

    private boolean isReminder(ArendeListItem item) {
        return item.getAmne().equals("PAMINNELSE") || item.getAmne().equals("PAMINN");
    }

    Map<String, String> getNamesByHsaIds(Set<String> hsaIds) {
        return ArendeConverter.getNamesByHsaIds(hsaIds, hsaEmployeeService);
    }

    private void markStatuses(ArendeListItem ali, PatientDetailsResolverResponse status) {
        ali.setAvliden(status.isDeceased());
        ali.setTestIntyg(status.isTestIndicator());
        ali.setSekretessmarkering(status.isProtectedPerson() == SekretessStatus.TRUE);
    }

    public static String getAmneString(String amne, Status status, Boolean paminnelse, String fragestallare) {
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

    private boolean passesSekretessCheck(String intygsTyp, WebCertUser user,
        PatientDetailsResolverResponse response) {
        final SekretessStatus sekretessStatus = response.isProtectedPerson();
        if (sekretessStatus == SekretessStatus.UNDEFINED) {
            return false;
        } else {
            return sekretessStatus == SekretessStatus.FALSE || authoritiesValidator.given(user, intygsTyp)
                .privilege(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT)
                .isVerified();
        }
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
            logService.logCreateMessage(webcertUserService.getUser(), closedArende);
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
    public void reopenClosedCompletions(String intygsId) {

        List<Arende> list = arendeRepository.findByIntygsId(intygsId);

        for (Arende arende : list) {
            if (arende.getAmne() == ArendeAmne.KOMPLT && arende.getStatus() == Status.CLOSED) {
                reopenClosedCompletion(arende);
            }
        }

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
        List<Arende> arendeListForCurrentUnit = getArendeForIntygId(intygsId).stream()
            .filter(isCorrectEnhet(webcertUserService))
            .collect(Collectors.toList());

        List<Arende> arendeList = filterKompletteringar(arendeListForCurrentUnit);

        return getLatestKomplArende(intygsId, arendeList).getMeddelandeId();
    }

    @Override
    public List<Arende> getKompletteringar(List<String> intygsIds) {
        return arendeRepository.findByIntygsIdAndType(intygsIds, ArendeAmne.KOMPLT);
    }


    @Override
    public List<Arende> getArendenExternal(List<String> intygsIds) {
        return arendeRepository.findByIntygsIds(intygsIds);
    }

    @Override
    public List<Arende> getArendenInternal(String intygsId) {
        return arendeRepository.findByIntygsId(intygsId);
    }

    @Override
    public List<Arende> getRelatedArenden(String questionId) {
        final var answers = arendeRepository.findBySvarPaId(questionId);
        final var reminders = arendeRepository.findByPaminnelseMeddelandeId(questionId);
        return Stream.of(answers, reminders).flatMap(Collection::stream).collect(Collectors.toList());
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

        final var answerWithCertificate = getAnsweredFromCertificateOutsideWebcert(intygsId);
        if (answerWithCertificate != null && isNotDuplicateAnswer(answerWithCertificate, kompltToIntyg)) {
            kompltToIntyg.add(answerWithCertificate);
        }

        List<ArendeDraft> arendeDraftList = arendeDraftService.listAnswerDrafts(intygsId);

        return arendeViewConverter.buildArendeConversations(
            intygsId,
            arendeList,
            kompltToIntyg,
            arendeDraftList);

    }

    private boolean isNotDuplicateAnswer(AnsweredWithIntyg answerWithCertificate, List<AnsweredWithIntyg> answeredWithIntygList) {
        return answeredWithIntygList.stream()
            .noneMatch(answeredWithIntyg -> answeredWithIntyg.getIntygsId().equalsIgnoreCase(answerWithCertificate.getIntygsId()));
    }

    private AnsweredWithIntyg getAnsweredFromCertificateOutsideWebcert(String certificateId) {
        if (utkastRepository.existsById(certificateId)) {
            return null;
        }

        final var certificate = intygService.fetchIntygDataForInternalUse(certificateId, true);
        if (isComplementedByCertificate(certificate)) {
            final var signedDate = certificate.getUtlatande().getGrundData().getSigneringsdatum();
            final var issuerByName = certificate.getUtlatande().getGrundData().getSkapadAv().getFullstandigtNamn();

            return AnsweredWithIntyg.create(certificateId, issuerByName, signedDate, signedDate, issuerByName);
        }
        return null;
    }

    private boolean isComplementedByCertificate(IntygContentHolder certificate) {
        return certificate != null && certificate.getRelations().getLatestChildRelations().getComplementedByIntyg() != null
            && !certificate.getRelations().getLatestChildRelations().getComplementedByIntyg().isMakulerat();
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

    private NotificationEvent determineReopenedNotificationEvent(Arende arende) {
        FrageStallare frageStallare = FrageStallare.getByKod(arende.getSkickatAv());

        if (FrageStallare.FORSAKRINGSKASSAN.equals(frageStallare)) {
            return NotificationEvent.NEW_QUESTION_FROM_RECIPIENT;
        }

        return null;
    }

    private void sendNotificationAndCreateEvent(Arende arende, NotificationEvent event) {
        if (event != null) {
            notificationService.sendNotificationForQAs(arende.getIntygsId(), event);
            EventCode eventCode = getEventCode(event);

            if (eventCode != null) {
                certificateEventService
                    .createCertificateEvent(arende.getIntygsId(), webcertUserService.getUser().getHsaId(), eventCode, event.name());
            }
        }
    }


    private void sendNotificationAndCreateEventForIncomingMessage(Arende saved) {
        String certificateId = saved.getIntygsId();
        String sentBy = saved.getSkickatAv();

        if (ArendeAmne.PAMINN == saved.getAmne() || saved.getSvarPaId() == null) {
            notificationService.sendNotificationForQuestionReceived(saved);

            if (saved.getPaminnelseMeddelandeId() != null) {
                certificateEventService.createCertificateEvent(certificateId, sentBy, EventCode.PAMINNELSE);
            } else if (saved.getAmne() == ArendeAmne.KOMPLT) {
                certificateEventService.createCertificateEvent(certificateId, sentBy, EventCode.KOMPLBEGARAN);
            } else {
                String message = saved.getAmne() != null ? saved.getAmne().getDescription() : EventCode.NYFRFM.getDescription();
                certificateEventService.createCertificateEvent(certificateId, sentBy, EventCode.NYFRFM, message);
            }
        } else {
            notificationService.sendNotificationForAnswerRecieved(saved);
            certificateEventService.createCertificateEvent(certificateId, sentBy, EventCode.NYSVFM);
        }
    }

    private EventCode getEventCode(NotificationEvent event) {
        switch (event) {
            case QUESTION_FROM_CARE_WITH_ANSWER_HANDLED:
            case QUESTION_FROM_CARE_HANDLED:
            case QUESTION_FROM_CARE_UNHANDLED:
                return EventCode.HANFRFV;
            case NEW_ANSWER_FROM_CARE:
            case QUESTION_FROM_RECIPIENT_HANDLED:
            case QUESTION_FROM_RECIPIENT_UNHANDLED:
                return EventCode.HANFRFM;
            case NEW_QUESTION_FROM_CARE:
                return EventCode.NYFRFV;
            case NEW_QUESTION_FROM_RECIPIENT:
                return EventCode.NYFRFM;
            case NEW_ANSWER_FROM_RECIPIENT:
            case QUESTION_FROM_CARE_WITH_ANSWER_UNHANDLED:
                return EventCode.NYSVFM;
        }
        return null;
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

    private void validateArende(IntygContentHolder certificate) {
        if (BLACKLISTED.contains(certificate.getUtlatande().getTyp())) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                "Certificate " + certificate.getUtlatande().getId() + " has wrong type. " + certificate.getUtlatande().getTyp()
                    + " is blacklisted.");
        } else if (certificate.isRevoked()) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.CERTIFICATE_REVOKED,
                "Certificate " + certificate.getUtlatande().getId() + " is revoked.");
        }
    }


    private Arende processOutgoingMessage(Arende arende, NotificationEvent notificationEvent, EventCode eventCode,
        boolean sendToRecipient) {
        Arende saved = arendeRepository.save(arende);
        monitoringLog.logArendeCreated(arende.getIntygsId(), arende.getIntygTyp(), arende.getEnhetId(), arende.getAmne(),
            arende.getSvarPaId() != null);

        updateSenasteHandelseAndStatusForRelatedArende(arende);

        if (sendToRecipient) {
            SendMessageToRecipientType request = SendMessageToRecipientTypeBuilder.build(arende, webcertUserService.getUser(),
                sendMessageToFKLogicalAddress);

            // Send to recipient
            try {
                certificateSenderService.sendMessageToRecipient(arende.getIntygsId(), SendMessageToRecipientTypeConverter.toXml(request));
            } catch (MarshallingFailureException | CertificateSenderException e) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, e.getMessage());
            }

            sendNotificationAndCreateEvent(saved, notificationEvent);
        }

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
        sendNotificationAndCreateEvent(closedArende, notificationEvent);

        return closedArende;
    }

    private void reopenClosedCompletion(Arende arende) {
        if (arende.getStatus() != Status.CLOSED) {
            return;
        }

        arende.setStatus(Status.PENDING_INTERNAL_ACTION);
        arende.setSenasteHandelse(LocalDateTime.now(systemClock));
        Arende reopenedArende = arendeRepository.save(arende);

        NotificationEvent notificationEvent = determineReopenedNotificationEvent(reopenedArende);
        sendNotificationAndCreateEvent(reopenedArende, notificationEvent);

    }

    private void validateAccessRightsToAnswerComplement(String intygsId, boolean newCertificate) {
        final Utlatande utlatande = getUtlatande(intygsId);
        certificateAccessServiceHelper.validateAccessToAnswerComplementQuestion(utlatande, newCertificate);
    }

    private void validateAccessRightsToForwardQuestions(String intygsId) {
        final Utlatande utlatande = getUtlatande(intygsId);
        certificateAccessServiceHelper.validateAccessToForwardQuestions(utlatande);
    }

    private void validateAccessRightsToCreateQuestion(String certificateId) {
        final Utlatande utlatande = getUtlatande(certificateId);
        certificateAccessServiceHelper.validateAccessToCreateQuestion(utlatande);
    }

    private void validateAccessRightsToReadArenden(String intygsId) {
        final Utlatande utlatande = getUtlatande(intygsId);
        certificateAccessServiceHelper.validateAccessToReadQuestions(utlatande);
    }

    private Utlatande getUtlatande(String intygsId) {
        final Utkast utkast = utkastRepository.findById(intygsId).orElse(null);
        if (utkast == null) {
            final IntygTypeInfo intygTypInfo = intygService.getIntygTypeInfo(intygsId, null);
            final IntygContentHolder intygContentHolder = intygService.fetchIntygData(intygsId, intygTypInfo.getIntygType(), false);
            return intygContentHolder.getUtlatande();
        }
        return getUtlatande(utkast);
    }

    private Utlatande getUtlatande(Utkast utkast) {
        return modelFacade.getUtlatandeFromInternalModel(utkast.getIntygsTyp(), utkast.getModel());
    }
}
