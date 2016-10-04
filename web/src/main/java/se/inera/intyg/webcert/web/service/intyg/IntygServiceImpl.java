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

package se.inera.intyg.webcert.web.service.intyg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.security.authorities.validation.AuthExpectationSpecImpl;
import se.inera.intyg.common.security.authorities.AuthoritiesHelper;
import se.inera.intyg.common.security.common.model.AuthoritiesConstants;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.modules.converter.InternalConverterUtil;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateResponse;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.peristence.dao.util.DaoUtil;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.converter.IntygDraftsConverter;
import se.inera.intyg.webcert.web.converter.util.IntygConverterUtil;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.certificatesender.CertificateSenderException;
import se.inera.intyg.webcert.web.service.certificatesender.CertificateSenderService;
import se.inera.intyg.webcert.web.service.intyg.config.SendIntygConfiguration;
import se.inera.intyg.webcert.web.service.intyg.converter.IntygModuleFacade;
import se.inera.intyg.webcert.web.service.intyg.converter.IntygModuleFacadeException;
import se.inera.intyg.webcert.web.service.intyg.decorator.UtkastIntygDecorator;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygPdf;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygServiceResult;
import se.inera.intyg.webcert.web.service.log.LogRequestFactory;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.relation.RelationService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.RelationItem;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v2.ListCertificatesForCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v2.ListCertificatesForCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v2.ListCertificatesForCareType;

