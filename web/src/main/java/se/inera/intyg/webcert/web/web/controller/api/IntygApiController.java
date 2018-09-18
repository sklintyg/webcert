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
package se.inera.intyg.webcert.web.web.controller.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.OptimisticLockException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;

import io.swagger.annotations.Api;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.peristence.dao.util.DaoUtil;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.converter.IntygDraftsConverter;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.api.dto.NotifiedState;

/**
 * Controller for the API that serves WebCert.
 *
 * @author nikpet
 */
@Path("/intyg")
@Api(value = "intyg", description = "REST API för intygshantering", produces = MediaType.APPLICATION_JSON)
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
    private MonitoringLogService monitoringLogService;

    @Autowired
    private AuthoritiesHelper authoritiesHelper;

    @Autowired
    private PatientDetailsResolver patientDetailsResolver;

    /**
     * Compiles a list of Intyg from two data sources. Signed Intyg are
     * retrieved from Intygstjänst, drafts are retrieved from Webcerts db. Both
     * types of Intyg are converted and merged into one sorted list.
     *
     * @param personNummerIn
     *            personnummer
     * @return a Response carrying a list containing all Intyg for a person.
     */
    @GET
    @Path("/person/{personNummer}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
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

        authoritiesValidator.given(getWebCertUserService().getUser())
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

        if (authoritiesValidator.given(getWebCertUserService().getUser()).features(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST)
                .isVerified()) {
            Set<String> intygstyper = authoritiesHelper.getIntygstyperForPrivilege(getWebCertUserService().getUser(),
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

        Response.ResponseBuilder responseBuilder = Response.ok(allIntyg);
        if (intygItemListResponse.getRight()) {
            responseBuilder = responseBuilder.header(OFFLINE_MODE, Boolean.TRUE.toString());
        }

        return responseBuilder.build();
    }

    /**
     * Sets the notified flag on an Intyg.
     *
     * @param intygsId
     *            Id of the Intyg
     * @param notifiedState
     *            True or False
     * @return Response
     */
    @PUT
    @Path("/{intygsTyp}/{intygsId}/{version}/vidarebefordra")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setNotifiedOnIntyg(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
            @PathParam("version") long version, NotifiedState notifiedState) {
        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                .features(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST)
                .privilege(AuthoritiesConstants.PRIVILEGE_VIDAREBEFORDRA_UTKAST)
                .orThrow();

        Utkast updatedIntyg;
        try {
            updatedIntyg = utkastService.setNotifiedOnDraft(intygsId, version, notifiedState.isNotified());
        } catch (OptimisticLockException | OptimisticLockingFailureException e) {
            monitoringLogService.logUtkastConcurrentlyEdited(intygsId, intygsTyp);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.CONCURRENT_MODIFICATION, e.getMessage());
        }

        LOG.debug("Set forward to {} on intyg {} with id '{}'",
                new Object[] { updatedIntyg.getVidarebefordrad(), intygsTyp, updatedIntyg.getIntygsId() });

        ListIntygEntry intygEntry = IntygDraftsConverter.convertUtkastToListIntygEntry(updatedIntyg);

        return Response.ok(intygEntry).build();
    }

    @PUT
    @Path("/{intygsTyp}/{intygsId}/{version}/redoattsignera")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Consumes(MediaType.APPLICATION_JSON)
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

    @GET
    @Path("/intygTypeVersion/{intygsId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getIntygTypeVersion(@PathParam("intygsId") String intygsId) {

        return Response.ok(intygService.getIntygTypeInfo(intygsId)).build();

    }

}
