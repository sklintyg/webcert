/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

import static se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum.DATA_NOT_FOUND;
import static se.inera.intyg.webcert.web.service.intyg.util.IntygVerificationHelper.verifyIsNotRevoked;
import static se.inera.intyg.webcert.web.service.intyg.util.IntygVerificationHelper.verifyIsNotSent;
import static se.inera.intyg.webcert.web.service.intyg.util.IntygVerificationHelper.verifyIsSigned;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import jakarta.annotation.PostConstruct;
import jakarta.xml.ws.WebServiceException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificatetypeinfo.v1.GetCertificateTypeInfoResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificatetypeinfo.v1.GetCertificateTypeInfoResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificatetypeinfo.v1.GetCertificateTypeInfoType;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.common.enumerations.EventCode;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.converter.InternalConverterUtil;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateResponse;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.modules.support.api.notification.ArendeCount;
import se.inera.intyg.common.support.peristence.dao.util.DaoUtil;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
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
import se.inera.intyg.webcert.web.event.CertificateEventService;
import se.inera.intyg.webcert.web.service.access.CertificateAccessServiceHelper;
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
import se.inera.intyg.webcert.web.service.intyg.dto.IntygWithNotificationsResponse;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.log.factory.LogRequestFactory;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.FragorOchSvarCreator;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.referens.ReferensService;
import se.inera.intyg.webcert.web.service.relation.CertificateRelationService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygTypeInfo;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v3.ListCertificatesForCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v3.ListCertificatesForCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v3.ListCertificatesForCareType;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

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
    private GetCertificateTypeInfoResponderInterface getCertificateTypeInfoService;

    @Autowired
    private ListCertificatesForCareResponderInterface listCertificateService;

    @Autowired
    private WebCertUserService webCertUserService;

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private CertificateEventService certificateEventService;

    @Autowired
    private IntygModuleFacade moduleFacade;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private LogService logService;

    @Autowired
    private LogRequestFactory logRequestFactory;

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

    @Autowired
    private CertificateAccessServiceHelper certificateAccessServiceHelper;

    @Autowired
    private IntygTextsService intygTextsService;

    private ChronoLocalDateTime sekretessmarkeringStartDatum;

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
    public IntygContentHolder fetchIntygData(String intygsId, String intygsTyp) {
        return fetchIntygData(intygsId, intygsTyp, false, true, true);
    }

    @Override
    public IntygContentHolder fetchIntygData(String intygsId, String intygsTyp, boolean pdlLogging) {
        return fetchIntygData(intygsId, intygsTyp, false, pdlLogging, true);
    }

    @Override
    public IntygContentHolder fetchIntygData(String intygsId, String intygsTyp, boolean pdlLogging, boolean validateAccess) {
        return fetchIntygData(intygsId, intygsTyp, false, pdlLogging, validateAccess);
    }

    @Override
    public IntygContentHolder fetchIntygDataWithRelations(String intygId, String intygsTyp) {
        return fetchIntygData(intygId, intygsTyp, true, true, true);
    }

    @Override
    public IntygContentHolder fetchIntygDataForInternalUse(String certificateId, boolean includeRelations) {
        return fetchIntygData(certificateId, null, includeRelations, false, false);
    }

    @Override
    public IntygContentHolder fetchIntygDataforCandidate(String intygsId, String intygsTyp, boolean pdlLogging) {
        return fetchIntygData(intygsId, intygsTyp, false, pdlLogging, false);
    }


    /**
     * Returns the IntygContentHolder. Used both externally to frontend and internally in the modules.
     *
     * @param intygsId the identifier of the intyg.
     * @param intygsTyp the typ of the intyg. Used to call the correct module.
     * @param relations If the relations between intyg should be populated. This can be expensive (several database
     * operations). Use sparsely.
     * @param pdlLogging If the call should be logged.
     * @param validateAccess If the call should validate access for logged in user.
     * @return IntygContentHolder.
     */
    private IntygContentHolder fetchIntygData(String intygsId, String intygsTyp, boolean relations, boolean pdlLogging,
        boolean validateAccess) {
        IntygContentHolder intygsData = getIntygData(intygsId, intygsTyp, relations);

        if (validateAccess) {
            certificateAccessServiceHelper.validateAccessToRead(intygsData.getUtlatande());
        }

        if (pdlLogging) {
            LogRequest logRequest = logRequestFactory.createLogRequestFromUtlatande(intygsData.getUtlatande(), checkSjf(intygsData));

            // Log read to PDL
            logService.logReadIntyg(logRequest);

            // Log read to monitoring log
            monitoringService.logIntygRead(intygsId, intygsTyp);
        }

        return intygsData;
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

    @Override
    public List<ListIntygEntry> listIntygFromIT(List<String> enhetId, Personnummer personnummer) {
        final var request = new ListCertificatesForCareType();
        request.setPersonId(InternalConverterUtil.getPersonId(personnummer));
        for (String id : enhetId) {
            request.getEnhetsId().add(InternalConverterUtil.getHsaId(id));
        }

        final var sekretessmarkering = patientDetailsResolver.getSekretessStatus(personnummer);
        final var response = listCertificateService.listCertificatesForCare(logicalAddress, request);
        final var fullIntygItemList = response.getIntygsLista().getIntyg().stream()
            .map(intyg -> intygConverter.convertIntygToListIntygEntry(intyg, null))
            .collect(Collectors.toList());

        intygRelationHelper.decorateIntygListWithRelations(fullIntygItemList);
        return filterByIntygTypeForUser(fullIntygItemList, sekretessmarkering);
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
        fullIntygItemList.addAll(
            webcertIntygItems.stream()
                .filter(wcIntyg ->
                    fullIntygItemList.stream()
                        .noneMatch(itIntyg -> itIntyg.getIntygId().equalsIgnoreCase(wcIntyg.getIntygId())))
                .collect(Collectors.toList())
        );
    }

    private List<ListIntygEntry> buildIntygItemListFromDrafts(List<Utkast> drafts) {
        return IntygDraftsConverter.convertUtkastsToListIntygEntries(drafts);
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

    private boolean checkSjf(IntygContentHolder intyg) {
        WebCertUser user = webCertUserService.getUser();
        if (intyg.getUtlatande().getGrundData().getSkapadAv().getVardenhet().getVardgivare().getVardgivarid()
            .equals(user.getValdVardgivare().getId())) {
            return false;
        } else {
            return userIsDjupintegreradWithSjf();
        }
    }

    @Override
    public IntygPdf fetchIntygAsPdf(String intygsId, String intygsTyp, boolean isEmployerCopy) {
        try {
            LOG.debug("Fetching intyg '{}' as PDF", intygsId);

            Utkast utkast = utkastRepository.findById(intygsId).orElse(null);

            IntygContentHolder intyg;
            if (utkast == null || UtkastStatus.SIGNED.equals(utkast.getStatus())) {
                // hämta från IT om ej finns i WC eller är signerat (INTYG-7580)
                intyg = getIntygData(intygsId, intygsTyp, false);
            } else {
                intyg = buildIntygContentHolderFromUtkast(utkast);
            }

            UtkastStatus utkastStatus = (utkast != null) ? utkast.getStatus() : UtkastStatus.SIGNED;

            boolean revoked = intyg.getStatuses() != null && intyg.getStatuses()
                .stream()
                .anyMatch(s -> s.getType().equals(CertificateState.CANCELLED));
            if (revoked) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Can't print revoked certificate.");
            }

            certificateAccessServiceHelper.validateAccessToPrint(intyg.getUtlatande(), isEmployerCopy);

            IntygPdf intygPdf = moduleFacade.convertFromInternalToPdfDocument(intygsTyp, intyg.getContents(), intyg.getStatuses(),
                utkastStatus, isEmployerCopy);

            // Log print as PDF to PDL log
            logPdfPrinting(intyg, isEmployerCopy);

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
        return user.getOrigin().equals(UserOriginType.DJUPINTEGRATION.name())
            && user.getParameters() != null
            && user.getParameters().isSjf();
    }

    /**
     * Creates log events for PDF printing actions. Creates both PDL and monitoring log events
     * depending the state of the intyg.
     */
    private void logPdfPrinting(IntygContentHolder intyg, boolean isEmployerCopy) {

        final String intygsId = intyg.getUtlatande().getId();
        final String intygsTyp = intyg.getUtlatande().getTyp();

        LogRequest logRequest = logRequestFactory.createLogRequestFromUtlatande(intyg.getUtlatande());
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
                monitoringService.logIntygPrintPdf(intygsId, intygsTyp, isEmployerCopy);
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
            LOG.error(String.format("Could not put certificate store message on queue: %s", cse.getMessage()), cse);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, cse);
        }
    }

    @Override
    public IntygServiceResult sendIntyg(String intygsId, String typ, String recipient, boolean delay) {

        final Optional<Utkast> optionalUtkast = Optional.ofNullable(utkastRepository.findById(intygsId).orElse(null));

        final Utlatande utlatande = optionalUtkast
            .map(utkast -> moduleFacade.getUtlatandeFromInternalModel(utkast.getIntygsTyp(), utkast.getModel()))
            .orElseGet(() -> getIntygData(intygsId, typ, false).getUtlatande());

        certificateAccessServiceHelper.validateAccessToSend(utlatande);

        if (optionalUtkast.isPresent()) {
            verifyIsNotRevoked(optionalUtkast.get(), IntygOperation.SEND);
            verifyIsSigned(optionalUtkast.get(), IntygOperation.SEND);
            verifyIsNotSent(optionalUtkast.get(), IntygOperation.SEND);
        } else {
            final CertificateResponse certificate;
            try {
                final IntygTypeInfo intygTypeInfo = getIntygTypeInfoFromIT(intygsId);
                certificate = moduleFacade.getCertificate(intygsId, typ, intygTypeInfo.getIntygTypeVersion());
            } catch (IntygModuleFacadeException e) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
            }
            verifyIsNotRevoked(certificate, IntygOperation.SEND);
            verifyIsSigned(certificate, IntygOperation.SEND);
            verifyIsNotSent(certificate, IntygOperation.SEND);

        }

        verifyNotReplacedBySignedIntyg(intygsId, IntygOperation.SEND);

        // WC-US-SM-001 - vi får ej skicka FK-intyg för sekretessmarkerad patient som innehåller personuppgifter.
        verifyNoExposureOfSekretessmarkeradPatient(utlatande);

        SendIntygConfiguration sendConfig = new SendIntygConfiguration(recipient, webCertUserService.getUser());

        monitoringService.logIntygSent(intygsId, utlatande.getTyp(), recipient);

        certificateEventService
            .createCertificateEvent(intygsId, webCertUserService.getUser().getHsaId(), EventCode.SKICKAT, "Recipient: " + recipient);

        // send PDL log event
        LogRequest logRequest = logRequestFactory.createLogRequestFromUtlatande(utlatande, sendConfig.getPatientConsentMessage());
        logService.logSendIntygToRecipient(logRequest);

        markUtkastWithSendDateAndRecipient(optionalUtkast.orElse(null), intygsId, recipient);

        return sendIntygToCertificateSender(sendConfig, utlatande, delay);
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
        if (Fk7263EntryPoint.MODULE_ID.equalsIgnoreCase(intyg.getTyp()) && sekretessStatus != SekretessStatus.FALSE) {
            if (intyg.getGrundData().getSigneringsdatum().isBefore(sekretessmarkeringStartDatum)) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.CERTIFICATE_TYPE_SEKRETESSMARKERING_HAS_PUDATA,
                    "Cannot send certificate for sekretessmarkerad patient having existing name or address.");
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

        certificateAccessServiceHelper.validateAccessToInvalidate(intyg.getUtlatande());

        verifyIsSigned(intyg, IntygOperation.REVOKE);

        if (intyg.isRevoked()) {
            LOG.debug("Certificate with id '{}' is already revoked", intygsId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Certificate is already revoked");
        }

        try {
            certificateSenderService.revokeCertificate(intygsId, moduleFacade.getRevokeCertificateRequest(intygsTyp, intyg.getUtlatande(),
                    IntygConverterUtil.buildHosPersonalFromWebCertUser(webCertUserService.getUser(), null), revokeMessage), intygsTyp,
                intyg.getUtlatande().getTextVersion());
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
        List<RelationKod> shouldCloseCompletionCodes = Lists.newArrayList(RelationKod.KOMPLT, RelationKod.ERSATT,
            RelationKod.FRLANG);
        boolean shouldCloseCompletions = shouldCloseCompletionCodes.stream().anyMatch(it -> it == utkast.getRelationKod());
        boolean isSigneraSkickaDirekt = authoritiesHelper
            .isFeatureActive(AuthoritiesConstants.FEATURE_SIGNERA_SKICKA_DIREKT, utkast.getIntygsTyp());
        if (isNotATestIntyg(utkast) && (isSigneraSkickaDirekt || utkast.getRelationKod() == RelationKod.KOMPLT)) {
            try {
                LOG.info("Send intyg '{}' directly to recipient", utkast.getIntygsId());
                sendIntyg(utkast.getIntygsId(), utkast.getIntygsTyp(), moduleRegistry.getModuleEntryPoint(
                    utkast.getIntygsTyp()).getDefaultRecipient(), true);
            } catch (ModuleNotFoundException e) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM,
                    "Could not send intyg directly to recipient", e);
            }
        }
        if (shouldCloseCompletions) {
            LOG.info("Set komplettering QAs as handled for {}", utkast.getRelationIntygsId());
            arendeService.closeCompletionsAsHandled(utkast.getRelationIntygsId(), utkast.getIntygsTyp());
        }
    }

    /**
     * Is considered a test intyg if the intyg is tagged with test intyg flag. Or if it was created without the flag, it will still
     * be considered a test intyg if the patient currently holds the testIndicator-flag.
     */
    private boolean isNotATestIntyg(Utkast utkast) {
        return !(utkast.isTestIntyg() || patientDetailsResolver.isTestIndicator(utkast.getPatientPersonnummer()));
    }

    @Override
    public String getIssuingVardenhetHsaId(String intygId, String intygsTyp) {
        try {
            IntygContentHolder intygData = getIntygData(intygId, intygsTyp, false);
            return intygData.getUtlatande().getGrundData().getSkapadAv().getVardenhet().getEnhetsid();
        } catch (WebCertServiceException e) {
            if (e.getErrorCode() == DATA_NOT_FOUND) {
                return null;
            }
            throw e;
        }
    }

    @Override
    public boolean isRevoked(String intygsId, String intygsTyp) {

        IntygContentHolder intygData = getIntygData(intygsId, intygsTyp, false);
        monitoringService.logIntygRevokeStatusRead(intygsId, intygsTyp);
        return intygData.isRevoked();
    }

    @Override
    public List<IntygWithNotificationsResponse> listCertificatesForCareWithQA(List<Handelse> notificationsForWC) {
        if (notificationsForWC.isEmpty()) {
            return Collections.emptyList();
        }

        final var intygWithNotificationsResponses = new ArrayList<IntygWithNotificationsResponse>();

        final var notificationCertificateIdHash = getNotificationCertificateIdHash(notificationsForWC);

        final var draftMap = getDraftMap(notificationCertificateIdHash.keySet());

        for (var certificateId : notificationCertificateIdHash.keySet()) {

            final var notifications = notificationCertificateIdHash.get(certificateId);

            IntygWithNotificationsResponse response = null;

            if (draftMap.containsKey(certificateId)) {
                final var draft = draftMap.get(certificateId);
                response = getIntygWithNotificationsResponse(draft, notifications);
            } else if (!missingDraftWasRemoved(notifications)) {
                final var certificate = getIntygData(certificateId, null, false);
                response = getIntygWithNotificationsResponse(certificate, notifications);
            }

            if (response != null) {
                intygWithNotificationsResponses.add(response);
            }
        }

        return intygWithNotificationsResponses;
    }

    /**
     * When no draft is found in WC but there are notifications in the database, it could be of two reasons. Either the certificate
     * wasn't issued in WC (and will be found in IT) or it was removed before it got signed. This method checks the latter based
     * on what notifications exists for the draft/certificate.
     *
     * @param notifications List of notifications for the draft/certificate.
     * @return True if it was a draft that has been removed before signed.
     */
    private boolean missingDraftWasRemoved(List<Handelse> notifications) {
        final var statusToExclude = Arrays.asList(HandelsekodEnum.SKAPAT, HandelsekodEnum.ANDRAT, HandelsekodEnum.RADERA,
            HandelsekodEnum.KFSIGN);
        return notifications.stream()
            .filter(notification -> !statusToExclude.contains(notification.getCode()))
            .collect(Collectors.toList())
            .isEmpty();
    }

    private HashMap<String, List<Handelse>> getNotificationCertificateIdHash(List<Handelse> allNotifications) {
        final var notificationCertificateIdHash = new HashMap<String, List<Handelse>>();
        for (var notification : allNotifications) {
            final var certificateId = notification.getIntygsId();
            if (!notificationCertificateIdHash.containsKey(certificateId)) {
                notificationCertificateIdHash.put(certificateId, new ArrayList<>());
            }

            notificationCertificateIdHash.get(certificateId).add(notification);
        }
        return notificationCertificateIdHash;
    }

    private HashMap<String, Utkast> getDraftMap(Set<String> certificateIds) {
        final var draftList = utkastRepository.findAllById(certificateIds);
        final var draftMap = new HashMap<String, Utkast>(draftList.size());
        for (var draft : draftList) {
            draftMap.put(draft.getIntygsId(), draft);
        }
        return draftMap;
    }

    private IntygWithNotificationsResponse getIntygWithNotificationsResponse(Utkast draft, List<Handelse> notifications) {
        try {
            final var ref = referensService.getReferensForIntygsId(draft.getIntygsId());
            final ModuleApi api = moduleRegistry.getModuleApi(draft.getIntygsTyp(), draft.getIntygTypeVersion());
            final Intyg intyg = api.getIntygFromUtlatande(api.getUtlatandeFromJson(draft.getModel()));
            final Pair<ArendeCount, ArendeCount> arenden = fragorOchSvarCreator.createArenden(draft.getIntygsId(), draft.getIntygsTyp());
            return new IntygWithNotificationsResponse(intyg, notifications, arenden.getLeft(), arenden.getRight(), ref);
        } catch (ModuleNotFoundException | ModuleException | IOException e) {
            LOG.error("Could not convert intyg {} to external format", draft.getIntygsId());
            return null;
        }
    }

    private IntygWithNotificationsResponse getIntygWithNotificationsResponse(IntygContentHolder certificate, List<Handelse> notifications) {
        try {
            final var certificateType = certificate.getUtlatande().getTyp();
            final var certificateTypeVersion = certificate.getUtlatande().getTextVersion();
            final Pair<ArendeCount, ArendeCount> arenden = fragorOchSvarCreator.createArenden(certificate.getUtlatande().getId(),
                certificateType);
            final ModuleApi api = moduleRegistry.getModuleApi(certificateType, certificateTypeVersion);
            final var intyg = api.getIntygFromUtlatande(certificate.getUtlatande());
            return new IntygWithNotificationsResponse(intyg, notifications, arenden.getLeft(), arenden.getRight(), "");
        } catch (ModuleNotFoundException | ModuleException e) {
            LOG.error("Could not convert intyg {} to external format", certificate.getUtlatande().getId());
            return null;
        }
    }

    @Override
    public IntygTypeInfo getIntygTypeInfo(String intygsId) {

        return getIntygTypeInfo(intygsId, utkastRepository.findById(intygsId).orElse(null));
    }

    @Override
    public IntygTypeInfo getIntygTypeInfo(String intygsId, Utkast utkast) {

        // 1. If WC has and utkast for this id, we don't need to query IT for type and version.
        if (utkast != null) {
            return new IntygTypeInfo(intygsId, utkast.getIntygsTyp(), utkast.getIntygTypeVersion());
        } else {
            return getIntygTypeInfoFromIT(intygsId);
        }
    }

    private IntygTypeInfo getIntygTypeInfoFromIT(String intygsId) {
        GetCertificateTypeInfoType requestType = new GetCertificateTypeInfoType();
        requestType.setIntygsId(intygsId);

        try {
            final GetCertificateTypeInfoResponseType certificateTypeInfo = getCertificateTypeInfoService.getCertificateTypeInfo(
                logicalAddress,
                requestType);
            return new IntygTypeInfo(intygsId, certificateTypeInfo.getTyp().getCode(), certificateTypeInfo.getTypVersion());
        } catch (WebServiceException e) {
            throw new WebCertServiceException(DATA_NOT_FOUND, String.format(
                "Failed retrieving certificate type information from Intygstjänsten. "
                    + "The certificate might not exist. Certificate id: %s",
                intygsId), e);
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
            LOG.error(String.format("Module problems occured when trying to send intyg %s", intygsId), e);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
        } catch (JsonProcessingException e) {
            LOG.error(String.format("Error writing skickatAv as string when trying to send intyg %s", intygsId), e);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
        } catch (CertificateSenderException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
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
     * <p>
     * The data will be updated with current patient data from PU unless otherwise stated in the module api.
     */
    private IntygContentHolder getIntygData(String intygId, String typ, boolean relations) {
        var intygType = typ;
        try {
            final var intygTypeInfo = getIntygTypeInfo(intygId);
            intygType = intygTypeInfo.getIntygType();
            final var intygTypeVersion = intygTypeInfo.getIntygTypeVersion();
            CertificateResponse certificate = moduleFacade.getCertificate(intygId, intygType, intygTypeVersion);
            String internalIntygJsonModel = certificate.getInternalModel();

            final Personnummer personId = certificate.getUtlatande().getGrundData().getPatient().getPersonId();

            // INTYG-4086: Patient object populated according to ruleset for the intygstyp at hand.
            // Since an FK-intyg never will have anything other than personId, try to fetch all using ruleset
            Patient newPatientData = patientDetailsResolver.resolvePatient(personId, intygType, intygTypeVersion);

            if (newPatientData == null) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.PU_PROBLEM,
                    "Patient data could not be fetched from the PU service or resolved from integration parameters.");
            }

            Utlatande utlatande = null;
            boolean patientNameChanged = false;
            boolean patientAddressChanged = false;
            try {
                utlatande = moduleRegistry.getModuleApi(intygType, intygTypeVersion).getUtlatandeFromJson(internalIntygJsonModel);
                patientNameChanged = patientDetailsResolver.isPatientNamedChanged(utlatande.getGrundData().getPatient(),
                    newPatientData);
                patientAddressChanged = patientDetailsResolver.isPatientAddressChanged(utlatande.getGrundData().getPatient(),
                    newPatientData);
            } catch (IOException e) {
                LOG.error("Failed to getUtlatandeFromJson intygsId {} while checking for updated patient information", intygId);
            }

            // Get the module api and use the "updateBeforeViewing" to update the outbound "model" with the
            // Patient object (not done for models with patient data saved in the model).

            ModuleApi moduleApi = moduleRegistry.getModuleApi(intygType, intygTypeVersion);
            // INTYG-5354, INTYG-5380: Don't use incomplete address from external data sources (PU/js).
            if (!newPatientData.isCompleteAddressProvided()) {
                // Use the old address data.
                Patient oldPatientData = utlatande.getGrundData().getPatient();
                copyOldAddressToNewPatientData(oldPatientData, newPatientData);
            }
            internalIntygJsonModel = moduleApi.updateBeforeViewing(internalIntygJsonModel, newPatientData);

            utkastIntygDecorator.decorateWithUtkastStatus(certificate);
            Relations certificateRelations = intygRelationHelper.getRelationsForIntyg(intygId);

            final SekretessStatus sekretessStatus = patientDetailsResolver.getSekretessStatus(personId);
            if (SekretessStatus.UNDEFINED.equals(sekretessStatus)) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.PU_PROBLEM,
                    "Sekretesstatus could not be fetched from the PU service");
            }
            final boolean sekretessmarkering = SekretessStatus.TRUE.equals(sekretessStatus);

            Utkast utkast = utkastRepository.findByIntygsIdAndIntygsTyp(intygId, intygType);
            final LocalDateTime created = utkast != null ? utkast.getSkapad() : null;

            return IntygContentHolder.builder()
                .contents(internalIntygJsonModel)
                .utlatande(certificate.getUtlatande())
                .statuses(certificate.getMetaData().getStatus())
                .revoked(certificate.isRevoked())
                .relations(certificateRelations)
                .created(created)
                .deceased(isDeceased(personId))
                .sekretessmarkering(sekretessmarkering)
                .patientNameChangedInPU(patientNameChanged)
                .patientAddressChangedInPU(patientAddressChanged)
                .testIntyg(utlatande.getGrundData().getPatient().isTestIndicator())
                .latestMajorTextVersion(intygTextsService.isLatestMajorVersion(intygType, intygTypeVersion))
                .build();

        } catch (IntygModuleFacadeException me) {
            // It's possible the Intygstjanst hasn't received the Intyg yet, look for it locally before rethrowing
            // exception
            Utkast utkast = utkastRepository.findByIntygsIdAndIntygsTyp(intygId, intygType);
            if (utkast == null) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
            }
            return buildIntygContentHolderFromUtkast(utkast);
        } catch (WebServiceException wse) {
            // Something went wrong communication-wise, try to find a matching Utkast instead.
            Utkast utkast = utkastRepository.findByIntygsIdAndIntygsTyp(intygId, intygType);
            if (utkast == null) {
                throw new WebCertServiceException(DATA_NOT_FOUND,
                    "Cannot get intyg. Intygstjansten was not reachable and the Utkast could "
                        + "not be found, perhaps it was issued by a non-webcert system?");
            }
            return buildIntygContentHolderFromUtkast(utkast);
        } catch (ModuleNotFoundException | ModuleException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
        }
    }

    // NOTE! INTYG-4086. This method is used when fetching Intyg/Utkast from WC locally.
    private IntygContentHolder buildIntygContentHolderFromUtkast(Utkast utkast) {

        try {
            // INTYG-4086: Patient object populated according to ruleset for the intygstyp at hand.
            Patient newPatientData = patientDetailsResolver.resolvePatient(utkast.getPatientPersonnummer(), utkast.getIntygsTyp(),
                utkast.getIntygTypeVersion());

            // Copied from getDraft in UtkastModuleApiController
            // INTYG-4086: Temporary, don't know if this is correct yet. If no patient was resolved,
            // create an "empty" Patient with personnummer only.
            if (newPatientData == null) {
                newPatientData = new Patient();
                newPatientData.setPersonId(utkast.getPatientPersonnummer());
            }

            Utlatande utlatande = moduleFacade.getUtlatandeFromInternalModel(utkast.getIntygsTyp(), utkast.getModel());

            // INTYG-5354, INTYG-5380: Don't use incomplete address from external data sources (PU/js).
            if (!newPatientData.isCompleteAddressProvided()) {
                // Use the old address data.
                Patient oldPatientData = utlatande.getGrundData().getPatient();
                copyOldAddressToNewPatientData(oldPatientData, newPatientData);
            }
            // INTYG-7449, INTYG-7529: Update patient data before (will not be done on intyg that store address data)
            String internalIntygJsonModel = moduleRegistry.getModuleApi(utkast.getIntygsTyp(), utkast.getIntygTypeVersion())
                .updateBeforeViewing(utkast.getModel(), newPatientData);

            utlatande = moduleFacade.getUtlatandeFromInternalModel(utkast.getIntygsTyp(), internalIntygJsonModel);
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
                .contents(internalIntygJsonModel)
                .utlatande(utlatande)
                .statuses(statuses)
                .revoked(utkast.getAterkalladDatum() != null)
                .relations(certificateRelations)
                .created(utkast.getSkapad())
                .deceased(isDeceased(utkast.getPatientPersonnummer()))
                .sekretessmarkering(sekretessmarkerad)
                .patientNameChangedInPU(patientNameChanged)
                .patientAddressChangedInPU(patientAddressChanged)
                .testIntyg(utkast.isTestIntyg())
                .latestMajorTextVersion(intygTextsService.isLatestMajorVersion(utlatande.getTyp(), utlatande.getTextVersion()))
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
        monitoringService.logIntygRevoked(intygsId, intyg.getTyp(), hsaId, reason);

        // First: send a notification informing stakeholders that this certificate has been revoked
        notificationService.sendNotificationForIntygRevoked(intygsId);

        certificateEventService.createCertificateEvent(intygsId, webCertUserService.getUser().getHsaId(), EventCode.MAKULERAT, reason);
        checkIfAddEventOnParent(intygsId);

        // Second: send a notification informing stakeholders that all questions related to the revoked
        // certificate has been closed.
        arendeService.closeAllNonClosedQuestions(intygsId);

        handleComplementedParent(intygsId);

        // Third: create a log event
        LogRequest logRequest = logRequestFactory.createLogRequestFromUtlatande(intyg);
        logService.logRevokeIntyg(logRequest);

        // Fourth: mark the originating Utkast as REVOKED
        markUtkastWithRevokedDate(intygsId);

        return IntygServiceResult.OK;
    }

    private void checkIfAddEventOnParent(String certificateId) {
        Relations relationsOfChild = intygRelationHelper.getRelationsForIntyg(certificateId);
        if (relationsOfChild != null) {
            WebcertCertificateRelation parent = relationsOfChild.getParent();
            if (parent != null && parent.getIntygsId() != null) {
                certificateEventService
                    .createCertificateEvent(parent.getIntygsId(), webCertUserService.getUser().getHsaId(), EventCode.RELINTYGMAKULE,
                        "Related certificate " + certificateId + " revoked");
            }
        }
    }

    private void handleComplementedParent(String intygsId) {
        Relations relationsOfChild = intygRelationHelper.getRelationsForIntyg(intygsId);
        if (isParentCompleted(relationsOfChild)
            && isLatestChildCompletionAndMatchIntygsId(relationsOfChild.getParent().getIntygsId(), intygsId)) {
            arendeService.reopenClosedCompletions(relationsOfChild.getParent().getIntygsId());
        }
    }

    private boolean isParentCompleted(Relations relationsOfChild) {
        return relationsOfChild != null
            && relationsOfChild.getParent() != null
            && relationsOfChild.getParent().getRelationKod() == RelationKod.KOMPLT;
    }

    private boolean isLatestChildCompletionAndMatchIntygsId(String intygsIdOfParent, String intygsIdToMatch) {
        Relations relationsOfParent = intygRelationHelper.getRelationsForIntyg(intygsIdOfParent);

        return relationsOfParent != null
            && relationsOfParent.getLatestChildRelations() != null
            && relationsOfParent.getLatestChildRelations().getComplementedByIntyg() != null
            && relationsOfParent.getLatestChildRelations().getComplementedByIntyg().getRelationKod() == RelationKod.KOMPLT
            && relationsOfParent.getLatestChildRelations().getComplementedByIntyg().getIntygsId().equals(intygsIdToMatch);
    }


    private void markUtkastWithSendDateAndRecipient(final Utkast foundUtkast, String intygsId, String recipient) {

        final Utkast utkast = (foundUtkast != null)
            ? foundUtkast
            : utkastRepository.findById(intygsId).orElse(null);

        if (utkast != null) {
            utkast.setSkickadTillMottagareDatum(LocalDateTime.now());
            utkast.setSkickadTillMottagare(recipient);
            utkastRepository.save(utkast);
        }
    }

    private void markUtkastWithRevokedDate(String intygsId) {
        Utkast utkast = utkastRepository.findById(intygsId).orElse(null);
        if (utkast != null) {

            utkast.setAterkalladDatum(LocalDateTime.now());
            utkastRepository.save(utkast);
        }
    }

    private boolean isDeceased(Personnummer personnummer) {
        return patientDetailsResolver.isAvliden(personnummer);
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