import javax.xml.ws.WebServiceException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private LogService logService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MonitoringLogService monitoringService;

    @Autowired
    private ArendeService arendeService;

    @Autowired
    private CertificateSenderService certificateSenderService;

    @Autowired
    private UtkastIntygDecorator utkastIntygDecorator;

    @Autowired
    private IntygDraftsConverter intygConverter;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RelationService relationService;

    @Autowired
    private AuthoritiesHelper authoritiesHelper;

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

            switch (response.getResult().getResultCode()) {
            case OK:
                List<ListIntygEntry> fullIntygItemList = intygConverter.convertIntygToListIntygEntries(response.getIntygsLista().getIntyg());
                fullIntygItemList = filterByIntygTypeForUser(fullIntygItemList);
                addDraftsToListForIntygNotSavedInIntygstjansten(fullIntygItemList, enhetId, personnummer);
                return Pair.of(fullIntygItemList, Boolean.FALSE);
            default:
                LOG.error("Error when calling webservice ListCertificatesForCare: {}", response.getResult().getResultText());
            }

        } catch (WebServiceException wse) {
            LOG.warn("Error when connecting to intygstjänsten: {}", wse.getMessage());
        }

        // If intygstjansten was unavailable, we return whatever certificates we can find and clearly inform
        // the caller that the set of certificates are only those that have been issued by WebCert.
        List<ListIntygEntry> intygItems = buildIntygItemListFromDrafts(enhetId, personnummer);
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

        List<Utkast> drafts = utkastRepository.findDraftsByPatientAndEnhetAndStatus(DaoUtil.formatPnrForPersistence(personnummer), enhetId, statuses,
                intygsTyper);

        return IntygDraftsConverter.convertUtkastsToListIntygEntries(drafts);
    }

    @Override
    public IntygPdf fetchIntygAsPdf(String intygsId, String intygsTyp, boolean isEmployer) {
        try {
            LOG.debug("Fetching intyg '{}' as PDF", intygsId);

            IntygContentHolder intyg = getIntygDataPreferWebcert(intygsId, intygsTyp);

            verifyEnhetsAuth(intyg.getUtlatande(), true);

            IntygPdf intygPdf = modelFacade.convertFromInternalToPdfDocument(intygsTyp, intyg.getContents(), intyg.getStatuses(), isEmployer);

            // Log print as PDF to PDL log
            LogRequest logRequest = LogRequestFactory.createLogRequestFromUtlatande(intyg.getUtlatande());
            logService.logPrintIntygAsPDF(logRequest);

            // Log print as PDF to monitoring log
            monitoringService.logIntygPrintPdf(intygsId, intygsTyp);

            return intygPdf;

        } catch (IntygModuleFacadeException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
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

        SendIntygConfiguration sendConfig = new SendIntygConfiguration(recipient, webCertUserService.getUser());

        monitoringService.logIntygSent(intygsId, recipient);

        // send PDL log event
        LogRequest logRequest = LogRequestFactory.createLogRequestFromUtlatande(intyg);
        logRequest.setAdditionalInfo(sendConfig.getPatientConsentMessage());
        logService.logSendIntygToRecipient(logRequest);

        markUtkastWithSendDateAndRecipient(intygsId, recipient);

        return sendIntygToCertificateSender(sendConfig, intyg);
    }

    /*
     * (non-Javadoc)
     *
     * @see se.inera.intyg.webcert.web.service.intyg.IntygService#revokeIntyg(java.lang.String, java.lang.String)
     */
    @Override
    public IntygServiceResult revokeIntyg(String intygsId, String intygsTyp, String revokeMessage) {
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
            whenSuccessfulRevoke(intyg.getUtlatande());
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
            notificationService.sendNotificationForIntygSent(intygsId);

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
        if (!webCertUserService.isAuthorizedForUnit(vardenhet.getVardgivare().getVardgivarid(), vardenhet.getEnhetsid(), isReadOnlyOperation)) {
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
            utkastIntygDecorator.decorateWithUtkastStatus(certificate);
            return new IntygContentHolder(internalIntygJsonModel,
                    certificate.getUtlatande(),
                    certificate.getMetaData().getStatus(),
                    certificate.isRevoked(),
                    relations ? relationService.getRelations(intygId)
                            .orElse(RelationItem.createBaseCase(intygId, certificate.getMetaData().getSignDate(), CertificateState.RECEIVED.name())) : null);

        } catch (IntygModuleFacadeException me) {
            // It's possible the Intygstjanst hasn't received the Intyg yet, look for it locally before rethrowing
            // exception
            Utkast utkast = utkastRepository.findOne(intygId);
            if (utkast == null) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
            }
            return buildIntygContentHolder(utkast, relations);
        } catch (WebServiceException wse) {
            // Something went wrong communication-wise, try to find a matching Utkast instead.
            Utkast utkast = utkastRepository.findOne(intygId);
            if (utkast == null) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
                        "Cannot get intyg. Intygstjansten was not reachable and the Utkast could "
                                + "not be found, perhaps it was issued by a non-webcert system?");
            }
            return buildIntygContentHolder(utkast, relations);
        }
    }

    /**
     * As the name of the method implies, this method builds a IntygContentHolder instance
     * from the Utkast stored in Webcert. If not present, it will try to fetch from Intygstjansten
     * instead.
     */
    private IntygContentHolder getIntygDataPreferWebcert(String intygId, String intygTyp) {
        Utkast utkast = utkastRepository.findOne(intygId);
        return (utkast != null) ? buildIntygContentHolder(utkast, false) : getIntygData(intygId, intygTyp, false);
    }

    private IntygContentHolder buildIntygContentHolder(Utkast utkast, boolean relations) {
        Utlatande utlatande = modelFacade.getUtlatandeFromInternalModel(utkast.getIntygsTyp(), utkast.getModel());
        List<Status> statuses = IntygConverterUtil.buildStatusesFromUtkast(utkast);
        return new IntygContentHolder(utkast.getModel(), utlatande, statuses, utkast.getAterkalladDatum() != null,
                relations ? relationService.getRelations(utkast.getIntygsId())
                        .orElse(RelationItem.createBaseCase(utkast)) : null);
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
    private IntygServiceResult whenSuccessfulRevoke(Utlatande intyg) {
        String intygsId = intyg.getId();

        String hsaId = webCertUserService.getUser().getHsaId();
        monitoringService.logIntygRevoked(intygsId, hsaId);

        // First: send a notification informing stakeholders that this certificate has been revoked
        notificationService.sendNotificationForIntygRevoked(intygsId);

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
}
