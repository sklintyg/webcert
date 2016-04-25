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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.xml.bind.JAXBException;
import javax.xml.ws.WebServiceException;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeType;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Relation;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateResponse;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.webcert.common.client.converter.RevokeRequestConverter;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.auth.authorities.AuthoritiesConstants;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.certificatesender.CertificateSenderException;
import se.inera.intyg.webcert.web.service.certificatesender.CertificateSenderService;
import se.inera.intyg.webcert.web.service.fragasvar.FragaSvarService;
import se.inera.intyg.webcert.web.service.fragasvar.dto.FrageStallare;
import se.inera.intyg.webcert.web.service.intyg.config.SendIntygConfiguration;
import se.inera.intyg.webcert.web.service.intyg.converter.IntygModuleFacade;
import se.inera.intyg.webcert.web.service.intyg.converter.IntygModuleFacadeException;
import se.inera.intyg.webcert.web.service.intyg.converter.IntygServiceConverter;
import se.inera.intyg.webcert.web.service.intyg.decorator.UtkastIntygDecorator;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygItem;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygItemListResponse;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygPdf;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygServiceResult;
import se.inera.intyg.webcert.web.service.log.LogRequestFactory;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.relation.RelationService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.web.controller.util.CertificateTypes;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareType;

/**
 * @author andreaskaltenbach
 */
@Service
public class IntygServiceImpl implements IntygService {

    public enum Event {
        REVOKE,
        SEND;
    }

    private static final Logger LOG = LoggerFactory.getLogger(IntygServiceImpl.class);

    /** LISU, LUSE.*/
    private static final List<String> FK_NEXT_GENERATION = Arrays.asList(CertificateTypes.LISU.toString(), CertificateTypes.LUSE.toString(), CertificateTypes.LUAE_NA.toString());

    @Value("${intygstjanst.logicaladdress}")
    private String logicalAddress;

    @Autowired
    private ListCertificatesForCareResponderInterface listCertificateService;

    @Autowired
    private WebCertUserService webCertUserService;

    @Autowired
    private RevokeRequestConverter revokeRequestConverter;

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private IntygModuleFacade modelFacade;

    @Autowired
    private IntygServiceConverter serviceConverter;

    @Autowired
    private LogService logService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MonitoringLogService monitoringService;

    @Autowired
    private FragaSvarService fragaSvarService;

    @Autowired
    private ArendeService arendeService;

    @Autowired
    private CertificateSenderService certificateSenderService;

    @Autowired
    private UtkastIntygDecorator utkastIntygDecorator;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RelationService relationService;

    @Override
    public IntygContentHolder fetchIntygData(String intygsId, String intygsTyp) {
        return fetchIntygData(intygsId, intygsTyp, false);
    }

    @Override
    public IntygContentHolder fetchIntygDataWithRelations(String intygId, String intygsTyp) {
        return fetchIntygData(intygId, intygsTyp, true);
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
    private IntygContentHolder fetchIntygData(String intygsId, String intygsTyp, boolean relations) {
        IntygContentHolder intygsData = getIntygData(intygsId, intygsTyp, relations);
        verifyEnhetsAuth(intygsData.getUtlatande(), true);

        // Log read to PDL
        LogRequest logRequest = LogRequestFactory.createLogRequestFromUtlatande(intygsData.getUtlatande());
        logService.logReadIntyg(logRequest);

        // Log read to monitoring log
        monitoringService.logIntygRead(intygsId, intygsTyp);

        return intygsData;
    }

    @Override
    public IntygItemListResponse listIntyg(List<String> enhetId, Personnummer personnummer) {
        ListCertificatesForCareType request = new ListCertificatesForCareType();
        request.setPersonId(personnummer.getPersonnummer());
        request.getEnhet().addAll(enhetId);

        try {
            ListCertificatesForCareResponseType response = listCertificateService.listCertificatesForCare(logicalAddress,
                    request);

            switch (response.getResult().getResultCode()) {
            case OK:
                List<IntygItem> fullIntygItemList = serviceConverter.convertToListOfIntygItem(response.getMeta());
                filterByIntygTypeForUser(fullIntygItemList);
                addDraftsToListForIntygNotSavedInIntygstjansten(fullIntygItemList, enhetId, personnummer);
                return new IntygItemListResponse(fullIntygItemList, false);
            default:
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.EXTERNAL_SYSTEM_PROBLEM,
                        "listCertificatesForCare WS call: ERROR :" + response.getResult().getResultText());
            }
        } catch (WebServiceException wse) {
            LOG.warn("Error when connecting to intygstjänsten: ", wse.getMessage());
            LOG.info("Stacktrace: ", wse.getStackTrace());
            wse.printStackTrace();
            if (wse.getCause() != null) {
                LOG.info(wse.getCause().toString());
            }
            // If intygstjansten was unavailable, we return whatever certificates we can find and clearly inform
            // the caller that the set of certificates are only those that have been issued by WebCert.
            List<IntygItem> intygItems = buildIntygItemListFromDrafts(enhetId, personnummer);
            return new IntygItemListResponse(intygItems, true);
        }
    }

