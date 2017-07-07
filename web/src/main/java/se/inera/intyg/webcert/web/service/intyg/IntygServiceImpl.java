/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.intyg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.modules.converter.InternalConverterUtil;
import se.inera.intyg.common.support.modules.registry.IntygModule;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateResponse;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.modules.support.api.notification.ArendeCount;
import se.inera.intyg.common.support.peristence.dao.util.DaoUtil;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.UtkastStatus;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.converter.IntygDraftsConverter;
import se.inera.intyg.webcert.web.converter.util.IntygConverterUtil;
import se.inera.intyg.webcert.web.security.WebCertUserOriginType;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.certificatesender.CertificateSenderException;
import se.inera.intyg.webcert.web.service.certificatesender.CertificateSenderService;
import se.inera.intyg.webcert.web.service.intyg.config.SendIntygConfiguration;
import se.inera.intyg.webcert.web.service.intyg.converter.IntygModuleFacade;
import se.inera.intyg.webcert.web.service.intyg.converter.IntygModuleFacadeException;
import se.inera.intyg.webcert.web.service.intyg.decorator.IntygRelationHelper;
import se.inera.intyg.webcert.web.service.intyg.decorator.UtkastIntygDecorator;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygPdf;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygServiceResult;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygWithNotificationsRequest;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygWithNotificationsResponse;
import se.inera.intyg.webcert.web.service.log.LogRequestFactory;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.FragorOchSvarCreator;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.relation.CertificateRelationService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v3.ListCertificatesForCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v3.ListCertificatesForCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v3.ListCertificatesForCareType;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author andreaskaltenbach
 */
@Service
public class IntygServiceImpl implements IntygService {

    private static final Logger LOG = LoggerFactory.getLogger(IntygServiceImpl.class);

    @Value("${intygstjanst.logicaladdress}")
    private String logicalAddress;

    @Autowired
    private ListCertificatesForCareResponderInterface listCertificateService;

    @Autowired
    private WebCertUserService webCertUserService;

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private IntygModuleFacade modelFacade;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private LogService logService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MonitoringLogService monitoringService;

    @Autowired
    private ArendeService arendeService;

    @Autowired
    private FragorOchSvarCreator fragorOchSvarCreator;

    @Autowired
    private CertificateSenderService certificateSenderService;

    @Autowired
    private UtkastIntygDecorator utkastIntygDecorator;

    @Autowired
    private IntygDraftsConverter intygConverter;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IntygRelationHelper intygRelationHelper;

    @Autowired
    private CertificateRelationService certificateRelationService;

    @Autowired
    private AuthoritiesHelper authoritiesHelper;

    @Autowired
    private PatientDetailsResolver patientDetailsResolver;

    @Override
    public IntygContentHolder fetchIntygData(String intygsId, String intygsTyp, boolean coherentJournaling) {
        return fetchIntygData(intygsId, intygsTyp, false, coherentJournaling);
    }

    @Override
    public IntygContentHolder fetchIntygDataWithRelations(String intygId, String intygsTyp, boolean coherentJournaling) {
        return fetchIntygData(intygId, intygsTyp, true, coherentJournaling);
    }

    /**
     * Returns the IntygContentHolder. Used both externally to frontend and internally in the modules.
     *
     * @param intygsId
     *            the identifier of the intyg.
     * @param intygsTyp
     *            the typ of the intyg. Used to call the correct module.
     * @param relations
     *            If the relations between intyg should be populated. This can be expensive (several database
     *            operations). Use sparsely.
     * @return
     */
    private IntygContentHolder fetchIntygData(String intygsId, String intygsTyp, boolean relations, boolean coherentJournaling) {
        IntygContentHolder intygsData = getIntygData(intygsId, intygsTyp, relations);
        LogRequest logRequest = LogRequestFactory.createLogRequestFromUtlatande(intygsData.getUtlatande(), coherentJournaling);

        if (!coherentJournaling) {
            verifyEnhetsAuth(intygsData.getUtlatande(), true);
        }

        // Log read to PDL
        logService.logReadIntyg(logRequest);

        // Log read to monitoring log
        monitoringService.logIntygRead(intygsId, intygsTyp);

        return intygsData;
    }

