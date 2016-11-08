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

package se.inera.intyg.webcert.web.service.arende;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.common.integration.hsa.services.HsaEmployeeService;
import se.inera.intyg.common.security.authorities.AuthoritiesHelper;
import se.inera.intyg.common.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.common.security.common.model.AuthoritiesConstants;
import se.inera.intyg.intygstyper.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.intygstyper.ts_bas.support.TsBasEntryPoint;
import se.inera.intyg.intygstyper.ts_diabetes.support.TsDiabetesEntryPoint;
import se.inera.intyg.webcert.common.client.converter.SendMessageToRecipientTypeConverter;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.arende.model.*;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeRepository;
import se.inera.intyg.webcert.persistence.model.Filter;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.converter.*;
import se.inera.intyg.webcert.web.converter.util.ArendeViewConverter;
import se.inera.intyg.webcert.web.integration.builder.SendMessageToRecipientTypeBuilder;
import se.inera.intyg.webcert.web.service.certificatesender.CertificateSenderException;
import se.inera.intyg.webcert.web.service.certificatesender.CertificateSenderService;
import se.inera.intyg.webcert.web.service.dto.Lakare;
import se.inera.intyg.webcert.web.service.fragasvar.FragaSvarService;
import se.inera.intyg.webcert.web.service.fragasvar.dto.*;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationEvent;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeConversationView;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v1.SendMessageToRecipientType;

@Service
@Transactional("jpaTransactionManager")
public class ArendeServiceImpl implements ArendeService {

    private static final Logger LOG = LoggerFactory.getLogger(ArendeServiceImpl.class);

    private static final List<String> BLACKLISTED = Arrays.asList(Fk7263EntryPoint.MODULE_ID, TsBasEntryPoint.MODULE_ID,
            TsDiabetesEntryPoint.MODULE_ID);

    private static final List<ArendeAmne> VALID_VARD_AMNEN = Arrays.asList(
            ArendeAmne.AVSTMN,
            ArendeAmne.KONTKT,
            ArendeAmne.OVRIGT);

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

    @Override
    public Arende processIncomingMessage(Arende arende) throws WebCertServiceException {
        if (arendeRepository.findOneByMeddelandeId(arende.getMeddelandeId()) != null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "meddelandeId not unique");
        }

        Utkast utkast = utkastRepository.findOne(arende.getIntygsId());

        validateArende(arende.getIntygsId(), utkast);

        ArendeConverter.decorateArendeFromUtkast(arende, utkast, LocalDateTime.now(), hsaEmployeeService);

        updateRelated(arende);

        monitoringLog.logArendeReceived(arende.getIntygsId(), utkast.getIntygsTyp(), utkast.getEnhetsId(), arende.getAmne(),
                arende.getKomplettering().stream().map(MedicinsktArende::getFrageId).collect(Collectors.toList()), arende.getSvarPaId() != null);

        Arende saved = arendeRepository.save(arende);