    private void filterByIntygTypeForUser(List<IntygItem> fullIntygItemList) {
        // Get intygstyper from the view privilege
        Set<String> intygsTyper = webCertUserService.getIntygstyper(AuthoritiesConstants.PRIVILEGE_VISA_INTYG);

        // If intygstyper is an empty set, user are not granted access to view intyg of any intygstyp.
        if (intygsTyper.isEmpty()) {
            fullIntygItemList.clear();
            return;
        }

        // If there are intygstyper in the set, then user is only granted access to
        // view intyg of intygstyper that are in the set.
        Iterator<IntygItem> i = fullIntygItemList.iterator();
        while (i.hasNext()) {
            IntygItem intygItem = i.next();
            if (!intygsTyper.contains(intygItem.getType())) {
                i.remove();
            }
        }
    }

    /**
     * Adds any IntygItems found in Webcert for this patient not present in the list from intygstjansten.
     */
    private void addDraftsToListForIntygNotSavedInIntygstjansten(List<IntygItem> fullIntygItemList, List<String> enhetId, Personnummer personnummer) {
        List<IntygItem> intygItems = buildIntygItemListFromDrafts(enhetId, personnummer);

        intygItems.removeAll(fullIntygItemList);
        fullIntygItemList.addAll(intygItems);
    }