    @Override
    public Pair<List<ListIntygEntry>, Boolean> listIntyg(List<String> enhetId, Personnummer personnummer) {
        ListCertificatesForCareType request = new ListCertificatesForCareType();
        request.setPersonId(InternalConverterUtil.getPersonId(personnummer));
        for (String id : enhetId) {
            request.getEnhetsId().add(InternalConverterUtil.getHsaId(id));
        }

        try {
            ListCertificatesForCareResponseType response = listCertificateService.listCertificatesForCare(logicalAddress, request);

            List<ListIntygEntry> fullIntygItemList = intygConverter
                    .convertIntygToListIntygEntries(response.getIntygsLista().getIntyg());

            intygRelationHelper.decorateIntygListWithRelations(fullIntygItemList);

            fullIntygItemList = filterByIntygTypeForUser(fullIntygItemList);
            addDraftsToListForIntygNotSavedInIntygstjansten(fullIntygItemList, enhetId, personnummer);
            return Pair.of(fullIntygItemList, Boolean.FALSE);

        } catch (WebServiceException wse) {
            LOG.warn("Error when connecting to intygstjänsten: {}", wse.getMessage());
        }

        // If intygstjansten was unavailable, we return whatever certificates we can find and clearly inform
        // the caller that the set of certificates are only those that have been issued by WebCert.
        List<ListIntygEntry> intygItems = buildIntygItemListFromDrafts(enhetId, personnummer);
        for (ListIntygEntry lie : intygItems) {
            lie.setRelations(certificateRelationService.getRelations(lie.getIntygId()));
        }
        return Pair.of(intygItems, Boolean.TRUE);
    }

    private List<ListIntygEntry> filterByIntygTypeForUser(List<ListIntygEntry> fullIntygItemList) {
        // Get intygstyper from the view privilege
        Set<String> intygsTyper = authoritiesHelper.getIntygstyperForPrivilege(webCertUserService.getUser(),
                AuthoritiesConstants.PRIVILEGE_VISA_INTYG);

        // The user is only granted access to view intyg of intygstyper that are in the set.
        return fullIntygItemList.stream().filter(i -> intygsTyper.contains(i.getIntygType())).collect(Collectors.toList());
    }

    /**
     * Adds any IntygItems found in Webcert for this patient not present in the list from intygstjansten.
     */
    private void addDraftsToListForIntygNotSavedInIntygstjansten(List<ListIntygEntry> fullIntygItemList, List<String> enhetId,
            Personnummer personnummer) {
        List<ListIntygEntry> intygItems = buildIntygItemListFromDrafts(enhetId, personnummer);
        intygItems.removeAll(fullIntygItemList);
        fullIntygItemList.addAll(intygItems);
    }

    private List<ListIntygEntry> buildIntygItemListFromDrafts(List<String> enhetId, Personnummer personnummer) {
        List<UtkastStatus> statuses = new ArrayList<>();
        statuses.add(UtkastStatus.SIGNED);

        Set<String> intygsTyper = authoritiesHelper.getIntygstyperForPrivilege(webCertUserService.getUser(),
                AuthoritiesConstants.PRIVILEGE_VISA_INTYG);

        List<Utkast> drafts = utkastRepository.findDraftsByPatientAndEnhetAndStatus(DaoUtil.formatPnrForPersistence(personnummer), enhetId,
                statuses,
                intygsTyper);

        return IntygDraftsConverter.convertUtkastsToListIntygEntries(drafts);
    }

