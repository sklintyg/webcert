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
package se.inera.intyg.webcert.web.service.intyg;

import static java.util.Objects.isNull;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v3.ListCertificatesForCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v3.ListCertificatesForCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v3.ListCertificatesForCareType;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;
import javax.annotation.PostConstruct;
import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificatetype.v1.GetCertificateTypeResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificatetype.v1.GetCertificateTypeResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificatetype.v1.GetCertificateTypeType;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.UtkastStatus;
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
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.converter.IntygDraftsConverter;
import se.inera.intyg.webcert.web.converter.util.IntygConverterUtil;
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
import se.inera.intyg.webcert.web.service.referens.ReferensService;
import se.inera.intyg.webcert.web.service.relation.CertificateRelationService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;

/**
 * @author andreaskaltenbach
 */
@Service
@Transactional
public class IntygServiceImpl implements IntygService {

    private static final Logger LOG = LoggerFactory.getLogger(IntygServiceImpl.class);

    @Value("${intygstjanst.logicaladdress}")
    private String logicalAddress;

    @Value("${sekretessmarkering.prod.date}")
    private String sekretessmarkeringProdDate;

    @Autowired
    private GetCertificateTypeResponderInterface getCertificateTypeService;

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

    @Autowired
    private ReferensService referensService;

    private ChronoLocalDateTime sekretessmarkeringStartDatum;

    private AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();

    private static void copyOldAddressToNewPatientData(Patient oldPatientData, Patient newPatientData) {
        if (oldPatientData == null) {
            newPatientData.setPostadress(null);
            newPatientData.setPostnummer(null);
            newPatientData.setPostort(null);
        } else {
            newPatientData.setPostadress(oldPatientData.getPostadress());
            newPatientData.setPostnummer(oldPatientData.getPostnummer());
            newPatientData.setPostort(oldPatientData.getPostort());

        }
    }

    @PostConstruct
    public void init() {
        sekretessmarkeringStartDatum = LocalDateTime.parse(sekretessmarkeringProdDate, DateTimeFormatter.ISO_DATE_TIME);
    }

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
     */
    private IntygContentHolder fetchIntygData(String intygsId, String intygsTyp, boolean relations, boolean coherentJournaling) {
        IntygContentHolder intygsData = getIntygData(intygsId, intygsTyp, relations);
        LogRequest logRequest = LogRequestFactory.createLogRequestFromUtlatande(intygsData.getUtlatande(), coherentJournaling);

        if (!coherentJournaling) {
            verifyEnhetsAuth(intygsData.getUtlatande(), true);
        }
        Personnummer pnr = intygsData.getUtlatande().getGrundData().getPatient().getPersonId();
        String enhetsId = intygsData.getUtlatande().getGrundData().getSkapadAv().getVardenhet()
                .getEnhetsid();

        verifySekretessmarkering(intygsTyp, webCertUserService.getUser(), enhetsId, pnr);

        // Log read to PDL
        logService.logReadIntyg(logRequest);

        // Log read to monitoring log
        monitoringService.logIntygRead(intygsId, intygsTyp);

        return intygsData;
    }

