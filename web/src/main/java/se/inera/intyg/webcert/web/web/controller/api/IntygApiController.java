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
package se.inera.intyg.webcert.web.web.controller.api;

import io.swagger.annotations.Api;
import jakarta.persistence.OptimisticLockException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.common.support.peristence.dao.util.DaoUtil;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.persistence.event.model.CertificateEvent;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.converter.CertificateEventConverter;
import se.inera.intyg.webcert.web.converter.IntygDraftsConverter;
import se.inera.intyg.webcert.web.event.CertificateEventService;
import se.inera.intyg.webcert.web.service.access.AccessEvaluationParameters;
import se.inera.intyg.webcert.web.service.access.CertificateAccessServiceHelper;
import se.inera.intyg.webcert.web.service.certificate.CertificateService;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.CertificateEventDTO;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.api.dto.NotifiedState;
import se.inera.intyg.webcert.web.web.controller.api.dto.QueryIntygParameter;
import se.inera.intyg.webcert.web.web.util.resourcelinks.ResourceLinkHelper;

/**
 * Controller for the API that serves WebCert.
 *
 * @author nikpet
 */
@Path("/intyg")
@Api(value = "intyg", produces = MediaType.APPLICATION_JSON)
public class IntygApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(IntygApiController.class);

    private static final List<UtkastStatus> ALL_DRAFTS = Arrays.asList(UtkastStatus.DRAFT_COMPLETE,
        UtkastStatus.DRAFT_INCOMPLETE, UtkastStatus.DRAFT_LOCKED);

    private static final String OFFLINE_MODE = "offline_mode";

    @Autowired
    private IntygService intygService;

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private UtkastService utkastService;

    @Autowired
    private CertificateEventService certificateEventService;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Autowired
    private AuthoritiesHelper authoritiesHelper;

    @Autowired
    private PatientDetailsResolver patientDetailsResolver;

    @Autowired
    private ResourceLinkHelper resourceLinkHelper;

    @Autowired
    private LogService logService;

    @Autowired
    private CertificateAccessServiceHelper certificateAccessServiceHelper;

    @Autowired
    private CertificateEventConverter certificateEventConverter;

    @Autowired
    private CertificateService certificateService;

    /**
     * Retrieves a list of all signed certificates for a doctor on the current logged in unit.
     *
     * @param queryParam Filter query including filters that user has chosen or default filters.
     * @return Response including list of all signed certificates for doctor and total number of certificates retrieved.
     */
    @GET
    @Path("/doctor/")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PerformanceLogging(eventAction = "intyg-get-signed-certificates-for-doctor", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response getSignedCertificatesForDoctor(@QueryParam("") QueryIntygParameter queryParam) {
        WebCertUser user = getWebCertUserService().getUser();
        LOG.debug("Fetching all signed intyg for doctor '{}' on unit '{}' from IT", user.getHsaId(), user.getValdVardenhet().getId());

        queryParam.setHsaId(user.getHsaId());
        final var unitIds = getEnhetIdsForCurrentUser();
        queryParam.setUnitIds(unitIds.toArray(new String[unitIds.size()]));

        final var certificateResponse = certificateService.listCertificatesForDoctor(queryParam);
        return Response.ok().entity(certificateResponse).build();
    }

    /**
     * Compiles a list of Intyg from two data sources. Signed Intyg are
     * retrieved from Intygstjänst, drafts are retrieved from Webcerts db. Both
     * types of Intyg are converted and merged into one sorted list.
     *
     * @param personNummerIn personnummer
     * @return a Response carrying a list containing all Intyg for a person.
     */
    @GET
    @Path("/person/{personNummer}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "intyg-list-drafts-and-certificates-for-person", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response listDraftsAndIntygForPerson(@PathParam("personNummer") String personNummerIn) {
        Personnummer personNummer = createPnr(personNummerIn);
        LOG.debug("Retrieving intyg for person {}", personNummer.getPersonnummerHash());

        // INTYG-4086 (epic) - make sure only users with HANTERA_SEKRETESSMARKERAD_PATIENT can list intyg for patient
        // with sekretessmarkering.
        SekretessStatus patientSekretess = patientDetailsResolver.getSekretessStatus(personNummer);
        if (patientSekretess == SekretessStatus.UNDEFINED) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.PU_PROBLEM,
                "Error checking sekretessmarkering state in PU-service.");
        }

        WebCertUser user = getWebCertUserService().getUser();
        authoritiesValidator.given(user)
            .privilegeIf(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT,
                patientSekretess == SekretessStatus.TRUE)
            .orThrow(new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM_SEKRETESSMARKERING,
                "User missing required privilege or cannot handle sekretessmarkerad patient"));

        List<String> enhetsIds = getEnhetIdsForCurrentUser();

        if (enhetsIds.isEmpty()) {
            LOG.error("Current user has no assignments");
            return Response.status(Status.BAD_REQUEST).build();
        }

        Pair<List<ListIntygEntry>, Boolean> intygItemListResponse = intygService.listIntyg(enhetsIds, personNummer);
        LOG.debug("Got #{} intyg", intygItemListResponse.getLeft().size());

        List<Utkast> utkastList;

        if (authoritiesValidator.given(user).features(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST)
            .isVerified()) {
            Set<String> intygstyper = authoritiesHelper.getIntygstyperForPrivilege(user,
                AuthoritiesConstants.PRIVILEGE_VISA_INTYG);

            utkastList = utkastRepository.findDraftsByPatientAndEnhetAndStatus(
                DaoUtil.formatPnrForPersistence(personNummer),
                enhetsIds,
                ALL_DRAFTS,
                intygstyper);

            LOG.debug("Got #{} utkast", utkastList.size());
        } else {
            utkastList = Collections.emptyList();
        }

        List<ListIntygEntry> allIntyg = IntygDraftsConverter.merge(intygItemListResponse.getLeft(), utkastList);

        // INTYG-4477
        if (patientSekretess == SekretessStatus.TRUE) {
            Set<String> allowedTypes = authoritiesHelper.getIntygstyperAllowedForSekretessmarkering();
            allIntyg = allIntyg.stream().filter(intyg -> allowedTypes.contains(intyg.getIntygType())).collect(Collectors.toList());
        }

        resourceLinkHelper.decorateIntygWithValidActionLinks(allIntyg, personNummer);

        Response.ResponseBuilder responseBuilder = Response.ok(allIntyg);
        if (intygItemListResponse.getRight()) {
            responseBuilder = responseBuilder.header(OFFLINE_MODE, Boolean.TRUE.toString());
        }

        // PDL Logging
        logService.logListIntyg(user, personNummer.getPersonnummerWithDash());

        return responseBuilder.build();
    }

    /**
     * Sets the notified flag on an Intyg.
     *
     * @param intygsId Id of the Intyg
     * @param notifiedState True or False
     * @return Response
     */
    @PUT
    @Path("/{intygsTyp}/{intygsId}/{version}/vidarebefordra")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Consumes(MediaType.APPLICATION_JSON)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "intyg-set-notified-on-certificate", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public Response setNotifiedOnIntyg(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
        @PathParam("version") long version, NotifiedState notifiedState) {

        Utkast updatedIntyg;
        try {
            updatedIntyg = utkastService.setNotifiedOnDraft(intygsId, version, notifiedState.isNotified());
        } catch (OptimisticLockException | OptimisticLockingFailureException e) {
            monitoringLogService.logUtkastConcurrentlyEdited(intygsId, intygsTyp);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.CONCURRENT_MODIFICATION, e.getMessage());
        }

        LOG.debug("Set forward to {} on intyg {} with id '{}'",
            new Object[]{updatedIntyg.getVidarebefordrad(), intygsTyp, updatedIntyg.getIntygsId()});

        ListIntygEntry intygEntry = IntygDraftsConverter.convertUtkastToListIntygEntry(updatedIntyg);

        return Response.ok(intygEntry).build();
    }

    @PUT
    @Path("/{intygsTyp}/{intygsId}/{version}/redoattsignera")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Consumes(MediaType.APPLICATION_JSON)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "intyg-set-ready-for-sign-and-send-status-message", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public Response setKlarForSigneraAndSendStatusMessage(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
        @PathParam("version") long version) {

        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
            .privilege(AuthoritiesConstants.PRIVILEGE_NOTIFIERING_UTKAST)
            .orThrow();

        utkastService.setKlarForSigneraAndSendStatusMessage(intygsId, intygsTyp);
        return Response.ok().build();
    }

    private Personnummer createPnr(String pnr) {
        return Personnummer.createPersonnummer(pnr)
            .orElseThrow(() -> new WebCertServiceException(WebCertServiceErrorCodeEnum.MISSING_PARAMETER,
                String.format("Cannot create Personnummer object with invalid personId %s", pnr)));
    }

    /**
     * Compiles a list of events for a certificate.
     *
     * @param certificateId Id of the certificate
     * @return a Response carrying a list containing all events for certificate
     */
    @GET
    @Path("/{certificateId}/events")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "intyg-get-events-for-certificate", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response getEventsForCertificate(@PathParam("certificateId") String certificateId) {

        List<CertificateEvent> eventList = certificateEventService.getCertificateEvents(certificateId);

        if (eventList == null) {
            LOG.error("Could not complete the request for certificate with id {}", certificateId);
            return Response.status(Status.BAD_REQUEST).build();
        }

        if (!eventList.isEmpty()) {
            LOG.debug("Got {} events for certificate with id {}", eventList.size(), certificateId);
        } else if (eventList.isEmpty()) {
            LOG.debug("No events for certificate with id {}", certificateId);
        }

        List<CertificateEventDTO> eventDTOList = eventList
            .stream()
            .map(event -> certificateEventConverter.convertToCertificateEventDTO(event))
            .collect(Collectors.toList());

        return Response.ok(eventDTOList).build();
    }

    @GET
    @Path("/intygTypeVersion/{intygsId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Consumes(MediaType.APPLICATION_JSON)
    @PerformanceLogging(eventAction = "intyg-get-certificate-type-version", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response getIntygTypeVersion(@PathParam("intygsId") String intygsId) {

        return Response.ok(intygService.getIntygTypeInfo(intygsId)).build();

    }

    @GET
    @Path("/allowToApprovedReceivers/{intygsId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Consumes(MediaType.APPLICATION_JSON)
    @PerformanceLogging(eventAction = "intyg-get-allow-to-approve-receivers", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response getAllowToApprovedReceivers(@PathParam("intygsId") String intygsId) {

        final var utkast = utkastRepository.findById(intygsId).orElse(null);

        if (utkast != null) {
            final var vardenhet = getVardenhet(utkast);

            final var params = AccessEvaluationParameters.create(utkast.getIntygsTyp(),
                utkast.getIntygTypeVersion(), vardenhet, utkast.getPatientPersonnummer(), utkast.isTestIntyg());

            final var accessResult = certificateAccessServiceHelper.validateAccessToApproveReceivers(params);

            return Response.ok(accessResult.isAllowed()).build();
        } else {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "Could not find certificate!");
        }
    }

    private Vardenhet getVardenhet(Utkast utkast) {
        final Vardgivare vardgivare = new Vardgivare();
        vardgivare.setVardgivarid(utkast.getVardgivarId());

        final Vardenhet vardenhet = new Vardenhet();
        vardenhet.setEnhetsid(utkast.getEnhetsId());
        vardenhet.setVardgivare(vardgivare);

        return vardenhet;
    }

}