    @Override
    public IntygPdf fetchIntygAsPdf(String intygsId, String intygsTyp, boolean isEmployer) {
        try {
            LOG.debug("Fetching intyg '{}' as PDF", intygsId);

            IntygContentHolder intyg = getIntygDataPreferWebcert(intygsId, intygsTyp);
            boolean coherentJournaling = userIsDjupintegreradWithSjf();
            if (!coherentJournaling) {
                verifyEnhetsAuth(intyg.getUtlatande(), true);
            }

            IntygPdf intygPdf = modelFacade.convertFromInternalToPdfDocument(intygsTyp, intyg.getContents(), intyg.getStatuses(),
                    isEmployer);

            // Log print as PDF to PDL log
            logPdfPrinting(intyg, coherentJournaling);

            return intygPdf;

        } catch (IntygModuleFacadeException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
        }
    }

    /**
     * Returns true if user has Origin DJUPINTEGRATION and Integration parameter sjf=true.
     */
    private boolean userIsDjupintegreradWithSjf() {
        WebCertUser user = webCertUserService.getUser();
        return user.getOrigin().equals(WebCertUserOriginType.DJUPINTEGRATION.name())
                && user.getParameters().isSjf();
    }

    /**
     * Creates log events for PDF printing actions. Creates both PDL and monitoring log events
     * depending the state of the intyg.
     *
     * @param intyg
     */
    private void logPdfPrinting(IntygContentHolder intyg, boolean coherentJournaling) {

        final String intygsId = intyg.getUtlatande().getId();
        final String intygsTyp = intyg.getUtlatande().getTyp();

        LogRequest logRequest = LogRequestFactory.createLogRequestFromUtlatande(intyg.getUtlatande(), coherentJournaling);

        // Are we printing a draft?
        if (intyg.getUtlatande().getGrundData().getSigneringsdatum() == null) {
            // Log printing of draft
            logService.logPrintIntygAsDraft(logRequest);
            monitoringService.logUtkastPrint(intygsId, intygsTyp);
        } else {
            // Log printing of intyg
            if (intyg.isRevoked()) {
                logService.logPrintRevokedIntygAsPDF(logRequest);
                monitoringService.logRevokedPrint(intygsId, intygsTyp);
            } else {
                logService.logPrintIntygAsPDF(logRequest);
                monitoringService.logIntygPrintPdf(intygsId, intygsTyp);
            }
        }
    }