    private void verifySekretessmarkering(String intygsTyp, WebCertUser user, String enhetsId, Personnummer pnr) {

        SekretessStatus sekretessStatus = patientDetailsResolver.getSekretessStatus(pnr);

        if (sekretessStatus == SekretessStatus.UNDEFINED) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.PU_PROBLEM,
                    "PU-service unavailable, cannot check sekretessmarkering.");
        }

        if (sekretessStatus == SekretessStatus.TRUE) {
            authoritiesValidator.given(user, intygsTyp)
                    .privilege(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT)
                    .orThrow();

            // INTYG-4231: Verifiera enhet / mottagning. Får ej visa utanför vald enhet (och dess underenheter)
            if (!webCertUserService.userIsLoggedInOnEnhetOrUnderenhet(enhetsId)) {
                LOG.debug("User not logged in on same unit as intyg unit for sekretessmarkerad patient.");
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM_SEKRETESSMARKERING_ENHET,
                        "User not logged in on same unit as intyg unit for sekretessmarkerad patient.");
            }
        }
    }

    @Override
    public Pair<List<ListIntygEntry>, Boolean> listIntyg(List<String> enhetId, Personnummer personnummer) {
        ListCertificatesForCareType request = new ListCertificatesForCareType();
        request.setPersonId(InternalConverterUtil.getPersonId(personnummer));

        SekretessStatus sekretessmarkering = patientDetailsResolver.getSekretessStatus(personnummer);

        for (String id : enhetId) {
            request.getEnhetsId().add(InternalConverterUtil.getHsaId(id));
        }

        // This is a list of Intyg from webcerts Utkast db
        final List<ListIntygEntry> webcertCerts = getIntygFromWebcert(enhetId, personnummer);

        try {
            ListCertificatesForCareResponseType response = listCertificateService.listCertificatesForCare(logicalAddress, request);
            List<ListIntygEntry> fullIntygItemList = intygConverter
                    .convertIntygToListIntygEntries(response.getIntygsLista().getIntyg(), webcertCerts);
            intygRelationHelper.decorateIntygListWithRelations(fullIntygItemList);
            fullIntygItemList = filterByIntygTypeForUser(fullIntygItemList, sekretessmarkering);
            addDraftsToListForIntygNotSavedInIntygstjansten(fullIntygItemList, webcertCerts);

            return Pair.of(fullIntygItemList, Boolean.FALSE);

        } catch (WebServiceException wse) {
            LOG.warn("Error when connecting to intygstjänsten: {}", wse.getMessage());
        }

        // If intygstjansten was unavailable, we return whatever certificates we can find and clearly inform
        // the caller that the set of certificates are only those that have been issued by WebCert.
        for (ListIntygEntry lie : webcertCerts) {
            lie.setRelations(certificateRelationService.getRelations(lie.getIntygId()));
        }
        return Pair.of(webcertCerts, Boolean.TRUE);
    }

    private List<ListIntygEntry> filterByIntygTypeForUser(List<ListIntygEntry> fullIntygItemList,
            SekretessStatus sekretessmarkering) {
        // Get intygstyper from the view privilege
        Set<String> base = authoritiesHelper.getIntygstyperForPrivilege(webCertUserService.getUser(),
                AuthoritiesConstants.PRIVILEGE_VISA_INTYG);

        // Remove intygstyper that cannot be issued for a sekretessmarkerad patient
        Set<String> intygsTyper = (sekretessmarkering == SekretessStatus.TRUE || sekretessmarkering == SekretessStatus.UNDEFINED)
                ? filterAllowedForSekretessMarkering(base)
                : base;

        // The user is only granted access to view intyg of intygstyper that are in the set.
        return fullIntygItemList.stream().filter(i -> intygsTyper.contains(i.getIntygType())).collect(Collectors.toList());
    }

    /**
     * Adds any IntygItems found in Webcert for this patient not present in the list from intygstjansten.
     */
    private void addDraftsToListForIntygNotSavedInIntygstjansten(List<ListIntygEntry> fullIntygItemList,
            List<ListIntygEntry> webcertIntygItems) {
        webcertIntygItems.removeAll(fullIntygItemList);
        fullIntygItemList.addAll(webcertIntygItems);
    }

    private List<ListIntygEntry> buildIntygItemListFromDrafts(List<Utkast> drafts) {
        return IntygDraftsConverter.convertUtkastsToListIntygEntries(drafts, null);
    }

    private List<ListIntygEntry> getIntygFromWebcert(List<String> enhetId, Personnummer personnummer) {
        List<UtkastStatus> statuses = new ArrayList<>();
        statuses.add(UtkastStatus.SIGNED);

        SekretessStatus sekretessmarkering = patientDetailsResolver.getSekretessStatus(personnummer);

        Set<String> base = authoritiesHelper.getIntygstyperForPrivilege(webCertUserService.getUser(),
                AuthoritiesConstants.PRIVILEGE_VISA_INTYG);

        Set<String> intygsTyper = (sekretessmarkering == SekretessStatus.TRUE || sekretessmarkering == SekretessStatus.UNDEFINED)
                ? filterAllowedForSekretessMarkering(base)
                : base;

        List<Utkast> drafts = utkastRepository.findDraftsByPatientAndEnhetAndStatus(DaoUtil.formatPnrForPersistence(personnummer), enhetId,
                statuses,
                intygsTyper);
        return buildIntygItemListFromDrafts(drafts);
    }

    private Set<String> filterAllowedForSekretessMarkering(Set<String> base) {
        Set<String> allowedForSekretess = authoritiesHelper.getIntygstyperAllowedForSekretessmarkering();
        return base.stream()
                .filter(allowedForSekretess::contains)
                .collect(Collectors.toSet());
    }

    @Override
    public IntygPdf fetchIntygAsPdf(String intygsId, String intygsTyp, boolean isEmployer) {
        try {
            LOG.debug("Fetching intyg '{}' as PDF", intygsId);

            Utkast utkast = utkastRepository.findOne(intygsId);
            IntygContentHolder intyg = (utkast != null) ? buildIntygContentHolderForUtkast(utkast, false)
                    : getIntygData(intygsId, intygsTyp, false);
            UtkastStatus utkastStatus = (utkast != null) ? utkast.getStatus() : UtkastStatus.SIGNED;

            boolean revoked = intyg.getStatuses() != null && intyg.getStatuses()
                    .stream()
                    .anyMatch(s -> s.getType().equals(CertificateState.CANCELLED));
            if (revoked) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Can't print revoked certificate.");
            }

            verifyPuServiceAvailable(intyg);

            boolean coherentJournaling = userIsDjupintegreradWithSjf();
            if (!coherentJournaling) {
                verifyEnhetsAuth(intyg.getUtlatande(), true);
            }

            IntygPdf intygPdf = modelFacade.convertFromInternalToPdfDocument(intygsTyp, intyg.getContents(), intyg.getStatuses(),
                    utkastStatus, isEmployer);

            // Log print as PDF to PDL log
            logPdfPrinting(intyg, coherentJournaling);

            return intygPdf;

        } catch (IntygModuleFacadeException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
        }
    }

    private void verifyPuServiceAvailable(IntygContentHolder intyg) {
        // INTYG-4086: All PDF-printing must pass through here. GE-002 explicitly states that if the PU-service is
        // unavailable, we must not let anyone print!
        PersonSvar personFromPUService = patientDetailsResolver
                .getPersonFromPUService(intyg.getUtlatande().getGrundData().getPatient().getPersonId());
        if (personFromPUService == null || personFromPUService.getStatus() != PersonSvar.Status.FOUND) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.PU_PROBLEM,
                    "PU-service unreachable, PDF printing is not allowed.");
        }
    }

    /**
     * Returns true if user has Origin DJUPINTEGRATION and Integration parameter sjf=true.
     */
    private boolean userIsDjupintegreradWithSjf() {
        WebCertUser user = webCertUserService.getUser();
        return user.getOrigin().equals(UserOriginType.DJUPINTEGRATION.name())
                && user.getParameters() != null
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
    public IntygServiceResult sendIntyg(String intygsId, String typ, String recipient, boolean delay) {

        IntygContentHolder intygData = getIntygData(intygsId, typ, true);
        Utlatande intyg = intygData.getUtlatande();

        verifyEnhetsAuth(intyg, true);
        verifyIsNotRevoked(intygData, IntygOperation.SEND);
        verifyIsSigned(intygData, IntygOperation.SEND);
        verifyNotReplacedBySignedIntyg(intygsId, IntygOperation.SEND);

        // WC-US-SM-001 - vi får ej skicka FK-intyg för sekretessmarkerad patient som innehåller personuppgifter.
        verifyNoExposureOfSekretessmarkeradPatient(intyg);

        SendIntygConfiguration sendConfig = new SendIntygConfiguration(recipient, webCertUserService.getUser());

        monitoringService.logIntygSent(intygsId, recipient);

        // send PDL log event
        LogRequest logRequest = LogRequestFactory.createLogRequestFromUtlatande(intyg);
        logRequest.setAdditionalInfo(sendConfig.getPatientConsentMessage());
        logService.logSendIntygToRecipient(logRequest);

        markUtkastWithSendDateAndRecipient(intygsId, recipient);

        return sendIntygToCertificateSender(sendConfig, intyg, delay);
    }

    // Kontrollera om det signerade intyget i intygstjänsten har namn- eller adressuppgifter. Om så är fallet,
    // så får vi EJ skicka intyget för sekretessmarkerad patient.
    private void verifyNoExposureOfSekretessmarkeradPatient(Utlatande intyg) {

        SekretessStatus sekretessStatus = patientDetailsResolver.getSekretessStatus(intyg.getGrundData().getPatient().getPersonId());

        // Om PU-tjänsten är otillgänglig får vi inte skicka. (Detta kollas redan client-side)
        if (sekretessStatus == SekretessStatus.UNDEFINED) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM,
                    "Cannot send intyg to recipient, PU-service is not accessible so sekretessmarkering cannot be checked.");
        }

        // Specialfall för fk7263 utfärdade innan patientuppgifter rensades från intyg.
        if (Fk7263EntryPoint.MODULE_ID.equalsIgnoreCase(intyg.getTyp())) {
            if (sekretessStatus != SekretessStatus.FALSE) {
                if (intyg.getGrundData().getSigneringsdatum().isBefore(sekretessmarkeringStartDatum)) {
                    throw new WebCertServiceException(WebCertServiceErrorCodeEnum.CERTIFICATE_TYPE_SEKRETESSMARKERING_HAS_PUDATA,
                            "Cannot send certificate for sekretessmarkerad patient having existing name or address.");
                }

            }
        }
    }

    private void verifyNotReplacedBySignedIntyg(final String intygsId, final IntygOperation operation) {
        final Optional<WebcertCertificateRelation> replacedByRelation = certificateRelationService.getNewestRelationOfType(intygsId,
                RelationKod.ERSATT, Arrays.asList(UtkastStatus.SIGNED));
        if (replacedByRelation.isPresent() && !replacedByRelation.get().isMakulerat()) {
            String errorString = String.format("Cannot %s certificate '%s', the certificate is replaced by certificate '%s'",
                    operation.getValue(), intygsId, replacedByRelation.get().getIntygsId());
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
        verifyIsSigned(intyg, IntygOperation.REVOKE);

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

    /**
     * Check if signed certificate is a completion, in that case, send to recipient and close pending completion QA /
     * Arende as handled.
     * <p>
     * Check if signed certificate should be sent directly to default recipient for this intygstyp.
     * <p>
     * Note that the send operation uses the "delay" boolean to allow the signing operation some time to complete
     * in intygstjansten.
     */
    @Override
    public void handleAfterSigned(Utkast utkast) {
        boolean isKomplettering = RelationKod.KOMPLT == utkast.getRelationKod();
        boolean isRenewal = RelationKod.FRLANG == utkast.getRelationKod();
        boolean isSigneraSkickaDirekt = authoritiesHelper
                .isFeatureActive(AuthoritiesConstants.FEATURE_SIGNERA_SKICKA_DIREKT, utkast.getIntygsTyp());

        if (isKomplettering || isSigneraSkickaDirekt) {
            try {
                LOG.info("Send intyg '{}' directly to recipient", utkast.getIntygsId());
                sendIntyg(utkast.getIntygsId(), utkast.getIntygsTyp(), moduleRegistry.getModuleEntryPoint(
                        utkast.getIntygsTyp()).getDefaultRecipient(), true);

                if (isKomplettering) {
                    LOG.info("Set komplettering QAs as handled for {}", utkast.getRelationIntygsId());
                    arendeService.closeCompletionsAsHandled(utkast.getRelationIntygsId(), utkast.getIntygsTyp());
                }

            } catch (ModuleNotFoundException e) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM,
                        "Could not send intyg directly to recipient", e);
            }
        }

        if (isRenewal) {
            LOG.info("Close all QA / arende on original certificate");
            arendeService.closeAllNonClosed(utkast.getRelationIntygsId());
        }
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

            String ref = referensService.getReferensForIntygsId(utkast.getIntygsId());

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

    @Override
    public String getIntygsTyp(String intygsId) {

        GetCertificateTypeType requestType = new GetCertificateTypeType();
        requestType.setIntygsId(intygsId);

        try {
            GetCertificateTypeResponseType responseType = getCertificateTypeService.getCertificateType(logicalAddress, requestType);
            return responseType.getTyp().getCode();

        } catch (WebCertServiceException e) {
            LOG.error("Failed to decide on the type of certificate");
            if (e.getErrorCode() == WebCertServiceErrorCodeEnum.DATA_NOT_FOUND) {
                return null;
            }
            throw e;
        }
    }

    public void setLogicalAddress(String logicalAddress) {
        this.logicalAddress = logicalAddress;
    }

    @VisibleForTesting
    public void setSekretessmarkeringStartDatum(ChronoLocalDateTime sekretessmarkeringStartDatum) {
        this.sekretessmarkeringStartDatum = sekretessmarkeringStartDatum;
    }

    protected IntygServiceResult sendIntygToCertificateSender(SendIntygConfiguration sendConfig, Utlatande intyg, boolean delay) {

        String intygsId = intyg.getId();
        String recipient = sendConfig.getRecipient();
        String intygsTyp = intyg.getTyp();
        HoSPersonal skickatAv = IntygConverterUtil.buildHosPersonalFromWebCertUser(webCertUserService.getUser(), null);

        try {
            LOG.debug("Sending intyg {} of type {} to recipient {}", intygsId, intygsTyp, recipient);

            // Ask the certificateSenderService to post a 'send' message onto the queue.
            certificateSenderService.sendCertificate(intygsId, intyg.getGrundData().getPatient().getPersonId(),
                    objectMapper.writeValueAsString(skickatAv), recipient, delay);

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
        if (!webCertUserService.isAuthorizedForUnit(vardenhet.getVardgivare().getVardgivarid(), vardenhet.getEnhetsid(),
                isReadOnlyOperation)) {
            String msg = "User not authorized for enhet " + vardenhet.getEnhetsid();
            LOG.debug(msg);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM, msg);
        }
    }

    private void verifyIsSigned(final IntygContentHolder intyg, final IntygOperation operation) {

        final List<Status> statuses = (isNull(intyg) || isEmpty(intyg.getStatuses()))
                ? Lists.newArrayList()
                : intyg.getStatuses();

        if (statuses.stream().noneMatch(status -> CertificateState.RECEIVED.equals(status.getType()) && status.getTimestamp() != null)) {

            final String message = MessageFormat.format("Certificate {0} is not signed, cannot {1} an unsigned certificate",
                    intyg.getUtlatande().getId(), operation.getValue());

            LOG.debug(message);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, message);
        }
    }

    private void verifyIsNotRevoked(final IntygContentHolder intyg, final IntygOperation operation) {
        if (intyg.isRevoked()) {

            final String message = MessageFormat.format("certificate with id '{0}' is revoked, cannot {1} a revoked certificate",
                    intyg.getUtlatande().getId(), operation.getValue());

            LOG.debug(message);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, message);
        }
    }

    /**
     * Builds a IntygContentHolder by first trying to get the Intyg from intygstjansten. If
     * not found or the Intygstjanst couldn't be reached, the local Utkast - if available -
     * will be used instead.
     * <p>
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

            final Personnummer personId = certificate.getUtlatande().getGrundData().getPatient().getPersonId();

            // INTYG-4086: Patient object populated according to ruleset for the intygstyp at hand.
            // Since an FK-intyg never will have anything other than personId, try to fetch all using ruleset
            Patient newPatientData = patientDetailsResolver.resolvePatient(personId, typ);

            if (newPatientData == null) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.PU_PROBLEM,
                        "Patient data could not be fetched from the PU service or resolved from integration parameters.");
            }

            Utlatande utlatande = null;
            boolean patientNameChanged = false;
            boolean patientAddressChanged = false;
            try {
                utlatande = moduleRegistry.getModuleApi(typ).getUtlatandeFromJson(internalIntygJsonModel);
                patientNameChanged = patientDetailsResolver.isPatientNamedChanged(utlatande.getGrundData().getPatient(),
                        newPatientData);
                patientAddressChanged = patientDetailsResolver.isPatientAddressChanged(utlatande.getGrundData().getPatient(),
                        newPatientData);
            } catch (IOException e) {
                LOG.error("Failed to getUtlatandeFromJson intygsId {} while checking for updated patient information", intygId);
            }

            // Patient is not expected to be null, since that means PU-service is probably down and no integration
            // parameters were available.

            // Get the module api and use the "updateBeforeSave" to update the outbound "model" with the
            // Patient object.
            ModuleApi moduleApi = moduleRegistry.getModuleApi(typ);
            // INTYG-5354, INTYG-5380: Don't use incomplete address from external data sources (PU/js).
            if (!newPatientData.isCompleteAddressProvided()) {
                // Use the old address data.
                Patient oldPatientData = utlatande.getGrundData().getPatient();
                copyOldAddressToNewPatientData(oldPatientData, newPatientData);
            }
            internalIntygJsonModel = moduleApi.updateBeforeSave(internalIntygJsonModel, newPatientData);

            utkastIntygDecorator.decorateWithUtkastStatus(certificate);
            Relations certificateRelations = intygRelationHelper.getRelationsForIntyg(intygId);

            final SekretessStatus sekretessStatus = patientDetailsResolver.getSekretessStatus(personId);
            if (SekretessStatus.UNDEFINED.equals(sekretessStatus)) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.PU_PROBLEM,
                        "Sekretesstatus could not be fetched from the PU service");
            }
            final boolean sekretessmarkering = SekretessStatus.TRUE.equals(sekretessStatus);

            Utkast utkast = utkastRepository.findByIntygsIdAndIntygsTyp(intygId, typ);
            final LocalDateTime created = utkast != null ? utkast.getSkapad() : null;

            return IntygContentHolder.builder()
                    .setContents(internalIntygJsonModel)
                    .setUtlatande(certificate.getUtlatande())
                    .setStatuses(certificate.getMetaData().getStatus())
                    .setRevoked(certificate.isRevoked())
                    .setRelations(certificateRelations)
                    .setCreated(created)
                    .setDeceased(isDeceased(personId))
                    .setSekretessmarkering(sekretessmarkering)
                    .setPatientNameChangedInPU(patientNameChanged)
                    .setPatientAddressChangedInPU(patientAddressChanged)
                    .build();

        } catch (IntygModuleFacadeException me) {
            // It's possible the Intygstjanst hasn't received the Intyg yet, look for it locally before rethrowing
            // exception
            Utkast utkast = utkastRepository.findByIntygsIdAndIntygsTyp(intygId, typ);
            if (utkast == null) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
            }
            return buildIntygContentHolderForUtkast(utkast, relations);
        } catch (WebServiceException wse) {
            // Something went wrong communication-wise, try to find a matching Utkast instead.
            Utkast utkast = utkastRepository.findByIntygsIdAndIntygsTyp(intygId, typ);
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
        return utkast != null ? buildIntygContentHolderForUtkast(utkast, false)
                : getIntygData(intygId, intygTyp, false);
    }

    // NOTE! INTYG-4086. This method is used when fetching Intyg/Utkast from WC locally. The question is, should we
    // replace the patient on the existing model with a freshly fetched one here or not? In case we're storing patient
    // info entered manually for non FK-types here, we may end up overwriting a manually stored name etc...
    private IntygContentHolder buildIntygContentHolderForUtkast(Utkast utkast, boolean relations) {

        try {
            // INTYG-4086: Patient object populated according to ruleset for the intygstyp at hand.
            Patient newPatientData = patientDetailsResolver.resolvePatient(utkast.getPatientPersonnummer(), utkast.getIntygsTyp());

            // Copied from getDraft in UtkastModuleApiController
            // INTYG-4086: Temporary, don't know if this is correct yet. If no patient was resolved,
            // create an "empty" Patient with personnummer only.
            if (newPatientData == null) {
                newPatientData = new Patient();
                newPatientData.setPersonId(utkast.getPatientPersonnummer());
            }

            // INTYG-5354, INTYG-5380: Don't use incomplete address from external data sources (PU/js).
            Utlatande utlatande = modelFacade.getUtlatandeFromInternalModel(utkast.getIntygsTyp(), utkast.getModel());
            if (!newPatientData.isCompleteAddressProvided()) {
                // Use the old address data.
                Patient oldPatientData = utlatande.getGrundData().getPatient();
                copyOldAddressToNewPatientData(oldPatientData, newPatientData);
            }
            String updatedModel = moduleRegistry.getModuleApi(utkast.getIntygsTyp()).updateBeforeSave(utkast.getModel(),
                    newPatientData);
            utlatande = modelFacade.getUtlatandeFromInternalModel(utkast.getIntygsTyp(), updatedModel);

            List<Status> statuses = IntygConverterUtil.buildStatusesFromUtkast(utkast);
            Relations certificateRelations = certificateRelationService.getRelations(utkast.getIntygsId());

            final SekretessStatus sekretessStatus = patientDetailsResolver.getSekretessStatus(newPatientData.getPersonId());
            if (SekretessStatus.UNDEFINED.equals(sekretessStatus)) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.PU_PROBLEM,
                        "Sekretesstatus could not be fetched from the PU service");
            }
            final boolean sekretessmarkerad = SekretessStatus.TRUE.equals(sekretessStatus);

            boolean patientNameChanged = patientDetailsResolver.isPatientNamedChanged(utlatande.getGrundData().getPatient(),
                    newPatientData);
            boolean patientAddressChanged = patientDetailsResolver.isPatientAddressChanged(utlatande.getGrundData().getPatient(),
                    newPatientData);

            return IntygContentHolder.builder()
                    .setContents(updatedModel)
                    .setUtlatande(utlatande)
                    .setStatuses(statuses)
                    .setRevoked(utkast.getAterkalladDatum() != null)
                    .setRelations(certificateRelations)
                    .setCreated(utkast.getSkapad())
                    .setDeceased(isDeceased(utkast.getPatientPersonnummer()))
                    .setSekretessmarkering(sekretessmarkerad)
                    .setPatientNameChangedInPU(patientNameChanged)
                    .setPatientAddressChangedInPU(patientAddressChanged)
                    .build();

        } catch (ModuleException | ModuleNotFoundException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
        }

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

    private boolean isDeceased(Personnummer personnummer) {
        WebCertUser user = webCertUserService.getUser();
        boolean deceasedAccordingToPu = patientDetailsResolver.isAvliden(personnummer);
        if (UserOriginType.DJUPINTEGRATION.name().equals(user.getOrigin())) {
            // INTYG-4469
            return deceasedAccordingToPu || (user.getParameters() != null && user.getParameters().isPatientDeceased());
        } else {
            return deceasedAccordingToPu;
        }
    }

    private String getUserReference() {
        WebCertUser user = webCertUserService.getUser();
        return user.getParameters() != null ? user.getParameters().getReference() : null;
    }

    public enum IntygOperation {
        SEND("send"),
        REVOKE("revoke");

        private final String value;

        IntygOperation(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