        if (ArendeAmne.PAMINN == saved.getAmne() || saved.getSvarPaId() == null) {
            notificationService.sendNotificationForQuestionReceived(saved);
        } else {
            notificationService.sendNotificationForAnswerRecieved(saved);
        }
        return saved;
    }

    @Override
    public ArendeConversationView createMessage(String intygId, ArendeAmne amne, String rubrik, String meddelande) throws WebCertServiceException {
        if (!VALID_VARD_AMNEN.contains(amne)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Invalid Amne " + amne
                    + " for new question from vard!");
        }
        Utkast utkast = utkastRepository.findOne(intygId);

        validateArende(intygId, utkast);

        verifyEnhetsAuth(utkast.getEnhetsId(), false);

        Arende arende = ArendeConverter.createArendeFromUtkast(amne, rubrik, meddelande, utkast, LocalDateTime.now(),
                webcertUserService.getUser().getNamn(), hsaEmployeeService);

        Arende saved = processOutgoingMessage(arende, NotificationEvent.NEW_QUESTION_FROM_CARE);

        return arendeViewConverter.convertToArendeConversationView(saved, null, new ArrayList<>());
    }

    @Override
    public ArendeConversationView answer(String svarPaMeddelandeId, String meddelande) {
        if (StringUtils.isEmpty(meddelande)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM,
                    "SvarsText cannot be empty!");
        }
        Arende svarPaMeddelande = lookupArende(svarPaMeddelandeId);

        verifyEnhetsAuth(svarPaMeddelande.getEnhetId(), false);

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

        // Implement Business Rule FS-005, FS-006
        WebCertUser user = webcertUserService.getUser();
        AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();
        if (ArendeAmne.KOMPLT.equals(svarPaMeddelande.getAmne())
                && !authoritiesValidator.given(user).privilege(AuthoritiesConstants.PRIVILEGE_BESVARA_KOMPLETTERINGSFRAGA).isVerified()) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM, "Arende with id "
                    + svarPaMeddelandeId + " and amne (" + svarPaMeddelande.getAmne()
                    + ") can only be answered by user that is Lakare");
        }
        Arende arende = ArendeConverter.createAnswerFromArende(meddelande, svarPaMeddelande, LocalDateTime.now(),
                webcertUserService.getUser().getNamn());

        Arende saved = processOutgoingMessage(arende, NotificationEvent.NEW_ANSWER_FROM_CARE);

        // Implement Business Rule FS-045
        if (ArendeAmne.KOMPLT.equals(svarPaMeddelande.getAmne())) {
            closeCompletionsAsHandled(svarPaMeddelande.getIntygsId(), svarPaMeddelande.getIntygTyp());
        }
        return arendeViewConverter.convertToArendeConversationView(svarPaMeddelande, saved,
                arendeRepository.findByPaminnelseMeddelandeId(svarPaMeddelandeId));
    }

    @Override
    public ArendeConversationView setForwarded(String meddelandeId, boolean vidarebefordrad) {
        Arende arende = lookupArende(meddelandeId);
        arende.setVidarebefordrad(vidarebefordrad);

        Arende updatedArende = arendeRepository.save(arende);

        return arendeViewConverter.convertToArendeConversationView(updatedArende,
                arendeRepository.findBySvarPaId(meddelandeId).stream().findFirst().orElse(null),
                arendeRepository.findByPaminnelseMeddelandeId(meddelandeId));
    }

    @Override
    public ArendeConversationView openArendeAsUnhandled(String meddelandeId) {
        Arende arende = lookupArende(meddelandeId);
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
                arendeRepository.findByPaminnelseMeddelandeId(meddelandeId));
    }

    @Override
    public List<Lakare> listSignedByForUnits(String enhetsId) throws WebCertServiceException {

        List<String> enhetsIdParams = new ArrayList<>();
        if (enhetsId != null) {
            verifyEnhetsAuth(enhetsId, true);
            enhetsIdParams.add(enhetsId);
        } else {
            enhetsIdParams.addAll(webcertUserService.getUser().getIdsOfSelectedVardenhet());
        }

        List<Lakare> arendeList = arendeRepository.findSigneratAvByEnhet(enhetsIdParams).stream()
                .map(arr -> new Lakare((String) arr[0], (String) arr[1]))
                .collect(Collectors.toList());

        // We need to maintain backwards compatibility. When FragaSvar no longer exist remove this part and return above
        // arendeList
        List<Lakare> fragaSvarList = fragaSvarService.getFragaSvarHsaIdByEnhet(enhetsId);
        return Lakare.merge(arendeList, fragaSvarList);
    }

    @Override
    public List<ArendeConversationView> getArenden(String intygsId) {
        List<Arende> arendeList = arendeRepository.findByIntygsId(intygsId);

        List<String> hsaEnhetIds = webcertUserService.getUser().getIdsOfSelectedVardenhet();

        return arendeViewConverter
                .buildArendeConversations(arendeList.stream().filter(a -> hsaEnhetIds.contains(a.getEnhetId())).collect(Collectors.toList()));
    }

    @Override
    @Transactional(value = "jpaTransactionManager", readOnly = true)
    public QueryFragaSvarResponse filterArende(QueryFragaSvarParameter filterParameters) {

        WebCertUser user = webcertUserService.getUser();
        Set<String> intygstyperForPrivilege = authoritiesHelper.getIntygstyperForPrivilege(user, AuthoritiesConstants.PRIVILEGE_VISA_INTYG);

        Filter filter;
        if (StringUtils.isNotEmpty(filterParameters.getEnhetId())) {
            verifyEnhetsAuth(filterParameters.getEnhetId(), true);
            filter = FilterConverter.convert(filterParameters, Arrays.asList(filterParameters.getEnhetId()), intygstyperForPrivilege);
        } else {
            filter = FilterConverter.convert(filterParameters, user.getIdsOfSelectedVardenhet(), intygstyperForPrivilege);
        }

        int originalStartFrom = filter.getStartFrom();
        int originalPageSize = filter.getPageSize();

        // update page size and start from to be able to merge FragaSvar and Arende properly
        filter.setStartFrom(Integer.valueOf(0));
        filter.setPageSize(originalPageSize + originalStartFrom);

        List<ArendeListItem> results = arendeRepository.filterArende(filter).stream()
                .map(ArendeListItemConverter::convert)
                .filter(Objects::nonNull)
                // We need to decorate the ArendeListItem with information whether there exist a reminder or not because
                // they want to display this information to the user. We cannot do this without a database access, hence
                // we do it after the convert
                .map(item -> {
                    item.setPaminnelse(!arendeRepository.findByPaminnelseMeddelandeId(item.getMeddelandeId()).isEmpty());
                    return item;
                })
                .collect(Collectors.toList());
        QueryFragaSvarResponse fsResults = fragaSvarService.filterFragaSvar(filter);

        int totalResultsCount = arendeRepository.filterArendeCount(filter) + fsResults.getTotalCount();

        results.addAll(fsResults.getResults());
        results.sort(Comparator.comparing(ArendeListItem::getReceivedDate).reversed());

        QueryFragaSvarResponse response = new QueryFragaSvarResponse();
        if (originalStartFrom >= results.size()) {
            response.setResults(new ArrayList<>());
        } else {
            response.setResults(results.subList(originalStartFrom, Math.min(originalPageSize + originalStartFrom, results.size())));
        }
        response.setTotalCount(totalResultsCount);

        return response;
    }

    @Override
    @Transactional
    public ArendeConversationView closeArendeAsHandled(String meddelandeId, String intygTyp) {
        if (Fk7263EntryPoint.MODULE_ID.equalsIgnoreCase(intygTyp)) {
            fragaSvarService.closeQuestionAsHandled(Long.parseLong(meddelandeId));
            return null;
        } else {
            Arende closedArende = closeArendeAsHandled(lookupArende(meddelandeId));
            return arendeViewConverter.convertToArendeConversationView(closedArende,
                    arendeRepository.findBySvarPaId(meddelandeId).stream().findFirst().orElse(null),
                    arendeRepository.findByPaminnelseMeddelandeId(meddelandeId));
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
    public void closeAllNonClosed(String intygsId) {

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
        if ((vardenheterIds == null) || vardenheterIds.isEmpty()) {
            LOG.warn("No ids for Vardenheter was supplied");
            return new HashMap<>();
        }

        if ((intygsTyper == null) || intygsTyper.isEmpty()) {
            LOG.warn("No intygsTyper for querying Arenden was supplied");
            return new HashMap<>();
        }

        return arendeRepository.countUnhandledGroupedByEnhetIdsAndIntygstyper(vardenheterIds, intygsTyper)
                .stream()
                .collect(Collectors.toMap(a -> (String) a[0], a -> (Long) a[1]));
    }

    private void verifyEnhetsAuth(String enhetsId, boolean isReadOnlyOperation) {
        if (!webcertUserService.isAuthorizedForUnit(enhetsId, isReadOnlyOperation)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
                    "User not authorized for enhet " + enhetsId);
        }
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
        }
    }

    private Arende processOutgoingMessage(Arende arende, NotificationEvent notificationEvent) throws WebCertServiceException {
        Arende saved = arendeRepository.save(arende);
        monitoringLog.logArendeCreated(arende.getIntygsId(), arende.getIntygTyp(), arende.getEnhetId(), arende.getAmne(),
                arende.getSvarPaId() != null);

        updateRelated(arende);

        SendMessageToRecipientType request = SendMessageToRecipientTypeBuilder.build(arende, webcertUserService.getUser(),
                sendMessageToFKLogicalAddress);

        // Send to recipient
        try {
            certificateSenderService.sendMessageToRecipient(arende.getIntygsId(), SendMessageToRecipientTypeConverter.toXml(request));
        } catch (JAXBException | CertificateSenderException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, e.getMessage());
        }

        sendNotification(saved, notificationEvent);

        return saved;
    }

    private void updateRelated(Arende arende) {
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
        NotificationEvent notificationEvent = determineNotificationEvent(arendeToClose, false);
        arendeToClose.setStatus(Status.CLOSED);
        Arende closedArende = arendeRepository.save(arendeToClose);

        sendNotification(closedArende, notificationEvent);

        return closedArende;
    }
}