    private List<IntygItem> buildIntygItemListFromDrafts(List<String> enhetId, Personnummer personnummer) {
        List<UtkastStatus> statuses = new ArrayList<>();
        statuses.add(UtkastStatus.SIGNED);
        Set<String> intygsTyper = webCertUserService.getIntygstyper(AuthoritiesConstants.PRIVILEGE_VISA_INTYG);
        List<Utkast> drafts = utkastRepository.findDraftsByPatientAndEnhetAndStatus(personnummer.getPersonnummer(), enhetId, statuses, intygsTyper);
        return serviceConverter.convertDraftsToListOfIntygItem(drafts);
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
    public IntygServiceResult sendIntyg(String intygsId, String typ, String recipient, boolean hasPatientConsent) {

        Utlatande intyg = getUtlatandeForIntyg(intygsId, typ);
        verifyEnhetsAuth(intyg, true);

        SendIntygConfiguration sendConfig = new SendIntygConfiguration(recipient, hasPatientConsent, webCertUserService.getUser());

        // Check if Utlatande is a completion, in that case, close pending QA / Arende as handled.
        handleCompletion(intyg);

        monitoringService.logIntygSent(intygsId, recipient);

        // send PDL log event
        LogRequest logRequest = LogRequestFactory.createLogRequestFromUtlatande(intyg);
        logRequest.setAdditionalInfo(sendConfig.getPatientConsentMessage());
        logService.logSendIntygToRecipient(logRequest);

        markUtkastWithSendDateAndRecipient(intygsId, recipient);

        return sendIntygToCertificateSender(sendConfig, intyg);
    }

    private void handleCompletion(Utlatande intyg) {
        Relation relation = intyg.getGrundData().getRelation();
        if (relation == null) {
            return;
        }
        if (relation.getRelationKod() == null || !relation.getRelationKod().equals(RelationKod.KOMPLT)) {
            return;
        }

        // Of course we have to handle FK7263 as a special case.
        if (intyg.getTyp().equals(CertificateTypes.FK7263.toString())) {
            LOG.info("Set fragaSvar komplettering as handled for {}", intyg.getId());
            fragaSvarService.closeQuestionAsHandled(Long.parseLong(relation.getMeddelandeId()));
        } else if (FK_NEXT_GENERATION.contains(intyg.getTyp().toLowerCase())) {
            LOG.info("Set Arende komplettering as handled for {}", intyg.getId());
            arendeService.closeArendeAsHandled(relation.getMeddelandeId());
        }
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

        RevokeType revokeType = serviceConverter.buildRevokeTypeFromUtlatande(intyg.getUtlatande(), revokeMessage);
        RevokeMedicalCertificateRequestType request = new RevokeMedicalCertificateRequestType();
        request.setRevoke(revokeType);

        try {
            String xmlBody = revokeRequestConverter.toXml(request);
            certificateSenderService.revokeCertificate(intygsId, xmlBody);
            whenSuccessfulRevoke(intyg.getUtlatande());
            return IntygServiceResult.OK;
        } catch (JAXBException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, e.getMessage());
        } catch (CertificateSenderException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM, e.getMessage());
        }
    }

    private void verifyIsSigned(List<Status> statuses) {

        boolean isSigned = false;
        for (Status status : statuses) {
            if (status.getType() == CertificateState.RECEIVED && status.getTimestamp() != null) {
                isSigned = true;
                break;
            }
        }

        if (!isSigned) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                    "Certificate is not signed, cannot revoke an unsigned certificate");
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
        HoSPersonal skickatAv = serviceConverter.buildHosPersonalFromWebCertUser(webCertUserService.getUser());

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
                    relations ? Optional.of(relationService.getRelations(intygId)) : Optional.empty());
        } catch (IntygModuleFacadeException me) {
            // It's possible the Intygstjanst hasn't received the Intyg yet, look for it locally before rethrowing
            // exception
            Utkast utkast = utkastRepository.findOne(intygId);
            if (utkast == null) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
            }
            return buildIntygContentHolder(utkast);
        } catch (WebServiceException wse) {
            // Something went wrong communication-wise, try to find a matching Utkast instead.
            Utkast utkast = utkastRepository.findOne(intygId);
            if (utkast == null) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
                        "Cannot get intyg. Intygstjansten was not reachable and the Utkast could "
                                + "not be found, perhaps it was issued by a non-webcert system?");
            }
            return buildIntygContentHolder(utkast);
        }
    }

    /**
     * As the name of the method implies, this method builds a IntygContentHolder instance
     * from the Utkast stored in Webcert. If not present, it will try to fetch from Intygstjansten
     * instead.
     */
    private IntygContentHolder getIntygDataPreferWebcert(String intygId, String intygTyp) {
        Utkast utkast = utkastRepository.findOne(intygId);
        return (utkast != null) ? buildIntygContentHolder(utkast) : getIntygData(intygId, intygTyp, false);
    }

    private IntygContentHolder buildIntygContentHolder(Utkast utkast) {
        Utlatande utlatande = serviceConverter.buildUtlatandeFromUtkastModel(utkast);
        List<Status> statuses = serviceConverter.buildStatusesFromUtkast(utkast);
        return new IntygContentHolder(utkast.getModel(), utlatande, statuses, utkast.getAterkalladDatum() != null, Optional.empty());
    }

    private Utlatande getUtlatandeForIntyg(String intygId, String typ) {
        Utkast utkast = utkastRepository.findOne(intygId);
        return (utkast != null) ? serviceConverter.buildUtlatandeFromUtkastModel(utkast) : getIntygData(intygId, typ, false).getUtlatande();
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
        FragaSvar[] closedFragaSvarArr = fragaSvarService.closeAllNonClosedQuestions(intygsId);

        for (FragaSvar closedFragaSvar : closedFragaSvarArr) {
            String frageStallare = closedFragaSvar.getFrageStallare();
            if (FrageStallare.FORSAKRINGSKASSAN.isKodEqual(frageStallare)) {
                notificationService.sendNotificationForQuestionHandled(closedFragaSvar);
            } else if (FrageStallare.WEBCERT.isKodEqual(frageStallare)) {
                notificationService.sendNotificationForAnswerHandled(closedFragaSvar);
            }

            LOG.debug("Notification sent: question with id '{}' (related with certificate with id '{}') was closed",
                    closedFragaSvar.getInternReferens(),
                    intygsId);
        }

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