    @Override
    public IntygServiceResult storeIntyg(Utkast utkast) {

        // Audit log
        monitoringService.logIntygRegistered(utkast.getIntygsId(), utkast.getIntygsTyp());
        try {
            certificateSenderService.storeCertificate(utkast.getIntygsId(), utkast.getIntygsTyp(), utkast.getModel());
            return IntygServiceResult.OK;
        } catch (CertificateSenderException cse) {
            LOG.error("Could not put certificate store message on queue: " + cse.getMessage());
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, cse);
        }
    }

    @Override
    public IntygServiceResult sendIntyg(String intygsId, String typ, String recipient) {

        Utlatande intyg = getUtlatandeForIntyg(intygsId, typ);
        verifyEnhetsAuth(intyg, true);

        if (isRevoked(intygsId, typ, false)) {
            LOG.debug("Cannot send certificate with id '{}', the certificate is revoked", intygsId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Certificate is revoked");
        }

        verifyNotReplacedBySignedIntyg(intygsId, "send");

        SendIntygConfiguration sendConfig = new SendIntygConfiguration(recipient, webCertUserService.getUser());

        monitoringService.logIntygSent(intygsId, recipient);

        // send PDL log event
        LogRequest logRequest = LogRequestFactory.createLogRequestFromUtlatande(intyg);
        logRequest.setAdditionalInfo(sendConfig.getPatientConsentMessage());
        logService.logSendIntygToRecipient(logRequest);

        markUtkastWithSendDateAndRecipient(intygsId, recipient);

        return sendIntygToCertificateSender(sendConfig, intyg);
    }

    private void verifyNotReplacedBySignedIntyg(String intygsId, String operation) {
        final Optional<WebcertCertificateRelation> replacedByRelation = certificateRelationService.getNewestRelationOfType(intygsId,
                RelationKod.ERSATT, Arrays.asList(UtkastStatus.SIGNED));
        if (replacedByRelation.isPresent()) {
            String errorString = String.format("Cannot %s certificate '%s', the certificate is replaced by certificate '%s'",
                    operation, intygsId, replacedByRelation.get().getIntygsId());
            LOG.debug(errorString);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                    errorString);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see se.inera.intyg.webcert.web.service.intyg.IntygService#revokeIntyg(java.lang.String, java.lang.String)
     */
    @Override
    public IntygServiceResult revokeIntyg(String intygsId, String intygsTyp, String revokeMessage, String reason) {
        LOG.debug("Attempting to revoke intyg {}", intygsId);
        IntygContentHolder intyg = getIntygData(intygsId, intygsTyp, false);
        verifyEnhetsAuth(intyg.getUtlatande(), true);
        verifyIsSigned(intyg.getStatuses());

        if (intyg.isRevoked()) {
            LOG.debug("Certificate with id '{}' is already revoked", intygsId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Certificate is already revoked");
        }

        try {
            certificateSenderService.revokeCertificate(intygsId, modelFacade.getRevokeCertificateRequest(intygsTyp, intyg.getUtlatande(),
                    IntygConverterUtil.buildHosPersonalFromWebCertUser(webCertUserService.getUser(), null), revokeMessage), intygsTyp);
            whenSuccessfulRevoke(intyg.getUtlatande(), reason);
            return IntygServiceResult.OK;
        } catch (CertificateSenderException | ModuleException | IntygModuleFacadeException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM, e.getMessage());
        }
    }

    @Override
    public void handleSignedCompletion(Utkast utkast, String recipient) {
        if (RelationKod.KOMPLT != utkast.getRelationKod()) {
            return;
        }

        LOG.info("Send komplettering '{}' directly to recipient", utkast.getIntygsId());
        sendIntyg(utkast.getIntygsId(), utkast.getIntygsTyp(), recipient);
        LOG.info("Set komplettering QAs as handled for {}", utkast.getRelationIntygsId());
        arendeService.closeCompletionsAsHandled(utkast.getRelationIntygsId(), utkast.getIntygsTyp());
    }

    @Override
    public String getIssuingVardenhetHsaId(String intygId, String intygsTyp) {
        try {
            IntygContentHolder intygData = getIntygData(intygId, intygsTyp, false);
            return intygData.getUtlatande().getGrundData().getSkapadAv().getVardenhet().getEnhetsid();
        } catch (WebCertServiceException e) {
            if (e.getErrorCode() == WebCertServiceErrorCodeEnum.DATA_NOT_FOUND) {
                return null;
            }
            throw e;
        }
    }

    @Override
    public boolean isRevoked(String intygsId, String intygsTyp, boolean coherentJournaling) {

        IntygContentHolder intygData = getIntygData(intygsId, intygsTyp, false);
        // Log read of revoke status to monitoring log
        monitoringService.logIntygRevokeStatusRead(intygsId, intygsTyp);
        return intygData.isRevoked();
    }

    @Override
    public List<IntygWithNotificationsResponse> listCertificatesForCareWithQA(IntygWithNotificationsRequest request) {
        List<Utkast> utkastList;
        if (request.shouldUseEnhetId()) {
            utkastList = utkastRepository.findDraftsByPatientAndEnhetAndStatus(
                    DaoUtil.formatPnrForPersistence(request.getPersonnummer()), request.getEnhetId(), Arrays.asList(UtkastStatus.values()),
                    moduleRegistry.listAllModules().stream().map(IntygModule::getId).collect(Collectors.toSet()));
        } else {
            utkastList = utkastRepository.findDraftsByPatientAndVardgivareAndStatus(
                    DaoUtil.formatPnrForPersistence(request.getPersonnummer()), request.getVardgivarId(),
                    Arrays.asList(UtkastStatus.values()),
                    moduleRegistry.listAllModules().stream().map(IntygModule::getId).collect(Collectors.toSet()));
        }

        List<IntygWithNotificationsResponse> res = new ArrayList<>();
        for (Utkast utkast : utkastList) {
            List<Handelse> notifications = notificationService.getNotifications(utkast.getIntygsId());

            // We still want to return the reference even if the SKAPAT was not in the time span. Hence we need to
            // extract this information before filtering.
            String ref = notifications.stream()
                    .filter(h -> HandelsekodEnum.SKAPAT == h.getCode())
                    .findAny()
                    .map(Handelse::getRef).orElse(null);

            notifications = notifications.stream().filter(handelse -> {
                if (request.getStartDate() != null && handelse.getTimestamp().isBefore(request.getStartDate())) {
                    return false;
                }
                if (request.getEndDate() != null && handelse.getTimestamp().isAfter(request.getEndDate())) {
                    return false;
                }
                return true;
            }).collect(Collectors.toList());

            // If the request contained either start date or end date we should not return any intyg with no handelse in
            // this time span
            if ((request.getStartDate() != null || request.getEndDate() != null) && notifications.isEmpty()) {
                continue;
            }

            try {
                ModuleApi api = moduleRegistry.getModuleApi(utkast.getIntygsTyp());
                Intyg intyg = api.getIntygFromUtlatande(api.getUtlatandeFromJson(utkast.getModel()));
                Pair<ArendeCount, ArendeCount> arenden = fragorOchSvarCreator.createArenden(utkast.getIntygsId(),
                        utkast.getIntygsTyp());
                res.add(new IntygWithNotificationsResponse(intyg, notifications, arenden.getLeft(), arenden.getRight(), ref));
            } catch (ModuleNotFoundException | ModuleException | IOException e) {
                LOG.error("Could not convert intyg {} to external format", utkast.getIntygsId());
            }
        }
        return res;
    }

    public void setLogicalAddress(String logicalAddress) {
        this.logicalAddress = logicalAddress;
    }

    /* --------------------- Protected scope --------------------- */

    protected IntygServiceResult sendIntygToCertificateSender(SendIntygConfiguration sendConfig, Utlatande intyg) {

        String intygsId = intyg.getId();
        String recipient = sendConfig.getRecipient();
        String intygsTyp = intyg.getTyp();
        HoSPersonal skickatAv = IntygConverterUtil.buildHosPersonalFromWebCertUser(webCertUserService.getUser(), null);

        try {
            LOG.debug("Sending intyg {} of type {} to recipient {}", intygsId, intygsTyp, recipient);

            // Ask the certificateSenderService to post a 'send' message onto the queue.
            certificateSenderService.sendCertificate(intygsId, intyg.getGrundData().getPatient().getPersonId(),
                    objectMapper.writeValueAsString(skickatAv), recipient);

            // Notify stakeholders when a certificate is sent
            notificationService.sendNotificationForIntygSent(intygsId, getUserReference());

            return IntygServiceResult.OK;

        } catch (WebServiceException wse) {
            LOG.error("An WebServiceException occured when trying to send intyg: " + intygsId, wse);
            return IntygServiceResult.FAILED;
        } catch (RuntimeException e) {
            LOG.error("Module problems occured when trying to send intyg " + intygsId, e);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
        } catch (JsonProcessingException e) {
            LOG.error("Error writing skickatAv as string when trying to send intyg " + intygsId, e);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
        } catch (CertificateSenderException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
        }
    }

    protected void verifyEnhetsAuth(Utlatande utlatande, boolean isReadOnlyOperation) {
        Vardenhet vardenhet = utlatande.getGrundData().getSkapadAv().getVardenhet();
        if (!webCertUserService.isAuthorizedForUnit(vardenhet.getVardgivare().getVardgivarid(), vardenhet.getEnhetsid(),
                isReadOnlyOperation)) {
            String msg = "User not authorized for enhet " + vardenhet.getEnhetsid();
            LOG.debug(msg);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM, msg);
        }
    }

    /* --------------------- Private scope --------------------- */

    private void verifyIsSigned(List<Status> statuses) {
        statuses.stream()
                .filter(status -> CertificateState.RECEIVED.equals(status.getType()) && status.getTimestamp() != null)
                .findAny()
                .orElseThrow(() -> new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                        "Certificate is not signed, cannot revoke an unsigned certificate"));
    }

    /**
     * Builds a IntygContentHolder by first trying to get the Intyg from intygstjansten. If
     * not found or the Intygstjanst couldn't be reached, the local Utkast - if available -
     * will be used instead.
     *
     * Note that even when found, we check if we need to decorate the response with data from the utkast in order
     * to mitigate async send states. (E.g. a send may be in resend due to 3rd party issues, in that case decorate with
     * data about sent state from the Utkast)
     *
     * @param relations
     */
    private IntygContentHolder getIntygData(String intygId, String typ, boolean relations) {
        try {
            CertificateResponse certificate = modelFacade.getCertificate(intygId, typ);
            String internalIntygJsonModel = certificate.getInternalModel();

            // INTYG-4086: Patient object populated according to ruleset for the intygstyp at hand.
            // Since an FK-intyg never will have anything other than personId, try to fetch all using ruleset
            Patient patient = patientDetailsResolver.resolvePatient(certificate.getUtlatande().getGrundData().getPatient().getPersonId(),
                    typ);
            // Get the module api and use the "updateBeforeSave" to update the outbound "model" with the
            // Patient object. NOTE! It's possible we should remove all usage of grundData.patient in the GUI, instead
            // we could rely on something new on the IntygContentHolder more transient in nature.
            ModuleApi moduleApi = moduleRegistry.getModuleApi(typ);
            String updatedModel = moduleApi.updateBeforeSave(internalIntygJsonModel, patient);

            utkastIntygDecorator.decorateWithUtkastStatus(certificate);
            Relations certificateRelations = intygRelationHelper.getRelationsForIntyg(intygId);

            return IntygContentHolder.builder()
                    .setContents(updatedModel)
                    .setUtlatande(certificate.getUtlatande())
                    .setStatuses(certificate.getMetaData().getStatus())
                    .setRevoked(certificate.isRevoked())
                    .setRelations(certificateRelations)
                    .setDeceased(isDeceased(certificate.getUtlatande().getGrundData().getPatient().getPersonId()))
                    .setSekretessmarkering(patientDetailsResolver
                            .isSekretessmarkering(certificate.getUtlatande().getGrundData().getPatient().getPersonId()))
                    .build();

        } catch (IntygModuleFacadeException me) {
            // It's possible the Intygstjanst hasn't received the Intyg yet, look for it locally before rethrowing
            // exception
            Utkast utkast = utkastRepository.findOneByIntygsIdAndIntygsTyp(intygId, typ);
            if (utkast == null) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
            }
            return buildIntygContentHolderForUtkast(utkast, relations);
        } catch (WebServiceException wse) {
            // Something went wrong communication-wise, try to find a matching Utkast instead.
            Utkast utkast = utkastRepository.findOneByIntygsIdAndIntygsTyp(intygId, typ);
            if (utkast == null) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
                        "Cannot get intyg. Intygstjansten was not reachable and the Utkast could "
                                + "not be found, perhaps it was issued by a non-webcert system?");
            }
            return buildIntygContentHolderForUtkast(utkast, relations);
        } catch (ModuleNotFoundException | ModuleException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
        }
    }

    /**
     * As the name of the method implies, this method builds a IntygContentHolder instance
     * from the Utkast stored in Webcert. If not present, it will try to fetch from Intygstjansten
     * instead.
     */
    private IntygContentHolder getIntygDataPreferWebcert(String intygId, String intygTyp) {
        Utkast utkast = utkastRepository.findOne(intygId);
        return (utkast != null) ? buildIntygContentHolderForUtkast(utkast, false) : getIntygData(intygId, intygTyp, false);
    }

    private IntygContentHolder buildIntygContentHolderForUtkast(Utkast utkast, boolean relations) {

        // try {
        // INTYG-4086: Patient object populated according to ruleset for the intygstyp at hand.
        // Patient patient = patientDetailsResolver.resolvePatient(utkast.getPatientPersonnummer(),
        // utkast.getIntygsTyp());
        // String updatedModel = moduleRegistry.getModuleApi(utkast.getIntygsTyp()).updateBeforeSave(utkast.getModel(),
        // patient);

        Utlatande utlatande = modelFacade.getUtlatandeFromInternalModel(utkast.getIntygsTyp(), utkast.getModel());
        List<Status> statuses = IntygConverterUtil.buildStatusesFromUtkast(utkast);
        Relations certificateRelations = certificateRelationService.getRelations(utkast.getIntygsId());

        return IntygContentHolder.builder()
                .setContents(utkast.getModel())
                .setUtlatande(utlatande)
                .setStatuses(statuses)
                .setRevoked(utkast.getAterkalladDatum() != null)
                .setRelations(certificateRelations)
                .setDeceased(isDeceased(utkast.getPatientPersonnummer()))
                .setSekretessmarkering(patientDetailsResolver.isSekretessmarkering(utkast.getPatientPersonnummer()))
                .build();

        // } catch (ModuleException | ModuleNotFoundException e) {
        // throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
        // }

    }

    private Utlatande getUtlatandeForIntyg(String intygId, String typ) {
        Utkast utkast = utkastRepository.findOne(intygId);
        return (utkast != null) ? modelFacade.getUtlatandeFromInternalModel(utkast.getIntygsTyp(), utkast.getModel())
                : getIntygData(intygId, typ, false).getUtlatande();
    }

    /**
     * Send a notification message to stakeholders informing that
     * a question related to a revoked certificate has been closed.
     */
    private IntygServiceResult whenSuccessfulRevoke(Utlatande intyg, String reason) {
        String intygsId = intyg.getId();

        String hsaId = webCertUserService.getUser().getHsaId();
        monitoringService.logIntygRevoked(intygsId, hsaId, reason);

        // First: send a notification informing stakeholders that this certificate has been revoked
        notificationService.sendNotificationForIntygRevoked(intygsId, getUserReference());

        // Second: send a notification informing stakeholders that all questions related to the revoked
        // certificate has been closed.
        arendeService.closeAllNonClosed(intygsId);

        // Third: create a log event
        LogRequest logRequest = LogRequestFactory.createLogRequestFromUtlatande(intyg);
        logService.logRevokeIntyg(logRequest);

        // Fourth: mark the originating Utkast as REVOKED
        markUtkastWithRevokedDate(intygsId);

        return IntygServiceResult.OK;
    }

    private void markUtkastWithSendDateAndRecipient(String intygsId, String recipient) {
        Utkast utkast = utkastRepository.findOne(intygsId);
        if (utkast != null) {
            utkast.setSkickadTillMottagareDatum(LocalDateTime.now());
            utkast.setSkickadTillMottagare(recipient);
            utkastRepository.save(utkast);
        }
    }

    private void markUtkastWithRevokedDate(String intygsId) {
        Utkast utkast = utkastRepository.findOne(intygsId);
        if (utkast != null) {
            utkast.setAterkalladDatum(LocalDateTime.now());
            utkastRepository.save(utkast);
        }
    }

    private boolean isDeceased(Personnummer personnummer) {
        WebCertUser user = webCertUserService.getUser();
        if (WebCertUserOriginType.DJUPINTEGRATION.name().equals(user.getOrigin())) {
            if (user.getParameters() != null) {
                return user.getParameters().isPatientDeceased();
            } else {
                return false;
            }
        } else {
            return patientDetailsResolver.isAvliden(personnummer);
        }
    }

    private String getUserReference() {
        WebCertUser user = webCertUserService.getUser();
        return user.getParameters() != null ? user.getParameters().getReference() : null;
    }
}
